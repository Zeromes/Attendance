package com.example.attendance.data

import android.app.Activity
import android.content.Context


object UsingUserData {
    var usingUserEmail : String? = null
    var usingUserName : String? = null

    fun initData(activity: Activity){
        val sharedPref = activity.getSharedPreferences("userLoginData",Context.MODE_PRIVATE)
        usingUserEmail = sharedPref.getString("usingUserEmail", "")
        usingUserName = sharedPref.getString("usingUserName", "")
        if(usingUserEmail == ""){
            usingUserEmail = null
        }
        if(usingUserName == ""){
            usingUserName = null
        }
    }

    fun setData(activity: Activity,email:String,name:String){
        usingUserEmail = email
        usingUserName = name
        //持久化处理
        val sharedPref = activity.getSharedPreferences("userLoginData",Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("usingUserEmail",email)
        editor.putString("usingUserName",name)
        editor.apply()
    }

    fun clearData(activity: Activity){
        usingUserEmail = null
        usingUserName = null
        //持久化处理
        val sharedPref = activity.getSharedPreferences("userLoginData",Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("usingUserEmail")
        editor.remove("usingUserName")
        editor.apply()
    }
}