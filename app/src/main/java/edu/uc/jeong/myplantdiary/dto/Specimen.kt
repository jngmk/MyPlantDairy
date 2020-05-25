package edu.uc.jeong.myplantdiary.dto

import com.google.firebase.firestore.Exclude

class Specimen(var plantName: String = "", var latitude: String = "", var longitute: String = "", var description: String = "", var datePlanted: String = "", var specimenId: String = "", var plantId: Int = 0) {

    private var _events: ArrayList<Event> = ArrayList<Event>()

    var events: ArrayList<Event>
        // exclude an attribute from being stored to Firebase Cloud Firestore
        // @Exclude get() {return _events}
        get() {return _events}
        set(value) {_events = value}
    override fun toString(): String {
        return "$plantName $description $latitude $longitute"
    }
}
