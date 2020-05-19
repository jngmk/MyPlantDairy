package edu.uc.jeong.myplantdiary.ui.main.service

import androidx.lifecycle.MutableLiveData
import edu.uc.jeong.myplantdiary.ui.main.dto.Plant

class PlantService {

    fun fetchPlants(plantName: String): MutableLiveData<ArrayList<Plant>> {
        return MutableLiveData<ArrayList<Plant>>()
    }
}