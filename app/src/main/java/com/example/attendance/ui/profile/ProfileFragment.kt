package com.example.attendance.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserName
import com.example.attendance.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val notificationsViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]//获取对应的ViewModel对象

        _binding = FragmentProfileBinding.inflate(inflater, container, false)//获取FragmentProfileBinding对象，并调用inflate函数进行绘制
        val root: View = binding.root//获取对应的View对象
        if(usingUserName != null){
            binding.nameTextView.text = usingUserName
        }
        else{
            binding.nameTextView.setText(R.string.login_or_register)
        }
//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}