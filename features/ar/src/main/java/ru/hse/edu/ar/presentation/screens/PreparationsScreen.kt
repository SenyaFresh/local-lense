package ru.hse.edu.ar.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import ru.hse.edu.ar.presentation.components.heading.HeadingPicker
import ru.hse.edu.ar.presentation.components.heading.normalizeHeading
import ru.hse.edu.ar.presentation.components.preparations.CompassCalibrationCard
import ru.hse.edu.ar.presentation.components.preparations.HeadingInfoCard
import ru.hse.edu.ar.presentation.components.preparations.LocationInfoCard
import ru.hse.edu.ar.presentation.mapkit.LocationPicker
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.presentation.OnLoadingEffect

@Composable
fun PreparationsScreen(
    initialLatitude: Double?,
    initialLongitude: Double?,
    initialHeading: Float? = null,
    onHeadingChange: (Float?) -> Unit,
    onContinue: (latitude: Double, longitude: Double) -> Unit,
) {
    val anchored = rememberAnchoredInitials(initialLatitude, initialLongitude, initialHeading)
        ?: run {
            OnLoadingEffect()
            return
        }

    var latitude by remember { mutableDoubleStateOf(anchored.latitude) }
    var longitude by remember { mutableDoubleStateOf(anchored.longitude) }
    var customHeading by remember { mutableStateOf<Float?>(null) }
    var isMapVisible by remember { mutableStateOf(false) }
    var isHeadingPickerVisible by remember { mutableStateOf(false) }
    var hasCustomLocation by remember { mutableStateOf(false) }

    val displayHeading = customHeading ?: anchored.heading
    val isCustomHeading = customHeading != null

    BackHandler(enabled = isMapVisible || isHeadingPickerVisible) {
        when {
            isHeadingPickerVisible -> isHeadingPickerVisible = false
            isMapVisible -> isMapVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PreparationsContent(
            latitude = latitude,
            longitude = longitude,
            displayHeading = displayHeading,
            isCustomHeading = isCustomHeading,
            isCustomLocation = hasCustomLocation,
            onHeadingClick = { isHeadingPickerVisible = true },
            onLocationClick = { isMapVisible = true },
            onContinueClick = {
                onHeadingChange(customHeading)
                onContinue(latitude, longitude)
            },
        )

        SlideInOverlay(visible = isMapVisible) {
            LocationPicker(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                title = stringResource(R.string.ar_preparations_picker_location_title),
                initialLatitude = latitude,
                initialLongitude = longitude,
                onConfirm = { lat, lng ->
                    latitude = lat
                    longitude = lng
                    hasCustomLocation = true
                    isMapVisible = false
                },
                onDismiss = { isMapVisible = false },
            )
        }

        SlideInOverlay(visible = isHeadingPickerVisible) {
            HeadingPicker(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                title = stringResource(R.string.ar_preparations_picker_heading_title),
                initialHeading = displayHeading ?: 0f,
                onConfirm = { heading ->
                    customHeading = normalizeHeading(heading)
                    isHeadingPickerVisible = false
                },
                onReset = {
                    customHeading = null
                    isHeadingPickerVisible = false
                },
                onDismiss = { isHeadingPickerVisible = false },
            )
        }
    }
}

@Composable
private fun PreparationsContent(
    latitude: Double,
    longitude: Double,
    displayHeading: Float?,
    isCustomHeading: Boolean,
    isCustomLocation: Boolean,
    onHeadingClick: () -> Unit,
    onLocationClick: () -> Unit,
    onContinueClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.ar_preparations_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.ar_preparations_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CompassCalibrationCard()

        Spacer(modifier = Modifier.height(12.dp))

        HeadingInfoCard(
            heading = displayHeading,
            isCustomHeading = isCustomHeading,
            onChangeClick = onHeadingClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LocationInfoCard(
            latitude = latitude,
            longitude = longitude,
            isCustomLocation = isCustomLocation,
            onChangeClick = onLocationClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        DefaultPrimaryButton(
            label = stringResource(R.string.ar_preparations_continue),
            onClick = onContinueClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SlideInOverlay(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = FastOutSlowInEasing),
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing),
        ) + fadeOut(animationSpec = tween(250)),
    ) {
        content()
    }
}

private data class AnchoredInitials(
    val latitude: Double,
    val longitude: Double,
    val heading: Float?,
)

@Composable
private fun rememberAnchoredInitials(
    initialLatitude: Double?,
    initialLongitude: Double?,
    initialHeading: Float?,
): AnchoredInitials? {
    var rememberedLat by remember { mutableStateOf<Double?>(null) }
    var rememberedLng by remember { mutableStateOf<Double?>(null) }
    var rememberedHeading by remember { mutableStateOf<Float?>(null) }

    if (rememberedLat == null && initialLatitude != null) rememberedLat = initialLatitude
    if (rememberedLng == null && initialLongitude != null) rememberedLng = initialLongitude
    if (rememberedHeading == null && initialHeading != null) {
        rememberedHeading = normalizeHeading(initialHeading)
    }

    val lat = rememberedLat ?: return null
    val lng = rememberedLng ?: return null
    return AnchoredInitials(lat, lng, rememberedHeading)
}
