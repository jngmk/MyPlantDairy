package edu.uc.jeong.myplantdiary.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.uc.jeong.myplantdiary.MainActivity
import edu.uc.jeong.myplantdiary.R
import edu.uc.jeong.myplantdiary.dto.Event
import edu.uc.jeong.myplantdiary.dto.Photo
import edu.uc.jeong.myplantdiary.dto.Plant
import edu.uc.jeong.myplantdiary.dto.Specimen
import kotlinx.android.synthetic.main.main_fragment.*
import kotlin.collections.ArrayList

class MainFragment : DiaryFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var locationViewModel: LocationViewModel
    private val CAMERA_REQUEST_CODE: Int = 1998
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    private val AUTH_REQUEST_CODE = 2002
    private var user: FirebaseUser? = null
    private var photos: ArrayList<Photo> = ArrayList<Photo>()
    private var specimen = Specimen()
    private var _events = ArrayList<Event>()
    private var _plantId = 0

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.plants.observe(viewLifecycleOwner, Observer {
            plants -> actPlantName.setAdapter(ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, plants))  // context, view, content
        })
        viewModel.specimens.observe(viewLifecycleOwner, Observer {
            specimens -> spnSpecimens.setAdapter(ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, specimens))
        })
        actPlantName.setOnItemClickListener { parent, view, position, id ->
            var selectedPlant = parent.getItemAtPosition(position) as Plant
            _plantId = selectedPlant.plantId
        }
        btnTakePhoto.setOnClickListener {
            prepTakePhoto()
        }
        btnLogon.setOnClickListener {
            logon()
        }
        btnSave.setOnClickListener {
            saveSpecimen()
        }
        btnForward.setOnClickListener {
            (activity as MainActivity).onSwipeLeft()
        }
        prepRequestLocationUpdates()
        spnSpecimens.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             *
             * Callback method to be invoked when an item in this view has been
             * selected. This callback is invoked only when the newly selected
             * position is different from the previously selected position or if
             * there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the
             * data associated with the selected item.
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                specimen = parent?.getItemAtPosition(position) as Specimen
                // use this specimen object to populate our UI fields
                actPlantName.setText(specimen.plantName)
                txtDescription.setText(specimen.description)
                txtDatePlanted.setText(specimen.datePlanted)
                viewModel.specimen = specimen
                // trigger an update of the events for this specimen.
                viewModel.fetchEvents()
            }
        }
        rcyEvents.hasFixedSize()
        rcyEvents.layoutManager = LinearLayoutManager(context)
        rcyEvents.itemAnimator = DefaultItemAnimator()
        rcyEvents.adapter = EventsAdapter(_events, R.layout.rowlayout)
        viewModel.events.observe(viewLifecycleOwner, Observer {
            events ->
            // remove everything that is in there
            _events.removeAll(_events)
            // update with the new events that we have observed
            _events.addAll(events)
            // tell the recycler view to update
            rcyEvents.adapter!!.notifyDataSetChanged()
        })
    }

    private fun logon() {
        var providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), AUTH_REQUEST_CODE
        )
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

    /**
     * Persist our specimen to long term storage.
     */
    internal fun saveSpecimen() {
        storeSpecimen()

        viewModel.save(specimen, photos, user!!)

        specimen = Specimen()
        photos = ArrayList<Photo>()
    }

    /**
     * Populate a specimen object based on the details entered into the user interface.
     */
    internal fun storeSpecimen() {
        specimen.apply {
            latitude = lblLatitudeValue.text.toString()
            longitude = lblLongitudeValue.text.toString()
            plantName = actPlantName.text.toString()
            description = txtDescription.text.toString()
            datePlanted = txtDatePlanted.text.toString()
            plantId = _plantId
        }
        viewModel.specimen = specimen
    }

    private fun prepOpenImageGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
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
                // make a call to the superclass to handle anything not to handled here.
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
            }
            else if (requestCode == SAVE_IMAGE_REQUEST_CODE) {
                Toast.makeText(context, "Image Saved", Toast.LENGTH_LONG).show()
                var photo = Photo(localURI = photoURI.toString())
                photos.add(photo)
            }
            else if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                if (data != null && data.data !== null) {  // data: stuff back to us, data.data: image uri user select
                    val image = data.data
                    val source = ImageDecoder.createSource(activity!!.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                }
            }
            else if (requestCode == AUTH_REQUEST_CODE) {
                user = FirebaseAuth.getInstance().currentUser
            }
        }
    }

}
