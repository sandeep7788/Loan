package com.cbi_solar.cbisolar.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.cbi_solar.BussinessForm
import com.cbi_solar.DashBoardMenuListModel
import com.cbi_solar.ResponseBodye
import com.cbi_solar.SalaryForm
import com.cbi_solar.cbisolar.R
import com.cbi_solar.cbisolar.databinding.AdapterDashboardMenuBinding
import com.cbi_solar.helper.Utility

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
            holder.binding.txtCaseId.setText("Case ID: "+case.caseId);
            holder.binding.MobileNo.setText("Mobile No: "+case.mobileNo);
            if (!case.customerProfile.equals("null") ||case.customerProfile != null  || !case.customerProfile.isNullOrEmpty()) {
                holder.binding.CustomerProfile.setText("Customer Profile: "+case.customerProfile);
            }

            holder.binding.layout1.setOnClickListener {
                if (case.customerProfile.lowercase().contains("salar",true)) {
                    val sendIntent = Intent(context, SalaryForm::class.java)
                    sendIntent.putExtra("case_id",case.caseId.toString())
                    context.startActivity(sendIntent)
                } else {
                    val sendIntent = Intent(context, BussinessForm::class.java)
                    sendIntent.putExtra("case_id",case.caseId.toString())
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