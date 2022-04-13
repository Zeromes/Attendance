package com.example.attendance.ui.profile.profileDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.attendance.R
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.utils.HttpsUtils.post

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }

    fun onClickConfirmChangePassword(view: View){
        val originalPassword = findViewById<TextView>(R.id.editTextOriginalPassword).text.toString()
        val newPassword = findViewById<TextView>(R.id.editTextNewPassword).text.toString()
        val confirmNewPassword = findViewById<TextView>(R.id.editTextConfirmNewPassword).text.toString()
        //判断是否填写了所有数据
        if(originalPassword.isBlank()){
            Toast.makeText(this,"请填写原密码！", Toast.LENGTH_LONG).show()
            return
        }
        if(newPassword.isBlank()){
            Toast.makeText(this,"请填写新密码！", Toast.LENGTH_LONG).show()
            return
        }
        //判断两次输入的密码是否不一致
        if(originalPassword == newPassword){
            Toast.makeText(this,"新密码与旧密码相同！", Toast.LENGTH_LONG).show()
            return
        }
        //判断确认密码是否一致
        if(newPassword != confirmNewPassword){
            Toast.makeText(this,"确认密码与新密码不一致！", Toast.LENGTH_LONG).show()
            return
        }

        val data : HashMap<String,String> = HashMap<String,String>().apply {
            put("email",usingUserEmail!!)
            put("originalPassword",originalPassword)
            put("newPassword",newPassword)
        }
        post(data,"changePassword",this,"正在提交",{
            if(it == "success"){
                Toast.makeText(this,"修改成功！", Toast.LENGTH_LONG).show()
                finish()
            }
            else if(it == "fail"){
                Toast.makeText(this,"旧密码错误！", Toast.LENGTH_LONG).show()
            }
        },{

        })
    }
}