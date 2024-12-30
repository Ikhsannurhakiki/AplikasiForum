package com.ikhsannurhakiki.aplikasiforum.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionsPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    val fragments = mutableListOf<Fragment>()
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: Fragment){
        fragments.add(fragment)
    }

    fun removeFragment(position: Int){
        fragments.removeAt(position)
        notifyDataSetChanged()
    }


//    override fun createFragment(position: Int): Fragment {
//        var fragment: Fragment? = null
//        when (position) {
//            0 -> fragment = SubjectFragment()
//            1 -> fragment = ProfileFragment()
//        }
//        return fragment as Fragment
//    }
//
//    override fun getItemCount(): Int {
//        return 2
//    }

}