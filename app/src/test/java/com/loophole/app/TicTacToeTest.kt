package com.loophole.app

import com.loophole.app.games.TicTacToe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Issue #17: the computer must never lose, whoever starts and whatever the human plays. */
class TicTacToeTest {

    /** Plays every possible human game against every optimal AI reply. */
    private fun explore(board: CharArray, toMove: Char, ai: Char) {
        if (TicTacToe.isOver(board)) {
            assertNotEquals("AI lost: ${String(board)}", TicTacToe.other(ai), TicTacToe.winner(board))
            return
        }
        val moves = if (toMove == ai) TicTacToe.bestMoves(board, ai)
        else board.indices.filter { board[it] == TicTacToe.EMPTY }
        for (cell in moves) {
            board[cell] = toMove
            explore(board, TicTacToe.other(toMove), ai)
            board[cell] = TicTacToe.EMPTY
        }
    }

    @Test fun neverLosesAsSecondPlayer() = explore(CharArray(9) { ' ' }, 'X', ai = 'O')

    @Test fun neverLosesAsFirstPlayer() = explore(CharArray(9) { ' ' }, 'O', ai = 'O')

    @Test fun takesImmediateWin() {
        val board = "OO XX    ".toCharArray()
        assertEquals(listOf(2), TicTacToe.bestMoves(board, 'O'))
    }

    @Test fun hintIsOptimalAndPrefersThreats() {
        val board = "    X   O".toCharArray() // X center, O corner: X should set up a threat
        val hint = TicTacToe.recommendedMove(board, 'X')!!
        assert(hint in TicTacToe.bestMoves(board, 'X'))
        assertEquals(TicTacToe.EMPTY, board[hint])
    }

    @Test fun blocksImmediateLoss() {
        val board = "XX  O    ".toCharArray()
        assertEquals(listOf(2), TicTacToe.bestMoves(board, 'O'))
    }
}
