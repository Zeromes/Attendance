package com.example.attendance.utils.dataClass

data class EventDetailData(
    val state : String,
    val id : Int,
    val name : String,
    val creatorName : String?,
    val cycle : String,
    val year : String?,
    val month : String?,
    val day : String?,
    val weekday1 : String?,
    val weekday2 : String?,
    val weekday3 : String?,
    val weekday4 : String?,
    val weekday5 : String?,
    val weekday6 : String?,
    val weekday7 : String?,
    val startHour : String,
    val startMinute : String,
    val endHour : String,
    val endMinute : String,
)
