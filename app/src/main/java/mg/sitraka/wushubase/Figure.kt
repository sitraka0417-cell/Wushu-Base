package mg.sitraka.wushubase

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Squelette 11 points, coordonnées 0..100 (vue de profil, face à droite).
 * Ordre : tête, cou, bassin, coude AV, main AV, coude AR, main AR,
 *         genou AV, pied AV, genou AR, pied AR
 */
private fun pose(vararg v: Int): FloatArray = FloatArray(22) { v[it].toFloat() }

val POSES: Map<String, FloatArray> = mapOf(
    "ready" to pose(50,13, 50,24, 50,52, 46,38, 45,50, 54,38, 55,50, 49,72, 48,91, 51,72, 52,91),
    "fists" to pose(50,13, 50,24, 50,52, 41,38, 44,48, 59,38, 56,48, 49,72, 47,91, 51,72, 53,91),
    "salute" to pose(50,13, 50,24, 50,52, 57,36, 64,30, 55,38, 63,31, 49,72, 48,91, 51,72, 52,91),
    "horse" to pose(50,22, 50,32, 50,62, 40,48, 42,58, 60,48, 58,58, 72,74, 70,91, 28,74, 30,91),
    "bow" to pose(54,17, 53,27, 50,60, 42,44, 44,54, 58,44, 56,54, 67,73, 66,91, 37,76, 24,91),
    "punch" to pose(55,17, 54,27, 50,60, 68,32, 84,33, 38,46, 41,55, 67,73, 66,91, 37,76, 24,91),
    "chop_up" to pose(54,17, 53,27, 50,60, 60,14, 62,2, 38,46, 41,55, 67,73, 66,91, 37,76, 24,91),
    "chop" to pose(55,17, 54,27, 50,60, 68,32, 84,42, 38,46, 41,55, 67,73, 66,91, 37,76, 24,91),
    "hook_arc" to pose(55,17, 54,27, 50,60, 68,22, 80,34, 38,46, 41,55, 67,73, 66,91, 37,76, 24,91),
    "palm" to pose(55,17, 54,27, 50,60, 67,30, 84,26, 42,46, 44,55, 67,73, 66,91, 37,76, 24,91),
    "thread" to pose(55,17, 54,27, 50,60, 66,36, 84,40, 60,40, 68,42, 67,73, 66,91, 37,76, 24,91),
    "showpalm" to pose(54,17, 53,27, 50,60, 64,20, 60,4, 40,40, 34,48, 67,73, 66,91, 37,76, 24,91),
    "hook" to pose(55,17, 54,27, 50,60, 66,30, 82,28, 38,36, 28,44, 67,73, 66,91, 37,76, 24,91),
    "elbow" to pose(55,17, 54,27, 50,60, 68,34, 60,24, 60,30, 68,32, 67,73, 66,91, 37,76, 24,91),
    "drop" to pose(42,43, 40,54, 36,76, 54,52, 66,60, 34,60, 30,68, 60,86, 86,91, 48,74, 34,91),
    "empty" to pose(50,17, 50,28, 48,58, 60,40, 70,36, 42,40, 48,30, 62,76, 68,91, 44,76, 40,91),
    "rest" to pose(48,43, 48,54, 50,78, 58,56, 66,52, 40,58, 34,50, 60,82, 66,91, 44,84, 54,91),
    "ding" to pose(50,19, 50,30, 50,58, 41,42, 44,52, 59,42, 56,52, 54,76, 56,90, 48,74, 48,91),
    "oneleg" to pose(50,13, 50,24, 50,52, 58,22, 52,8, 42,38, 36,46, 66,46, 62,62, 50,72, 50,91),
    "kick_front" to pose(50,13, 50,24, 50,52, 62,38, 74,38, 40,38, 34,30, 69,43, 88,34, 50,72, 50,91),
    "kick_side" to pose(40,15, 45,25, 50,52, 60,28, 66,18, 36,34, 26,28, 64,35, 78,18, 49,72, 49,91),
    "kick_heel" to pose(50,13, 50,24, 50,52, 62,36, 70,28, 40,40, 32,48, 66,50, 88,48, 50,72, 50,91),
    "kick_snap" to pose(50,13, 50,24, 50,52, 41,38, 44,48, 59,38, 56,48, 66,56, 84,62, 50,72, 50,91),
    "kick_cresc" to pose(50,13, 50,24, 50,52, 62,26, 58,14, 38,34, 30,28, 53,31, 56,10, 50,72, 50,91),
    "kick_thrust" to pose(40,17, 44,27, 50,52, 34,36, 28,46, 56,38, 62,30, 70,51, 90,50, 50,72, 50,91),
    "prep" to pose(48,32, 48,42, 48,64, 40,52, 34,62, 36,50, 30,60, 60,76, 58,91, 42,76, 42,91),
    "air_kick" to pose(48,6, 49,16, 50,40, 68,22, 84,20, 34,18, 22,10, 66,31, 84,20, 46,56, 36,62),
    "tornado" to pose(40,10, 46,18, 52,40, 62,16, 78,20, 34,24, 24,32, 68,36, 84,28, 50,58, 40,52),
    "air_cresc" to pose(48,6, 49,16, 50,40, 58,16, 60,6, 36,16, 26,8, 56,22, 60,4, 46,56, 40,62),
    "butterfly" to pose(24,34, 32,36, 52,38, 26,44, 18,50, 40,28, 46,20, 68,34, 84,30, 68,44, 84,46),
    "cartwheel" to pose(50,52, 50,42, 50,28, 44,54, 40,66, 56,54, 60,66, 66,14, 78,4, 34,14, 22,4),
    "swallow" to pose(26,42, 34,44, 54,46, 24,44, 14,42, 40,36, 48,28, 72,41, 92,36, 54,69, 54,91),
    "moon" to pose(44,12, 48,24, 50,52, 44,30, 36,16, 62,30, 74,22, 42,34, 36,16, 50,72, 50,91),
)

@Composable
fun FigureStage(poseIds: List<String>, modifier: Modifier = Modifier) {
    val frames = remember(poseIds) {
        val ids = if (poseIds.isEmpty()) listOf("ready") else poseIds
        val base = ids.map { POSES[it] ?: POSES.getValue("ready") }
        base + base.first()
    }
    val n = frames.size - 1
    val transition = rememberInfiniteTransition(label = "figure")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = n.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400 * n, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2E0707), Color(0xFF8E1B1B))))
    ) {
        val s = size.minDimension / 100f
        val ox = (size.width - 100f * s) / 2f
        val oy = (size.height - 100f * s) / 2f

        val i = t.toInt().coerceIn(0, n - 1)
        val f = (t - i).coerceIn(0f, 1f)
        val k = ((f - 0.2f) / 0.6f).coerceIn(0f, 1f)
        val e = k * k * (3f - 2f * k)
        val a = frames[i]
        val b = frames[i + 1]
        val c = FloatArray(22) { a[it] + (b[it] - a[it]) * e }

        fun px(v: Float) = ox + v * s
        fun py(v: Float) = oy + v * s
        fun seg(x1: Float, y1: Float, x2: Float, y2: Float, col: Color, w: Float) {
            drawLine(col, Offset(px(x1), py(y1)), Offset(px(x2), py(y2)), strokeWidth = w * s, cap = StrokeCap.Round)
        }

        val back = Color(0xFFC9951A)
        val front = Color(0xFFFFD23F)
        val skin = Color(0xFFFFE9A8)

        // sol + ombre
        drawLine(Color(0x66FFD23F), Offset(px(4f), py(92f)), Offset(px(96f), py(92f)), strokeWidth = 1.6f * s, cap = StrokeCap.Round)
        drawOval(
            Color(0x44000000),
            topLeft = Offset(px(c[4] - 14f), py(89.5f)),
            size = Size(28f * s, 5f * s)
        )

        val sx = c[2]
        val sy = c[3] + 2f

        // arrière : bras puis jambe
        seg(sx, sy, c[10], c[11], back, 5f); seg(c[10], c[11], c[12], c[13], back, 5f)
        seg(c[4], c[5], c[18], c[19], back, 6f); seg(c[18], c[19], c[20], c[21], back, 5.5f)
        seg(c[20], c[21], c[20] + 5f, c[21], back, 4f)
        // tronc
        seg(c[2], c[3], c[4], c[5], front, 8f)
        // avant : jambe puis bras
        seg(c[4], c[5], c[14], c[15], front, 6.5f); seg(c[14], c[15], c[16], c[17], front, 5.5f)
        seg(c[16], c[17], c[16] + 5f, c[17], front, 4f)
        seg(sx, sy, c[6], c[7], front, 5.5f); seg(c[6], c[7], c[8], c[9], front, 5f)
        // mains + tête
        drawCircle(back, 3f * s, Offset(px(c[12]), py(c[13])))
        drawCircle(front, 3.3f * s, Offset(px(c[8]), py(c[9])))
        drawCircle(skin, 7f * s, Offset(px(c[0]), py(c[1])))
        drawCircle(Color(0xFFC62828), 7f * s, Offset(px(c[0]), py(c[1])), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f * s))
    }
}
