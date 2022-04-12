package com.example.attendance.utils

import android.os.Parcel
import android.os.Parcelable
import org.opencv.core.Mat

class ParcelableMat() : Parcelable {

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableMat> {
        override fun createFromParcel(parcel: Parcel): ParcelableMat {
            return ParcelableMat(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableMat?> {
            return arrayOfNulls(size)
        }
    }
}