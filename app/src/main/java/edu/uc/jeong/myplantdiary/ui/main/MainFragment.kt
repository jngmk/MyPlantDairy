package edu.uc.jeong.myplantdiary.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.uc.jeong.myplantdiary.R
import edu.uc.jeong.myplantdiary.dto.Specimen
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private lateinit var currentPhotoPath: String
    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val SAVE_IMAGE_REQUEST_CODE: Int = 1999
    private val CAMERA_REQUEST_CODE: Int = 1998
    private val CAMERA_PERMISSION_REQUEST_CODE = 1997
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)  <- depreciated
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.plants.observe(viewLifecycleOwner, Observer {
            plants -> actPlantName.setAdapter(ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, plants))  // context, view, content
        })
        btnTakePhoto.setOnClickListener {
            prepTakePhoto()
        }
        btnLogon.setOnClickListener {
            prepOpenImageGallery()
        }
        btnSave.setOnClickListener {
            saveSpecimen()
        }
        prepRequestLocationUpdates()
    }

    private fun prepRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestLocationUpdates() {
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationLiveData().observe(viewLifecycleOwner, Observer {
            lblLatitudeValue.text = it.latitude
            lblLongitudeValue.text = it.longitude
        })  
    }

    private fun saveSpecimen() {
        var specimen = Specimen().apply {
            latitude = lblLatitudeValue.text.toString()
            longitute = lblLongitudeValue.text.toString()
            plantName = actPlantName.text.toString()
            description = txtDescription.text.toString()
            datePlanted = txtDatePlanted.text.toString()

        }
    }

    private fun prepOpenImageGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
        }
    }

    // See if we have permission or not
    private fun prepTakePhoto() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto()
        } else {
            val permissionRequest = arrayOf(Manifest.permission.CAMERA)  // arrayOf is to see array form
            requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {  // like a case switch
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    takePhoto()
                } else {
                    Toast.makeText(context, "Unable to take photo without permission", Toast.LENGTH_LONG).show()
                }
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                } else {
                    Toast.makeText(context, "Unable to update location without permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun takePhoto() {
        // also is like when in Kotlin
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(context!!.packageManager)
            if (takePictureIntent == null) {
                Toast.makeText(context, "Unable to save photo", Toast.LENGTH_LONG).show()
            } else {
                // if we are here, we have a valid intent
                val photoFile: File = createImageFile()
                photoFile.also {
                    val photoURI = FileProvider.getUriForFile(activity!!.applicationContext, "com.myplantdiary.android.FileProvider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, SAVE_IMAGE_REQUEST_CODE)
                }
            }
        }
    }
    // need annotation because createSource method and decodeBitmap method are required API 28 levels
//    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // now we can get the thumbnail
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                imgPlant.setImageBitmap(imageBitmap)
            }
            else if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(context, "Image Saved", Toast.LENGTH_LONG).show()
            }
            else if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data !== null) {  // data: stuff back to us, data.data: image uri user select
                    val image = data.data
                    val source = ImageDecoder.createSource(activity!!.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    imgPlant.setImageBitmap(bitmap)
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

}
