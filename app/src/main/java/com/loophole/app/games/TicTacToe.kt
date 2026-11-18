package com.loophole.app.games

/**
 * Issue #17: Tic-Tac-Toe rules and a perfect (minimax) player.
 * Cells are 0..8, left to right, top to bottom; each holds 'X', 'O' or ' '.
 */
object TicTacToe {

    const val EMPTY = ' '

    private val LINES = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6),
    )

    fun other(player: Char) = if (player == 'X') 'O' else 'X'

    /** The winning line, or null. */
    fun winningLine(board: CharArray): IntArray? =
        LINES.firstOrNull { (a, b, c) -> board[a] != EMPTY && board[a] == board[b] && board[b] == board[c] }

    fun winner(board: CharArray): Char? = winningLine(board)?.let { board[it[0]] }

    fun isFull(board: CharArray) = board.none { it == EMPTY }

    fun isOver(board: CharArray) = winner(board) != null || isFull(board)

    /** Every move that is optimal for [player]: wins fastest, or loses slowest. */
    fun bestMoves(board: CharArray, player: Char): List<Int> {
        val scores = board.indices.filter { board[it] == EMPTY }.associateWith { cell ->
            board[cell] = player
            val score = -negamax(board, other(player), 1)
            board[cell] = EMPTY
            score
        }
        val best = scores.values.maxOrNull() ?: return emptyList()
        return scores.filterValues { it == best }.keys.toList()
    }

    /**
     * One optimal move to suggest as a hint. When several moves are equally good (often all
     * draws), prefer the one that sets up the most two-in-a-rows, then corners, then the center.
     */
    fun recommendedMove(board: CharArray, player: Char): Int? =
        bestMoves(board, player).maxByOrNull { cell ->
            board[cell] = player
            val threats = LINES.count { line ->
                line.count { board[it] == player } == 2 && line.count { board[it] == EMPTY } == 1
            }
            board[cell] = EMPTY
            threats * 10 + when (cell) { 0, 2, 6, 8 -> 2; 4 -> 1; else -> 0 }
        }

    /** Score for [toMove]: positive means [toMove] wins; quicker wins score higher. */
    private fun negamax(board: CharArray, toMove: Char, depth: Int): Int {
        winner(board)?.let { return if (it == toMove) 10 - depth else depth - 10 }
        if (isFull(board)) return 0
        var best = Int.MIN_VALUE
        for (cell in board.indices) {
            if (board[cell] != EMPTY) continue
            board[cell] = toMove
            best = maxOf(best, -negamax(board, other(toMove), depth + 1))
            board[cell] = EMPTY
        }
        return best
    }
}
