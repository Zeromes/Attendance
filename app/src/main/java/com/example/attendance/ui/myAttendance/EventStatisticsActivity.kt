package com.example.attendance.ui.myAttendance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.CalendarView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.data.UsingUserData
import com.example.attendance.utils.HttpsUtils
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.adapter.StatisticsAdapter
import com.example.attendance.utils.dataClass.HistoryCardData
import com.example.attendance.utils.dataClass.HistoryListData
import com.example.attendance.utils.dataClass.StatisticsData
import com.example.attendance.utils.dataClass.StatisticsListData
import com.google.gson.Gson
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import me.devilsen.czxing.thread.ExecutorUtil
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.HashMap

class EventStatisticsActivity : AppCompatActivity() {
    lateinit var mAdapter : StatisticsAdapter
    lateinit var refreshLayout : RefreshLayout
    lateinit var statisticsList : MutableList<StatisticsData>
    var selectedYear : Int = 0
    var selectedMonth : Int = 0
    var selectedDay : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_statistics)

        statisticsList = mutableListOf<StatisticsData>()

        val recyclerView : RecyclerView = findViewById(R.id.statisticsRecyclerView)//获取RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = StatisticsAdapter(statisticsList)
        recyclerView.adapter = mAdapter

        refreshLayout = findViewById(R.id.refreshLayout)
        refreshLayout.setRefreshHeader(ClassicsHeader(this))
        refreshLayout.setRefreshFooter(ClassicsFooter(this))

        refreshLayout.setOnRefreshListener{
            statisticsList.clear()
            post(HashMap<String, String>().apply {
                put("id",intent.getStringExtra("id")!!)
                put("year",selectedYear.toString())
                put("month",selectedMonth.toString())
                put("dayOfMonth",selectedDay.toString())
                put("offset","0")
            }, "getStatisticsList", this, "", {
                val result: StatisticsListData = Gson().fromJson(it, StatisticsListData::class.java)
                statisticsList.clear()
                for(item in result.result){
                    statisticsList.add(item)
                }
                ExecutorUtil.runOnUiThread {
                    mAdapter.notifyDataSetChanged()
                    refreshLayout.finishRefresh()
                    refreshLayout.resetNoMoreData()
                }
            }, {
                ExecutorUtil.runOnUiThread{
                    refreshLayout.finishRefresh(false)
                }
            })
        }
        refreshLayout.setOnLoadMoreListener{
            post(HashMap<String, String>().apply {
                put("id",intent.getStringExtra("id")!!)
                put("year",selectedYear.toString())
                put("month",selectedMonth.toString())
                put("dayOfMonth",selectedDay.toString())
                put("offset", statisticsList.size.toString())
            }, "getStatisticsList", this, "", {
                val result: StatisticsListData = Gson().fromJson(it, StatisticsListData::class.java)
                for(item in result.result){
                    statisticsList.add(item)
                }
                ExecutorUtil.runOnUiThread {
                    mAdapter.notifyDataSetChanged()
                    if (result.state == "noMore") {
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    } else {
                        refreshLayout.finishLoadMore()
                    }
                }
            }, {
                ExecutorUtil.runOnUiThread{
                    refreshLayout.finishLoadMore(false)
                }
            })
        }

        val calendarView : CalendarView = findViewById(R.id.calendarView)
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = calendarView.date
        }
        selectedYear = startCalendar.get(Calendar.YEAR)
        selectedMonth = startCalendar.get(Calendar.MONTH)+1
        selectedDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        getRecordFromServer()

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month+1
            selectedDay = dayOfMonth
            getRecordFromServer()
        }

    }

    //切换日期时用
    private fun getRecordFromServer(){
        post(HashMap<String,String>().apply {
            put("id",intent.getStringExtra("id")!!)
            put("year",this@EventStatisticsActivity.selectedYear.toString())
            put("month",this@EventStatisticsActivity.selectedMonth.toString())
            put("dayOfMonth",this@EventStatisticsActivity.selectedDay.toString())
            put("offset","0")
        },"getStatisticsList",this,"获取中",{
            val result: StatisticsListData = Gson().fromJson(it, StatisticsListData::class.java)
            ExecutorUtil.runOnUiThread{
                statisticsList.clear()
                for(item in result.result){
                    statisticsList.add(item)
                }
                //设置界面
                if (statisticsList.isEmpty()){
                    findViewById<TextView>(R.id.noDataNoteTextView).visibility = VISIBLE
                    findViewById<RecyclerView>(R.id.statisticsRecyclerView).visibility = GONE
                }
                else{
                    findViewById<TextView>(R.id.noDataNoteTextView).visibility = GONE
                    findViewById<RecyclerView>(R.id.statisticsRecyclerView).visibility = VISIBLE
                }
                mAdapter.notifyDataSetChanged()
            }
        },{
            ExecutorUtil.runOnUiThread{
                statisticsList.clear()
                findViewById<TextView>(R.id.noDataNoteTextView).visibility = VISIBLE
                findViewById<RecyclerView>(R.id.statisticsRecyclerView).visibility = GONE
                mAdapter.notifyDataSetChanged()
            }
        })
    }
}