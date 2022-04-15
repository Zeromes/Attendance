package com.example.attendance.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.attendance.databinding.FragmentAttendanceBinding
import com.example.attendance.databinding.FragmentMyAttendanceBinding
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dashboardViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]

        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        val refreshLayout: RefreshLayout = binding.refreshLayout
        refreshLayout.setRefreshHeader(ClassicsHeader(this.context))
        refreshLayout.setRefreshFooter(ClassicsFooter(this.context))
        refreshLayout.setOnRefreshListener {
            it.finishRefresh(2000/*,false*/);//传入false表示刷新失败
        }
        refreshLayout.setOnLoadMoreListener {
            it.finishLoadMore(2000/*,false*/);//传入false表示加载失败
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}