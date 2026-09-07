import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
fun main() {
    val hex = "#80FFFFFF"
    val intVal = android.graphics.Color.parseColor(hex)
    val c = Color(intVal)
    println(c.alpha)
}
