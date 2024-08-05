package com.example.hust_map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.apis.utils.core.api.AMapUtilCoreApi
import com.example.hust_map.data.Markers
import com.example.hust_map.data.MarkersInSchool.initMarkerData
import com.example.hust_map.data.MarkersInSchool.initPoints
import com.example.hust_map.onMap.MapLife
import com.example.hust_map.onMap.MapLifeCallBack
import com.example.hust_map.onMap.MapTool
import com.example.hust_map.onMap.MapToolCallBack
import com.example.hust_map.page.RouteSearchScreen
import com.example.hust_map.page.ShowMapScreen
import com.example.hust_map.page.ShowSearchScreen
import com.example.hust_map.ui.theme.Hust_mapTheme

@SuppressLint("MutableCollectionMutableState")
class MainActivity : ComponentActivity(), MapToolCallBack, MapLifeCallBack {
    private val TAG = "MainActivity"
    private var mEndPoint by mutableStateOf(LatLonPoint(0.0, 0.0))
    private var pois by mutableStateOf(ArrayList<Markers>())
    private var mStartPoint: LatLonPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePermission()
        initMarkerData(this, false)
        enableEdgeToEdge()
        setContent {
            Hust_mapTheme {
                val mapView = MapView(
                    this, AMapOptions().camera(
                        CameraPosition(
                            LatLng(30.513197, 114.413301), 18f, 0f, 0f
                        )
                    )
                )
                MapLife(this, this).MapLifecycle(mapView = mapView)
                MapApp(mapView = mapView, changeMap = {
                    if (mapView.map.mapType == AMap.MAP_TYPE_NORMAL) {
                        mapView.map.mapType = AMap.MAP_TYPE_SATELLITE
                    } else if (mapView.map.mapType == AMap.MAP_TYPE_SATELLITE) {
                        mapView.map.mapType = AMap.MAP_TYPE_NORMAL
                    }
                }, factoryReset = {
                    initMarkerData(this, true)
                    initPoints(mapView = mapView, this)
                })
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MapApp(mapView: MapView, changeMap: () -> Unit, factoryReset: () -> Unit) {
        val mapTool = MapTool(context = this, mapView = mapView, this)
        var currentScreen: State by remember { mutableStateOf(State.Map) }
        var expanded by remember { mutableStateOf(false) }
        Scaffold(topBar = {
            TopAppBar(title = { Text(text = "这是一个地图APP") }, navigationIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    DropdownMenuItem(text = { Text(text = "更换地图显示样式") }, onClick = {
                        changeMap()
                    })
                    DropdownMenuItem(text = { Text(text = "重置所有") },
                        onClick = { factoryReset() })
                }
            })

        }, bottomBar = {
            NavigationBar(modifier = Modifier.height(70.dp)) {
                Button(onClick = { currentScreen = State.Map }, modifier = Modifier.weight(2f)) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "map")
                    Text(text = "地图界面")
                }
                Button(onClick = { currentScreen = State.Search }, modifier = Modifier.weight(2f)) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "search")
                    Text(text = "搜索")
                }
            }

        }) { paddingValues ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(paddingValues)) {
                Crossfade(targetState = currentScreen, label = "") { screen ->
                    when (screen) {
                        State.Map -> {
                            ShowMapScreen(mEndPoint = mEndPoint,
                                mapView = mapView,
                                toSearchScreen = {
                                    currentScreen = State.Search
                                },
                                toRouteScreen = {
                                    mapTool.startRouteSearch(
                                        mStartPoint = LatLonPoint(30.507964, 114.413512),
                                        mEndPoint = mEndPoint
                                    )
                                },
                                clear = {
                                    mapView.map.clear()
                                    initPoints(mapView, this@MainActivity)
                                })
                        }

                        State.Search -> {
                            ShowSearchScreen(poiList = pois,
                                toShowMapScreen = { currentScreen = State.Map },
                                searchForPoi = { mapTool.searchForPoi(it) },
                                onSelected = { mapTool.onSelected(it) })
                        }

                        State.Route -> {
                            mapView.removeAllViews()
                            mapTool.startRouteSearch(
                                mStartPoint = LatLonPoint(30.507964, 114.413512),
                                mEndPoint = mEndPoint
                            )
                            RouteSearchScreen(mapView = mapView) { currentScreen = State.Map }
                        }
                    }
                }
            }
        }
    }

    private fun handlePermission() {
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        AMapUtilCoreApi.setCollectInfoEnable(true)
        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result: Boolean ->
            Log.d(TAG, "onCreate: $result")
            if (!result) {
                Toast.makeText(this, "请授予权限", Toast.LENGTH_LONG).show()
            }
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    interface State {
        data object Map : State
        data object Search : State
        data object Route : State
    }

    override fun returnPoi(items: ArrayList<Markers>) {
        pois = items

    }

    override fun returnMsg(word: String) {
        Toast.makeText(this, word, Toast.LENGTH_LONG).show()
    }

    override fun returnPoint(point: LatLonPoint) {
        mEndPoint = point
    }

    override fun returnEndLocation(point: LatLonPoint, name: String) {
        mEndPoint = point
        Toast.makeText(this, "你选择了$name", Toast.LENGTH_SHORT).show()
    }

    override fun returnNowLocation(point: LatLonPoint) {
        mStartPoint = point
    }
}