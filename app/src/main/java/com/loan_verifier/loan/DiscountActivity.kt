package com.loan_verifier.loan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.loan_verifier.loan.databinding.ActivityDiscountBinding

class DiscountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_discount)
        binding.toolbar.txtTitle.text = "Branches"
        binding.toolbar.imgBack.setOnClickListener { finish() }
    }

    lateinit var binding: ActivityDiscountBinding


}