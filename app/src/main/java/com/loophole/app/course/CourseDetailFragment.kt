package com.loophole.app.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.loophole.app.R
import com.loophole.app.data.Course
import com.loophole.app.data.CourseRepository
import com.loophole.app.data.ProgressStore
import com.loophole.app.databinding.FragmentCourseDetailBinding
import com.loophole.app.databinding.ItemLessonBinding
import com.loophole.app.learn.LearnFragment
import com.loophole.app.openings.CourseAdapter

/** Issue #6: Course Detail. Shows the opening, its lessons, and a "Start Lesson" button. */
class CourseDetailFragment : Fragment() {

    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var course: Course

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getString(ARG_COURSE_ID).orEmpty()
        course = CourseRepository.course(requireContext(), id) ?: run {
            findNavController().popBackStack()
            return
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = course.name

        binding.preview.interactive = false
        binding.preview.showCoordinates = false
        binding.preview.flipped = !course.userIsWhite
        binding.preview.setPosition(course.previewPosition)
        binding.courseName.text = course.name
        binding.courseMeta.text = CourseAdapter.courseMeta(requireContext(), course)
        binding.courseDescription.text = course.description
    }

    override fun onResume() {
        super.onResume()
        if (::course.isInitialized) renderProgress()
    }

    private fun renderProgress() {
        val context = requireContext()
        val done = ProgressStore.all(context)
        val completed = course.lessons.count { it.id in done }
        binding.progressText.text = getString(R.string.lessons_completed, completed, course.lessons.size)

        binding.lessonList.removeAllViews()
        for (lesson in course.lessons) {
            val row = ItemLessonBinding.inflate(layoutInflater, binding.lessonList, true)
            val isDone = lesson.id in done
            row.lessonTitle.text = lesson.title
            row.lessonSummary.text = lesson.summary
            row.lessonBadge.text = if (isDone) "✓" else lesson.order.toString()
            row.lessonBadge.isActivated = isDone
            row.lessonBadge.setTextColor(
                ContextCompat.getColor(context, if (isDone) R.color.white else R.color.brand_green)
            )
            row.root.contentDescription = getString(
                if (isDone) R.string.lesson_row_done else R.string.lesson_row_todo, lesson.order, lesson.title
            )
            row.root.setOnClickListener { openLesson(lesson.id) }
        }

        val next = course.lessons.firstOrNull { it.id !in done }
        if (next != null) {
            binding.startButton.text = getString(
                if (completed == 0) R.string.start_lesson else R.string.continue_lesson, next.order
            )
            binding.startButton.setOnClickListener { openLesson(next.id) }
        } else {
            binding.startButton.text = getString(R.string.practice_again)
            binding.startButton.setOnClickListener { openLesson(course.lessons.first().id) }
        }
    }

    private fun openLesson(lessonId: String) {
        findNavController().navigate(R.id.learnFragment, LearnFragment.learnArgs(lessonId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        fun args(courseId: String) = bundleOf(ARG_COURSE_ID to courseId)
    }
}
