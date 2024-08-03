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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.example.hust_map.onMap.MapLife
import com.example.hust_map.onMap.MapLifeCallBack
import com.example.hust_map.page.RouteSearchScreen
import com.example.hust_map.page.ShowMapScreen
import com.example.hust_map.page.ShowSearchScreen
import com.example.hust_map.ui.theme.Hust_mapTheme

@SuppressLint("MutableCollectionMutableState")
class MainActivity : ComponentActivity(), MapToolCallBack, MapLifeCallBack {
    private val TAG = "MainActivity"
    private lateinit var mapView: MapView
    private var mEndPoint by mutableStateOf(LatLonPoint(0.0, 0.0))
    private var poiItemV2s by mutableStateOf(ArrayList<PoiItemV2>())
    private var mStartPoint: LatLonPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePermission()
        enableEdgeToEdge()
        setContent {
            Hust_mapTheme {
                mapView = MapView(
                    LocalContext.current,
                    AMapOptions().camera(CameraPosition(LatLng(30.513197, 114.413301), 18f, 0f, 0f))
                )
                MapLife(this, this).MapLifecycle(mapView = mapView)
                MapApp()
            }
        }
    }

    @Composable
    private fun MapApp() {
        val mapTool = MapTool(context = this, mapView = mapView, this)
        mapTool.initPoints()
        var currentScreen: State by remember { mutableStateOf(State.Map) }
        Crossfade(targetState = currentScreen, label = "") { screen ->
            when (screen) {
                State.Map -> {
                    ShowMapScreen(mEndPoint = mEndPoint, mapView = mapView, toSearchScreen = {
                        currentScreen = State.Search
                    }, toRouteScreen = {
                        mapTool.startRouteSearch(
                            mStartPoint = LatLonPoint(30.507964, 114.413512), mEndPoint = mEndPoint
                        )
                    }, clear = {
                        mapView.map.clear()
                        mapTool.initPoints()
                    })
                }

                State.Search -> {
                    ShowSearchScreen(list = poiItemV2s,
                        toShowMapScreen = { currentScreen = State.Map },
                        searchForPoi = { mapTool.searchForPoi(keyword = it) },
                        onSelected = { mapTool.onSelected(it) })
                }

                State.Route -> {
                    mapView.removeAllViews()
                    mapTool.startRouteSearch(
                        mStartPoint = LatLonPoint(30.507964, 114.413512), mEndPoint = mEndPoint
                    )
                    RouteSearchScreen(mapView = mapView) { currentScreen = State.Map }
                }
            }
        }
    }

    private fun handlePermission() {
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
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

    sealed interface State {
        data object Map : State
        data object Search : State
        data object Route : State
    }


    override fun returnPoi(poiItems: ArrayList<PoiItemV2>) {
        poiItemV2s = poiItems
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


//    private fun changeMarkerIcon(marker: Marker) {
//        marker.setIcon()
//    }
}