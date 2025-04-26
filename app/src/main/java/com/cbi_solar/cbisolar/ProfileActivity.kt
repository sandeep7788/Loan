package com.cbi_solar.cbisolar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.databinding.ActivityProfileBinding
import com.cbi_solar.helper.ApiContants
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.MyApplication
import com.cbi_solar.helper.RetrofitManager
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    var TAG = "@@ProfileActivity"

    private fun getDetail() {
        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.profile(
            MyApplication.ReadStringPreferences(ApiContants.id)
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@ProfileActivity,
                    " " + resources.getString(R.string.error),
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(TAG, "onFailure: " + t.message)

            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                try {
                    if (response.isSuccessful) {

                        Log.d(TAG, "onResponse: " + response.body().toString())
                        val jsonObject = JSONObject(response.body().toString())


                        if (jsonObject.getBoolean("status")) {

                            var jsonArray = jsonObject.getJSONObject("responseBody")

                            if (true) {

                                var data = jsonArray;// JSONObject(jsonArray.getJSONObject(0).toString())

                                binding.txtName.text = data.getString("VerifierName")
                                binding.edtName.setText(data.getString("VerifierName"))
                                binding.edtNumber.setText(data.getString("MobileNumber"))
                                binding.edtAddress.setText(data.getString("Address"))

                                MyApplication.writeStringPreference(ApiContants.PREF_F_name, data.getString("VerifierName"))
                            }
                        } else {
                            Utility.showDialog(
                                this@ProfileActivity,
                                SweetAlertDialog.WARNING_TYPE,
                                resources.getString(R.string.error)
                            )

                            Toast.makeText(
                                this@ProfileActivity,
                                "Bad Response ! ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            " " + resources.getString(R.string.error),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this@ProfileActivity,
                        " " + resources.getString(R.string.error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

        })
    }

    fun profileupdate() {

        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.profileupdate(
            MyApplication.ReadStringPreferences(ApiContants.id),
            binding.edtAddress.text.toString()
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@ProfileActivity,
                    " " + resources.getString(R.string.error),
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(TAG, "onFailure: " + t.message)

            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                if (response.isSuccessful) {

                    Log.d(TAG, "onResponse: " + response.body().toString())
                    val jsonObject = JSONObject(response.body().toString())

                    if (jsonObject.getBoolean("status")) {


                        val pDialog =
                            SweetAlertDialog(this@ProfileActivity, SweetAlertDialog.SUCCESS_TYPE)
                        pDialog.titleText = jsonObject.getString("msg") + ""
                        pDialog.confirmText = "OK"
                        pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
                        pDialog.setCancelable(false)
                        pDialog.setConfirmClickListener {
                            pDialog.dismiss()
                            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                            finish()
                        }
                        pDialog.show()

                    } else {
                        Utility.showDialog(
                            this@ProfileActivity,
                            SweetAlertDialog.WARNING_TYPE,
                            resources.getString(R.string.error)
                        )

                        Toast.makeText(
                            this@ProfileActivity,
                            "Bad Response ! ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Bad Response ! ", Toast.LENGTH_LONG)
                        .show()
                }

            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        binding.toolbar.txtTitle.text = "Profile"
        getDetail()

/*        binding.btn.setOnClickListener {
            binding.edtAddress.setBackgroundResource(R.drawable.edit_txtbg)
            when {
                binding.edtAddress.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter Address")
                    binding.edtAddress.setBackgroundResource(R.drawable.edit_txt_error)
                }
                else -> {
//                    profileupdate()
                }
            }
        }*/

        binding.toolbar.imgBack.setOnClickListener { finish() }

        binding.edtName.setText(MyApplication.ReadStringPreferences(ApiContants.PREF_F_name))

    }

}