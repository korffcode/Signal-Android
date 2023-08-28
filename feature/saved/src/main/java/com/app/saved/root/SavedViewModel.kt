package com.app.saved.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.saved.root.model.SavedAction
import com.app.saved.root.model.SavedItem
import com.app.signal.domain.model.State
import com.app.signal.domain.model.mapStateListItem
import com.app.signal.domain.service.PhotoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal data class SavedViewModel @Inject constructor(
    private val photoService: PhotoService
) : ViewModel() {

    private val _actionFlow = MutableSharedFlow<SavedAction>(0, 1)
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    suspend fun deletePhoto(photoId: String): StateFlow<State<Int>> {
        return photoService.deletePhoto(photoId)
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Lazily, State.Loading())
    }

    suspend fun markPhotoAsFavourite(photoId: String): StateFlow<State<Int>> {
        return photoService.markPhotoAsFavourite(photoId)
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Lazily, State.Loading())
    }


    val savedPhotos = _refreshTrigger.flatMapLatest {
        photoService.getSavedPhotos()
            .mapStateListItem {
                SavedItem.Photo(
                    it.id,
                    it.title,
                    it.img,
                    _actionFlow,
                )
            }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, State.Loading())


    fun getActionFlow(): SharedFlow<SavedAction> {
        return _actionFlow
    }

    fun refreshTrigger() {
        _refreshTrigger.value = System.currentTimeMillis()
    }


}