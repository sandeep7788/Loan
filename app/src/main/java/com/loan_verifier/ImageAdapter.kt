package com.loan_verifier

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loan_verifier.loan.R
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.ByteArrayOutputStream
import java.io.File

class ImageAdapter(private val imageList: ArrayList<Uri>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    lateinit var context: Context

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val trashImage: ImageView = view.findViewById(R.id.trashImage)
        val view: ImageView = view.findViewById(R.id.view)
    }
    var imageActionListener: OnImageActionListener? = null
    interface OnImageActionListener {
        fun onDeleteImage(position: Int)
    }

    fun add(uri: Uri?) {
        try {
            if (uri != null && uri.toString().isNotEmpty() && !imageList.contains(uri)) {
                imageList.add(uri)
                notifyItemInserted(imageList.size - 1)
            } else {
                notifyDataSetChanged()
            }

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

/*
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
*/
fun remove(uri: Uri?) {
    try {
        if (uri == null) {
            Log.w("ImageAdapter", "Attempted to remove a null URI.")
            return
        }

        // Use string comparison to avoid issues with URI equality on Android 12+
        val index = imageList.indexOfFirst { it.toString() == uri.toString() }

        if (index != -1) {
            imageList.removeAt(index)
            try {
                notifyItemRemoved(index)
            } catch (e:Exception) {
                try {
                    notifyDataSetChanged()
                } catch (e:Exception) {

                }
            }
        } else {
            Log.w("ImageAdapter", "URI not found in list: $uri")
        }

    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Log.e("ImageAdapter", "Error while removing image: ${e.message}", e)
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
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
//                imageList.removeAt(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, imageList.size)
                remove(uri)
            }

            holder.trashImage.setOnClickListener {
                imageActionListener?.onDeleteImage(position)
//                deleteImage(position)
            }
            holder.view.setOnClickListener {
                showFullImageDialog(imageList.get(position))
            }
        } catch (e:Exception) {
            remove(uri)
//            holder.imageView.setImageResource(R.drawable.ic_baseline_privacy_tip_24)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun showFullImageDialog(uri: Uri) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_full_image)

        val fullImageView: ImageView = dialog.findViewById(R.id.fullImageView)
        val closeButton: ImageView = dialog.findViewById(R.id.closeButton)

        Glide.with(context)
            .load(uri)
            .into(fullImageView)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
