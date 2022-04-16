package com.example.attendance.ui.myAttendance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.example.attendance.R

class MapActivity : AppCompatActivity() {
    private var mMapView : MapView? = null
    private var latitude : Double? = null
    private var longitude : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val options = BaiduMapOptions()
            .rotateGesturesEnabled(false)
            .overlookingGesturesEnabled(false)
        val mapView = MapView(this, options)
        val layout : FrameLayout = findViewById(R.id.locationSelectMapLayout)
        layout.addView(mapView)
        //获取地图控件引用
        mMapView = mapView

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude",0.0)
        //定义Maker坐标点
        val point = LatLng(latitude!!, longitude!!)
        //构建Marker图标
        val bitmap = BitmapDescriptorFactory
            .fromResource(R.drawable.ic_pointer)
        //构建MarkerOption，用于在地图上添加Marker
        val option: OverlayOptions = MarkerOptions()
            .position(point)
            .icon(bitmap)
        //在地图上添加Marker，并显示
        mMapView!!.map.addOverlay(option)

        val cenPt = LatLng(latitude!!,longitude!!);  //设定中心点坐标

        val mMapStatus : MapStatus = MapStatus.Builder()//定义地图状态
            .target(cenPt)
            .zoom(18F)
            .build();  //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        val mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mMapView!!.map.setMapStatus(mMapStatusUpdate);//改变地图状态

        //设置单击监听器
        mMapView!!.map.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(point: LatLng?) {
                /*mMapView!!.map.clear()
                mMapView!!.map.addOverlay(MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer))
                )
                latitude = point!!.latitude
                longitude = point.longitude*/
                Log.i("地图监听器","onMapClick")
            }

            override fun onMapPoiClick(mapPoi: MapPoi?) {

            }

        })
        mMapView!!.map.setOnMapStatusChangeListener(object : BaiduMap.OnMapStatusChangeListener{
            override fun onMapStatusChangeStart(status: MapStatus?) {
            }

            override fun onMapStatusChangeStart(status: MapStatus?, reason: Int) {
            }

            override fun onMapStatusChange(status: MapStatus?) {
            }

            override fun onMapStatusChangeFinish(status: MapStatus?) {
                mMapView!!.map.clear()
                mMapView!!.map.addOverlay(MarkerOptions()
                    .position(status!!.target)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer))
                )
                latitude = status.target.latitude
                longitude = status.target.longitude
            }

        })
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView?.onResume();
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onResume时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView?.onPause();
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onResume时执行mMapView. onDestroy ()，实现地图生命周期管理
        mMapView?.onDestroy();
        mMapView=null
    }

    fun onClickConfirm(view: View){
        val intent = Intent().apply {
            putExtra("latitude",latitude)
            putExtra("longitude",longitude)
        }
        setResult(1,intent)
        finish()
    }
}