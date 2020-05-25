package edu.uc.jeong.myplantdiary.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import edu.uc.jeong.myplantdiary.R
import edu.uc.jeong.myplantdiary.dto.Event
import kotlinx.android.synthetic.main.event_fragment.*

class EventFragment : Fragment() {

    companion object {
        fun newInstance() = EventFragment()
    }

    private lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        btnSaveEvent.setOnClickListener {
            saveEvent()
        }
    }

    private fun saveEvent() {
        val event = Event()
        with (event) {
            description = edtDescription.text.toString()
            val quantityString = edtQuantity.text.toString()
            if (quantityString.length > 0) {
                quantity = quantityString.toDouble()
            }
            units = actUnits.text.toString()
            type = actEventType.text.toString()
            date = edtEventDate.text.toString()

        }
    }

}
