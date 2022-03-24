package com.tolgaay.myhomework384_fragmentnavigation.RoomDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface ArtDao {

    @Query("Select id,name From Art")
    fun getArtWithNameAndId(): Flowable<List<Art>>

    @Query("Select * From Art Where id = :id")
    fun getArtById(id: Int): Flowable<Art>

    @Insert()
    fun insert(art: Art) : Completable

    @Delete
    fun delete(art: Art) : Completable
}