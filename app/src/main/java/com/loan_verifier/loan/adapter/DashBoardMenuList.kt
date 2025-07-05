package com.loan_verifier.loan.adapter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.loan_verifier.BussinessForm
import com.loan_verifier.ResponseBodye
import com.loan_verifier.SalaryForm
import com.loan_verifier.loan.R
import com.loan_verifier.loan.databinding.AdapterDashboardMenuBinding

class DashBoardMenuList(var requestManager: RequestManager, var mOptionList: ArrayList<ResponseBodye>) :
    RecyclerView.Adapter<DashBoardMenuList.ViewHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewProductCategoryBinding: AdapterDashboardMenuBinding =
            AdapterDashboardMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        context = parent.context
        return ViewHolder(viewProductCategoryBinding)
    }

    fun showCallConfirmationDialog(phoneNumber: String) {
        AlertDialog .Builder(context)
            .setTitle("Make a Call")
            .setMessage("Do you want to call $phoneNumber?")
            .setPositiveButton("Yes") { _, _ ->
                makePhoneCall(context, phoneNumber)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun makePhoneCall(context: Context, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CALL_PHONE), 1)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = getItem(position)


        try {
            val case = mOptionList[position]

//            holder.binding.txtCaseId.text = "Case ID: " + case.getId()
//            holder.binding.txtName.text = "Name: " + case.getName()
            holder.binding.txtApplicationNo.text = "Application No: " + case.applicationNo
            holder.binding.ApplicantName.text = "ApplicantName : " + case.applicantName
            holder.binding.txtAddress.text = "VisitAddress: " + case.visitAddress
//            holder.binding.txtBankId.text = "Bank ID: " + case.getBankId()
//            holder.binding.CustomerProfile.text = "Customer Profile: " + case.customerProfile
            holder.binding.Activity.text = "Activity: " + case.activity

            holder.binding.txtStatus.text = case.caseStatus

//            holder.binding.txtStatus.setBackgroundResource(R.drawable.round_corner_shape)
            holder.binding.txtCaseId.setText("Case ID: "+case.caseId)
            holder.binding.MobileNo.setText(case.mobileNo);
            if (!case.customerProfile.equals("null") ||case.customerProfile != null  || !case.customerProfile.isNullOrEmpty()) {
                holder.binding.CustomerProfile.setText("Customer Profile: "+case.customerProfile);
            }

            holder.binding.layout1.setOnClickListener {
                if (case.customerProfile.lowercase().contains("salar",true)) {
                    val sendIntent = Intent(context, SalaryForm::class.java)
                    sendIntent.putExtra("case_id",case.caseId.toString())
                    sendIntent.putExtra("Completed",case.caseStatus.equals("Completed"))
                    context.startActivity(sendIntent)
                } else {
                    val sendIntent = Intent(context, BussinessForm::class.java)
                    sendIntent.putExtra("case_id",case.caseId.toString())
                    sendIntent.putExtra("Completed",case.caseStatus.equals("Completed"))
                    context.startActivity(sendIntent)
                }
            }

            if (case.caseStatus.equals("Completed")) {
                holder.binding.txtStatus.setBackgroundResource(R.drawable.green_button_background)
            } else if (case.caseStatus.equals("Pending")) {
                holder.binding.txtStatus.setBackgroundResource(R.drawable.round_corner_shape)
            } else {
                holder.binding.txtStatus.setBackgroundResource(R.drawable.gray_button_background)
            }
            holder.binding.txtStatus.setPadding(8,8,8,8)

            holder.binding.imageView3.setOnClickListener {
                showCallConfirmationDialog(case.mobileNo.toString())
            }

            holder.binding.bankName.setText("BankName: "+case.bankName)
            holder.binding.branchName.setText("BranchName: "+case.branchName)

        } catch (e: Exception) {
            Log.e("@@TAG", "onResponse: "+e.message)
            e.printStackTrace()
        }

    }

    fun setData(mOptionList: ArrayList<ResponseBodye>) {
        this.mOptionList = mOptionList
        notifyDataSetChanged()
    }

    fun updateList(list: ArrayList<ResponseBodye>) {
        mOptionList = list
        notifyDataSetChanged()
    }


    private fun getItem(index: Int): String {
        return mOptionList[index].toString()
    }

    override fun getItemCount(): Int {
        return mOptionList.size
    }


    inner class ViewHolder(val binding: AdapterDashboardMenuBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
            ////
        }
    }


}