package com.jp.foodyvilla_backoffice.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import com.jp.foodyvilla_backoffice.presentation.navigation.Screen
import com.jp.foodyvilla_backoffice.presentation.screens.login.LoginViewModel
 import  com.jp.foodyvilla_backoffice.R
@Composable
fun SplashScreen(
    loginViewModel: LoginViewModel,
    navController: NavController
) {

    val currentSession = loginViewModel.currentSession.collectAsStateWithLifecycle().value

    var splashFinished by remember {
        mutableStateOf(false)
    }

    // Minimum splash duration
    LaunchedEffect(Unit) {
        delay(1800)
        splashFinished = true
    }

    LaunchedEffect(currentSession, splashFinished) {

        if (!splashFinished) return@LaunchedEffect

        navController.navigate(if (currentSession != null) Screen.BackOffice else Screen.BackOfficeLogin) {
            popUpTo(Screen.Splash) {
                inclusive = true
            }
        }
    }

    SplashScreen0()
}


@Composable
fun SplashScreen0() {



    /* entry scale + fade */
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val logoScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.4f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label         = "logoAlpha"
    )

    /* plate gentle rocking */
    val infiniteT = rememberInfiniteTransition(label = "splash")
    val plateRock by infiniteT.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "plateRock"
    )

    /* loading bar */
    var barTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { delay(400); barTarget = 1f }
    val barProg by animateFloatAsState(
        targetValue   = barTarget,
        animationSpec = tween(2_000, easing = FastOutSlowInEasing),
        label         = "barProg"
    )

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        /* decorative pulsing circles */
        PulsingCircle(340.dp, (-90).dp, (-200).dp, delay = 0)
        PulsingCircle(240.dp,  120.dp,   200.dp,   delay = 600)
        PulsingCircle(150.dp, (-60).dp,  180.dp,   delay = 1_200)

        /* logo block */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(logoScale).graphicsLayer(alpha = logoAlpha)
        ) {
            /* plate circle */
            Box(
                Modifier.size(200.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
//                Text("🍽️", fontSize = 54.sp,
//                    modifier = Modifier.graphicsLayer(rotationZ = plateRock))

                Icon(painter = painterResource(R.drawable.logo_new), contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    modifier = Modifier.size(180.dp).graphicsLayer(rotationZ = plateRock))
            }

            Spacer(Modifier.height(24.dp))

            /* brand name – Bebas Neue via displayLarge */
//            Text(
//                "FoodyVilla",
//                style     = MaterialTheme.typography.displayLarge.copy(color = White),
//                textAlign = TextAlign.Center
//            )

            Spacer(Modifier.height(6.dp))

            Text(
                "BACK OFFICE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    letterSpacing = 3.sp
                )
            )

            Spacer(Modifier.height(60.dp))

            /* loading bar */
            Box(
                Modifier.width(160.dp).height(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(barProg).clip(CircleShape).background(
                        MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun PulsingCircle(size: Dp, offsetX: Dp, offsetY: Dp, delay: Int) {
    val inf = rememberInfiniteTransition(label = "circle$delay")
    val sc by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(2_000, delayMillis = delay, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sc$delay"
    )
    val al by inf.animateFloat(
        1f, 0.5f,
        infiniteRepeatable(tween(2_000, delayMillis = delay, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "al$delay"
    )
    Box(
        Modifier
            .size(size)
            .offset(offsetX, offsetY)
            .scale(sc)
            .graphicsLayer(alpha = al)
            .clip(CircleShape)
            .background(Color.White.copy(.2f))
    )
}
@Composable
fun FoodyVillaSplash() {
    val inf = rememberInfiniteTransition(label = "splash")

    val ring1Scale by inf.animateFloat(0.5f, 1.9f, infiniteRepeatable(tween(1700, easing = EaseOut), RepeatMode.Restart), label = "r1s")
    val ring1Alpha by inf.animateFloat(0.4f, 0f,   infiniteRepeatable(tween(1700, easing = EaseOut), RepeatMode.Restart), label = "r1a")
    val ring2Scale by inf.animateFloat(0.5f, 1.9f, infiniteRepeatable(tween(1700, easing = EaseOut, delayMillis = 600), RepeatMode.Restart), label = "r2s")
    val ring2Alpha by inf.animateFloat(0.4f, 0f,   infiniteRepeatable(tween(1700, easing = EaseOut, delayMillis = 600), RepeatMode.Restart), label = "r2a")
    val dotRot     by inf.animateFloat(0f, 360f,   infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "dot")
    val textAlpha  by inf.animateFloat(0.45f, 1f,  infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse), label = "txt")

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(190.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val c     = Offset(size.width / 2, size.height / 2)
                    val baseR = size.minDimension * 0.27f
                    drawCircle(primaryColor.copy(ring1Alpha), baseR * ring1Scale, c)
                    drawCircle(secondaryColor.copy(ring2Alpha),  baseR * ring2Scale, c)

                    val orbitR = baseR * 0.96f
                    val angle  = Math.toRadians(dotRot.toDouble())
                    val dp     = Offset(c.x + orbitR * cos(angle).toFloat(), c.y + orbitR * sin(angle).toFloat())
                    drawCircle(primaryColor, 7.dp.toPx(), dp)
                    drawCircle(onSurface.copy(0.5f), 3.dp.toPx(), dp)
                    drawCircle(onSurfaceVariant.copy(alpha = 0.2f), orbitR, c, style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                }

                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(primaryColor.copy(0.38f), surfaceContainer.copy(0.7f)))
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(listOf(onSurface.copy(0.4f), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) { Text("🍽️", fontSize = 40.sp) }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                "FoodyVilla",
                fontSize      = 34.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = onSurface,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Getting your menu ready",
                fontSize      = 13.sp,
                color         = onSurfaceVariant.copy(alpha = textAlpha),
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(52.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(0, 280, 560).forEach { delay ->
                    val a by rememberInfiniteTransition(label = "d$delay").animateFloat(
                        0.18f, 1f,
                        infiniteRepeatable(tween(550, delayMillis = delay, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "da"
                    )
                    Box(Modifier.size(7.dp).clip(CircleShape).background(primaryColor.copy(a)))
                }
            }
        }
    }
}


@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "bg")
    val shift by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "shift"
    )

    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(background, background.copy(alpha = 0.8f))))

        val b1x = size.width * (0.78f + sin(shift * 2 * PI.toFloat()) * 0.08f)
        val b1y = size.height * (0.15f + cos(shift * 2 * PI.toFloat()) * 0.06f)
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(primary.copy(0.28f), Color.Transparent),
                center = Offset(b1x, b1y), radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(b1x, b1y)
        )

        val b2x = size.width * (0.18f + cos(shift * 2 * PI.toFloat()) * 0.07f)
        val b2y = size.height * (0.78f + sin(shift * 2 * PI.toFloat() + 1f) * 0.08f)
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(secondary.copy(0.18f), Color.Transparent),
                center = Offset(b2x, b2y), radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(b2x, b2y)
        )

        val step = 48.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(primary.copy(0.022f), Offset(x, 0f), Offset(x, size.height), 0.5f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(primary.copy(0.022f), Offset(0f, y), Offset(size.width, y), 0.5f)
            y += step
        }
    }
}
