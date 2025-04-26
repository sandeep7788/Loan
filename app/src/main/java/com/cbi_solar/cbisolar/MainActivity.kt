package com.cbi_solar.cbisolar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.cbi_solar.ChangePasswordsActivity
import com.cbi_solar.ResponseBodye
import com.cbi_solar.SalaryForm
import com.cbi_solar.cbisolar.adapter.DashBoardMenuList
import com.cbi_solar.cbisolar.databinding.ActivityMainBinding
import com.cbi_solar.helper.ApiContants
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.MyApplication
import com.cbi_solar.helper.RetrofitManager
import com.cbi_solar.helper.VerticalSpacingItemDecorator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonObject
import com.ouattararomuald.slider.ImageSlider
import com.ouattararomuald.slider.SliderAdapter
import com.ouattararomuald.slider.loaders.picasso.PicassoImageLoaderFactory
import io.supercharge.shimmerlayout.ShimmerLayout
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets


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

        apiInterface.product_list("4")?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                binding.shimmerSkeleton1.visibility=View.GONE
                binding.shimmerSkeleton2.visibility=View.GONE
                binding.shimmerSkeleton3.visibility=View.GONE
                binding.shimmerSkeleton4.visibility=View.GONE

                Log.e("@@TAG", "onFailure: "+t.message.toString() )
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

                    if (jsonObject.getBoolean("status")) {
                        var jsonArray = jsonObject.getJSONArray("responseBody")


                        if (jsonArray.length() > 0) {

//jsonArray.length()
                            try {
                                for (i in 0 until Math.min(100,jsonArray.length())) {

                                    val json = jsonArray.getJSONObject(i)  // Get JSON object directly

                                    val data = ResponseBodye()

                                    data.setApplicationNo(json.optString("ApplicationNo", ""))
                                    data.setMobileNo(json.optString("MobileNo", ""))
                                    data.setActivity(json.optString("Activity", ""))
                                    data.setApplicantName(json.optString("ApplicantName", ""))
                                    data.setVisitAddress(json.optString("VisitAddress", ""))
                                    data.setCaseId(json.optString("CaseId", ""))
                                    data.setCustomerProfile(json.optString("CustomerProfile", ""))
                                    data.setCaseStatus(json.optString("CaseStatus", ""))


                                    list.add(data)
                                }

//                                progressDialog!!.dismiss()
                            } catch (e:Exception) {
                                Log.e("@@TAG", "onResponse: "+e.message)
                            }

                        }
                    }
                } catch (e: JSONException) {
                    Log.e("@@TAG", "onResponse: "+e.message)
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

                binding.shimmerSkeleton1.visibility=View.GONE
                binding.shimmerSkeleton2.visibility=View.GONE
                binding.shimmerSkeleton3.visibility=View.GONE
                binding.shimmerSkeleton4.visibility=View.GONE
                binding.recyclerView.visibility=View.VISIBLE
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
    }

    var completed=false;
    var pending=false;

    fun filter() {
//        initRecyclerView()
        adapter!!.setData(list)
        var filteredResults:ArrayList<ResponseBodye> = ArrayList<ResponseBodye>()

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
                                Log.e("@@TAG", "onResponse: " + e.message)
                            }
                        }
                    }
                } catch (e: JSONException) {
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
        adapter = DashBoardMenuList(initGlide()!!,list)
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
}