package com.loan_verifier

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loan_verifier.loan.R
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.ByteArrayOutputStream
import java.io.File

class ImageAdapter1(private val imageList: ArrayList<Uri>) : RecyclerView.Adapter<ImageAdapter1.ImageViewHolder>() {

    lateinit var context: Context

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image1, parent, false)
        context = parent.context;
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageList[position]
        try {
            if (uri != null && uri.toString().isNotEmpty()) {
//                holder.imageView.setImageURI(uri)
                Glide.with(context)
                    .load(uri)
                    .error(R.drawable.ic_baseline_privacy_tip_24)
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.ic_baseline_privacy_tip_24)
                remove(uri)
            }
        } catch (e:Exception) {
            remove(uri)
//            holder.imageView.setImageResource(R.drawable.ic_baseline_privacy_tip_24)
            FirebaseCrashlytics.getInstance().recordException(e)
        }


    }
    fun add(uri: Uri?): Boolean {
        return try {
            if (uri != null && uri.toString().isNotEmpty() && !imageList.contains(uri)) {


                imageList.add(uri)
                notifyItemInserted(imageList.size - 1)
                true
            } else {
                notifyDataSetChanged()
                false
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }
    fun remove(uri: Uri?) {
        try {

            if (uri != null && imageList.contains(uri)) {
                val index = imageList.indexOf(uri)
                if (index != -1) {
                    imageList.removeAt(index)
                    notifyDataSetChanged()
                } else if (index != -1) {
                    imageList.removeAt(index)
                    notifyItemRemoved(index)
                } else {
                }
            } else {
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
