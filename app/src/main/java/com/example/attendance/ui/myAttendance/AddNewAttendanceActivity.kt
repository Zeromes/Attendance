package com.example.attendance.ui.myAttendance

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.utils.HttpsUtils.post
import java.util.*
import kotlin.math.abs


class AddNewAttendanceActivity : AppCompatActivity() {

    private var mMapView : TextureMapView? = null

    var mLocationClient: LocationClient? = null

    private var selectedCycle : String = "一次性"
    private var selectedYear : String? = null
    private var selectedMonth : String? = null
    private var selectedDay : String? = null
    private var selectedStartHour : String? = null
    private var selectedStartMinute : String? = null
    private var selectedEndHour : String? = null
    private var selectedEndMinute : String? = null

    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private var selectedRange : Int = 50

    private var selfLatitude : Double? = null
    private var selfLongitude : Double? = null

    private var isBadTime = false

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty()&&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED&&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    renderUI()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this, "权限被拒绝！", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun renderUI(){
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        /*SDKInitializer.setAgreePrivacy(applicationContext, true)
        SDKInitializer.initialize(applicationContext)*/

        setContentView(R.layout.activity_add_new_attendance)
        val options = BaiduMapOptions()
            .compassEnabled(true)
            .rotateGesturesEnabled(false)
            .scrollGesturesEnabled(false)
            .overlookingGesturesEnabled(false)
            .zoomGesturesEnabled(false)
        val mapView = TextureMapView(this, options)
        mapView.map.uiSettings.setAllGesturesEnabled(false)
        val layout : FrameLayout = findViewById(R.id.mapLayout)
        layout.addView(mapView)
        mMapView = mapView
        mLocationClient!!.start()

        findViewById<Spinner>(R.id.cycleSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCycle = parent!!.getItemAtPosition(position).toString()
                when(selectedCycle){
                    //做相应的界面显示处理
                    "一次性"->{
                        findViewById<LinearLayout>(R.id.datePickGroup).visibility = VISIBLE
                        findViewById<LinearLayout>(R.id.weekdayPickGroup).visibility = GONE
                    }
                    "每天"->{
                        findViewById<LinearLayout>(R.id.datePickGroup).visibility = GONE
                        findViewById<LinearLayout>(R.id.weekdayPickGroup).visibility = GONE
                    }
                    "每星期"->{
                        findViewById<LinearLayout>(R.id.datePickGroup).visibility = GONE
                        findViewById<LinearLayout>(R.id.weekdayPickGroup).visibility = VISIBLE
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //检查权限
        if(
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        else{
            LocationClient.setAgreePrivacy(true)
            //声明LocationClient类
            mLocationClient = LocationClient(applicationContext)
            //注册监听函数
            mLocationClient!!.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {
                    //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                    //以下只列举部分获取经纬度相关（常用）的结果信息
                    //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
                    val latitude = location!!.latitude //获取纬度信息
                    val longitude = location.longitude //获取经度信息

                    //定义Maker坐标点
                    val point = LatLng(latitude, longitude)
                    //构建Marker图标
                    val bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_pointer)
                    //构建MarkerOption，用于在地图上添加Marker
                    val option: OverlayOptions = MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                    //在地图上添加Marker，并显示
                    mMapView!!.map.addOverlay(option)

                    val cenPt = LatLng(latitude,longitude)  //设定中心点坐标

                    val mMapStatus : MapStatus = MapStatus.Builder()//定义地图状态
                        .target(cenPt)
                        .zoom(18F)
                        .build()  //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                    val mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus)
                    mMapView!!.map.setMapStatus(mMapStatusUpdate)//改变地图状态
                    selectedLatitude = latitude
                    selectedLongitude = longitude
                    selfLatitude = latitude
                    selfLongitude = longitude
                    mLocationClient!!.stop()
                    findViewById<SeekBar>(R.id.rangeSeekBar).setProgress(50,true)
                    selectedRange = 50
                    findViewById<TextView>(R.id.rangeText).text = "$selectedRange 米"
                    resetMapOverlay()

                    //获取到位置之后再注册seekbar的监听
                    findViewById<SeekBar>(R.id.rangeSeekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            selectedRange = progress
                            findViewById<TextView>(R.id.rangeText).text = "$selectedRange 米"
                            //在地图上显示圆
                            resetMapOverlay()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }

                    })
                }
            })
            mLocationClient!!.locOption = LocationClientOption().apply {
                setCoorType("bd09ll")
                setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC)
                setOnceLocation(true)
                isOpenGps = true
                isLocationNotify = true
            }
            renderUI()
        }
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

    private fun checkTime(){
        if(selectedStartHour!=null&&selectedStartMinute!=null&&selectedEndHour!=null&&selectedEndMinute!=null){
            when {
                selectedEndHour!!.toInt()< selectedStartHour!!.toInt() -> {
                    //跨天
                    findViewById<TextView>(R.id.warningNote).run {
                        setText(R.string.cross_day_warning_note)
                        visibility = VISIBLE
                    }
                    isBadTime = false
                }
                selectedStartHour!!.toInt() == selectedEndHour!!.toInt() -> {//小时相等的特殊情况
                    when {
                        selectedEndMinute!!.toInt() < selectedStartMinute!!.toInt() -> {
                            //跨天
                            findViewById<TextView>(R.id.warningNote).run {
                                setText(R.string.cross_day_warning_note)
                                visibility = VISIBLE
                            }
                            isBadTime = false
                        }
                        else -> {
                            //未跨天
                            if(selectedEndMinute!!.toInt() - selectedStartMinute!!.toInt() < 10){
                                //间隔时间小于十分钟
                                findViewById<TextView>(R.id.warningNote).run {
                                    setText(R.string.interval_too_short_warning_note)
                                    visibility = VISIBLE
                                }
                                isBadTime = true
                            }
                            else{
                                //正常
                                findViewById<TextView>(R.id.warningNote).run {
                                    visibility = GONE
                                }
                                isBadTime = false
                            }
                        }
                    }
                }
                else -> {
                    //未跨天
                    if((selectedEndHour!!.toInt() - selectedStartHour!!.toInt() == 1)&&(selectedEndMinute!!.toInt() + 60 - selectedStartMinute!!.toInt() < 10)){
                        //间隔时间小于十分钟
                        findViewById<TextView>(R.id.warningNote).run {
                            setText(R.string.interval_too_short_warning_note)
                            visibility = VISIBLE
                        }
                        isBadTime = true
                    }
                    else{
                        //正常
                        findViewById<TextView>(R.id.warningNote).run {
                            visibility = GONE
                        }
                        isBadTime = false
                    }
                }
            }
        }
    }

    fun onClickDatePicker(view: View){
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { datePicker, year, month, dayOfMonth ->
            //Toast.makeText(this,"$year 年 $month 月 $dayOfMonth 日",Toast.LENGTH_SHORT).show()
            selectedYear = year.toString()
            selectedMonth = (month+1).toString()
            selectedDay = dayOfMonth.toString()
            val displayMonth = if(month+1<10) "0$selectedMonth" else selectedMonth
            val displayDay = if(dayOfMonth<10) "0$selectedDay" else selectedDay
            findViewById<TextView>(R.id.dateTextView).text = "${selectedYear}年${displayMonth}月${displayDay}日"
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun onClickStartTimePicker(view: View){
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { timePicker, hourOfDay, minute ->
            //Toast.makeText(this,"$hourOfDay 时 $minute 分",Toast.LENGTH_SHORT).show()
            selectedStartHour = hourOfDay.toString()
            selectedStartMinute = minute.toString()
            val displayHour = if(hourOfDay<10) "0$selectedStartHour" else selectedStartHour
            val displayMinute = if(minute<10) "0$selectedStartMinute" else selectedStartMinute
            findViewById<TextView>(R.id.startTimeTextView).text = "${displayHour}:${displayMinute}"
            checkTime()
        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show()
    }

    fun onClickEndTimePicker(view: View){
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { timePicker, hourOfDay, minute ->
            //Toast.makeText(this,"$hourOfDay 时 $minute 分",Toast.LENGTH_SHORT).show()
            selectedEndHour = hourOfDay.toString()
            selectedEndMinute = minute.toString()
            val displayHour = if(hourOfDay<10) "0$selectedEndHour" else selectedEndHour
            val displayMinute = if(minute<10) "0$selectedEndMinute" else selectedEndMinute
            findViewById<TextView>(R.id.endTimeTextView).text = "${displayHour}:${displayMinute}"
            checkTime()
        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show()
    }

    fun onClickSubmit(view: View){
        Log.i("提交数据","纬度：$selectedLatitude 经度:$selectedLongitude")
        //判空
        if(findViewById<EditText>(R.id.editTextAttendanceEventName).text.toString().isBlank()){
            Toast.makeText(this,"请填写事件名称！",Toast.LENGTH_LONG).show()
            return
        }
        if(selectedCycle=="一次性"&&selectedYear==null){
            Toast.makeText(this,"请选择日期！",Toast.LENGTH_LONG).show()
            return
        }
        if(selectedStartHour==null){
            Toast.makeText(this,"请选择开始时间！",Toast.LENGTH_LONG).show()
            return
        }
        if(selectedEndHour==null){
            Toast.makeText(this,"请选择结束时间！",Toast.LENGTH_LONG).show()
            return
        }
        //badTime处理
        if(isBadTime){
            Toast.makeText(this,"要求时间间隔大于十分钟！",Toast.LENGTH_LONG).show()
            return
        }
        if(selectedLatitude == null){
            Toast.makeText(this,"请等待首次定位",Toast.LENGTH_SHORT).show()
            return
        }

        //处理空数据
        var submitYear : String? = selectedYear
        var submitMonth : String? = selectedMonth
        var submitDay : String? = selectedDay
        if(selectedYear==null||selectedCycle!="一次性"){
            submitYear = "null"
            submitMonth = "null"
            submitDay = "null"
        }


        post(HashMap<String,String>().apply {
            put("name",findViewById<EditText>(R.id.editTextAttendanceEventName).text.toString())
            put("creatorEmail",usingUserEmail!!)
            put("cycle",selectedCycle)
            put("year",submitYear!!)
            put("month",submitMonth!!)
            put("day",submitDay!!)
            put("weekday1",findViewById<CheckBox>(R.id.weekdayCheckBox1).isChecked.toString())
            put("weekday2",findViewById<CheckBox>(R.id.weekdayCheckBox2).isChecked.toString())
            put("weekday3",findViewById<CheckBox>(R.id.weekdayCheckBox3).isChecked.toString())
            put("weekday4",findViewById<CheckBox>(R.id.weekdayCheckBox4).isChecked.toString())
            put("weekday5",findViewById<CheckBox>(R.id.weekdayCheckBox5).isChecked.toString())
            put("weekday6",findViewById<CheckBox>(R.id.weekdayCheckBox6).isChecked.toString())
            put("weekday7",findViewById<CheckBox>(R.id.weekdayCheckBox7).isChecked.toString())
            put("startHour",selectedStartHour!!)
            put("startMinute",selectedStartMinute!!)
            put("endHour",selectedEndHour!!)
            put("endMinute",selectedEndMinute!!)
            put("latitude", selectedLatitude!!.toString())
            put("longitude", selectedLongitude!!.toString())
            put("locationRange", selectedRange.toString())
        },"addNewEvent",this,"正在提交",{
            //Log.i("创建考勤事件","返回了id：$it")
            //返回给上一个activity，调用另一个activitiy用来显示该考勤事件的详细信息
            val intent = Intent().apply {
                putExtra("id",it)
            }
            setResult(1,intent)
            finish()
        },{

        })
    }

    fun onClickSelectPosition(view: View){
        if(selectedLatitude == null){
            Toast.makeText(this,"请等待首次定位",Toast.LENGTH_SHORT).show()
        }
        else{
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("latitude",selectedLatitude)
                putExtra("longitude",selectedLongitude)
            }
            startActivityForResult(intent,1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 1){//获取了定位
            selectedLatitude = data!!.getDoubleExtra("latitude",0.0)
            selectedLongitude = data.getDoubleExtra("longitude",0.0)
            //设置地图显示
            resetMapOverlay()
        }
    }

    fun resetMapOverlay(){
        mMapView!!.map.run {
            clear()
            //重新显示圆
            //在地图上显示圆
            addOverlay(CircleOptions().center(LatLng(selectedLatitude!!, selectedLongitude!!))
                .radius(selectedRange)
                .fillColor(getColor(R.color.translucent_range_blue)) //填充颜色
                //.stroke(Stroke(5, -0x55ff0100)) //边框宽和边框颜色
            )
            //显示mark标记
            addOverlay(MarkerOptions()
                .position(LatLng(selectedLatitude!!,selectedLongitude!!))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer))
            )
            setMapStatus(MapStatusUpdateFactory.newMapStatus(MapStatus.Builder()//定义地图状态
                .target(LatLng(selectedLatitude!!,selectedLongitude!!))
                .zoom(18F)
                .build()
            ))
        }

    }
}