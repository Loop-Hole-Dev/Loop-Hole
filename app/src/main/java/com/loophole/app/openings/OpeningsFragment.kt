package com.loophole.app.openings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.loophole.app.R
import com.loophole.app.auth.AuthManager
import com.loophole.app.course.CourseDetailFragment
import com.loophole.app.data.CourseRepository
import com.loophole.app.databinding.FragmentOpeningsBinding

/** Issue #5: Openings Catalog (Home). Lists every course; tapping one opens Course Detail. */
class OpeningsFragment : Fragment() {

    private var _binding: FragmentOpeningsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOpeningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val name = AuthManager.currentEmail(requireContext())?.substringBefore('@') ?: getString(R.string.guest)
        adapter = CourseAdapter(
            welcome = getString(R.string.welcome_user, name),
            onClick = { course ->
                findNavController().navigate(R.id.courseDetailFragment, CourseDetailFragment.args(course.id))
            },
        )
        binding.courseList.layoutManager = LinearLayoutManager(requireContext())
        binding.courseList.adapter = adapter
        adapter.submit(CourseRepository.courses(requireContext()))

        CourseRepository.refreshFromFirestore(requireContext()) { courses ->
            if (_binding != null) adapter.submit(courses)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged() // progress bars may have changed after a lesson
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
