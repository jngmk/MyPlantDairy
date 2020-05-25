package edu.uc.jeong.myplantdiary.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import edu.uc.jeong.myplantdiary.R
import edu.uc.jeong.myplantdiary.dto.Event
import kotlinx.android.synthetic.main.rowlayout.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class DiaryFragment: Fragment() {
    private lateinit var currentPhotoPath: String
    protected val CAMERA_PERMISSION_REQUEST_CODE = 1997
    protected val SAVE_IMAGE_REQUEST_CODE: Int = 1999
    protected var photoURI : Uri? = null

    // See if we have permission or not
    protected fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.CAMERA)  // arrayOf is to see array form
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    protected fun takePhoto() {
        // also is like when in Kotlin
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(context!!.packageManager)
            if (takePictureIntent == null) {
                Toast.makeText(context, "Unable to save photo", Toast.LENGTH_LONG).show()
            } else {
                // if we are here, we have a valid intent
                val photoFile: File = createImageFile()
                photoFile.also {
                    photoURI = FileProvider.getUriForFile(activity!!.applicationContext, "com.myplantdiary.android.FileProvider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, SAVE_IMAGE_REQUEST_CODE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // generate unique file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        // get access to the directory where we can write pictures.
        val storageDir: File? = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("PlantDiary${timeStamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {  // like a case switch
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    takePhoto()
                } else {
                    Toast.makeText(context, "Unable to take photo without permission", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
    inner class EventsAdapter(val events: List<Event>, val itemLayout: Int): RecyclerView.Adapter<DiaryFragment.EventViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
            return EventViewHolder(view)
        }

        override fun getItemCount(): Int {
            return events.size
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val event = events.get(position)
            holder.updateEvent(event)
        }

    }

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imgEventThumbnail: ImageView = itemView.findViewById(R.id.imgEventThumbnail)
        private var lblEventInfo: TextView = itemView.findViewById(R.id.lblEventInfo)

        /**
         * This function will get called once for each item in the collection that we want show in our recycler view.
         * Paint a single row of the recycler view with this event data class.
         */
        fun updateEvent(event: Event) {
            lblEventInfo.text = event.toString()
            if (event.localPhotoURI != null && event.localPhotoURI != "null") {
                // we have an imgae URI
                val source = ImageDecoder.createSource(activity!!.contentResolver, Uri.parse(event.localPhotoURI))
                val bitmap = ImageDecoder.decodeBitmap(source)
                // take the image, and put it in the thumbnail of the rowlayout.
                imgEventThumbnail.setImageBitmap(bitmap)
            }
        }
    }
}