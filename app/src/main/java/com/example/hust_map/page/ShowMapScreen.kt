package com.example.hust_map.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.MapView
import com.amap.api.services.core.LatLonPoint

@Composable
fun ShowMapScreen(
    mEndPoint: LatLonPoint,
    mapView: MapView,
    toSearchScreen: () -> Unit,
    toRouteScreen: () -> Unit,
    clear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Spacer(modifier = Modifier.height(2f.dp))
            AndroidView(modifier = Modifier.weight(3F), factory = { mapView })
            Row(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (mEndPoint != LatLonPoint(0.0, 0.0)) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(onClick = { toRouteScreen() }) {
                        Text(text = "导航去选定地址")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(onClick = {
                        clear()
                    }) {
                        Text(text = "清除路径")
                    }
                }
            }
        }
    }
}