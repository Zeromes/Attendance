package com.example.attendance.ui.profile.register

import android.content.Intent
import android.media.FaceDetector
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.attendance.R
import com.example.attendance.ui.profile.faceRecognize.FaceRecognitionActivity
import com.example.attendance.ui.profile.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun onClickRegisterConfirm(view: View){
        val email = findViewById<TextView>(R.id.editTextTextEmailAddress).text.toString()
        val password = findViewById<TextView>(R.id.editTextTextPassword1).text.toString()
        val confirmPassword = findViewById<TextView>(R.id.editTextTextPassword2).text.toString()
        val name = findViewById<TextView>(R.id.editTextTextName).text.toString()

        //TODO:判断是否填写了所有数据
        if(email.isBlank()){
            Toast.makeText(this,"请填写邮箱！",Toast.LENGTH_LONG).show()
            return
        }
        if(password.isBlank()){
            Toast.makeText(this,"请填写密码！",Toast.LENGTH_LONG).show()
            return
        }
        if(confirmPassword.isBlank()){
            Toast.makeText(this,"请填写确认密码！",Toast.LENGTH_LONG).show()
            return
        }
        if(confirmPassword.isBlank()){
            Toast.makeText(this,"请填写确认密码！",Toast.LENGTH_LONG).show()
            return
        }
        if(name.isBlank()){
            Toast.makeText(this,"请填写姓名！",Toast.LENGTH_LONG).show()
            return
        }

        //判断两次输入密码是否一致
        if(password != confirmPassword){
            Toast.makeText(this,"两次输入密码不一致！",Toast.LENGTH_LONG).show()
            return
        }
        else{//向服务器发送数据注册账号
            //显示加载界面

        }
    }

    fun onClickFaceRecord(view: View){
        startActivity(Intent(this, FaceRecognitionActivity::class.java))
    }
}