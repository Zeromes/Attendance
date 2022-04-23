package com.example.attendance.ui.myAttendance

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.example.attendance.R
import com.example.attendance.data.UsingUserData
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
                var displayHour = if(data!!.startHour.toInt()<10) "0${data!!.startHour}" else data!!.startHour
                var displayMinute = if(data!!.startMinute.toInt()<10) "0${data!!.startMinute}" else data!!.startMinute
                findViewById<TextView>(R.id.detailStartTimeTextView).text = "${displayHour}:${displayMinute}"
                displayHour = if(data!!.endHour.toInt()<10) "0${data!!.endHour}" else data!!.endHour
                displayMinute = if(data!!.endMinute.toInt()<10) "0${data!!.endMinute}" else data!!.endMinute
                findViewById<TextView>(R.id.detailEndTimeTextView).text = "${displayHour}:${displayMinute}"

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
        val intent = Intent(this, AddNewAttendanceActivity::class.java).apply {
            putExtra("id",this@EventDetailActivity.data!!.id.toString())
            putExtra("name",this@EventDetailActivity.data!!.name)
            putExtra("cycle",this@EventDetailActivity.data!!.cycle)
            putExtra("year",this@EventDetailActivity.data?.year)
            putExtra("month",this@EventDetailActivity.data?.month)
            putExtra("day",this@EventDetailActivity.data?.day)
            putExtra("weekday1",this@EventDetailActivity.data?.weekday1)
            putExtra("weekday2",this@EventDetailActivity.data?.weekday2)
            putExtra("weekday3",this@EventDetailActivity.data?.weekday3)
            putExtra("weekday4",this@EventDetailActivity.data?.weekday4)
            putExtra("weekday5",this@EventDetailActivity.data?.weekday5)
            putExtra("weekday6",this@EventDetailActivity.data?.weekday6)
            putExtra("weekday7",this@EventDetailActivity.data?.weekday7)
            putExtra("startHour",this@EventDetailActivity.data!!.startHour)
            putExtra("startMinute",this@EventDetailActivity.data!!.startMinute)
            putExtra("endHour",this@EventDetailActivity.data!!.endHour)
            putExtra("endMinute",this@EventDetailActivity.data!!.endMinute)
            putExtra("latitude", this@EventDetailActivity.data!!.latitude)
            putExtra("longitude", this@EventDetailActivity.data!!.longitude)
            putExtra("locationRange", this@EventDetailActivity.data!!.locationRange)
        }
        startActivityForResult(intent,1)
    }

    fun onClickGetEventStatistics(view: View){
        val intent = Intent(this, EventStatisticsActivity::class.java).apply {
            putExtra("id",this@EventDetailActivity.data!!.id.toString())
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 2){//做出了修改
            //更新界面
            post(HashMap<String,String>().apply {
                put("id",this@EventDetailActivity.data!!.id.toString())
            },"getEventDetail",this,"正在刷新",{
                this.data = Gson().fromJson(it, EventDetailData::class.java)
                runOnUiThread {
                    //渲染其他
                    findViewById<TextView>(R.id.detailIDTextView).text = this.data!!.id.toString()
                    findViewById<TextView>(R.id.detailNameTextView).text = this.data!!.name
                    findViewById<TextView>(R.id.detailCreatorTextView).text = this.data!!.creatorName
                    findViewById<TextView>(R.id.detailCycleTextView).text = this.data!!.cycle
                    //日期和重复星期
                    when(this.data!!.cycle){
                        "一次性"->{
                            findViewById<LinearLayout>(R.id.detailDateGroup).visibility = View.VISIBLE
                            findViewById<LinearLayout>(R.id.detailRepetitionGroup).visibility = View.GONE
                            //初始化日期
                            val displayMonth = if(this.data!!.month!!.toInt()<10) "0${this.data!!.month}" else this.data!!.month
                            val displayDay = if(this.data!!.day!!.toInt()<10) "0${this.data!!.day}" else this.data!!.day
                            findViewById<TextView>(R.id.detailDateTextView).text = "${this.data!!.year}年${displayMonth}月${displayDay}日"
                        }
                        "每天"->{
                            findViewById<LinearLayout>(R.id.detailDateGroup).visibility = View.GONE
                            findViewById<LinearLayout>(R.id.detailRepetitionGroup).visibility = View.GONE
                            //不初始化数据
                        }
                        "每星期"->{
                            findViewById<LinearLayout>(R.id.detailDateGroup).visibility = View.GONE
                            findViewById<LinearLayout>(R.id.detailRepetitionGroup).visibility = View.VISIBLE
                            //只初始化重复星期
                            var displayRepetition = ""
                            if(this.data!!.weekday1 == "true"){
                                displayRepetition += "，星期一"
                            }
                            if(this.data!!.weekday2 == "true"){
                                displayRepetition += "，星期二"
                            }
                            if(this.data!!.weekday3 == "true"){
                                displayRepetition += "，星期三"
                            }
                            if(this.data!!.weekday4 == "true"){
                                displayRepetition += "，星期四"
                            }
                            if(this.data!!.weekday5 == "true"){
                                displayRepetition += "，星期五"
                            }
                            if(this.data!!.weekday6 == "true"){
                                displayRepetition += "，星期六"
                            }
                            if(this.data!!.weekday7 == "true"){
                                displayRepetition += "，星期七"
                            }
                            displayRepetition = displayRepetition.substring(1)
                            findViewById<TextView>(R.id.detailRepetitionTextView).text = displayRepetition
                        }
                    }
                    //开始和结束时间
                    var displayHour = if(this.data!!.startHour.toInt()<10) "0${this.data!!.startHour}" else this.data!!.startHour
                    var displayMinute = if(this.data!!.startMinute.toInt()<10) "0${this.data!!.startMinute}" else this.data!!.startMinute
                    findViewById<TextView>(R.id.detailStartTimeTextView).text = "${displayHour}:${displayMinute}"
                    displayHour = if(this.data!!.endHour.toInt()<10) "0${this.data!!.endHour}" else this.data!!.endHour
                    displayMinute = if(this.data!!.endMinute.toInt()<10) "0${this.data!!.endMinute}" else this.data!!.endMinute
                    findViewById<TextView>(R.id.detailEndTimeTextView).text = "${displayHour}:${displayMinute}"

                    //渲染地图
                    mMapView!!.map.run {
                        clear()
                        //重新显示圆
                        //在地图上显示圆
                        addOverlay(
                            CircleOptions().center(LatLng(this@EventDetailActivity.data!!.latitude.toDouble(), this@EventDetailActivity.data!!.longitude.toDouble()))
                                .radius(this@EventDetailActivity.data!!.locationRange.toInt())
                                .fillColor(getColor(R.color.translucent_range_blue)) //填充颜色
                            //.stroke(Stroke(5, -0x55ff0100)) //边框宽和边框颜色
                        )
                        //显示mark标记
                        addOverlay(
                            MarkerOptions()
                                .position(LatLng(this@EventDetailActivity.data!!.latitude.toDouble(),this@EventDetailActivity.data!!.longitude.toDouble()))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer))
                        )
                        setMapStatus(
                            MapStatusUpdateFactory.newMapStatus(
                                MapStatus.Builder()//定义地图状态
                                    .target(LatLng(this@EventDetailActivity.data!!.latitude.toDouble(),this@EventDetailActivity.data!!.longitude.toDouble()))
                                    .zoom(18F)
                                    .build()
                            ))
                    }
                }
            },{

            })
        }
    }


}