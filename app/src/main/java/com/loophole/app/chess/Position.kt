package com.loophole.app.chess

import kotlin.math.abs

/** A move from one square to another. Squares are 0..63 with a1 = 0, h1 = 7, a8 = 56. */
data class Move(val from: Int, val to: Int, val promotion: Char? = null) {
    /** Long algebraic form, e.g. "e2e4" or "e7e8q". */
    override fun toString() = Squares.name(from) + Squares.name(to) + (promotion ?: "")
}

object Squares {
    fun file(sq: Int) = sq % 8
    fun rank(sq: Int) = sq / 8
    fun name(sq: Int) = "${'a' + file(sq)}${rank(sq) + 1}"
    fun parse(name: String): Int = (name[1] - '1') * 8 + (name[0] - 'a')
}

/**
 * An immutable chess position with full legal move generation (castling, en passant,
 * promotion, check) and SAN conversion. Pieces use FEN letters: uppercase = White.
 */
class Position private constructor(
    private val board: CharArray,
    val whiteToMove: Boolean,
    /** Remaining castling rights, a subset of "KQkq". */
    val castling: String,
    /** En passant target square, or -1. */
    val epSquare: Int,
    val halfmoveClock: Int,
    val fullmoveNumber: Int,
) {

    fun pieceAt(sq: Int): Char? = board[sq].takeIf { it != EMPTY }

    val legalMoves: List<Move> by lazy {
        pseudoLegalMoves().filter { move ->
            val next = play(move)
            !next.isAttacked(next.kingSquare(whiteToMove), byWhite = !whiteToMove)
        }
    }

    fun inCheck(): Boolean {
        val king = kingSquare(whiteToMove)
        return king >= 0 && isAttacked(king, byWhite = !whiteToMove)
    }

    fun isCheckmate() = legalMoves.isEmpty() && inCheck()

    // ---- Making moves ----

    /** Plays [move] without checking legality. Use [legalMoves] or [parseSan] to get valid moves. */
    fun play(move: Move): Position {
        val b = board.copyOf()
        val piece = b[move.from]
        val white = piece.isUpperCase()
        val isPawn = piece.lowercaseChar() == 'p'
        var captured = b[move.to] != EMPTY

        // En passant: the captured pawn sits behind the target square.
        if (isPawn && move.to == epSquare && Squares.file(move.from) != Squares.file(move.to)) {
            b[move.to + if (white) -8 else 8] = EMPTY
            captured = true
        }

        b[move.to] = move.promotion?.let { if (white) it.uppercaseChar() else it.lowercaseChar() } ?: piece
        b[move.from] = EMPTY

        // Castling also moves the rook.
        if (piece.lowercaseChar() == 'k' && abs(move.to - move.from) == 2) {
            if (move.to > move.from) {
                b[move.from + 1] = b[move.from + 3]; b[move.from + 3] = EMPTY
            } else {
                b[move.from - 1] = b[move.from - 4]; b[move.from - 4] = EMPTY
            }
        }

        var rights = castling
        for (sq in intArrayOf(move.from, move.to)) {
            rights = when (sq) {
                4 -> rights.replace("K", "").replace("Q", "")
                60 -> rights.replace("k", "").replace("q", "")
                0 -> rights.replace("Q", "")
                7 -> rights.replace("K", "")
                56 -> rights.replace("q", "")
                63 -> rights.replace("k", "")
                else -> rights
            }
        }

        val ep = if (isPawn && abs(move.to - move.from) == 16) (move.from + move.to) / 2 else -1
        return Position(
            board = b,
            whiteToMove = !whiteToMove,
            castling = rights,
            epSquare = ep,
            halfmoveClock = if (isPawn || captured) 0 else halfmoveClock + 1,
            fullmoveNumber = if (whiteToMove) fullmoveNumber else fullmoveNumber + 1,
        )
    }

    // ---- SAN ----

    /** Standard algebraic notation for a legal [move], including +/# suffixes. */
    fun san(move: Move): String {
        val next = play(move)
        val suffix = when {
            !next.inCheck() -> ""
            next.legalMoves.isEmpty() -> "#"
            else -> "+"
        }
        return sanWithoutSuffix(move) + suffix
    }

    /** Finds the legal move written as [san] (e.g. "Nf3", "exd5", "O-O", "e8=Q+"), or null. */
    fun parseSan(san: String): Move? {
        val clean = san.trim().replace('0', 'O').trimEnd('+', '#', '!', '?')
        return legalMoves.firstOrNull { sanWithoutSuffix(it) == clean }
    }

    private fun sanWithoutSuffix(move: Move): String {
        val piece = board[move.from]
        val kind = piece.lowercaseChar()
        if (kind == 'k' && abs(move.to - move.from) == 2) {
            return if (move.to > move.from) "O-O" else "O-O-O"
        }
        val isCapture = board[move.to] != EMPTY ||
            (kind == 'p' && move.to == epSquare && Squares.file(move.from) != Squares.file(move.to))
        val sb = StringBuilder()
        if (kind == 'p') {
            if (isCapture) sb.append('a' + Squares.file(move.from))
        } else {
            sb.append(kind.uppercaseChar())
            val rivals = legalMoves.filter { it.to == move.to && it.from != move.from && board[it.from] == piece }
            if (rivals.isNotEmpty()) {
                val sameFile = rivals.any { Squares.file(it.from) == Squares.file(move.from) }
                val sameRank = rivals.any { Squares.rank(it.from) == Squares.rank(move.from) }
                when {
                    !sameFile -> sb.append('a' + Squares.file(move.from))
                    !sameRank -> sb.append('1' + Squares.rank(move.from))
                    else -> sb.append(Squares.name(move.from))
                }
            }
        }
        if (isCapture) sb.append('x')
        sb.append(Squares.name(move.to))
        move.promotion?.let { sb.append('=').append(it.uppercaseChar()) }
        return sb.toString()
    }

    // ---- FEN ----

    fun toFen(): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val c = board[rank * 8 + file]
                if (c == EMPTY) {
                    empty++
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    sb.append(c)
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank > 0) sb.append('/')
        }
        sb.append(if (whiteToMove) " w " else " b ")
        sb.append(castling.ifEmpty { "-" })
        sb.append(' ').append(if (epSquare >= 0) Squares.name(epSquare) else "-")
        sb.append(' ').append(halfmoveClock).append(' ').append(fullmoveNumber)
        return sb.toString()
    }

    // ---- Move generation ----

    private fun kingSquare(white: Boolean) = board.indexOf(if (white) 'K' else 'k')

    private fun isOwn(c: Char) = c != EMPTY && c.isUpperCase() == whiteToMove
    private fun isEnemy(c: Char) = c != EMPTY && c.isUpperCase() != whiteToMove

    fun isAttacked(sq: Int, byWhite: Boolean): Boolean {
        val f = Squares.file(sq)
        val r = Squares.rank(sq)

        val pawn = if (byWhite) 'P' else 'p'
        val pawnRank = if (byWhite) r - 1 else r + 1
        if (pawnRank in 0..7) {
            if (f > 0 && board[pawnRank * 8 + f - 1] == pawn) return true
            if (f < 7 && board[pawnRank * 8 + f + 1] == pawn) return true
        }
        if (hasPieceAtOffsets(f, r, KNIGHT_STEPS, if (byWhite) 'N' else 'n')) return true
        if (hasPieceAtOffsets(f, r, KING_STEPS, if (byWhite) 'K' else 'k')) return true

        val queen = if (byWhite) 'Q' else 'q'
        if (slidesInto(f, r, ROOK_DIRS, if (byWhite) 'R' else 'r', queen)) return true
        if (slidesInto(f, r, BISHOP_DIRS, if (byWhite) 'B' else 'b', queen)) return true
        return false
    }

    private fun hasPieceAtOffsets(f: Int, r: Int, steps: Array<IntArray>, piece: Char): Boolean {
        for ((df, dr) in steps) {
            val nf = f + df
            val nr = r + dr
            if (nf in 0..7 && nr in 0..7 && board[nr * 8 + nf] == piece) return true
        }
        return false
    }

    private fun slidesInto(f: Int, r: Int, dirs: Array<IntArray>, piece: Char, queen: Char): Boolean {
        for ((df, dr) in dirs) {
            var nf = f + df
            var nr = r + dr
            while (nf in 0..7 && nr in 0..7) {
                val c = board[nr * 8 + nf]
                if (c != EMPTY) {
                    if (c == piece || c == queen) return true
                    break
                }
                nf += df; nr += dr
            }
        }
        return false
    }

    private fun pseudoLegalMoves(): List<Move> {
        val moves = ArrayList<Move>(48)
        for (sq in 0 until 64) {
            val c = board[sq]
            if (!isOwn(c)) continue
            val f = Squares.file(sq)
            val r = Squares.rank(sq)
            when (c.lowercaseChar()) {
                'p' -> addPawnMoves(moves, sq, f, r)
                'n' -> addSteps(moves, sq, f, r, KNIGHT_STEPS)
                'b' -> addSlides(moves, sq, f, r, BISHOP_DIRS)
                'r' -> addSlides(moves, sq, f, r, ROOK_DIRS)
                'q' -> { addSlides(moves, sq, f, r, BISHOP_DIRS); addSlides(moves, sq, f, r, ROOK_DIRS) }
                'k' -> { addSteps(moves, sq, f, r, KING_STEPS); addCastling(moves, sq) }
            }
        }
        return moves
    }

    private fun addPawnMoves(moves: MutableList<Move>, sq: Int, f: Int, r: Int) {
        val dir = if (whiteToMove) 1 else -1
        val startRank = if (whiteToMove) 1 else 6
        val promoRank = if (whiteToMove) 7 else 0
        val r1 = r + dir
        if (r1 !in 0..7) return
        val one = r1 * 8 + f
        if (board[one] == EMPTY) {
            addPawnMove(moves, sq, one, r1 == promoRank)
            val two = (r + 2 * dir) * 8 + f
            if (r == startRank && board[two] == EMPTY) moves.add(Move(sq, two))
        }
        for (df in intArrayOf(-1, 1)) {
            val nf = f + df
            if (nf !in 0..7) continue
            val target = r1 * 8 + nf
            if (isEnemy(board[target]) || target == epSquare) addPawnMove(moves, sq, target, r1 == promoRank)
        }
    }

    private fun addPawnMove(moves: MutableList<Move>, from: Int, to: Int, promotes: Boolean) {
        if (promotes) "qrbn".forEach { moves.add(Move(from, to, it)) } else moves.add(Move(from, to))
    }

    private fun addSteps(moves: MutableList<Move>, sq: Int, f: Int, r: Int, steps: Array<IntArray>) {
        for ((df, dr) in steps) {
            val nf = f + df
            val nr = r + dr
            if (nf in 0..7 && nr in 0..7 && !isOwn(board[nr * 8 + nf])) moves.add(Move(sq, nr * 8 + nf))
        }
    }

    private fun addSlides(moves: MutableList<Move>, sq: Int, f: Int, r: Int, dirs: Array<IntArray>) {
        for ((df, dr) in dirs) {
            var nf = f + df
            var nr = r + dr
            while (nf in 0..7 && nr in 0..7) {
                val target = nr * 8 + nf
                val c = board[target]
                if (isOwn(c)) break
                moves.add(Move(sq, target))
                if (c != EMPTY) break
                nf += df; nr += dr
            }
        }
    }

    private fun addCastling(moves: MutableList<Move>, sq: Int) {
        val home = if (whiteToMove) 4 else 60
        if (sq != home) return
        val enemy = !whiteToMove
        val (kingSide, queenSide) = if (whiteToMove) 'K' to 'Q' else 'k' to 'q'
        val rook = if (whiteToMove) 'R' else 'r'
        if (isAttacked(home, enemy)) return
        if (kingSide in castling && board[home + 3] == rook &&
            board[home + 1] == EMPTY && board[home + 2] == EMPTY &&
            !isAttacked(home + 1, enemy) && !isAttacked(home + 2, enemy)
        ) moves.add(Move(home, home + 2))
        if (queenSide in castling && board[home - 4] == rook &&
            board[home - 1] == EMPTY && board[home - 2] == EMPTY && board[home - 3] == EMPTY &&
            !isAttacked(home - 1, enemy) && !isAttacked(home - 2, enemy)
        ) moves.add(Move(home, home - 2))
    }

    companion object {
        private const val EMPTY = '.'
        const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        private val KNIGHT_STEPS = arrayOf(
            intArrayOf(1, 2), intArrayOf(2, 1), intArrayOf(2, -1), intArrayOf(1, -2),
            intArrayOf(-1, -2), intArrayOf(-2, -1), intArrayOf(-2, 1), intArrayOf(-1, 2),
        )
        private val KING_STEPS = arrayOf(
            intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(-1, 1),
            intArrayOf(-1, 0), intArrayOf(-1, -1), intArrayOf(0, -1), intArrayOf(1, -1),
        )
        private val ROOK_DIRS = arrayOf(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1))
        private val BISHOP_DIRS = arrayOf(intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1))

        fun start() = fromFen(START_FEN)

        fun fromFen(fen: String): Position {
            val parts = fen.trim().split(Regex("\\s+"))
            val rows = parts[0].split('/')
            require(rows.size == 8) { "Bad FEN: $fen" }
            val board = CharArray(64) { EMPTY }
            for ((i, row) in rows.withIndex()) {
                val rank = 7 - i
                var file = 0
                for (c in row) {
                    if (c.isDigit()) {
                        file += c - '0'
                    } else {
                        board[rank * 8 + file] = c
                        file++
                    }
                }
            }
            return Position(
                board = board,
                whiteToMove = parts.getOrElse(1) { "w" } == "w",
                castling = parts.getOrElse(2) { "-" }.let { if (it == "-") "" else it },
                epSquare = parts.getOrElse(3) { "-" }.let { if (it == "-") -1 else Squares.parse(it) },
                halfmoveClock = parts.getOrElse(4) { "0" }.toInt(),
                fullmoveNumber = parts.getOrElse(5) { "1" }.toInt(),
            )
        }

        /** Plays a sequence of SAN moves from the start position. Throws if any move is illegal. */
        fun fromSanMoves(moves: List<String>): Position =
            moves.fold(start()) { pos, san ->
                pos.play(requireNotNull(pos.parseSan(san)) { "Illegal move $san in ${pos.toFen()}" })
            }
    }
}
