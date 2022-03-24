package com.tolgaay.myhomework384_fragmentnavigation.RoomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Art (

    @ColumnInfo(name = "name")
    var artName : String,

    @ColumnInfo(name = "artistname")
    var artistName: String?,

    @ColumnInfo(name = "year")
    var year : String?,

    @ColumnInfo(name = "image")
    var image : ByteArray?

    ) {
        @PrimaryKey(autoGenerate = true)
        var id : Int = 0
}