package com.example.hust_map.page

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.MapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.example.hust_map.data.Markers
import com.example.hust_map.data.MarkersInSchool.initMarkerData
import com.example.hust_map.data.MarkersInSchool.initPoints
import com.example.hust_map.onMap.MapController
import com.example.hust_map.onMap.MapSearchUtil
import com.example.hust_map.onMap.overlay.AMapServicesUtil.convertToLatLonPoint
import com.example.hust_map.ultis.MapUtil.changeMapType
import com.example.hust_map.ultis.MapUtil.showMsg

@Composable
fun MapApp() {
    val context = LocalContext.current
    var currentScreen: State by remember { mutableStateOf(State.Map) }
    var mEndPoint: LatLonPoint = convertToLatLonPoint(LatLng(30.513197, 114.413301))
    var pois: List<Markers>? = null
    val mapView = MapView(
        context, AMapOptions().camera(
            CameraPosition(
                LatLng(30.513197, 114.413301), 18f, 0f, 0f
            )
        )
    )
    val mapController = MapController(context = context, poiClick = {
        it.let {
            mEndPoint = convertToLatLonPoint(it!!.coordinate)
            showMsg(context, it.name)
        }
    }, mapClick = {
        it?.let {
            mEndPoint = convertToLatLonPoint(it)
        }
    }, markerClick = {
        it?.let {
            mEndPoint = convertToLatLonPoint(it.position)
            showMsg(context, it.title)
        }
    })
    mapController.MapLifecycle(mapView)
    var expanded by remember { mutableStateOf(false) }
    val mapSearchUtil = MapSearchUtil(
        context = context, mapView = mapView,
        onPoiSearched = { pois = it },
        returnMsg = { showMsg(context, it) },
        returnPoint = { mEndPoint = it },
    )
    MapAppSurface(expanded = expanded,
        onChangeExpanded = { expanded = it },
        onFactoryReset = { initMarkerData(context = context, reset = true) },
        onChangeScreen = { currentScreen = it },
        onChangeMap = { mapView.changeMapType() },
        content = {
            MapContent(
                currentScreen = currentScreen,
                mEndPoint = mEndPoint,
                mapView = mapView,
                onChangeMap = { currentScreen = it },
                pois = pois,
                context = context,
                mapSearchUtil = mapSearchUtil,
            )
        })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapAppSurface(
    expanded: Boolean,
    onChangeExpanded: (Boolean) -> Unit,
    onFactoryReset: () -> Unit,
    onChangeScreen: (State) -> Unit,
    onChangeMap: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "这是一个地图APP") }, navigationIcon = {
            IconButton(onClick = { onChangeExpanded(true) }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onChangeExpanded(false) },
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                Menu(onChangeMap = { onChangeMap() }, onFactoryReset = { onFactoryReset() })
            }
        })

    }, bottomBar = {
        NavigationBar(modifier = Modifier.height(70.dp)) {
            Button(onClick = { onChangeScreen(State.Map) }, modifier = Modifier.weight(2f)) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "map")
                Text(text = "地图界面")
            }
            Button(onClick = { onChangeScreen(State.Search) }, modifier = Modifier.weight(2f)) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "search")
                Text(text = "搜索")
            }
        }

    }) { paddingValues ->
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

@Composable
private fun Menu(onChangeMap: () -> Unit, onFactoryReset: () -> Unit) {
    DropdownMenuItem(text = { Text(text = "更换地图显示样式") }, onClick = {
        onChangeMap()
    })
    DropdownMenuItem(text = { Text(text = "重置所有") }, onClick = { onFactoryReset() })
}

@Composable
private fun MapContent(
    pois: List<Markers>?,
    context: Context,
    currentScreen: State,
    mEndPoint: LatLonPoint,
    mapView: MapView,
    mapSearchUtil: MapSearchUtil,
    onChangeMap: (State) -> Unit
) {
    Crossfade(targetState = currentScreen, label = "") { screen ->
        when (screen) {
            State.Map -> {
                ShowMapScreen(mEndPoint = mEndPoint, mapView = mapView, toSearchScreen = {
                    onChangeMap(State.Search)
                }, toRouteScreen = {
                    mapSearchUtil.startRouteSearch(
                        mStartPoint = LatLonPoint(30.507964, 114.413512), mEndPoint = mEndPoint
                    )
                }, clear = {
                    mapView.map.clear()
                    initPoints(mapView, context)
                })
            }

            State.Search -> {
                ShowSearchScreen(poiList = pois,
                    toShowMapScreen = { onChangeMap(State.Map) },
                    searchForPoi = {
                        mapSearchUtil.searchForPoi(it)
                    },
                    onSelected = { mapSearchUtil.onSelected(it) })
            }

            State.Route -> {
                mapView.removeAllViews()
                mapSearchUtil.startRouteSearch(
                    mStartPoint = LatLonPoint(30.507964, 114.413512), mEndPoint = mEndPoint
                )
                RouteSearchScreen(mapView = mapView) { onChangeMap(State.Map) }
            }
        }
    }
}


interface State {
    data object Map : State
    data object Search : State
    data object Route : State
}
