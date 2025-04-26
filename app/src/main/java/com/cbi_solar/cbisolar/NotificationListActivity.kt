package com.cbi_solar.cbisolar

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
import com.cbi_solar.NotificationModel
import com.cbi_solar.StoreModel
import com.cbi_solar.cbisolar.adapter.NotifactionAdapter
import com.cbi_solar.cbisolar.adapter.StoreListAdapter
import com.cbi_solar.cbisolar.databinding.ActivityNotifactionListBinding
import com.cbi_solar.cbisolar.databinding.ActivityStoreListBinding
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.RetrofitManager
import com.cbi_solar.helper.VerticalSpacingItemDecorator
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationListActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotifactionListBinding
    var list: ArrayList<NotificationModel> = ArrayList()
    var adapter: NotifactionAdapter? = null
    var progressDialog: SweetAlertDialog? = null
    var TAG = "@@NotificationListActivity"


    fun initRecyclerView() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        var linearLayoutManager: LinearLayoutManager? =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycler.layoutManager = linearLayoutManager
        val itemDecorator = VerticalSpacingItemDecorator(20)
        binding.recycler.addItemDecoration(itemDecorator)
        adapter = NotifactionAdapter(initGlide()!!)
        binding.recycler.adapter = adapter
    }

    fun initGlide(): RequestManager? {
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_store_24)
            .error(R.drawable.red_button_background)
        return Glide.with(this)
            .setDefaultRequestOptions(options)
    }

    fun notification() {
        progressDialog!!.show()

        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.notification()?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@NotificationListActivity,
                    " " + t.message.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
                progressDialog!!.dismiss()
                adapter?.setData(java.util.ArrayList<NotificationModel>())
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

                                val data = NotificationModel()
                                data.id = json.getInt("id")
                                data.title = json.getString("title")
                                data.des = json.getString("des")
                                data.isactive = json.getString("isactive")
                                list.add(data)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@NotificationListActivity,
                        " " + resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                if (response.body() == null) {
                    adapter!!.setData(ArrayList<NotificationModel>())
                } else {
                    adapter!!.setData(list)
                }
            }

        })
    }


    fun set_ClickListener() {
        binding.toolbar.imgBack.setOnClickListener { finish() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifaction_list)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifaction_list)
        binding.toolbar.txtTitle.text = "Notification List"
        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        set_ClickListener()
        initRecyclerView()
        notification()
    }


}