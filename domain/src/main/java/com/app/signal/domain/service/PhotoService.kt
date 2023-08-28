package com.app.signal.domain.service

import com.app.signal.domain.form.photo.SearchQueryParams
import com.app.signal.domain.model.photo.Photo
import com.app.signal.domain.repository.IntState
import com.app.signal.domain.repository.LongState
import com.app.signal.domain.repository.PhotosPageState
import com.app.signal.domain.repository.PhotosState
import kotlinx.coroutines.flow.Flow

interface PhotoService {

    fun observePreviousSearches(): Flow<List<String>>

    suspend fun searchPhotos(searchQueryParams: SearchQueryParams): Flow<PhotosPageState>

    fun getSavedPhotos(): Flow<PhotosState>

    suspend fun savePhoto(photo: Photo): Flow<LongState>

    suspend fun deletePhoto(id: String): Flow<IntState>
    suspend fun markPhotoAsFavourite(id: String): Flow<IntState>
}