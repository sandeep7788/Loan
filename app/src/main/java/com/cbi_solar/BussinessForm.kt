package com.cbi_solar

import android.Manifest
import android.annotation.SuppressLint
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
import com.cbi_solar.cbisolar.databinding.ActivityBussnessFormBinding
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.FileUtils
import com.cbi_solar.helper.RetrofitManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
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

lateinit var bindinge: ActivityBussnessFormBinding

class BussinessForm : AppCompatActivity() {

    var count: Int = 0
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
                setSubmitData()
            }
        } catch (e: Exception) {
            setSubmitData()
        }
        try {
            progressDialog.show()

            val file = File(FileUtils.getPath(this@BussinessForm, uri))
            addTimestampToImage(file)

            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val parts = MultipartBody.Part.createFormData("image", file.name, requestFile)


            val apiInterface: ApiInterface =
                RetrofitManager().instance1!!.create(ApiInterface::class.java)

            apiInterface.uploadImage(parts, "describtion")?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    Log.e("@@TAG", "onFailure: " + t.message.toString())
                    count = 0
                    Toast.makeText(
                        this@BussinessForm, "Error ", Toast.LENGTH_LONG
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
//                        Toast.makeText(
//                            this@BussinessForm, "" + response.body(), Toast.LENGTH_LONG
//                        ).show()

                        val jsonObject = JSONObject(response.body().toString())

                        if (jsonObject.getString("status").equals("success")) {
                            srtarr.append(jsonObject.getString("image_name")).append(",")

                            if (count < selectedImages.size) {
                                count++
                                uploadImage(selectedImages.get(count))
                            } else {
                                setSubmitData()
                            }
//                            Toast.makeText(
//                                this@BussinessForm, " Image processing: $count", Toast.LENGTH_SHORT
//                            ).show()
                        } else {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                            count = 0
                            Toast.makeText(
                                this@BussinessForm,
                                " " + jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } catch (e: Exception) {
                        setSubmitData()

                    }


                }

            })
        } catch (e: Exception) {
            Toast.makeText(
                this@BussinessForm, " " + e.message, Toast.LENGTH_SHORT
            ).show()
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    private val selectedImages = ArrayList<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    val CAMERA_PERMISSION_CODE = 100
    val spinnerfamilystatus = arrayOf("Select Family Status", "Joint Family", "Nuclear Family")
    val houseStatusOptions = arrayOf("Select House Status", "Owned", "Rented")
    val shopStatusOptions = arrayOf("Select Shop Status", "Owned", "Rented")
    val spinnerSelectAreaAdapterOptions = arrayOf("Select Area", "Commercial", "Residential")
    val BusinessSetupOptions = arrayOf("Select Area", "Excellent", "Good", "Poor")

    lateinit var houseStatusAdapter: ArrayAdapter<String>
    lateinit var shopStatusAdapter: ArrayAdapter<String>
    lateinit var spinnerSelectAreaAdapter: ArrayAdapter<String>
    lateinit var spinnerBusinessSetupdapter: ArrayAdapter<String>
    lateinit var Adapter_spinnerfamilystatus: ArrayAdapter<String>

    @SuppressLint("UseCheckPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindinge = DataBindingUtil.setContentView(this, R.layout.activity_bussness_form)

        /*
        * businesscaretakersNameEditTest
        * businesscaretakersRelationEditTest
        * sizeOfShopEditText
        * sinceOperating
        * aboutBusiness
        * shopTimings
        * holidayIfAny
        * ShopActRegNumber
        * AnyOtherRegistration
        * proportioncCash
        * proportioncCheque
        * fixedEmp
        * temporaryEmp
        * stockValue
        * */

        if (intent.extras != null) {
            case_id = intent.getStringExtra("case_id").toString()
        }


        val employmentStatusOptions = arrayOf("Joint Family", "Nuclear Family")
//        val adapter = ArrayAdapter(this, R.layout.list_item, employmentStatusOptions)
//        bindinge.spinnerHouseStatus.setAdapter(adapter)


        houseStatusAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, houseStatusOptions)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindinge.spinnerHouseStatus.adapter = houseStatusAdapter

        shopStatusAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, shopStatusOptions)
        shopStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindinge.spinnerShopStatus.adapter = shopStatusAdapter

        spinnerSelectAreaAdapter =
            ArrayAdapter(this, R.layout.simple_spinner_item, spinnerSelectAreaAdapterOptions)
        spinnerSelectAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindinge.spinnerSelectArea.adapter = spinnerSelectAreaAdapter

        spinnerBusinessSetupdapter =
            ArrayAdapter(this, R.layout.simple_spinner_item, BusinessSetupOptions)
        spinnerBusinessSetupdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindinge.spinnerBusinessSetup.adapter = spinnerBusinessSetupdapter

        bindinge.spinnerHouseStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    bindinge.houseValueRentEditTextLayout.visibility = View.VISIBLE
                    val selectedItem = houseStatusOptions[position]
//                Toast.makeText(this@BussinessForm, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                    if (selectedItem.equals("Owned")) {
                        bindinge.houseValueRentEditText.setHint("Value of House")
                    } else if (selectedItem.equals("Rented"))
                        bindinge.houseValueRentEditText.setHint(
                        "Rent of House"
                    ) else {
                        bindinge.houseValueRentEditTextLayout.visibility = View.GONE
                        bindinge.houseValueRentEditText.setText("")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    bindinge.houseValueRentEditTextLayout.visibility = View.GONE
                }
            }

        bindinge.spinnerShopStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    bindinge.shopStatusEditTextLayout1.visibility = View.VISIBLE
                    val selectedItem = houseStatusOptions[position]

                    if (selectedItem.equals("Owned")) {
                        bindinge.shopStatusEditText.setHint("Value of Shop")
                    } else if (selectedItem.equals("Rented")) bindinge.shopStatusEditText.setHint(
                        "Rent of shop"
                    ) else if (selectedItem.equals("Select House Status")) bindinge.shopStatusEditTextLayout1.visibility =
                        View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    bindinge.shopStatusEditTextLayout1.visibility = View.GONE
                }
            }

        Adapter_spinnerfamilystatus =
            ArrayAdapter(this, R.layout.simple_spinner_item, spinnerfamilystatus)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindinge.spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus

        setCurrentDate(bindinge.txtVisitedDate)
        setCurrentTime(bindinge.visitedTime)

        bindinge.buttonAddLoans.setOnClickListener {
            addLoanField("", "", "", "")
        }
        bindinge.buttonAddFamilyIncome.setOnClickListener {
            addFamilyIncomeField("", "", "")

        }
        bindinge.buttonAddBank.setOnClickListener {
            addBackField("", "", "", "")
        }
        bindinge.submitButton.setOnClickListener {
            Log.e("@@TAG", "setList: loanDataJson " + bankdetailsDataJson.toString())
            Log.e("@@TAG", "setList: loanDataJson " + loanDataJson.toString())

            if (true) {

                val dialog =
                    SweetAlertDialog(this@BussinessForm, SweetAlertDialog.WARNING_TYPE).apply {
//                    setTitleText("Confirm")
                        setContentText("All set to submit?")
                        setConfirmText("Continue")
                        setCancelText("Cancel")

                        setConfirmClickListener {
                            it.dismissWithAnimation()
                            progressDialog.show()

                            if (selectedImages.isNotEmpty()) {
                                uploadImage(selectedImages[count])
                            } else {
                                setSubmitData()
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
            bindinge.image.setText("Selected Images: "+selectedImages.size)
        }

        imageAdapter = ImageAdapter(selectedImages)
        bindinge.recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bindinge.recyclerViewImages.adapter = imageAdapter


        setlocation()
        Handler(Looper.getMainLooper()).postDelayed({
            setlocation()
        }, 6 * 60 * 1000)

        loadFormData(case_id, this@BussinessForm, bindinge)

        progressDialog = SweetAlertDialog(
            ContextThemeWrapper(
                this, R.style.ThemeOverlay_MaterialComponents_Dialog
            ), SweetAlertDialog.PROGRESS_TYPE
        )
        progressDialog.setTitleText("Loading...")
        progressDialog.setCancelable(false)



        bindinge.buttonAddCustomerDetail.setOnClickListener { addCustomerDetail("", "") }
        bindinge.buttonAddSupplier.setOnClickListener { addbuttonAddSupplier("", "", "") }
        bindinge.submitSave.setOnClickListener {
            saveFormData(bindinge, case_id, this@BussinessForm,true)
        }
    }

    lateinit var progressDialog: SweetAlertDialog

    fun setlocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    var addLat = ""
    var addLong = ""
    var addAddress = ""

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


    private fun validateFields(): Boolean {
        val fieldMap = mapOf(
            bindinge.loanAmountEditText to "Loan Amount",
            bindinge.visitedAddress to "Visited Address",
            bindinge.txtVisitedDate to "Visited Date",
            bindinge.visitedTime to "Visited Time",
            bindinge.persontmeetnameEditText to "Person Met",
            bindinge.loanPurposeEditText to "Loan Purpose",
            bindinge.orgnization to "Organization Name",
            bindinge.workexprience to "Work Experience",
            bindinge.education to "Education",
            bindinge.numberoffamilynumber to "Number of Family Members",
            bindinge.familymonthlyExpenditure to "Monthly Family Expenditure",
            bindinge.editTextHouseSize to "House Size",
            bindinge.editTextResidenceSince to "Residence Since",
            bindinge.houseValueRentEditText to "House Value/Rent",
            bindinge.panNumberEditText to "PAN Number",
            bindinge.securityOfferedEditText to "Security Offered Against Loan",
            bindinge.addressOfSecurityEditText to "Address of Security",
            bindinge.valueOfSecurityEditText to "Security Value",
            bindinge.Assets to "Assets Value"
        )

        val emptyFields = mutableListOf<String>()

        for ((field, fieldName) in fieldMap) {
            if (field.text.toString().trim().isEmpty()) {
                field.error = "This field is required"
                emptyFields.add(fieldName)
            }
        }

        // If any fields are empty, show a toast listing them
        if (emptyFields.isNotEmpty()) {
            val errorMessage = "Please fill: " + emptyFields.joinToString(", ")
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            return false
        }

        // Validate Spinner Selections
        if (bindinge.spinnerFamilystatus.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a Family Status", Toast.LENGTH_SHORT).show()
            return false
        }
        if (bindinge.spinnerHouseStatus.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a House Status", Toast.LENGTH_SHORT).show()
            return false
        }

        return true // If everything is valid
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@BussinessForm, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@BussinessForm, arrayOf(permission), requestCode)
        } else {
//            openImagePicker()
            openCamera()
//            Toast.makeText(this@BussinessForm, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@BussinessForm, "Camera Permission Granted", Toast.LENGTH_SHORT)
                    .show()
//                openImagePicker()
                openCamera()
            } else {
                Toast.makeText(this@BussinessForm, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
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
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
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
                finish()
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
            }
            bindinge.image.setText("Selected Images: "+selectedImages.size)
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

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) +
                " lat:$addLat long:$addLat"

        val fullAddress = addressTextView.toString()
        val halfLength = fullAddress.length / 2
        val addressLine1 = fullAddress.substring(0, halfLength).trim()
        val addressLine2 = fullAddress.substring(halfLength).trim()

        val padding = 20f
        val lineSpacing = 10f

        val boundsLine1 = Rect()
        val boundsLine2 = Rect()
        val boundsTimestamp = Rect()

        textPaint.getTextBounds(addressLine1, 0, addressLine1.length, boundsLine1)
        textPaint.getTextBounds(addressLine2, 0, addressLine2.length, boundsLine2)
        textPaint.getTextBounds(timeStamp, 0, timeStamp.length, boundsTimestamp)

        val maxTextWidth = maxOf(boundsLine1.width(), boundsLine2.width(), boundsTimestamp.width())
        val totalTextHeight = boundsLine1.height() + boundsLine2.height() + boundsTimestamp.height() + (2 * lineSpacing)

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

        val yLine1 = yStart + boundsLine1.height()
        val yLine2 = yLine1 + boundsLine2.height() + lineSpacing
        val yTimestamp = yLine2 + boundsTimestamp.height() + lineSpacing

        canvas.drawText(addressLine1, x, yLine1, textPaint)
        canvas.drawText(addressLine2, x, yLine2, textPaint)
        canvas.drawText(timeStamp, x, yTimestamp, textPaint)

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

        // Get timestamp
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Measure text size to determine background size
//        addressTextView = "$addressTextView $timeStamp"
        val textBounds = Rect()
        textPaint.getTextBounds(("$addressTextView $timeStamp"), 0, ("$addressTextView $timeStamp").length, textBounds)

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

                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
            bindinge.image.setText("Selected Images: "+selectedImages.size)
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
        bindinge.image.setText("Selected Images: "+selectedImages.size)
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
            }
        }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

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

    var case_id = ""
    var deviceID = ""

    private fun buildLoanRequestJson(): JsonObject {
        return JsonObject().apply {
            fun safeString(value: CharSequence?) = value?.toString()?.trim() ?: ""
            fun safeJsonArray(value: JsonArray?) = value?.toString()?.trim() ?: ""

//            fun safeJsonArray(value: JsonArray?): JsonArray = value ?: JsonArray()

            addProperty("case_id", safeString(case_id))
            addProperty("about_buisness_brief_note", safeString(bindinge.businessDetailsBrif.text))
            addProperty("size_of_security", safeString(bindinge.sizeOfSecurityEditText.text))
            addProperty("loan_amt", safeString(bindinge.loanAmountEditText.text))
            addProperty("visit_address", safeString(bindinge.visitedAddress.text))
            addProperty("date_of_visit", safeString(bindinge.txtVisitedDate.text))
            addProperty("time", safeString(bindinge.visitedTime.text))
            addProperty("person_meet", safeString(bindinge.persontmeetnameEditText.text))
            addProperty("loan_purpose", safeString(bindinge.loanPurposeEditText.text))
            addProperty("name_of_organization", safeString(bindinge.orgnization.text))
            addProperty("work_experience", safeString(bindinge.workexprience.text))
            addProperty("education", safeString(bindinge.education.text))
            addProperty("number_of_family_members", safeString(bindinge.numberoffamilynumber.text))
            addProperty("dependent_family_member", safeString(bindinge.dependentFamilyMember.text))
            addProperty("remark", safeString(bindinge.remarkText.text))

            addProperty("lat", addLat)
            addProperty("long", addLong)
            addProperty("location", addAddress)
            addProperty("submitTime", getTime())
            addProperty("submitDate", getDate())
            addProperty("deviceID", deviceID ?: "")

            addProperty("monthly_salary", safeString(bindinge.salaryOfEmploy.text))
            addProperty("monthly_family_expenditure", safeString(bindinge.familymonthlyExpenditure.text))

            addProperty("family_status", spinnerfamilystatus.getOrNull(bindinge.spinnerFamilystatus.selectedItemPosition) ?: "")
            addProperty("house_status", houseStatusOptions.getOrNull(bindinge.spinnerHouseStatus.selectedItemPosition) ?: "")

            addProperty("house_size", safeString(bindinge.editTextHouseSize.text))
            addProperty("residence_at_address_since", safeString(bindinge.editTextResidenceSince.text))


            addProperty("prop_part_status", if (bindinge.ProprietorRadio.isChecked) "Proprietor" else "Partnership")
            addProperty("business_care_taker_name", safeString(bindinge.businesscaretakersNameEditTest.text))
            addProperty("business_care_taker_relation", safeString(bindinge.businesscaretakersRelationEditTest.text))

            addProperty("shop_status", shopStatusOptions.getOrNull(bindinge.spinnerShopStatus.selectedItemPosition) ?: "")
            addProperty("size_of_shop", safeString(bindinge.sizeofShop.text))
            addProperty("since_operating", safeString(bindinge.sinceOperating.text))
            addProperty("area_status", spinnerSelectAreaAdapterOptions.getOrNull(bindinge.spinnerSelectArea.selectedItemPosition) ?: "")
            addProperty("about_buisness", safeString(bindinge.aboutBusiness.text))
            addProperty("shop_timings", safeString(bindinge.shopTimings.text))
            addProperty("holiday", safeString(bindinge.holidayIfAny.text))
            addProperty("gst_reg_no", safeString(bindinge.gstnumber.text))
            addProperty("shop_act_reg_no", safeString(bindinge.ShopActRegNumber.text))
            addProperty("any_other_reg", safeString(bindinge.anyOtherRegistration.text))

            addProperty("proportion_of_sale_cash_basis", safeString(bindinge.proportioncCash.text))
            addProperty("proportion_of_sale_cheque_basis", safeString(bindinge.proportioncCheque.text))

            addProperty("office_setup_seen", if (bindinge.radioButtonOfficeSetup.isChecked) "Good" else "Average")
            addProperty("business_setup", BusinessSetupOptions.getOrNull(bindinge.spinnerBusinessSetup.selectedItemPosition) ?: "")

            addProperty("fixed_employee", safeString(bindinge.fixedEmp.text))
            addProperty("temp_employee", safeString(bindinge.temporaryEmp.text))
            addProperty("value_of_stock", safeString(bindinge.stockValue.text))

            // Handle major_customers and major_suppliers
            addProperty("major_customers", safeJsonArray(addCustomerJson))
            addProperty("major_suppliers", safeJsonArray(addSupplierRequestJson))

            addProperty("as_per_customer_sale", safeString(bindinge.perCustomerSales.text))
            addProperty("as_per_customer_exp", safeString(bindinge.perCustomerExpenditure.text))
            addProperty("as_per_customer_profit", safeString(bindinge.perMonthlyProfit.text))

            addProperty("as_per_ce_sale", safeString(bindinge.perCreditExecutiveSales.text))
            addProperty("as_per_ce_exp", safeString(bindinge.perCreditExecutiveExpenditure.text))
            addProperty("as_per_ce_profit", safeString(bindinge.perCreditExecutiveMonthlyProfit.text))
            addProperty("net_profit_margin", safeString(bindinge.netProfitMargin.text))

            addProperty("pan_number", safeString(bindinge.panNumberEditText.text))
            addProperty("name_board_seen", if (bindinge.OfficeNameBoardObservedRadio.isChecked) "Yes" else "No")

            addProperty("security_offered_against_loan", safeString(bindinge.securityOfferedEditText.text))
            addProperty("address_of_security", safeString(bindinge.addressOfSecurityEditText.text))
            addProperty("security_value", safeString(bindinge.valueOfSecurityEditText.text))
            addProperty("size_of_security", safeString(bindinge.editTextHouseSize.text))

            addProperty("neighbour_check_status", if (bindinge.EnterNeighborCheckStatusone.isChecked) "Positive" else "Negative")

            // Use empty arrays instead of nulls for server safety
            addProperty("earning_family_members", safeJsonArray(familyIncomeDataJson))
            addProperty("bank_details", safeJsonArray(bankdetailsDataJson))
            addProperty("current_loans", safeJsonArray(loanDataJson))

            addProperty("assets_owned", safeString(bindinge.Assets.text))

            // Convert srtarr (List<String>) into proper JSON array
//            val imageArray = JsonArray().apply {
//                srtarr.forEach { add(it) }
//            }
//
            addProperty("image_name", srtarr.toString())
            addProperty("file_name", srtarr.toString())

            if (bindinge.spinnerShopStatus.selectedItemPosition==1) {
                addProperty("value_of_shop", safeString(bindinge.shopStatusEditText.text))
            }else if (bindinge.spinnerShopStatus.selectedItemPosition==2) {
                addProperty("rent_of_shop", safeString(bindinge.shopStatusEditText.text))
            }

            if (bindinge.spinnerHouseStatus.selectedItemPosition==1) {
                addProperty("value_of_house", safeString(bindinge.houseValueRentEditText.text))
            }else if (bindinge.spinnerShopStatus.selectedItemPosition==2) {
                addProperty("rent_of_house", safeString(bindinge.houseValueRentEditText.text))
            }
        }
    }

    fun setSubmitData() {
        try {
            saveFormData(bindinge, case_id, this@BussinessForm,false)
            deviceID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            Log.e("SubmitData", "Error in saveFormData or getting deviceID", e)
        }

        progressDialog.show()

        val requestBody = buildLoanRequestJson()

        Log.e("@@@@@@@@@@@@TAG", "RequestBody: $requestBody")

        val apiInterface = RetrofitManager().instance1!!.create(ApiInterface::class.java)

        apiInterface.submitBussinessDetails(requestBody)?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("TAG", "API Failure: ${t.message}")
                Toast.makeText(this@BussinessForm, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                progressDialog.dismiss()
                val jsonObject = response.body()
                Log.e("TAG", "API Response: $jsonObject")

                try {
                    val res = JSONObject(jsonObject.toString())
                    if (res.getBoolean("status")) {
                        SweetAlertDialog(
                            ContextThemeWrapper(
                                this@BussinessForm,
                                R.style.ThemeOverlay_MaterialComponents_Dialog
                            ),
                            SweetAlertDialog.SUCCESS_TYPE
                        ).apply {
                            titleText = "Success!"
                            contentText = "Details submitted successfully"
                            setConfirmText("OK")
                            setConfirmClickListener {
                                it.dismissWithAnimation()
                                startActivity(Intent(this@BussinessForm, MainActivity::class.java))
                                finish()
                            }
                            show()
                            findViewById<TextView>(R.id.confirm_button)?.apply {
                                setBackgroundColor(Color.GREEN)
                                setTextColor(Color.BLACK)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e("TAG", "JSON Parsing Error: ${e.message}")
                    Toast.makeText(this@BussinessForm, R.string.error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private var loanDataJson = JsonArray()
    private var bankdetailsDataJson = JsonArray()
    private var familyIncomeDataJson = JsonArray()
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

    private var addSupplierRequestJson = JsonArray()
    private var AddSupplierNum = 0
    private var addCustomerJson = JsonArray()
    private var addCustomerNum = 0

    private var loans = 0
    private var bankdetails = 0
    private var familyincome = 0

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
        bankNameEditText.inputType = InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        loanLayout.addView(bankNameEditText)

        val amountEditText = EditText(this)
        amountEditText.hint = "EMI Balance $loans"
        amountEditText.inputType = InputType.TYPE_CLASS_TEXT
        amountEditText.setText(emi)
        loanLayout.addView(amountEditText)

        val typeOfLoanEditText = EditText(this)
        typeOfLoanEditText.hint = "Type of Loan $loans"
        typeOfLoanEditText.inputType = InputType.TYPE_CLASS_TEXT
        typeOfLoanEditText.setText(type)
        loanLayout.addView(typeOfLoanEditText)

        val tenureOfLoanEditText = EditText(this)
        tenureOfLoanEditText.hint = "Tenure of Loan $loans"
        tenureOfLoanEditText.inputType = InputType.TYPE_CLASS_TEXT
        tenureOfLoanEditText.setText(tenure)
        loanLayout.addView(tenureOfLoanEditText)

        val jsonObject = JsonObject()
        loanDataJson.add(jsonObject) // Add to array immediately

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
            bindinge.buttonAddLoansLayout.removeView(loanCard)
            loanDataJson.remove(jsonObject) // Remove from JSON array
            loans--
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        bindinge.buttonAddLoansLayout.addView(loanCard)
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
        bankNameEditText.inputType = InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        bankLayout.addView(bankNameEditText)

        val branchNameEditText = EditText(this)
        branchNameEditText.hint = "Branch Name $bankdetails"
        branchNameEditText.inputType = InputType.TYPE_CLASS_TEXT
        branchNameEditText.setText(branch)
        bankLayout.addView(branchNameEditText)

        val accountTypeEditText = EditText(this)
        accountTypeEditText.hint = "Account Type $bankdetails"
        accountTypeEditText.inputType = InputType.TYPE_CLASS_TEXT
        accountTypeEditText.setText(type)
        bankLayout.addView(accountTypeEditText)

        val accountSinceEditText = EditText(this)
        accountSinceEditText.hint = "Account Since $bankdetails"
        accountSinceEditText.inputType = InputType.TYPE_CLASS_TEXT
        accountSinceEditText.setText(since)
        bankLayout.addView(accountSinceEditText)

        val jsonObject = JsonObject()
        bankdetailsDataJson.add(jsonObject) // Add to array immediately

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
            bindinge.layoutloans2.removeView(bankCard)
            bankdetailsDataJson.remove(jsonObject) // Remove from JSON array
            bankdetails--
        }
        bankLayout.addView(removeButton)

        bankCard.addView(bankLayout)
        bindinge.layoutloans2.addView(bankCard)
    }

    private fun addFamilyIncomeField(
        name: String,
        income: String,
        relation: String
    ) {
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
        bankNameEditText.inputType = InputType.TYPE_CLASS_TEXT
        bankNameEditText.setText(name)
        loanLayout.addView(bankNameEditText)

        val amountEditText = EditText(this)
        amountEditText.hint = "Income $familyincome"
        amountEditText.inputType = InputType.TYPE_CLASS_TEXT
        amountEditText.setText(income)
        loanLayout.addView(amountEditText)

        val relationEditText = EditText(this)
        relationEditText.hint = "Relation $familyincome"
        relationEditText.inputType = InputType.TYPE_CLASS_TEXT
        relationEditText.setText(relation)
        loanLayout.addView(relationEditText)

        val jsonObject = JsonObject()
        familyIncomeDataJson.add(jsonObject) // Add to array immediately

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
        relationEditText.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            bindinge.layoutfamilyincome.removeView(loanCard)

            familyincome--
            familyIncomeDataJson.remove(jsonObject) // Remove from JSON array
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        bindinge.layoutfamilyincome.addView(loanCard)
    }

    private fun addCustomerDetail(name: String, mobile: String) {
        addCustomerNum++

        val loanCard = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        loanCard.layoutParams = layoutParams
        loanCard.setPadding(16, 16, 16, 16)
        loanCard.radius = 16f
        loanCard.cardElevation = 8f
        loanCard.tag = "loan_$addCustomerNum"

        val loanLayout = LinearLayout(this)
        loanLayout.orientation = LinearLayout.VERTICAL
        loanLayout.setPadding(16, 16, 16, 16)

        val textView = TextView(this)
        textView.text = "Customer Detail $addCustomerNum"
        textView.textSize = 18f
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val nameText = EditText(this)
        nameText.hint = "Name $addCustomerNum"
        nameText.inputType = InputType.TYPE_CLASS_TEXT
        nameText.setText(name)
        loanLayout.addView(nameText)

        val mobileNo = EditText(this)
        mobileNo.hint = "Mobile No. $addCustomerNum"
        mobileNo.inputType = InputType.TYPE_CLASS_NUMBER
        mobileNo.setText(mobile)
        loanLayout.addView(mobileNo)

        val jsonObject = JsonObject()
        addCustomerJson.add(jsonObject)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jsonObject.addProperty("Name", nameText.text.toString())
                jsonObject.addProperty("mobile", mobileNo.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        nameText.addTextChangedListener(textWatcher)
        mobileNo.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            bindinge.buttonAddCustomerDetailLayout.removeView(loanCard)
            addCustomerJson.remove(jsonObject) // Remove from JSON array
            addCustomerNum--
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        bindinge.buttonAddCustomerDetailLayout.addView(loanCard)
    }

    private fun addbuttonAddSupplier(name: String, mobile: String, citystr: String) {
        AddSupplierNum++

        val loanCard = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        loanCard.layoutParams = layoutParams
        loanCard.setPadding(16, 16, 16, 16)
        loanCard.radius = 16f
        loanCard.cardElevation = 8f
        loanCard.tag = "loan_$AddSupplierNum"

        val loanLayout = LinearLayout(this)
        loanLayout.orientation = LinearLayout.VERTICAL
        loanLayout.setPadding(16, 16, 16, 16)

        val textView = TextView(this)
        textView.text = "Suppliers Detail $AddSupplierNum"
        textView.textSize = 18f
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val nameText = EditText(this)
        nameText.hint = "Name $AddSupplierNum"
        nameText.inputType = InputType.TYPE_CLASS_TEXT
        nameText.setText(name)
        loanLayout.addView(nameText)

        val mobileNo = EditText(this)
        mobileNo.hint = "Mobile No. $AddSupplierNum"
        mobileNo.inputType = InputType.TYPE_CLASS_NUMBER
        mobileNo.setText(mobile)
        loanLayout.addView(mobileNo)

        val city = EditText(this)
        city.hint = "City $AddSupplierNum"
        city.inputType = InputType.TYPE_CLASS_TEXT
        city.setText(citystr)
        loanLayout.addView(city)

        val jsonObject = JsonObject()
        addSupplierRequestJson.add(jsonObject)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jsonObject.addProperty("Name", nameText.text.toString())
                jsonObject.addProperty("mobile", mobileNo.text.toString())
                jsonObject.addProperty("city", city.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        nameText.addTextChangedListener(textWatcher)
        mobileNo.addTextChangedListener(textWatcher)
        city.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            bindinge.buttonAddSupplierLayout.removeView(loanCard)
            addSupplierRequestJson.remove(jsonObject) // Remove from JSON array
            AddSupplierNum--
        }
        loanLayout.addView(removeButton)

        loanCard.addView(loanLayout)
        bindinge.buttonAddSupplierLayout.addView(loanCard)
    }

    fun saveFormData(bindinge: ActivityBussnessFormBinding, case_id: String, context: Context,status:Boolean) {
        val requestBody = JsonObject()

        requestBody.addProperty("case_id", case_id)
        requestBody.addProperty("loan_amt", bindinge.loanAmountEditText.text.toString())
        requestBody.addProperty("visit_address", bindinge.visitedAddress.text.toString())
        requestBody.addProperty("date_of_visit", bindinge.txtVisitedDate.text.toString())
        requestBody.addProperty("time", bindinge.visitedTime.text.toString())
        requestBody.addProperty("person_meet", bindinge.persontmeetnameEditText.text.toString())
        requestBody.addProperty("loan_purpose", bindinge.loanPurposeEditText.text.toString())
        requestBody.addProperty("name_of_organization", bindinge.orgnization.text.toString())
        requestBody.addProperty("work_experience", bindinge.workexprience.text.toString())
        requestBody.addProperty("education", bindinge.education.text.toString())
        requestBody.addProperty("remark", bindinge.remarkText.text.toString())
        requestBody.addProperty(
            "number_of_family_members", bindinge.numberoffamilynumber.text.toString()
        )
        requestBody.addProperty(
            "dependent_family_member", bindinge.dependentFamilyMember.text.toString()
        )
        requestBody.addProperty("lat", addLat)
        requestBody.addProperty("long", addLong)
        requestBody.addProperty("location", addAddress)
        requestBody.addProperty("submitTime", getTime())
        requestBody.addProperty("submitDate", getDate())
        requestBody.addProperty("deviceID", deviceID.toString())
        requestBody.addProperty("monthly_salary", bindinge.salaryOfEmploy.text.toString())
        requestBody.addProperty("about_buisness_brief_note", bindinge.businessDetailsBrif.text.toString())
        requestBody.addProperty(
            "monthly_family_expenditure", bindinge.familymonthlyExpenditure.text.toString()
        )
        requestBody.addProperty(
            "family_status",
            spinnerfamilystatus.get(bindinge.spinnerFamilystatus.selectedItemPosition)
        )
        requestBody.addProperty(
            "house_status", houseStatusOptions.get(bindinge.spinnerHouseStatus.selectedItemPosition)
        )
        requestBody.addProperty("house_size", bindinge.editTextHouseSize.text.toString())
        requestBody.addProperty(
            "residence_at_address_since", bindinge.editTextResidenceSince.text.toString()
        )
        requestBody.addProperty(
            "prop_part_status",
            if (bindinge.ProprietorRadio.isSelected) "Proprietor" else "Partnership"
        )
        requestBody.addProperty(
            "business_care_taker_name", bindinge.businesscaretakersNameEditTest.text.toString()
        )
        requestBody.addProperty(
            "business_care_taker_relation",
            bindinge.businesscaretakersRelationEditTest.text.toString()
        )
        requestBody.addProperty(
            "shop_status",
            shopStatusOptions.get(bindinge.spinnerShopStatus.selectedItemPosition).toString()
        )
        requestBody.addProperty("size_of_shop", bindinge.sizeofShop.text.toString())

        requestBody.addProperty("value_of_shop", bindinge.shopStatusEditText.text.toString())
        requestBody.addProperty("rent_of_shop", bindinge.shopStatusEditText.text.toString())

        requestBody.addProperty("since_operating", bindinge.sinceOperating.text.toString())
        requestBody.addProperty(
            "area_status",
            spinnerSelectAreaAdapterOptions.get(bindinge.spinnerSelectArea.selectedItemPosition)
        )
        requestBody.addProperty("about_buisness", bindinge.aboutBusiness.text.toString())
        requestBody.addProperty("shop_timings", bindinge.shopTimings.text.toString())
        requestBody.addProperty("holiday", bindinge.holidayIfAny.text.toString())
        requestBody.addProperty("gst_reg_no", bindinge.gstnumber.text.toString())
        requestBody.addProperty("shop_act_reg_no", bindinge.ShopActRegNumber.text.toString())
        requestBody.addProperty("any_other_reg", bindinge.anyOtherRegistration.text.toString())
        requestBody.addProperty(
            "proportion_of_sale_cash_basis", bindinge.proportioncCash.text.toString()
        )
        requestBody.addProperty(
            "proportion_of_sale_cheque_basis", bindinge.proportioncCheque.text.toString()
        )
        requestBody.addProperty(
            "office_setup_seen",
            if (bindinge.radioButtonOfficeSetup.isSelected) "Good" else "Average"
        )
        requestBody.addProperty(
            "business_setup",
            BusinessSetupOptions.get(bindinge.spinnerBusinessSetup.selectedItemPosition)
        )
        requestBody.addProperty("fixed_employee", bindinge.fixedEmp.text.toString())
        requestBody.addProperty("temp_employee", bindinge.temporaryEmp.text.toString())
        requestBody.addProperty("value_of_stock", bindinge.stockValue.text.toString())
        requestBody.addProperty("major_customers", addCustomerJson.toString())
        requestBody.addProperty("major_suppliers", addSupplierRequestJson.toString())
        requestBody.addProperty("about_buisness_brief_note", bindinge.aboutBusiness.text.toString())
        requestBody.addProperty("as_per_customer_sale", bindinge.perCustomerSales.text.toString())
        requestBody.addProperty(
            "as_per_customer_exp", bindinge.perCustomerExpenditure.text.toString()
        )
        requestBody.addProperty("as_per_customer_profit", bindinge.perMonthlyProfit.text.toString())
        requestBody.addProperty("as_per_ce_sale", bindinge.perCreditExecutiveSales.text.toString())
        requestBody.addProperty(
            "as_per_ce_exp", bindinge.perCreditExecutiveExpenditure.text.toString()
        )
        requestBody.addProperty(
            "as_per_ce_profit", bindinge.perCreditExecutiveMonthlyProfit.text.toString()
        )
        requestBody.addProperty("net_profit_margin", bindinge.netProfitMargin.text.toString())
        requestBody.addProperty("pan_number", bindinge.panNumberEditText.text.toString())
        requestBody.addProperty(
            "name_board_seen", if (bindinge.OfficeNameBoardObservedRadio.isSelected) "Yes" else "No"
        )
        requestBody.addProperty(
            "security_offered_against_loan", bindinge.securityOfferedEditText.text.toString()
        )
        requestBody.addProperty(
            "address_of_security", bindinge.addressOfSecurityEditText.text.toString()
        )
        requestBody.addProperty("security_value", bindinge.valueOfSecurityEditText.text.toString())
        requestBody.addProperty("size_of_security", bindinge.editTextHouseSize.text.toString())
        requestBody.addProperty(
            "neighbour_check_status",
            if (bindinge.EnterNeighborCheckStatusone.isSelected) "Positive" else "Negative"
        )
        requestBody.addProperty("earning_family_members", familyIncomeDataJson.toString())
        requestBody.addProperty("bank_details", bankdetailsDataJson.toString())
        requestBody.addProperty("current_loans", loanDataJson.toString())
        requestBody.addProperty("assets_owned", bindinge.Assets.text.toString())
//        requestBody.addProperty("image_name", srtarr.toString())
//        requestBody.addProperty("file_name", srtarr.toString())
        requestBody.addProperty("size_of_security", bindinge.sizeOfSecurityEditText.text.toString())
        requestBody.addProperty("value_of_house", bindinge.houseValueRentEditText.text.toString())
        requestBody.addProperty("rent_of_house", bindinge.houseValueRentEditText.text.toString())

        Log.e("@@TAG", "Form Data: $requestBody")

        // Save to SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(case_id, requestBody.toString()).apply()

        if (status) {
            val successDialog = SweetAlertDialog(
                ContextThemeWrapper(
                    this@BussinessForm, R.style.ThemeOverlay_MaterialComponents_Dialog
                ), SweetAlertDialog.SUCCESS_TYPE
            )

            successDialog.setTitleText("Success!").setContentText("Details Saved").setConfirmText("OK")
                .setConfirmClickListener {
                    it.dismissWithAnimation()

                    this@BussinessForm.startActivity(
                        Intent(
                            this@BussinessForm, MainActivity::class.java
                        )
                    )
                    finish()

                }.show()

//                        successDialog.setOnShowListener {
            val confirmButton = successDialog.findViewById<TextView>(R.id.confirm_button)
            confirmButton?.setBackgroundColor(Color.parseColor("#FF6200EE")) // Use your custom color
            confirmButton?.setTextColor(Color.BLACK) // Ensure text is visible
            confirmButton?.setBackgroundColor(Color.GREEN)
        }
    }

    fun loadFormData(caseId: String, context: Context, bindinge: ActivityBussnessFormBinding)
    {
        try     {

            val sharedPreferences =
                context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString(caseId, null)

            if (jsonString != null) {
                val data = JsonParser.parseString(jsonString).asJsonObject
                Log.e(TAG, "loadFormData: " + data)

                bindinge.loanAmountEditText.setText(data["loan_amt"]?.asString)
                bindinge.visitedAddress.setText(data["visit_address"]?.asString)
                bindinge.txtVisitedDate.text = data["date_of_visit"]?.asString
                bindinge.visitedTime.text = data["time"]?.asString
                bindinge.persontmeetnameEditText.setText(data["person_meet"]?.asString)
                bindinge.loanPurposeEditText.setText(data["loan_purpose"]?.asString)
                bindinge.orgnization.setText(data["name_of_organization"]?.asString)
                bindinge.workexprience.setText(data["work_experience"]?.asString)
                bindinge.education.setText(data["education"]?.asString)
                bindinge.numberoffamilynumber.setText(data["number_of_family_members"]?.asString)
                bindinge.salaryOfEmploy.setText(data["monthly_salary"]?.asString)
                bindinge.familymonthlyExpenditure.setText(data["monthly_family_expenditure"]?.asString)
                bindinge.editTextHouseSize.setText(data["house_size"]?.asString)
                bindinge.editTextResidenceSince.setText(data["residence_at_address_since"]?.asString)
                bindinge.houseValueRentEditText.setText(data["value_of_house"]?.asString)
                bindinge.houseValueRentEditText.setText(data["rent_of_house"]?.asString)
                bindinge.businesscaretakersNameEditTest.setText(data["business_care_taker_name"]?.asString)
                bindinge.businesscaretakersRelationEditTest.setText(data["business_care_taker_relation"]?.asString)
                bindinge.sizeofShop.setText(data["size_of_shop"]?.asString)
                bindinge.shopStatusEditText.setText(data["value_of_shop"]?.asString)
                bindinge.shopStatusEditText.setText(data["rent_of_shop"]?.asString)
                bindinge.sinceOperating.setText(data["since_operating"]?.asString)
                bindinge.aboutBusiness.setText(data["about_buisness"]?.asString)
                bindinge.shopTimings.setText(data["shop_timings"]?.asString)
                bindinge.holidayIfAny.setText(data["holiday"]?.asString)
                bindinge.gstnumber.setText(data["gst_reg_no"]?.asString)
                bindinge.ShopActRegNumber.setText(data["shop_act_reg_no"]?.asString)
                bindinge.anyOtherRegistration.setText(data["any_other_reg"]?.asString)
                bindinge.proportioncCash.setText(data["proportion_of_sale_cash_basis"]?.asString)
                bindinge.proportioncCheque.setText(data["proportion_of_sale_cheque_basis"]?.asString)
                bindinge.fixedEmp.setText(data["fixed_employee"]?.asString)
                bindinge.temporaryEmp.setText(data["temp_employee"]?.asString)
                bindinge.stockValue.setText(data["value_of_stock"]?.asString)
                bindinge.perCustomerSales.setText(data["as_per_customer_sale"]?.asString)
                bindinge.perCustomerExpenditure.setText(data["as_per_customer_exp"]?.asString)
                bindinge.perMonthlyProfit.setText(data["as_per_customer_profit"]?.asString)
                bindinge.perCreditExecutiveSales.setText(data["as_per_ce_sale"]?.asString)
                bindinge.perCreditExecutiveExpenditure.setText(data["as_per_ce_exp"]?.asString)
                bindinge.perCreditExecutiveMonthlyProfit.setText(data["as_per_ce_profit"]?.asString)
                bindinge.netProfitMargin.setText(data["net_profit_margin"]?.asString)
                bindinge.panNumberEditText.setText(data["pan_number"]?.asString)
                bindinge.securityOfferedEditText.setText(data["security_offered_against_loan"]?.asString)
                bindinge.addressOfSecurityEditText.setText(data["address_of_security"]?.asString)
                bindinge.valueOfSecurityEditText.setText(data["security_value"]?.asString)
                bindinge.editTextHouseSize.setText(data["size_of_security"]?.asString)
                bindinge.Assets.setText(data["assets_owned"]?.asString)
                bindinge.businessDetailsBrif.setText(data["about_buisness_brief_note"]?.asString)
                bindinge.sizeOfSecurityEditText.setText(data["size_of_security"]?.asString)
                bindinge.remarkText.setText(data["remark"]?.asString)

                var shop_status_num = 0
                var spinnerSelectAreaAdapterOptions_num = 0
                var BusinessSetupOptions_num = 0
                var spinnerfamilystatus_num = 0
                var houseStatusOptions_num = 0

                for (i in 0 until shopStatusOptions.size) {
                    if (data["shop_status"] != null && data["shop_status"].equals(shopStatusOptions[i])) {
                        shop_status_num = i
                    }
                }
                for (i in 0 until spinnerSelectAreaAdapterOptions.size) {
                    if (data["area_status"] != null && data["area_status"].equals(spinnerSelectAreaAdapterOptions[i])) {
                        spinnerSelectAreaAdapterOptions_num = i
                    }
                }
                for (i in 0 until BusinessSetupOptions.size) {
                    if (data["business_setup"] != null && data["business_setup"].equals(BusinessSetupOptions[i])) {
                        BusinessSetupOptions_num = i
                    }
                }
                for (i in 0 until spinnerfamilystatus.size) {
                    if (data["family_status"] != null && data["family_status"].equals(spinnerfamilystatus[i])) {
                        spinnerfamilystatus_num = i
                    }
                }
                for (i in 0 until houseStatusOptions.size) {
                    if (data["house_status"] != null && data["house_status"].equals(houseStatusOptions[i])) {
                        houseStatusOptions_num = i
                    }
                }

                bindinge.spinnerShopStatus.setSelection(shop_status_num)
                bindinge.spinnerSelectArea.setSelection(spinnerSelectAreaAdapterOptions_num)
                bindinge.spinnerBusinessSetup.setSelection(BusinessSetupOptions_num)
                bindinge.spinnerFamilystatus.setSelection(spinnerfamilystatus_num)
                bindinge.spinnerHouseStatus.setSelection(houseStatusOptions_num)

                bindinge.spinnerHouseStatus.adapter = houseStatusAdapter
                bindinge.spinnerSelectArea.adapter = shopStatusAdapter
                bindinge.spinnerBusinessSetup.adapter = spinnerSelectAreaAdapter
                bindinge.spinnerFamilystatus.adapter = spinnerBusinessSetupdapter
                bindinge.spinnerHouseStatus.adapter = Adapter_spinnerfamilystatus

                // For spinners - set selection based on string value
                val familyStatus = data["family_status"]?.asString
                val houseStatus = data["house_status"]?.asString
                val shopStatus = data["shop_status"]?.asString
                val businessSetup = data["spinnerBusinessSetup"]?.asString
                val areaStatus = data["area_status"]?.asString

                familyStatus?.let {
                    val index = spinnerfamilystatus.indexOf(it)
                    if (index >= 0) bindinge.spinnerFamilystatus.setSelection(index)
                }

                houseStatus?.let {
                    val index = houseStatusOptions.indexOf(it)
                    if (index >= 0) bindinge.spinnerHouseStatus.setSelection(index)
                }

                shopStatus?.let {
                    val index = shopStatusOptions.indexOf(it)
                    if (index >= 0) bindinge.spinnerShopStatus.setSelection(index)
                }

                businessSetup?.let {
                    val index = BusinessSetupOptions.indexOf(it)
                    if (index >= 0) bindinge.spinnerBusinessSetup.setSelection(index)
                }

                areaStatus?.let {
                    val index = spinnerSelectAreaAdapterOptions.indexOf(it)
                    if (index >= 0) bindinge.spinnerSelectArea.setSelection(index)
                }

                val earningFamilyMembersString = data["earning_family_members"].asString

                if (earningFamilyMembersString != null && !earningFamilyMembersString.isEmpty()) {
                    familyIncomeDataJson =
                        JsonParser.parseString(earningFamilyMembersString).asJsonArray

                }
                val current_loans_ = data["current_loans"].asString

                if (current_loans_ != null && !current_loans_.isEmpty()) {
                    loanDataJson = JsonParser.parseString(current_loans_).asJsonArray
                }

                val bank_details = data["bank_details"].asString
                if (bank_details != null && !bank_details.isEmpty()) {
                    bankdetailsDataJson = JsonParser.parseString(bank_details).asJsonArray
                }

                val major_customers = data["major_customers"].asString
                if (major_customers != null && !major_customers.isEmpty()) {
                    addCustomerJson = JsonParser.parseString(major_customers).asJsonArray
                }

                val major_suppliers = data["major_suppliers"].asString
                if (major_suppliers != null && !major_suppliers.isEmpty()) {
                    addSupplierRequestJson = JsonParser.parseString(major_suppliers).asJsonArray
                }

                setFamilyIncomeFieldsFromJsonArray()
                // For radio buttons
                bindinge.ProprietorRadio.isSelected = data["prop_part_status"]?.asString == "Proprietor"
                bindinge.radioButtonOfficeSetup.isSelected =
                    data["office_setup_seen"]?.asString == "Good"
                bindinge.OfficeNameBoardObservedRadio.isSelected =
                    data["name_board_seen"]?.asString == "Yes"
                bindinge.EnterNeighborCheckStatusone.isSelected =
                    data["neighbour_check_status"]?.asString == "Positive"
                bindinge.dependentFamilyMember.setText(data["dependent_family_member"]?.asString)
                bindinge.aboutBusiness.setText(data["about_buisness_brief_note"]?.asString)
                Log.d("@@TAG", "Form data loaded for case_id: $caseId")
            } else {
                Log.d("@@TAG", "No saved form data found for case_id: $caseId")
            }
        } catch (e:Exception) {

        }

    }
    private fun setFamilyIncomeFieldsFromJsonArray() {
        familyincome = 0
        loans = 0
        bankdetails = 0
        AddSupplierNum = 0
        addCustomerNum = 0

        for (i in 0 until addSupplierRequestJson.size()) {
            val data = addSupplierRequestJson[i].asJsonObject

            val name = data.get("Name")?.asString ?: ""
            val mobile = data.get("mobile")?.asString ?: ""
            val city = data.get("city")?.asString ?: ""

            if (name.isNotBlank() && mobile.isNotBlank()) addbuttonAddSupplier(name, mobile, city)
        }
        for (i in 0 until addCustomerJson.size()) {
            val data = addCustomerJson[i].asJsonObject

            val name = data.get("Name")?.asString ?: ""
            val mobile = data.get("mobile")?.asString ?: ""
            if (name.isNotBlank() && mobile.isNotBlank()) addCustomerDetail(name, mobile)
        }

        for (i in 0 until bankdetailsDataJson.size()) {
            val data = bankdetailsDataJson[i].asJsonObject

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

        for (i in 0 until loanDataJson.size()) {
            val data = loanDataJson[i].asJsonObject

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
        for (i in 0 until familyIncomeDataJson.size()) {
            val data = familyIncomeDataJson[i].asJsonObject

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