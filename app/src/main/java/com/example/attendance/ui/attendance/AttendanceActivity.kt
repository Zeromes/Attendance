package com.example.attendance.ui.attendance

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.ui.profile.faceRecognize.FaceRecognitionActivity
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.dataClass.CheckResultData
import com.example.attendance.utils.dataClass.EventDetailData
import com.example.attendance.utils.dataClass.UserData
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap

class AttendanceActivity : AppCompatActivity() {
    private lateinit var loadingDialog : ProgressDialog
    private lateinit var userData : UserData
    private lateinit var eventData : EventDetailData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        //显示加载中
        loadingDialog = ProgressDialog.show(this, "", "处理中", true)
        //获取考勤事件的相关信息
        post(HashMap<String, String>().apply {
            put("id", intent.getStringExtra("id")!!)
        }, "getEventDetail", this, "",{ data ->
            eventData = Gson().fromJson(data, EventDetailData::class.java)
            post(HashMap<String,String>().apply {
                put("eventId", eventData.id.toString())
                put("participantEmail", usingUserEmail!!)
            },"checkCanParticipant",this,"",{
                val checkResult: CheckResultData = Gson().fromJson(it, CheckResultData::class.java)
                if(!checkResult.inTime){
                    //不在时间段内
                    runOnUiThread {
                        loadingDialog.dismiss()
                        val displayStartHour = if(eventData.startHour.toInt()<10) "0${eventData.startHour}" else eventData.startHour
                        val displayStartMinute = if(eventData.startMinute.toInt()<10) "0${eventData.startMinute}" else eventData.startMinute
                        val displayEndHour = if(eventData.endHour.toInt()<10) "0${eventData.endHour}" else eventData.endHour
                        val displayEndMinute = if(eventData.endMinute.toInt()<10) "0${eventData.endMinute}" else eventData.endMinute
                        AlertDialog.Builder(this@AttendanceActivity).setMessage("当前不在考勤时间内！考勤时间：" +
                                when(eventData.cycle){
                                    "一次性"-> "${eventData.year}年${if(eventData.month!!.toInt()<10) "0${eventData.month!!}" else eventData.month!!}月${if(eventData.day!!.toInt()<10) "0${eventData.day!!}" else eventData.day!!}日"
                                    "每天"-> ""
                                    "每星期"-> (if(eventData.weekday1=="true")"星期一、" else "") +
                                            (if(eventData.weekday2=="true")"星期二、" else "") +
                                            (if(eventData.weekday3=="true")"星期三、" else "") +
                                            (if(eventData.weekday4=="true")"星期四、" else "") +
                                            (if(eventData.weekday5=="true")"星期五、" else "") +
                                            (if(eventData.weekday6=="true")"星期六、" else "") +
                                            (if(eventData.weekday7=="true")"星期日" else "")
                                    else ->  ""
                                } +
                                "${displayStartHour}:${displayStartMinute} - ${displayEndHour}:${displayEndMinute}"
                        ).setNeutralButton("确定"){ dialog, which ->
                            dialog.dismiss()
                            finish()
                        }.show()
                    }
                }
                else{
                    if(!checkResult.notYet){
                        runOnUiThread{
                            loadingDialog.dismiss()
                            AlertDialog.Builder(this@AttendanceActivity).setMessage("你已经考勤过，请勿重复考勤！")
                                .setNeutralButton("确定"){ dialog, which ->
                                    dialog.dismiss()
                                    finish()
                                }.show()
                        }
                    }
                    else{
                        //进行定位
                        LocationClient.setAgreePrivacy(true)
                        //声明LocationClient类
                        val mLocationClient = LocationClient(applicationContext)
                        //注册监听函数
                        mLocationClient.registerLocationListener(object : BDAbstractLocationListener() {
                            //定位的回调
                            override fun onReceiveLocation(location: BDLocation?) {
                                //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                                //以下只列举部分获取经纬度相关（常用）的结果信息
                                //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
                                val latitude = location!!.latitude //获取纬度信息
                                val longitude = location.longitude //获取经度信息
                                //判断距离是否在范围内
                                val distance = DistanceUtil.getDistance(LatLng(latitude,longitude), LatLng(eventData.latitude.toDouble(),eventData.longitude.toDouble()))
                                loadingDialog.dismiss()
                                if(distance > eventData.locationRange.toInt()){
                                    //不在范围内
                                    runOnUiThread{
                                        AlertDialog.Builder(this@AttendanceActivity).setMessage("当前位置不在考勤范围内！相差${distance.toInt()} 米,要求在${eventData.locationRange.toInt()}米内！")
                                            .setNeutralButton("确定"){ dialog, which ->
                                                dialog.dismiss()
                                                finish()
                                            }.show()
                                    }
                                }else{
                                    //在范围内
                                    //进行人脸识别
                                    //获取该账号的人脸信息
                                    post(HashMap<String,String>().apply {
                                        put("email",usingUserEmail!!)
                                    },"getProfile",this@AttendanceActivity,"处理中",{
                                        userData = Gson().fromJson(it, UserData::class.java)
                                        val intent = Intent(this@AttendanceActivity, FaceRecognitionActivity::class.java).apply {
                                            putExtra("mode","matchFeature")
                                            putExtra("originFeature",userData.faceFeature)
                                        }
                                        startActivityForResult(intent,1)
                                    },{
                                        //获取账号信息失败
                                        runOnUiThread {
                                            AlertDialog.Builder(this@AttendanceActivity).setMessage("发生错误！")
                                                .setNeutralButton("确定"){ dialog, which ->
                                                    dialog.dismiss()
                                                    finish()
                                                }.show()
                                        }
                                    })
                                }
                            }
                        })
                        mLocationClient.locOption = LocationClientOption().apply {
                            setCoorType("bd09ll")
                            setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC)
                            setOnceLocation(true)
                            isOpenGps = true
                            isLocationNotify = true
                        }
                        //开始定位
                        mLocationClient.start()
                    }
                }
            },{
                //检查时间请求失败
                runOnUiThread {
                    loadingDialog.dismiss()
                    AlertDialog.Builder(this).setMessage("发生错误！")
                        .setNeutralButton("确定"){ dialog, which ->
                            dialog.dismiss()
                            finish()
                        }.show()
                }
            })
        },{
            //获取考勤时间信息失败
            runOnUiThread {
                loadingDialog.dismiss()
                AlertDialog.Builder(this).setMessage("发生错误！")
                    .setNeutralButton("确定"){ dialog, which ->
                        dialog.dismiss()
                        finish()
                    }.show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 2){//从FaceRecognitionActivity获取到特征
            val result = data!!.getStringExtra("matchResult")
            if(result == "true"){
                //是本人
                //上传并创建考勤记录
                post(HashMap<String,String>().apply {
                    put("eventId",eventData.id.toString())
                    put("participantEmail", usingUserEmail!!)
                },"participant",this,"",{
                    runOnUiThread {
                        AlertDialog.Builder(this@AttendanceActivity).setMessage("考勤成功！")
                            .setNeutralButton("确定"){ dialog, which ->
                                dialog.dismiss()
                                finish()
                            }.show()
                    }
                },{
                    runOnUiThread {
                        AlertDialog.Builder(this@AttendanceActivity).setMessage("发生错误！")
                            .setNeutralButton("确定"){ dialog, which ->
                                dialog.dismiss()
                                finish()
                            }.show()
                    }
                })
            }else{
                //不是本人
                AlertDialog.Builder(this@AttendanceActivity).setMessage("人脸识别不通过！")
                    .setPositiveButton("确认"){ dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .setNegativeButton("重试"){ dialog, which ->
                        dialog.dismiss()
                        val intent = Intent(this@AttendanceActivity, FaceRecognitionActivity::class.java).apply {
                            putExtra("mode","matchFeature")
                            putExtra("originFeature",userData.faceFeature)
                        }
                        startActivityForResult(intent,1)
                    }
                    .show()
            }

        }
        /*//测试用
        else if(requestCode == 1 && resultCode == 2){
            Log.i("从FaceRecognitionActivity获取match结果","获取到结果：${data!!.getStringExtra("matchResult")}")
        }*/
    }
}