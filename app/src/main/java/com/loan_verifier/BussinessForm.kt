package com.loan_verifier

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
import android.view.Gravity
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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.loan_verifier.helper.ApiInterface
import com.loan_verifier.helper.FileUtils
import com.loan_verifier.helper.RetrofitManager
import com.loan_verifier.loan.JsonFieldsPreviewDialog
import com.loan_verifier.loan.MainActivity
import com.loan_verifier.loan.R
import com.loan_verifier.loan.Utility
import com.loan_verifier.loan.databinding.ActivityBussnessFormBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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


class BussinessForm : AppCompatActivity() {

    var count: Int = 0
    var srtarr: StringBuilder = StringBuilder()
    var latitudeTextView: Double? = null
    var longitudeTextView: Double? = null
    var addressTextView: String? = " "
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var PERMISSION_ID: Int = 44
    var TAG = "@@TAG"
    lateinit var bindinge: ActivityBussnessFormBinding
    lateinit var progressDialog: SweetAlertDialog

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

            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.dialog_image_upload_progress, null)

            progressBar = dialogView.findViewById(R.id.progressBar)
            progressText = dialogView.findViewById(R.id.textProgress)

            if (progressBar == null || progressText == null) {
                Log.e("@@TAG", "Missing progressBar or progressText in layout")
                return
            }

            progressBar?.max = maxImages
            progressBar?.progress = 0
            progressText?.text = "Uploading 0 of $maxImages"

            uploadDialog =
                AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

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
            runOnUiThreadSafely {
                if (uploadDialog == null || !uploadDialog!!.isShowing) {
                    Log.w("@@TAG", "Upload dialog is not showing. Skipping update.")
                } else {

                    progressBar?.progress = current
                    progressText?.text = "Uploading $current of $max"

                    if (current >= max) {
                        dismissUploadDialog()
                        showProgressDialog() // You can customize or remove this based on your flow
                    }
                }
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
        runOnUiThreadSafely {
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

    fun uploadImage(uri: Uri) {
        Utility.hideKeyboard(this@BussinessForm)
        val thread1 = Thread {
            try {

                try {
                    runOnUiThreadSafely {
                        updateImageUploadDialog(count, selectedImages.size)
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showToast(e.message.toString())
                    showUploadErrorDialog()
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
//            progressDialog.show()


                    val file = File(FileUtils.getPath(this@BussinessForm, uri))
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

    var soundPool: SoundPool? = null
    var shutterSound: Int? = null
    var isCompleted = false

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
            isCompleted = intent.getBooleanExtra("Completed",false)
        }
        if (isCompleted) {
            bindinge.submitButton.setText("Preview & Update")
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

        soundPool = SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0)
        shutterSound = soundPool!!.load(this, R.raw.image, 0)

        bindinge.spinnerHouseStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    bindinge.houseValueRentEditTextLayout.visibility = View.VISIBLE
                    val selectedItem = houseStatusOptions[position]

                    if (selectedItem.equals("Owned")) {
                        bindinge.houseValueRentEditText.setHint("Value of House")
                    } else if (selectedItem.equals("Rented")) bindinge.houseValueRentEditText.setHint(
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
            saveFormData(bindinge, case_id, this@BussinessForm, false)
            if (true) {

                val requestJson = buildLoanRequestJson()
                Log.e(TAG, "requestJson: " + requestJson)

                JsonFieldsPreviewDialog(requestJson) {
                    bindinge.submitButton.setText("Submit Application")
                    val color = ContextCompat.getColor(this, R.color.theme_color)
                    bindinge.submitButton.setBackgroundColor(color)

                    processSubmit()
                }.show(supportFragmentManager, "jsonPreview")
            }
        }

        val storage: TextView? = findViewById(R.id.storage)

        storage?.setOnClickListener {
            checkPermissionsCamera()
            bindinge.image.setText("Selected Images: " + selectedImages.size)
        }

        imageAdapter = ImageAdapter(selectedImages)
        bindinge.recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bindinge.recyclerViewImages.adapter = imageAdapter

        imageAdapter1 = ImageAdapter1(selectedImages1)
        bindinge.recyclerViewImages1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bindinge.recyclerViewImages1.adapter = imageAdapter1

        if (!isNetworkAvailable(this)) {
            val message = "No internet connection"
            Log.e("@@TAG", message)
            showToast(message)
            addressTextView = message
        }

        setlocation()
        Handler(Looper.getMainLooper()).postDelayed({
            setlocation()
        }, 6 * 60 * 1000)


        progressDialog = SweetAlertDialog(
            ContextThemeWrapper(
                this, R.style.ThemeOverlay_MaterialComponents_Dialog
            ), SweetAlertDialog.PROGRESS_TYPE
        )
        progressDialog.setTitleText("Loading...")
        progressDialog.setCancelable(false)

        bindinge.buttonAddCustomerDetail.setOnClickListener { addCustomerDetail("", "") }
        bindinge.buttonAddSupplier.setOnClickListener { addbuttonAddSupplier("", "", "") }
//        bindinge.submitSave.setOnClickListener {
//            saveFormData(bindinge, case_id, this@BussinessForm, true)
//        }
//        bindinge.submitSave.setOnClickListener {
//            saveFormData(bindinge, case_id, this@BussinessForm, true)
//        }
//
//        progressDialog = ProgressDialog(this)
//        showProgressDialog()


        previewView = bindinge.previewView
        captureButton = bindinge.captureButton
        captureButton.setOnClickListener {
            captureImage()
            bindinge.laoutCameraVisible.visibility = View.VISIBLE
            bindinge.laoutCameraHide.visibility = View.GONE
        }

        bindinge.done.setOnClickListener {
            bindinge.laoutCameraVisible.visibility = View.GONE
            bindinge.laoutCameraHide.visibility = View.VISIBLE
        }

        bindinge.cameraChange.setOnClickListener {
            switchCamera()
        }
        loadFormData(case_id, this@BussinessForm, bindinge)

//        Handler().postDelayed(Runnable {
//            try {
//                saveFormData(bindinge, case_id, this@BussinessForm, true)
//            } catch (e: Exception) {
//                FirebaseCrashlytics.getInstance().recordException(e)
//            }
//        }, 3000)

        bindinge.radioGroupPropPartStatus.setOnCheckedChangeListener { group, checkedId ->
            if (bindinge.ProprietorRadioThree.isChecked) {
                bindinge.proprietorRadiolayout.visibility = View.VISIBLE
            } else bindinge.proprietorRadiolayout.visibility = View.INVISIBLE
        }

        imageAdapter.imageActionListener = object : ImageAdapter.OnImageActionListener {
            override fun onDeleteImage(position: Int) {
                runOnUiThreadSafely {
                    imageAdapter.remove(selectedImages.get(position))
                    imageAdapter1.remove(selectedImages1.get(position))
                    bindinge.image.text = "Selected Images: ${selectedImages.size}"
                }
            }
        }
        bindinge.txtCleanData.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Permissions Needed")
                .setMessage("Do you want to clean All Data.")
                .setPositiveButton("Clear") { _, _ ->
                    statusclear=true;
                    getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE).edit()
                        .putString(case_id, "").apply()
                    super.onBackPressed()
                }.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }
    var statusclear=false;

    fun processSubmit() {


        val dialog =
            SweetAlertDialog(this@BussinessForm, SweetAlertDialog.WARNING_TYPE).apply {
//                    setTitleText("Confirm")
                setContentText("All set to submit?")
                setConfirmText("Continue")
                setCancelText("Cancel")

                setConfirmClickListener {
                    it.dismissWithAnimation()
//
//                            showProgressDialog()
                    if (selectedImages.isNotEmpty()) {
                        count = 0
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
        try {
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
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        dialog.show()
    }

    fun setlocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    var addLat = ""
    var addLong = ""
    var addAddress = ""

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    fun getAddressFromLocation(lat: Double, lon: Double) {
        if (!isNetworkAvailable(this)) {
            val message = "No internet connection"
            Log.e("@@TAG", message)
            showToast(message)
            addressTextView = message
            return
        }

        if (!Geocoder.isPresent()) {
            val message = "Geocoder service not available on this device"
            Log.e("@@TAG", message)
            showToast(message)
            addressTextView = message
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@BussinessForm, Locale.getDefault())
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
                        addressTextView = addAddress
//                        showToast("Location fetched successfully")
                    } else {
                        addressTextView = "Location not found!"
                        showToast(addressTextView!!)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    addressTextView = "Geocoding timed out. Try again."
                    showToast(addressTextView!!)
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "TimeoutException in getAddressFromLocation: ${e.message}")
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    addressTextView = "Unable to fetch location (IO error)"
                    showToast(addressTextView!!)
                }
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "IOException in getAddressFromLocation: ${e.message}")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addressTextView = "Unexpected error while fetching location"
                    showToast(addressTextView!!)
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


    private fun validateFields(): Boolean {
        val fieldMap = mapOf(
            bindinge.loanAmountEditText to "Loan Amount",
            bindinge.visitedAddress to "Visited Address",
            bindinge.caseinitiatedAddress to "caseinitiatedAddress",
            bindinge.addressNew to "addressNew",
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
            showToast(errorMessage)
            return false
        }

        // Validate Spinner Selections
        if (bindinge.spinnerFamilystatus.selectedItemPosition == 0) {
            showToast("Please select a Family Status")
            return false
        }
        if (bindinge.spinnerHouseStatus.selectedItemPosition == 0) {
            showToast("Please select a House Status")
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
        }
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

    private lateinit var imageAdapter1: ImageAdapter1

    fun Float.toPx(context: Context): Float {
        return this * context.resources.displayMetrics.scaledDensity
    }

    fun addTimestampToImage(file: File) {
        var bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IllegalArgumentException("Invalid image file")

        // Correct the image orientation first (fix rotation if needed)
        bitmap = rotateImageIfRequired(bitmap, file)

        var tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f.toPx(this@BussinessForm)
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val timeStamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        ).format(Date())

        val line1 = timeStamp
        val line2 = "Lat: $addLat" + " Long: $addLong"
        val address = addressTextView?.toString() ?: ""
        val mid = address.length / 2
        val line3 = if (address.isNotEmpty()) address.substring(0, mid) else ""
        val line4 = if (address.length > mid) address.substring(mid) else ""

//        val line3 = addressTextView.toString().subSequence(0, addressTextView.toString().length / 2)
//        val line4 = addressTextView.toString().subSequence(
//            addressTextView.toString().length / 2,
//            addressTextView.toString().length - 1
//        )

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
            addProperty("ownerOfPlot", safeString(bindinge.ownerEdittext.text))
            addProperty("ownerOfShop", safeString(bindinge.ownerShopEdittext.text))
            addProperty("about_buisness_brief_note", safeString(bindinge.businessDetailsBrif.text))
            addProperty("size_of_security", safeString(bindinge.sizeOfSecurityEditText.text))
            addProperty("loan_amt", safeString(bindinge.loanAmountEditText.text))
            addProperty("visit_address", safeString(bindinge.visitedAddress.text))
            addProperty("caseinitiatedAddress", safeString(bindinge.caseinitiatedAddress.text))
            addProperty("addressNew", safeString(bindinge.addressNew.text))
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
            addProperty(
                "monthly_family_expenditure", safeString(bindinge.familymonthlyExpenditure.text)
            )

            addProperty(
                "family_status",
                spinnerfamilystatus.getOrNull(bindinge.spinnerFamilystatus.selectedItemPosition)
                    ?: ""
            )
            addProperty(
                "house_status",
                houseStatusOptions.getOrNull(bindinge.spinnerHouseStatus.selectedItemPosition) ?: ""
            )

            addProperty("house_size", safeString(bindinge.editTextHouseSize.text))
            addProperty(
                "residence_at_address_since", safeString(bindinge.editTextResidenceSince.text)
            )


            addProperty(
                "prop_part_status", if (bindinge.ProprietorRadio.isChecked) "Proprietor"
                else if (bindinge.ProprietorRadioTwo.isChecked) "Partnership"
                else "Other"
            )

            addProperty(
                "proprietorRadioButtonOtherDetails",
                safeString(bindinge.proprietorRadioButtonOtherDetails.text)
            )

            addProperty(
                "business_care_taker_name", safeString(bindinge.businesscaretakersNameEditTest.text)
            )
            addProperty(
                "business_care_taker_relation",
                safeString(bindinge.businesscaretakersRelationEditTest.text)
            )

            addProperty(
                "shop_status",
                shopStatusOptions.getOrNull(bindinge.spinnerShopStatus.selectedItemPosition) ?: ""
            )
            addProperty("size_of_shop", safeString(bindinge.sizeofShop.text))
            addProperty("since_operating", safeString(bindinge.sinceOperating.text))
            addProperty(
                "area_status",
                spinnerSelectAreaAdapterOptions.getOrNull(bindinge.spinnerSelectArea.selectedItemPosition)
                    ?: ""
            )
            addProperty("about_buisness", safeString(bindinge.aboutBusiness.text))
            addProperty("shop_timings", safeString(bindinge.shopTimings.text))
            addProperty("holiday", safeString(bindinge.holidayIfAny.text))
            addProperty("gst_reg_no", safeString(bindinge.gstnumber.text))
            addProperty("shop_act_reg_no", safeString(bindinge.ShopActRegNumber.text))
            addProperty("any_other_reg", safeString(bindinge.anyOtherRegistration.text))

            addProperty("proportion_of_sale_cash_basis", safeString(bindinge.proportioncCash.text))
            addProperty(
                "proportion_of_sale_cheque_basis", safeString(bindinge.proportioncCheque.text)
            )

            addProperty(
                "office_setup_seen",
                if (bindinge.radioButtonOfficeSetup.isChecked) "Good" else "Average"
            )
            addProperty(
                "business_setup",
                BusinessSetupOptions.getOrNull(bindinge.spinnerBusinessSetup.selectedItemPosition)
                    ?: ""
            )

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
            addProperty(
                "as_per_ce_profit", safeString(bindinge.perCreditExecutiveMonthlyProfit.text)
            )
            addProperty("net_profit_margin", safeString(bindinge.netProfitMargin.text))

            addProperty("pan_number", safeString(bindinge.panNumberEditText.text))
            addProperty(
                "name_board_seen",
                if (bindinge.OfficeNameBoardObservedRadio.isChecked) "Yes" else "No"
            )

            addProperty(
                "security_offered_against_loan", safeString(bindinge.securityOfferedEditText.text)
            )
            addProperty("address_of_security", safeString(bindinge.addressOfSecurityEditText.text))
            addProperty("security_value", safeString(bindinge.valueOfSecurityEditText.text))
            addProperty("size_of_security", safeString(bindinge.editTextHouseSize.text))

            addProperty(
                "neighbour_check_status",
                if (bindinge.EnterNeighborCheckStatusone.isChecked) "Positive" else "Negative"
            )

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

            if (bindinge.spinnerShopStatus.selectedItemPosition == 1) {
                addProperty("value_of_shop", safeString(bindinge.shopStatusEditText.text))
            } else if (bindinge.spinnerShopStatus.selectedItemPosition == 2) {
                addProperty("rent_of_shop", safeString(bindinge.shopStatusEditText.text))
            }

            if (bindinge.spinnerHouseStatus.selectedItemPosition == 1) {
                addProperty("value_of_house", safeString(bindinge.houseValueRentEditText.text))
            } else if (bindinge.spinnerShopStatus.selectedItemPosition == 2) {
                addProperty("rent_of_house", safeString(bindinge.houseValueRentEditText.text))
            }
        }
    }

    var previewStatus = true

    fun setSubmitData() {


        try {
            saveFormData(bindinge, case_id, this@BussinessForm, false)
            deviceID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            Log.e("SubmitData", "Error in saveFormData or getting deviceID", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        val requestBody = buildLoanRequestJson()

        Log.e("@@@@@@@@@@@@TAG", "RequestBody: $requestBody")

        val apiInterface = RetrofitManager().instance1!!.create(ApiInterface::class.java)

        apiInterface.submitBussinessDetails(requestBody)?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dismissProgressDialog()
                Log.e("TAG", "API Failure: ${t.message}")
                showToast(t.message.toString())
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dismissUploadDialog()
                dismissProgressDialog()
                val jsonObject = response.body()
                Log.e("TAG", "API Response: $jsonObject")

                try {
                    val res = JSONObject(jsonObject.toString())
                    if (res.getBoolean("status")) {

                        SweetAlertDialog(
                            ContextThemeWrapper(
                                this@BussinessForm, R.style.ThemeOverlay_MaterialComponents_Dialog
                            ), SweetAlertDialog.SUCCESS_TYPE
                        ).apply {
                            titleText = "Success!"
                            contentText = "Details submitted successfully"
                            setConfirmText("OK")
                            setCancelable(false)
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
                        try {
//                            selectedImages.forEach {
//                                val deletedRows = contentResolver.delete(it, null, null)
//                            }
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                } catch (e: JSONException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("TAG", "JSON Parsing Error: ${e.message}")
                    showToast(R.string.error.toString())
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
        textView.textSize = 7f.toPx(this@BussinessForm)
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
        textView.textSize = 7f.toPx(this@BussinessForm)
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
        textView.textSize = 7f.toPx(this@BussinessForm)
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
        var layoutParams = ViewGroup.MarginLayoutParams(
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
        textView.textSize = 7f.toPx(this@BussinessForm)
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val nameText = EditText(this)
        nameText.hint = "Name $addCustomerNum"
        nameText.inputType = InputType.TYPE_CLASS_TEXT
        nameText.setText(name)
        loanLayout.addView(nameText)

//        val mobileNo = EditText(this)
//        mobileNo.hint = "Mobile No. $addCustomerNum"
//        mobileNo.inputType = InputType.TYPE_CLASS_NUMBER
//        mobileNo.setText(mobile)
//        loanLayout.addView(mobileNo)
//
//        val image = ImageView(this)
//        image.setImageResource(R.drawable.baseline_add_call_24)
//        loanLayout.addView(image)

        // Create a horizontal LinearLayout
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0) // left, top, right, bottom (in pixels)
            }
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER_VERTICAL
        }

// Create the EditText
        val mobileNo = EditText(this).apply {
            hint = "Mobile No. $AddSupplierNum"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(mobile)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // take remaining horizontal space
            ).apply {
                marginEnd = 8 // space between EditText and ImageView
            }
        }

// Create the ImageView
        val image = ImageView(this).apply {
            setImageResource(R.drawable.baseline_add_call_24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
            isClickable = true
            isFocusable = true
        }

// Add views to the horizontal layout
        horizontalLayout.addView(mobileNo)
        horizontalLayout.addView(image)

// Add horizontal layout to the main layout
        loanLayout.addView(horizontalLayout)

        val jsonObject = JsonObject()
        addCustomerJson.add(jsonObject)

        image.setOnClickListener {
            showCallConfirmationDialog(mobileNo.text.toString().trim())
        }

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

    fun showCallConfirmationDialog(phoneNumber: String) {

        if (phoneNumber.length < 10) {
            showToast("should be correct phone number")
            return;
        }
        AlertDialog.Builder(this@BussinessForm).setTitle("Make a Call")
            .setMessage("Do you want to call $phoneNumber?").setPositiveButton("Yes") { _, _ ->
                makePhoneCall(this@BussinessForm, phoneNumber)
            }.setNegativeButton("No", null).show()
    }

    private fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")

            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivity(callIntent)
            } else {
                ActivityCompat.requestPermissions(
                    context as Activity, arrayOf(Manifest.permission.CALL_PHONE), 1
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun addbuttonAddSupplier(name: String, mobile: String, citystr: String) {
        AddSupplierNum++

        val loanCard = CardView(this)
        var layoutParams = ViewGroup.MarginLayoutParams(
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
//        textView.textSize = 18f
        textView.textSize = 7f.toPx(this@BussinessForm)

        textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
        loanLayout.addView(textView)

        val nameText = EditText(this)
        nameText.hint = "Name $AddSupplierNum"
        nameText.inputType = InputType.TYPE_CLASS_TEXT
        nameText.setText(name)
        loanLayout.addView(nameText)

//        val mobileNo = EditText(this)
//        mobileNo.hint = "Mobile No. $AddSupplierNum"
//        mobileNo.inputType = InputType.TYPE_CLASS_NUMBER
//        mobileNo.setText(mobile)
//        loanLayout.addView(mobileNo)
//
//        val image = ImageView(this)
//        image.setImageResource(R.drawable.baseline_add_call_24)
//        loanLayout.addView(image)

        // Create a horizontal LinearLayout
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 0) // left, top, right, bottom (in pixels)
            }
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER_VERTICAL
        }

// Create the EditText
        val mobileNo = EditText(this).apply {
            hint = "Mobile No. $AddSupplierNum"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(mobile)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // take remaining horizontal space
            ).apply {
                marginEnd = 8 // space between EditText and ImageView
            }
        }

// Create the ImageView
        val image = ImageView(this).apply {
            setImageResource(R.drawable.baseline_add_call_24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
            isClickable = true
            isFocusable = true
        }

// Add views to the horizontal layout
        horizontalLayout.addView(mobileNo)
        horizontalLayout.addView(image)

// Add horizontal layout to the main layout
        loanLayout.addView(horizontalLayout)


        val city = EditText(this)
        city.hint = "City $AddSupplierNum"
        city.inputType = InputType.TYPE_CLASS_TEXT
        city.setText(citystr)
        loanLayout.addView(city)

        image.setOnClickListener {
            showCallConfirmationDialog(mobileNo.text.toString().trim())
        }

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

    fun saveFormData(
        bindinge: ActivityBussnessFormBinding, case_id: String, context: Context, status: Boolean
    ) {


        if (case_id.isNullOrEmpty() || statusclear) {
            return
        }
        val requestBody = JsonObject()
        Log.e(TAG, "saveFormData1: " + houseStatusOptions.toString())
        Log.e(TAG, "saveFormData2: " + bindinge.spinnerHouseStatus.selectedItemPosition)


        // Helper function for safe EditText
        fun safeText(text: Editable?): String = text?.toString()?.takeIf { it.isNotBlank() } ?: ""
        fun safeString(str: String?): String = str?.takeIf { it.isNotBlank() } ?: ""

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
        requestBody.addProperty("ownerOfPlot", safeText(bindinge.ownerEdittext.text))
        requestBody.addProperty("ownerOfShop", safeText(bindinge.ownerShopEdittext.text))
        requestBody.addProperty("loan_amt", safeText(bindinge.loanAmountEditText.text))
        requestBody.addProperty("visit_address", safeText(bindinge.visitedAddress.text))
        requestBody.addProperty("caseinitiatedAddress", safeText(bindinge.caseinitiatedAddress.text))
        requestBody.addProperty("addressNew", safeText(bindinge.addressNew.text))
        requestBody.addProperty(
            "date_of_visit", safeString(bindinge.txtVisitedDate.text.toString())
        )
        requestBody.addProperty("time", safeString(bindinge.visitedTime.text.toString()))
        requestBody.addProperty("person_meet", safeText(bindinge.persontmeetnameEditText.text))
        requestBody.addProperty("loan_purpose", safeText(bindinge.loanPurposeEditText.text))
        requestBody.addProperty("name_of_organization", safeText(bindinge.orgnization.text))
        requestBody.addProperty("work_experience", safeText(bindinge.workexprience.text))
        requestBody.addProperty("education", safeText(bindinge.education.text))
        requestBody.addProperty("remark", safeText(bindinge.remarkText.text))
        requestBody.addProperty(
            "number_of_family_members", safeText(bindinge.numberoffamilynumber.text)
        )
        requestBody.addProperty(
            "dependent_family_member", safeText(bindinge.dependentFamilyMember.text)
        )
        requestBody.addProperty("lat", addLat ?: "0.0")
        requestBody.addProperty("long", addLong ?: "0.0")
        requestBody.addProperty("location", safeString(addAddress))
        requestBody.addProperty("submitTime", safeString(getTime()))
        requestBody.addProperty("submitDate", safeString(getDate()))
        requestBody.addProperty("deviceID", deviceID?.toString() ?: "UNKNOWN")
        requestBody.addProperty("monthly_salary", safeText(bindinge.salaryOfEmploy.text))
        requestBody.addProperty(
            "about_buisness_brief_note", safeText(bindinge.businessDetailsBrif.text)
        )
        requestBody.addProperty(
            "monthly_family_expenditure", safeText(bindinge.familymonthlyExpenditure.text)
        )

        requestBody.addProperty(
            "family_status",
            safeSpinnerItem(spinnerfamilystatus, bindinge.spinnerFamilystatus.selectedItemPosition)
        )
        requestBody.addProperty(
            "house_status",
            safeSpinnerItem(houseStatusOptions, bindinge.spinnerHouseStatus.selectedItemPosition)
        )

        requestBody.addProperty("house_size", safeText(bindinge.editTextHouseSize.text))
        requestBody.addProperty(
            "residence_at_address_since", safeText(bindinge.editTextResidenceSince.text)
        )

        requestBody.addProperty(
            "prop_part_status", if (bindinge.ProprietorRadio.isChecked) "Proprietor"
            else if (bindinge.ProprietorRadioTwo.isChecked) "Partnership"
            else "Other"
        )

        requestBody.addProperty(
            "proprietorRadioButtonOtherDetails",
            safeString(bindinge.proprietorRadioButtonOtherDetails.text.toString())
        )

        requestBody.addProperty(
            "business_care_taker_name", safeText(bindinge.businesscaretakersNameEditTest.text)
        )
        requestBody.addProperty(
            "business_care_taker_relation",
            safeText(bindinge.businesscaretakersRelationEditTest.text)
        )
        requestBody.addProperty(
            "shop_status",
            safeSpinnerItem(shopStatusOptions, bindinge.spinnerShopStatus.selectedItemPosition)
        )
        requestBody.addProperty("size_of_shop", safeText(bindinge.sizeofShop.text))
        requestBody.addProperty("value_of_shop", safeText(bindinge.shopStatusEditText.text))
        requestBody.addProperty("rent_of_shop", safeText(bindinge.shopStatusEditText.text))
        requestBody.addProperty("since_operating", safeText(bindinge.sinceOperating.text))
        requestBody.addProperty(
            "area_status", safeSpinnerItem(
                spinnerSelectAreaAdapterOptions, bindinge.spinnerSelectArea.selectedItemPosition
            )
        )
        requestBody.addProperty("about_buisness", safeText(bindinge.aboutBusiness.text))
        requestBody.addProperty("shop_timings", safeText(bindinge.shopTimings.text))
        requestBody.addProperty("holiday", safeText(bindinge.holidayIfAny.text))
        requestBody.addProperty("gst_reg_no", safeText(bindinge.gstnumber.text))
        requestBody.addProperty("shop_act_reg_no", safeText(bindinge.ShopActRegNumber.text))
        requestBody.addProperty("any_other_reg", safeText(bindinge.anyOtherRegistration.text))
        requestBody.addProperty(
            "proportion_of_sale_cash_basis", safeText(bindinge.proportioncCash.text)
        )
        requestBody.addProperty(
            "proportion_of_sale_cheque_basis", safeText(bindinge.proportioncCheque.text)
        )

        requestBody.addProperty(
            "office_setup_seen",
            if (bindinge.radioButtonOfficeSetup.isChecked) "Good" else "Average"
        )

        requestBody.addProperty(
            "business_setup", safeSpinnerItem(
                BusinessSetupOptions, bindinge.spinnerBusinessSetup.selectedItemPosition
            )
        )
        requestBody.addProperty("fixed_employee", safeText(bindinge.fixedEmp.text))
        requestBody.addProperty("temp_employee", safeText(bindinge.temporaryEmp.text))
        requestBody.addProperty("value_of_stock", safeText(bindinge.stockValue.text))
        requestBody.addProperty(
            "major_customers", addCustomerJson?.toString() ?: JsonArray().toString()
        )
        requestBody.addProperty(
            "major_suppliers", addSupplierRequestJson?.toString() ?: JsonArray().toString()
        )
        requestBody.addProperty("as_per_customer_sale", safeText(bindinge.perCustomerSales.text))
        requestBody.addProperty(
            "as_per_customer_exp", safeText(bindinge.perCustomerExpenditure.text)
        )
        requestBody.addProperty("as_per_customer_profit", safeText(bindinge.perMonthlyProfit.text))
        requestBody.addProperty("as_per_ce_sale", safeText(bindinge.perCreditExecutiveSales.text))
        requestBody.addProperty(
            "as_per_ce_exp", safeText(bindinge.perCreditExecutiveExpenditure.text)
        )
        requestBody.addProperty(
            "as_per_ce_profit", safeText(bindinge.perCreditExecutiveMonthlyProfit.text)
        )
        requestBody.addProperty("net_profit_margin", safeText(bindinge.netProfitMargin.text))
        requestBody.addProperty("pan_number", safeText(bindinge.panNumberEditText.text))
        requestBody.addProperty(
            "name_board_seen", if (bindinge.OfficeNameBoardObservedRadio.isChecked) "Yes" else "No"
        )
        requestBody.addProperty(
            "security_offered_against_loan", safeText(bindinge.securityOfferedEditText.text)
        )
        requestBody.addProperty(
            "address_of_security", safeText(bindinge.addressOfSecurityEditText.text)
        )
        requestBody.addProperty("security_value", safeText(bindinge.valueOfSecurityEditText.text))
        requestBody.addProperty("size_of_security", safeText(bindinge.sizeOfSecurityEditText.text))
        requestBody.addProperty(
            "neighbour_check_status",
            if (bindinge.EnterNeighborCheckStatusone.isChecked) "Positive" else "Negative"
        )
        requestBody.addProperty(
            "earning_family_members", familyIncomeDataJson?.toString() ?: JsonArray().toString()
        )
        requestBody.addProperty(
            "bank_details", bankdetailsDataJson?.toString() ?: JsonArray().toString()
        )
        requestBody.addProperty("current_loans", loanDataJson?.toString() ?: JsonArray().toString())
        requestBody.addProperty("assets_owned", safeText(bindinge.Assets.text))
        requestBody.addProperty("value_of_house", safeText(bindinge.houseValueRentEditText.text))
        requestBody.addProperty("rent_of_house", safeText(bindinge.houseValueRentEditText.text))
        requestBody.addProperty(
            "business_care_taker_name", safeText(bindinge.businesscaretakersNameEditTest.text)
        )

        val jsonArray = JSONArray()

        selectedImages.forEach {
            Log.e("@@TAG", "Form Data: $it")
        }
        selectedImages.forEach { jsonArray.put(it.toString()) }

        requestBody.addProperty(
            "imageList", jsonArray.toString()
        )

        Log.e("@@TAG", "Form Data: $requestBody")

        // Save to SharedPreferences
        context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE).edit()
            .putString(case_id, requestBody.toString()).apply()
    }
    /*
        fun saveFormData(
            bindinge: ActivityBussnessFormBinding,
            case_id: String,
            context: Context,
            status: Boolean
        ) {
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
            requestBody.addProperty(
                "about_buisness_brief_note",
                bindinge.businessDetailsBrif.text.toString()
            )
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
        }
    */

    /*
        fun loadFormData(caseId: String, context: Context, bindinge: ActivityBussnessFormBinding) {
            try {

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
                        if (data["area_status"] != null && data["area_status"].equals(
                                spinnerSelectAreaAdapterOptions[i]
                            )
                        ) {
                            spinnerSelectAreaAdapterOptions_num = i
                        }
                    }
                    for (i in 0 until BusinessSetupOptions.size) {
                        if (data["business_setup"] != null && data["business_setup"].equals(
                                BusinessSetupOptions[i]
                            )
                        ) {
                            BusinessSetupOptions_num = i
                        }
                    }
                    for (i in 0 until spinnerfamilystatus.size) {
                        if (data["family_status"] != null && data["family_status"].equals(
                                spinnerfamilystatus[i]
                            )
                        ) {
                            spinnerfamilystatus_num = i
                        }
                    }
                    for (i in 0 until houseStatusOptions.size) {
                        if (data["house_status"] != null && data["house_status"].equals(
                                houseStatusOptions[i]
                            )
                        ) {
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
                    bindinge.ProprietorRadio.isSelected =
                        data["prop_part_status"]?.asString == "Proprietor"
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
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }

        }
    */

    fun loadFormData(caseId: String, context: Context, binding: ActivityBussnessFormBinding) {
        try {
            val sharedPreferences =
                context.getSharedPreferences("FormDataStorage", Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString(caseId, null)

            if (jsonString.isNullOrEmpty()) {
                Log.d("@@TAG", "No saved form data found for case_id: $caseId")
                return
            }

            val data = try {
                JsonParser.parseString(jsonString).asJsonObject
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("@@TAG", "Failed to parse JSON: $jsonString", e)
                return
            }

            Log.d("@@TAG", "Parsed form data: $data")

            // Helper to set EditText safely
            fun setText(view: EditText, key: String) {
                try {
                    view.setText(data[key]?.asString ?: "")
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    view.setText("")
                }
            }

            // Fill EditText fields
            setText(binding.loanAmountEditText, "loan_amt")
            setText(binding.ownerEdittext, "ownerOfPlot")
            setText(binding.ownerShopEdittext, "ownerOfShop")
            setText(binding.visitedAddress, "visit_address")
            setText(binding.caseinitiatedAddress, "caseinitiatedAddress")
            setText(binding.addressNew, "addressNew")
//            binding.txtVisitedDate.text = data["date_of_visit"]?.asString ?: ""
//            binding.visitedTime.text = data["time"]?.asString ?: ""

            setCurrentDate(bindinge.txtVisitedDate)
            setCurrentTime(bindinge.visitedTime)

            setText(binding.persontmeetnameEditText, "person_meet")
            setText(binding.loanPurposeEditText, "loan_purpose")
            setText(binding.orgnization, "name_of_organization")
            setText(binding.workexprience, "work_experience")
            setText(binding.education, "education")
            setText(binding.numberoffamilynumber, "number_of_family_members")
            setText(binding.salaryOfEmploy, "monthly_salary")
            setText(binding.familymonthlyExpenditure, "monthly_family_expenditure")
            setText(binding.editTextHouseSize, "house_size")
            setText(binding.editTextResidenceSince, "residence_at_address_since")
            setText(binding.houseValueRentEditText, "value_of_house")
            setText(binding.businesscaretakersNameEditTest, "business_care_taker_name")
            setText(binding.businesscaretakersRelationEditTest, "business_care_taker_relation")
            setText(binding.sizeofShop, "size_of_shop")
            setText(binding.shopStatusEditText, "value_of_shop") // this is overwritten by next line
            setText(binding.shopStatusEditText, "rent_of_shop")  // consider separating these
            setText(binding.sinceOperating, "since_operating")
            setText(binding.aboutBusiness, "about_buisness")
            setText(binding.shopTimings, "shop_timings")
            setText(binding.holidayIfAny, "holiday")
            setText(binding.gstnumber, "gst_reg_no")
            setText(binding.ShopActRegNumber, "shop_act_reg_no")
            setText(binding.anyOtherRegistration, "any_other_reg")
            setText(binding.proportioncCash, "proportion_of_sale_cash_basis")
            setText(binding.proportioncCheque, "proportion_of_sale_cheque_basis")
            setText(binding.fixedEmp, "fixed_employee")
            setText(binding.temporaryEmp, "temp_employee")
            setText(binding.stockValue, "value_of_stock")
            setText(binding.perCustomerSales, "as_per_customer_sale")
            setText(binding.perCustomerExpenditure, "as_per_customer_exp")
            setText(binding.perMonthlyProfit, "as_per_customer_profit")
            setText(binding.perCreditExecutiveSales, "as_per_ce_sale")
            setText(binding.perCreditExecutiveExpenditure, "as_per_ce_exp")
            setText(binding.perCreditExecutiveMonthlyProfit, "as_per_ce_profit")
            setText(binding.netProfitMargin, "net_profit_margin")
            setText(binding.panNumberEditText, "pan_number")
            setText(binding.securityOfferedEditText, "security_offered_against_loan")
            setText(binding.addressOfSecurityEditText, "address_of_security")
            setText(binding.valueOfSecurityEditText, "security_value")
            setText(binding.editTextHouseSize, "size_of_security") // double-check key name
            setText(binding.Assets, "assets_owned")
            setText(binding.businessDetailsBrif, "about_buisness_brief_note")
            setText(binding.sizeOfSecurityEditText, "size_of_security")
            setText(binding.remarkText, "remark")
            setText(binding.dependentFamilyMember, "dependent_family_member")

            // Ensure adapters are set
//            lateinit var spinnerBusinessSetupdapter: ArrayAdapter<String>

            binding.spinnerHouseStatus.adapter = houseStatusAdapter
            binding.spinnerSelectArea.adapter = shopStatusAdapter

            binding.spinnerSelectArea.adapter = spinnerSelectAreaAdapter
            binding.spinnerBusinessSetup.adapter = spinnerBusinessSetupdapter

            binding.spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus


            // Spinner setter
            fun setSpinnerSelection(spinner: Spinner, options: Array<String>, key: String) {
                try {
                    val value = data[key]?.asString?.trim()
                    if (!value.isNullOrBlank()) {
                        val index = options.indexOf(value)
                        if (index in options.indices) {
                            spinner.setSelection(index)
                        } else {
                            Log.w(
                                "SpinnerHelper",
                                "Value \"$value\" for key \"$key\" not found in options: ${options.joinToString()}"
                            )
                        }
                    } else {
                        Log.w("SpinnerHelper", "No valid value for key: $key")
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("SpinnerHelper", "Error setting spinner selection for key: $key", e)
                }
            }

            setSpinnerSelection(binding.spinnerShopStatus, shopStatusOptions, "shop_status")
            setSpinnerSelection(
                binding.spinnerSelectArea, spinnerSelectAreaAdapterOptions, "area_status"
            )
            setSpinnerSelection(
                binding.spinnerBusinessSetup, BusinessSetupOptions, "business_setup"
            )
            setSpinnerSelection(binding.spinnerHouseStatus, houseStatusOptions, "house_status")

            setSpinnerSelection(binding.spinnerFamilystatus, spinnerfamilystatus, "family_status")

            // JSON arrays
            fun parseJsonArray(key: String): JsonArray? {
                return try {
                    data[key]?.asString?.takeIf { it.isNotBlank() }?.let {
                        JsonParser.parseString(it).asJsonArray
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    null
                }
            }

            familyIncomeDataJson = parseJsonArray("earning_family_members") ?: JsonArray()
            loanDataJson = parseJsonArray("current_loans") ?: JsonArray()
            bankdetailsDataJson = parseJsonArray("bank_details") ?: JsonArray()
            addCustomerJson = parseJsonArray("major_customers") ?: JsonArray()
            addSupplierRequestJson = parseJsonArray("major_suppliers") ?: JsonArray()


            setFamilyIncomeFieldsFromJsonArray()

            // Radio buttons
            binding.ProprietorRadio.isChecked = data["prop_part_status"]?.asString == "Proprietor"
            binding.ProprietorRadioTwo.isChecked =
                data["prop_part_status"]?.asString == "Partnership"
            binding.ProprietorRadioThree.isChecked = data["prop_part_status"]?.asString == "Other"

            if (data["prop_part_status"]?.asString == "Other") {
                binding.proprietorRadiolayout.visibility = View.VISIBLE
                setText(
                    binding.proprietorRadioButtonOtherDetails, "proprietorRadioButtonOtherDetails"
                )
            }

            binding.radioButtonOfficeSetuptwo.isChecked =
                data["office_setup_seen"]?.asString == "Average"
            binding.OfficeNameBoardObservedRadioTwo.isChecked =
                data["name_board_seen"]?.asString == "No"
            binding.EnterNeighborCheckStatusoneTwo.isChecked =
                data["neighbour_check_status"]?.asString == "Negative"

//            val jsonArray = JSONArray(data["imageList"].toString())
//            val uriList = ArrayList<Uri>()
//            for (i in 0 until jsonArray.length()) {
//                val data = jsonArray.getString(i).removeSurrounding("\"")
//                val uri = Uri.parse(data)
//                Log.e(TAG, "loadFormData: "+uri )
//
//                uriList.add(uri);
//            }
            val imageListElement = data["imageList"]
            val uriList = ArrayList<Uri>()

            if (imageListElement != null && imageListElement.isJsonPrimitive) {
                val rawJsonArrayString =
                    imageListElement.asString  // the actual string: ["uri1", "uri2"]

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
                bindinge.recyclerViewImages.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                bindinge.recyclerViewImages.adapter = imageAdapter

                imageAdapter1 = ImageAdapter1(selectedImages1)
                bindinge.recyclerViewImages1.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                bindinge.recyclerViewImages1.adapter = imageAdapter1
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            Log.d("@@TAG", "Form data loaded for case_id: $caseId")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("@@TAG", "Exception in loadFormData", e)
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

    private fun setFamilyIncomeFieldsFromJsonArray() {
        try {
            familyincome = 0
            loans = 0
            bankdetails = 0
            AddSupplierNum = 0
            addCustomerNum = 0

            // Helper to parse and run safely
            fun parseSafely(jsonArray: JsonArray, block: (JsonObject) -> Unit) {
                for (i in 0 until jsonArray.size()) {
                    try {
                        val obj = jsonArray[i].asJsonObject
                        block(obj)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Log.e("@@TAG", "Error parsing item at index $i", e)
                    }
                }
            }

            // === Suppliers ===
            parseSafely(addSupplierRequestJson) { data ->
                val name = data.get("Name")?.asString ?: ""
                val mobile = data.get("mobile")?.asString ?: ""
                val city = data.get("city")?.asString ?: ""
                if (name.isNotBlank() || mobile.isNotBlank()) {
                    addbuttonAddSupplier(name, mobile, city)
                }
            }

            // === Customers ===
            parseSafely(addCustomerJson) { data ->
                val name = data.get("Name")?.asString ?: ""
                val mobile = data.get("mobile")?.asString ?: ""
                if (name.isNotBlank() || mobile.isNotBlank()) {
                    addCustomerDetail(name, mobile)
                }
            }

            // === Bank Details ===
            parseSafely(bankdetailsDataJson) { data ->
                val name = data.get("BankName")?.asString ?: ""
                val branch = data.get("BranchName")?.asString ?: ""
                val type = data.get("AccountType")?.asString ?: ""
                val tenure = data.get("AccountSince")?.asString ?: ""
                if (name.isNotBlank() || branch.isNotBlank() || type.isNotBlank() || tenure.isNotBlank()) {
                    addBackField(name, branch, type, tenure)
                }
            }

            // === Loans ===
            parseSafely(loanDataJson) { data ->
                val name = data.get("BankName")?.asString ?: ""
                val type = data.get("TypeofLoan")?.asString ?: ""
                val emi = data.get("EMIBalance")?.asString ?: ""
                val tenure = data.get("TenureofLoan")?.asString ?: ""
                if (name.isNotBlank() || type.isNotBlank() || emi.isNotBlank() || tenure.isNotBlank()) {
                    addLoanField(name, emi, type, tenure)
                } else {
                }
            }

            // === Family Income ===
            parseSafely(familyIncomeDataJson) { data ->
                val name = data.get("Name")?.asString ?: ""
                val income = data.get("income")?.asString ?: ""
                val relation = data.get("relation")?.asString ?: ""
                if (name.isNotBlank() || income.isNotBlank() || relation.isNotBlank()) {
                    addFamilyIncomeField(name, income, relation)
                }
            }

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("@@TAG", "Error in setFamilyIncomeFieldsFromJsonArray", e)
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
        Utility.hideKeyboard(this@BussinessForm)
        bindinge.laoutCameraVisible.visibility = View.VISIBLE
        bindinge.laoutCameraHide.visibility = View.GONE

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

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
                    bindinge.laoutCameraVisible.visibility = View.GONE
                    bindinge.laoutCameraHide.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                showToast("Failed to start camera")
                bindinge.laoutCameraVisible.visibility = View.GONE
                bindinge.laoutCameraHide.visibility = View.VISIBLE
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Captures an image and saves it to the gallery
    private fun captureImage() {

        try {
            soundPool!!.play(shutterSound!!, 1f, 1f, 0, 0, 1f)
        } catch (e: Exception) {
//            FirebaseCrashlytics.getInstance().recordException(e)
        }
        val imageCapture = imageCapture ?: return

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

        val rotation = windowManager.defaultDisplay.rotation
        imageCapture.targetRotation = rotation
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    bindinge.laoutCameraVisible.visibility = View.GONE
//                    bindinge.laoutCameraHide.visibility = View.VISIBLE
//
                    Thread {
                        // This runs in the background
                        imageUri = outputFileResults.savedUri
                        imageUri?.let { uri ->

                            // UI updates MUST go on main thread
                            runOnUiThreadSafely {

                                imageAdapter1.add(uri)
                                imageAdapter.add(uri)

                                try {
                                    bindinge.recyclerViewImages1.scrollToPosition(imageAdapter1.itemCount - 1)
                                } catch (e: Exception) {

                                }
                                bindinge.image.text = "Selected Images: ${selectedImages.size}"
//                                bindinge.laoutCameraVisible.visibility = View.GONE
//                                bindinge.laoutCameraHide.visibility = View.VISIBLE
                            }
                        }
                    }.start()
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast("Capture again.")
                    runOnUiThreadSafely {
                        bindinge.laoutCameraVisible.visibility = View.GONE
                        bindinge.laoutCameraHide.visibility = View.VISIBLE
                    }
                }
            })
    }

    override fun onDestroy() {
        saveFormData(bindinge, case_id, this@BussinessForm, true)
        super.onDestroy()
    }

    override fun onPause() {
        saveFormData(bindinge, case_id, this@BussinessForm, true)
        super.onPause()
    }

    override fun onBackPressed() {
        saveFormData(bindinge, case_id, this@BussinessForm, false)
        if (bindinge.laoutCameraVisible.isVisible) {
            bindinge.laoutCameraVisible.visibility = View.GONE
            bindinge.laoutCameraHide.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        try {
            saveFormData(bindinge, case_id, this@BussinessForm, true)
        } catch (e: Exception) {
            Log.e("FormSave", "Error in onStop: ${e.message}")
            showToast("Failed to save form data (onStop)")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        super.onStop()
    }

    private lateinit var previewView: PreviewView  // Displays the camera preview
    private lateinit var captureButton: ImageView  // Button to capture image
    private var imageCapture: ImageCapture? = null
    private lateinit var displayManager: DisplayManager
    private var displayId: Int = -1

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
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // User permanently denied permission; show dialog to open app settings
                    AlertDialog.Builder(this).setTitle("Permission Required")
                        .setMessage("Storage permissions are required to save and access files. Please enable them in app settings.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }.setNegativeButton("Cancel", null).show()
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    // User permanently denied permission; show dialog to open app settings
                    AlertDialog.Builder(this).setTitle("Permission Required")
                        .setMessage("Storage permissions are required to save and access files. Please enable them in app settings.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        }.setNegativeButton("Cancel", null).show()
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
                            this, deniedPermissions.toTypedArray(), REQUEST_CODE
                        )
                    }
                }
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
                    showToast("Permission denied!")
                }
            }
        }

    }

    private fun showPermissionExplanationDialog(onPositiveClick: () -> Unit) {
        AlertDialog.Builder(this).setTitle("Permissions Needed")
            .setMessage("We need these permissions to provide full functionality. Please allow them.")
            .setPositiveButton("Allow") { _, _ -> onPositiveClick() }
            .setNegativeButton("Deny") { dialog, _ -> dialog.dismiss() }.setCancelable(false).show()
    }

    var permissionCount = 0;
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

    private val REQUEST_CODE = 100
    fun showToast(message: String) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(this@BussinessForm, message, Toast.LENGTH_SHORT).show()
            } else {
                runOnUiThreadSafely {
                    try {
                        Toast.makeText(this@BussinessForm, message, Toast.LENGTH_SHORT).show()
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
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                }

                var readPermission = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    readPermission = ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
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
                            this, REQUIRED_PERMISSIONS, STORAGE_PERMISSION_CODE
                        )
                    }
                    false
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // API 33+ (Android 13+)

                val readMediaImagesPermission = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES
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

    fun stringToUriList(uriString: String): List<Uri> {
        return uriString
            .removePrefix("[")
            .removeSuffix("]")
            .split(", ")
            .filter { it.isNotBlank() }
            .map { Uri.parse(it) }
    }

    fun showUploadErrorDialog() {
        runOnUiThreadSafely {
            AlertDialog.Builder(this@BussinessForm).setTitle("Upload Failed")
                .setMessage("Getting issues while uploading image. Please try again.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    count = 0
                    dismissProgressDialog()
                    dismissUploadDialog()
                }.setCancelable(true).show()
        }
    }

}