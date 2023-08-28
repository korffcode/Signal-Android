package com.app.signal.data.room.entities

import androidx.room.Entity
import com.app.signal.data.dto.response.photo.AnyImageDto
import com.app.signal.domain.model.photo.Photo

@Entity(tableName = "photo", primaryKeys = ["id"])
data class PhotoEntity(
    override val id: String,
    val image: AnyImageDto,
    override val title: String,
    val isFavourite: Boolean
) : Photo {
    override val img: AnyImageDto
        get() = image
}
