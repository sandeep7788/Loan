package com.cbi_solar.cbisolar

import androidx.appcompat.app.AppCompatActivity

class TrackStatusActivity : AppCompatActivity() {

}

//
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.databinding.DataBindingUtil
//import cn.pedant.SweetAlert.SweetAlertDialog
//import com.cbi_solar.cbisolar.databinding.ActivityTrackStatusBinding
//import com.cbi_solar.helper.ApiInterface
//import com.cbi_solar.helper.RetrofitManager
//import com.google.gson.JsonObject
//import com.kofigyan.stateprogressbar.StateProgressBar
//import org.json.JSONObject
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class TrackStatusActivity : AppCompatActivity() {
//    lateinit var binding: ActivityTrackStatusBinding
//    var progressDialog: SweetAlertDialog? = null
//
//    lateinit var stateProgressBar: StateProgressBar
//    var descriptionData =
//        arrayOf("Submit", "Accepted", "Engineer \nAppointment", "Resolved")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_track_status)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_status)
//
//        binding.toolbar.txtTitle.text = "Track Status"
//        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
//        progressDialog!!.progressHelper.barColor = R.color.theme_color
//        progressDialog!!.titleText = "Loading ..."
//        progressDialog!!.setCancelable(false)
//
//        stateProgressBar = findViewById(R.id.your_state_progress_bar_id)
//        stateProgressBar.setStateDescriptionData(descriptionData)
//
//
//        binding.toolbar.imgBack.setOnClickListener { finish() }
//
//        binding.btnSubmit.setOnClickListener {
//            binding.l1.setBackgroundResource(R.drawable.edit_txtbg)
//            when {
//                binding.txtNumber.text.isEmpty() -> {
//                    Utility.showSnackBar(this, "Please enter Reference number")
//                    binding.l1.setBackgroundResource(R.drawable.edit_txt_error)
//                }
//                else -> {
//                    reff_no()
//                }
//            }
//
//        }
//    }
//
//    fun reff_no() {
//        binding.remarks.text = ""
//        binding.layout1.visibility = View.GONE
//        progressDialog!!.show()
//        val apiInterface: ApiInterface =
//            RetrofitManager().instance!!.create(ApiInterface::class.java)
//
//        var reff_no = binding.txtNumber.text
//        Log.e("@@", "reff_no: " + reff_no)
//
//        apiInterface.ser_req_status(
//            reff_no.toString()
//        ).enqueue(object :
//            Callback<JsonObject> {
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                Toast.makeText(
//                    this@TrackStatusActivity,
//                    " " + resources.getString(R.string.error),
//                    Toast.LENGTH_LONG
//                )
//                    .show()
//                progressDialog!!.dismiss()
//            }
//
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                progressDialog!!.dismiss()
//                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
//                try {
//                    if (response.isSuccessful) {
//
//
//                        val jsonObject = JSONObject(response.body().toString())
//                        Log.e("@@TAG", "onResponse: " + jsonObject)
//
//
//                        if (jsonObject.getBoolean("status")) {
//
//                            var jsonArray = jsonObject.getJSONArray("responseBody")
//
//                            if (jsonArray != null && jsonArray.length() > 0) {
//
//                                var data = JSONObject(jsonArray.getJSONObject(0).toString())
//
//
//                                binding.txtReffNo.text = data.getString("reff_no")
//
//                                if (data.getString("remarks") !=null && !data.getString("remarks").equals("")){
//                                    binding.remarks.text = "Remarks: " + data.getString("remarks")
//                                }
//
//
//
//                                binding.layout1.visibility = View.VISIBLE
//                                if (data.has("resolved") && data.getString("resolved")
//                                        .equals("1", true)
//                                ) {
//                                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)
//                                } else if (data.has("ser_eng_apoint") && data.getString("ser_eng_apoint")
//                                        .equals("1", true)
//                                ) {
//                                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
//                                } else if (data.has("accept") && data.getString("accept")
//                                        .equals("1", true)
//                                ) {
//                                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
//                                } else if (data.has("reject") && data.getString("reject")
//                                        .equals("1", true)
//                                ) {
//                                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
//
//                                    val pDialog =
//                                        SweetAlertDialog(
//                                            this@TrackStatusActivity,
//                                            SweetAlertDialog.SUCCESS_TYPE
//                                        )
//                                    pDialog.titleText = "Your Service Request has been rejected."
//                                    pDialog.confirmText = "OK"
//                                    pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
//                                    pDialog.setCancelable(false)
//                                    pDialog.setConfirmClickListener {
//                                        pDialog.dismiss()
//                                        startActivity(
//                                            Intent(
//                                                this@TrackStatusActivity,
//                                                MainActivity::class.java
//                                            )
//                                        )
//                                        finish()
//                                    }
//                                    pDialog.show()
//                                } else if (data.has("submitted") && data.getString("submitted")
//                                        .equals("1", true)
//                                ) {
//
//
//                                } else {
//                                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
//
//
//                                    val pDialog =
//                                        SweetAlertDialog(
//                                            this@TrackStatusActivity,
//                                            SweetAlertDialog.SUCCESS_TYPE
//                                        )
//                                    pDialog.titleText = jsonObject.getString("msg") + ""
//                                    pDialog.confirmText = "OK"
//                                    pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
//                                    pDialog.setCancelable(false)
//                                    pDialog.setConfirmClickListener {
//                                        pDialog.dismiss()
//                                        startActivity(
//                                            Intent(
//                                                this@TrackStatusActivity,
//                                                MainActivity::class.java
//                                            )
//                                        )
//                                        finish()
//                                    }
//                                    pDialog.show()
//                                }
//                            } else {
//                                val pDialog =
//                                    SweetAlertDialog(
//                                        this@TrackStatusActivity,
//                                        SweetAlertDialog.SUCCESS_TYPE
//                                    )
//                                pDialog.titleText = jsonObject.getString("msg") + ""
//                                pDialog.confirmText = "OK"
//                                pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
//                                pDialog.setCancelable(false)
//                                pDialog.setConfirmClickListener {
//                                    pDialog.dismiss()
//                                    startActivity(
//                                        Intent(
//                                            this@TrackStatusActivity,
//                                            MainActivity::class.java
//                                        )
//                                    )
//                                    finish()
//                                }
//                                pDialog.show()
//                            }
//                        } else {
//                            Utility.showDialog(
//                                this@TrackStatusActivity,
//                                SweetAlertDialog.WARNING_TYPE,
//                                jsonObject.getString("msg") + ""
//                            )
//                        }
//                    } else {
//                        Toast.makeText(
//                            this@TrackStatusActivity,
//                            " " + resources.getString(R.string.error),
//                            Toast.LENGTH_LONG
//                        )
//                            .show()
//                    }
//
//                } catch (e: Exception) {
//                    Log.e("@@tag", "onResponse: " + e.message)
//                    Toast.makeText(
//                        this@TrackStatusActivity,
//                        " " + resources.getString(R.string.error),
//                        Toast.LENGTH_LONG
//                    )
//                        .show()
//                }
//            }
//
//        })
//    }
//}