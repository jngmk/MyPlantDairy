package edu.uc.jeong.myplantdiary.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uc.jeong.myplantdiary.ui.main.dto.Plant
import edu.uc.jeong.myplantdiary.ui.main.service.PlantService

class MainViewModel : ViewModel() {
    var plants: MutableLiveData<ArrayList<Plant>> = MutableLiveData<ArrayList<Plant>>()
    var plantService: PlantService = PlantService()

    fun fetchPlants(plantName: String) {
        plants = plantService.fetchPlant(plantName)
    }
    // TODO: Implement the ViewModel
}
