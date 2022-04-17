package com.example.attendance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.attendance.data.UsingUserData
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.databinding.ActivityMainBinding
import com.example.attendance.ui.attendance.AttendanceActivity
import com.example.attendance.ui.myAttendance.AddNewAttendanceActivity
import com.example.attendance.ui.myAttendance.EventDetailActivity
import com.example.attendance.ui.profile.login.LoginActivity
import com.example.attendance.ui.profile.profileDetail.ProfileDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.devilsen.czxing.Scanner
import me.devilsen.czxing.util.BarCodeUtil
import me.devilsen.czxing.view.ScanActivityDelegate.OnClickAlbumDelegate
import me.devilsen.czxing.view.ScanActivityDelegate.OnScanDelegate
import me.devilsen.czxing.view.ScanView
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UsingUserData.initData(this)
        binding = ActivityMainBinding.inflate(layoutInflater)//构造一个ActivityMainBinding对象，并调用inflate方法
        setContentView(binding.root)//root是ActivityMainBinding对应的View对象，将界面设为这个view对象

        val navView: BottomNavigationView = binding.navView //通过id获取BottomNavigationView对象

        val navController = findNavController(R.id.nav_host_fragment_activity_main) //通过id获取获取到Fragment的NavController对象
        //将每个menu的ID以一个ID集合的形式传入，因为每个menu应当被认为是顶级目标
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(//AppBarConfiguration构造函数，设置应用的AppBar
            setOf(
                R.id.navigation_attendance, R.id.navigation_my_attendance, R.id.navigation_profile  //这里对应了在bottom_nav_menu.xml文件中定义的每个菜单项
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)//通过NavController来设置ActionBar
        navView.setupWithNavController(navController)//使用NavController来设置底部导航栏（BottomNavigationView）对象
    }

    fun onClickProfile(view : View){
        if(usingUserEmail == null){
            //尚未登录的情况
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent,1)
        }
        else{
            //已经登录的情况
            val intent = Intent(this, ProfileDetailActivity::class.java)
            startActivityForResult(intent,2)
        }
    }

    fun onClickCreateAttendanceEvent(view: View){
        //未登录请先登录
        if(usingUserEmail == null){
            Toast.makeText(this,"请先登录！",Toast.LENGTH_LONG).show()
        }
        else{
            val intent = Intent(this, AddNewAttendanceActivity::class.java)
            startActivityForResult(intent,3)
        }
    }

    fun onClickScanQRCode(view: View){
        //未登录请先登录
        if(usingUserEmail == null){
            Toast.makeText(this,"请先登录！",Toast.LENGTH_LONG).show()
        }
        else{
            Scanner.with(this)
                .setScanMode(ScanView.SCAN_MODE_TINY) // 扫描区域 0：混合 1：只扫描框内 2：只扫描整个屏幕
                .setTitle("扫码考勤") // 扫码界面标题
                .showAlbum(false) // 显示相册(默认为true)
                .setScanNoticeText("将二维码置于框中") // 设置扫码文字提示
                .setFlashLightInvisible() // 不使用闪光灯图标及提示
                .enableOpenCVDetect(true)// OpenCV探测
                .continuousScan()// 连续扫码，不关闭扫码界面
                .setOnScanResultDelegate { activity, result, format ->
                    //Log.i("扫码","activity：$activity")
                    //Log.i("扫码","结果：$result")
                    //Log.i("扫码","format：$format")
                    // 接管扫码成功的数据
                    //TODO：创建人扫码直接进入事件详情页
                    if (result.substring(0, 22) == "AttendanceSystemQRCode") {
                        activity.finish()
                        val intent = Intent(this, AttendanceActivity::class.java).apply {
                            putExtra("id", result.substring(22))
                        }
                        startActivity(intent)
                    }
                }
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 1){//成功登录
            findViewById<TextView>(R.id.nameTextView).text = data!!.getStringExtra("name")
        }
        else if(requestCode == 2 && resultCode == 1){//退出登录了
            findViewById<TextView>(R.id.nameTextView).setText(R.string.login_or_register)
        }
        else if(requestCode == 3 && resultCode == 1){//创建考勤事件Activity的监听
            //获取到新创建的考勤事件的id
            val id = data!!.getStringExtra("id")
            //Log.i("创建考勤事件","返回了id：$id")
            val intent = Intent(this, EventDetailActivity::class.java).apply {
                putExtra("id", id)
            }
            startActivity(intent)
        }
    }
}