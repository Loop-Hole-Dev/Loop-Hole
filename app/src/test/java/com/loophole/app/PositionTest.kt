package com.loophole.app

import com.loophole.app.chess.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Issue #7: legal move validation, checked against standard perft node counts. */
class PositionTest {

    private fun perft(p: Position, depth: Int): Long =
        if (depth == 0) 1 else p.legalMoves.sumOf { perft(p.play(it), depth - 1) }

    @Test fun startPosition() {
        val p = Position.start()
        assertEquals(20, perft(p, 1))
        assertEquals(400, perft(p, 2))
        assertEquals(8902, perft(p, 3))
    }

    @Test fun kiwipete() { // castling, pins, en passant, promotions
        val p = Position.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        assertEquals(48, perft(p, 1))
        assertEquals(2039, perft(p, 2))
        assertEquals(97862, perft(p, 3))
    }

    @Test fun endgameWithEnPassantPins() {
        val p = Position.fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1")
        assertEquals(2812, perft(p, 3))
        assertEquals(43238, perft(p, 4))
    }

    @Test fun promotionsAndChecks() {
        val p = Position.fromFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1")
        assertEquals(264, perft(p, 2))
        assertEquals(9467, perft(p, 3))
    }

    @Test fun position5() {
        val p = Position.fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")
        assertEquals(1486, perft(p, 2))
        assertEquals(62379, perft(p, 3))
    }

    @Test fun sanRoundTrip() {
        val p = Position.fromSanMoves(listOf("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5", "c3", "Nf6", "d4", "exd4", "cxd4", "Bb4+", "Bd2", "Bxd2+"))
        val move = p.parseSan("Nbxd2")!!
        assertEquals("Nbxd2", p.san(move))
        assertNull(p.parseSan("Nxd2")) // ambiguous without the file
        assertEquals("O-O", Position.fromSanMoves(listOf("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5")).let { it.san(it.parseSan("O-O")!!) })
    }

    @Test fun checkmateSuffix() {
        val p = Position.fromSanMoves(listOf("f3", "e5", "g4"))
        val mate = p.parseSan("Qh4")!!
        assertEquals("Qh4#", p.san(mate))
        assertTrue(p.play(mate).isCheckmate())
    }

    @Test fun fenRoundTrip() {
        val fen = "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3"
        assertEquals(fen, Position.fromSanMoves(listOf("e4", "e5", "Nf3", "Nc6", "Bc4")).toFen())
        assertEquals(fen, Position.fromFen(fen).toFen())
    }
}
