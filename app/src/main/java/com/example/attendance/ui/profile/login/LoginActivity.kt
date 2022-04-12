package com.example.attendance.ui.profile.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.attendance.R
import com.example.attendance.ui.profile.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onClickRegister(view: View){
        startActivity(Intent(this,RegisterActivity::class.java))
    }
}