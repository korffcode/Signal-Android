package com.app.signal.domain.repository

import com.app.signal.domain.form.photo.SearchQueryParams
import com.app.signal.domain.model.PhotoListPage
import com.app.signal.domain.model.State
import com.app.signal.domain.model.photo.Photo
import kotlinx.coroutines.flow.Flow

typealias PhotosState = State<List<Photo>>

typealias PhotosPageState = State<PhotoListPage<Photo>>

typealias LongState = State<Long>

typealias IntState = State<Int>

interface PhotoRepository {

    fun getSearchResults(params: SearchQueryParams): Flow<PhotosPageState>

    fun getSavedPhotos(): Flow<PhotosState>

    fun savePhoto(photo: Photo): Flow<LongState>

    fun removeSavedPhoto(id: String): Flow<IntState>

    fun markPhotoAsFavourite(id: String): Flow<IntState>
}