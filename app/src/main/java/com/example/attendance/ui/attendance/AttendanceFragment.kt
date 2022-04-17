package com.example.attendance.ui.attendance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.data.UsingUserData
import com.example.attendance.databinding.FragmentAttendanceBinding
import com.example.attendance.ui.myAttendance.EventDetailActivity
import com.example.attendance.utils.HttpsUtils
import com.example.attendance.utils.ItemClickSupport
import com.example.attendance.utils.adapter.EventCardAdapter
import com.example.attendance.utils.adapter.HistoryCardAdapter
import com.example.attendance.utils.dataClass.EventCardData
import com.example.attendance.utils.dataClass.EventListData
import com.example.attendance.utils.dataClass.HistoryCardData
import com.example.attendance.utils.dataClass.HistoryListData
import com.google.gson.Gson
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import me.devilsen.czxing.thread.ExecutorUtil

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    private lateinit var refreshLayout : RefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dashboardViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]

        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


        val historyCardList = mutableListOf<HistoryCardData>()

        val recyclerView : RecyclerView = binding.recordListRecyclerView//获取RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val mAdapter = HistoryCardAdapter(historyCardList)
        recyclerView.adapter = mAdapter

        refreshLayout = binding.refreshLayout
        refreshLayout.setEnableFooterFollowWhenNoMoreData(true)
        refreshLayout.setRefreshHeader(ClassicsHeader(this.context))
        refreshLayout.setRefreshFooter(ClassicsFooter(this.context))
        refreshLayout.setOnRefreshListener {
            //it.finishRefresh(1000/*,false*/);//传入false表示刷新失败
            if(UsingUserData.usingUserEmail == null){
                Toast.makeText(context,"请先登录！", Toast.LENGTH_SHORT).show()
                refreshLayout.finishRefresh(false)
            }
            else{
                historyCardList.clear()
                HttpsUtils.post(HashMap<String, String>().apply {
                    put("email", UsingUserData.usingUserEmail!!)
                    put("offset", "0")
                }, "getHistoryList", requireContext(), "", {
                    val result: HistoryListData = Gson().fromJson(it, HistoryListData::class.java)
                    for (item in result.result) {
                        val displayHour = if (item.hour!!.toInt() < 10) "0${item.hour}" else item.hour
                        val displayMinute = if (item.minute!!.toInt() < 10) "0${item.minute}" else item.minute
                        historyCardList.add(
                            HistoryCardData(
                                item.id,
                                item.eventName,
                                "${item.year}年${item.month}月${item.day}日",
                                "${displayHour}:${displayMinute}"
                            )
                        )
                    }
                    ExecutorUtil.runOnUiThread {
                        mAdapter.notifyDataSetChanged()
                        refreshLayout.finishRefresh()
                        refreshLayout.resetNoMoreData()
                    }
                }, {
                    refreshLayout.finishRefresh(false)
                })
            }
        }
        refreshLayout.setOnLoadMoreListener {
            //it.finishLoadMore(1000/*,false*/);//传入false表示加载失败
            if(UsingUserData.usingUserEmail == null){
                Toast.makeText(context,"请先登录！",Toast.LENGTH_SHORT).show()
                refreshLayout.finishLoadMore(false)
            }
            else{
                HttpsUtils.post(HashMap<String, String>().apply {
                    put("email", UsingUserData.usingUserEmail!!)
                    put("offset", historyCardList.size.toString())
                }, "getHistoryList", requireContext(), "", {
                    val result: HistoryListData = Gson().fromJson(it, HistoryListData::class.java)
                    for (item in result.result) {
                        val displayHour = if (item.hour!!.toInt() < 10) "0${item.hour}" else item.hour
                        val displayMinute = if (item.minute!!.toInt() < 10) "0${item.minute}" else item.minute
                        historyCardList.add(
                            HistoryCardData(
                                item.id,
                                item.eventName,
                                "${item.year}年${item.month}月${item.day}日",
                                "${displayHour}:${displayMinute}"
                            )
                        )
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
                    refreshLayout.finishLoadMore(false)
                })
            }
        }
        if(UsingUserData.usingUserEmail != null){
            refreshLayout.autoRefresh()
        }
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener { recyclerView, position, v ->
            //Log.i("点击事件", "获取到事件名：${eventCardList[position].name}")

        }
        return root
    }

    /*override fun onResume() {
        super.onResume()
        if(UsingUserData.usingUserEmail != null){
            refreshLayout.autoRefresh()
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}