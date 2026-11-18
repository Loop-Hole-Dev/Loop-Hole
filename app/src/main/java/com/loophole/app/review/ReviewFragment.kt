package com.loophole.app.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.loophole.app.R
import com.loophole.app.data.CourseRepository
import com.loophole.app.data.ProgressStore
import com.loophole.app.databinding.FragmentReviewBinding
import com.loophole.app.databinding.ItemScheduleBinding
import com.loophole.app.learn.LearnFragment

/** Issue #10: shows lessons due for review and starts a no-hints review session. */
class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val context = requireContext()
        val now = System.currentTimeMillis()
        val all = ProgressStore.all(context).values
        val due = ProgressStore.dueNow(context, now)
            .filter { CourseRepository.lesson(context, it.lessonId) != null }

        binding.dueCount.text = due.size.toString()
        binding.dueLabel.text = resources.getQuantityString(R.plurals.reviews_due_label, due.size)
        binding.reviewMessage.text = when {
            all.isEmpty() -> getString(R.string.review_empty)
            due.isNotEmpty() -> getString(R.string.review_ready)
            else -> getString(
                R.string.review_caught_up,
                DueText.relative(all.minOf { it.dueAt }, now, midSentence = true),
            )
        }

        if (due.isNotEmpty()) {
            binding.startReviewButton.text = getString(R.string.start_review)
            binding.startReviewButton.setOnClickListener {
                findNavController().navigate(R.id.learnFragment, LearnFragment.reviewArgs(due.map { it.lessonId }))
            }
        } else {
            binding.startReviewButton.text = getString(R.string.browse_openings)
            binding.startReviewButton.setOnClickListener {
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
                    .selectedItemId = R.id.openingsFragment
            }
        }

        // Every learned lesson with when it's next due.
        binding.scheduleList.removeAllViews()
        val scheduled = all.sortedBy { it.dueAt }
        binding.scheduleHeader.isVisible = scheduled.isNotEmpty()
        for (p in scheduled) {
            val lesson = CourseRepository.lesson(context, p.lessonId) ?: continue
            val course = CourseRepository.course(context, lesson.courseId) ?: continue
            val row = ItemScheduleBinding.inflate(layoutInflater, binding.scheduleList, true)
            row.title.text = lesson.title
            row.subtitle.text = course.name
            val isDue = p.dueAt <= now
            row.trailing.text = if (isDue) getString(R.string.due_now)
            else DueText.relative(p.dueAt, now)
            row.trailing.setTextColor(
                if (isDue) ContextCompat.getColor(context, R.color.status_wrong)
                else com.google.android.material.color.MaterialColors.getColor(
                    row.trailing, com.google.android.material.R.attr.colorOnSurfaceVariant
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
