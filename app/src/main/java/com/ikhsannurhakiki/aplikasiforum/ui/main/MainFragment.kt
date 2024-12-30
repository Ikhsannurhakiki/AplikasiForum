package com.ikhsannurhakiki.aplikasiforum.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.ikhsannurhakiki.aplikasiforum.ui.home.SubjectFragment


class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.ikhsannurhakiki.aplikasiforum.R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container: FrameLayout = requireView().findViewById(com.ikhsannurhakiki.aplikasiforum.R.id.container)
        val secondFragment = SubjectFragment()
        childFragmentManager
            .beginTransaction()
            .replace(container.id, secondFragment)
            .commit()
    }


}