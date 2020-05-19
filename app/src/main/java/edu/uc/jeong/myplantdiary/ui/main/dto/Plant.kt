package edu.uc.jeong.myplantdiary.ui.main.dto

data class Plant(var genus: String, var species: String, var common: String) {
    override fun toString(): String {
        return common
    }
}