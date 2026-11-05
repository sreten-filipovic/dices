package com.cubes.template.shared.ui.main.demo_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.local.LocalDataSource
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.domain.use_case.GetDemoItemDetailsUseCase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.demo_submit_success
import com.cubes.template.shared.resources.demo_submit_success_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shows: screen argument (`item`), refresh from network, local storage write,
 * a "submit" action with loading state and a success popup that navigates back on dismiss.
 */
class DemoDetailsViewModel(
    item: DemoItem,
    private val onBack: () -> Unit,
    private val popupManager: PopupManager,
    private val stringProvider: StringProvider,
    private val localDataSource: LocalDataSource,
    private val getDemoItemDetailsUseCase: GetDemoItemDetailsUseCase,
) : ViewModel(), DemoDetailsViewListener {

    private val _viewState = MutableStateFlow<DemoDetails.ViewState>(
        DemoDetails.ViewState.Success(DemoDetails.ViewData(item = item, listener = this))
    )
    val viewState: StateFlow<DemoDetails.ViewState> = _viewState.asStateFlow()

    init {
        localDataSource.saveLastOpenedDemoItemId(item.id.toString())
        onRefresh()
    }

    override fun onRefresh() {
        _viewState.update { DemoDetails.ViewState.Loading(it.data) }
        viewModelScope.launch {
            when (val result = getDemoItemDetailsUseCase.getDemoItem(_viewState.value.data.item.id)) {
                is Result.Success -> _viewState.update {
                    DemoDetails.ViewState.Success(it.data.copy(item = result.data))
                }

                is Result.Error -> _viewState.update { DemoDetails.ViewState.Error(it.data) }
            }
        }
    }

    override fun onBackClick() = onBack()

    override fun onSubmitClick() {
        val current = _viewState.value as? DemoDetails.ViewState.Success ?: return
        if (current.data.isSubmitting) return

        _viewState.value = current.copy(data = current.data.copy(isSubmitting = true))
        viewModelScope.launch {
            delay(800) // simulate a POST call

            _viewState.update { state ->
                (state as? DemoDetails.ViewState.Success)
                    ?.copy(data = state.data.copy(isSubmitting = false))
                    ?: state
            }

            popupManager.show(
                PopupState.Success(
                    title = stringProvider.getString(Res.string.demo_submit_success_title),
                    subtitle = current.data.item.title,
                    message = stringProvider.getString(Res.string.demo_submit_success),
                    onDismiss = { onBack() }
                )
            )
        }
    }
}
