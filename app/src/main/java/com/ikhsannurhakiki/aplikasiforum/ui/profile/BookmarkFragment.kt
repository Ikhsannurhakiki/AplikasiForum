package com.ikhsannurhakiki.aplikasiforum.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.adapter.HomeAdapter
import com.ikhsannurhakiki.aplikasiforum.adapter.MyLoadStateAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.StatusResponses
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentMyQuestionBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.DetailQuestionActivity
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*


class BookmarkFragment : Fragment(), ForumInterface {

    private lateinit var _binding: FragmentMyQuestionBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private var myQuestionList: ArrayList<QuestionTagResponse> = arrayListOf()
    private lateinit var viewModel: HomeViewModel
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyQuestionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.title = "Ditandai"
        auth.currentUser?.let { displayData("", 0, 6, it.uid) }
    }

    override fun click(question: QuestionTagResponse) {
        val userId = auth.currentUser!!.uid
        viewModel.checkPunishment(question.suppLecturer,userId).observe(viewLifecycleOwner) {
            when (it.status) {
                StatusResponses.SUCCESS -> {
                    var isType2 = false
                    var isType1= false
                    val currentDate = Date()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    for (i in 0 until it.body!!.size) {
                        val punishmentDateTime = it.body?.get(i)?.date?.let { it1 ->
                            dateFormat.parse(
                                it1
                            )
                        }
                        if (punishmentDateTime != null) {
                            if (punishmentDateTime.before(currentDate)){
                            }else{
                                if (it.body?.get(i)?.type.toString() == "Tipe 2") {
                                    isType2 = true
                                }
                                if (it.body?.get(i)?.type.toString() == "Tipe 1") {
                                    isType1 = true
                                }
                            }
                        }
                    }

                    if (isType2) {
                        Toast.makeText(
                            context,
                            "Anda tidak diizinkan masuk ke forum, Silahkan hubungi dosen bersangkutan!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else if (isType1){
                        val views = question.views + 1
                        updateViews(question.id, views)
                        val bundle = Bundle()
                        bundle.putInt(DetailQuestionActivity.SUBJECTID, question.subjectId)
                        bundle.putString(DetailQuestionActivity.LECTURERID, question.lecturerId)
                        bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
                        bundle.putString(DetailQuestionActivity.ISPUNISHED, "Tipe 1")
                        val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }else{
                        val views = question.views + 1
                        updateViews(question.id, views)
                        val bundle = Bundle()
                        bundle.putInt(DetailQuestionActivity.SUBJECTID, question.subjectId)
                        bundle.putString(DetailQuestionActivity.LECTURERID, question.lecturerId)
                        bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
                        bundle.putString(DetailQuestionActivity.ISPUNISHED, "")
                        val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
//                    Toast.makeText(context, "Teststsst", Toast.LENGTH_SHORT).show()
//                    if (it.body?.get(0)?.type.toString() == "Tipe 1") {
//                        Toast.makeText(context, "aaaaaaaaaa", Toast.LENGTH_SHORT).show()
//                        val views = question.views + 1
//                        updateViews(question.id, views)
//                        val bundle = Bundle()
//                        bundle.putInt(DetailQuestionActivity.SUBJECTID, question.subjectId)
//                        bundle.putString(DetailQuestionActivity.LECTURERID, question.lecturerId)
//                        bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
//                        bundle.putString(DetailQuestionActivity.ISPUNISHED, "Tipe 1")
//                        val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
//                        intent.putExtras(bundle)
//                        startActivity(intent)
//                    }
                }
                StatusResponses.EMPTY -> {
                    val views = question.views + 1
                    updateViews(question.id, views)
                    val bundle = Bundle()
                    bundle.putInt(DetailQuestionActivity.SUBJECTID, question.subjectId)
                    bundle.putString(DetailQuestionActivity.LECTURERID, question.lecturerId)
                    bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
                    bundle.putString(DetailQuestionActivity.ISPUNISHED, "")
                    val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onImageClicked(userId: String) {
        TODO("Not yet implemented")
    }

    override fun onTagClicked(tag: String, subjectId: Int, lecturerId: String?) {
        TODO("Not yet implemented")
    }

    override fun answerThreeDotsClicked(answer: Answer) {
        TODO("Not yet implemented")
    }

    private fun displayData(lecturerId: String?, subjectId: Int, code: Int, key: String) {
        binding.apply {
            homeAdapter = HomeAdapter(
                requireContext(),
                viewLifecycleOwner,
                this@BookmarkFragment,
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

            viewModel.getAllQuestions(0, code, key, 1)
            viewModel.allQuestionLiveData
                .observe(viewLifecycleOwner) {
                    rvQuestion.layoutManager = LinearLayoutManager(context)
                    homeAdapter.submitData(lifecycle, it)
                }
        }
    }

    private fun updateViews(questionId: Int, views: Int) {
        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.updateQuestionViews(questionId, views)
    }
}