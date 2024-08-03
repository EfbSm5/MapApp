package com.example.hust_map.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.MapView

@Composable
fun RouteSearchScreen(mapView: MapView, callback: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { callback() })
                Text(
                    text = "这是导航",
                    style = TextStyle(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            AndroidView(modifier = Modifier, factory = { mapView })
        }

    }
}