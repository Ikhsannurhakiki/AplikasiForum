package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.adapter.HomeAdapter
import com.ikhsannurhakiki.aplikasiforum.adapter.MyLoadStateAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.local.entity.SearchEntity
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.BottomSheetDialogSearchBinding
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentHomeBinding
import com.ikhsannurhakiki.aplikasiforum.ui.main.MainActivity
import com.ikhsannurhakiki.aplikasiforum.ui.main.dataStore
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.AuthViewModelFactory
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory

class HomeFragment : Fragment(), ForumInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding
    private lateinit var _binding2: BottomSheetDialogSearchBinding
    private val binding2 get() = _binding2
    private var lecturerId: String? = null
    private var subjectId: Int = 0
    private var materialId: Int = 0
    private var subjectName: String? = null
    private var lecturerName: String? = null
    private var isPunished: String? = null
    private var suppLecturer: Int = 0
    private var tags: String? = null
    private var tagClicked: Boolean = true
    private lateinit var viewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var homeAdapter: HomeAdapter
    private var listSearch = mutableListOf<SearchEntity>()
    private val ADD_DATA_REQUEST_CODE = 1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        arguments?.let {
            subjectName = it.getString(SUBJECTNAME)
            lecturerName = it.getString(LECTURERNAME)
        }
        actionBar?.show()
        actionBar?.title = "Daftar pertanyaan ($subjectName)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val activity = activity as MainActivity
        val pref = UserPreferences.getInstance(activity.dataStore)
        authViewModel =
            ViewModelProvider(
                this,
                AuthViewModelFactory(pref)
            )[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.getSession().observe(viewLifecycleOwner) {
            if (it.accessRights == "Lecturer") {
                binding.fabAdd.isVisible = false
            }
        }
        arguments?.let {
            subjectId = it.getInt(SUBJECTID)
            subjectName = it.getString(SUBJECTNAME)
            lecturerId = it.getString(LECTURERID)
            lecturerName = it.getString(LECTURERNAME)
            tagClicked = it.getBoolean(TAGCLICKED.toString())
            tags = it.getString(TAG)
            suppLecturer = it.getInt(SUPPLECTURER, 0)
            isPunished = it.getString(ISPUNISHED)
            materialId = it.getInt(MATERIALID)
        }
        checkPunishment()

        //code 0 = all
        //code 1 = title
        //code 2 = question
        //code 3 = username
        //code 4 = tag
//
//        val code = 0
//        val key = ""


        if (tagClicked && tags != null) {
            displayData(suppLecturer, 4, tags!!, materialId)
        } else {
            displayData(suppLecturer, 0, "", materialId)
        }

//        if (lecturerName.isNullOrEmpty()) {
//            (activity as MainActivity).setActionBarTitle("$subjectName")
//        } else {
//            (activity as MainActivity).setActionBarTitle("$subjectName | $lecturerName")
//        }

        binding.apply {

            fabSearch.setOnClickListener {
                showSearchDialogBox()
            }

            fabAdd.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt(AddQuestionActivity.SUBJECTID, subjectId)
                bundle.putString(AddQuestionActivity.LECTURERID, lecturerId)
                bundle.putString(AddQuestionActivity.SUBJECTNAME, subjectName)
                bundle.putInt(AddQuestionActivity.SUPPLECTURER, suppLecturer)
                bundle.putInt(AddQuestionActivity.MATERIALID, materialId)
                val intent = Intent(requireContext(), AddQuestionActivity::class.java)
                intent.putExtras(bundle)
                startActivityForResult(intent, ADD_DATA_REQUEST_CODE)
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                val searchCount = listSearch.lastIndex
                if (searchCount > 0) {
                    listSearch.removeAt(searchCount)
                    val code = listSearch[searchCount - 1].code
                    val key = listSearch[searchCount - 1].key
                    displayData(suppLecturer, code, key, materialId)
                } else if (searchCount == 0) {
                    listSearch.removeAt(searchCount)
                    displayData(suppLecturer, 0, "", materialId)
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun showSearchDialogBox() {
        _binding2 = BottomSheetDialogSearchBinding.inflate(
            layoutInflater
        )
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)
        dialog.setContentView(binding2.root)
        dialog.show()

        binding2.btnSearch.setOnClickListener {
            val key = binding2.etSearch.text.toString().trim()
            val radioGroup = binding2.radioGroup.checkedRadioButtonId

            if (key.isNullOrEmpty()) {
                binding2.etSearch.error
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                var code: Int = 0
                when (resources.getResourceEntryName(radioGroup)) {
                    "radio1" -> {
                        displayData(suppLecturer, 1, key, materialId)
                        code = 1
                    }
                    "radio2" -> {
                        displayData(suppLecturer, 2, key, materialId)
                        code = 2
                    }
                    "radio3" -> {
                        displayData(suppLecturer, 3, key, materialId)
                        code = 3
                    }
                    "radio4" -> {
                        displayData(suppLecturer, 4, key, materialId)
                        code = 4
                    }
                }
                val searchEntity = SearchEntity(key, code)
                listSearch.add(searchEntity)
                dialog.hide()
            }
        }
    }

    private fun displayData(suppLecturer: Int, code: Int, key: String, materialId: Int) {
        binding.apply {
            homeAdapter = HomeAdapter(
                requireContext(),
                viewLifecycleOwner,
                this@HomeFragment,
                subjectId,
                lecturerId,
                viewModel
            )

            rvQuestion.setHasFixedSize(true)
            rvQuestion.adapter = homeAdapter.withLoadStateHeaderAndFooter(
                header = MyLoadStateAdapter { homeAdapter.retry() },
                footer = MyLoadStateAdapter { homeAdapter.retry() })

            homeAdapter.addLoadStateListener { loadState ->
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                rvQuestion.isVisible = loadState.source.refresh is LoadState.NotLoading
            }

            viewModel.getAllQuestions(suppLecturer, code, key, materialId)
            viewModel.allQuestionLiveData
                .observe(viewLifecycleOwner) {
                    rvQuestion.layoutManager = LinearLayoutManager(context)
                    homeAdapter.submitData(lifecycle, it)
                }
        }
    }

    override fun click(question: QuestionTagResponse) {
        val views = question.views + 1
        updateViews(question.id, views)
        val bundle = Bundle()
        bundle.putInt(DetailQuestionActivity.SUBJECTID, subjectId)
        bundle.putString(DetailQuestionActivity.LECTURERID, lecturerId)
        bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
        bundle.putString(DetailQuestionActivity.ASKEDBYID, question.askedBy)
        bundle.putString(DetailQuestionActivity.SUBJECTNAME, subjectName)
        bundle.putString(DetailQuestionActivity.QUESTIONSTATUS, question.status)
        bundle.putString(DetailQuestionActivity.ISPUNISHED, isPunished)
        val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
        intent.putExtras(bundle)
        startActivityForResult(intent, ADD_DATA_REQUEST_CODE)
    }


    override fun onImageClicked(userId: String) {
//        val bundle = Bundle()
//        bundle.putString(ImagePopUpDialogFragment.USERID, userId)
//        findNavController().navigate(R.id.navigation_pop_up_image, bundle)
    }

    override fun onTagClicked(tag: String, subjectId: Int, lecturerId: String?) {
        displayData(suppLecturer, 4, tag, materialId)
    }

    override fun answerThreeDotsClicked(answer: Answer) {

    }

    private fun updateViews(questionId: Int, views: Int) {
        viewModel.updateQuestionViews(questionId, views)
    }

    private fun resetPrefKey() {
        val sharedPref = requireActivity().getSharedPreferences("answerPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("answerKey")
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        resetPrefKey()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DATA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            // Data was added in AddDataActivity
            if (data != null) {
                val isSuccess = data.getStringExtra("isSuccess")
                val isSuccess2 = data.getStringExtra("isInsertQuestionSuccess")
                if (isSuccess == "deleteSuccess" || isSuccess2 == "insertSuccess") displayData(
                    suppLecturer,
                    0,
                    "",
                    materialId
                )
            }
        }
    }

    private fun checkPunishment() {
        if (isPunished == "Tipe 1") {
            Toast.makeText(
                context,
                "Anda tidak bisa berinteraksi di kelas ini (Pelanggaran Tipe 1)",
                Toast.LENGTH_SHORT
            ).show()
            binding.fabAdd.isClickable = false
        }
    }

companion object {
    const val MATERIALID = "2"
    const val ISPUNISHED = "ISPUNISHED"
    const val SUPPLECTURER = "0"
    const val TAGCLICKED: Boolean = false
    const val LECTURERID = "LECTURERID"
    const val TAG = "TAG"
    const val SUBJECTID = "1"
    const val SUBJECTNAME = "SUBJECT_NAME"
    const val LECTURERNAME = "LECTURER_NAME"
}
}