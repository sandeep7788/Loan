package com.cbi_solar


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.MainActivity
import com.cbi_solar.cbisolar.R
import com.cbi_solar.cbisolar.Utility
import com.cbi_solar.cbisolar.databinding.ActivitySalaryFormBinding
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.FileUtils
import com.cbi_solar.helper.RetrofitManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

lateinit var binding: ActivitySalaryFormBinding
private var loans = 0;
private var bankdetails = 0;
private var familyincome = 0;

class SalaryForm : AppCompatActivity() {

    var count: Int = 0;
    var srtarr: StringBuilder = StringBuilder()
    var latitudeTextView: Double? = null
    var longitudeTextView: Double? = null
    var addressTextView: String? = " "
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var PERMISSION_ID: Int = 44
    var TAG = "@@TAG"

    fun uploadImage(uri: Uri) {

        try {
            if (uri == null) {
                setList()
            }
        } catch (e: Exception) {
            setList()
        }
        try {
            progressDialog.show()

            val file = File(FileUtils.getPath(this@SalaryForm, uri))
            addTimestampToImage(file)

            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val parts = MultipartBody.Part.createFormData("image", file.name, requestFile)


            val apiInterface: ApiInterface =
                RetrofitManager().instance1!!.create(ApiInterface::class.java)

            apiInterface.uploadImage(parts, "describtion")?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                    Log.e("@@TAG", "onFailure: " + t.message.toString())
                    count = 0
                    Toast.makeText(
                        this@SalaryForm, "Error ", Toast.LENGTH_LONG
                    ).show()
                }

                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    try {
                        Log.e(
                            "@@TAG",
                            "onResponse: " + response.body() + " " + count + " " + selectedImages.size
                        )
                        Toast.makeText(
                            this@SalaryForm, "" + response.body(), Toast.LENGTH_LONG
                        ).show()

                        val jsonObject = JSONObject(response.body().toString())

                        if (jsonObject.getString("status").equals("success")) {
                            srtarr.append(jsonObject.getString("image_name")).append(",")

                            if (count < selectedImages.size) {
                                count++;
                                uploadImage(selectedImages.get(count))
                            } else {
                                setList()
                            }
                            Toast.makeText(
                                this@SalaryForm, " Image processing: $count", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (progressDialog!!.isShowing) {
                                progressDialog!!.dismiss()
                            }
                            count = 0
                            Toast.makeText(
                                this@SalaryForm,
                                " " + jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } catch (e: Exception) {
                        setList()

                    }


                }

            })
        } catch (e: Exception) {
            Toast.makeText(
                this@SalaryForm, " " + e.message, Toast.LENGTH_SHORT
            ).show()
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    private val selectedImages = ArrayList<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    val CAMERA_PERMISSION_CODE = 100
    val spinnerfamilystatus = arrayOf("Select Family Status", "Joint Family", "Nuclear Family")
    val houseStatusOptions = arrayOf("Select House Status", "Owned", "Rented")
    lateinit var houseStatusAdapter: ArrayAdapter<String>
    lateinit var Adapter_spinnerfamilystatus: ArrayAdapter<String>

    @SuppressLint("UseCheckPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_salary_form)

        if (intent.extras != null) {
            case_id = intent.getStringExtra("case_id").toString()
        }


        val employmentStatusOptions = arrayOf("Joint Family", "Nuclear Family")
        val adapter = ArrayAdapter(this, R.layout.list_item, employmentStatusOptions)
        binding.spinnerHouseStatus.setAdapter(adapter)

        houseStatusAdapter =
            ArrayAdapter(this, R.layout.simple_spinner_item, houseStatusOptions)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHouseStatus.adapter = houseStatusAdapter

        binding.spinnerHouseStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    binding.houseValueRentEditTextLayout.visibility = View.VISIBLE
                    val selectedItem = houseStatusOptions[position]
//                Toast.makeText(this@SalaryForm, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                    if (selectedItem.equals("Owned")) {
                        binding.houseValueRentEditText.setHint("Value of House")
                    } else if (selectedItem.equals("Rented")) binding.houseValueRentEditText.setHint(
                        "Rent of House"
                    ) else {
                        binding.houseValueRentEditTextLayout.visibility = View.GONE
                        binding.houseValueRentEditText.setText("")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.houseValueRentEditTextLayout.visibility = View.GONE
                }
            }

        Adapter_spinnerfamilystatus =
            ArrayAdapter(this, R.layout.simple_spinner_item, spinnerfamilystatus)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus

//        binding.txtVisitedDate.setOnClickListener { setDate(binding.txtVisitedDate) }
        binding.txtDateofjoin.setOnClickListener { setDate(binding.txtDateofjoin) }
        setCurrentDate(binding.txtVisitedDate)
        setCurrentTime(binding.visitedTime)

        binding.buttonAddLoans.setOnClickListener {
            addLoanField("", "", "", "")
        }
        binding.buttonAddFamilyIncome.setOnClickListener {
            addFamilyIncomeField("", "", "")

        }
        binding.buttonAddBank.setOnClickListener {
            addBackField("", "", "", "")
        }

        binding.submitButton.setOnClickListener {
            Log.e("@@TAG", "setList: loanDataArray " + bankdetailsDataArray.toString())
            Log.e("@@TAG", "setList: loanDataArray " + loanDataArray.toString())

            if (true) {

                val dialog =
                    SweetAlertDialog(this@SalaryForm, SweetAlertDialog.WARNING_TYPE).apply {
//                    setTitleText("Confirm")
                        setContentText("All set to submit?")
                        setConfirmText("Continue")
                        setCancelText("Cancel")

                        setConfirmClickListener {
                            it.dismissWithAnimation()
                            progressDialog?.show()

                            if (selectedImages.isNotEmpty()) {
                                uploadImage(selectedImages[count])
                            } else {
                                setList()
                            }
                        }

                        setCancelClickListener {
                            it.dismissWithAnimation()
                        }
                    }


// 🔧 Style button after the dialog is shown
                dialog.window?.decorView?.post {
                    val confirmButton = dialog.findViewById<Button>(R.id.confirm_button)
                    val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
                    confirmButton?.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.green_color))
                        // Optional: setTextColor(Color.WHITE)
                    }

                    cancelButton?.apply {
                        setBackgroundColor(
                            ContextCompat.getColor(
                                context, R.color.red_btn_bg_color
                            )
                        )
                        setTextColor(Color.WHITE) // optional
                    }
                }
                dialog.show()


//            val alertDialog = AlertDialog.Builder(this)
//                .setTitle("You Want to Submit?")
//                .setMessage("Are you sure you want to submit?")
//                .setPositiveButton("OK") { dialog, which ->
//
//                }
//                .setNegativeButton("Cancel") { dialog, which ->
//                    dialog.dismiss()
//                }
//                .setCancelable(false)  // Prevent closing the dialog by tapping outside
//                .create()
//
//            // Show the dialog
//            alertDialog.show()

            }
        }

        val storage: TextView? = findViewById(R.id.storage)

        storage?.setOnClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE, CAMERA_PERMISSION_CODE
            )
            binding.image.setText("Selected Images: " + selectedImages.size)
        }

        imageAdapter = ImageAdapter(selectedImages)
        binding.recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages.adapter = imageAdapter

        setlocation()
        Handler(Looper.getMainLooper()).postDelayed({
            setlocation()
        }, 6 * 60 * 1000)

        progressDialog = SweetAlertDialog(
            ContextThemeWrapper(
                this, com.cbi_solar.cbisolar.R.style.ThemeOverlay_MaterialComponents_Dialog
            ), SweetAlertDialog.PROGRESS_TYPE
        )
        progressDialog.setTitleText("Loading...")
        progressDialog.setCancelable(false)


        binding.submitSave.setOnClickListener {
            saveFormData(binding, case_id, this@SalaryForm, true)
        }
        loadFormData(case_id)
    }

    lateinit var progressDialog: SweetAlertDialog

    fun setlocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun getAddressFromLocation(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)  // Full address
                val city = addresses[0].locality              // City
                val country = addresses[0].countryName        // Country
                addLat = lat.toString()
                addLong = lon.toString()
                addAddress = "Address: $address\nCity: $city\nCountry: $country"

                addressTextView = "Address: $address\nCity: $city\nCountry: $country"
            } else {
                addressTextView = "Location not found!"
            }
//            Toast.makeText(this, "addressTextView "+addressTextView, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("@@TAG", "getAddressFromLocation: " + e.message)
            addressTextView = "Error fetching location"
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitudeTextView = location.latitude
                        longitudeTextView = location.longitude
                        getAddressFromLocation(latitudeTextView!!, longitudeTextView!!)
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            latitudeTextView = lastLocation.latitude
            longitudeTextView = lastLocation.longitude
            getAddressFromLocation(latitudeTextView!!, longitudeTextView!!)
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Show dialog explaining why location is needed
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSION_ID
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this).setTitle("Location Permission Needed")
            .setMessage("This app requires location access to provide better services. Please allow it.")
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), PERMISSION_ID
                )
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun validateFields(): Boolean {
        val context = this@SalaryForm // Replace with your actual Activity or Context reference

        fun isEmptyField(value: String?, message: String): Boolean {
            if (value.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return true
            }
            return false
        }

        if (isEmptyField(case_id, "Case ID is required")) return false
        if (isEmptyField(
                binding.loanAmountEditText.text.toString(),
                "Loan amount is required"
            )
        ) return false
        if (isEmptyField(
                binding.visitedAddress.text.toString(),
                "Visited address is required"
            )
        ) return false
        if (isEmptyField(
                binding.txtVisitedDate.text.toString(),
                "Date of visit is required"
            )
        ) return false
        if (isEmptyField(
                binding.visitedTime.text.toString(),
                "Visit time is required"
            )
        ) return false
        if (isEmptyField(
                binding.persontmeetnameEditText.text.toString(),
                "Person met is required"
            )
        ) return false
        if (isEmptyField(
                binding.loanPurposeEditText.text.toString(),
                "Loan purpose is required"
            )
        ) return false
        if (isEmptyField(
                binding.orgnization.text.toString(),
                "Name of organization is required"
            )
        ) return false
        if (isEmptyField(
                binding.workexprience.text.toString(),
                "Work experience is required"
            )
        ) return false
        if (isEmptyField(binding.education.text.toString(), "Education is required")) return false
        if (isEmptyField(
                binding.numberoffamilynumber.text.toString(),
                "Number of family members is required"
            )
        ) return false

        if (addLat == null || addLong == null || addAddress.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Location details (lat/long/address) are missing",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (isEmptyField(
                binding.familymonthlyExpenditure.text.toString(),
                "Monthly family expenditure is required"
            )
        ) return false
        if (binding.spinnerFamilystatus.selectedItemPosition == 0) {
            Toast.makeText(context, "Please select family status", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.spinnerHouseStatus.selectedItemPosition == 0) {
            Toast.makeText(context, "Please select house status", Toast.LENGTH_SHORT).show()
            return false
        }
        if (isEmptyField(
                binding.editTextHouseSize.text.toString(),
                "House size is required"
            )
        ) return false
        if (isEmptyField(
                binding.editTextResidenceSince.text.toString(),
                "Residence since is required"
            )
        ) return false
        if (isEmptyField(
                binding.houseValueRentEditText.text.toString(),
                "Value/Rent of house is required"
            )
        ) return false
        if (isEmptyField(
                binding.grossSalaryEditText.text.toString(),
                "Gross salary is required"
            )
        ) return false
        if (isEmptyField(
                binding.netSalaryEditText.text.toString(),
                "Net salary is required"
            )
        ) return false
        if (isEmptyField(
                binding.currentPositionEditText.text.toString(),
                "Current position is required"
            )
        ) return false
        if (isEmptyField(
                binding.txtDateofjoin.text.toString(),
                "Date of joining is required"
            )
        ) return false
        if (isEmptyField(
                binding.otherIncomeEditText.text.toString(),
                "Other income is required"
            )
        ) return false
        if (isEmptyField(
                binding.previousEmploymentEditText.text.toString(),
                "Previous employment details are required"
            )
        ) return false
        if (isEmptyField(
                binding.officeTimingsEditText.text.toString(),
                "Office timings are required"
            )
        ) return false
        if (isEmptyField(
                binding.holidayEditText.text.toString(),
                "Holiday info is required"
            )
        ) return false
        if (isEmptyField(
                binding.panNumberEditText.text.toString(),
                "PAN number is required"
            )
        ) return false
        if (isEmptyField(
                binding.employerNameEditText.text.toString(),
                "Employer name is required"
            )
        ) return false
        if (isEmptyField(
                binding.employerNumberEditText.text.toString(),
                "Employer mobile no is required"
            )
        ) return false
        if (isEmptyField(
                binding.coEmployeeNameEditText.text.toString(),
                "Co-employee name is required"
            )
        ) return false
        if (isEmptyField(
                binding.coEmployeeNumber.text.toString(),
                "Co-employee mobile no is required"
            )
        ) return false
        if (isEmptyField(
                binding.securityOfferedEditText.text.toString(),
                "Security offered is required"
            )
        ) return false
        if (isEmptyField(
                binding.addressOfSecurityEditText.text.toString(),
                "Address of security is required"
            )
        ) return false
        if (isEmptyField(
                binding.valueOfSecurityEditText.text.toString(),
                "Security value is required"
            )
        ) return false
        if (isEmptyField(
                binding.editTextHouseSize.text.toString(),
                "Size of security is required"
            )
        ) return false
        if (familyIncomeDataArray.size() == 0) {
            Toast.makeText(context, "Please add earning family members", Toast.LENGTH_SHORT).show()
            return false
        }
        if (bankdetailsDataArray.size() == 0) {
            Toast.makeText(context, "Please add bank details", Toast.LENGTH_SHORT).show()
            return false
        }
        if (loanDataArray.size() == 0) {
            Toast.makeText(context, "Please add current loans", Toast.LENGTH_SHORT).show()
            return false
        }
        if (isEmptyField(binding.Assets.text.toString(), "Assets owned is required")) return false
        if (srtarr.isEmpty()) {
            Toast.makeText(context, "Please upload at least one image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@SalaryForm, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@SalaryForm, arrayOf(permission), requestCode)
        } else {
//            openImagePicker()
            openCamera()
//            Toast.makeText(this@SalaryForm, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@SalaryForm, "Camera Permission Granted", Toast.LENGTH_SHORT)
                    .show()
//                openImagePicker()
                openCamera()
            } else {
                Toast.makeText(this@SalaryForm, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // "Don't ask again" selected
                    showSettingsDialog()
                } else {
                    Toast.makeText(this, "Location Permission denied!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("You have denied location permission. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission still denied!", Toast.LENGTH_SHORT).show()
                finish();
            }.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastLocation()
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
//                selectedImages.clear()


                selectedImages.addAll(uris)
                imageAdapter.notifyDataSetChanged()  // Refresh RecyclerView
                binding.image.setText("Selected Images: "+selectedImages.size)
            }
        }

    fun addTimestampToImage(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IllegalArgumentException("Invalid image file")

        val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) + " lat:" + addLat + " long:" + addLat
        val addressText = addressTextView.toString().substring(0, addressTextView.toString().length-1 /2)
        val extraInfo = addressTextView.toString().substring(addressTextView.toString().length /2, addressTextView.toString().length-1)

        val padding = 20f
        val lineSpacing = 10f

        val boundsAddress = Rect()
        val boundsTimestamp = Rect()
        val boundsExtra = Rect()

        textPaint.getTextBounds(addressText, 0, addressText.length, boundsAddress)
        textPaint.getTextBounds(timeStamp, 0, timeStamp.length, boundsTimestamp)
        textPaint.getTextBounds(extraInfo, 0, extraInfo.length, boundsExtra)

        val maxTextWidth = maxOf(boundsAddress.width(), boundsTimestamp.width(), boundsExtra.width())
        val totalTextHeight = boundsAddress.height() + boundsTimestamp.height() + boundsExtra.height() + (2 * lineSpacing)

        val x = tempBitmap.width - maxTextWidth - padding
        val yStart = tempBitmap.height - totalTextHeight - padding

        val rectLeft = x - padding
        val rectTop = yStart - padding
        val rectRight = x + maxTextWidth + padding
        val rectBottom = tempBitmap.height.toFloat()

        val backgroundPaint = Paint().apply {
            color = Color.BLACK
            alpha = 150
        }

        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, backgroundPaint)

        val yAddress = yStart + boundsAddress.height()
        val yTimestamp = yAddress + boundsTimestamp.height() + lineSpacing
        val yExtra = yTimestamp + boundsExtra.height() + lineSpacing

        canvas.drawText(addressText, x, yAddress, textPaint)
        canvas.drawText(timeStamp, x, yTimestamp, textPaint)
        canvas.drawText(extraInfo, x, yExtra, textPaint)

        FileOutputStream(file).use { out ->
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }


    /*
        fun addTimestampToImage(file: File) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ?: throw IllegalArgumentException("Invalid image file")

            val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(tempBitmap)

            // Set up Paint for text
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 80f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

            val textBounds = Rect()
            textPaint.getTextBounds(addressTextView, 0, addressTextView!!.length, textBounds)

            // Define position (bottom-right)
            val padding = 20f
            val x = tempBitmap.width - textBounds.width() - padding
            val y = tempBitmap.height - padding

            // Draw a transparent black background rectangle
            val backgroundPaint = Paint().apply {
                color = Color.BLACK
                alpha = 150 // Adjust transparency (0 = fully transparent, 255 = solid black)
            }
            val rectLeft = x - padding
            val rectTop = y - textBounds.height() - padding
            val rectRight = x + textBounds.width() + padding
            val rectBottom = y + padding

            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, backgroundPaint)

            // Draw timestamp on top of the background
            canvas.drawText(addressTextView.toString(), x, y, textPaint)

            // Overwrite the original file
            FileOutputStream(file).use { out ->
                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }
    */

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private var imageUri: Uri? = null
    lateinit var arr: ArrayList<String>

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                // Image successfully captured
                Toast.makeText(this, "Image captured: $imageUri", Toast.LENGTH_SHORT).show()
                if (imageUri != null) {

                    selectedImages.add(imageUri!!)
                    imageAdapter.notifyDataSetChanged()
                    binding.image.setText("Selected Images: "+selectedImages.size)
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun ensureCorrectOrientation(context: Context, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        val orientation = ExifInterface.ORIENTATION_NORMAL

        // Modify the matrix based on the EXIF orientation
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return if (matrix.isIdentity) {
            bitmap // No rotation needed, return as is
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }


    fun openCamera() {
        if (checkCameraPermission()) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (imageUri != null) {
                cameraLauncher.launch(imageUri)
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestCameraPermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show()
                showCameraSettingsDialog()
            }
        }
    private fun showCameraSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("You have denied camera permission. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission still denied!", Toast.LENGTH_SHORT).show()
                finish();
            }.show()
    }
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var case_id = "";
    var addLat = "";
    var addLong = "";
    var addAddress = "";
    private fun getDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1 // Months are 0-based
        val year = calendar[Calendar.YEAR]

        val date = "$day/$month/$year"
        return date
    }

    private fun getTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

//        txtView.text = String.format("%02d:%02d:%02d", hour, minute, second)
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }

    var deviceID = ""
    fun setList() {

        try {
            deviceID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            saveFormData(binding, case_id, this@SalaryForm,false)
        } catch (e: Exception) {
        }

        progressDialog.show()
        val requestBody = JsonObject()
//        requestBody.addProperty("case_id", "42071")
        requestBody.addProperty("case_id", case_id)
        requestBody.addProperty("loan_amt", binding.loanAmountEditText.text.toString())
        requestBody.addProperty("visit_address", binding.visitedAddress.text.toString())
        requestBody.addProperty("date_of_visit", binding.txtVisitedDate.text.toString())
        requestBody.addProperty("time", binding.visitedTime.text.toString())
        requestBody.addProperty("person_meet", binding.persontmeetnameEditText.text.toString())
        requestBody.addProperty("loan_purpose", binding.loanPurposeEditText.text.toString())
        requestBody.addProperty("name_of_organization", binding.orgnization.text.toString())
        requestBody.addProperty("work_experience", binding.workexprience.text.toString())
        requestBody.addProperty("education", binding.education.text.toString())
        requestBody.addProperty("size_of_security", binding.sizeOfSecurityEditText.text.toString())
        requestBody.addProperty(
            "number_of_family_members", binding.numberoffamilynumber.text.toString()
        )
        requestBody.addProperty(
            "lat", addLat
        )
        requestBody.addProperty(
            "long", addLong
        )
        requestBody.addProperty(
            "location", addAddress
        )
        requestBody.addProperty(
            "submitTime", getTime()
        )
        requestBody.addProperty(
            "submitDate", getDate()
        )
        requestBody.addProperty(
            "deviceID", deviceID.toString()
        )
        requestBody.addProperty(
            "monthly_family_expenditure", binding.familymonthlyExpenditure.text.toString()
        )
        requestBody.addProperty(
            "family_status",
            spinnerfamilystatus.get(binding.spinnerFamilystatus.selectedItemPosition)
        )
        requestBody.addProperty(
            "house_status", houseStatusOptions.get(binding.spinnerHouseStatus.selectedItemPosition)
        )
        requestBody.addProperty("house_size", binding.editTextHouseSize.text.toString())
        requestBody.addProperty(
            "residence_at_address_since", binding.editTextResidenceSince.text.toString()
        )

        requestBody.addProperty("gross_salary", binding.grossSalaryEditText.text.toString())
        requestBody.addProperty("net_salary", binding.netSalaryEditText.text.toString())
        requestBody.addProperty(
            "current_position_of_employee", binding.currentPositionEditText.text.toString()
        )
        requestBody.addProperty("date_of_joining", binding.txtDateofjoin.text.toString())
        requestBody.addProperty(
            "other_income_of_applicant", binding.otherIncomeEditText.text.toString()
        )
        requestBody.addProperty(
            "previous_employement_details", binding.previousEmploymentEditText.text.toString()
        )
        requestBody.addProperty("office_timings", binding.officeTimingsEditText.text.toString())
        requestBody.addProperty("holiday", binding.holidayEditText.text.toString())
        requestBody.addProperty("pan_number", binding.panNumberEditText.text.toString())
//        requestBody.addProperty("office_setup_seen", binding.businessSetupSeenEditText.text.toString())
        requestBody.addProperty(
            "name_board_seen", if (binding.radioButton1One.isSelected) "Yes" else "No"
        )
        requestBody.addProperty("employer_name", binding.employerNameEditText.text.toString())
        requestBody.addProperty(
            "employer_mobile_no", binding.employerNumberEditText.text.toString()
        )
        requestBody.addProperty("co_employee_name", binding.coEmployeeNameEditText.text.toString())
        requestBody.addProperty("co_employee_mobile_no", binding.coEmployeeNumber.text.toString())
        requestBody.addProperty(
            "security_offered_against_loan", binding.securityOfferedEditText.text.toString()
        )
        requestBody.addProperty(
            "address_of_security", binding.addressOfSecurityEditText.text.toString()
        )
        requestBody.addProperty("security_value", binding.valueOfSecurityEditText.text.toString())
        requestBody.addProperty("size_of_security", binding.editTextHouseSize.text.toString())
        requestBody.addProperty(
            "neighbour_check_status",
            if (binding.EnterNeighborCheckStatusone.isSelected) "Positive" else "Negative"
        )
        requestBody.addProperty(
            "business_setup", if (binding.radioButtonOfficeSetup.isSelected) "Good" else "Average"
        )

        requestBody.addProperty(
            "earning_family_members", familyIncomeDataArray.toString()
        )

        requestBody.addProperty(
            "bank_details", bankdetailsDataArray.toString()
        )

        Log.e("@@TAG", "setList: loanDataArray " + loanDataArray)

        requestBody.addProperty(
            "current_loans", loanDataArray.toString()
        )
        requestBody.addProperty(
            "salary_received", if (binding.SalaryReceivedInone.isSelected) "Bank" else "Cash"
        )
        requestBody.addProperty("assets_owned", binding.Assets.text.toString())
        requestBody.addProperty("image_name", srtarr.toString())
        requestBody.addProperty("file_name", srtarr.toString())

        if (binding.spinnerHouseStatus.selectedItemPosition==1) {
            requestBody.addProperty("value_of_house", binding.houseValueRentEditText.text.toString())
        }else if (binding.spinnerHouseStatus.selectedItemPosition==2) {
            requestBody.addProperty("rent_of_house", binding.houseValueRentEditText.text.toString())
        }

        val apiInterface: ApiInterface =
            RetrofitManager().instance1!!.create(ApiInterface::class.java)

        Log.e("@@TAG--------------", requestBody.toString());

        apiInterface.submitLoanDetails(requestBody)?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }

                Log.e("@@TAG", "onFailure: " + t.message.toString())
                Toast.makeText(
                    this@SalaryForm, " " + t.message.toString(), Toast.LENGTH_LONG
                ).show()
            }

            override fun onResponse(
                call: Call<JsonObject>, response: Response<JsonObject>
            ) {
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
                Log.e("@@TAG", "onResponse: " + response.body())
                try {


                    val jsonObject = JSONObject(response.body().toString())

                    if (jsonObject.getBoolean("status")) {

                        val successDialog = SweetAlertDialog(
                            ContextThemeWrapper(
                                this@SalaryForm, R.style.ThemeOverlay_MaterialComponents_Dialog
                            ), SweetAlertDialog.SUCCESS_TYPE
                        )

                        successDialog.setTitleText("Success!")
                            .setContentText("Details submitted successfully").setConfirmText("OK")
                            .setConfirmClickListener {
                                it.dismissWithAnimation()

                                this@SalaryForm.startActivity(
                                    Intent(
                                        this@SalaryForm, MainActivity::class.java
                                    )
                                )
                                finish()

                            }.show()

//                        successDialog.setOnShowListener {
                        val confirmButton =
                            successDialog.findViewById<TextView>(R.id.confirm_button)
                        confirmButton?.setBackgroundColor(Color.parseColor("#FF6200EE")) // Use your custom color
                        confirmButton?.setTextColor(Color.BLACK) // Ensure text is visible
                        confirmButton?.setBackgroundColor(Color.GREEN)
//                        }
//                        successDialog.setConfirmButtonBackgroundColor(Color.parseColor("#FF6200EE"))
//                        successDialog.setConfirmButton("OK", SweetAlertDialog::dismissWithAnimation)

//                        val dialog = Dialog(this@SalaryForm)
//                        dialog.setContentView(R.layout.success_dialog)
//                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Remove background corners
//
//                        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)
//                        btnOk.setOnClickListener {
//                            dialog.dismiss() // Close dialog on button click
//
//                        }
//
//                        dialog.show()


                    }
                } catch (e: JSONException) {
                    Log.e("@@TAG", "onResponse: " + e.message)
                    e.printStackTrace()
                    Toast.makeText(
                        this@SalaryForm,
                        " " + resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    ).show()
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                }


            }

        })
    }

    private var loanDataArray = JsonArray()
    private var bankdetailsDataArray = JsonArray()
    private var familyIncomeDataArray = JsonArray()
    var date: Calendar = Calendar.getInstance()
    var thisAYear = date.get(Calendar.YEAR).toInt()
    var thisAMonth = date.get(Calendar.MONTH).toInt()
    var thisADay = date.get(Calendar.DAY_OF_MONTH).toInt()

    private fun setCurrentDate(txtView: TextView) {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1 // Months are 0-based
        val year = calendar[Calendar.YEAR]

        val date = "$day/$month/$year"
        txtView.text = date
    }

    private fun setCurrentTime(txtView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        txtView.text = String.format("%02d:%02d:%02d", hour, minute, second)
    }

    fun setDate(txtView: TextView) {
        Utility.hideKeyboard(this)
        val dpd = DatePickerDialog(
            this,
            R.style.DialogTheme,
            DatePickerDialog.OnDateSetListener { view2, thisYear, thisMonth, thisDay ->
                thisAMonth = thisMonth + 1
                thisADay = thisDay
                thisAYear = thisYear

                txtView.text = thisDay.toString() + "/" + thisAMonth + "/" + thisYear
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(thisYear, thisMonth, thisDay)
//                mh.entryDate = newDate.timeInMillis // setting new date
//                    Log.e("@@date1", newDate.timeInMillis.toString() + " ")
            },
            thisAYear,
            thisAMonth,
            thisADay
        )
        dpd.datePicker.spinnersShown = true
        dpd.datePicker.calendarViewShown = false
        dpd.show()
    }

    fun saveFormData(bindinge: ActivitySalaryFormBinding, case_id: String, context: Context, status: Boolean) {
        val requestBody = JsonObject()

        requestBody.addProperty("case_id", case_id)
        requestBody.addProperty("loan_amt", binding.loanAmountEditText.text.toString())
        requestBody.addProperty("visit_address", binding.visitedAddress.text.toString())
        requestBody.addProperty("date_of_visit", binding.txtVisitedDate.text.toString())
        requestBody.addProperty("time", binding.visitedTime.text.toString())
        requestBody.addProperty("person_meet", binding.persontmeetnameEditText.text.toString())
        requestBody.addProperty("loan_purpose", binding.loanPurposeEditText.text.toString())
        requestBody.addProperty("name_of_organization", binding.orgnization.text.toString())
        requestBody.addProperty("work_experience", binding.workexprience.text.toString())
        requestBody.addProperty("education", binding.education.text.toString())
        requestBody.addProperty("remark", binding.remarkText.text.toString())
        requestBody.addProperty("number_of_family_members", binding.numberoffamilynumber.text.toString())
        requestBody.addProperty("lat", addLat)
        requestBody.addProperty("long", addLong)
        requestBody.addProperty("location", addAddress)
        requestBody.addProperty("submitTime", getTime())
        requestBody.addProperty("submitDate", getDate())
        requestBody.addProperty("deviceID", deviceID.toString())

        // 🔹 Missing Fields from Salary Form
        requestBody.addProperty("gross_salary", binding.grossSalaryEditText.text.toString())
        requestBody.addProperty("net_salary", binding.netSalaryEditText.text.toString())
        requestBody.addProperty("current_position_of_employee", binding.currentPositionEditText.text.toString())
        requestBody.addProperty("date_of_joining", binding.txtDateofjoin.text.toString())
        requestBody.addProperty("other_income_of_applicant", binding.otherIncomeEditText.text.toString())
        requestBody.addProperty("previous_employement_details", binding.previousEmploymentEditText.text.toString())
        requestBody.addProperty("office_timings", binding.officeTimingsEditText.text.toString())
        requestBody.addProperty("employer_name", binding.employerNameEditText.text.toString())
        requestBody.addProperty("employer_mobile_no", binding.employerNumberEditText.text.toString())
        requestBody.addProperty("co_employee_name", binding.coEmployeeNameEditText.text.toString())
        requestBody.addProperty("co_employee_mobile_no", binding.coEmployeeNumber.text.toString())
        requestBody.addProperty("salary_received", if (binding.SalaryReceivedInone.isSelected) "Bank" else "Cash")

        requestBody.addProperty("monthly_family_expenditure", binding.familymonthlyExpenditure.text.toString())
        requestBody.addProperty("family_status", spinnerfamilystatus[binding.spinnerFamilystatus.selectedItemPosition])
        requestBody.addProperty("house_status", houseStatusOptions[binding.spinnerHouseStatus.selectedItemPosition])
        requestBody.addProperty("house_size", binding.editTextHouseSize.text.toString())
        requestBody.addProperty("residence_at_address_since", binding.editTextResidenceSince.text.toString())
        requestBody.addProperty("value_of_house", binding.houseValueRentEditText.text.toString())
        requestBody.addProperty("rent_of_house", binding.houseValueRentEditText.text.toString())
        requestBody.addProperty("office_setup_seen", if (binding.radioButtonOfficeSetup.isSelected) "Good" else "Average")
        requestBody.addProperty("pan_number", binding.panNumberEditText.text.toString())
        requestBody.addProperty("security_offered_against_loan", binding.securityOfferedEditText.text.toString())
        requestBody.addProperty("address_of_security", binding.addressOfSecurityEditText.text.toString())
        requestBody.addProperty("security_value", binding.valueOfSecurityEditText.text.toString())
        requestBody.addProperty("size_of_security", binding.sizeOfSecurityEditText.text.toString())
        requestBody.addProperty("neighbour_check_status", if (binding.EnterNeighborCheckStatusone.isSelected) "Positive" else "Negative")
        requestBody.addProperty("assets_owned", binding.Assets.text.toString())
        requestBody.addProperty("earning_family_members", familyIncomeDataArray.toString())
        requestBody.addProperty("current_loans", loanDataArray.toString())
        requestBody.addProperty("bank_details", bankdetailsDataArray.toString())
        requestBody.addProperty("holiday", binding.holidayEditText.text.toString())
        requestBody.addProperty("assets_owned", binding.Assets.text.toString())


        Log.e("@@TAG", "Form Data: $requestBody")

        val sharedPreferences = context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(case_id, requestBody.toString()).apply()

        if (status) {
            val successDialog = SweetAlertDialog(
                ContextThemeWrapper(context, R.style.ThemeOverlay_MaterialComponents_Dialog),
                SweetAlertDialog.SUCCESS_TYPE
            )
            successDialog.setTitleText("Success!")
                .setContentText("Details Saved")
                .setConfirmText("OK")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    (context as Activity).startActivity(Intent(context, MainActivity::class.java))
                    (context as Activity).finish()
                }
                .show()

            val confirmButton = successDialog.findViewById<TextView>(R.id.confirm_button)
            confirmButton?.setBackgroundColor(Color.parseColor("#FF6200EE"))
            confirmButton?.setTextColor(Color.BLACK)
            confirmButton?.setBackgroundColor(Color.GREEN)
        }
    }

/*
    fun loadFormDataFromSharedPrefs(caseId: String) {
        val prefs = getSharedPreferences("FormData", Context.MODE_PRIVATE)
        fun key(name: String) = "${caseId}_$name"

        binding.loanAmountEditText.setText(prefs.getString(key("loan_amt"), ""))
        binding.visitedAddress.setText(prefs.getString(key("visit_address"), ""))
        binding.txtVisitedDate.setText(prefs.getString(key("date_of_visit"), ""))
        binding.visitedTime.setText(prefs.getString(key("time"), ""))
        binding.persontmeetnameEditText.setText(prefs.getString(key("person_meet"), ""))
        binding.loanPurposeEditText.setText(prefs.getString(key("loan_purpose"), ""))
        binding.orgnization.setText(prefs.getString(key("name_of_organization"), ""))
        binding.workexprience.setText(prefs.getString(key("work_experience"), ""))
        binding.education.setText(prefs.getString(key("education"), ""))
        binding.numberoffamilynumber.setText(prefs.getString(key("number_of_family_members"), ""))
        binding.sizeOfSecurityEditText.setText(prefs.getString(key("sizeOfSecurityEditText"), ""))

        addLat = prefs.getString(key("lat"), "") ?: ""
        addLong = prefs.getString(key("long"), "") ?: ""
        addAddress = prefs.getString(key("location"), "") ?: ""

        binding.familymonthlyExpenditure.setText(
            prefs.getString(
                key("monthly_family_expenditure"),
                ""
            )
        )
        binding.spinnerFamilystatus.setSelection(
            spinnerfamilystatus.indexOf(
                prefs.getString(
                    key("family_status"),
                    ""
                )
            )
        )
        binding.spinnerHouseStatus.setSelection(
            houseStatusOptions.indexOf(
                prefs.getString(
                    key("house_status"),
                    ""
                )
            )
        )
        binding.editTextHouseSize.setText(prefs.getString(key("house_size"), ""))
        binding.editTextResidenceSince.setText(
            prefs.getString(
                key("residence_at_address_since"),
                ""
            )
        )
        binding.houseValueRentEditText.setText(prefs.getString(key("value_of_house"), ""))
        binding.grossSalaryEditText.setText(prefs.getString(key("gross_salary"), ""))
        binding.netSalaryEditText.setText(prefs.getString(key("net_salary"), ""))
        binding.currentPositionEditText.setText(
            prefs.getString(
                key("current_position_of_employee"),
                ""
            )
        )
        binding.txtDateofjoin.setText(prefs.getString(key("date_of_joining"), ""))
        binding.otherIncomeEditText.setText(prefs.getString(key("other_income_of_applicant"), ""))
        binding.previousEmploymentEditText.setText(
            prefs.getString(
                key("previous_employement_details"),
                ""
            )
        )
        binding.officeTimingsEditText.setText(prefs.getString(key("office_timings"), ""))
        binding.holidayEditText.setText(prefs.getString(key("holiday"), ""))
        binding.panNumberEditText.setText(prefs.getString(key("pan_number"), ""))

        binding.radioButton1One.isSelected = prefs.getString(key("name_board_seen"), "") == "Yes"
        binding.employerNameEditText.setText(prefs.getString(key("employer_name"), ""))
        binding.employerNumberEditText.setText(prefs.getString(key("employer_mobile_no"), ""))
        binding.coEmployeeNameEditText.setText(prefs.getString(key("co_employee_name"), ""))
        binding.coEmployeeNumber.setText(prefs.getString(key("co_employee_mobile_no"), ""))
        binding.securityOfferedEditText.setText(
            prefs.getString(
                key("security_offered_against_loan"),
                ""
            )
        )
        binding.addressOfSecurityEditText.setText(prefs.getString(key("address_of_security"), ""))
        binding.valueOfSecurityEditText.setText(prefs.getString(key("security_value"), ""))
        binding.editTextHouseSize.setText(prefs.getString(key("size_of_security"), ""))

        binding.EnterNeighborCheckStatusone.isSelected =
            prefs.getString(key("neighbour_check_status"), "") == "Positive"
        binding.radioButtonOfficeSetup.isSelected =
            prefs.getString(key("business_setup"), "") == "Good"
        binding.SalaryReceivedInone.isSelected =
            prefs.getString(key("salary_received"), "") == "Bank"
        binding.Assets.setText(prefs.getString(key("assets_owned"), ""))

        prefs.getString(key("earning_family_members"), "[]")?.let {
            familyIncomeDataArray = JsonArray()
            familyIncomeDataArray.apply {
//                clear()
//                addAll(JsonParser.parseString(it).asJsonArray)
            }
        }
//
//        prefs.getString(key("bank_details"), "[]")?.let {
//            bankdetailsDataArray.apply {
//                clear()
//                addAll(JsonParser.parseString(it).asJsonArray)
//            }
//        }
//
//        prefs.getString(key("current_loans"), "[]")?.let {
//            loanDataArray.apply {
//                clear()
//                addAll(JsonParser.parseString(it).asJsonArray)
//            }
//        }
//
//        prefs.getString(key("image    _name"), "[]")?.let {
//            srtarr = JsonParser.parseString(it).asJsonArray
//        }
        setFamilyIncomeFieldsFromJsonArray()
    }
*/

    fun loadFormData(case_id: String) {
        try {
            val sharedPreferences = getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString(case_id, null) ?: return

            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            binding.loanAmountEditText.setText(jsonObject.get("loan_amt")?.asString ?: "")
            binding.visitedAddress.setText(jsonObject.get("visit_address")?.asString ?: "")
            binding.txtVisitedDate.setText(jsonObject.get("date_of_visit")?.asString ?: "")
            binding.visitedTime.setText(jsonObject.get("time")?.asString ?: "")
            binding.persontmeetnameEditText.setText(jsonObject.get("person_meet")?.asString ?: "")
            binding.loanPurposeEditText.setText(jsonObject.get("loan_purpose")?.asString ?: "")
            binding.orgnization.setText(jsonObject.get("name_of_organization")?.asString ?: "")
            binding.workexprience.setText(jsonObject.get("work_experience")?.asString ?: "")
            binding.education.setText(jsonObject.get("education")?.asString ?: "")
            binding.remarkText.setText(jsonObject.get("remark")?.asString ?: "")
            binding.numberoffamilynumber.setText(jsonObject.get("number_of_family_members")?.asString ?: "")

            binding.grossSalaryEditText.setText(jsonObject.get("gross_salary")?.asString ?: "")
            binding.netSalaryEditText.setText(jsonObject.get("net_salary")?.asString ?: "")
            binding.currentPositionEditText.setText(jsonObject.get("current_position_of_employee")?.asString ?: "")
            binding.txtDateofjoin.setText(jsonObject.get("date_of_joining")?.asString ?: "")
            binding.otherIncomeEditText.setText(jsonObject.get("other_income_of_applicant")?.asString ?: "")
            binding.previousEmploymentEditText.setText(jsonObject.get("previous_employement_details")?.asString ?: "")
            binding.officeTimingsEditText.setText(jsonObject.get("office_timings")?.asString ?: "")
            binding.employerNameEditText.setText(jsonObject.get("employer_name")?.asString ?: "")
            binding.employerNumberEditText.setText(jsonObject.get("employer_mobile_no")?.asString ?: "")
            binding.coEmployeeNameEditText.setText(jsonObject.get("co_employee_name")?.asString ?: "")
            binding.coEmployeeNumber.setText(jsonObject.get("co_employee_mobile_no")?.asString ?: "")

            // Handle Salary Received safely
            val salaryReceived = jsonObject.get("salary_received")?.asString ?: ""
            binding.SalaryReceivedInone.isSelected = salaryReceived == "Bank"
            binding.SalaryReceivedIntwo.isSelected = salaryReceived == "Cash"

            binding.familymonthlyExpenditure.setText(jsonObject.get("monthly_family_expenditure")?.asString ?: "")

            binding.editTextHouseSize.setText(jsonObject.get("house_size")?.asString ?: "")
            binding.editTextResidenceSince.setText(jsonObject.get("residence_at_address_since")?.asString ?: "")
            binding.houseValueRentEditText.setText(jsonObject.get("value_of_house")?.asString ?: "")

            // Office setup selection
            val officeSetup = jsonObject.get("office_setup_seen")?.asString ?: ""
            binding.radioButtonOfficeSetup.isSelected = officeSetup == "Good"
            binding.radioButtonOfficeSetup.isSelected = officeSetup == "Average"

            binding.panNumberEditText.setText(jsonObject.get("pan_number")?.asString ?: "")
            binding.securityOfferedEditText.setText(jsonObject.get("security_offered_against_loan")?.asString ?: "")
            binding.addressOfSecurityEditText.setText(jsonObject.get("address_of_security")?.asString ?: "")
            binding.valueOfSecurityEditText.setText(jsonObject.get("security_value")?.asString ?: "")
            binding.sizeOfSecurityEditText.setText(jsonObject.get("size_of_security")?.asString ?: "")
            binding.holidayEditText.setText(jsonObject.get("holiday")?.asString ?: "")
            binding.Assets.setText(jsonObject.get("assets_owned")?.asString ?: "")

            // Handle earning family members parsing
            try {
                val earningFamilyMembersString = jsonObject.get("earning_family_members")?.asString
                if (!earningFamilyMembersString.isNullOrEmpty()) {
                    familyIncomeDataArray = JsonParser.parseString(earningFamilyMembersString).asJsonArray
                }
            } catch (e: Exception) {
                Log.e("LoadData", "Error parsing earning_family_members: ${e.message}")
            }

            // Handle current loans parsing
            try {
                val currentLoansString = jsonObject.get("current_loans")?.asString
                if (!currentLoansString.isNullOrEmpty()) {
                    loanDataArray = JsonParser.parseString(currentLoansString).asJsonArray
                }
            } catch (e: Exception) {
                Log.e("LoadData", "Error parsing current_loans: ${e.message}")
            }

            // Handle bank details parsing
            try {
                val bankDetailsString = jsonObject.get("bank_details")?.asString
                if (!bankDetailsString.isNullOrEmpty()) {
                    bankdetailsDataArray = JsonParser.parseString(bankDetailsString).asJsonArray
                }
            } catch (e: Exception) {
                Log.e("LoadData", "Error parsing bank_details: ${e.message}")
            }

            // Handle spinner family status matching
            var spinnerfamilystatus_num = 0
            val familyStatusFromJson = jsonObject.get("family_status")?.asString?.trim()
            for (i in 0 until spinnerfamilystatus.size) {
                if (familyStatusFromJson == spinnerfamilystatus[i].trim()) {
                    spinnerfamilystatus_num = i
                    break
                }
            }

            // Handle spinner house status matching
            var houseStatusOptions_num = 0
            val houseStatusFromJson = jsonObject.get("house_status")?.asString?.trim()
            for (i in 0 until houseStatusOptions.size) {
                if (houseStatusFromJson == houseStatusOptions[i].trim()) {
                    houseStatusOptions_num = i
                    break
                }
            }

            // Set adapters and selections
            binding.spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus
            binding.spinnerHouseStatus.adapter = houseStatusAdapter

            binding.spinnerFamilystatus.setSelection(spinnerfamilystatus_num)
            binding.spinnerHouseStatus.setSelection(houseStatusOptions_num)

            houseStatusAdapter.notifyDataSetChanged()
            Adapter_spinnerfamilystatus.notifyDataSetChanged()

            // Set family income fields if any
            setFamilyIncomeFieldsFromJsonArray()

        } catch (e: Exception) {
            Log.e("LoadData", "Error in loadFormData: ${e.message}", e)
        }
    }

    private fun addFamilyIncomeField(name: String,
                                     income: String,
                                     relation: String) {
        familyincome++

        val loanCard = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        loanCard.layoutParams = layoutParams
        loanCard.setPadding(16, 16, 16, 16)
        loanCard.radius = 16f
        loanCard.cardElevation = 8f
        loanCard.tag = "loan_$familyincome"

        val loanLayout = LinearLayout(this)
        loanLayout.orientation = LinearLayout.VERTICAL
        loanLayout.setPadding(16, 16, 16, 16)

        val textView = TextView(this)
        textView.text = "Earning Family Member Detail $familyincome"
        textView.textSize = 18f
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val bankNameEditText = EditText(this)
        bankNameEditText.hint = "Name $familyincome"
        bankNameEditText.inputType= InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        loanLayout.addView(bankNameEditText)

        val amountEditText = EditText(this)
        amountEditText.hint = "Income $familyincome"
        amountEditText.inputType= InputType.TYPE_CLASS_TEXT
        amountEditText.setText(income)
        loanLayout.addView(amountEditText)

        val relationEditText = EditText(this)
        relationEditText.hint = "Relation $familyincome"
        relationEditText.inputType= InputType.TYPE_CLASS_TEXT
        relationEditText.setText(relation)
        loanLayout.addView(relationEditText)

        val jsonObject = JsonObject()
        familyIncomeDataArray.add(jsonObject) // Add to array immediately

        // ✅ Change listener for real-time updates
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jsonObject.addProperty("Name", bankNameEditText.text.toString())
                jsonObject.addProperty("income", amountEditText.text.toString())
                jsonObject.addProperty("relation", relationEditText.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        bankNameEditText.addTextChangedListener(textWatcher)
        amountEditText.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            binding.layoutfamilyincome.removeView(loanCard)

            familyincome--;
            familyIncomeDataArray.remove(jsonObject) // Remove from JSON array

            Log.e("@@TAG", "addFamilyIncomeField: "+familyIncomeDataArray.size() )
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        binding.layoutfamilyincome.addView(loanCard)
    }

    private fun addLoanField(name: String, emi: String, type: String, tenure: String) {
        loans++

        val loanCard = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        loanCard.layoutParams = layoutParams
        loanCard.setPadding(16, 16, 16, 16)
        loanCard.radius = 16f
        loanCard.cardElevation = 8f
        loanCard.tag = "loan_$loans"

        val loanLayout = LinearLayout(this)
        loanLayout.orientation = LinearLayout.VERTICAL
        loanLayout.setPadding(16, 16, 16, 16)

        val textView = TextView(this)
        textView.text = "Existing Loan Details $loans"
        textView.textSize = 18f
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val bankNameEditText = EditText(this)
        bankNameEditText.hint = "Name of Bank $loans"
        bankNameEditText.inputType= InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        loanLayout.addView(bankNameEditText)

        val amountEditText = EditText(this)
        amountEditText.hint = "EMI Balance $loans"
        amountEditText.inputType= InputType.TYPE_CLASS_TEXT
        amountEditText.setText(emi)
        loanLayout.addView(amountEditText)

        val typeOfLoanEditText = EditText(this)
        typeOfLoanEditText.hint = "Type of Loan $loans"
        typeOfLoanEditText.inputType= InputType.TYPE_CLASS_TEXT
        typeOfLoanEditText.setText(type)
        loanLayout.addView(typeOfLoanEditText)

        val tenureOfLoanEditText = EditText(this)
        tenureOfLoanEditText.hint = "Tenure of Loan $loans"
        tenureOfLoanEditText.inputType= InputType.TYPE_CLASS_TEXT
        tenureOfLoanEditText.setText(tenure)
        loanLayout.addView(tenureOfLoanEditText)

        val jsonObject = JsonObject()
        loanDataArray.add(jsonObject) // Add to array immediately

        // ✅ Change listener for real-time updates
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jsonObject.addProperty("BankName", bankNameEditText.text.toString())
                jsonObject.addProperty("TypeofLoan", typeOfLoanEditText.text.toString())
                jsonObject.addProperty("EMIBalance", amountEditText.text.toString())
                jsonObject.addProperty("TenureofLoan", tenureOfLoanEditText.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        bankNameEditText.addTextChangedListener(textWatcher)
        amountEditText.addTextChangedListener(textWatcher)
        typeOfLoanEditText.addTextChangedListener(textWatcher)
        tenureOfLoanEditText.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            binding.layoutloans1.removeView(loanCard)
            loanDataArray.remove(jsonObject) // Remove from JSON array
            loans--;
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        binding.layoutloans1.addView(loanCard)
    }

    private fun addBackField(name: String, branch: String, type: String, since: String) {
        bankdetails++

        val bankCard = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        bankCard.layoutParams = layoutParams
        bankCard.setPadding(16, 16, 16, 16)
        bankCard.radius = 16f
        bankCard.cardElevation = 8f
        bankCard.tag = "bank_$bankdetails"

        val bankLayout = LinearLayout(this)
        bankLayout.orientation = LinearLayout.VERTICAL
        bankLayout.setPadding(16, 16, 16, 16)

        val textView = TextView(this)
        textView.text = "Existing Bank Details $bankdetails"
        textView.textSize = 18f
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        bankLayout.addView(textView)

        val bankNameEditText = EditText(this)
        bankNameEditText.hint = "Bank Name $bankdetails"
        bankNameEditText.inputType= InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        bankLayout.addView(bankNameEditText)

        val branchNameEditText = EditText(this)
        branchNameEditText.hint = "Branch Name $bankdetails"
        branchNameEditText.inputType= InputType.TYPE_CLASS_TEXT
        branchNameEditText.setText(branch)
        bankLayout.addView(branchNameEditText)

        val accountTypeEditText = EditText(this)
        accountTypeEditText.hint = "Account Type $bankdetails"
        accountTypeEditText.inputType= InputType.TYPE_CLASS_TEXT
        accountTypeEditText.setText(type)
        bankLayout.addView(accountTypeEditText)

        val accountSinceEditText = EditText(this)
        accountSinceEditText.hint = "Account Since $bankdetails"
        accountSinceEditText.inputType= InputType.TYPE_CLASS_TEXT
        accountSinceEditText.setText(since)
        bankLayout.addView(accountSinceEditText)

        val jsonObject = JsonObject()
        bankdetailsDataArray.add(jsonObject) // Add to array immediately

        // ✅ Change listener for real-time updates
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jsonObject.addProperty("BankName", bankNameEditText.text.toString())
                jsonObject.addProperty("BranchName", branchNameEditText.text.toString())
                jsonObject.addProperty("AccountType", accountTypeEditText.text.toString())
                jsonObject.addProperty("AccountSince", accountSinceEditText.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        bankNameEditText.addTextChangedListener(textWatcher)
        branchNameEditText.addTextChangedListener(textWatcher)
        accountTypeEditText.addTextChangedListener(textWatcher)
        accountSinceEditText.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            binding.layoutloans2.removeView(bankCard)
            bankdetailsDataArray.remove(jsonObject) // Remove from JSON array
            bankdetails--;
        }
        bankLayout.addView(removeButton)

        bankCard.addView(bankLayout)
        binding.layoutloans2.addView(bankCard)
    }
    private fun setFamilyIncomeFieldsFromJsonArray() {
        familyincome = 0
        loans = 0
        bankdetails = 0

        for (i in 0 until bankdetailsDataArray.size()) {
            val data = bankdetailsDataArray[i].asJsonObject

            val name = data.get("BankName")?.asString ?: ""
            val branch = data.get("BranchName")?.asString ?: ""
            val type = data.get("AccountType")?.asString ?: ""
            val TenureofLoan = data.get("AccountSince")?.asString ?: ""

            if (name.isNotBlank() && branch.isNotBlank() && type.isNotBlank() && TenureofLoan.isNotBlank()) addBackField(
                name,
                branch,
                type,
                TenureofLoan
            )
        }

        for (i in 0 until loanDataArray.size()) {
            val data = loanDataArray[i].asJsonObject

            val name = data.get("BankName")?.asString ?: ""
            val TypeofLoan = data.get("TypeofLoan")?.asString ?: ""
            val EMIBalance = data.get("EMIBalance")?.asString ?: ""
            val TenureofLoan = data.get("TenureofLoan")?.asString ?: ""

            if (name.isNotBlank() && TypeofLoan.isNotBlank() && EMIBalance.isNotBlank() && TenureofLoan.isNotBlank()) addLoanField(
                name,
                EMIBalance,
                TypeofLoan,
                TenureofLoan
            )
        }
        for (i in 0 until familyIncomeDataArray.size()) {
            val data = familyIncomeDataArray[i].asJsonObject

            val name = data.get("Name")?.asString ?: ""
            val income = data.get("income")?.asString ?: ""
            val relation = data.get("relation")?.asString ?: ""

            if (name.isNotBlank() && income.isNotBlank() && relation.isNotBlank()) addFamilyIncomeField(
                name,
                income,
                relation
            )
        }
    }

}