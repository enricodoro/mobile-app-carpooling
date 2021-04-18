package com.mad.carpooling.ui.trip_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mad.carpooling.R

class TripDetailsFragment : Fragment() {

    private lateinit var tripDetailsViewModel: TripDetailsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        tripDetailsViewModel =
                ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_trip_details, container, false)
        return root
    }
}