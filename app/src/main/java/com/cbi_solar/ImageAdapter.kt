package com.cbi_solar

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cbi_solar.cbisolar.R
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File

class ImageAdapter(private val imageList: ArrayList<Uri>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    fun imageViewToTempFile(imageView: ImageView): File? {
        val drawable = imageView.drawable as? BitmapDrawable ?: return null
        val bitmap = drawable.bitmap

        // Convert bitmap to byte array
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        // Create a temporary file
        val tempFile = File.createTempFile("temp_image", ".png")
        tempFile.outputStream().use { it.write(byteArray) }

        return tempFile
    }

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageURI(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
