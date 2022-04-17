package com.example.attendance.ui.myAttendance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.geocode.*
import com.example.attendance.MainActivity
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.databinding.FragmentMyAttendanceBinding
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.ItemClickSupport
import com.example.attendance.utils.adapter.EventCardAdapter
import com.example.attendance.utils.dataClass.EventCardData
import com.example.attendance.utils.dataClass.EventListData
import com.google.gson.Gson
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import me.devilsen.czxing.thread.ExecutorUtil.runOnUiThread


class MyAttendanceFragment : Fragment() {

    private var _binding: FragmentMyAttendanceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var refreshLayout : RefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(MyAttendanceViewModel::class.java)

        _binding = FragmentMyAttendanceBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


        val eventCardList = mutableListOf<EventCardData>()

        val recyclerView : RecyclerView = binding.historyRecyclerView//获取RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val mAdapter = EventCardAdapter(eventCardList)
        recyclerView.adapter = mAdapter

        refreshLayout = binding.refreshLayout//获取RefreshLayout
        refreshLayout.setEnableFooterFollowWhenNoMoreData(true)
        refreshLayout.setRefreshHeader(ClassicsHeader(this.context))
        refreshLayout.setRefreshFooter(ClassicsFooter(this.context))

        refreshLayout.setOnRefreshListener {
            //it.finishRefresh(1000/*,false*/);//传入false表示刷新失败
            if(usingUserEmail == null){
                Toast.makeText(context,"请先登录！",Toast.LENGTH_SHORT).show()
                refreshLayout.finishRefresh(false)
            }
            else{
                eventCardList.clear()
                post(HashMap<String,String>().apply {
                    put("email",usingUserEmail!!)
                    put("offset", "0")
                },"getEventList",requireContext(),"",{
                    val result : EventListData = Gson().fromJson(it,EventListData::class.java)
                    for (item in result.result){
                        val dateOrWeekday = when(item.cycle){
                            "一次性"-> "${item.year}年${item.month}月${item.day}日"
                            "每天"-> "无日期"
                            "每星期"-> (if(item.weekday1=="true")"星期一、" else "") +
                                    (if(item.weekday2=="true")"星期二、" else "") +
                                    (if(item.weekday3=="true")"星期三、" else "") +
                                    (if(item.weekday4=="true")"星期四、" else "") +
                                    (if(item.weekday5=="true")"星期五、" else "") +
                                    (if(item.weekday6=="true")"星期六、" else "") +
                                    (if(item.weekday7=="true")"星期日" else "")
                            else ->  ""
                        }
                        val displayStartHour = if(item.startHour.toInt()<10) "0${item.startHour}" else item.startHour
                        val displayStartMinute = if(item.startMinute.toInt()<10) "0${item.startMinute}" else item.startMinute
                        val displayEndHour = if(item.endHour.toInt()<10) "0${item.endHour}" else item.endHour
                        val displayEndMinute = if(item.endMinute.toInt()<10) "0${item.endMinute}" else item.endMinute
                        eventCardList.add(EventCardData(
                            item.id,
                            item.name,
                            item.cycle,
                            dateOrWeekday,
                            "${displayStartHour}:${displayStartMinute} - ${displayEndHour}:${displayEndMinute}"
                        ))
                    }
                    runOnUiThread{
                        mAdapter.notifyDataSetChanged()
                        refreshLayout.finishRefresh()
                        refreshLayout.resetNoMoreData()
                    }
                },{
                    refreshLayout.finishRefresh(false)
                })
            }
        }
        refreshLayout.setOnLoadMoreListener {
            //it.finishLoadMore(1000/*,false*/);//传入false表示加载失败
            if(usingUserEmail == null){
                Toast.makeText(context,"请先登录！",Toast.LENGTH_SHORT).show()
                refreshLayout.finishLoadMore(false)
            }
            else{
                post(HashMap<String,String>().apply {
                    put("email",usingUserEmail!!)
                    put("offset",eventCardList.size.toString())
                },"getEventList",requireContext(),"",{
                    val result : EventListData = Gson().fromJson(it,EventListData::class.java)
                    for (item in result.result){
                        val dateOrWeekday = when(item.cycle){
                            "一次性"-> "${item.year}年${item.month}月${item.day}日"
                            "每天"-> "无日期"
                            "每星期"-> (if(item.weekday1=="true")"星期一、" else "") +
                                    (if(item.weekday2=="true")"星期二、" else "") +
                                    (if(item.weekday3=="true")"星期三、" else "") +
                                    (if(item.weekday4=="true")"星期四、" else "") +
                                    (if(item.weekday5=="true")"星期五、" else "") +
                                    (if(item.weekday6=="true")"星期六、" else "") +
                                    (if(item.weekday7=="true")"星期日" else "")
                            else ->  ""
                        }
                        val displayStartHour = if(item.startHour.toInt()<10) "0${item.startHour}" else item.startHour
                        val displayStartMinute = if(item.startMinute.toInt()<10) "0${item.startMinute}" else item.startMinute
                        val displayEndHour = if(item.endHour.toInt()<10) "0${item.endHour}" else item.endHour
                        val displayEndMinute = if(item.endMinute.toInt()<10) "0${item.endMinute}" else item.endMinute
                        eventCardList.add(EventCardData(
                            item.id,
                            item.name,
                            item.cycle,
                            dateOrWeekday,
                            "${displayStartHour}:${displayStartMinute} - ${displayEndHour}:${displayEndMinute}"
                        ))
                    }
                    runOnUiThread{
                        mAdapter.notifyDataSetChanged()
                        if(result.state == "noMore"){
                            refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        else{
                            refreshLayout.finishLoadMore()
                        }
                    }
                },{
                    refreshLayout.finishLoadMore(false)
                })
            }
        }
        if(usingUserEmail != null){
            refreshLayout.autoRefresh()
        }
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener { recyclerView, position, v ->
            //Log.i("点击事件", "获取到事件名：${eventCardList[position].name}")
            val intent = Intent(context, EventDetailActivity::class.java).apply {
                putExtra("id", eventCardList[position].id.toString())
            }
            startActivity(intent)
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        if(usingUserEmail != null){
            refreshLayout.autoRefresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}