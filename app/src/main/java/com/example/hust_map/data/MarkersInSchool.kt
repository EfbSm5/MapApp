package com.example.hust_map.data

import android.content.Context
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions


object MarkersInSchool {
    private var points: ArrayList<Marker> = ArrayList()
    private val MarkersInSchool = ArrayList<Markers>(
        listOf(
            Markers("南大门", LatLng(30.507950, 114.413514)),
            Markers("西十二教学楼", LatLng(30.508906, 114.407151)),
            Markers("东九教学楼", LatLng(30.513447, 114.426866)),
            Markers("东十二教学楼", LatLng(30.512079, 114.433870)),
            Markers("工程训练中心", LatLng(30.513482, 114.437642)),
            Markers("主校区图书馆", LatLng(30.512467, 114.411494)),
            Markers("东校区图书馆", LatLng(30.510511, 114.432504)),
            Markers("沁苑学生公寓", LatLng(30.509529, 114.421445)),
            Markers("韵苑学生公寓", LatLng(30.514932, 114.433405)),
            Markers("紫菘学生公寓", LatLng(30.511534, 114.402793)),
            Markers("百景园餐厅", LatLng(30.516212, 114.407680)),
            Markers("光谷体育馆", LatLng(30.508022, 114.418187)),
            Markers("梧桐语问学中心", LatLng(30.514391, 114.415781)),
            Markers("校史陈列馆", LatLng(30.511914, 114.413735)),
            Markers("毛泽东像", LatLng(30.509097, 114.413469)),
            Markers("醉晚亭", LatLng(30.510621, 114.417479)),
            Markers("青年园", LatLng(30.512675, 114.409023)),
            Markers("化成天下人文科学纪念墙", LatLng(30.510814, 114.413577)),
            Markers("启明学院", LatLng(30.508988, 114.430793)),
            Markers("先进制造大楼", LatLng(30.513292, 114.417746)),
            Markers("新光电大楼", LatLng(30.507581, 114.434076)),
            Markers("国家脉冲强磁场科学中心", LatLng(30.509282, 114.433829)),
            Markers("精密重力测量科学中心大楼", LatLng(30.518150, 114.416543)),
            Markers("集贸市场", LatLng(30.516531, 114.414179)),
            Markers("校医院", LatLng(30.517255, 114.414325))
        )
    )

    fun initPoints(mapView: MapView, context: Context) {
        Thread {
            val list = getMarkerData(context)
            points.clear()
            for (i in 0..<MarkersInSchool.size) {
                points.add(
                    mapView.map.addMarker(
                        MarkerOptions().position(MarkersInSchool[i].latLng)
                            .title(MarkersInSchool[i].name).icon(
                                if (list[i] == 0) {
                                    BitmapDescriptorFactory.defaultMarker(1f)
                                } else {
                                    BitmapDescriptorFactory.defaultMarker(19f)
                                }
                            )
                    )
                )

            }
        }.start()
    }

    fun getPoints(): ArrayList<Marker> {
        return points
    }

    private fun getMarkerData(context: Context): List<Int> {
        val sharedPreferences = context.getSharedPreferences("markers", Context.MODE_PRIVATE)
        val listString = sharedPreferences.getString("int_list_key", null)
        return listString?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    private fun updateMarkerData(context: Context, location: Int) {
        val sharedPreferences = context.getSharedPreferences("markers", Context.MODE_PRIVATE)
        val originalListString = sharedPreferences.getString("int_list_key", null)
        if (originalListString != null) {
            val originalIntList = originalListString.split(",").map { it.toInt() }.toMutableList()
            originalIntList[location] = 1
            val updatedListString = originalIntList.joinToString(separator = ",")
            with(sharedPreferences.edit()) {
                putString("int_list_key", updatedListString)
                commit()
            }
        }
    }

    fun initMarkerData(context: Context, reset: Boolean) {
        val sharedPreferences = context.getSharedPreferences("markers", Context.MODE_PRIVATE)
        val hasKey = sharedPreferences.contains("int_list_key")
        if (!hasKey || reset) {
            val intList = List(25) { 0 }
            val listString = intList.joinToString(separator = ",")
            with(sharedPreferences.edit()) {
                putString("int_list_key", listString)
                commit()
            }
        }
    }

    fun updateAndChangeMarkerIcon(marker: Marker, context: Context) {
        val number = getPoints().indexOf(marker)
        updateMarkerData(context = context, number)
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(19f))
    }

    fun searchFromMarkers(keyword: String): Markers? {
        return MarkersInSchool.firstOrNull { it.name.contains(keyword) }
    }
}


