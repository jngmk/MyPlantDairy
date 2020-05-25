package edu.uc.jeong.myplantdiary.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import edu.uc.jeong.myplantdiary.MainActivity

import edu.uc.jeong.myplantdiary.R
import edu.uc.jeong.myplantdiary.dto.Event
import kotlinx.android.synthetic.main.event_fragment.*

class EventFragment : DiaryFragment() {

    companion object {
        fun newInstance() = EventFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        btnSaveEvent.setOnClickListener {
            saveEvent()
        }
        btnTakeImagePhoto.setOnClickListener {
            prepTakePhoto()
        }
        btnBackToSpecimen.setOnClickListener {
            (activity as MainActivity).onSwipeRight()
        }
        rcyEvents.hasFixedSize()
        rcyEvents.layoutManager = LinearLayoutManager(context)
        rcyEvents.itemAnimator = DefaultItemAnimator()
        rcyEvents.adapter = EventsAdapter(viewModel.specimen.events, R.layout.rowlayout)
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
            if (photoURI != null) {
                event.localPhotoURI = photoURI.toString()
            }
        }
        viewModel.specimen.events.add(event)
        viewModel.save(event)
        clearAll()
        rcyEvents.adapter?.notifyDataSetChanged()
    }

    private fun clearAll() {
        edtEventDate.setText("")
        actEventType.setText("")
        edtQuantity.setText("")
        actUnits.setText("")
        edtDescription.setText("")
        photoURI = null
    }
}
