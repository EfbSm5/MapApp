package com.example.hust_map.onMap

import android.content.Context
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import com.amap.api.services.route.BusRouteResult
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RideRouteResult
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.WalkRouteResult
import com.example.hust_map.R
import com.example.hust_map.data.Markers
import com.example.hust_map.data.MarkersInSchool
import com.example.hust_map.onMap.overlay.AMapServicesUtil.convertToLatLng
import com.example.hust_map.onMap.overlay.AMapServicesUtil.convertToLatLonPoint
import com.example.hust_map.onMap.overlay.WalkRouteOverlay

class MapSearchUtil(
    private val context: Context,
    private val mapView: MapView,
    val onPoiSearched: (ArrayList<Markers>) -> Unit,
    val returnMsg: (String) -> Unit,
    val returnPoint: (point: LatLonPoint) -> Unit
) : PoiSearchV2.OnPoiSearchListener, RouteSearch.OnRouteSearchListener {
    private var routeSearch: RouteSearch? = null


    fun searchForPoi(keyword: String) {
        Thread {
            val result = MarkersInSchool.searchFromMarkers(keyword = keyword)
            if (result != null) {
                onPoiSearched(arrayListOf(result))
                return@Thread
            }
            ServiceSettings.updatePrivacyShow(context, true, true)
            ServiceSettings.updatePrivacyAgree(context, true)
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
        }.start()
    }

    override fun onPoiSearched(p0: PoiResultV2?, p1: Int) {
        val arrayList =
            p0!!.pois.map { poi -> Markers(poi.title, convertToLatLng(poi.latLonPoint)) }
        onPoiSearched(ArrayList(arrayList))
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
            returnMsg("出错了")
            return
        }
        if (walkRouteResult?.paths == null) {
            returnMsg("没有搜索到相关数据")
            return
        }
        if (walkRouteResult.paths.isEmpty()) {
            returnMsg("没有搜索到相关数据")
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

    fun onSelected(markers: Markers) {
        val point = markers.latLng
        mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18f))
        returnPoint(convertToLatLonPoint(point))
        returnMsg("你选择了${markers.name}")
    }
}

