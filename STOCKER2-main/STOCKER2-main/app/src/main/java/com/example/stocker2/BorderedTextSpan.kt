import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.TypefaceSpan

class BorderedTextSpan(
    private val borderColor: Int = Color.BLACK, // Color del borde
    private val borderWidth: Float = 2f // Ancho del borde en píxeles
) : TypefaceSpan("default") {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint)
    }

    private fun applyCustomTypeFace(paint: TextPaint) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = borderWidth
        paint.color = borderColor
    }

   fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val originalColor = paint.color
        val originalStyle = paint.style

        // Draw the border
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderWidth
        canvas.drawText(text, start, end, x, y.toFloat(), paint)

        // Draw the text
        paint.style = originalStyle
        paint.color = originalColor
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}
