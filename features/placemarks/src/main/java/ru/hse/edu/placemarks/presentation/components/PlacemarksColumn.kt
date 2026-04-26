package ru.hse.edu.placemarks.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.hse.edu.placemarks.R
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.presentation.ResultContent
import ru.hse.locallense.presentation.locals.LocalSpacing

@Composable
fun PlacemarksColumn(
    placemarks: ResultContainer<List<Placemark>>,
    onPlacemarkDelete: (Long) -> Unit,
    onPlacemarkOpenOnMap: (Long) -> Unit,
    onPlacemarkOpenInAr: (Long) -> Unit
) {
    ResultContent(
        container = placemarks,
        onTryAgain = { },
        onSuccess = {
            val unwrappedPlacemarks = placemarks.unwrap()
            if (unwrappedPlacemarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.placemarks_empty_state),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = LocalSpacing.current.semiMedium,
                        end = LocalSpacing.current.semiMedium,
                        bottom = LocalSpacing.current.small
                    )
                ) {
                    items(
                        items = unwrappedPlacemarks,
                        key = { it.id }
                    ) {
                        Box(modifier = Modifier.padding(vertical = LocalSpacing.current.extraSmall)) {
                            PlacemarkListItem(
                                placemark = it,
                                onPlacemarkDelete = { onPlacemarkDelete(it.id) },
                                onOpenOnMap = { onPlacemarkOpenOnMap(it.id) },
                                onOpenInAr = { onPlacemarkOpenInAr(it.id) }
                            )
                        }
                    }
                }
            }
        }
    )
}