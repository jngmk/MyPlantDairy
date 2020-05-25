package edu.uc.jeong.myplantdiary.dto

data class Event(var type: String = "", var date: String = "", var quantity: Double? = 0.0, var units: String = "", var description: String = "", var localPhotoURI: String? = null, var id: String = "") {
    override fun toString(): String {
        return "$type $quantity $units $description"
    }
}