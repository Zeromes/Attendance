package com.example.attendance.ui.profile.register

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.R
import com.example.attendance.data.UsingUserData
import com.example.attendance.data.UsingUserData.usingUserEmail
import com.example.attendance.data.UsingUserData.usingUserName
import com.example.attendance.ui.profile.faceRecognize.FaceRecognitionActivity
import com.example.attendance.utils.HttpsUtils.post
import com.example.attendance.utils.dataClass.UserData

class RegisterActivity : AppCompatActivity() {
    /*private var testMode = "getFeature"//getFeature matchFeature 测试用
    private var gotFeature = false //测试用*/

    private var faceFeature : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun onClickRegisterConfirm(view: View){
        val email = findViewById<TextView>(R.id.editTextTextEmailAddress).text.toString()
        val password = findViewById<TextView>(R.id.editTextTextPassword1).text.toString()
        val confirmPassword = findViewById<TextView>(R.id.editTextTextPassword2).text.toString()
        val name = findViewById<TextView>(R.id.editTextTextName).text.toString()

        //判断是否填写了所有数据
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
        if(faceFeature == null){
            Toast.makeText(this,"请录入人脸数据！",Toast.LENGTH_LONG).show()
            return
        }
        //判断两次输入密码是否一致
        if(password != confirmPassword){
            Toast.makeText(this,"两次输入密码不一致！",Toast.LENGTH_LONG).show()
            return
        }
        //向服务器发送数据注册账号
        //显示加载界面
        post(UserData(email, password, name, faceFeature!!),"register",this,"正在提交",{
            //请求成功
            //Log.i("网络请求","请求成功：返回：$it")
            if(it == "rep"){
                Toast.makeText(this,"该邮箱已经使用！",Toast.LENGTH_LONG).show()
            }
            else if(it == "success"){
                usingUserEmail = email
                usingUserName = name
                val intent = Intent().apply {
                    putExtra("name",name)
                }
                setResult(1,intent)
                Toast.makeText(this,"注册成功！",Toast.LENGTH_LONG).show()
                finish()
            }
        },{
            //请求失败
            //Log.e("网络请求","注册请求失败，返回码：${conn.responseCode}")
            Toast.makeText(this,"网络错误！",Toast.LENGTH_LONG).show()
        })

        /*//生成Json数据
        val gson = Gson()
        val userdata = UserData(email, password, name, faceFeature!!)
        val jsonData = gson.toJson(userdata)

        val url = URL("https://www.zeromes.cn/attendance")
        val conn: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        conn.readTimeout = 5000
        conn.connectTimeout = 5000
        conn.doOutput = true
        conn.useCaches = false
        conn.instanceFollowRedirects = true
        //Log.i("网络请求","要传输的数据：${"data=${URLEncoder.encode(jsonData,"UTF-8")}"}")
        val data = "method=register&data=${URLEncoder.encode(jsonData,"UTF-8")}".toByteArray()
        Thread(){
            Looper.prepare()
            conn.connect()
            val dos = conn.outputStream
            dos.write(data)
            dos.flush()
            dos.close()
            if(conn.responseCode == HttpURLConnection.HTTP_OK){
                //请求成功
                val result = String(conn.inputStream.readBytes(), charset("UTF-8"))
                //Log.i("网络请求","请求成功：返回：$result")
                loadingDialog.dismiss()
                if(result == "rep"){
                    Toast.makeText(this,"该邮箱已经使用！",Toast.LENGTH_LONG).show()
                }
                else if(result == "success"){
                    Toast.makeText(this,"注册成功！",Toast.LENGTH_LONG).show()
                    val intent = Intent().apply {
                        putExtra("email",email)
                    }
                    setResult(1,intent)
                    finish()
                }
            }
            else{
                //请求失败
                //Log.e("网络请求","注册请求失败，返回码：${conn.responseCode}")
                loadingDialog.dismiss()
                Toast.makeText(this,"网络错误！",Toast.LENGTH_LONG).show()
            }
        }.start()*/
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.appcompat.app.AppCompatActivity"
    )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == 1){//从FaceRecognitionActivity获取到特征
            val feature = data!!.getStringExtra("feature")
            //Log.i("从FaceRecognitionActivity获取feature","获取到feature${feature}")
            faceFeature = feature
            //设置界面以及内部状态
            findViewById<TextView>(R.id.FaceDataNoteTextView).run {
                setText(R.string.face_data_committed_note)
                setTextColor(resources.getColor(R.color.green,theme))
            }
            findViewById<Button>(R.id.buttonFaceRecognize).setText(R.string.face_data_recommit_button_string)

        }
        /*//测试用
        else if(requestCode == 1 && resultCode == 2){
            Log.i("从FaceRecognitionActivity获取match结果","获取到结果：${data!!.getStringExtra("matchResult")}")
        }*/
    }

    fun onClickFaceRecord(view: View){
        val intent = Intent(this, FaceRecognitionActivity::class.java).apply {
            putExtra("mode","getFeature")
        }
        startActivityForResult(intent,1)


        /*//测试用
        if(gotFeature){
            intent = Intent(this, FaceRecognitionActivity::class.java).apply {
                putExtra("mode","matchFeature")
                putExtra("originFeature",faceFeature)
            }
            startActivityForResult(intent,1)
        }
        else{
            intent = Intent(this, FaceRecognitionActivity::class.java).apply {
                putExtra("mode","getFeature")
            }
            startActivityForResult(intent,1)
        }*/
    }


}