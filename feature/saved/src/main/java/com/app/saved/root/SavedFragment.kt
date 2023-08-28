package com.app.saved.root

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alert_sheet.presentAlert
import com.app.navigation.router.ImageDetailRouter
import com.app.saved.root.adapter.SavedAdapter
import com.app.saved.root.model.SavedAction
import com.app.saved.root.model.SavedItem
import com.app.signal.control_kit.ex.present
import com.app.signal.control_kit.fragment.ActionBarToolbarFragment
import com.app.signal.control_kit.fragment.ScrollableFragment
import com.app.signal.control_kit.fragment.ex.consumeWindowInsets
import com.app.signal.control_kit.fragment.ex.promptToast
import com.app.signal.control_kit.fragment.ex.requireRouterFragmentManager
import com.app.signal.control_kit.recycler_view.decorations.MarginDecoration
import com.app.signal.control_kit.recycler_view.decorations.SpacingDecoration
import com.app.signal.design_system.R.dimen
import com.app.signal.domain.model.State
import com.app.signal.saved.R
import com.app.signal.utils.loadAttrDimension
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class SavedFragment : ActionBarToolbarFragment(R.layout.fragment_saved),
    ScrollableFragment {
    private val vm: SavedViewModel by viewModels()

    private lateinit var savedRv: RecyclerView

    private lateinit var _indicatorView: CircularProgressIndicator

    @Inject
    lateinit var _imageDetailRouter: ImageDetailRouter

    override fun resetScroll() {
        savedRv.smoothScrollToPosition(0)
    }

    override fun onResume() {
        super.onResume()
        refreshSavedPhotos()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layout = LinearLayoutManager(view.context)

        savedRv = view.findViewById(R.id.saved_rv)

        savedRv.also {
            it.setItemViewCacheSize(24)
            it.addItemDecoration(
                SpacingDecoration(
                    ctx = it.context,
                    spacingRes = dimen.spacing_sm
                )
            )

            it.addItemDecoration(
                MarginDecoration(
                    ctx = it.context,
                    horizontalResId = dimen.spacing_md
                )
            )

            it.adapter = SavedAdapter()
            it.layoutManager = layout

            _indicatorView = view.findViewById(R.id.indicator_view)

            val spacing =
                view.context.loadAttrDimension(dimen.spacing_md)

            consumeWindowInsets { _, insets ->
                toolbar.updatePadding(top = insets.top)

                savedRv.updatePadding(
                    top = spacing,
                    bottom = insets.bottom
                )

                WindowInsetsCompat.CONSUMED
            }

            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                    scrolledHeight = view.computeVerticalScrollOffset()
                    updateToolbar(scrolledHeight)
                    super.onScrolled(view, dx, dy)
                }
            })

        }

        bindFlows()
    }

    private fun bindFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { bindItemsFlow() }
                launch { bindActionFlow() }
            }
        }
    }

    private suspend fun bindActionFlow() {
        vm.getActionFlow().collect(this::handleSelection)

    }

    private suspend fun bindItemsFlow() {
        val adapter = savedRv.adapter as SavedAdapter
        vm.savedPhotos.collect {
            _indicatorView.isVisible = it.isLoading == true && it.data.isNullOrEmpty()
            adapter.submit(it.data ?: emptyList())
        }
    }

    private fun handleSelection(action: SavedAction) {
        when (action) {
            is SavedAction.Delete -> onDeletePhoto(action.photo.id)
            is SavedAction.Select -> toImageDetail(action.photo)
            is SavedAction.MarkAsFavourite -> onMarkPhotoAsFavourite(action.photo.id)
        }
    }

    private fun toImageDetail(photo: SavedItem.Photo) {
        photo.image?.largeImageUrl?.let {
            _imageDetailRouter.getImageDetailFragment(photo.title, it)
        }?.let {
            requireRouterFragmentManager().present(it)
        }
    }

    private fun onDeletePhoto(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.deletePhoto(id).collect { state ->
                when (state) {
                    is State.Success -> onPhotoDeleted()
                    is State.Error -> presentAlert(throwable = state.cause)
                    else -> {}
                }
            }
        }
    }

    private fun onMarkPhotoAsFavourite(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.markPhotoAsFavourite(id).collect { state ->
                when (state) {
                    is State.Success -> {
                        //noop
                    }
                    is State.Error -> presentAlert(throwable = state.cause)
                    else -> {}
                }
            }
        }
    }

    private fun updateToolbar(dy: Int) {
        val distance = toolbar.height.toFloat()
        toolbar.updateToolbarProgress(dy.toFloat() / distance)
    }

    private fun onPhotoDeleted() {
        promptToast(getString(R.string.image_deleted))
        refreshSavedPhotos()
    }

    private fun refreshSavedPhotos() {
        vm.refreshTrigger()
    }

}