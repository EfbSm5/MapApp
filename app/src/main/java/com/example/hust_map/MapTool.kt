package com.example.hust_map

import android.content.Context
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
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
import com.example.hust_map.data.MarkersInSchool
import com.example.hust_map.overlay.AMapServicesUtil.convertToLatLonPoint
import com.example.hust_map.overlay.WalkRouteOverlay
import com.example.hust_map.ultis.MapUtil.convertToLatLng

class MapTool(
    private val context: Context,
    private val mapView: MapView,
    private val callBack: MapToolCallBack
) : PoiSearchV2.OnPoiSearchListener, RouteSearch.OnRouteSearchListener {
    private var routeSearch: RouteSearch? = null

    fun searchForPoi(keyword: String) {
        val thread = Thread {
            val query: PoiSearchV2.Query = PoiSearchV2.Query(keyword, "", "027")
            query.pageSize = 5
            query.pageNum = 1
            try {
                val poiSearch = PoiSearchV2(context, query)
                poiSearch.setOnPoiSearchListener(this)
                poiSearch.searchPOIAsyn()
            } catch (e: AMapException) {
                throw RuntimeException(e)
            }
        }
        thread.start()
    }

    override fun onPoiSearched(p0: PoiResultV2?, p1: Int) {
        callBack.returnPoi(p0!!.pois)
    }

    override fun onPoiItemSearched(p0: PoiItemV2?, p1: Int) {
    }

    fun startRouteSearch(
        mStartPoint: LatLonPoint, mEndPoint: LatLonPoint
    ) {
        Thread {
            try {
                routeSearch = RouteSearch(context)
                routeSearch!!.setRouteSearchListener(this)
                mapView.map.addMarker(
                    MarkerOptions().position(convertToLatLng(mStartPoint))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
                )
                mapView.map.addMarker(
                    MarkerOptions().position(convertToLatLng(mEndPoint))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
                )
                val query = RouteSearch.WalkRouteQuery(
                    RouteSearch.FromAndTo(mStartPoint, mEndPoint), RouteSearch.WalkDefault
                )
                routeSearch!!.calculateWalkRouteAsyn(query)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }.start()
    }

    override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {}

    override fun onDriveRouteSearched(p0: DriveRouteResult?, p1: Int) {}

    override fun onWalkRouteSearched(walkRouteResult: WalkRouteResult?, p1: Int) {
        mapView.map.clear() // 清理地图上的所有覆盖物
        if (p1 != AMapException.CODE_AMAP_SUCCESS) {
            callBack.returnMsg("出错了")
            return
        }
        if (walkRouteResult?.paths == null) {
            callBack.returnMsg("没有搜索到相关数据")
            return
        }
        if (walkRouteResult.paths.isEmpty()) {
            callBack.returnMsg("没有搜索到相关数据")
            return
        }
        val walkPath = walkRouteResult.paths[0] ?: return
        val walkRouteOverlay = WalkRouteOverlay(
            context, mapView.map, walkPath, walkRouteResult.startPos, walkRouteResult.targetPos
        )
        walkRouteOverlay.removeFromMap()
        walkRouteOverlay.addToMap()
        walkRouteOverlay.zoomToSpan()
    }

    override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {}

    fun onSelected(poiItemV2: PoiItemV2) {
        val latLonPoint = poiItemV2.latLonPoint
        val point = LatLng(latLonPoint.latitude, latLonPoint.longitude)
        mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18f))
        callBack.returnPoint(convertToLatLonPoint(point))
        callBack.returnMsg("你选择了${poiItemV2.title}")
    }

    fun initPoints() {
        Thread {
            for (poi in MarkersInSchool.MarkersInSchool) {
                mapView.map.addMarker(
                    MarkerOptions().position(poi.latLng).title(poi.name)
                )
            }
        }.start()
    }
}

interface MapToolCallBack {
    fun returnPoi(poiItems: ArrayList<PoiItemV2>)
    fun returnMsg(word: String)
    fun returnPoint(point: LatLonPoint)
}