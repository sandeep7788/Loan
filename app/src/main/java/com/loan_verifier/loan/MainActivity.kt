package com.loan_verifier.loan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.loan_verifier.ChangePasswordsActivity
import com.loan_verifier.ResponseBodye
import com.loan_verifier.helper.ApiContants
import com.loan_verifier.helper.ApiInterface
import com.loan_verifier.helper.MyApplication
import com.loan_verifier.helper.RetrofitManager
import com.loan_verifier.helper.VerticalSpacingItemDecorator
import com.loan_verifier.loan.adapter.DashBoardMenuList
import com.loan_verifier.loan.databinding.ActivityMainBinding
import com.ouattararomuald.slider.ImageSlider
import com.ouattararomuald.slider.SliderAdapter
import com.ouattararomuald.slider.loaders.picasso.PicassoImageLoaderFactory
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    fun closeDrawerBar() {
        var mDrawerLayout: DrawerLayout = binding.drawerLayout

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers()
        }
    }

    fun setData() {
        binding.includeNavigation.txtNameAndId.text =
            "" + MyApplication.ReadStringPreferences(ApiContants.PREF_F_name)
        binding.txtName.text = "" + MyApplication.ReadStringPreferences(ApiContants.PREF_F_name)
    }

    @SuppressLint("WrongConstant")
    fun clickListener() {
        var mDrawerLayout: DrawerLayout = binding.drawerLayout




        binding.imgMenu.setOnClickListener {
            if (!mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.openDrawer(Gravity.START)
            else mDrawerLayout.closeDrawer(Gravity.END)
        }

        binding.includeNavigation.layoutSignOut.setOnClickListener {
            closeDrawerBar()
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to sign out?")
                .setCancelable(false)
                .setPositiveButton(
                    "Yes"
                ) { dialog, id -> MyApplication.logout(true) }
                .setNegativeButton("No", null)
                .show()
        }

        binding.bottomNev.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    closeDrawerBar()
                    setList()
//                    setBanner_list()
//                    verifierLogin()
                }
//                R.id.winners -> {
//                    startActivity(Intent(this@MainActivity,DiscountActivity::class.java))
//                }
//                R.id.store -> {
//                    startActivity(Intent(this@MainActivity, StoreListActivity::class.java))
//                }
                R.id.profile -> {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                }
            }
            true
        })
        binding.includeNavigation.layoutForm.setOnClickListener {
            closeDrawerBar()

        }

        binding.includeNavigation.layoutServiceRequest.setOnClickListener {
            closeDrawerBar()
            startActivity(Intent(this@MainActivity, ServicesForm::class.java))
        }

        binding.includeNavigation.layoutConsentForm.setOnClickListener {
            closeDrawerBar()

        }

        binding.includeNavigation.layoutTrackStatus.setOnClickListener {
            closeDrawerBar()
            startActivity(Intent(this@MainActivity, TrackStatusActivity::class.java))
        }

        binding.includeNavigation.layoutChangePassword.setOnClickListener {
            closeDrawerBar()
            startActivity(Intent(this@MainActivity, ChangePasswordsActivity::class.java))
        }

        binding.includeNavigation.layoutPrivacyPolicy.setOnClickListener {
            closeDrawerBar()
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(com.cbi_solar.helper.ApiContants.PREF_privacypolicy)))
            Toast.makeText(this, "try later", Toast.LENGTH_SHORT).show()
        }

        binding.imgNotifaction.setOnClickListener {
            closeDrawerBar()
            startActivity(Intent(this@MainActivity, NotificationListActivity::class.java))
        }
    }

    //    var progressDialog: SweetAlertDialog? = null
    fun setList() {
//        progressDialog!!.show()
        list.clear()

        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

//    verifier_id

        var verifier_id: String =
            MyApplication.ReadStringPreferences(ApiContants.verifier_name).toString()

        apiInterface.product_list(verifier_id.toString().trim())
            ?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.shimmerSkeleton1.visibility = View.GONE
                    binding.shimmerSkeleton2.visibility = View.GONE
                    binding.shimmerSkeleton3.visibility = View.GONE
                    binding.shimmerSkeleton4.visibility = View.GONE

                    Log.e("@@TAG", "onFailure: " + t.message.toString())
                    Toast.makeText(
                        this@MainActivity,
                        " " + t.message.toString(),
                        Toast.LENGTH_LONG
                    )
                        .show()
//                progressDialog!!.dismiss()
                    adapter?.setData(java.util.ArrayList<ResponseBodye>())
                }

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {


                    try {


                        val jsonObject = JSONObject(response.body().toString())

                        if (jsonObject.has("msg") && jsonObject.getString("msg") != null
                            && !"5".contains(jsonObject.getString("msg"))
                        ) {
                            val dialog =
                                SweetAlertDialog(
                                    this@MainActivity,
                                    SweetAlertDialog.WARNING_TYPE
                                ).apply {
//                    setTitleText("Confirm")
                                    setContentText("A newer version of this app is available. Your current version is no longer supported.\n\nPlease update to continue enjoying the latest features and improvements.")
                                    setConfirmText("ok")
                                    setCancelable(false)

                                    setConfirmClickListener {
                                        it.dismissWithAnimation()
                                        exitProcess(0)
                                    }

//                                    setCancelClickListener {
//                                        it.dismissWithAnimation()
//                                    }
                                }
                            try {
                                dialog.window?.decorView?.post {
                                    val confirmButton =
                                        dialog.findViewById<Button>(R.id.confirm_button)
                                    val cancelButton =
                                        dialog.findViewById<Button>(R.id.cancel_button)
                                    confirmButton?.apply {
                                        setBackgroundColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.green_color
                                            )
                                        )
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

                        if (jsonObject.getBoolean("status")) {
                            var jsonArray = jsonObject.getJSONArray("responseBody")


                            if (jsonArray.length() > 0) {

//jsonArray.length()
                                try {
                                    for (i in 0 until Math.min(100, jsonArray.length())) {

                                        val json =
                                            jsonArray.getJSONObject(i)  // Get JSON object directly

                                        val data = ResponseBodye()

                                        data.setApplicationNo(json.optString("ApplicationNo", ""))
                                        data.setMobileNo(json.optString("MobileNo", ""))
                                        data.setActivity(json.optString("Activity", ""))
                                        data.setApplicantName(json.optString("ApplicantName", ""))
                                        data.setVisitAddress(json.optString("VisitAddress", ""))
                                        data.setCaseId(json.optString("CaseId", ""))
                                        data.setCustomerProfile(
                                            json.optString(
                                                "CustomerProfile",
                                                ""
                                            )
                                        )
                                        data.setCaseStatus(json.optString("CaseStatus", ""))
                                        data.setBankName(json.optString("BankName", ""))
                                        data.setBranchName(json.optString("BranchName", ""))


                                        list.add(data)
                                    }

//                                progressDialog!!.dismiss()
                                } catch (e: Exception) {
                                    FirebaseCrashlytics.getInstance().recordException(e)
                                    Log.e("@@TAG", "onResponse: " + e.message)
                                }

                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("@@TAG", "onResponse: " + e.message)
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            " " + resources.getString(R.string.error),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    if (response.body() == null) {
                        adapter!!.setData(ArrayList<ResponseBodye>())
                    } else {
                        adapter!!.setData(list)
                    }

                    binding.shimmerSkeleton1.visibility = View.GONE
                    binding.shimmerSkeleton2.visibility = View.GONE
                    binding.shimmerSkeleton3.visibility = View.GONE
                    binding.shimmerSkeleton4.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }

            })
    }

    val data: MutableList<String> = mutableListOf()
    val title: MutableList<String> = mutableListOf()
    /*
        fun setBanner_list() {
            progressDialog!!.show()

            val apiInterface: ApiInterface =
                RetrofitManager().instance!!.create(ApiInterface::class.java)

            apiInterface.banner_list()?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        " " + t.message.toString(),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    progressDialog!!.dismiss()
                    adapter?.setData(java.util.ArrayList<ResponseBodye>())
                }

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    progressDialog!!.dismiss()


                    try {


                            val jsonObject = JSONObject(response.body().toString())

                        if (jsonObject.getBoolean("status")) {
                            var jsonArray = jsonObject.getJSONArray("responseBody")

                            if (jsonArray.length() > 0) {

                                for (i in 0 until jsonArray.length()) {

                                    var json = JSONObject(jsonArray.getJSONObject(i).toString())
                                    data.add("http://cbisolar.vidhyalaya.co.in" + json.getString("banner_img"))
                                    title.add("")
                                }
                                if (data.size > 0) {
                                    imageSlider.adapter = SliderAdapter(
                                        this@MainActivity,
                                        PicassoImageLoaderFactory(),
                                        imageUrls = data,
                                        descriptions = title
                                    )
                                }
                            } else {
                                binding.c1.visibility = View.GONE
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@MainActivity,
                            " " + resources.getString(R.string.error),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    if (response.body() == null) {
                        adapter!!.setData(ArrayList<ResponseBodye>())
                    } else {
                        adapter!!.setData(list)
                    }
                }

            })
        }
    */


    lateinit var binding: ActivityMainBinding
    private lateinit var imageSlider: ImageSlider

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.drawerLayout.bringToFront()

//        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
//        progressDialog!!.progressHelper.barColor = R.color.theme_color
//        progressDialog!!.titleText = "Loading ..."
//        progressDialog!!.setCancelable(false)

        imageSlider = binding.imageSlider

        val REQUIRED_PERMISSIONSOne = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {

            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

//        ActivityCompat.requestPermissions(
//            this,
//            REQUIRED_PERMISSIONSOne,
//            10001
//        )

        setData()
        clickListener()
        initRecyclerView()
        setList()



        val shimmerText1 = binding.shimmerSkeleton1
        shimmerText1.startShimmerAnimation()

        val shimmerText2 = binding.shimmerSkeleton2
        shimmerText2.startShimmerAnimation()

        val shimmerText3 = binding.shimmerSkeleton3
        shimmerText3.startShimmerAnimation()

        val shimmerText4 = binding.shimmerSkeleton4
        shimmerText4.startShimmerAnimation()


//      verifierLogin()


//        setBanner_list()


//            data.add("https://as1.ftcdn.net/v2/jpg/05/59/09/50/1000_F_559095057_YeiS6zTM107wRlnenmZZ8ztDjOmG8858.jpg")
//            title.add("Real Estate Property")
//            data.add("https://as1.ftcdn.net/v2/jpg/01/03/71/90/1000_F_103719031_GCNfwZDiO4VCXXieWApdO2vSrSzd8ZsF.jpg")
//            title.add("Real estate broker agent presenting ")
//            data.add("https://as1.ftcdn.net/v2/jpg/03/81/68/18/1000_F_381681893_yD17WGx5lrkj9fbIuU2TtgtHXGgEjouY.jpg")
//            title.add("mortgage loan ")

        if (data.size > 0) {
            imageSlider.adapter = SliderAdapter(
                this@MainActivity,
                PicassoImageLoaderFactory(),
                imageUrls = data,
                descriptions = title
            )
        }




        binding.Completed.setOnCheckedChangeListener { _, isChecked ->
            completed = isChecked
            filter()
        }
        binding.Pending.setOnCheckedChangeListener { _, isChecked ->
            pending = isChecked
            filter()
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkAndRequestPermissions(this@MainActivity)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Android 10 to 12 (API 29 - 32)
//            // Use MediaStore or SAF for read/write access
//            checkPermissions()
//        } else {
//            checkPermissions()
//        }
        checkPermissions()

//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) { // Only for Android 10 (API 29)
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {


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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= 29) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE
            )
        }
    }

    val STORAGE_PERMISSION_CODE = 1001

    // 89529 39557
    var completed = false;
    var pending = false;

    fun filter() {
//        initRecyclerView()
        adapter!!.setData(list)
        var filteredResults: ArrayList<ResponseBodye> = ArrayList<ResponseBodye>()

        if (completed) {

            for (i in 0 until list.size) {
                if ("Completed".lowercase().equals(list.get(i).caseStatus.lowercase())) {
                    filteredResults.add(list.get(i))
                }
            }
//            initRecyclerView()

        }
        if (pending) {

            for (i in 0 until list.size) {
                if ("Pending".lowercase().equals(list.get(i).caseStatus.lowercase())) {
                    filteredResults.add(list.get(i))
                }
            }


        }

        if (!completed && !pending) {
            for (i in 0 until list.size) {
                filteredResults.add(list.get(i))
            }
        }

        adapter!!.setData(filteredResults)
//        initRecyclerView()

    }


    fun parseStringToJson(response: String?): JSONObject? {
        try {
            if (response == null || response.isEmpty()) {
                throw java.lang.Exception("Empty or null response")
            }

            // Convert to UTF-8 (fix encoding issues)
            var cleanResponse = String(
                response.toByteArray(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8
            ).trim { it <= ' ' }

            // Remove all non-JSON characters (symbols, spaces, unwanted tags)
//            cleanResponse = cleanResponse.replace(
//                "[^\\x20-\\x7E]".toRegex(),
//                ""
//            ) // Remove non-printable ASCII characters
//
//            // Ensure response is a valid JSON by trimming any leading or trailing non-JSON parts
//            cleanResponse = cleanResponse.replace("^[^\\{]+".toRegex(), "")
//                .replace("[^\\}]+$".toRegex(), "")

            return JSONObject(cleanResponse)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
            return null // Return null if parsing fails
        }
    }

    fun verifierLogin(

    ) {


        val url = "https://vidhyalaya.co.in/MIS/API/Restapi/CaseList?verifier_id=4"

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            com.android.volley.Response.Listener { response1 ->
                val jsonObject = parseStringToJson(response1.toString().trim())
                try {
                    if (jsonObject!!.getBoolean("status")) {
                        val jsonArray = jsonObject!!.getJSONArray("responseBody")

                        if (jsonArray.length() > 0) {
                            try {
                                for (i in 0 until 20) { // Prevent out-of-bounds

                                    val json = jsonArray.getJSONObject(i)

                                    val data = ResponseBodye()

                                    data.applicationNo = json.optString("ApplicationNo", "")
                                    data.mobileNo = json.optString("MobileNo", "")
                                    data.activity = json.optString("Activity", "")
                                    data.applicantName = json.optString("ApplicantName", "")
                                    data.visitAddress = json.optString("VisitAddress", "")

                                    list.add(data)
                                }
                            } catch (e: java.lang.Exception) {
                                FirebaseCrashlytics.getInstance().recordException(e)
                                Log.e("@@TAG", "onResponse: " + e.message)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    throw RuntimeException(e)
                }
            },
            com.android.volley.Response.ErrorListener { error ->
                Log.e("API_ERROR", error.toString()) // Log error
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Content-Type"] = "text/html; charset=UTF-8" // JSON format
                headers["User-Agent"] = "PostmanRuntime/7.29.4" // Prevent blocks
//                headers["Authorization"] =
//                    "Bearer YOUR_ACCESS_TOKEN" // If API needs authentication
//                headers["Accept-Encoding"] = "gzip, deflate, br" // Maintain session
                headers["Cache-Control"] = "no-cache" // Prevent caching
                return headers
            }
        }

        //        MyApplication.getInstance().addToRequestQueue(stringRequest);
        requestQueue.add(stringRequest)
    }


    fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_store_24)
            .error(R.drawable.red_button_background)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    var list: ArrayList<ResponseBodye> = ArrayList()
    var adapter: DashBoardMenuList? = null
    fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        val itemDecorator = VerticalSpacingItemDecorator(20)
        binding.recyclerView.addItemDecoration(itemDecorator)
        adapter = DashBoardMenuList(initGlide()!!, list)
        binding.recyclerView.adapter = adapter
    }

    private var exit = false
    override fun onBackPressed() {
        closeDrawerBar()
        if (exit) {
            finish() // finish activity
        } else {
            Toast.makeText(
                this, "Press Back again to Exit.",
                Toast.LENGTH_SHORT
            ).show()
            exit = true
            Handler().postDelayed({ exit = false }, 3 * 1000)

        }
    }

    private val REQUIRED_PERMISSIONS = mutableListOf(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
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

    private fun checkPermissions() {
        val permissionsNeeded = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            // Show a dialog explaining the need for permissions
            showPermissionExplanationDialog {
                // User agrees, request permissions
                ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toTypedArray(),
                    REQUEST_CODE
                )
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
        }

//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
//            if (ContextCompat.checkSelfPermission(
//                    this@MainActivity,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    REQUEST_CODE
//                )
//            }
//        }
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

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
            for ((index, permission) in permissions.withIndex()) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission)
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                // Show the dialog again or take other actions

                permissionCount++
                if (permissionCount == 2) {
                    permissionCount = 0;
                    showGoToSettingsDialog()
                } else {

                    showPermissionExplanationDialog {
                        // User agrees, request permissions again
                        ActivityCompat.requestPermissions(
                            this,
                            deniedPermissions.toTypedArray(),
                            REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }

    private val mediaPermissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    var permissionCount = 0;
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Denied Permanently")
            .setMessage("You have permanently denied the required permissions. Please go to settings and enable them manually.")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Direct user to the app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    fun checkAndRequestPermissions(activity: Activity) {
        val permissionsToRequest = mediaPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                100
            )
        } else {
            // Permissions already granted
        }
    }

}