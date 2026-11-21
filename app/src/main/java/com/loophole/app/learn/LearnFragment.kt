package com.loophole.app.learn

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.loophole.app.R
import com.loophole.app.chess.Move
import com.loophole.app.chess.Position
import com.loophole.app.data.Course
import com.loophole.app.data.CourseRepository
import com.loophole.app.data.Lesson
import com.loophole.app.data.LessonMove
import com.loophole.app.data.ProgressStore
import com.loophole.app.data.Xp
import com.loophole.app.databinding.FragmentLearnBinding
import com.loophole.app.review.DueText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Issue #8: Learn screen. Plays a lesson move by move: the user finds their side's moves,
 * the opponent's replies play automatically, and each move's "why" is shown.
 *
 * Issue #9: correct moves flash green with a sound and haptic; wrong moves flash red,
 * snap back, and (in learn mode) offer a hint.
 *
 * Issue #10: in review mode the same screen runs a queue of due lessons with no hints,
 * counts mistakes, and reschedules each lesson with SM-2.
 *
 * Issue #14: in practice mode the computer opponent picks its replies at random from all
 * of the course's learned lines, so the user has to know every branch.
 *
 * Internally the screen follows a set of candidate lines. Learn and review use one line;
 * practice starts with every learned line and narrows the set as moves are played.
 */
class LearnFragment : Fragment() {

    private enum class Mode { LEARN, REVIEW, PRACTICE }

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    private lateinit var mode: Mode
    private lateinit var queue: List<String>
    private var queueIndex = 0
    private lateinit var course: Course
    private lateinit var lines: List<Lesson>
    private var candidates: List<Lesson> = emptyList()

    private var position = Position.start()
    private var lastMove: Move? = null
    private var ply = 0
    private var mistakes = 0
    private var correctMoves = 0
    private var mistakesThisMove = 0
    private var hintLevel = 0
    private var pending: Job? = null
    private val recentNotes = ArrayDeque<Pair<String, String>>()

    /** The single lesson in learn/review mode. */
    private val lesson get() = lines.first()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        mode = Mode.valueOf(args.getString(ARG_MODE) ?: Mode.LEARN.name)
        queue = args.getStringArrayList(ARG_LESSONS).orEmpty()
        if (queue.isEmpty()) {
            findNavController().popBackStack()
            return
        }
        binding.board.onMove = ::onUserMove
        binding.hintButton.setOnClickListener { showHint() }
        binding.restartButton.setOnClickListener { startLine() }
        if (mode == Mode.PRACTICE) loadPractice() else loadLesson(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pending?.cancel()
        _binding = null
    }

    // ---- Setup ----

    private fun loadLesson(index: Int) {
        val context = requireContext()
        queueIndex = index
        val lesson = CourseRepository.lesson(context, queue[index]) ?: return run { findNavController().popBackStack() }
        course = CourseRepository.course(context, lesson.courseId) ?: return run { findNavController().popBackStack() }
        lines = listOf(lesson)

        setTitle(lesson.title)
        binding.courseLabel.text = if (mode == Mode.REVIEW) {
            getString(R.string.review_label, course.name, index + 1, queue.size)
        } else {
            getString(R.string.lesson_label, course.name, lesson.order, course.lessons.size)
        }
        binding.board.flipped = !course.userIsWhite
        startLine()
    }

    private fun loadPractice() {
        val context = requireContext()
        lines = queue.mapNotNull { CourseRepository.lesson(context, it) }
        course = lines.firstOrNull()?.let { CourseRepository.course(context, it.courseId) }
            ?: return run { findNavController().popBackStack() }
        setTitle(getString(R.string.practice_title, course.name))
        binding.courseLabel.text = resources.getQuantityString(R.plurals.practice_label, lines.size, lines.size)
        binding.board.flipped = !course.userIsWhite
        startLine()
    }

    private fun setTitle(title: String) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    // ---- Flow ----

    private fun startLine() {
        pending?.cancel()
        candidates = lines
        position = Position.start()
        lastMove = null
        ply = 0
        mistakes = 0
        correctMoves = 0
        recentNotes.clear()
        binding.board.clearMarks()
        binding.board.setPosition(position)
        binding.board.interactive = false
        binding.nextButton.isVisible = false
        binding.secondaryButton.isVisible = false
        binding.restartButton.isVisible = true
        binding.hintButton.isVisible = mode == Mode.LEARN

        val side = getString(if (course.userIsWhite) R.string.side_white else R.string.side_black)
        when (mode) {
            Mode.LEARN -> {
                setStatus(getString(R.string.learn_intro, side))
                binding.noteText.text = lesson.summary
            }
            Mode.REVIEW -> {
                setStatus(getString(R.string.review_intro, side))
                binding.noteText.setText(R.string.review_intro_body)
            }
            Mode.PRACTICE -> {
                setStatus(getString(R.string.practice_intro, side))
                binding.noteText.setText(R.string.practice_intro_body)
            }
        }
        advance()
    }

    private fun isUserTurn() = (ply % 2 == 0) == course.userIsWhite

    /** Lines that still have a move at the current ply. */
    private fun continuing() = candidates.filter { it.moves.size > ply }

    private fun advance() {
        updateProgress()
        val next = continuing()
        if (next.isEmpty()) {
            finishLine()
            return
        }
        if (isUserTurn()) {
            mistakesThisMove = 0
            hintLevel = 0
            binding.board.interactive = true
            binding.hintButton.isEnabled = true
            if (ply > 0) setStatus(getString(R.string.your_move))
        } else {
            binding.board.interactive = false
            binding.hintButton.isEnabled = false
            pending = viewLifecycleOwner.lifecycleScope.launch {
                delay(if (ply == 0) FIRST_MOVE_DELAY_MS else OPPONENT_DELAY_MS)
                playOpponentMove(next.random())
            }
        }
    }

    private fun playOpponentMove(line: Lesson) {
        val step = line.moves[ply]
        val move = position.parseSan(step.san) ?: error("Lesson ${line.id} has an illegal move ${step.san}")
        candidates = continuing().filter { it.moves[ply].san == step.san }
        position = position.play(move)
        lastMove = move
        binding.board.clearMarks()
        binding.board.setPosition(position, move)
        addNote(ply, step)
        ply++
        advance()
    }

    private fun onUserMove(move: Move) {
        if (!isUserTurn()) return
        val options = continuing()
        if (options.isEmpty()) return
        val matching = options.filter { position.parseSan(it.moves[ply].san) == move }
        if (matching.isNotEmpty()) {
            candidates = matching
            onCorrect(move, matching.first().moves[ply])
        } else {
            onWrong(move, position.parseSan(options.first().moves[ply].san))
        }
    }

    private fun onCorrect(move: Move, step: LessonMove) {
        correctMoves++
        Feedback.correct(binding.board)
        position = position.play(move)
        lastMove = move
        binding.board.clearMarks()
        binding.board.setPosition(position, move)
        binding.board.mark(move.to, ContextCompat.getColor(requireContext(), R.color.mark_correct))
        setStatus(getString(R.string.correct, moveLabel(ply, step.san)), R.color.status_correct)
        addNote(ply, step)
        ply++
        advance()
    }

    private fun onWrong(move: Move, expected: Move?) {
        mistakes++
        mistakesThisMove++
        Feedback.wrong(binding.board)
        val red = ContextCompat.getColor(requireContext(), R.color.mark_wrong)
        binding.board.interactive = false
        binding.board.setPosition(position.play(move), move)
        binding.board.mark(move.to, red)
        setStatus(
            getString(if (mode == Mode.LEARN) R.string.wrong_learn else R.string.wrong_review),
            R.color.status_wrong,
        )

        // Snap the piece back after a moment.
        pending = viewLifecycleOwner.lifecycleScope.launch {
            delay(SNAP_BACK_DELAY_MS)
            binding.board.clearMarks()
            binding.board.setPosition(position, lastMove)
            binding.board.interactive = true
            if (expected == null) return@launch
            when {
                mode != Mode.LEARN && mistakesThisMove >= 2 -> {
                    binding.board.showHint(expected.from, expected.to)
                    setStatus(getString(R.string.reveal_move, position.san(expected)), R.color.status_wrong)
                }
                mode == Mode.LEARN && mistakesThisMove >= 2 && hintLevel == 0 -> showHint()
            }
        }
    }

    private fun showHint() {
        if (!isUserTurn()) return
        val step = continuing().firstOrNull()?.moves?.get(ply) ?: return
        val expected = position.parseSan(step.san) ?: return
        hintLevel = (hintLevel + 1).coerceAtMost(2)
        if (hintLevel == 1) {
            binding.board.showHint(expected.from)
            setStatus(getString(R.string.hint_piece))
        } else {
            binding.board.showHint(expected.from, expected.to)
            setStatus(getString(R.string.hint_move, step.san))
        }
    }

    private fun finishLine() {
        binding.board.interactive = false
        binding.hintButton.isVisible = false
        binding.nextButton.isVisible = true
        Feedback.correct(binding.board)
        when (mode) {
            Mode.LEARN -> finishLesson()
            Mode.REVIEW -> finishReview()
            Mode.PRACTICE -> finishPractice()
        }
    }

    private fun finishLesson() {
        val context = requireContext()
        val firstTime = !ProgressStore.isCompleted(context, lesson.id)
        ProgressStore.completeLesson(context, lesson)
        val xp = (if (firstTime) Xp.LESSON_FIRST_TIME else Xp.LESSON_REPEAT) +
            (if (mistakes == 0) Xp.PERFECT_BONUS else 0)
        Xp.award(context, xp)

        setStatus(getString(R.string.lesson_complete_xp, xp), R.color.status_correct)
        binding.noteText.text = if (mistakes == 0) getString(R.string.lesson_complete_perfect)
        else resources.getQuantityString(R.plurals.lesson_complete_mistakes, mistakes, mistakes)

        val next = course.lessons.getOrNull(lesson.order) // lessons are 1-indexed
        if (next != null) {
            binding.nextButton.text = getString(R.string.next_lesson, next.title)
            binding.nextButton.setOnClickListener {
                queue = listOf(next.id)
                loadLesson(0)
            }
            showSecondary(R.string.back_to_course) { findNavController().popBackStack() }
        } else {
            binding.nextButton.setText(R.string.course_finished)
            binding.nextButton.setOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun finishReview() {
        val context = requireContext()
        val progress = ProgressStore.recordReview(context, lesson, mistakes)
        val xp = Xp.REVIEW + if (mistakes == 0) Xp.PERFECT_BONUS else 0
        Xp.award(context, xp)
        val due = DueText.relative(progress.dueAt, midSentence = true)
        setStatus(
            getString(if (mistakes == 0) R.string.review_perfect_xp else R.string.review_done_xp, xp),
            if (mistakes == 0) R.color.status_correct else null,
        )
        binding.noteText.text = resources.getQuantityString(R.plurals.review_result, mistakes, mistakes, due)

        val remaining = queue.size - queueIndex - 1
        if (remaining > 0) {
            binding.nextButton.text = resources.getQuantityString(R.plurals.next_review, remaining, remaining)
            binding.nextButton.setOnClickListener { loadLesson(queueIndex + 1) }
        } else {
            binding.nextButton.setText(R.string.finish_review)
            binding.nextButton.setOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun finishPractice() {
        val context = requireContext()
        val xp = correctMoves * Xp.PRACTICE_PER_MOVE
        if (xp > 0) Xp.award(context, xp)
        setStatus(
            getString(if (mistakes == 0) R.string.practice_perfect_xp else R.string.practice_done_xp, xp),
            if (mistakes == 0) R.color.status_correct else null,
        )
        val lineName = candidates.firstOrNull()?.title.orEmpty()
        binding.noteText.text = resources.getQuantityString(
            R.plurals.practice_result, mistakes, lineName, correctMoves, mistakes
        )
        binding.restartButton.isVisible = false
        binding.nextButton.setText(R.string.practice_another)
        binding.nextButton.setOnClickListener { startLine() }
        showSecondary(R.string.back_to_course) { findNavController().popBackStack() }
    }

    private fun showSecondary(text: Int, onClick: () -> Unit) {
        binding.secondaryButton.isVisible = true
        binding.secondaryButton.setText(text)
        binding.secondaryButton.setOnClickListener { onClick() }
    }

    // ---- UI helpers ----

    private fun updateProgress() {
        val length = candidates.maxOfOrNull { it.moves.size } ?: 0
        binding.lessonProgress.max = length.coerceAtLeast(1)
        binding.lessonProgress.setProgressCompat(ply, true)
        binding.stepText.text = getString(R.string.move_progress, ply, length)
    }

    private fun setStatus(text: String, @ColorRes color: Int? = null) {
        binding.statusText.text = text
        val resolved = color?.let { ContextCompat.getColor(requireContext(), it) }
            ?: MaterialColors.getColor(binding.statusText, com.google.android.material.R.attr.colorOnSurface)
        binding.statusText.setTextColor(resolved)
    }

    /** Shows the explanations for the last two moves (yours and the reply). */
    private fun addNote(ply: Int, step: LessonMove) {
        recentNotes.addLast(moveLabel(ply, step.san) to step.note)
        while (recentNotes.size > 2) recentNotes.removeFirst()
        val text = SpannableStringBuilder()
        recentNotes.forEachIndexed { i, (label, note) ->
            if (i > 0) text.append("\n\n")
            text.inSpans(StyleSpan(Typeface.BOLD)) { append(label) }
            text.append("  ").append(note)
        }
        binding.noteText.text = text
    }

    private fun moveLabel(ply: Int, san: String): String {
        val number = ply / 2 + 1
        return if (ply % 2 == 0) "$number. $san" else "$number... $san"
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_LESSONS = "lessonIds"
        private const val FIRST_MOVE_DELAY_MS = 700L
        private const val OPPONENT_DELAY_MS = 650L
        private const val SNAP_BACK_DELAY_MS = 550L

        fun learnArgs(lessonId: String) =
            bundleOf(ARG_MODE to Mode.LEARN.name, ARG_LESSONS to arrayListOf(lessonId))

        fun reviewArgs(lessonIds: List<String>) =
            bundleOf(ARG_MODE to Mode.REVIEW.name, ARG_LESSONS to ArrayList(lessonIds))

        /** Practice against the computer using these learned lessons of one course. */
        fun practiceArgs(lessonIds: List<String>) =
            bundleOf(ARG_MODE to Mode.PRACTICE.name, ARG_LESSONS to ArrayList(lessonIds))
    }
}
