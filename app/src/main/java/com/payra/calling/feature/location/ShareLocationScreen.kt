package com.payra.calling.feature.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassSurface
import com.payra.calling.designsystem.GlowBackground
import com.payra.calling.designsystem.InfoBanner
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraPrimaryButton
import com.payra.calling.designsystem.ScreenHeader
import com.payra.calling.designsystem.enterFromBottom
import java.util.Locale

private fun bestLastKnown(context: Context): Location? {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    var best: Location? = null
    for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)) {
        try {
            val candidate = manager.getLastKnownLocation(provider)
            if (candidate != null && (best == null || candidate.time > (best?.time ?: 0L))) best = candidate
        } catch (e: SecurityException) {
            // permission missing: skip
        } catch (e: Exception) {
            // provider missing: skip
        }
    }
    return best
}

/** Share your current location: a map card, the coordinates, and a Send button. Uses the phone's own location service. */
@Composable
fun ShareLocationScreen(onBack: () -> Unit, onSend: (name: String, address: String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var location by remember { mutableStateOf<Location?>(null) }
    var searchFinished by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result.values.any { it }
    }

    DisposableEffect(hasPermission) {
        var listener: LocationListener? = null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (hasPermission && manager != null) {
            location = bestLastKnown(context)
            if (location == null) {
                val l = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        location = loc
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String) {
                    }

                    override fun onProviderDisabled(provider: String) {
                    }
                }
                listener = l
                try {
                    for (provider in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                        if (manager.isProviderEnabled(provider)) {
                            @Suppress("DEPRECATION")
                            manager.requestSingleUpdate(provider, l, Looper.getMainLooper())
                        }
                    }
                } catch (e: SecurityException) {
                    // permission revoked meanwhile
                }
            }
        }
        onDispose {
            val l = listener
            if (l != null && manager != null) {
                try {
                    manager.removeUpdates(l)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    LaunchedEffect(hasPermission, location) {
        if (hasPermission && location == null) {
            kotlinx.coroutines.delay(12000)
            searchFinished = true
        }
    }

    val loc = location
    val coords = if (loc != null) String.format(Locale.US, "%.6f, %.6f", loc.latitude, loc.longitude) else ""

    val transition = rememberInfiniteTransition(label = "pin")
    val pulse = transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pinPulse",
    )
    val pulseAlpha = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pinPulseAlpha",
    )

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenHeader(title = "Share location", onBack = onBack, modifier = Modifier.enterFromBottom(0, distance = 12.dp))
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .enterFromBottom(1)
                    .clip(RoundedCornerShape(28.dp)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFFE3F1E8))
                    drawOval(
                        color = Color(0xFFCDEAF5),
                        topLeft = Offset(-size.width * 0.1f, size.height * 0.62f),
                        size = Size(size.width * 1.2f, size.height * 0.5f),
                    )
                    val road = Color.White.copy(alpha = 0.95f)
                    drawRect(road, Offset(0f, size.height * 0.30f), Size(size.width, size.height * 0.07f))
                    drawRect(road, Offset(0f, size.height * 0.68f), Size(size.width, size.height * 0.06f))
                    drawRect(road, Offset(size.width * 0.25f, 0f), Size(size.width * 0.04f, size.height))
                    drawRect(road, Offset(size.width * 0.72f, 0f), Size(size.width * 0.05f, size.height))
                }
                if (loc != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(60.dp)
                            .graphicsLayer {
                                scaleX = pulse.value
                                scaleY = pulse.value
                                alpha = pulseAlpha.value
                            }
                            .clip(RoundedCornerShape(30.dp)),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) { drawRect(PayraColors.Primary) }
                    }
                }
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PayraColors.Danger,
                    modifier = Modifier.align(Alignment.Center).size(48.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            GlassSurface(modifier = Modifier.fillMaxWidth().enterFromBottom(2), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current location", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PayraColors.Ink)
                    Spacer(Modifier.height(4.dp))
                    when {
                        !hasPermission -> Text("Allow location to share where you are.", fontSize = 14.sp, color = PayraColors.Muted)
                        loc != null -> Column {
                            Text(coords, fontSize = 14.sp, color = PayraColors.Muted)
                            if (loc.hasAccuracy()) {
                                Text("Accurate to about ${loc.accuracy.toInt()} m", fontSize = 12.sp, color = PayraColors.Muted)
                            }
                        }
                        searchFinished -> Text("Could not find your location. Turn on Location in the phone settings and try again.", fontSize = 14.sp, color = PayraColors.Danger)
                        else -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Finding you", fontSize = 14.sp, color = PayraColors.Muted)
                            Spacer(Modifier.width(8.dp))
                            BouncingDots(color = PayraColors.Muted, dotSize = 5.dp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (!hasPermission) {
                PayraPrimaryButton(
                    text = "Allow location",
                    modifier = Modifier.enterFromBottom(3),
                    onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    },
                )
            } else {
                PayraPrimaryButton(
                    text = if (loc != null) "Send current location" else "Waiting for location...",
                    modifier = Modifier.enterFromBottom(3),
                    onClick = { if (loc != null) onSend("Current location", coords) },
                )
            }
            Spacer(Modifier.height(12.dp))
            InfoBanner("Only the place where you are right now is shared, once. It does not follow you.", modifier = Modifier.enterFromBottom(4))
        }
    }
}
