package com.ikhsannurhakiki.aplikasiforum.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityFragmentContainerBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.DetailForumFragment
import com.ikhsannurhakiki.aplikasiforum.ui.home.HomeFragment
import com.ikhsannurhakiki.aplikasiforum.ui.profile.EditProfileFragment
import com.ikhsannurhakiki.aplikasiforum.ui.profile.MyQuestionFragment


class FragmentContainerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentContainerBinding
    private lateinit var childFragment: Fragment
    private var actionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val destination = intent.getStringExtra("destination")
        val bitmap = intent.getStringExtra("bitmap")
        when (destination) {
            resources.getString(R.string.profile) -> childFragment = EditProfileFragment()
            resources.getString(R.string.myQuestion) -> childFragment = MyQuestionFragment()
            resources.getString(R.string.bookmark) -> childFragment = EditProfileFragment()
            resources.getString(R.string.title_home) -> childFragment = HomeFragment()
            resources.getString(R.string.detail) -> childFragment = DetailForumFragment()
        }

        actionBar = supportActionBar
        actionBar?.let {
            it.title = destination
        }

        val bundle = Bundle()
        bundle.putString("bitmap", bitmap)

        when (destination) {
            resources.getString(R.string.title_home)-> {
                val intent = getIntent()
                val bundle = intent.getBundleExtra("bundle")
                val a = bundle?.getInt("subjectId")
                bundle?.getString("lecturerId")
                bundle?.getString("subjectName")
                bundle?.getString("lecturerName")
//                childFragment.arguments = bundle
//                val childFragmentManager: FragmentManager = supportFragmentManager
//                childFragmentManager.beginTransaction()
//                    .add(R.id.nav_host_fragment, childFragment)
//                    .commit()

                Toast.makeText(this, a.toString(), Toast.LENGTH_SHORT).show()
            }
//            else -> {
//                childFragment.arguments = bundle
//                val childFragmentManager: FragmentManager = supportFragmentManager
//                childFragmentManager.beginTransaction()
//                    .add(R.id.nav_host_fragment, childFragment)
//                    .commit()
//            }
        }
    }
}