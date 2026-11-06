package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PricePoint
import com.example.ui.theme.CharcoalBase
import com.example.ui.theme.CoolGray
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldBullish
import com.example.ui.theme.EmeraldBullishSoft
import com.example.ui.theme.EmeraldBullishSurface
import com.example.ui.theme.GlassSpecularBright
import com.example.ui.theme.GlassSpecularSubtle
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.GlassSurfaceLow
import com.example.ui.theme.IcyBlueAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiquidCyanRefraction
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SilverNeutral
import com.example.ui.theme.SilverReflection
import com.example.ui.theme.SoftRedBearish
import com.example.ui.theme.SoftRedBearishLight
import com.example.ui.theme.SoftRedBearishSurface
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.TranslucentGraphite
import com.example.ui.theme.TranslucentGraphiteElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Ambient Dark Liquid Glass Canvas Background with subtle caustic light refraction.
 */
@Composable
fun LiquidGlassScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // Subtle top atmospheric Liquid Glass hero refraction texture
        Image(
            painter = painterResource(id = R.drawable.img_liquid_glass_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.22f,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .align(Alignment.TopCenter)
        )

        // Smooth ambient vignette + subtle optical caustic glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x3307090D),
                                Color(0xCC07090D),
                                DeepObsidian,
                                DeepObsidian
                            ),
                            startY = 0f,
                            endY = size.height * 0.48f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x168AB4F8),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.82f, size.height * 0.08f),
                            radius = size.width * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1010B981),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.28f),
                            radius = size.width * 0.55f
                        )
                    )
                }
        )

        content()
    }
}

/**
 * Core Dark Premium Liquid Glass Surface Component.
 * Features:
 * - Translucent graphite glass fill with internal depth gradient
 * - Optical light refraction rim along top-left and bottom-right edges
 * - Dynamic specular highlight shift on touch/press interaction
 * - Top inner caustic reflection line simulating physical curved glass
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    elevated: Boolean = false,
    accentGlowColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_scale"
    )
    val specularAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.62f else 0.36f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "glass_specular"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val baseFillTop = if (elevated) Color(0xD81D2636) else Color(0xC4151C28)
    val baseFillBottom = if (elevated) Color(0xCC121822) else Color(0xB80E131B)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .then(clickModifier)
            .drawWithContent {
                val rPx = cornerRadius.toPx()
                // 1. Translucent frosted graphite body
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            baseFillTop,
                            baseFillBottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(rPx, rPx)
                )

                // 2. Optional subtle semantic refraction bloom
                if (accentGlowColor != null) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentGlowColor.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.18f),
                            radius = size.width * 0.6f
                        )
                    )
                }

                // 3. Diagonal top-left specular glass refraction sheen
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isPressed) 0.14f else 0.07f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.02f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.75f, size.height * 0.85f)
                    ),
                    cornerRadius = CornerRadius(rPx, rPx)
                )

                drawContent()

                // 4. Top edge caustic light refraction line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassSpecularBright.copy(alpha = specularAlpha),
                            SilverReflection.copy(alpha = specularAlpha * 0.85f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(rPx * 0.6f, 1.dp.toPx()),
                    end = Offset(size.width - rPx * 0.6f, 1.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )

                // 5. Outer glass prism border
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassSpecularBright.copy(alpha = specularAlpha),
                            GlassSpecularSubtle.copy(alpha = 0.14f),
                            Color(0x0DFFFFFF),
                            GlassSpecularSubtle.copy(alpha = 0.22f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(rPx, rPx),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun LiquidGlassPill(
    text: String,
    selected: Boolean = false,
    accentColor: Color = IcyBlueAccent,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.22f) else GlassSurfaceLow,
        animationSpec = tween(200),
        label = "pill_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.65f) else GlassSpecularSubtle,
        animationSpec = tween(200),
        label = "pill_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) SoftWhite else CoolGray,
        animationSpec = tween(200),
        label = "pill_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .then(
                if (onClick != null) {
                    Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun PriceChangeBadge(
    changePercent: Double,
    modifier: Modifier = Modifier
) {
    val isPositive = changePercent >= 0.0
    val bg = if (isPositive) EmeraldBullishSurface else SoftRedBearishSurface
    val fg = if (isPositive) EmeraldBullishSoft else SoftRedBearishLight
    val sign = if (isPositive) "+" else ""

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = 0.8.dp,
                color = fg.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$sign${String.format(Locale.US, "%.2f", changePercent)}%",
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 3-Way Probability Distribution Bar (Chance Up + Sideways + Chance Down = 100%)
 */
@Composable
fun ProbabilityDistributionBar(
    chanceUp: Int,
    chanceSideways: Int,
    chanceDown: Int,
    modifier: Modifier = Modifier
) {
    val upWeight = max(0.05f, chanceUp / 100f)
    val sideWeight = max(0.05f, chanceSideways / 100f)
    val downWeight = max(0.05f, chanceDown / 100f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Up $chanceUp%",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldBullishSoft,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sideways $chanceSideways%",
                style = MaterialTheme.typography.labelSmall,
                color = SilverNeutral
            )
            Text(
                text = "Down $chanceDown%",
                style = MaterialTheme.typography.labelMedium,
                color = SoftRedBearishLight,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(CharcoalBase),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(upWeight)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(EmeraldBullish, EmeraldBullishSoft)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .weight(sideWeight)
                    .fillMaxHeight()
                    .background(SilverNeutral.copy(alpha = 0.65f))
            )
            Box(
                modifier = Modifier
                    .weight(downWeight)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(SoftRedBearishLight, SoftRedBearish)
                        )
                    )
            )
        }
    }
}

/**
 * Interactive Hardware-Accelerated Stock Chart with Touch Crosshair, Support/Resistance, and EMA Overlays.
 */
@Composable
fun InteractiveLiquidGlassChart(
    bars: List<PricePoint>,
    currency: String,
    isPositive: Boolean,
    supportLevel: Double,
    resistanceLevel: Double,
    ema20: Double,
    ema50: Double,
    showOverlays: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (bars.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Insufficient chart data", color = CoolGray)
        }
        return
    }

    var scrubbedIndex by remember(bars) { mutableStateOf<Int?>(null) }
    val activeBar = scrubbedIndex?.let { bars.getOrNull(it) } ?: bars.last()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    val lineColor = if (isPositive) EmeraldBullishSoft else SoftRedBearishLight
    val fillTopColor = if (isPositive) EmeraldBullish.copy(alpha = 0.30f) else SoftRedBearish.copy(alpha = 0.30f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Active Bar Readout Header during Scrubbing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (scrubbedIndex != null) "Selected Bar (${dateFormat.format(Date(activeBar.timestampEpochSec * 1000L))})" else "Latest Close (${dateFormat.format(Date(activeBar.timestampEpochSec * 1000L))})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolGray
                )
                Text(
                    text = "$currency ${String.format(Locale.US, "%.2f", activeBar.close)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("High / Low", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", activeBar.high)} / ${String.format(Locale.US, "%.1f", activeBar.low)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverNeutral
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Volume", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Text(
                        text = "${(activeBar.volume / 1_000_000.0).let { String.format(Locale.US, "%.1fM", it) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IcyBlueAccent
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x66080B11))
                .border(0.8.dp, GlassSpecularSubtle, RoundedCornerShape(16.dp))
                .pointerInput(bars) {
                    detectTapGestures(
                        onPress = { offset ->
                            val idx = ((offset.x / size.width.coerceAtLeast(1)) * (bars.size - 1))
                                .roundToInt()
                                .coerceIn(0, bars.size - 1)
                            scrubbedIndex = idx
                            tryAwaitRelease()
                            scrubbedIndex = null
                        }
                    )
                }
                .pointerInput(bars) {
                    detectHorizontalDragGestures(
                        onDragEnd = { scrubbedIndex = null },
                        onDragCancel = { scrubbedIndex = null },
                        onHorizontalDrag = { change, _ ->
                            val idx = ((change.position.x / size.width.coerceAtLeast(1)) * (bars.size - 1))
                                .roundToInt()
                                .coerceIn(0, bars.size - 1)
                            scrubbedIndex = idx
                        }
                    )
                }
                .padding(horizontal = 8.dp, vertical = 14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val closes = bars.map { it.close }
                val rawMin = min(closes.minOrNull() ?: 0.0, if (showOverlays) supportLevel else Double.MAX_VALUE)
                val rawMax = max(closes.maxOrNull() ?: 1.0, if (showOverlays) resistanceLevel else Double.MIN_VALUE)
                val priceSpan = (rawMax - rawMin).coerceAtLeast(0.01)

                fun priceToY(price: Double): Float {
                    val norm = ((price - rawMin) / priceSpan).toFloat().coerceIn(0f, 1f)
                    return size.height * (1f - norm)
                }

                // Subtle horizontal grid lines
                for (g in 1..3) {
                    val y = size.height * (g / 4f)
                    drawLine(
                        color = Color(0x14FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Support & Resistance overlay lines
                if (showOverlays) {
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    val rY = priceToY(resistanceLevel)
                    drawLine(
                        color = SoftRedBearishLight.copy(alpha = 0.45f),
                        start = Offset(0f, rY),
                        end = Offset(size.width, rY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                    val sY = priceToY(supportLevel)
                    drawLine(
                        color = EmeraldBullishSoft.copy(alpha = 0.45f),
                        start = Offset(0f, sY),
                        end = Offset(size.width, sY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                val stepX = size.width / (bars.size - 1).coerceAtLeast(1)
                val linePath = Path()
                val fillPath = Path()

                bars.forEachIndexed { idx, bar ->
                    val x = idx * stepX
                    val y = priceToY(bar.close)
                    if (idx == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, size.height)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (idx - 1) * stepX
                        val prevY = priceToY(bars[idx - 1].close)
                        val ctrlX = (prevX + x) / 2f
                        linePath.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                        fillPath.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                    }
                }
                fillPath.lineTo(size.width, size.height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillTopColor, Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                )

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw touch crosshair or latest point glow
                val highlightIdx = scrubbedIndex ?: (bars.size - 1)
                val hx = highlightIdx * stepX
                val hy = priceToY(bars[highlightIdx].close)

                if (scrubbedIndex != null) {
                    drawLine(
                        color = IcyBlueAccent.copy(alpha = 0.65f),
                        start = Offset(hx, 0f),
                        end = Offset(hx, size.height),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                drawCircle(
                    color = lineColor.copy(alpha = 0.28f),
                    radius = 9.dp.toPx(),
                    center = Offset(hx, hy)
                )
                drawCircle(
                    color = SoftWhite,
                    radius = 4.dp.toPx(),
                    center = Offset(hx, hy)
                )
            }
        }
    }
}

enum class MainNavTab(val label: String, val tag: String) {
    HOME("Home", "nav_tab_home"),
    MARKETS("Markets", "nav_tab_markets"),
    WATCHLIST("Watchlist", "nav_tab_watchlist"),
    NEWS("News", "nav_tab_news"),
    PROFILE("Profile", "nav_tab_profile")
}

data class NavTabItem(
    val tab: MainNavTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating Translucent Liquid Glass Bottom Navigation Dock.
 */
@Composable
fun LiquidGlassBottomNavigationDock(
    items: List<NavTabItem>,
    selectedTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            cornerRadius = 28.dp,
            elevated = true,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.tab == selectedTab
                    val pillPadding by animateDpAsState(
                        targetValue = if (isSelected) 12.dp else 6.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dock_padding"
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) SoftWhite else CoolGray,
                        animationSpec = tween(200),
                        label = "dock_icon"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    IcyBlueAccent.copy(alpha = 0.25f),
                                                    LiquidCyanRefraction.copy(alpha = 0.10f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 0.8.dp,
                                            color = GlassSpecularBright.copy(alpha = 0.55f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                } else Modifier
                            )
                            .clickable { onTabSelected(item.tab) }
                            .padding(vertical = 8.dp, horizontal = pillPadding)
                            .testTag(item.tab.tag)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.tab.label,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) SoftWhite else CoolGray,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
