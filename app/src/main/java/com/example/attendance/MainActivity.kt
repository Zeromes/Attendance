package com.example.attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.attendance.databinding.ActivityMainBinding
import com.example.attendance.ui.profile.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        startActivity(Intent(this,LoginActivity::class.java))
    }
}