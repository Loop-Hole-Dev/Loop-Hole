package com.loophole.app.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.loophole.app.R
import com.loophole.app.course.CourseDetailFragment
import com.loophole.app.data.CourseRepository
import com.loophole.app.data.ProgressStore
import com.loophole.app.databinding.FragmentDashboardBinding
import com.loophole.app.databinding.ItemCourseProgressBinding

/** Issue #11: Progress Dashboard. Per-course progress, reviews due today, and "Review Now". */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statLearned.statLabel.setText(R.string.stat_learned)
        binding.statDue.statLabel.setText(R.string.stat_due)
        binding.statStreak.statLabel.setText(R.string.stat_streak)
        binding.reviewNowButton.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.reviewFragment
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val context = requireContext()
        val courses = CourseRepository.courses(context)
        val done = ProgressStore.all(context)
        val totalLessons = courses.sumOf { it.lessons.size }
        val learned = courses.sumOf { c -> c.lessons.count { it.id in done } }
        val dueToday = ProgressStore.dueTodayCount(context)

        binding.statLearned.statValue.text = getString(R.string.fraction, learned, totalLessons)
        binding.statDue.statValue.text = dueToday.toString()
        binding.statStreak.statValue.text = ProgressStore.streak(context).toString()
        binding.reviewNowButton.text = if (dueToday > 0) getString(R.string.review_now_count, dueToday)
        else getString(R.string.review_now)

        binding.courseProgressList.removeAllViews()
        for (course in courses) {
            val completed = course.lessons.count { it.id in done }
            val row = ItemCourseProgressBinding.inflate(layoutInflater, binding.courseProgressList, true)
            row.courseName.text = course.name
            row.courseCount.text = getString(R.string.lessons_completed, completed, course.lessons.size)
            row.courseBar.max = course.lessons.size
            row.courseBar.setProgressCompat(completed, false)
            row.root.setOnClickListener {
                findNavController().navigate(R.id.courseDetailFragment, CourseDetailFragment.args(course.id))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
