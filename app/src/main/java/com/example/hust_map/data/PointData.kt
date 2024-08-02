package com.example.hust_map.data

import com.amap.api.services.core.LatLonPoint
import java.io.Serializable

object PointData : Serializable {
    private fun readResolve(): Any = PointData
    private lateinit var mStartPoint: LatLonPoint
    private lateinit var mEndPoint: LatLonPoint
    fun setData(mStartPoint: LatLonPoint, mEndPoint: LatLonPoint) {
        this.mStartPoint = mStartPoint
        this.mEndPoint = mEndPoint
    }

    fun getStartPoint(): LatLonPoint {
        return mStartPoint
    }

    fun getEndPoint(): LatLonPoint {
        return mEndPoint
    }
}