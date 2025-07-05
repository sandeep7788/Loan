package com.loan_verifier.loan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.loan_verifier.StoreModel
import com.loan_verifier.loan.adapter.StoreListAdapter
import com.loan_verifier.loan.databinding.ActivityStoreListBinding
import com.loan_verifier.helper.ApiInterface
import com.loan_verifier.helper.RetrofitManager
import com.loan_verifier.helper.VerticalSpacingItemDecorator
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoreListActivity : AppCompatActivity() {
    lateinit var binding: ActivityStoreListBinding
    var list: ArrayList<StoreModel> = ArrayList()
    var adapter: StoreListAdapter? = null
    var progressDialog: SweetAlertDialog? = null
    var TAG = "@@StoreListActivity"

    fun set_ClickListener() {
        binding.toolbar.imgBack.setOnClickListener { finish() }
    }

    fun initRecyclerView() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        var linearLayoutManager: LinearLayoutManager? =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycler.layoutManager = linearLayoutManager
        val itemDecorator = VerticalSpacingItemDecorator(20)
        binding.recycler.addItemDecoration(itemDecorator)
        adapter = StoreListAdapter(initGlide()!!)
        binding.recycler.adapter = adapter
    }

    fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_store_24)
            .error(R.drawable.red_button_background)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    fun list() {
        progressDialog!!.show()

        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.listStore()?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@StoreListActivity,
                    " " + t.message.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
                progressDialog!!.dismiss()
                adapter?.setData(java.util.ArrayList<StoreModel>())
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                progressDialog!!.dismiss()


                try {

                    Log.d(TAG, "onResponse: " + response.body().toString())
                    val jsonObject = JSONObject(response.body().toString())

                    if (jsonObject.getBoolean("status")) {
                        var jsonArray = jsonObject.getJSONArray("responseBody")


                        if (jsonArray.length() > 0) {

                            for (i in 0 until jsonArray.length()) {

                                var json = JSONObject(jsonArray.getJSONObject(i).toString())

                                val data = StoreModel()
                                data.store_name = json.getString("store_name")
                                data.contact_no = json.getString("contact_no")
                                data.address = json.getString("address")
                                list.add(data)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                    Toast.makeText(
                        this@StoreListActivity,
                        " " + resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                if (response.body() == null) {
                    adapter!!.setData(ArrayList<StoreModel>())
                } else {
                    adapter!!.setData(list)
                }
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_store_list)
        binding.toolbar.txtTitle.text = "Updates & Notifications"
        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        set_ClickListener()
        initRecyclerView()
        list()
    }
}