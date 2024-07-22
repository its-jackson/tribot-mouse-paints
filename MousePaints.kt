package scripts.nexus.sdk.mouse

import com.allatori.annotations.DoNotRename
import org.tribot.api.input.Mouse.getPos
import org.tribot.script.sdk.ScriptListening.addMouseClickListener
import org.tribot.script.sdk.Waiting.wait
import org.tribot.script.sdk.interfaces.MouseClickListener
import org.tribot.script.sdk.painting.MousePaint
import org.tribot.script.sdk.painting.Painting.*
import java.awt.*
import java.awt.geom.GeneralPath
import java.lang.System.currentTimeMillis
import java.util.Collections.synchronizedList
import java.util.function.Consumer
import kotlin.math.sin

@DoNotRename
data class MouseCursorPaintConfig(
    @DoNotRename
    val size: Int = 8,
    @DoNotRename
    val primary: Color = Color(222, 222, 222),
    @DoNotRename
    val secondary: Color = Color(245, 245, 245),
    @DoNotRename
    val shadow: Color = Color(0, 0, 0, 50),
    @DoNotRename
    val gradient: Color = Color(
        defaultGradientColorRGB.first,
        defaultGradientColorRGB.second,
        defaultGradientColorRGB.third
    )
) {
    companion object {
        @DoNotRename
        val defaultGradientColorRGB = Triple(Color.CYAN.red, Color.CYAN.green, Color.CYAN.blue)
    }
}

interface PolymorphicMousePaint : MousePaint {
    var isRippleActive: Boolean
}

class MouseCursorPaint(
    private val config: MouseCursorPaintConfig = MouseCursorPaintConfig()
) : PolymorphicMousePaint {
    private var angle = 0.0
    override var isRippleActive = false

    override fun paintMouse(
        g: Graphics,
        mousePos: Point,
        dragPos: Point
    ) {
        val startX = mousePos.x
        val startY = mousePos.y
        val g2 = g as Graphics2D

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Rotate around the mouse position
        g2.rotate(Math.toRadians(angle), startX.toDouble(), startY.toDouble())

        // Define the dimensions of the oval
        val ovalWidth = 2 * config.size + 12
        val ovalHeight = 2 * config.size + 12

        // Draw the rotating arc with a dynamic stroke and gradient color
        val arcAngle = (180 + 90 * sin(Math.toRadians(angle))).toInt()
        val dynamicStrokeWidth = 2F + 0.5F * sin(Math.toRadians(angle)).toFloat()
        g2.stroke = BasicStroke(dynamicStrokeWidth)

        if (isRippleActive) {
            val gradient = GradientPaint(
                (startX - ovalWidth / 2).toFloat(), startY.toFloat(),
                Color(224, 224, 224),
                (startX + ovalWidth / 2).toFloat(), startY.toFloat(),
                Color(192, 192, 192)
            )
            g2.paint = gradient
            val glowStrokeWidth = dynamicStrokeWidth * 2
            g2.stroke = BasicStroke(glowStrokeWidth)
        } else {
            val gradient = GradientPaint(
                (startX - ovalWidth / 2).toFloat(), startY.toFloat(),
                config.gradient, (startX + ovalWidth / 2).toFloat(),
                startY.toFloat(), Color(
                    0,
                    (config.gradient.green - 95).coerceAtLeast(0),
                    (config.gradient.blue - 55).coerceAtLeast(0)
                )
            )
            g2.paint = gradient
            g2.stroke = BasicStroke(dynamicStrokeWidth)
        }

        g2.drawArc(startX - ovalWidth / 2, startY - ovalHeight / 2, ovalWidth, ovalHeight, -arcAngle / 2, arcAngle)

        // Draw particle effect
        val particleRadius = if (isRippleActive) {
            (6 * Math.random()).toInt() + 2
        } else {
            (4 * Math.random()).toInt()
        }
        val particleColor = if (isRippleActive) {
            Color(192, 192, 192, 200)
        } else {
            config.gradient
        }
        g2.color = particleColor
        g2.fillOval(startX - particleRadius, startY - particleRadius, 2 * particleRadius, 2 * particleRadius)

        // Draw the main lines
        g2.color = config.primary
        g2.drawLine(startX - config.size, startY - config.size, startX + config.size, startY + config.size)
        g2.color = config.secondary
        g2.drawLine(startX + config.size, startY - config.size, startX - config.size, startY + config.size)

        // Reset rotation for other potential drawing
        g2.rotate(-Math.toRadians(angle), startX.toDouble(), startY.toDouble())

        // Update angle for next repaint
        angle += 3.0  // Adjust for faster or slower rotations
        if (angle >= 360) {
            angle -= 360
        }
    }
}

class OriginalMouseCursorPaint(
    private val config: MouseCursorPaintConfig
) : PolymorphicMousePaint {
    override var isRippleActive = false

    override fun paintMouse(
        g: Graphics,
        mousePos: Point,
        dragPos: Point
    ) {
        val startX = mousePos.x
        val startY = mousePos.y
        val g2 = g as Graphics2D

        g2.stroke = BasicStroke(2F)
        g2.color = config.shadow

        g2.drawLine(
            startX - config.size + 1,
            startY - config.size + 1,
            startX + config.size + 1,
            startY + config.size + 1
        )
        g2.drawLine(
            startX + config.size + 1,
            startY - config.size + 1,
            startX - config.size + 1,
            startY + config.size + 1
        )

        g2.color = config.primary
        g2.drawLine(startX - config.size, startY - config.size, startX + config.size, startY + config.size)
        g2.color = config.secondary
        g2.drawLine(startX + config.size, startY - config.size, startX - config.size, startY + config.size)
    }
}

class PlusSignMouseCursorPaint(
    private val config: MouseCursorPaintConfig
) : PolymorphicMousePaint {
    override var isRippleActive = false

    override fun paintMouse(
        g: Graphics,
        mousePos: Point,
        dragPos: Point
    ) {
        val startX = mousePos.x
        val startY = mousePos.y
        val g2 = g as Graphics2D

        g2.stroke = BasicStroke(2F)
        g2.color = config.shadow

        g2.drawLine(startX, startY - config.size, startX, startY + config.size)

        g2.color = config.primary
        g2.drawLine(startX, startY - config.size + 1, startX, startY + config.size - 1)

        g2.color = config.shadow
        g2.drawLine(startX - config.size, startY, startX + config.size, startY)

        g2.color = config.primary
        g2.drawLine(startX - config.size + 1, startY, startX + config.size - 1, startY)
    }
}

@DoNotRename
data class MouseRipple(
    @DoNotRename
    val center: Point,
    @DoNotRename
    val color: Color,
    @DoNotRename
    var radius: Float = 0f,
    @DoNotRename
    var alpha: Int = 255,
    @DoNotRename
    var expanding: Boolean = true
)

@DoNotRename
data class MouseRipplePaintConfig(
    @DoNotRename
    val rippleColorOne: Color = Color(
        defaultRippleColorOneRGB.first,
        defaultRippleColorOneRGB.second,
        defaultRippleColorOneRGB.third
    ),
    @DoNotRename
    val rippleColorTwo: Color = Color(
        defaultRippleColorTwoRGB.first,
        defaultRippleColorTwoRGB.second,
        defaultRippleColorTwoRGB.third
    ),
    @DoNotRename
    val rippleColorThree: Color = Color(
        defaultRippleColorThreeRGB.first,
        defaultRippleColorThreeRGB.second,
        defaultRippleColorThreeRGB.third
    )
) {
    companion object {
        @DoNotRename
        val defaultRippleColorOneRGB = Triple(0, 255, 255)
        @DoNotRename
        val defaultRippleColorTwoRGB = Triple(255, 255, 255)
        @DoNotRename
        val defaultRippleColorThreeRGB = Triple(192, 192, 192)
    }
}

@DoNotRename
data class MouseSplinePaintConfig(
    @DoNotRename
    val trailColor: Color = Color(
        defaultTrailColorRGB.first,
        defaultTrailColorRGB.second,
        defaultTrailColorRGB.third
    )
) {
    companion object {
        @DoNotRename
        val defaultTrailColorRGB = Triple(Color.WHITE.red, Color.WHITE.green, Color.WHITE.blue)
    }
}

class MousePaintThread(
    private var mouseSplinePaintConfig: MouseSplinePaintConfig = MouseSplinePaintConfig(),
    private var mouseCursorPaintConfig: MouseCursorPaintConfig = MouseCursorPaintConfig(),
    private var mouseRipplePaintConfig: MouseRipplePaintConfig = MouseRipplePaintConfig(),
    private var mouseCursorPaint: PolymorphicMousePaint = MouseCursorPaint(mouseCursorPaintConfig)
) : Thread("Mouse Paint Thread"),
    Consumer<Graphics2D>,
    MouseClickListener {
    private val maxPoints = 15
    private val xPoints = IntArray(maxPoints)
    private val yPoints = IntArray(maxPoints)
    private val colors = IntArray(maxPoints) { mouseSplinePaintConfig.trailColor.rgb }
    private val trailPath = GeneralPath()

    private var pointCount = 0
    private var head = 0
    private var tail = 0
    private var lastMouseX = -1
    private var lastMouseY = -1
    private var lastUpdateTime = currentTimeMillis()
    private val ripples = synchronizedList(mutableListOf<MouseRipple>())
    private var clickCount = 0

    fun configure(
        mouseCursorPaintConfig: MouseCursorPaintConfig,
        mouseSplinePaintConfig: MouseSplinePaintConfig,
        mouseRipplePaintConfig: MouseRipplePaintConfig,
        polymorphicMousePaint: PolymorphicMousePaint
    ) {
        this.mouseCursorPaintConfig = mouseCursorPaintConfig
        this.mouseSplinePaintConfig = mouseSplinePaintConfig
        this.mouseRipplePaintConfig = mouseRipplePaintConfig
        this.mouseCursorPaint = polymorphicMousePaint
        setMousePaint(this.mouseCursorPaint)
    }

    override fun mouseClicked(
        point: Point,
        mouseButton: Int,
        isBot: Boolean
    ) {
        val color = when (clickCount) {
            0 -> mouseRipplePaintConfig.rippleColorOne
            1 -> mouseRipplePaintConfig.rippleColorTwo
            else -> mouseRipplePaintConfig.rippleColorThree
        }

        addRipple(MouseRipple(center = point, color = color))

        // Reset after 3 ripples
        clickCount = (clickCount + 1) % 3
    }

    override fun run() {
        addMouseClickListener(this)
        addPaint(this)
        setMousePaint(mouseCursorPaint)
        setMouseSplinePaint { _, _ -> }

        while (true) {
            // We want 50 fps (max limit)
            wait(20)

            val currentTime = currentTimeMillis()
            val deltaTime = currentTime - lastUpdateTime

            val mousePos = getPos()
            addPoint(mousePos.x, mousePos.y)
            fadeEffect(deltaTime)

            synchronized(ripples) {
                val iterator = ripples.iterator()
                while (iterator.hasNext()) {
                    mouseCursorPaint.isRippleActive = true
                    val ripple = iterator.next()

                    if (ripple.expanding) {
                        ripple.radius += 4
                        ripple.alpha -= 8
                        if (ripple.radius >= 50) {
                            // When the radius reaches 50, start contracting
                            ripple.expanding = false
                        }
                    } else {
                        ripple.radius -= 4
                        ripple.alpha += 8
                        if (ripple.radius <= 5 || ripple.alpha >= 255) {
                            // When the radius is too small, or alpha is back to max, remove
                            iterator.remove()
                            mouseCursorPaint.isRippleActive = false
                        }
                    }
                }
            }

            lastUpdateTime = currentTime
        }
    }

    override fun accept(g2: Graphics2D) = draw(g2)

    private fun addRipple(ripple: MouseRipple) = synchronized(ripples) {
        ripples.add(ripple)
    }

    private fun addPoint(x: Int, y: Int) {
        // Mouse hasn't moved, so don't add a new point
        if (x == lastMouseX && y == lastMouseY) return

        // Update last mouse position
        lastMouseX = x
        lastMouseY = y

        xPoints[head] = x
        yPoints[head] = y
        colors[head] = mouseSplinePaintConfig.trailColor.rgb

        head = (head + 1) % maxPoints
        if (pointCount < maxPoints) {
            pointCount++
        } else {
            tail = (tail + 1) % maxPoints
        }
    }

    private fun fadeEffect(deltaTime: Long) {
        // Ensure a minimum fade amount
        val fadeAmount = (deltaTime * 10 / 50).coerceAtLeast(1)

        for (i in 0 until pointCount) {
            val index = (tail + i) % maxPoints
            val currentAlpha = colors[index] ushr 24
            val newAlpha = (currentAlpha - fadeAmount).coerceIn(0, 255)

            colors[index] = (((newAlpha shl 24) or (colors[index] and 0x00FFFFFF).toLong()).toInt())
        }
    }

    private fun draw(g: Graphics2D) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (pointCount >= 3) {
            // IMPORTANT: Reset the path before iteration
            trailPath.reset()
            for (i in 0 until pointCount - 2) {
                val index = (tail + i) % maxPoints
                val nextIndex = (index + 1) % maxPoints
                trailPath.moveTo(xPoints[index].toFloat(), yPoints[index].toFloat())

                val midX = (xPoints[index] + xPoints[nextIndex]) / 2f
                val midY = (yPoints[index] + yPoints[nextIndex]) / 2f
                trailPath.quadTo(midX, midY, xPoints[nextIndex].toFloat(), yPoints[nextIndex].toFloat())

                val color = Color(colors[index], true)
                val nextColor = Color(colors[nextIndex], true)
                val gradient = GradientPaint(xPoints[index].toFloat(), yPoints[index].toFloat(), color, midX, midY, nextColor)

                g.paint = gradient
                g.draw(trailPath)
                trailPath.reset()
            }
        }

        synchronized(ripples) {
            for (ripple in ripples) {
                g.color = Color(ripple.color.red, ripple.color.green, ripple.color.blue, ripple.alpha)
                g.drawOval(
                    ripple.center.x - ripple.radius.toInt(),
                    ripple.center.y - ripple.radius.toInt(),
                    (ripple.radius * 2).toInt(),
                    (ripple.radius * 2).toInt()
                )
            }
        }
    }
}