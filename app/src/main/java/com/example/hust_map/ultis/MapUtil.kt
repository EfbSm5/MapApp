package com.example.hust_map.ultis

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.example.hust_map.R
import java.text.DecimalFormat


object MapUtil {
    fun convertToLatLonPoint(latLng: LatLng): LatLonPoint {
        return LatLonPoint(latLng.latitude, latLng.longitude)
    }

    fun convertToLatLng(latLonPoint: LatLonPoint): LatLng {
        return LatLng(latLonPoint.latitude, latLonPoint.longitude)
    }

    fun getFriendlyTime(second: Int): String {
        if (second > 3600) {
            val hour = second / 3600
            val miniate = (second % 3600) / 60
            return hour.toString() + "小时" + miniate + "分钟"
        }
        if (second >= 60) {
            val miniate = second / 60
            return miniate.toString() + "分钟"
        }
        return second.toString() + "秒"
    }

    fun getFriendlyLength(lenMeter: Int): String {
        if (lenMeter > 10000) // 10 km
        {
            val dis = lenMeter / 1000
            return dis.toString() + ChString.Kilometer
        }

        if (lenMeter > 1000) {
            val dis = lenMeter.toFloat() / 1000
            val fnum = DecimalFormat("##0.0")
            val dstr = fnum.format(dis.toDouble())
            return dstr + ChString.Kilometer
        }

        if (lenMeter > 100) {
            val dis = lenMeter / 50 * 50
            return dis.toString() + ChString.Meter
        }

        var dis = lenMeter / 10 * 10
        if (dis == 0) {
            dis = 10
        }

        return dis.toString() + ChString.Meter
    }


}

