package com.cbi_solar.cbisolar

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.databinding.ActivitySignUpBinding
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.RetrofitManager
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SignUpActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivitySignUpBinding
    var status_gender = false
    var date: Calendar = Calendar.getInstance()
    var thisAYear = date.get(Calendar.YEAR).toInt()
    var thisAMonth = date.get(Calendar.MONTH).toInt()
    var thisADay = date.get(Calendar.DAY_OF_MONTH).toInt()

    fun ClickListener() {

        binding.btnSignUp.setOnClickListener {
            binding.layoutName.setBackgroundResource(R.drawable.edit_txtbg)
            binding.layoutEmail.setBackgroundResource(R.drawable.edit_txtbg)
            binding.layoutDob.setBackgroundResource(R.drawable.edit_txtbg)
            binding.layoutGender.setBackgroundResource(R.drawable.edit_txtbg)
            when {
                binding.edtName.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter User Name")
                    binding.layoutName.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.edtMail.text.isEmpty() || binding.edtMail.text.length < 9 -> {
                    Utility.showSnackBar(this, "Please enter valid Number")
                    binding.layoutEmail.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtDateOfBrith.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please select Date of Birth")
                    binding.layoutDob.setBackgroundResource(R.drawable.edit_txt_error)
                }
                !status_gender -> {
                    Utility.showSnackBar(this, "Please select Gender")
                    binding.layoutGender.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.edtAddress.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter Address")
                    binding.layoutAddress.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.edtNewPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter new password")
                    binding.layoutNewPassword.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.edtConfirmPassword.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter confirm new password")
                    binding.layoutConfirmPassword.setBackgroundResource(R.drawable.edit_txt_error)
                }
                else -> {
                    if (!binding.edtConfirmPassword.text.toString().trim().equals(binding.edtNewPassword.text.toString().trim(),true)){
                        Utility.showSnackBar(this, "New and Confirm password not matched")
                        binding.edtConfirmPassword.setBackgroundResource(R.drawable.edit_txt_error)
                    }else signUp()
                }
            }
        }
        binding.txtDateOfBrith.setOnClickListener { setDate(binding.txtDateOfBrith) }


    }

    var progressDialog: SweetAlertDialog? = null
    var TAG = "@@SignInActivity"
    fun signUp() {
        progressDialog!!.show()
        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.signUp(
            binding.edtName.text.toString(),
            binding.edtMail.text.toString().trim(),
            binding.spinnerGender.selectedItem.toString(),
            binding.edtAddress.text.toString(),
            binding.edtConfirmPassword.text.toString(),
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@SignUpActivity,
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
                            SweetAlertDialog(this@SignUpActivity, SweetAlertDialog.SUCCESS_TYPE)
                        pDialog.titleText = jsonObject.getString("msg") + ""
                        pDialog.confirmText = "OK"
                        pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
                        pDialog.setCancelable(false)
                        pDialog.setConfirmClickListener {
                            pDialog.dismiss()
                            startActivity(Intent(this@SignUpActivity, SplashScreen::class.java))
                            finish()
                        }
                        pDialog.show()

                    } else {
                        Utility.showDialog(
                            this@SignUpActivity,
                            SweetAlertDialog.WARNING_TYPE,
                            resources.getString(R.string.error)
                        )

                        Toast.makeText(
                            this@SignUpActivity,
                            "Bad Response ! ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "Bad Response ! ", Toast.LENGTH_LONG)
                        .show()
                }

            }

        })
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

    fun setGenderList(spinner: Spinner) {
        val item = resources?.getStringArray(R.array.gender_array)

        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                R.layout.simple_spinner_item, item!!
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    binding.spinnerHint.visibility = View.GONE
                    /*   Toast.makeText(
                           this@SignUpActivity,"selected gender" +
                                   "" + item[position], Toast.LENGTH_SHORT
                       ).show()*/
                    status_gender = true
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                    binding.spinnerHint.visibility = View.VISIBLE
                    status_gender = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        binding.spinnerGender.onItemSelectedListener = this
        ClickListener()
        setGenderList(binding.spinnerGender)
//        binding.imageView3.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

}