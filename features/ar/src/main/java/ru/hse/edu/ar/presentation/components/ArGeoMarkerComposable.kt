package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult

@Composable
fun ArGeoMarkerComposable(placementResult: ArGeoObjectPlacementResult?) {
    Button(
        onClick = { },
        modifier = Modifier
            .width(300.dp)
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE)
        )
    ) {
        Text(
            text = "MARKER",
            fontSize = 32.sp,
            color = Color.White
        )
    }
}
