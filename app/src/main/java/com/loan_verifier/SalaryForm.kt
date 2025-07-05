package com.loan_verifier

import kotlinx.coroutines.withTimeout
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
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ExifInterface
import android.media.SoundPool
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.loan_verifier.loan.MainActivity
import com.loan_verifier.loan.R
import com.loan_verifier.loan.Utility
import com.loan_verifier.loan.databinding.ActivitySalaryFormBinding
import com.loan_verifier.helper.ApiInterface
import com.loan_verifier.helper.FileUtils
import com.loan_verifier.helper.RetrofitManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.loan_verifier.loan.JsonFieldsPreviewDialog
import com.loan_verifier.loan.MainActivity.Companion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class SalaryForm : AppCompatActivity() {

    var count: Int = 0;
    var srtarr: StringBuilder = StringBuilder()
    var latitudeTextView: Double? = null
    var longitudeTextView: Double? = null
    var addressString: String? = " "
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var binding: ActivitySalaryFormBinding
    private var loans = 0;
    private var bankdetails = 0;
    private var familyincome = 0;

    var TAG = "@@TAG"

    /*    private lateinit var uploadDialog: AlertDialog
        private lateinit var progressText: TextView
        private lateinit var progressBar: ProgressBar

        private fun showImageUploadDialog(maxImages: Int) {
            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.dialog_image_upload_progress, null)
            progressText = dialogView.findViewById(R.id.textProgress)
            progressBar = dialogView.findViewById(R.id.progressBar)

            progressBar.max = maxImages
            progressBar.progress = 0
            progressText.text = "Uploading 0 of $maxImages"

            uploadDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            uploadDialog.show()
        }

        private fun updateImageUploadDialog(current: Int, max: Int) {
            dismissProgressDialog()
            progressBar.progress = current
            progressText.text = "Uploading $current of $max"
            if (current >= max) {
                uploadDialog.dismiss()
                showProgressDialog()
            }
        }*/
    private var progressBar: ProgressBar? = null
    private var progressText: TextView? = null
    private var uploadDialog: AlertDialog? = null

    /**
     * Show a dialog for uploading images.
     */
    private fun showImageUploadDialog(maxImages: Int) {
        try {
            if (uploadDialog?.isShowing == true) {
                Log.d("@@TAG", "Upload dialog is already showing.")
                return
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_upload_progress, null)

            progressBar = dialogView.findViewById(R.id.progressBar)
            progressText = dialogView.findViewById(R.id.textProgress)

            if (progressBar == null || progressText == null) {
                Log.e("@@TAG", "Missing progressBar or progressText in layout")
                return
            }

            progressBar?.max = maxImages
            progressBar?.progress = 0
            progressText?.text = "Uploading 0 of $maxImages"

            uploadDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            uploadDialog?.show()

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("@@TAG", "Error showing upload dialog: ${e.message}")
        }
    }

    /**
     * Update the image upload progress dialog.
     */
    private fun updateImageUploadDialog(current: Int, max: Int) {
        try {
            if (uploadDialog == null || !uploadDialog!!.isShowing) {
                Log.w("@@TAG", "Upload dialog is not showing. Skipping update.")
                return
            }

            progressBar?.progress = current
            progressText?.text = "Uploading $current of $max"

            if (current >= max) {
                dismissUploadDialog()
                showProgressDialog() // You can customize or remove this based on your flow
            }

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("@@TAG", "Error updating upload dialog: ${e.message}")
        }
    }

    /**
     * Dismiss the upload dialog safely.
     */
    private fun dismissUploadDialog() {
        try {
            if (uploadDialog?.isShowing == true) {
                uploadDialog?.dismiss()
            }
            uploadDialog = null
            progressBar = null
            progressText = null
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("@@TAG", "Error dismissing upload dialog: ${e.message}")
        }
    }



    fun uploadImage(uri: Uri) {
        Utility.hideKeyboard(this@SalaryForm)
        val thread1 = Thread {
            try {

                try {
                    runOnUiThreadSafely {
                        updateImageUploadDialog(count, selectedImages.size)
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast(e.message.toString())
                }

                try {
                    if (uri == null) {
                        setSubmitData()
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast(e.message.toString())
                    setSubmitData()
                }

                try {

                    val file = File(FileUtils.getPath(this@SalaryForm, uri))
                    addTimestampToImage(file)

                    val requestFile: RequestBody =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val parts = MultipartBody.Part.createFormData("image", file.name, requestFile)


                    val apiInterface: ApiInterface =
                        RetrofitManager().instance1!!.create(ApiInterface::class.java)

                    apiInterface.uploadImage(parts, case_id.toString())
                        ?.enqueue(object : Callback<JsonObject> {
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                dismissProgressDialog()
                                Log.e("@@TAG", "onFailure: " + t.message.toString())
                                count = 0

                                dismissUploadDialog()
                                showToast("Error")
                                showUploadErrorDialog()

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
                                        srtarr.append(jsonObject.getString("image_name"))
                                            .append(",")

                                        if (count < selectedImages.size) {
                                            uploadImage(selectedImages.get(count))
                                            count++
                                        } else {
                                            setSubmitData()
                                        }
                                    } else {
                                        dismissProgressDialog()
                                        count = 0
//                                        Toast.makeText(
//                                            this@BussinessForm,
//                                            " " + jsonObject.getString("message"),
//                                            Toast.LENGTH_SHORT
//                                        ).show()

                                    }


                                } catch (e: Exception) {
//                                    setSubmitData()
                                    Log.e(TAG, "uploadImage: " + e.message)
                                    FirebaseCrashlytics.getInstance().recordException(e)
                                    showToast(e.message.toString())
                                    showUploadErrorDialog()
                                }


                            }

                        })
                } catch (e: Exception) {
                    Log.e(TAG, "uploadImage: " + e.message)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast(e.message.toString())
                    dismissProgressDialog()
                    showUploadErrorDialog()
                }

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                showToast(e.message.toString())
                showUploadErrorDialog()
            }
        }
//        thread1.join()
        thread1.start()
    }

    private val selectedImages = ArrayList<Uri>()
    private val selectedImages1 = ArrayList<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageAdapter1: ImageAdapter1
    val spinnerfamilystatus = arrayOf("Select Family Status", "Joint Family", "Nuclear Family")
    val houseStatusOptions = arrayOf("Select House Status", "Owned", "Rented")
    lateinit var houseStatusAdapter: ArrayAdapter<String>
    lateinit var Adapter_spinnerfamilystatus: ArrayAdapter<String>
    var soundPool: SoundPool? = null
    var shutterSound: Int?=null
    var isCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_salary_form)

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)


        if (intent.extras != null) {
            case_id = intent.getStringExtra("case_id").toString()
            isCompleted = intent.getBooleanExtra("Completed",false)
        }
        if (isCompleted) {
            binding.submitButton.setText("Preview & Update")
        }

//        val REQUIRED_PERMISSIONSOne = mutableListOf(
//            Manifest.permission.CAMERA
//        ).apply {
//
//                add(Manifest.permission.READ_EXTERNAL_STORAGE)
//                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                add(Manifest.permission.READ_MEDIA_IMAGES)
//            add(Manifest.permission.ACCESS_FINE_LOCATION)
//            add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }.toTypedArray()
//
//
//        ActivityCompat.requestPermissions(
//            this,
//            REQUIRED_PERMISSIONSOne,
//            10001
//        )

        val employmentStatusOptions = arrayOf("Joint Family", "Nuclear Family")
        val adapter = ArrayAdapter(this, R.layout.list_item, employmentStatusOptions)
        binding.spinnerHouseStatus.setAdapter(adapter)

        houseStatusAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, houseStatusOptions)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHouseStatus.adapter = houseStatusAdapter

        soundPool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0)
        shutterSound = soundPool!!.load(this, R.raw.image, 0)

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
            saveFormData(binding, case_id, this@SalaryForm, false)
            if (true) {
                val requestJson = buildLoanRequestJson()
                Log.e(TAG, "requestJson: " + requestJson)

                JsonFieldsPreviewDialog(requestJson) {
                    binding.submitButton.setText("Submit Application")
                    val color = ContextCompat.getColor(this, R.color.theme_color)
                    binding.submitButton.setBackgroundColor(color)

                    processSubmit()
                }.show(supportFragmentManager, "jsonPreview")
            }
        }

        val storage: TextView? = findViewById(R.id.storage)

        storage?.setOnClickListener {
            checkPermissionsCamera()
            binding.image.setText("Selected Images: " + selectedImages.size)
        }

        imageAdapter = ImageAdapter(selectedImages)
        binding.recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages.adapter = imageAdapter

        imageAdapter1 = ImageAdapter1(selectedImages1)
        binding.recyclerViewImages1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages1.adapter = imageAdapter1

        if (!isNetworkAvailable(this)) {
            val message = "No internet connection"
            Log.e("@@TAG", message)
            showToast(message)
            addressString = message
        }

        setlocation()
        Handler(Looper.getMainLooper()).postDelayed({
            setlocation()
        }, 6 * 60 * 1000)

        progressDialog = SweetAlertDialog(
            ContextThemeWrapper(
                this, com.loan_verifier.loan.R.style.ThemeOverlay_MaterialComponents_Dialog
            ), SweetAlertDialog.PROGRESS_TYPE
        )
        progressDialog.setTitleText("Loading...")
        progressDialog.setCancelable(false)

//
//        binding.submitSave.setOnClickListener {
//            saveFormData(binding, case_id, this@SalaryForm, true)
//        }

        loadFormData(case_id)

        previewView = binding.previewView
        captureButton = binding.captureButton
        captureButton.setOnClickListener {
            captureImage()
            binding.laoutCameraVisible.visibility = View.VISIBLE
            binding.laoutCameraHide.visibility = View.GONE
        }

        binding.done.setOnClickListener {
            binding.laoutCameraVisible.visibility = View.GONE
            binding.laoutCameraHide.visibility = View.VISIBLE
        }
        binding.cameraChange.setOnClickListener {
            switchCamera()
        }

//        Handler().postDelayed(Runnable {
//            try {
//                saveFormData(binding, case_id, this@SalaryForm, true)
//            } catch (e: Exception) {
//                FirebaseCrashlytics.getInstance().recordException(e)
//            }
//        }, 3000)

        imageAdapter.imageActionListener = object : ImageAdapter.OnImageActionListener {
            override fun onDeleteImage(position: Int) {
                runOnUiThreadSafely {
                    imageAdapter.remove(selectedImages.get(position))
                    imageAdapter1.remove(selectedImages1.get(position))
                    binding.image.text = "Selected Images: ${selectedImages.size}"
                }
            }
        }
    }

    lateinit var progressDialog: SweetAlertDialog

    fun processSubmit() {
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

                        if (selectedImages.isNotEmpty()) {
                            count=0
                            showImageUploadDialog(selectedImages.size)
                            uploadImage(selectedImages[count])
                            count++
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

        } else {
            showToast("Storage permission is needed to continue.")
        }
    }
    fun setlocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    /*  private fun getAddressFromLocation(lat: Double, lon: Double) {
          val geocoder = Geocoder(this, Locale.getDefault())

          try {
              val addresses = geocoder.getFromLocation(lat, lon, 1)
              if (!addresses.isNullOrEmpty()) {
                  val address = addresses[0].getAddressLine(0)  // Full address
                  val city = addresses[0].locality              // City
                  val country = addresses[0].countryName        // Country
                  addLat = lat.toString()
                  addLong = lon.toString()
                  addAddress = "Address: $address\n City: $city\n Country: $country "
                  addressString = "Address: $address\n City: $city\n Country: $country "
              } else {
                  addressString = "Location not found!"
              }
  //            Toast.makeText(this, "addressString "+addressString, Toast.LENGTH_SHORT).show()
          } catch (e: Exception) {
              e.printStackTrace()
              FirebaseCrashlytics.getInstance().recordException(e)
              Log.e("@@TAG", "getAddressFromLocation: " + e.message)
              addressString = "Error fetching location"
          }

      }
  */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

/*
    fun getAddressFromLocation(lat: Double, lon: Double) {
        if (!isNetworkAvailable(this)) {
            addressString = "No internet connection"
            Log.e("@@TAG", "No internet connection for geocoding")
            showToast(addressString!!)
            return
        }

        if (!Geocoder.isPresent()) {
            addressString = "Geocoder service not available"
            Log.e("@@TAG", "Geocoder service not available on device")
            showToast(addressString!!)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@SalaryForm, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        val city = addresses[0].locality ?: "Unknown City"
                        val country = addresses[0].countryName ?: "Unknown Country"

                        addLat = lat.toString()
                        addLong = lon.toString()
                        addAddress = "Address: $address\nCity: $city\nCountry: $country"
                        addressString = addAddress
                    } else {
                        addressString = "Location not found!"
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    addressString = "Geocoder service not available"
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "IOException in getAddressFromLocation: ${e.message}")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addressString = "Error fetching location"
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "Exception in getAddressFromLocation: ${e.message}")
            }
        }
    }
*/

    fun getAddressFromLocation(lat: Double, lon: Double) {
        if (!isNetworkAvailable(this)) {
            val message = "No internet connection"
            Log.e("@@TAG", message)
            showToast(message)
            addressString = message
            return
        }

        if (!Geocoder.isPresent()) {
            val message = "Geocoder service not available on this device"
            Log.e("@@TAG", message)
            showToast(message)
            addressString = message
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@SalaryForm, Locale.getDefault())
                val addresses = withTimeout(5000L) {
                    geocoder.getFromLocation(lat, lon, 1)
                }

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressLine = address.getAddressLine(0) ?: "Unknown Address"
                        val city = address.locality ?: "Unknown City"
                        val country = address.countryName ?: "Unknown Country"

                        addLat = lat.toString()
                        addLong = lon.toString()
                        addAddress = "Address: $addressLine\nCity: $city\nCountry: $country"
                        addressString = addAddress
//                        showToast("Location fetched successfully")
                    } else {
                        addressString = "Location not found!"
                        showToast(addressString!!)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    addressString = "Geocoding timed out. Try again."
                    showToast(addressString!!)
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "TimeoutException in getAddressFromLocation: ${e.message}")
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    addressString = "Unable to fetch location (IO error)"
                    showToast(addressString!!)
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "IOException in getAddressFromLocation: ${e.message}")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addressString = "Unexpected error while fetching location"
                    showToast(addressString!!)
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "Exception in getAddressFromLocation: ${e.message}")
            }
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
                showToast("Please turn on your location...")
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
                showToast("Permission Denied!")
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkAndOpenCamera(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@SalaryForm, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this@SalaryForm, arrayOf(permission), requestCode)
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("You have denied location permission. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showToast("Permission still denied!")
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

    fun Float.toPx(context: Context): Float {
        return this * context.resources.displayMetrics.scaledDensity
    }


    fun getCorrectlyOrientedBitmap(imagePath: String): Bitmap {
        val originalBitmap = BitmapFactory.decodeFile(imagePath)
        val exif = ExifInterface(imagePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
    }


    fun addTimestampToImage(file: File) {
        var bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IllegalArgumentException("Invalid image file")

        // Correct the image orientation first (fix rotation if needed)
        val bitmap1 = getCorrectlyOrientedBitmap(file.absolutePath)
        val width = bitmap1.width
        val height = bitmap1.height

        if (width > height) {
            Log.d("@@ImageCheck", "Landscape image")
        } else if (height > width) {
            Log.d("@@ImageCheck", "Portrait image")
        } else {
            Log.d("@@ImageCheck", "Square image")
        }

        bitmap = rotateImageIfRequired(bitmap, file)


        var tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f.toPx(this@SalaryForm)
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val timeStamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        ).format(Date())

        val line1 = timeStamp
        val line2 = "Lat: $addLat" + " Long: $addLong"
        val address = addressString?.toString() ?: ""
        val mid = address.length / 2
        val line3 = if (address.isNotEmpty()) address.substring(0, mid) else ""
        val line4 = if (address.length > mid) address.substring(mid) else ""

//        val line3 = addressString.toString().subSequence(0, addressString.toString().length / 2)
//        val line4 = addressString.toString()
//            .subSequence(addressString.toString().length / 2, addressString.toString().length - 1)

        val lines = listOf(line1, line2, line3, line4)

        val padding = 30f
        val lineSpacing = 20f

        val textHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top
        val totalTextHeight = lines.size * textHeight + (lines.size - 1) * lineSpacing

        val centerX = tempBitmap.width / 2f
        val startY = tempBitmap.height - totalTextHeight - padding

        val rectLeft = 0f
        val rectTop = startY - padding
        val rectRight = tempBitmap.width.toFloat()
        val rectBottom = tempBitmap.height.toFloat()

        val backgroundPaint = Paint().apply {
            color = Color.BLACK
            alpha = 150
        }

        // Draw background rectangle
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, backgroundPaint)

        var y = startY
        for (line in lines) {
            y += textHeight
            canvas.drawText(line.toString(), centerX, y, textPaint)
            y += lineSpacing
        }

        FileOutputStream(file).use { out ->
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
        }

//        tempBitmap = rotateImage(tempBitmap, 180f)
//        FileOutputStream(file).use { out ->
//            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
//        }

//        bitmap = rotateImageIfRequired(bitmap, file)
    }

    fun rotateImageIfRequired(img: Bitmap, selectedFile: File): Bitmap {
        val ei = ExifInterface(selectedFile.absolutePath)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    private var imageUri: Uri? = null

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

    fun buildLoanRequestJson(): JsonObject {
        val requestBody = JsonObject()
//        requestBody.addProperty("case_id", "42071")
        requestBody.addProperty("case_id", case_id)
        requestBody.addProperty("loan_amt", binding.loanAmountEditText.text.toString())
        requestBody.addProperty("visit_address", binding.visitedAddress.text.toString())
        requestBody.addProperty("addressNew", binding.addressNew.text.toString())
        requestBody.addProperty("caseinitiatedAddress", binding.caseinitiatedAddress.text.toString())
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
            "name_board_seen", if (binding.OfficeNameBoardObservedTwo.isChecked) "Yes" else "No"
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
            if (binding.EnterNeighborCheckStatusone.isChecked) "Positive" else "Negative"
        )
        requestBody.addProperty(
            "business_setup", if (binding.radioButtonOfficeSetup.isChecked) "Good" else "Average"
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
            "salary_received", if (binding.SalaryReceivedInone.isChecked) "Bank" else "Cash"
        )
        requestBody.addProperty("assets_owned", binding.Assets.text.toString())
        requestBody.addProperty("image_name", srtarr.toString())
        requestBody.addProperty("file_name", srtarr.toString())

        if (binding.spinnerHouseStatus.selectedItemPosition == 1) {
            requestBody.addProperty(
                "value_of_house", binding.houseValueRentEditText.text.toString()
            )
        } else if (binding.spinnerHouseStatus.selectedItemPosition == 2) {
            requestBody.addProperty("rent_of_house", binding.houseValueRentEditText.text.toString())
        }

        return requestBody;
    }
    fun setSubmitData() {

        showProgressDialog()
        try {
            deviceID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            saveFormData(binding, case_id, this@SalaryForm, false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }




        val apiInterface: ApiInterface =
            RetrofitManager().instance1!!.create(ApiInterface::class.java)

        var requestBody = buildLoanRequestJson()
        Log.e("@@TAG--------------", requestBody.toString());

        apiInterface.submitLoanDetails(requestBody)?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dismissProgressDialog()
                showToast(t.message!!)
            }

            override fun onResponse(
                call: Call<JsonObject>, response: Response<JsonObject>
            ) {
                dismissProgressDialog()
                Log.e("@@TAG", "onResponse: " + response.body())
                try {


                    val jsonObject = JSONObject(response.body().toString())

                    if (jsonObject.getBoolean("status")) {

                        try {
                            val successDialog = SweetAlertDialog(
                                ContextThemeWrapper(
                                    this@SalaryForm, R.style.ThemeOverlay_MaterialComponents_Dialog
                                ), SweetAlertDialog.SUCCESS_TYPE
                            )

                            successDialog.setTitleText("Success!")
                                .setContentText("Details submitted successfully")
                                .setConfirmText("OK")
                                .setConfirmClickListener {
                                    it.dismissWithAnimation()

                                    this@SalaryForm.startActivity(
                                        Intent(
                                            this@SalaryForm, MainActivity::class.java
                                        )
                                    )
                                    finish()

                                }.setCancelable(false);
                            successDialog.show();

//                        successDialog.setOnShowListener {
                            val confirmButton =
                                successDialog.findViewById<TextView>(R.id.confirm_button)
                            confirmButton?.setBackgroundColor(Color.parseColor("#FF6200EE")) // Use your custom color
                            confirmButton?.setTextColor(Color.BLACK) // Ensure text is visible
                            confirmButton?.setBackgroundColor(Color.GREEN)
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                            showToast("submited succefully")
                        }

                        try {
//                            selectedImages.forEach {
//                                contentResolver.delete(it, null, null)
//                            }
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }

                    }

                } catch (e: JSONException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("@@TAG", "onResponse: " + e.message)
                    e.printStackTrace()
                    showToast(e.message!!)
                    dismissProgressDialog()
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

    fun saveFormData(
        binding: ActivitySalaryFormBinding, case_id: String, context: Context, status: Boolean
    ) {

        if (case_id.isNullOrEmpty()) {
            return
        }

        val requestBody = JsonObject()

        // Helper functions for safety
        fun safeText(value: CharSequence?): String =
            value?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""

        //        fun safeSpinnerItem(list: Array<String>, pos: Int): String = list.getOrNull(pos)?.trim() ?: ""
        fun safeSpinnerItem(options: Array<String>, index: Int): String {
            return if (index in options.indices) {
                options[index]
            } else {
                Log.w(
                    TAG,
                    "Spinner index $index is out of bounds for array: ${options.joinToString()}"
                )
                ""
            }
        }

        requestBody.addProperty("case_id", case_id)
        requestBody.addProperty("loan_amt", safeText(binding.loanAmountEditText.text))
        requestBody.addProperty("visit_address", safeText(binding.visitedAddress.text))
        requestBody.addProperty("caseinitiatedAddress", safeText(binding.caseinitiatedAddress.text))
        requestBody.addProperty("addressNew", safeText(binding.addressNew.text))
        requestBody.addProperty("date_of_visit", safeText(binding.txtVisitedDate.text))
        requestBody.addProperty("time", safeText(binding.visitedTime.text))
        requestBody.addProperty("person_meet", safeText(binding.persontmeetnameEditText.text))
        requestBody.addProperty("loan_purpose", safeText(binding.loanPurposeEditText.text))
        requestBody.addProperty("name_of_organization", safeText(binding.orgnization.text))
        requestBody.addProperty("work_experience", safeText(binding.workexprience.text))
        requestBody.addProperty("education", safeText(binding.education.text))
        requestBody.addProperty("remark", safeText(binding.remarkText.text))
        requestBody.addProperty(
            "number_of_family_members",
            safeText(binding.numberoffamilynumber.text)
        )

        requestBody.addProperty("lat", addLat ?: "")
        requestBody.addProperty("long", addLong ?: "")
        requestBody.addProperty("location", addAddress ?: "")
        requestBody.addProperty("submitTime", getTime() ?: "")
        requestBody.addProperty("submitDate", getDate() ?: "")
        requestBody.addProperty("deviceID", deviceID?.toString() ?: "")

        // Salary-specific fields
        requestBody.addProperty("gross_salary", safeText(binding.grossSalaryEditText.text))
        requestBody.addProperty("net_salary", safeText(binding.netSalaryEditText.text))
        requestBody.addProperty(
            "current_position_of_employee",
            safeText(binding.currentPositionEditText.text)
        )
        requestBody.addProperty("date_of_joining", safeText(binding.txtDateofjoin.text))
        requestBody.addProperty(
            "other_income_of_applicant",
            safeText(binding.otherIncomeEditText.text)
        )
        requestBody.addProperty(
            "previous_employement_details",
            safeText(binding.previousEmploymentEditText.text)
        )
        requestBody.addProperty("office_timings", safeText(binding.officeTimingsEditText.text))
        requestBody.addProperty("employer_name", safeText(binding.employerNameEditText.text))
        requestBody.addProperty("employer_mobile_no", safeText(binding.employerNumberEditText.text))
        requestBody.addProperty("co_employee_name", safeText(binding.coEmployeeNameEditText.text))
        requestBody.addProperty("co_employee_mobile_no", safeText(binding.coEmployeeNumber.text))



        requestBody.addProperty(
            "monthly_family_expenditure",
            safeText(binding.familymonthlyExpenditure.text)
        )
        requestBody.addProperty(
            "family_status",
            safeSpinnerItem(spinnerfamilystatus, binding.spinnerFamilystatus.selectedItemPosition)
        )
        requestBody.addProperty(
            "house_status",
            safeSpinnerItem(houseStatusOptions, binding.spinnerHouseStatus.selectedItemPosition)
        )
        requestBody.addProperty("house_size", safeText(binding.editTextHouseSize.text))
        requestBody.addProperty(
            "residence_at_address_since",
            safeText(binding.editTextResidenceSince.text)
        )
        requestBody.addProperty("value_of_house", safeText(binding.houseValueRentEditText.text))
        requestBody.addProperty("rent_of_house", safeText(binding.houseValueRentEditText.text))

        requestBody.addProperty(
            "office_setup_seen",
            if (binding.radioButtonOfficeSetup?.isChecked == true) "Good" else "Average"
        )

        requestBody.addProperty("pan_number", safeText(binding.panNumberEditText.text))
        requestBody.addProperty(
            "security_offered_against_loan",
            safeText(binding.securityOfferedEditText.text)
        )
        requestBody.addProperty(
            "address_of_security",
            safeText(binding.addressOfSecurityEditText.text)
        )
        requestBody.addProperty("security_value", safeText(binding.valueOfSecurityEditText.text))
        requestBody.addProperty("size_of_security", safeText(binding.sizeOfSecurityEditText.text))

        requestBody.addProperty(
            "neighbour_check_status",
            if (binding.EnterNeighborCheckStatusone?.isChecked == true) "Positive" else "Negative"
        )

        requestBody.addProperty("assets_owned", safeText(binding.Assets.text))
        requestBody.addProperty("earning_family_members", familyIncomeDataArray?.toString() ?: "[]")
        requestBody.addProperty("current_loans", loanDataArray?.toString() ?: "[]")
        requestBody.addProperty("bank_details", bankdetailsDataArray?.toString() ?: "[]")
        requestBody.addProperty("holiday", safeText(binding.holidayEditText.text))

        requestBody.addProperty(
            "salary_received",
            if (binding.SalaryReceivedInone?.isChecked == true) "Bank" else "Cash"
        )
        requestBody.addProperty(
            "office_setup_seen",
            if (binding.radioButtonOfficeSetup?.isChecked == true) "Good" else "Average"
        )
        requestBody.addProperty(
            "name_board_seen",
            if (binding.OfficeNameBoardObservedOne?.isChecked == true) "Yes" else "No"
        )
        requestBody.addProperty(
            "neighbour_check_status",
            if (binding.EnterNeighborCheckStatusone?.isChecked == true) "Positive" else "Negative"
        )
        val jsonArray = JSONArray()

        selectedImages.forEach {
            Log.e("@@TAG", "Form Data: $it")
        }
        selectedImages.forEach { jsonArray.put(it.toString()) }
        requestBody.addProperty(
            "imageList", jsonArray.toString()
        )
        // Log and Save
        Log.e("@@TAG", "Form Data: $requestBody")

        val sharedPreferences =
            context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(case_id, requestBody.toString()).apply()
    }

    /*
        fun saveFormData2(
            binding: ActivitySalaryFormBinding, case_id: String, context: Context, status: Boolean
        ) {
            val requestBody = JsonObject()

            // Helper functions for safety
            fun safeText(value: CharSequence?): String = value?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
            fun safeOption(list: List<String>, pos: Int): String = list.getOrNull(pos)?.trim() ?: ""

            requestBody.addProperty("case_id", case_id)
            requestBody.addProperty("loan_amt", safeText(binding.loanAmountEditText.text))
            requestBody.addProperty("visit_address", safeText(binding.visitedAddress.text))
            requestBody.addProperty("date_of_visit", safeText(binding.txtVisitedDate.text))
            requestBody.addProperty("time", safeText(binding.visitedTime.text))
            requestBody.addProperty("person_meet", safeText(binding.persontmeetnameEditText.text))
            requestBody.addProperty("loan_purpose", safeText(binding.loanPurposeEditText.text))
            requestBody.addProperty("name_of_organization", safeText(binding.orgnization.text))
            requestBody.addProperty("work_experience", safeText(binding.workexprience.text))
            requestBody.addProperty("education", safeText(binding.education.text))
            requestBody.addProperty("remark", safeText(binding.remarkText.text))
            requestBody.addProperty("number_of_family_members", safeText(binding.numberoffamilynumber.text))

            requestBody.addProperty("lat", addLat ?: "")
            requestBody.addProperty("long", addLong ?: "")
            requestBody.addProperty("location", addAddress ?: "")
            requestBody.addProperty("submitTime", getTime() ?: "")
            requestBody.addProperty("submitDate", getDate() ?: "")
            requestBody.addProperty("deviceID", deviceID?.toString() ?: "")

            // Salary-specific fields
            requestBody.addProperty("gross_salary", safeText(binding.grossSalaryEditText.text))
            requestBody.addProperty("net_salary", safeText(binding.netSalaryEditText.text))
            requestBody.addProperty("current_position_of_employee", safeText(binding.currentPositionEditText.text))
            requestBody.addProperty("date_of_joining", safeText(binding.txtDateofjoin.text))
            requestBody.addProperty("other_income_of_applicant", safeText(binding.otherIncomeEditText.text))
            requestBody.addProperty("previous_employement_details", safeText(binding.previousEmploymentEditText.text))
            requestBody.addProperty("office_timings", safeText(binding.officeTimingsEditText.text))
            requestBody.addProperty("employer_name", safeText(binding.employerNameEditText.text))
            requestBody.addProperty("employer_mobile_no", safeText(binding.employerNumberEditText.text))
            requestBody.addProperty("co_employee_name", safeText(binding.coEmployeeNameEditText.text))
            requestBody.addProperty("co_employee_mobile_no", safeText(binding.coEmployeeNumber.text))

            requestBody.addProperty(
                "salary_received",
                if (binding.SalaryReceivedInone?.isSelected == true) "Bank" else "Cash"
            )

            requestBody.addProperty("monthly_family_expenditure", safeText(binding.familymonthlyExpenditure.text))
            requestBody.addProperty(
                "family_status",
                safeOption(spinnerfamilystatus, binding.spinnerFamilystatus.selectedItemPosition)
            )
            requestBody.addProperty(
                "house_status",
                safeOption(houseStatusOptions, binding.spinnerHouseStatus.selectedItemPosition)
            )
            requestBody.addProperty("house_size", safeText(binding.editTextHouseSize.text))
            requestBody.addProperty("residence_at_address_since", safeText(binding.editTextResidenceSince.text))
            requestBody.addProperty("value_of_house", safeText(binding.houseValueRentEditText.text))
            requestBody.addProperty("rent_of_house", safeText(binding.houseValueRentEditText.text))

            requestBody.addProperty(
                "office_setup_seen",
                if (binding.radioButtonOfficeSetup?.isSelected == true) "Good" else "Average"
            )

            requestBody.addProperty("pan_number", safeText(binding.panNumberEditText.text))
            requestBody.addProperty("security_offered_against_loan", safeText(binding.securityOfferedEditText.text))
            requestBody.addProperty("address_of_security", safeText(binding.addressOfSecurityEditText.text))
            requestBody.addProperty("security_value", safeText(binding.valueOfSecurityEditText.text))
            requestBody.addProperty("size_of_security", safeText(binding.sizeOfSecurityEditText.text))

            requestBody.addProperty(
                "neighbour_check_status",
                if (binding.EnterNeighborCheckStatusone?.isSelected == true) "Positive" else "Negative"
            )

            requestBody.addProperty("assets_owned", safeText(binding.Assets.text))
            requestBody.addProperty("earning_family_members", familyIncomeDataArray?.toString() ?: "[]")
            requestBody.addProperty("current_loans", loanDataArray?.toString() ?: "[]")
            requestBody.addProperty("bank_details", bankdetailsDataArray?.toString() ?: "[]")
            requestBody.addProperty("holiday", safeText(binding.holidayEditText.text))

            // Log and Save
            Log.e("@@TAG", "Form Data: $requestBody")

            val sharedPreferences = context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(case_id, requestBody.toString()).apply()
        }
    */

    /*
        fun saveFormData(
            bindinge: ActivitySalaryFormBinding, case_id: String, context: Context, status: Boolean
        ) {
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
            requestBody.addProperty(
                "number_of_family_members", binding.numberoffamilynumber.text.toString()
            )
            requestBody.addProperty("lat", addLat)
            requestBody.addProperty("long", addLong)
            requestBody.addProperty("location", addAddress)
            requestBody.addProperty("submitTime", getTime())
            requestBody.addProperty("submitDate", getDate())
            requestBody.addProperty("deviceID", deviceID.toString())

            // 🔹 Missing Fields from Salary Form
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
            requestBody.addProperty("employer_name", binding.employerNameEditText.text.toString())
            requestBody.addProperty(
                "employer_mobile_no", binding.employerNumberEditText.text.toString()
            )
            requestBody.addProperty("co_employee_name", binding.coEmployeeNameEditText.text.toString())
            requestBody.addProperty("co_employee_mobile_no", binding.coEmployeeNumber.text.toString())
            requestBody.addProperty(
                "salary_received", if (binding.SalaryReceivedInone.isSelected) "Bank" else "Cash"
            )

            requestBody.addProperty(
                "monthly_family_expenditure", binding.familymonthlyExpenditure.text.toString()
            )
            requestBody.addProperty(
                "family_status", spinnerfamilystatus[binding.spinnerFamilystatus.selectedItemPosition]
            )
            requestBody.addProperty(
                "house_status", houseStatusOptions[binding.spinnerHouseStatus.selectedItemPosition]
            )
            requestBody.addProperty("house_size", binding.editTextHouseSize.text.toString())
            requestBody.addProperty(
                "residence_at_address_since", binding.editTextResidenceSince.text.toString()
            )
            requestBody.addProperty("value_of_house", binding.houseValueRentEditText.text.toString())
            requestBody.addProperty("rent_of_house", binding.houseValueRentEditText.text.toString())
            requestBody.addProperty(
                "office_setup_seen",
                if (binding.radioButtonOfficeSetup.isSelected) "Good" else "Average"
            )
            requestBody.addProperty("pan_number", binding.panNumberEditText.text.toString())
            requestBody.addProperty(
                "security_offered_against_loan", binding.securityOfferedEditText.text.toString()
            )
            requestBody.addProperty(
                "address_of_security", binding.addressOfSecurityEditText.text.toString()
            )
            requestBody.addProperty("security_value", binding.valueOfSecurityEditText.text.toString())
            requestBody.addProperty("size_of_security", binding.sizeOfSecurityEditText.text.toString())
            requestBody.addProperty(
                "neighbour_check_status",
                if (binding.EnterNeighborCheckStatusone.isSelected) "Positive" else "Negative"
            )
            requestBody.addProperty("assets_owned", binding.Assets.text.toString())
            requestBody.addProperty("earning_family_members", familyIncomeDataArray.toString())
            requestBody.addProperty("current_loans", loanDataArray.toString())
            requestBody.addProperty("bank_details", bankdetailsDataArray.toString())
            requestBody.addProperty("holiday", binding.holidayEditText.text.toString())
            requestBody.addProperty("assets_owned", binding.Assets.text.toString())


            Log.e("@@TAG", "Form Data: $requestBody")

            val sharedPreferences =
                context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(case_id, requestBody.toString()).apply()

    //        if (status) {
    //            val successDialog = SweetAlertDialog(
    //                ContextThemeWrapper(context, R.style.ThemeOverlay_MaterialComponents_Dialog),
    //                SweetAlertDialog.SUCCESS_TYPE
    //            )
    //            successDialog.setTitleText("Success!").setContentText("Details Saved")
    //                .setConfirmText("OK").setConfirmClickListener {
    //                    it.dismissWithAnimation()
    //                    (context as Activity).startActivity(Intent(context, MainActivity::class.java))
    //                    (context as Activity).finish()
    //                }.show()
    //
    //            val confirmButton = successDialog.findViewById<TextView>(R.id.confirm_button)
    //            confirmButton?.setBackgroundColor(Color.parseColor("#FF6200EE"))
    //            confirmButton?.setTextColor(Color.BLACK)
    //            confirmButton?.setBackgroundColor(Color.GREEN)
    //        }
        }
    */

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

    fun loadFormData(caseId: String) {
        try {
            val sharedPreferences = getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString(caseId, null) ?: return
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            // Helper: Safely get string from JSON
            fun getString(key: String): String = jsonObject.get(key)?.asString ?: ""

            // Helper: Parse JSON array safely
            fun parseJsonArrayField(key: String): JsonArray {
                return try {
                    val jsonArrayString = jsonObject.get(key)?.asString
                    if (!jsonArrayString.isNullOrBlank()) {
                        JsonParser.parseString(jsonArrayString).asJsonArray
                    } else JsonArray()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("LoadData", "Error parsing $key: ${e.message}")
                    JsonArray()
                }
            }

            // Helper: Get spinner index from array
            fun getSpinnerIndex(options: Array<String>, value: String?): Int {
                return value?.trim()?.let {
                    options.indexOfFirst { it.trim() == value }
                }?.takeIf { it >= 0 } ?: 0
            }

            with(binding) {
                // Text fields
                loanAmountEditText.setText(getString("loan_amt"))
                visitedAddress.setText(getString("visit_address"))
                visitedAddress.setText(getString("addressNew"))
                visitedAddress.setText(getString("caseinitiatedAddress"))
//
//                txtVisitedDate.setText(getString("date_of_visit"))
//                visitedTime.setText(getString("time"))

                setCurrentDate(txtVisitedDate)
                setCurrentTime(visitedTime)

                persontmeetnameEditText.setText(getString("person_meet"))
                loanPurposeEditText.setText(getString("loan_purpose"))
                orgnization.setText(getString("name_of_organization"))
                workexprience.setText(getString("work_experience"))
                education.setText(getString("education"))
                remarkText.setText(getString("remark"))
                numberoffamilynumber.setText(getString("number_of_family_members"))
                grossSalaryEditText.setText(getString("gross_salary"))
                netSalaryEditText.setText(getString("net_salary"))
                currentPositionEditText.setText(getString("current_position_of_employee"))
                txtDateofjoin.setText(getString("date_of_joining"))
                otherIncomeEditText.setText(getString("other_income_of_applicant"))
                previousEmploymentEditText.setText(getString("previous_employement_details"))
                officeTimingsEditText.setText(getString("office_timings"))
                employerNameEditText.setText(getString("employer_name"))
                employerNumberEditText.setText(getString("employer_mobile_no"))
                coEmployeeNameEditText.setText(getString("co_employee_name"))
                coEmployeeNumber.setText(getString("co_employee_mobile_no"))
                familymonthlyExpenditure.setText(getString("monthly_family_expenditure"))
                editTextHouseSize.setText(getString("house_size"))
                editTextResidenceSince.setText(getString("residence_at_address_since"))
                houseValueRentEditText.setText(getString("value_of_house"))
                panNumberEditText.setText(getString("pan_number"))
                securityOfferedEditText.setText(getString("security_offered_against_loan"))
                addressOfSecurityEditText.setText(getString("address_of_security"))
                valueOfSecurityEditText.setText(getString("security_value"))
                sizeOfSecurityEditText.setText(getString("size_of_security"))
                holidayEditText.setText(getString("holiday"))
                Assets.setText(getString("assets_owned"))

                // Radio buttons: Salary received

                // You can add more logic for other options like "Average", "Bad", etc.

                // Spinners
                val familyStatusIndex =
                    getSpinnerIndex(spinnerfamilystatus, getString("family_status"))
                val houseStatusIndex =
                    getSpinnerIndex(houseStatusOptions, getString("house_status"))

                spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus
                spinnerHouseStatus.adapter = houseStatusAdapter
                spinnerFamilystatus.setSelection(familyStatusIndex)
                spinnerHouseStatus.setSelection(houseStatusIndex)
                Adapter_spinnerfamilystatus.notifyDataSetChanged()
                houseStatusAdapter.notifyDataSetChanged()

                val salaryReceived = getString("salary_received")
                SalaryReceivedInTwo.isChecked = salaryReceived == "Cash"

//                if (salaryReceived == "Bank") {
//                    SalaryReceivedInTwo.isChecked = true
//                }

                // Radio button: Office setup seen
                val officeSetup = getString("office_setup_seen")
                radioButtonOfficeSetupTwo.isChecked = officeSetup == "Average"

                val name_board_seen = getString("name_board_seen")
                OfficeNameBoardObservedTwo.isChecked = name_board_seen == "No"

                val enterNeighborCheckStatusTwo = getString("neighbour_check_status")
                EnterNeighborCheckStatusTwo.isChecked = enterNeighborCheckStatusTwo == "Negative"
            }

            // Load arrays
            familyIncomeDataArray = parseJsonArrayField("earning_family_members")
            loanDataArray = parseJsonArrayField("current_loans")
            bankdetailsDataArray = parseJsonArrayField("bank_details")

            // Populate dynamic fields
            setFamilyIncomeFieldsFromJsonArray()

            val imageListElement = getString("imageList")
            Log.e(TAG, "loadFormData: "+imageListElement)
            val uriList = ArrayList<Uri>()

            if (imageListElement != null) {
                val rawJsonArrayString =
                    imageListElement

                try {
                    val jsonArray = JSONArray(rawJsonArrayString)
                    for (i in 0 until jsonArray.length()) {


                        val uriString = jsonArray.getString(i)
                        val uri = Uri.parse(uriString)
                        if (uriExistsInMemory(uri)) {
                            Log.e(TAG, "loadFormData: $uri")
                            uriList.add(uri)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse imageList as JSONArray", e)
                }
            }

//            content://media/external/images/media/1000131734
//            content://media/external/images/media/1000131734
            runOnUiThreadSafely {
                selectedImages1.addAll(uriList)
                selectedImages.addAll(uriList)
            }



            try {
                imageAdapter = ImageAdapter(selectedImages)
                binding.recyclerViewImages.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewImages.adapter = imageAdapter

                imageAdapter1 = ImageAdapter1(selectedImages1)
                binding.recyclerViewImages1.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewImages1.adapter = imageAdapter1
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }

        } catch (e: Exception) {
            Log.e("LoadData", "Error in loadFormData: ${e.message}", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    fun uriExistsInMemory(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    private fun addFamilyIncomeField(
        name: String, income: String, relation: String
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
//        textView.textSize = 7f.toPx(this@SalaryForm)
        textView.textSize = 7f.toPx(this@SalaryForm)

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
        relationEditText.addTextChangedListener(textWatcher)

        val removeButton = Button(this)
        removeButton.text = "❌ Remove"
        removeButton.setOnClickListener {
            binding.layoutfamilyincome.removeView(loanCard)

            familyincome--;
            familyIncomeDataArray.remove(jsonObject) // Remove from JSON array

            Log.e("@@TAG", "addFamilyIncomeField: " + familyIncomeDataArray.size())
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
        textView.textSize = 7f.toPx(this@SalaryForm)
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
        textView.textSize = 7f.toPx(this@SalaryForm)
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

        // Parse bank details
        for (i in 0 until bankdetailsDataArray.size()) {
            try {
                val data = bankdetailsDataArray[i].asJsonObject
                val name = data.get("BankName")?.asString ?: ""
                val branch = data.get("BranchName")?.asString ?: ""
                val type = data.get("AccountType")?.asString ?: ""
                val tenureOfLoan = data.get("AccountSince")?.asString ?: ""

                if (name.isNotBlank() && branch.isNotBlank() && type.isNotBlank() && tenureOfLoan.isNotBlank()) {
                    addBackField(name, branch, type, tenureOfLoan)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadData", "Error parsing bankdetails item [$i]: ${e.message}")
            }
        }

        // Parse current loans
        for (i in 0 until loanDataArray.size()) {
            try {
                val data = loanDataArray[i].asJsonObject
                val name = data.get("BankName")?.asString ?: ""
                val typeOfLoan = data.get("TypeofLoan")?.asString ?: ""
                val emiBalance = data.get("EMIBalance")?.asString ?: ""
                val tenureOfLoan = data.get("TenureofLoan")?.asString ?: ""

                if (name.isNotBlank() && typeOfLoan.isNotBlank() && emiBalance.isNotBlank() && tenureOfLoan.isNotBlank()) {
                    addLoanField(name, emiBalance, typeOfLoan, tenureOfLoan)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadData", "Error parsing loan item [$i]: ${e.message}")
            }
        }

        // Parse earning family members
        for (i in 0 until familyIncomeDataArray.size()) {
            try {
                val data = familyIncomeDataArray[i].asJsonObject
                val name = data.get("Name")?.asString ?: ""
                val income = data.get("income")?.asString ?: ""
                val relation = data.get("relation")?.asString ?: ""

                if (name.isNotBlank() && income.isNotBlank() && relation.isNotBlank()) {
                    addFamilyIncomeField(name, income, relation)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadData", "Error parsing familyIncome item [$i]: ${e.message}")
            }
        }
    }

    private lateinit var previewView: PreviewView  // Displays the camera preview
    private lateinit var captureButton: ImageView  // Button to capture image

    // CameraX Image Capture Use Case
    private var imageCapture: ImageCapture? = null


    private lateinit var displayManager: DisplayManager
    private var displayId: Int = -1

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(id: Int) {
            if (id == displayId) {
                imageCapture?.targetRotation = previewView.display.rotation
            }
        }
    }

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraSelector: CameraSelector

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera() // or your method to bind use cases again
    }

    private fun startCamera() {
        Utility.hideKeyboard(this@SalaryForm)
        binding.laoutCameraVisible.visibility = View.VISIBLE
        binding.laoutCameraHide.visibility = View.GONE

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {

                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // CameraX Preview Use Case
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                displayId = previewView.display.displayId

                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display.rotation)  // <--- Important
                    .build()


                try {
                    // Unbind previous use cases and bind new ones
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast("Failed to start camera")
                    binding.laoutCameraVisible.visibility = View.GONE
                    binding.laoutCameraHide.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Captures an image and saves it to the gallery

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        try {
            soundPool!!.play(shutterSound!!, 1f, 1f, 0, 0, 1f)
        } catch (e:Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        // Create a unique filename using timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$timestamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Images"
                )  // Save to gallery
            }
        }

        // Configure output file options

        val rotation = windowManager.defaultDisplay.rotation
        imageCapture.targetRotation = rotation
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        // Capture and save the image
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    Thread {
                        // This runs in the background
                        imageUri = outputFileResults.savedUri
                        imageUri?.let { uri ->
//                            selectedImages.add(0, uri)

                            // UI updates MUST go on main thread
                            runOnUiThreadSafely {
                                imageAdapter.add(uri)
                                imageAdapter1.add(uri)
                                try {
                                    binding.recyclerViewImages1.scrollToPosition(imageAdapter1.itemCount - 1)
                                } catch (e:Exception) {

                                }
                                binding.image.text = "Selected Images: ${selectedImages.size}"
//                                binding.laoutCameraVisible.visibility = View.GONE
//                                binding.laoutCameraHide.visibility = View.VISIBLE
                            }
                        }
                    }.start()
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast("Capture again.")
                    runOnUiThreadSafely {
                        binding.laoutCameraVisible.visibility = View.GONE
                        binding.laoutCameraHide.visibility = View.VISIBLE
                    }
                }
            })
    }

    override fun onBackPressed() {
        saveFormData(binding, case_id, this@SalaryForm, true)
        if (binding.laoutCameraVisible.isVisible) {
            binding.laoutCameraVisible.visibility = View.GONE
            binding.laoutCameraHide.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }


    override fun onDestroy() {
        saveFormData(binding, case_id, this@SalaryForm, true)
        super.onDestroy()
    }

    override fun onPause() {
        saveFormData(binding, case_id, this@SalaryForm, true)
        super.onPause()
    }

    override fun onStop() {
        try {
            saveFormData(binding, case_id, this@SalaryForm, true)
        } catch (e: Exception) {
            Log.e("FormSave", "Error in onStop: ${e.message}")
            showToast("Failed to save form data (onStop)")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        super.onStop()
    }

    fun openCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            checkPermissionsCamera()
        }
    }

    fun allPermissionsGranted(): Boolean {
        // Iterate through the permissions in REQUIRED_PERMISSIONS
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false // Return false if any permission is not granted
            }
        }
        return true // Return true if all permissions are granted
    }

    val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA
    ).apply {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }.toTypedArray()

    private fun checkPermissionsCamera() {
        val permissionsNeeded = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            // Show the permission explanation dialog
            showPermissionExplanationDialog(permissionsNeeded)
        } else {
            openCamera()
        }
    }

    private fun showPermissionExplanationDialog(permissionsNeeded: List<String>) {
        AlertDialog.Builder(this).setTitle("Permissions Needed")
            .setMessage("We need these permissions to ensure full functionality. Please grant the required permissions.")
            .setPositiveButton("Grant") { _, _ ->
                // Request the permissions if user agrees
                ActivityCompat.requestPermissions(
                    this, permissionsNeeded.toTypedArray(), REQUEST_CODE
                )
            }.setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show()
                // Proceed with storage-related operations
            } else {
                Toast.makeText(
                    this,
                    "Storage permissions denied. Please allow permissions from settings.",
                    Toast.LENGTH_LONG
                ).show()

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // User permanently denied permission; show dialog to open app settings
                    AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Storage permissions are required to save and access files. Please enable them in app settings.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    // User permanently denied permission; show dialog to open app settings
                    AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Storage permissions are required to save and access files. Please enable them in app settings.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

            }
        }


        if (requestCode == REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            val permanentlyDeniedPermissions = mutableListOf<String>()

            for ((index, permission) in permissions.withIndex()) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission)

                    // Check if the user denied the permission permanently
//                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//                        permanentlyDeniedPermissions.add(permission)
//                    }
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                // Show the dialog again or take other actions

                permissionCount++
                if (permissionCount == 5) {
                    permissionCount = 0;
                    showGoToSettingsDialog()
                } else {
                    showPermissionExplanationDialog {
                        // User agrees, request permissions again
                        ActivityCompat.requestPermissions(
                            this, deniedPermissions.toTypedArray(),
                            REQUEST_CODE
                        )
                    }
                }
            }


//            if (deniedPermissions.isNotEmpty()) {
//                // Show the dialog if permissions were denied
//                if (permanentlyDeniedPermissions.isNotEmpty()) {
//                    // Show a dialog that directs the user to settings if they selected "Don't ask again"
//                    showGoToSettingsDialog()
//                } else {
//                    // If permissions were just denied but not permanently, show explanation dialog again
//                    showPermissionExplanationDialog(deniedPermissions)
//                }
//            }
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
                    showToast("Permission denied!")
                }
            }
        }

    }

    private fun showPermissionExplanationDialog(onPositiveClick: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Permissions Needed")
            .setMessage("We need these permissions to provide full functionality. Please allow them.")
            .setPositiveButton("Allow") { _, _ -> onPositiveClick() }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    var permissionCount = 0;

    // Show dialog directing the user to settings if they permanently denied the permissions
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permissions Denied Permanently")
            .setMessage("You have permanently denied the required permissions. Please go to settings and enable them manually.")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Direct user to the app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    companion object {
        private const val REQUEST_CODE = 100
        private const val PERMISSION_ID: Int = 44
    }

    private val REQUEST_CODE = 100
    fun showToast(message: String) {
        Log.e(TAG, "showToast: "+message)
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(this@SalaryForm, message, Toast.LENGTH_SHORT).show()
            } else {
                runOnUiThreadSafely {
                    try {
                        Toast.makeText(this@SalaryForm, message, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("ToastError", "UI Toast Error: ${e.message}")
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ToastError", "Toast error: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun showProgressDialog() {
        try {
            runOnUiThreadSafely {
                try {
                    if (!isFinishing && !progressDialog.isShowing) {
                        progressDialog.show()
                    }
                } catch (e: Exception) {
                    Log.e("ProgressDialog", "Error showing progress dialog: ${e.message}")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast("Failed to show progress, please wait")
                }
            }
        } catch (e: Exception) {
            Log.e("ProgressDialog", "UI thread error while showing dialog: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
            showToast("Something went wrong, please wait")
        }
    }

    fun dismissProgressDialog() {
        try {
            runOnUiThreadSafely {
                try {
                    if (!isFinishing && progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.e("ProgressDialog", "Error dismissing progress dialog: ${e.message}")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast("Failed to dismiss progress, please wait")
                }
            }
        } catch (e: Exception) {
            Log.e("ProgressDialog", "UI thread error while dismissing dialog: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
            showToast("Something went wrong, please wait")
        }
    }

    val STORAGE_PERMISSION_CODE = 1001
    fun checkAndRequestStoragePermissions(): Boolean {
        return when {

            Build.VERSION.SDK_INT <= 28 -> {
                // For Android 6 to 10 (API 23-29)
                var writePermission = true
                if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.P) {
                    writePermission = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                }

                var readPermission = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    readPermission = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                }


                if (writePermission && readPermission) {
                    true
                } else {
                    val REQUIRED_PERMISSIONS = mutableListOf(
                        Manifest.permission.CAMERA
                    ).apply {
                        // For Android 9 (Pie) or lower
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                            add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }

                        // For Android 13 (Tiramisu) and higher
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }.toTypedArray()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= 29) {
                        ActivityCompat.requestPermissions(
                            this,
                            REQUIRED_PERMISSIONS,
                            STORAGE_PERMISSION_CODE
                        )
                    }
                    false
                }
            } Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // API 33+ (Android 13+)

                val readMediaImagesPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                if (readMediaImagesPermission) {
                    true
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        STORAGE_PERMISSION_CODE
                    )
                    false
                }
            }

            else -> {
                // Below Android 6 (API 23), permissions granted at install time
                true
            }
        }
    }

    fun runOnUiThreadSafely(action: () -> Unit) {
        try {
            runOnUiThread {
                try {
                    action()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast("UI error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showToast("UI thread error: ${e.message}")
        }
    }
    fun showUploadErrorDialog() {
        runOnUiThreadSafely {
            AlertDialog.Builder(this@SalaryForm)
                .setTitle("Upload Failed")
                .setMessage("Getting issues while uploading image. Please try again.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    count=0
                    dismissProgressDialog()
                    dismissUploadDialog()
                }
                .setCancelable(true)
                .show()
        }
    }

}