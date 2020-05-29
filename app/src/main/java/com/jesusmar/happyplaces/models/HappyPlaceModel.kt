package com.jesusmar.happyplaces.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull


@Entity(tableName = "HappyPlacesTable")
data class HappyPlaceModel (

    @PrimaryKey(autoGenerate = true) val _id: Int,
    @ColumnInfo(name = "title" ) val title: String?,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "location") val location: String?,
    @ColumnInfo(name = "latitude" ) val latitude: String?,
    @ColumnInfo(name = "longitude") val longitude: String?,
    @ColumnInfo(name = "size") val size: String?

): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(_id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(latitude!!)
        parcel.writeString(longitude!!)
        parcel.writeString(size)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlaceModel> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceModel {
            return HappyPlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlaceModel?> {
            return arrayOfNulls(size)
        }
    }
}
