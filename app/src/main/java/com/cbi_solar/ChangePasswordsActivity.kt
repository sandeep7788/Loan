package com.cbi_solar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.MainActivity
import com.cbi_solar.cbisolar.R
import com.cbi_solar.cbisolar.Utility
import com.cbi_solar.cbisolar.databinding.ActivityChangePassworsActivcityBinding
import com.cbi_solar.helper.ApiContants
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.MyApplication
import com.cbi_solar.helper.RetrofitManager
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordsActivity : AppCompatActivity() {

    lateinit var binding: ActivityChangePassworsActivcityBinding
    var progressDialog: SweetAlertDialog? = null
    var TAG = "@@ChangePasswordsActivity"


    fun passwordchange() {
        progressDialog!!.show()
        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.passwordchange(
            MyApplication.ReadStringPreferences(ApiContants.id),
            binding.edtOldPassword.text.toString(),
            binding.newPassword.text.toString(),binding.newPassword.text.toString()
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@ChangePasswordsActivity,
                    " " + resources.getString(R.string.error),
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(TAG, "onFailure: " + t.message)
                progressDialog!!.dismiss()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                progressDialog!!.dismiss()
                if (response.isSuccessful) {

                    Log.d(TAG, "onResponse: " + response.body().toString())
                    val jsonObject = JSONObject(response.body().toString())

                    if (jsonObject.getBoolean("status")) {


                        val pDialog =
                            SweetAlertDialog(
                                this@ChangePasswordsActivity,
                                SweetAlertDialog.SUCCESS_TYPE
                            )
                        pDialog.titleText = jsonObject.getString("msg") + ""
                        pDialog.confirmText = "OK"
                        pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
                        pDialog.setCancelable(false)
                        pDialog.setConfirmClickListener {
                            pDialog.dismiss()
                            startActivity(
                                Intent(
                                    this@ChangePasswordsActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }
                        pDialog.show()

                    } else {
                        Utility.showDialog(
                            this@ChangePasswordsActivity,
                            SweetAlertDialog.WARNING_TYPE,
                            jsonObject.getString("msg")
                        )


                    }
                } else {
                    Toast.makeText(
                        this@ChangePasswordsActivity,
                        "Bad Response ! ",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_passwors_activcity)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_passwors_activcity)
        progressDialog =
            SweetAlertDialog(this@ChangePasswordsActivity, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)
        binding.toolbar.txtTitle.text = "Change Password"

        binding.btn.setOnClickListener {
            binding.edtOldPassword.setBackgroundResource(R.drawable.edit_txtbg)
            binding.newPassword.setBackgroundResource(R.drawable.edit_txtbg)
            binding.confirmPassword.setBackgroundResource(R.drawable.edit_txtbg)
            when {
                binding.edtOldPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter old Password")
                    binding.edtOldPassword.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.newPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter new Password")
                    binding.newPassword.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.confirmPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter confirm Password")
                    binding.confirmPassword.setBackgroundResource(R.drawable.edit_txt_error)
                }
                else -> {
                    if (!binding.confirmPassword.text.toString().trim().equals(binding.newPassword.text.toString().trim(),true)){
                        Utility.showSnackBar(this, "New and Confirm password not matched")
                        binding.confirmPassword.setBackgroundResource(R.drawable.edit_txt_error)
                    }else passwordchange()

                }
            }
        }
        binding.toolbar.imgBack.setOnClickListener { finish() }

    }

}