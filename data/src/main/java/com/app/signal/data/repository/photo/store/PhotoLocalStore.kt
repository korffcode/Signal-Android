package com.app.signal.data.repository.photo.store

import androidx.room.withTransaction
import com.app.signal.data.dto.response.photo.ImageDto
import com.app.signal.data.room.AppDatabase
import com.app.signal.data.room.entities.PhotoEntity
import com.app.signal.domain.model.photo.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

interface PhotoLocalStore {
    fun getSavedPhotos(): List<Photo>

    suspend fun savePhoto(dto: Photo): Flow<Long>

    suspend fun removePhoto(id: String): Flow<Int>

    suspend fun markPhotoAsFavourite(id: String): Flow<Int>
}

data class PhotoRoomStore @Inject constructor(
    private val db: AppDatabase
) : PhotoLocalStore {
    private val photoDao = db.photoDao

    override fun getSavedPhotos(): List<Photo> {
        return photoDao.getSavedPhotos()
    }

    override suspend fun savePhoto(dto: Photo): Flow<Long> {
        return channelFlow {
            db.withTransaction {
                val entity = PhotoEntity(
                    dto.id,
                    ImageDto(
                        dto.img.smallImageUrl,
                        dto.img.largeImageUrl,
                        dto.img.thumbNailUrl
                    ),
                    dto.title,
                    false
                )

                trySend(photoDao.insertOrUpdate(entity))
            }
        }
    }

    override suspend fun markPhotoAsFavourite(id: String): Flow<Int> {
        return channelFlow {
            db.withTransaction {
                trySend(photoDao.markAsFavourite(id))
            }
        }
    }

    override suspend fun removePhoto(id: String): Flow<Int> {
        return channelFlow {
            db.withTransaction {
                trySend(photoDao.deletePhoto(id))
            }
        }
    }

}