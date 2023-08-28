package com.app.signal.data.room.dao.photo

import androidx.room.Dao
import androidx.room.Query
import com.app.signal.data.room.dao.AnyDao
import com.app.signal.data.room.entities.PhotoEntity

@Dao
abstract class PhotoDao : AnyDao<PhotoEntity>() {

    @Query("SELECT * FROM `photo`")
    abstract fun getSavedPhotos(): List<PhotoEntity>

    @Query("DELETE FROM `photo` WHERE id=:id")
    abstract suspend fun deletePhoto(id: String): Int

    @Query("UPDATE `photo` SET isFavourite = 0 WHERE id=:id")
    abstract suspend fun markAsFavourite(id: String): Int

}