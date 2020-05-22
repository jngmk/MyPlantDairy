package edu.uc.jeong.myplantdiary.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import edu.uc.jeong.myplantdiary.dto.Photo
import edu.uc.jeong.myplantdiary.dto.Plant
import edu.uc.jeong.myplantdiary.dto.Specimen
import edu.uc.jeong.myplantdiary.service.PlantService

class MainViewModel : ViewModel() {
    private var _plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    private var plantService: PlantService = PlantService()
    private var firestore: FirebaseFirestore

    init {
        fetchPlants("e")
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun fetchPlants(plantName: String) {
        _plants = plantService.fetchPlants(plantName)
    }

    fun save(
        specimen: Specimen,
        photos: java.util.ArrayList<Photo>
    ) {
        val document = firestore.collection("specimens").document()  // set custom collection id, we can update data
        specimen.specimenId = document.id
        val set = document.set(specimen)
        set.addOnSuccessListener {
            Log.d("Firebase", "document saved")
            if (photos != null && photos.size > 0) {
                savePhotos(specimen, photos)
            }
        }
        set.addOnFailureListener {
            Log.d("Firebase", "save failed")
        }
    }

    private fun savePhotos(specimen: Specimen, photos: ArrayList<Photo>) {
        val collection = firestore.collection("specimens")
            .document(specimen.specimenId)
            .collection("photos")
        photos.forEach {
            photo -> val task = collection.add(photo)
            task.addOnSuccessListener {
                photo.id = it.id
            }
        }

    }

    internal var plants: MutableLiveData<ArrayList<Plant>>
        get() {return _plants}
        set(value) {_plants = value}
}
