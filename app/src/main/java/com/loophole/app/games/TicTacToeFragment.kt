package com.loophole.app.games

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.loophole.app.R
import com.loophole.app.databinding.FragmentTicTacToeBinding
import com.loophole.app.learn.Feedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Issue #17: Tic-Tac-Toe. You are X; the computer (O) plays perfectly, so the best you can
 * do is draw. Hint shows the optimal move(s). Who starts alternates each game.
 */
class TicTacToeFragment : Fragment() {

    private var _binding: FragmentTicTacToeBinding? = null
    private val binding get() = _binding!!

    private val board = CharArray(9) { TicTacToe.EMPTY }
    private val cells = ArrayList<MaterialButton>(9)
    private var humanStarts = true
    private var aiJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTicTacToeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buildGrid()
        binding.hintButton.setOnClickListener { showHint() }
        binding.newGameButton.setOnClickListener {
            humanStarts = !humanStarts
            newGame()
        }
        newGame()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        aiJob?.cancel()
        cells.clear()
        _binding = null
    }

    private fun buildGrid() {
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        for (row in 0..2) {
            val rowLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            binding.grid.addView(rowLayout, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            for (col in 0..2) {
                val index = row * 3 + col
                val cell = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    textSize = 48f
                    insetTop = 0
                    insetBottom = 0
                    cornerRadius = margin * 4
                    setPadding(0, 0, 0, 0)
                    setOnClickListener { onCellTapped(index) }
                }
                rowLayout.addView(
                    cell,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { setMargins(margin, margin, margin, margin) }
                )
                cells.add(cell)
            }
        }
    }

    private fun newGame() {
        aiJob?.cancel()
        board.fill(TicTacToe.EMPTY)
        render()
        if (humanStarts) {
            setStatus(getString(R.string.ttt_your_turn))
        } else {
            setStatus(getString(R.string.ttt_computer_turn))
            playComputer()
        }
    }

    private fun onCellTapped(index: Int) {
        if (board[index] != TicTacToe.EMPTY || TicTacToe.isOver(board) || aiJob?.isActive == true) return
        board[index] = HUMAN
        render()
        if (!checkGameOver()) playComputer()
    }

    private fun playComputer() {
        setStatus(getString(R.string.ttt_computer_turn))
        aiJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(AI_DELAY_MS)
            board[TicTacToe.bestMoves(board, AI).random()] = AI
            render()
            if (!checkGameOver()) setStatus(getString(R.string.ttt_your_turn))
        }
    }

    private fun showHint() {
        if (TicTacToe.isOver(board) || aiJob?.isActive == true) return
        val best = TicTacToe.recommendedMove(board, HUMAN) ?: return
        render(hints = setOf(best))
        setStatus(getString(R.string.ttt_hint))
    }

    /** Updates the tally and status if the game ended. Returns true if it did. */
    private fun checkGameOver(): Boolean {
        val winner = TicTacToe.winner(board)
        if (winner == null && !TicTacToe.isFull(board)) return false
        val key = when (winner) {
            HUMAN -> KEY_WINS
            AI -> KEY_LOSSES
            else -> KEY_DRAWS
        }
        val prefs = requireContext().getSharedPreferences(PREFS, 0)
        prefs.edit { putInt(key, prefs.getInt(key, 0) + 1) }
        when (winner) {
            HUMAN -> { setStatus(getString(R.string.ttt_you_win)); Feedback.correct(binding.grid) }
            AI -> { setStatus(getString(R.string.ttt_you_lose)); Feedback.wrong(binding.grid) }
            else -> setStatus(getString(R.string.ttt_draw))
        }
        render(winLine = TicTacToe.winningLine(board)?.toSet().orEmpty())
        return true
    }

    private fun render(hints: Set<Int> = emptySet(), winLine: Set<Int> = emptySet()) {
        val context = requireContext()
        val green = ContextCompat.getColor(context, R.color.brand_green)
        val red = ContextCompat.getColor(context, R.color.status_wrong)
        val hint = ContextCompat.getColor(context, R.color.hint_blue)
        val outline = MaterialColors.getColor(binding.grid, com.google.android.material.R.attr.colorOutline)
        val density = resources.displayMetrics.density
        cells.forEachIndexed { i, cell ->
            val mark = board[i]
            cell.text = if (mark == TicTacToe.EMPTY) "" else mark.toString()
            cell.setTextColor(if (mark == HUMAN) green else red)
            val highlighted = i in hints || i in winLine
            cell.strokeColor = ColorStateList.valueOf(if (highlighted) hint else outline)
            cell.strokeWidth = ((if (highlighted) 4 else 1) * density).toInt()
            cell.contentDescription = getString(
                R.string.ttt_cell, i / 3 + 1, i % 3 + 1,
                when (mark) { HUMAN -> "X"; AI -> "O"; else -> getString(R.string.ttt_empty) },
            )
        }
        val prefs = context.getSharedPreferences(PREFS, 0)
        binding.tallyText.text = getString(
            R.string.ttt_tally, prefs.getInt(KEY_WINS, 0), prefs.getInt(KEY_DRAWS, 0), prefs.getInt(KEY_LOSSES, 0)
        )
    }

    private fun setStatus(text: String) {
        binding.statusText.text = text
    }

    companion object {
        private const val HUMAN = 'X'
        private const val AI = 'O'
        private const val AI_DELAY_MS = 450L
        private const val PREFS = "tic_tac_toe"
        private const val KEY_WINS = "wins"
        private const val KEY_DRAWS = "draws"
        private const val KEY_LOSSES = "losses"
    }
}
