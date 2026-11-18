package com.loophole.app.openings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loophole.app.R
import com.loophole.app.data.Course
import com.loophole.app.data.ProgressStore
import com.loophole.app.databinding.ItemCatalogHeaderBinding
import com.loophole.app.databinding.ItemCourseBinding

/** Header row (welcome text) followed by one card per course. */
class CourseAdapter(
    private val welcome: String,
    private val onClick: (Course) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var courses: List<Course> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<Course>) {
        courses = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = courses.size + 1
    override fun getItemViewType(position: Int) = if (position == 0) TYPE_HEADER else TYPE_COURSE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) HeaderHolder(ItemCatalogHeaderBinding.inflate(inflater, parent, false))
        else CourseHolder(ItemCourseBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> holder.binding.welcomeText.text = welcome
            is CourseHolder -> holder.bind(courses[position - 1])
        }
    }

    class HeaderHolder(val binding: ItemCatalogHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    inner class CourseHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.preview.interactive = false
            binding.preview.showCoordinates = false
        }

        fun bind(course: Course) {
            val context = binding.root.context
            binding.courseName.text = course.name
            binding.courseMeta.text = courseMeta(context, course)
            binding.courseDescription.text = course.description
            binding.preview.flipped = !course.userIsWhite
            binding.preview.setPosition(course.previewPosition)
            val done = ProgressStore.completedCount(context, course)
            binding.courseProgress.max = course.lessons.size
            binding.courseProgress.setProgressCompat(done, false)
            binding.root.contentDescription = context.getString(
                R.string.course_card_description, course.name, done, course.lessons.size
            )
            binding.root.setOnClickListener { onClick(course) }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_COURSE = 1

        fun courseMeta(context: android.content.Context, course: Course): String = context.getString(
            R.string.course_meta,
            context.getString(if (course.userIsWhite) R.string.side_white else R.string.side_black),
            course.difficulty,
            context.resources.getQuantityString(R.plurals.lesson_count, course.lessons.size, course.lessons.size),
        )
    }
}
