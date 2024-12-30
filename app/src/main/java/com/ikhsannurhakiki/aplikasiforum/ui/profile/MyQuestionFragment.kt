package com.ikhsannurhakiki.aplikasiforum.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.adapter.HomeAdapter
import com.ikhsannurhakiki.aplikasiforum.adapter.MyLoadStateAdapter
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.FragmentMyQuestionBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.DetailQuestionActivity
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface
import com.ikhsannurhakiki.aplikasiforum.ui.home.ImagePopUpDialogFragment
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import com.ikhsannurhakiki.aplikasiforum.viewmodel.ViewModelFactory


class MyQuestionFragment : Fragment(), ForumInterface {

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
        activity?.supportActionBar?.title = "Pertanyaan saya"
        auth.currentUser?.let { displayData("", 0, 5, it.uid) }
    }

    override fun click(question: QuestionTagResponse) {
        //Tambahkan check isPunished
        val views = question.views + 1
        updateViews(question.id, views)
        val bundle = Bundle()
        bundle.putInt(DetailQuestionActivity.SUBJECTID, question.subjectId)
        bundle.putString(DetailQuestionActivity.LECTURERID, question.lecturerId)
        bundle.putInt(DetailQuestionActivity.QUESTIONID, question.id)
        val intent = Intent(requireContext(), DetailQuestionActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onImageClicked(userId: String) {
        val bundle = Bundle()
        bundle.putString(ImagePopUpDialogFragment.USERID, userId)
        findNavController().navigate(R.id.navigation_pop_up_image, bundle)
    }

    override fun onTagClicked(tag: String, subjectId: Int, lecturerId: String?) {

    }

    override fun answerThreeDotsClicked(answer: Answer) {
    }

    private fun displayData(lecturerId: String?, subjectId: Int, code: Int, key: String) {
        binding.apply {
            homeAdapter = HomeAdapter(
                requireContext(),
                viewLifecycleOwner,
                this@MyQuestionFragment,
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