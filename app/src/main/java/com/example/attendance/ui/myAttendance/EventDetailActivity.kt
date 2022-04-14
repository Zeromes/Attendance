package com.example.attendance.ui.myAttendance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.attendance.R
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.dataClass.EventDetailData
import com.example.attendance.utils.dataClass.UserLoginReturnData
import com.google.gson.Gson

class EventDetailActivity : AppCompatActivity() {
    private var data : EventDetailData? = null
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
            }
        },{

        })
    }

    fun onClickEditEvent(view: View){

    }

    fun onClickArchiveEvent(view: View){
        
    }

}