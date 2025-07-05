package com.loan_verifier;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DashBoardMenuListModel {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("applicant_coapplicant")
    @Expose
    private String applicantCoapplicant;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("application_no")
    @Expose
    private String applicationNo;

    @SerializedName("case_initiated_date")
    @Expose
    private String caseInitiatedDate;

    @SerializedName("report_release_date")
    @Expose
    private String reportReleaseDate;

    @SerializedName("submit_report")
    @Expose
    private String submitReport;

    @SerializedName("state_id")
    @Expose
    private String stateId;

    @SerializedName("bank_id")
    @Expose
    private String bankId;

    @SerializedName("branch_id")
    @Expose
    private String branchId;

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("latitude")
    @Expose
    private Double latitude;

    @SerializedName("longitude")
    @Expose
    private Double longitude;

    @SerializedName("local_outstation")
    @Expose
    private String localOutstation;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("remarks")
    @Expose
    private String remarks;

    @SerializedName("mobile_no")
    @Expose
    private String mobileNo;

    @SerializedName("customer_profile")
    @Expose
    private String customerProfile;

    @SerializedName("loan_amt")
    @Expose
    private String loanAmt;

    @SerializedName("activity")
    @Expose
    private String activity;

    @SerializedName("visit_status")
    @Expose
    private String visitStatus;

    @SerializedName("visit_date")
    @Expose
    private String visitDate;

    @SerializedName("tvr_status")
    @Expose
    private String tvrStatus;

    @SerializedName("tvr")
    @Expose
    private String tvr;

    @SerializedName("created_by")
    @Expose
    private String createdBy;

    @SerializedName("closed_by")
    @Expose
    private String closedBy;

    @SerializedName("verifier_id")
    @Expose
    private String verifierId;

    @SerializedName("verifier_allot_by")
    @Expose
    private String verifierAllotBy;

    @SerializedName("drafting_status")
    @Expose
    private String draftingStatus;

    @SerializedName("drafted_by")
    @Expose
    private String draftedBy;

    @SerializedName("under_write")
    @Expose
    private String underWrite;

    @SerializedName("case_final_status")
    @Expose
    private String caseFinalStatus;

    @SerializedName("product")
    @Expose
    private String product;

    @SerializedName("notes")
    @Expose
    private String notes;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    @SerializedName("isactive")
    @Expose
    private String isActive;

    @SerializedName("payment_status")
    @Expose
    private String paymentStatus;

    @SerializedName("case_amount")
    @Expose
    private String caseAmount;

    @SerializedName("payment_date")
    @Expose
    private String paymentDate;

    @SerializedName("distance")
    @Expose
    private String distance;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicantCoapplicant() {
        return applicantCoapplicant;
    }

    public void setApplicantCoapplicant(String applicantCoapplicant) {
        this.applicantCoapplicant = applicantCoapplicant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    public String getCaseInitiatedDate() {
        return caseInitiatedDate;
    }

    public void setCaseInitiatedDate(String caseInitiatedDate) {
        this.caseInitiatedDate = caseInitiatedDate;
    }

    public String getReportReleaseDate() {
        return reportReleaseDate;
    }

    public void setReportReleaseDate(String reportReleaseDate) {
        this.reportReleaseDate = reportReleaseDate;
    }

    public String getSubmitReport() {
        return submitReport;
    }

    public void setSubmitReport(String submitReport) {
        this.submitReport = submitReport;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getAddress() {
        return address.replace("\r\n", " "); // Remove extra new lines
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks.replace("\r\n", " "); // Remove extra new lines
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getLoanAmt() {
        return "₹ " + loanAmt;
    }

    public void setLoanAmt(String loanAmt) {
        this.loanAmt = loanAmt;
    }

    public String getCreatedAt() {
        return "Created On: " + createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return "Last Updated: " + updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getIsActive() {
        return isActive.equals("1") ? "Active" : "Inactive";
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getCaseAmount() {
        return "₹ " + caseAmount;
    }

    public void setCaseAmount(String caseAmount) {
        this.caseAmount = caseAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus.equals("0") ? "Pending" : "Completed";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDistance() {
        return distance == null ? "N/A" : distance + " km";
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public void setLatitude(String latitude) { this.latitude = Double.valueOf(latitude); }
    public void setLongitude(String longitude) { this.longitude = Double.valueOf(longitude); }
    public void setLocalOutstation(String localOutstation) { this.localOutstation = localOutstation; }


    public void setCustomerProfile(String customerProfile) { this.customerProfile = customerProfile; }

    public void setActivity(String activity) { this.activity = activity; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public void setTvrStatus(String tvrStatus) { this.tvrStatus = tvrStatus; }
    public void setTvr(String tvr) { this.tvr = tvr; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public void setVerifierId(String verifierId) { this.verifierId = verifierId; }
    public void setVerifierAllotBy(String verifierAllotBy) { this.verifierAllotBy = verifierAllotBy; }
    public void setDraftingStatus(String draftingStatus) { this.draftingStatus = draftingStatus; }
    public void setDraftedBy(String draftedBy) { this.draftedBy = draftedBy; }
    public void setUnderWrite(String underWrite) { this.underWrite = underWrite; }
    public void setCaseFinalStatus(String caseFinalStatus) { this.caseFinalStatus = caseFinalStatus; }
    public void setProduct(String product) { this.product = product; }
    public void setNotes(String notes) { this.notes = notes; }

    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

}


/*package com.cbi_solar

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
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
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.loan_verifier.loan.MainActivity
import com.loan_verifier.loan.R
import com.loan_verifier.loan.Utility
import com.loan_verifier.loan.databinding.ActivitySalaryFormBinding
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
import java.io.IOException
import java.io.OutputStream
import java.util.Calendar
import java.util.Locale


lateinit var binding: ActivitySalaryFormBinding
private var numberOfEarningFamilyMembers = 0
private var loans = 0;
private var bankdetails = 0;
private var familyincome = 0;



class SalaryForm : AppCompatActivity() {

    var count:Int =0;
    var srtarr : StringBuilder = StringBuilder()
    var mFusedLocationClient: FusedLocationProviderClient? = null

    // Initializing other items
    // from layout file
    var latitudeTextView: Double? = null
    var longitudeTextView: Double? = null
    var addressTextView: String? = " "
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var PERMISSION_ID: Int = 44



    fun getAllAdapterImages(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>): ArrayList<Bitmap> {
        val bitmapList = ArrayList<Bitmap>()

        for (i in 0 until adapter.itemCount) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)
            if (viewHolder != null) {
                val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView)
                val drawable = imageView.drawable as? BitmapDrawable
                drawable?.bitmap?.let { bitmapList.add(it) }
            }
        }

        return bitmapList
    }
    fun getAllImageUris(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>): ArrayList<Uri> {
        val uriList = ArrayList<Uri>()

        for (i in 0 until adapter.itemCount) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)
            if (viewHolder != null) {
                val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView)
                val drawable = imageView.drawable as? BitmapDrawable ?: continue
                val bitmap = drawable.bitmap

                val uri = bitmapToUri(this, bitmap)
                uri?.let { uriList.add(it) }
            }
        }

        return uriList
    }
    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val contentResolver: ContentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyAppTemp") // Saves in `Pictures/MyAppTemp`
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }

        return uri
    }



    fun uploadImage(uri:Uri) {

        try {
            if (uri == null) {
                setList()
            }
        }catch (e:Exception) {
            setList()
        }
        try {
            progressDialog.show()

            val file = File(FileUtils.getPath(this@SalaryForm, uri))
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val parts =
                MultipartBody.Part.createFormData("image", file.name, requestFile)


            val apiInterface: ApiInterface =
                RetrofitManager().instance1!!.create(ApiInterface::class.java)

            apiInterface.uploadImage(parts, "describtion")?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                    Log.e("@@TAG", "onFailure: " + t.message.toString())
                    count=0
                    Toast.makeText(
                        this@SalaryForm,
                        "Error ",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    try {
                        Log.e("@@TAG", "onResponse: " + response.body() + " "+count +" "+ allBitmaps.size)
                        Toast.makeText(
                            this@SalaryForm,
                            ""+response.body(),
                            Toast.LENGTH_LONG
                        )
                            .show()

                        val jsonObject = JSONObject(response.body().toString())

                        if (jsonObject.getString("status").equals("success")) {
                            srtarr.append(jsonObject.getString("image_name")+",")

                            if (count < allBitmaps.size) {
                                count++;
                                uploadImage(allBitmaps.get(count))
                            } else{
                                setList()
                            }
                            Toast.makeText(
                                this@SalaryForm,
                                " Image processing: $count",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            if (progressDialog!!.isShowing) {
                                progressDialog!!.dismiss()
                            }
                            count=0
                            Toast.makeText(
                                this@SalaryForm,
                                " "+jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }


                    }catch (e:Exception) {
                        setList()

                    }


                }

            })
        } catch (e:Exception) {
            Toast.makeText(
                this@SalaryForm,
                " "+e.message,
                Toast.LENGTH_SHORT
            )
                .show()
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    private val selectedImages = ArrayList<Uri>()
    private lateinit var imageAdapter: ImageAdapter


         val CAMERA_PERMISSION_CODE = 100
         val STORAGE_PERMISSION_CODE = 101

    val spinnerfamilystatus = arrayOf("Select Family Status","Joint Family", "Nuclear Family")
    val houseStatusOptions = arrayOf("Select House Status","Owned", "Rented")

    @SuppressLint("UseCheckPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_salary_form)

        val employmentStatusOptions = arrayOf("Joint Family", "Nuclear Family")
        val adapter = ArrayAdapter(this, R.layout.list_item, employmentStatusOptions)
        binding.spinnerHouseStatus.setAdapter(adapter)

        // Set up House Status Spinner

        val houseStatusAdapter =
            ArrayAdapter(this, R.layout.simple_spinner_item, houseStatusOptions)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHouseStatus.adapter = houseStatusAdapter


        val Adapter_spinnerfamilystatus =
            ArrayAdapter(this, R.layout.simple_spinner_item, spinnerfamilystatus)
        houseStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFamilystatus.adapter = Adapter_spinnerfamilystatus

//        binding.txtVisitedDate.setOnClickListener { setDate(binding.txtVisitedDate) }
        binding.txtDateofjoin.setOnClickListener { setDate(binding.txtDateofjoin) }
        setCurrentDate(binding.txtVisitedDate)
        setCurrentTime(binding.visitedTime)

//        binding.visitedTime.setOnClickListener { Utility.setTime(this@SalaryForm,binding.visitedTime,"Select Visited Time") }


        binding.buttonAddLoans.setOnClickListener {
            addLoanField()

        }


        binding.buttonAddFamilyIncome.setOnClickListener {
            addFamilyIncomeField()

        }

        binding.buttonAddBank.setOnClickListener {
            addBackField()

        }

        binding.submitButton.setOnClickListener {
            Log.e("@@TAG", "setList: loanDataArray "+bankdetailsDataArray.toString() )
            Log.e("@@TAG", "setList: loanDataArray "+loanDataArray.toString() )

//            allBitmaps = getAllImageUris(binding.recyclerViewImages, adapter = imageAdapter)
            allBitmaps = selectedImages;

            if (true) {



                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("You Want to Submit?")
                    .setMessage("Are you sure you want to submit?")
                    .setPositiveButton("OK") { dialog, which ->
                        progressDialog!!.show()

                        if (allBitmaps.size>0) {
                            uploadImage(allBitmaps.get(count))
                        } else {
                            setList()
                        }



                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)  // Prevent closing the dialog by tapping outside
                    .create()

                // Show the dialog
                alertDialog.show()
            }

        }

        val storage: TextView? = findViewById(R.id.storage)

        storage?.setOnClickListener {
            checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE, CAMERA_PERMISSION_CODE
            )

        }

        imageAdapter = ImageAdapter(selectedImages)
        binding.recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewImages.adapter = imageAdapter

        setlocation()

        progressDialog = SweetAlertDialog(
            ContextThemeWrapper(
                this,
                com.loan_verifier.loan.R.style.ThemeOverlay_MaterialComponents_Dialog
            ), SweetAlertDialog.PROGRESS_TYPE
        )
        progressDialog.setTitleText("Loading...")
        progressDialog.setCancelable(false)



    }
    lateinit var progressDialog:SweetAlertDialog



    lateinit var allBitmaps:ArrayList<Uri>

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

                addressTextView = "Address: $address\nCity: $city\nCountry: $country"
            } else {
                addressTextView = "Location not found!"
            }
//            Toast.makeText(this, "addressTextView "+addressTextView, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("@@TAG", "getAddressFromLocation: "+e.message )
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

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
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
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
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
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app requires location access to provide better services. Please allow it.")
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), PERMISSION_ID
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun validateFields(): Boolean {
        val fieldMap = mapOf(
            binding.loanAmountEditText to "Loan Amount",
            binding.visitedAddress to "Visited Address",
            binding.txtVisitedDate to "Visited Date",
            binding.visitedTime to "Visited Time",
            binding.persontmeetnameEditText to "Person Met",
            binding.loanPurposeEditText to "Loan Purpose",
            binding.orgnization to "Organization Name",
            binding.workexprience to "Work Experience",
            binding.education to "Education",
            binding.numberoffamilynumber to "Number of Family Members",
            binding.familymonthlyExpenditure to "Monthly Family Expenditure",
            binding.editTextHouseSize to "House Size",
            binding.editTextResidenceSince to "Residence Since",
            binding.houseValueRentEditText to "House Value/Rent",
            binding.grossSalaryEditText to "Gross Salary",
            binding.netSalaryEditText to "Net Salary",
            binding.currentPositionEditText to "Current Position",
            binding.txtDateofjoin to "Date of Joining",
            binding.otherIncomeEditText to "Other Income",
            binding.previousEmploymentEditText to "Previous Employment",
            binding.officeTimingsEditText to "Office Timings",
            binding.holidayEditText to "Holiday",
            binding.panNumberEditText to "PAN Number",
            binding.employerNameEditText to "Employer Name",
            binding.employerNumberEditText to "Employer Mobile No",
            binding.coEmployeeNameEditText to "Co-Employee Name",
            binding.coEmployeeNumber to "Co-Employee Mobile No",
            binding.securityOfferedEditText to "Security Offered Against Loan",
            binding.addressOfSecurityEditText to "Address of Security",
            binding.valueOfSecurityEditText to "Security Value"
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
        if (binding.spinnerFamilystatus.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a Family Status", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.spinnerHouseStatus.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a House Status", Toast.LENGTH_SHORT).show()
            return false
        }

        return true // If everything is valid
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@SalaryForm, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@SalaryForm, arrayOf(permission), requestCode)
        } else {
//            openImagePicker()
            openCamera()
//            Toast.makeText(this@SalaryForm, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@SalaryForm, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
//                openImagePicker()
                openCamera()
            } else {
                Toast.makeText(this@SalaryForm, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        /*else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@SalaryForm, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                openImagePicker()

            } else {
                Toast.makeText(this@SalaryForm, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
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
    AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("You have denied location permission. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
            openAppSettings()
    }
            .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        Toast.makeText(this, "Permission still denied!", Toast.LENGTH_SHORT).show()
        finish();
    }
            .show()
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
                    }

private fun openImagePicker() {
    imagePickerLauncher.launch("image/*")
}
private var imageUri: Uri? = null
lateinit var arr:ArrayList<String>

private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
        // Image successfully captured
        Toast.makeText(this, "Image captured: $imageUri", Toast.LENGTH_SHORT).show()
                if (imageUri != null) {

val bitmap = getBitmapFromUri(this, imageUri!!)


bitmap?.let {

//                        arr = addressTextView!!.split(",")
    val addressList = ArrayList(addressTextView!!.split(","))

    val resultBitmap = drawTextOnBitmap(it,addressList,40, Color.RED)
    val newImageUri = saveBitmapAndGetUri(this, resultBitmap)

    selectedImages.add(newImageUri!!)
    imageAdapter.notifyDataSetChanged()
}

                } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
                        }
                        }
fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
}
    }
fun drawTextOnBitmap(bitmap: Bitmap, textArray: ArrayList<String>, textSize: Int, textColor: Int): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        this.textSize = textSize.toFloat()
        textAlign = Paint.Align.CENTER
    }

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 0, 0, 0) // Semi-transparent black
    }

    val bitmapWidth = mutableBitmap.width
    val bitmapHeight = mutableBitmap.height
    val padding = 10
    val totalTextHeight = (textArray.size * textSize) + ((textArray.size - 1) * padding) + 20
    val backgroundPadding = 20

    val backgroundRect = RectF(
            0f,
            (bitmapHeight - totalTextHeight - backgroundPadding).toFloat(),
            bitmapWidth.toFloat(),
            bitmapHeight.toFloat()
    )

    canvas.drawRect(backgroundRect, backgroundPaint)

    var yPos = bitmapHeight - totalTextHeight + 10 // Adjust for bottom padding
    for (text in textArray) {
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, (bitmapWidth / 2).toFloat(), (yPos + textBounds.height()).toFloat(), paint)
        yPos += textSize + padding
    }

    return mutableBitmap
}    var num:Int=0


    fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri? {
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyImages")
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "modified_image_${System.currentTimeMillis()}.jpg")

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        val uri = FileProvider.getUriForFile(context, "com.loan_verifier.loan.provider", file)

        return uri;
    }


fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri? {
val correctedBitmap = ensureCorrectOrientation(context, bitmap)

val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyImages")
        if (!folder.exists()) folder.mkdirs()

val file = File(folder, "modified_image_${System.currentTimeMillis()}.jpg")

        try {
FileOutputStream(file).use { out ->
        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    out.flush()
}
        } catch (e: IOException) {
        e.printStackTrace()
            return null
                    }

                    return FileProvider.getUriForFile(context, "com.loan_verifier.loan.provider", file)
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
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
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

fun setList() {

    progressDialog.show()
    val requestBody = JsonObject()
    requestBody.addProperty("case_id", "42071")
    requestBody.addProperty("loan_amt", binding.loanAmountEditText.text.toString())
    requestBody.addProperty("visit_address", binding.visitedAddress.text.toString())
    requestBody.addProperty("date_of_visit", binding.txtVisitedDate.text.toString())
    requestBody.addProperty("time", binding.visitedTime.text.toString())
    requestBody.addProperty("person_meet", binding.persontmeetnameEditText.text.toString())
    requestBody.addProperty("loan_purpose", binding.loanPurposeEditText.text.toString())
    requestBody.addProperty("name_of_organization", binding.orgnization.text.toString())
    requestBody.addProperty("work_experience", binding.workexprience.text.toString())
    requestBody.addProperty("education", binding.education.text.toString())
    requestBody.addProperty("number_of_family_members", binding.numberoffamilynumber.text.toString())


    requestBody.addProperty("monthly_family_expenditure", binding.familymonthlyExpenditure.text.toString())
    requestBody.addProperty("family_status", spinnerfamilystatus.get(binding.spinnerFamilystatus.selectedItemPosition))
    requestBody.addProperty("house_status", houseStatusOptions.get(binding.spinnerHouseStatus.selectedItemPosition))
    requestBody.addProperty("house_size", binding.editTextHouseSize.text.toString())
    requestBody.addProperty("residence_at_address_since", binding.editTextResidenceSince.text.toString())
    requestBody.addProperty("value_of_house", binding.houseValueRentEditText.text.toString())
    requestBody.addProperty("rent_of_house", binding.houseValueRentEditText.text.toString())
    requestBody.addProperty("gross_salary", binding.grossSalaryEditText.text.toString())
    requestBody.addProperty("net_salary", binding.netSalaryEditText.text.toString())
    requestBody.addProperty("current_position_of_employee", binding.currentPositionEditText.text.toString())
    requestBody.addProperty("date_of_joining", binding.txtDateofjoin.text.toString())
    requestBody.addProperty("other_income_of_applicant", binding.otherIncomeEditText.text.toString())
    requestBody.addProperty("previous_employement_details", binding.previousEmploymentEditText.text.toString())
    requestBody.addProperty("office_timings", binding.officeTimingsEditText.text.toString())
    requestBody.addProperty("holiday", binding.holidayEditText.text.toString())
    requestBody.addProperty("pan_number", binding.panNumberEditText.text.toString())
//        requestBody.addProperty("office_setup_seen", binding.businessSetupSeenEditText.text.toString())
    requestBody.addProperty("name_board_seen", if (binding.radioButton1One.isSelected) "Yes" else "No")
    requestBody.addProperty("employer_name", binding.employerNameEditText.text.toString())
    requestBody.addProperty("employer_mobile_no", binding.employerNumberEditText.text.toString())
    requestBody.addProperty("co_employee_name", binding.coEmployeeNameEditText.text.toString())
    requestBody.addProperty("co_employee_mobile_no", binding.coEmployeeNumber.text.toString())
    requestBody.addProperty("security_offered_against_loan", binding.securityOfferedEditText.text.toString())
    requestBody.addProperty("address_of_security", binding.addressOfSecurityEditText.text.toString())
    requestBody.addProperty("security_value", binding.valueOfSecurityEditText.text.toString())
    requestBody.addProperty("size_of_security", binding.editTextHouseSize.text.toString())
    requestBody.addProperty("neighbour_check_status", if (binding.EnterNeighborCheckStatusone.isSelected) "Good" else "Bad")
    requestBody.addProperty("business_setup", if (binding.radioButtonOfficeSetup.isSelected) "Good" else "Average")

    requestBody.addProperty(
            "earning_family_members",
            familyIncomeDataArray.toString()
    )

    requestBody.addProperty(
            "bank_details",bankdetailsDataArray.toString()
    )

    Log.e("@@TAG", "setList: loanDataArray "+loanDataArray )

    requestBody.addProperty(
            "current_loans",
            loanDataArray.toString()
    )

//        requestBody.addProperty(
//            "bank_details",
//            "[{\"BankName\":\"Punjab National Bank\",\"BranchName\":\"Vidhyadhar Nagar\",\"AccountType\":\"Saving\",\"AccountSince\":\"15 Years\"},{\"BankName\":\"SBI\",\"BranchName\":\"Vidhyadhar Nagar\",\"AccountType\":\"Saving\",\"AccountSince\":\"10 Years\"}]"
//        )
//        requestBody.addProperty(
//            "current_loans",
//            "[{\"BankName\":\"Punjab National Bank\",\"TypeofLoan\":\"Commercial Loan\",\"EMIBalance\":\"18000\",\"TenureofLoan\":\"15 Years\"},{\"BankName\":\"HDFC\",\"TypeofLoan\":\"Personal Loan\",\"EMIBalance\":\"12000\",\"TenureofLoan\":\"5 Years\"}]"
//        )
    requestBody.addProperty("salary_received", if (binding.SalaryReceivedInone.isSelected) "Bank" else "Cash")
    requestBody.addProperty("assets_owned", binding.Assets.text.toString())
    requestBody.addProperty("image_name", srtarr.toString())

    val apiInterface: ApiInterface =
            RetrofitManager().instance1!!.create(ApiInterface::class.java)

    Log.e("@@TAG", requestBody.toString());

    apiInterface.submitLoanDetails(requestBody)?.enqueue(object : Callback<JsonObject> {
        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }

            Log.e("@@TAG", "onFailure: "+t.message.toString() )
            Toast.makeText(
                    this@SalaryForm,
            " " + t.message.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
        }

        override fun onResponse(
                call: Call<JsonObject>,
        response: Response<JsonObject>
            ) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
            Log.e("@@TAG", "onResponse: "+response.body() )
            try {


                val jsonObject = JSONObject(response.body().toString())

                if (jsonObject.getBoolean("status")) {

                    val successDialog = SweetAlertDialog(
                            ContextThemeWrapper(this@SalaryForm,
                    R.style.ThemeOverlay_MaterialComponents_Dialog),
                    SweetAlertDialog.SUCCESS_TYPE
                        )

                    successDialog.setTitleText("Success!")
                            .setContentText("Your operation successful.")
                            .setConfirmText("OK")
                            .setConfirmClickListener {
                        it.dismissWithAnimation()

                        this@SalaryForm.startActivity(Intent(this@SalaryForm, MainActivity::class.java))
                        finish()

                    }
                            .show()

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
                Log.e("@@TAG", "onResponse: "+e.message)
                e.printStackTrace()
                Toast.makeText(
                        this@SalaryForm,
                " " + resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
            }


        }

    })
}

private val loanDataArray = JsonArray()
private val bankdetailsDataArray = JsonArray()
private val familyIncomeDataArray = JsonArray()
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
private fun addFamilyIncomeField() {
    familyincome++

    val loanCard = CardView(this)
    val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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
    loanLayout.addView(bankNameEditText)

    val amountEditText = EditText(this)
    amountEditText.hint = "Income $familyincome"
    loanLayout.addView(amountEditText)

    val relationEditText = EditText(this)
    relationEditText.hint = "Relation $familyincome"
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
    }
    loanLayout.addView(removeButton)

    loanCard.addView(loanLayout)
    binding.layoutfamilyincome.addView(loanCard)
}
private fun addLoanField() {
    loans++

    val loanCard = CardView(this)
    val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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
    textView.text = "Existing Bank Details $loans"
    textView.textSize = 18f
    textView.setTypeface(ResourcesCompat.getFont(this, R.font.intel), Typeface.BOLD)
    loanLayout.addView(textView)

    val bankNameEditText = EditText(this)
    bankNameEditText.hint = "Name of Bank $loans"
    loanLayout.addView(bankNameEditText)

    val amountEditText = EditText(this)
    amountEditText.hint = "EMI Balance $loans"
    loanLayout.addView(amountEditText)

    val typeOfLoanEditText = EditText(this)
    typeOfLoanEditText.hint = "Type of Loan $loans"
    loanLayout.addView(typeOfLoanEditText)

    val tenureOfLoanEditText = EditText(this)
    tenureOfLoanEditText.hint = "Tenure of Loan $loans"
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
private fun addBackField() {
    bankdetails++

    val bankCard = CardView(this)
    val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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
    bankLayout.addView(bankNameEditText)

    val branchNameEditText = EditText(this)
    branchNameEditText.hint = "Branch Name $bankdetails"
    bankLayout.addView(branchNameEditText)

    val accountTypeEditText = EditText(this)
    accountTypeEditText.hint = "Account Type $bankdetails"
    bankLayout.addView(accountTypeEditText)

    val accountSinceEditText = EditText(this)
    accountSinceEditText.hint = "Account Since $bankdetails"
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







}
*/