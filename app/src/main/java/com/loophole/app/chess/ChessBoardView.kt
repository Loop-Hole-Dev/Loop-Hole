package com.loophole.app.chess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.loophole.app.R
import kotlin.math.min

/**
 * Issue #7: interactive touch chessboard.
 *
 * Renders a [Position], lets the user tap a piece then tap a highlighted square to move
 * (only legal moves are offered), and can be flipped so Black plays from the bottom.
 * Pawns reaching the last rank promote to a queen.
 */
class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var position: Position = Position.start()
        private set

    /** True to draw the board from Black's side. */
    var flipped = false
        set(value) { field = value; invalidate() }

    /** When false the board ignores touches (opponent's turn, thumbnails, finished lessons). */
    var interactive = true
        set(value) { field = value; if (!value) clearSelection(); invalidate() }

    var showCoordinates = true
        set(value) { field = value; invalidate() }

    /** Called with a legal move the user made. The board doesn't apply it; the caller decides. */
    var onMove: ((Move) -> Unit)? = null

    private var lastMove: Move? = null
    private var selected = -1
    private var targets: List<Move> = emptyList()
    private val marks = HashMap<Int, Int>()
    private var hintSquares: List<Int> = emptyList()

    private val lightColor = ContextCompat.getColor(context, R.color.board_light)
    private val darkColor = ContextCompat.getColor(context, R.color.board_dark)
    private val pieceDark = ContextCompat.getColor(context, R.color.piece_dark)
    private val pieceLight = ContextCompat.getColor(context, R.color.piece_light)
    private val hintColor = ContextCompat.getColor(context, R.color.hint_blue)

    private val squarePaint = Paint()
    private val overlayPaint = Paint()
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33000000 }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val coordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD }
    private val pieceFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val pieceStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }

    fun setPosition(position: Position, lastMove: Move? = null) {
        this.position = position
        this.lastMove = lastMove
        clearSelection()
        invalidate()
    }

    /** Tints a square, e.g. green for a correct move or red for a wrong one. */
    fun mark(square: Int, color: Int) {
        marks[square] = color
        invalidate()
    }

    fun clearMarks() {
        marks.clear()
        hintSquares = emptyList()
        invalidate()
    }

    /** Outlines squares to hint at a move (the piece to move, and optionally where it goes). */
    fun showHint(vararg squares: Int) {
        hintSquares = squares.toList()
        invalidate()
    }

    private fun clearSelection() {
        selected = -1
        targets = emptyList()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = when {
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED -> w
            MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED -> h
            else -> min(w, h)
        }
        setMeasuredDimension(size, size)
    }

    private fun squareAt(col: Int, row: Int): Int {
        val file = if (flipped) 7 - col else col
        val rank = if (flipped) row else 7 - row
        return rank * 8 + file
    }

    private fun colOf(sq: Int) = if (flipped) 7 - Squares.file(sq) else Squares.file(sq)
    private fun rowOf(sq: Int) = if (flipped) Squares.rank(sq) else 7 - Squares.rank(sq)

    override fun onDraw(canvas: Canvas) {
        val s = width / 8f

        for (row in 0..7) for (col in 0..7) {
            val sq = squareAt(col, row)
            val isLight = (Squares.file(sq) + Squares.rank(sq)) % 2 == 1
            squarePaint.color = if (isLight) lightColor else darkColor
            val left = col * s
            val top = row * s
            canvas.drawRect(left, top, left + s, top + s, squarePaint)

            val overlay = when {
                marks.containsKey(sq) -> marks.getValue(sq)
                sq == selected -> 0x9AF7EC5E.toInt()
                lastMove?.let { sq == it.from || sq == it.to } == true -> 0x66F7EC5E
                else -> 0
            }
            if (overlay != 0) {
                overlayPaint.color = overlay
                canvas.drawRect(left, top, left + s, top + s, overlayPaint)
            }

            if (showCoordinates) {
                coordPaint.textSize = s * 0.2f
                coordPaint.color = if (isLight) darkColor else lightColor
                if (col == 0) canvas.drawText("${Squares.rank(sq) + 1}", left + s * 0.05f, top + s * 0.22f, coordPaint)
                if (row == 7) canvas.drawText("${'a' + Squares.file(sq)}", left + s * 0.8f, top + s * 0.94f, coordPaint)
            }
        }

        ringPaint.color = hintColor
        ringPaint.strokeWidth = s * 0.08f
        for (sq in hintSquares) {
            val inset = ringPaint.strokeWidth / 2
            val left = colOf(sq) * s
            val top = rowOf(sq) * s
            canvas.drawRect(left + inset, top + inset, left + s - inset, top + s - inset, ringPaint)
        }

        pieceFill.textSize = s * 0.82f
        pieceStroke.textSize = pieceFill.textSize
        pieceStroke.strokeWidth = s * 0.05f
        val fm = pieceFill.fontMetrics
        val baselineOffset = -(fm.ascent + fm.descent) / 2
        for (sq in 0 until 64) {
            val piece = position.pieceAt(sq) ?: continue
            val glyph = GLYPHS[piece.lowercaseChar()] ?: continue
            val cx = colOf(sq) * s + s / 2
            val cy = rowOf(sq) * s + s / 2 + baselineOffset
            val white = piece.isUpperCase()
            pieceStroke.color = pieceDark
            pieceFill.color = if (white) pieceLight else pieceDark
            canvas.drawText(glyph, cx, cy, pieceStroke)
            canvas.drawText(glyph, cx, cy, pieceFill)
        }

        ringPaint.color = 0x33000000
        ringPaint.strokeWidth = s * 0.08f
        for (move in targets) {
            val cx = colOf(move.to) * s + s / 2
            val cy = rowOf(move.to) * s + s / 2
            if (position.pieceAt(move.to) != null) canvas.drawCircle(cx, cy, s * 0.44f, ringPaint)
            else canvas.drawCircle(cx, cy, s * 0.15f, dotPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!interactive) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val s = width / 8f
                val col = (event.x / s).toInt().coerceIn(0, 7)
                val row = (event.y / s).toInt().coerceIn(0, 7)
                handleTap(squareAt(col, row))
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    private fun handleTap(sq: Int) {
        if (selected >= 0) {
            val candidates = targets.filter { it.to == sq }
            if (candidates.isNotEmpty()) {
                val move = candidates.firstOrNull { it.promotion == 'q' } ?: candidates.first()
                clearSelection()
                invalidate()
                onMove?.invoke(move)
                return
            }
        }
        val piece = position.pieceAt(sq)
        if (piece != null && piece.isUpperCase() == position.whiteToMove && sq != selected) {
            selected = sq
            targets = position.legalMoves.filter { it.from == sq }
        } else {
            clearSelection()
        }
        invalidate()
    }

    companion object {
        // Filled chess glyphs; U+FE0E asks for text (not emoji) presentation.
        private val GLYPHS = mapOf(
            'k' to "♚︎", 'q' to "♛︎", 'r' to "♜︎",
            'b' to "♝︎", 'n' to "♞︎", 'p' to "♟︎",
        )
    }
}
