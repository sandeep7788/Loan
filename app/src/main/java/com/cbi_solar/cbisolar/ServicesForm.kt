package com.cbi_solar.cbisolar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cbi_solar.cbisolar.databinding.ActivityServicesFormBinding
import com.cbi_solar.helper.ApiContants
import com.cbi_solar.helper.ApiInterface
import com.cbi_solar.helper.MyApplication
import com.cbi_solar.helper.RetrofitManager
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ServicesForm : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityServicesFormBinding
    var spinner_service_ststus = false
    var progressDialog: SweetAlertDialog? = null

    fun setSpinnerServices(spinner: Spinner) {
        val item = resources?.getStringArray(R.array.services_array)

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
                    spinner_service_ststus = true
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                    binding.spinnerHint.visibility = View.VISIBLE
                    spinner_service_ststus = false
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_form)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_services_form)


        binding.toolbar.txtTitle.text = "Service Request"
        progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        binding.spinnerServices.onItemSelectedListener = this
        setSpinnerServices(binding.spinnerServices)

        ClickListener()
    }

    fun ClickListener() {

        binding.btnSubmit.setOnClickListener {
            binding.layout1.setBackgroundResource(R.drawable.edit_txtbg)
            binding.layout2.setBackgroundResource(R.drawable.edit_txtbg)
            when {

                !spinner_service_ststus -> {
                    Utility.showSnackBar(this, "Please select service Type")
                    binding.layout1.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.edtDescription.text.isEmpty() -> {
                    Utility.showSnackBar(this, "Please enter description")
                    binding.layout2.setBackgroundResource(R.drawable.edit_txt_error)
                }
                else -> {
                    sendRequest()
                }
            }
        }

        binding.toolbar.imgBack.setOnClickListener { finish() }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    var TAG = "@@ServicesForm"
    fun sendRequest() {
        progressDialog!!.show()
        val apiInterface: ApiInterface =
            RetrofitManager().instance!!.create(ApiInterface::class.java)

        apiInterface.ser_req(
            MyApplication.ReadStringPreferences(ApiContants.id),
            binding.spinnerServices.selectedItem.toString(),
            binding.edtDescription.text.toString(),
            getCurrentDateAndTime(),
        ).enqueue(object :
            Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(
                    this@ServicesForm,
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
                            SweetAlertDialog(this@ServicesForm, SweetAlertDialog.SUCCESS_TYPE)
                        pDialog.titleText = jsonObject.getString("msg") + ""
                        pDialog.confirmText = "OK"
                        pDialog.progressHelper.barColor = Color.parseColor("#0173B7")
                        pDialog.setCancelable(false)
                        pDialog.setConfirmClickListener {
                            pDialog.dismiss()
                            startActivity(Intent(this@ServicesForm, MainActivity::class.java))
                            finish()
                        }
                        pDialog.show()

                    } else {
                        Utility.showDialog(
                            this@ServicesForm,
                            SweetAlertDialog.WARNING_TYPE,
                            resources.getString(R.string.error)
                        )

                        Toast.makeText(
                            this@ServicesForm,
                            "Bad Response ! ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@ServicesForm, "Bad Response ! ", Toast.LENGTH_LONG)
                        .show()
                }

            }

        })
    }

    fun getCurrentDateAndTime(): String? {
        val c: Date = Calendar.getInstance().time
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        return simpleDateFormat.format(c)
    }
}