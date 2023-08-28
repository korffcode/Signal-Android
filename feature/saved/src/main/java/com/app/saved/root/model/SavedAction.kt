package com.app.saved.root.model

import com.app.saved.root.model.SavedItem.Photo

sealed class SavedAction {
    data class Select(val photo: Photo) : SavedAction()
    data class Delete(val photo: Photo) : SavedAction()
    data class MarkAsFavourite(val photo: Photo) : SavedAction()
}