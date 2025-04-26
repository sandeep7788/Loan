package com.cbi_solar.cbisolar

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.databinding.ActivitySignInBinding
import com.cbi_solar.helper.ApiContants
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.ApiRequest
import com.cbi_solar.helper.MyApplication
import com.cbi_solar.helper.RetrofitManager
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    var progressDialog: AlertDialog? = null
    var TAG = "@@SignInActivity"
    var deviceID=""
    fun ClickListener() {

        binding.btnSignIn.setOnClickListener {
            binding.l1.setBackgroundResource(R.drawable.edit_txtbg)
            binding.l2.setBackgroundResource(R.drawable.edit_txtbg)
            when {
                binding.txtNumber.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter Number")
                    binding.txtNumber.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l1.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter password")
                    binding.l2.setBackgroundResource(R.drawable.edit_txt_error)
                }

                else -> {
//                    signIn()
                    try {
                        deviceID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
                    } catch (e:Exception) {
                    }
                    progressDialog!!.show()
                    ApiRequest.verifierLogin(this@SignInActivity, binding.txtNumber.text.toString().trim(),
                        binding.txtPassword.text.toString().trim(),deviceID , progressDialog)
                }
            }
        }

        binding.txtForgotPassword.setOnClickListener {
            Utility.showSnackBar(this, "Please try later.")
        }
    }


    fun signIn() {
        progressDialog!!.show()
        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.signIn(
            binding.txtNumber.text.toString().trim(),
            binding.txtPassword.text.toString().trim()
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@SignInActivity,
                    " " + resources.getString(R.string.error),
                    Toast.LENGTH_LONG
                )
                    .show()
                Log.e(TAG, "onFailure: " + t.message)
                progressDialog!!.dismiss()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                progressDialog!!.dismiss()
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val jsonObject = response.body()!! // No need for JSONObject conversion

                        Log.d(TAG, "onResponse: $jsonObject")

                        Toast.makeText(this@SignInActivity, " ${jsonObject.get("msg").asString}", Toast.LENGTH_LONG).show()

                        if (jsonObject.get("status").asBoolean) {
                            val responseBody = jsonObject.getAsJsonObject("responseBody")

                            MyApplication.writeStringPreference(ApiContants.id, responseBody.get("id").asString)
                            MyApplication.writeStringPreference(ApiContants.PREF_F_name, responseBody.get("verifier_name").asString)
                            MyApplication.writeStringPreference(ApiContants.login, "true")

                            startActivity(Intent(this@SignInActivity, SplashScreen::class.java))
                            finish()
                        } else {
                            Utility.showDialog(
                                this@SignInActivity,
                                SweetAlertDialog.WARNING_TYPE,
                                resources.getString(R.string.error)
                            )

                            Toast.makeText(this@SignInActivity, "Bad Response!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, " ${resources.getString(R.string.error)}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception: ${e.message}")
                    Toast.makeText(this@SignInActivity, " ${resources.getString(R.string.error)}", Toast.LENGTH_LONG).show()
                }
            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
/*
        val widthDp = resources.displayMetrics.run { widthPixels / density }
        val heightDp = resources.displayMetrics.run { heightPixels / density }

        var width:Int= widthDp.roundToInt()
        binding.l1.setMargins(top = (widthDp+100).roundToInt())*/

/*        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(this.binding.l1.getWidth(), this.binding.l1.getHeight())
        params.setMargins(left, top, right, bottom)
        this.binding.l1.setLayoutParams(params)*/
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Loading...")
            .setMessage("Please wait...") // Adding a message for better user experience
            .setCancelable(false) // Prevent dismissal when tapping outside
            .create()

        ClickListener()
    }
}