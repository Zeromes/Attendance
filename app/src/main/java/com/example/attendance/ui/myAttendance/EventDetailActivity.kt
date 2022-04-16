package com.example.attendance.ui.myAttendance

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.example.attendance.R
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.dataClass.EventDetailData
import com.google.gson.Gson
import me.devilsen.czxing.code.BarcodeWriter
import me.devilsen.czxing.util.BarCodeUtil


class EventDetailActivity : AppCompatActivity() {
    private var data : EventDetailData? = null
    private var mMapView : TextureMapView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        val id = intent.getStringExtra("id")
        //Log.i("事件详情",id!!)
        post(HashMap<String,String>().apply {
            put("id",id!!)
        },"getEventDetail",this,"正在加载",{
            data = Gson().fromJson(it, EventDetailData::class.java)
            runOnUiThread {
                //渲染界面
                //渲染二维码
                val codeBitmap: Bitmap = BarcodeWriter().write(
                    "AttendanceSystemQRCode${data!!.id}",
                    BarCodeUtil.dp2px(this, 300f)
                )
                findViewById<ImageView>(R.id.QRCodeImageView).setImageBitmap(codeBitmap)
                //渲染其他
                findViewById<TextView>(R.id.detailIDTextView).text = data!!.id.toString()
                findViewById<TextView>(R.id.detailNameTextView).text = data!!.name
                findViewById<TextView>(R.id.detailCreatorTextView).text = data!!.creatorName
                findViewById<TextView>(R.id.detailCycleTextView).text = data!!.cycle
                //日期和重复星期
                when(data!!.cycle){
                    "一次性"->{
                        findViewById<LinearLayout>(R.id.detailRepetitionGroup).visibility = View.GONE
                        //只初始化日期
                        val displayMonth = if(data!!.month!!.toInt()<10) "0${data!!.month}" else data!!.month
                        val displayDay = if(data!!.day!!.toInt()<10) "0${data!!.day}" else data!!.day
                        findViewById<TextView>(R.id.detailDateTextView).text = "${data!!.year}年${displayMonth}月${displayDay}日"
                    }
                    "每天"->{
                        findViewById<LinearLayout>(R.id.detailDateGroup).visibility = View.GONE
                        findViewById<LinearLayout>(R.id.detailRepetitionGroup).visibility = View.GONE
                        //不初始化数据
                    }
                    "每星期"->{
                        findViewById<LinearLayout>(R.id.detailDateGroup).visibility = View.GONE
                        //只初始化重复星期
                        var displayRepetition = ""
                        if(data!!.weekday1 == "true"){
                            displayRepetition += "，星期一"
                        }
                        if(data!!.weekday2 == "true"){
                            displayRepetition += "，星期二"
                        }
                        if(data!!.weekday3 == "true"){
                            displayRepetition += "，星期三"
                        }
                        if(data!!.weekday4 == "true"){
                            displayRepetition += "，星期四"
                        }
                        if(data!!.weekday5 == "true"){
                            displayRepetition += "，星期五"
                        }
                        if(data!!.weekday6 == "true"){
                            displayRepetition += "，星期六"
                        }
                        if(data!!.weekday7 == "true"){
                            displayRepetition += "，星期七"
                        }
                        displayRepetition = displayRepetition.substring(1)
                        findViewById<TextView>(R.id.detailRepetitionTextView).text = displayRepetition
                    }
                }
                //开始和结束时间
                findViewById<TextView>(R.id.detailStartTimeTextView).text = "${data!!.startHour}:${data!!.startMinute}"
                findViewById<TextView>(R.id.detailEndTimeTextView).text = "${data!!.endHour}:${data!!.endMinute}"

                //渲染地图
                val options = BaiduMapOptions()
                    .compassEnabled(true)
                    .rotateGesturesEnabled(false)
                    .scrollGesturesEnabled(false)
                    .overlookingGesturesEnabled(false)
                    .zoomGesturesEnabled(false)
                val mapView = TextureMapView(this, options)
                mapView.map.uiSettings.setAllGesturesEnabled(false)
                val layout : FrameLayout = findViewById(R.id.detailMapLayout)
                mapView!!.map.run {
                    clear()
                    //重新显示圆
                    //在地图上显示圆
                    addOverlay(
                        CircleOptions().center(LatLng(data!!.latitude.toDouble(), data!!.longitude.toDouble()))
                        .radius(data!!.locationRange.toInt())
                        .fillColor(getColor(R.color.translucent_range_blue)) //填充颜色
                        //.stroke(Stroke(5, -0x55ff0100)) //边框宽和边框颜色
                    )
                    //显示mark标记
                    addOverlay(
                        MarkerOptions()
                        .position(LatLng(data!!.latitude.toDouble(),data!!.longitude.toDouble()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer))
                    )
                    setMapStatus(
                        MapStatusUpdateFactory.newMapStatus(
                            MapStatus.Builder()//定义地图状态
                        .target(LatLng(data!!.latitude.toDouble(),data!!.longitude.toDouble()))
                        .zoom(18F)
                        .build()
                    ))
                }
                layout.addView(mapView)
                mMapView = mapView
            }
        },{

        })
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onResume时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onResume时执行mMapView. onDestroy ()，实现地图生命周期管理
        mMapView?.onDestroy()
        mMapView=null
    }

    fun onClickEditEvent(view: View){

    }

    fun onClickArchiveEvent(view: View){

    }


}