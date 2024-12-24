package com.example.hust_map.ultis

import android.content.Context
import android.widget.Toast
import com.amap.api.maps.AMap.MAP_TYPE_NORMAL
import com.amap.api.maps.AMap.MAP_TYPE_SATELLITE
import com.amap.api.maps.MapView
import java.text.DecimalFormat


object MapUtil {
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

    fun MapView.changeMapType() {
        if (this.map.mapType == MAP_TYPE_SATELLITE) this.map.mapType = MAP_TYPE_NORMAL
        else this.map.mapType = MAP_TYPE_SATELLITE
    }

    fun showMsg(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}

