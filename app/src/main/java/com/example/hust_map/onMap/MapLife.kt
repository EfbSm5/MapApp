package com.example.hust_map.onMap

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Poi
import com.amap.api.maps.model.VisibleRegion
import com.amap.api.services.core.LatLonPoint
import com.example.hust_map.overlay.AMapServicesUtil.convertToLatLonPoint
import com.example.hust_map.ultis.MapUtil


class MapLife(context: Context, private val callBack: MapLifeCallBack) : LocationSource,
    AMap.OnMapClickListener, AMap.OnPOIClickListener, AMap.OnMarkerClickListener,
    AMapLocationListener {
    private var mLocationOption =
        AMapLocationClientOption().setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
            .setOnceLocationLatest(true).setNeedAddress(true).setHttpTimeOut(6000)
    private var mLocationClient: AMapLocationClient? = AMapLocationClient(context)
    private var mListener: OnLocationChangedListener? = null

    @Composable
    fun MapLifecycle(mapView: MapView) {
        mLocationClient!!.setLocationOption(mLocationOption)
        mLocationClient!!.setLocationListener(this)
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
                    initMap(this)
                }

                Lifecycle.Event.ON_RESUME -> {
                    this.onResume()
                }

                Lifecycle.Event.ON_PAUSE -> this.onPause()  // 暂停地图的绘制
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

    private fun initMap(mapView: MapView) {
        mapView.map.setLocationSource(this)
        mapView.map.mapType = AMap.MAP_TYPE_SATELLITE
        val visibleRegion = VisibleRegion(
            LatLng(30.506589, 114.405456),
            LatLng(30.505737, 114.434915),
            LatLng(30.519343, 114.403142),
            LatLng(30.517576, 114.440049),
            LatLngBounds(LatLng(30.505633, 114.401235), LatLng(30.519878, 114.441127))
        )
        mapView.map.isMyLocationEnabled = true
        mapView.map.uiSettings.isMyLocationButtonEnabled = true
        mapView.map.myLocationStyle = MyLocationStyle().interval(2000)
            .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        mapView.map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(30.513197, 114.413301), 16f
            )
        )
        mapView.map.setOnMapClickListener(this)
        mapView.map.setOnPOIClickListener(this)
        mapView.map.setOnMarkerClickListener(this)
        mapView.map.showMapText(false)
    }


    override fun activate(p0: OnLocationChangedListener?) {
        if (mListener == null) {
            mListener = p0
        }
        mLocationClient!!.startLocation()
    }

    override fun deactivate() {
        mListener = null
        mLocationClient!!.stopLocation()
        mLocationClient!!.onDestroy()
        mLocationClient = null
    }

    override fun onMapClick(p0: LatLng?) {
        callBack.returnEndLocation(convertToLatLonPoint(p0!!), "地图")
    }

    override fun onPOIClick(p0: Poi?) {
        callBack.returnEndLocation(convertToLatLonPoint(p0!!.coordinate), p0.name)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        callBack.returnEndLocation((convertToLatLonPoint(p0!!.position)), p0.title)
        return true
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation!!.errorCode == 0) {
            mListener!!.onLocationChanged(aMapLocation)
            val latitude = aMapLocation.latitude
            val longitude = aMapLocation.longitude
            val point = LatLng(latitude, longitude)
            callBack.returnNowLocation(MapUtil.convertToLatLonPoint(point))
//            if (isNearMarker(point, markerPosition)) {
//                // 如果接近Marker，改变其icon
//                changeMarkerIcon(marker);
//            }
        }
    }

}

interface MapLifeCallBack {
    fun returnEndLocation(point: LatLonPoint, name: String)
    fun returnNowLocation(point: LatLonPoint)
}