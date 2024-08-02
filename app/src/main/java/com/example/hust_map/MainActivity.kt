package com.example.hust_map

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentCallbacks
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Poi
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import com.amap.api.services.route.BusRouteResult
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RideRouteResult
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.WalkRouteResult
import com.example.hust_map.data.PointData
import com.example.hust_map.overlay.AMapServicesUtil.convertToLatLonPoint
import com.example.hust_map.overlay.WalkRouteOverlay
import com.example.hust_map.ui.theme.Hust_mapTheme
import com.example.hust_map.ultis.MapUtil
import com.example.hust_map.ultis.MapUtil.convertToLatLng

@SuppressLint("MutableCollectionMutableState")
class MainActivity : ComponentActivity(), AMapLocationListener, LocationSource,
    AMap.OnMapClickListener, AMap.OnPOIClickListener, PoiSearchV2.OnPoiSearchListener,
    RouteSearch.OnRouteSearchListener {
    private val TAG = "MainActivity"
    private var requestPermission: ActivityResultLauncher<String>? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var aMap: AMap? = null
    private lateinit var mapView: MapView
    private var mListener: OnLocationChangedListener? = null
    private val cityCode = "027"
    private var mEndPoint by mutableStateOf(LatLonPoint(0.0, 0.0))
    private var mStartPoint: LatLonPoint? = null
    private var poiItemV2s by mutableStateOf(ArrayList<PoiItemV2>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatePrivacy()
        handlePermission()
        initLocationOption()
        initLocationClient()
        enableEdgeToEdge()
        setContent {
            Hust_mapTheme {
                mapView = MapView(
                    LocalContext.current,
                    AMapOptions().camera(CameraPosition(LatLng(30.513197, 114.413301), 18f, 0f, 0f))
                )
                MapLifecycle(mapView = mapView)
                MapApp()
            }
        }
    }

    @Composable
    private fun MapApp() {
        var currentScreen: State by remember { mutableStateOf(State.Map) }
        Crossfade(targetState = currentScreen, label = "") { screen ->
            when (screen) {
                State.Map -> {
                    ShowMapScreen(mEndPoint = mEndPoint, mapView = mapView, toSearchScreen = {
                        currentScreen = State.Search
                    }, toRouteScreen = {
                        //currentScreen = State.Route
                        startRouteSearch()
                    })
                }

                State.Search -> {
                    ShowSearchScreen(list = poiItemV2s) {
                        currentScreen = State.Map
                    }
                }

                State.Route -> {
                    mapView.removeAllViews()
                    startRouteSearch()
                    RouteSearchScreen { currentScreen = State.Map }
                }
            }
        }
    }

    @Composable
    private fun ShowMapScreen(
        mEndPoint: LatLonPoint,
        mapView: MapView,
        toSearchScreen: () -> Unit,
        toRouteScreen: () -> Unit
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = "这是一个地图APP",
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 40.sp),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                AndroidView(modifier = Modifier.weight(3F), factory = { mapView })
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { toSearchScreen() }) {
                        Text(text = "搜索地址")
                    }
                    if (mEndPoint != LatLonPoint(0.0, 0.0)) {
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(onClick = { toRouteScreen() }) {
                            Text(text = "导航去选定地址")
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(onClick = { mapView.map.clear() }) {
                            Text(text = "清除")
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun ShowSearchScreen(
        list: ArrayList<PoiItemV2>, callback: () -> Unit
    ) {
        var keyword by remember { mutableStateOf("") }
        if (keyword.isNotEmpty()) {
            searchForPoi(keyword = keyword)
        }
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { Spacer(modifier = Modifier.height(50.dp)) }
                item {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { callback() })
                        TextField(value = keyword, onValueChange = { keyword = it })
                        Icon(Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    searchForPoi(keyword = keyword)
                                })
                    }
                }
                item { Spacer(modifier = Modifier.height(50.dp)) }
                if (list.isNotEmpty()) {
                    items(list) { poiItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    onSelected(poiItemV2 = poiItem)
                                    callback()
                                },
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = poiItem.title,
                                style = TextStyle(fontSize = 20.sp),
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        DividerDefaults
                    }
                }
            }
        }
    }

    @Composable
    private fun RouteSearchScreen(callback: () -> Unit) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Close,
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

    @Composable
    private fun MapLifecycle(mapView: MapView) {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(context, lifecycle, mapView) {
            val mapLifecycleObserver = mapView.lifecycleObserver()
            val callbacks = mapView.componentCallbacks()
            lifecycle.addObserver(mapLifecycleObserver)
            context.registerComponentCallbacks(callbacks)
            onDispose {
                lifecycle.removeObserver(mapLifecycleObserver)
                context.unregisterComponentCallbacks(callbacks)
            }
        }
    }

    private fun MapView.lifecycleObserver(): LifecycleEventObserver =
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    this.onCreate(Bundle())
                    initMap()
                }

                Lifecycle.Event.ON_START -> {}
                Lifecycle.Event.ON_RESUME -> {
                    this.onResume()
                } // 重新绘制加载地图
                Lifecycle.Event.ON_PAUSE -> this.onPause()  // 暂停地图的绘制
                Lifecycle.Event.ON_STOP -> {}
                Lifecycle.Event.ON_DESTROY -> this.onDestroy() // 销毁地图
                else -> {}
            }
        }

    private fun MapView.componentCallbacks(): ComponentCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(config: Configuration) {}
        override fun onLowMemory() {
            this@componentCallbacks.onLowMemory()
        }
    }


    private fun updatePrivacy() {
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }

    private fun handlePermission() {
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result: Boolean ->
            Log.d(TAG, "onCreate: $result")
            if (!result) {
                Toast.makeText(this, "请授予权限", Toast.LENGTH_LONG).show()
            }
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission!!.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initLocationOption() {
        if (mLocationOption == null) {
            mLocationOption = AMapLocationClientOption()
            mLocationOption!!.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
            mLocationOption!!.setOnceLocationLatest(true)
            mLocationOption!!.setNeedAddress(true)
            mLocationOption!!.setHttpTimeOut(6000)
        }
    }

    private fun initLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(applicationContext)
            mLocationClient!!.setLocationListener(this)
            mLocationClient!!.setLocationOption(mLocationOption)
            mLocationClient!!.startLocation()
        }
    }

    private fun initMap() {
        if (aMap == null) {
            aMap = mapView.map
            aMap!!.setLocationSource(this)
            aMap!!.isMyLocationEnabled = true
            aMap!!.uiSettings.isMyLocationButtonEnabled = true
            aMap!!.myLocationStyle = MyLocationStyle().interval(2000)
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(30.513197, 114.413301), 16f))
            aMap!!.setOnMapClickListener(this)
            aMap!!.setOnPOIClickListener(this)
        }
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation!!.errorCode == 0) {
            if (mListener != null) {
                mListener!!.onLocationChanged(aMapLocation)
            }
            val latitude = aMapLocation.latitude
            val longitude = aMapLocation.longitude
            mStartPoint = MapUtil.convertToLatLonPoint(LatLng(latitude, longitude))
        }
    }

    private fun searchForPoi(keyword: String) {
        val thread = Thread {
            val query: PoiSearchV2.Query = PoiSearchV2.Query(keyword, "", cityCode)
            query.pageSize = 5
            query.pageNum = 1
            try {
                val poiSearch = PoiSearchV2(this, query)
                poiSearch.setOnPoiSearchListener(this)
                poiSearch.searchPOIAsyn()
            } catch (e: AMapException) {
                throw RuntimeException(e)
            }
        }
        thread.start()
    }

    override fun activate(p0: OnLocationChangedListener?) {
        if (mListener == null) {
            mListener = p0
        }
        mLocationClient!!.startLocation()
    }

    override fun deactivate() {
        mListener = null
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
    }

    override fun onMapClick(p0: LatLng?) {
        mEndPoint = p0?.let { convertToLatLonPoint(it) }!!
    }

    override fun onPOIClick(p0: Poi?) {
        if (p0 != null) {
            mEndPoint = convertToLatLonPoint(p0.coordinate)
        }
        Toast.makeText(this, "你选择了${p0?.name}", Toast.LENGTH_LONG).show()
    }

    override fun onPoiSearched(p0: PoiResultV2?, p1: Int) {
        if (p0 == null) {
            Toast.makeText(this, "请重新输入", Toast.LENGTH_LONG).show()
        } else {
            poiItemV2s = p0.pois!!
        }
    }

    private fun onSelected(poiItemV2: PoiItemV2) {
        val latLonPoint = poiItemV2.latLonPoint
        val point = LatLng(latLonPoint.latitude, latLonPoint.longitude)
        mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18f))
        mEndPoint = convertToLatLonPoint(point)
        Toast.makeText(this, "你选择了${poiItemV2.title}", Toast.LENGTH_LONG).show()
    }

    override fun onPoiItemSearched(p0: PoiItemV2?, p1: Int) {
    }


    sealed interface State {
        data object Map : State
        data object Search : State
        data object Route : State
    }

    override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onDriveRouteSearched(p0: DriveRouteResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onWalkRouteSearched(walkRouteResult: WalkRouteResult?, p1: Int) {
        aMap!!.clear() // 清理地图上的所有覆盖物
        if (p1 != AMapException.CODE_AMAP_SUCCESS) {
            Toast.makeText(this, "错误", Toast.LENGTH_LONG).show()
            return
        }
        if (walkRouteResult?.paths == null) {
            Toast.makeText(this, "没有搜索到相关数据", Toast.LENGTH_LONG).show()
            return
        }
        if (walkRouteResult.paths.isEmpty()) {
            Toast.makeText(this, "没有搜索到相关数据", Toast.LENGTH_LONG).show()

            return
        }
        val walkPath = walkRouteResult.paths[0] ?: return
        val walkRouteOverlay = WalkRouteOverlay(
            this, aMap!!, walkPath, walkRouteResult.startPos, walkRouteResult.targetPos
        )
        walkRouteOverlay.removeFromMap()
        walkRouteOverlay.addToMap()
        walkRouteOverlay.zoomToSpan()
    }

    override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
        TODO("Not yet implemented")
    }

    private fun startRouteSearch() {
        Toast.makeText(this, "由于测试，出发点定为南大门", Toast.LENGTH_LONG).show()
        val thread = Thread {
            try {
                val routeSearch = RouteSearch(this)
                routeSearch.setRouteSearchListener(this)
                mStartPoint = LatLonPoint(30.507964, 114.413512)
                mapView.map.addMarker(
                    MarkerOptions().position(convertToLatLng(mStartPoint!!))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
                )
                mapView.map.addMarker(
                    MarkerOptions().position(convertToLatLng(mEndPoint))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
                )
                val query = RouteSearch.WalkRouteQuery(
                    RouteSearch.FromAndTo(mStartPoint, mEndPoint), RouteSearch.WalkDefault
                )
                routeSearch.calculateWalkRouteAsyn(query)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
        thread.start()
    }
}