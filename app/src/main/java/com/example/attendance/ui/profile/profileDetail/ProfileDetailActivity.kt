package com.example.attendance.ui.profile.profileDetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.data.UsingUserData.usingUserName

class ProfileDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)
        findViewById<TextView>(R.id.nameTextView).text = usingUserName
        findViewById<TextView>(R.id.emailTextView).text = usingUserEmail
    }

    fun onClickChangePassword(view: View){
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivityForResult(intent,1)
    }

    fun onClickLogout(view: View){
        usingUserEmail = null
        usingUserName = null
        val intent = Intent()
        setResult(1,intent)
        Toast.makeText(this,"已退出登录",Toast.LENGTH_LONG).show()
        finish()
    }

}