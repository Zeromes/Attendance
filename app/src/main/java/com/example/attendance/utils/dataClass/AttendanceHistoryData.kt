package com.example.attendance.utils.dataClass

data class AttendanceHistoryData(
    val id : Int,
    val eventName : String,
    val participantEmail : String?,
    val year : String?,
    val month : String?,
    val day : String?,
    val hour : String?,
    val minute : String?
)
