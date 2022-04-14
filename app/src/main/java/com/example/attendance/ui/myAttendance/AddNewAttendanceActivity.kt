package com.example.attendance.ui.myAttendance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.data.UsingUserData.usingUserName
import com.example.attendance.utils.HttpsUtils.post
import java.util.*
import kotlin.collections.HashMap

class AddNewAttendanceActivity : AppCompatActivity() {


    private var selectedCycle : String = "一次性"
    private var selectedYear : String? = null
    private var selectedMonth : String? = null
    private var selectedDay : String? = null
    private var selectedStartHour : String? = null
    private var selectedStartMinute : String? = null
    private var selectedEndHour : String? = null
    private var selectedEndMinute : String? = null

    private var isBadTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_attendance)

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
                //TODO("Not yet implemented")
            }

        }
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
        DatePickerDialog(this,DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
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
        TimePickerDialog(this,TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
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
        TimePickerDialog(this,TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
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
}