package com.example.attendance.utils

import org.opencv.core.Mat
import com.google.gson.JsonObject
import org.opencv.core.CvType
import org.apache.commons.lang3.SerializationUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.lang.UnsupportedOperationException
import java.util.*

object MatJsonUtils {

    fun matToJson(mat: Mat): String {
        val obj = JsonObject()
        if (mat.isContinuous) {
            val cols = mat.cols()
            val rows = mat.rows()
            val elemSize = mat.elemSize().toInt()
            val type = mat.type()
            obj.addProperty("rows", rows)
            obj.addProperty("cols", cols)
            obj.addProperty("type", type)

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            val dataString: String
            dataString =
                if (type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
                    val data = IntArray(cols * rows * elemSize)
                    mat[0, 0, data]
                    String(Base64.getEncoder().encode(SerializationUtils.serialize(data)))
                } else if (type == CvType.CV_32F || type == CvType.CV_32FC2) {
                    val data = FloatArray(cols * rows * elemSize)
                    mat[0, 0, data]
                    String(Base64.getEncoder().encode(SerializationUtils.serialize(data)))
                } else if (type == CvType.CV_64F || type == CvType.CV_64FC2) {
                    val data = DoubleArray(cols * rows * elemSize)
                    mat[0, 0, data]
                    String(Base64.getEncoder().encode(SerializationUtils.serialize(data)))
                } else if (type == CvType.CV_8U) {
                    val data = ByteArray(cols * rows * elemSize)
                    mat[0, 0, data]
                    String(Base64.getEncoder().encode(data))
                } else {
                    throw UnsupportedOperationException("unknown type")
                }
            obj.addProperty("data", dataString)
            val gson = Gson()
            return gson.toJson(obj)
        } else {
            println("Mat not continuous.")
        }
        return "{}"
    }

    fun matFromJson(json: String?): Mat {
        val parser = JsonParser()
        val JsonObject = parser.parse(json).asJsonObject
        val rows = JsonObject["rows"].asInt
        val cols = JsonObject["cols"].asInt
        val type = JsonObject["type"].asInt
        val mat = Mat(rows, cols, type)
        val dataString = JsonObject["data"].asString
        if (type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
            val data = SerializationUtils.deserialize<IntArray>(
                Base64.getDecoder().decode(dataString.toByteArray())
            )
            mat.put(0, 0, data)
        } else if (type == CvType.CV_32F || type == CvType.CV_32FC2) {
            val data = SerializationUtils.deserialize<FloatArray>(
                Base64.getDecoder().decode(dataString.toByteArray())
            )
            mat.put(0, 0, data)
        } else if (type == CvType.CV_64F || type == CvType.CV_64FC2) {
            val data = SerializationUtils.deserialize<DoubleArray>(
                Base64.getDecoder().decode(dataString.toByteArray())
            )
            mat.put(0, 0, *data)
        } else if (type == CvType.CV_8U) {
            val data = Base64.getDecoder().decode(dataString.toByteArray())
            mat.put(0, 0, data)
        } else {
            throw UnsupportedOperationException("unknown type")
        }
        return mat
    }
}