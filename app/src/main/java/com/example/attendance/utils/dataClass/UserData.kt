package com.example.attendance.utils.dataClass

data class UserData(
    val email : String,
    val password : String,
    val name : String?,
    val faceFeature : String?
){
    constructor(email: String,password: String):this(email,password,null,null)
}
