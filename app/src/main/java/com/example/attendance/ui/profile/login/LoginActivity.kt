package com.example.attendance.ui.profile.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.data.UsingUserData.usingUserName
import com.example.attendance.ui.profile.register.RegisterActivity
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.dataClass.UserData
import com.example.attendance.utils.dataClass.UserLoginReturnData
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 1){//成功注册了一个新账号
            val intent = Intent().apply {
                putExtra("name",data!!.getStringExtra("name"))
            }
            setResult(1,intent)
            finish()
        }
    }


    fun onClickRegister(view: View){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivityForResult(intent,1)
    }

    fun onClickLogin(view: View){
        val email = findViewById<TextView>(R.id.editTextLoginEmail).text.toString()
        val password = findViewById<TextView>(R.id.editTextLoginPassword).text.toString()
        //判断是否填写了所有数据
        if(email.isBlank()){
            Toast.makeText(this,"请填写邮箱！",Toast.LENGTH_LONG).show()
            return
        }
        if(password.isBlank()){
            Toast.makeText(this,"请填写密码！",Toast.LENGTH_LONG).show()
            return
        }
        post(UserData(email, password),"login",this,"正在登录",{
            //获取到一个Json字符串，对应UserLoginReturnData类
            val tempData : UserLoginReturnData = Gson().fromJson(it,UserLoginReturnData::class.java)
            if(tempData.state == "success"){
                usingUserEmail = tempData.email
                usingUserName = tempData.name
                val intent = Intent().apply {
                    putExtra("name",tempData.name)
                }
                setResult(1,intent)
                Toast.makeText(this,"登录成功！", Toast.LENGTH_LONG).show()
                finish()
            }
            else if(tempData.state == "fail"){
                Toast.makeText(this,"账号或密码错误！",Toast.LENGTH_LONG).show()
            }
        },{
        })
    }
}