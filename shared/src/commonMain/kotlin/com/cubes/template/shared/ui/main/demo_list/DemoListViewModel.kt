package com.cubes.template.shared.ui.main.demo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.domain.use_case.GetDemoItemsUseCase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.error_loading_data
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemoListViewModel(
    private val screenNavigator: DemoListScreenNavigator,
    private val popupManager: PopupManager,
    private val stringProvider: StringProvider,
    private val getDemoItemsUseCase: GetDemoItemsUseCase,
) : ViewModel(), DemoListViewListener {

    private val _viewState = MutableStateFlow<DemoList.ViewState>(
        DemoList.ViewState.Loading(DemoList.ViewData(listener = this))
    )
    val viewState: StateFlow<DemoList.ViewState> = _viewState.asStateFlow()

    init {
        onRefresh()
    }

    override fun onRefresh() {
        _viewState.value = DemoList.ViewState.Loading(_viewState.value.data)
        viewModelScope.launch {
            when (val result = getDemoItemsUseCase.getDemoItems()) {
                is Result.Success -> _viewState.update {
                    DemoList.ViewState.Success(it.data.copy(items = result.data))
                }

                is Result.Error -> {
                    _viewState.update { DemoList.ViewState.Error(it.data) }
                    popupManager.show(
                        PopupState.Error(
                            message = stringProvider.getString(Res.string.error_loading_data)
                        )
                    )
                }
            }
        }
    }

    override fun onQueryChanged(query: String) {
        _viewState.update { state ->
            (state as? DemoList.ViewState.Success)
                ?.copy(data = state.data.copy(query = query))
                ?: state
        }
    }

    override fun onItemClick(item: DemoItem) = screenNavigator.showDemoDetails(item)

    override fun onOpenDocsClick() =
        screenNavigator.openUrl("https://www.jetbrains.com/compose-multiplatform/")
}
