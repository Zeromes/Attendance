package com.example.attendance.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object HttpsUtils {
    fun post(data : Any,method :String,context: Context,loadingNote : String, successCallback : (String)->Unit, failCallback : ()->Unit){
        val loadingDialog = ProgressDialog.show(context, "", "正在提交", true)
        //生成Json数据
        val gson = Gson()
        val jsonData = gson.toJson(data)

        val url = URL("https://www.zeromes.cn/attendance")
        val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        conn.readTimeout = 5000
        conn.connectTimeout = 5000
        conn.doOutput = true
        conn.useCaches = false
        conn.instanceFollowRedirects = true
        val data = "method=${method}&data=${URLEncoder.encode(jsonData,"UTF-8")}".toByteArray()
        Thread(){
            Looper.prepare()
            conn.connect()
            val dos = conn.outputStream
            dos.write(data)
            dos.flush()
            dos.close()
            loadingDialog.dismiss()
            if(conn.responseCode == HttpURLConnection.HTTP_OK){
                //请求成功
                val result = String(conn.inputStream.readBytes(), charset("UTF-8"))
                if(result == "error"){
                    //服务器应用出错
                    failCallback()
                }
                else{
                    //正常返回数据
                    successCallback(result)
                }
            }
            else{
                //请求失败
                failCallback()
            }
        }.start()
    }
}