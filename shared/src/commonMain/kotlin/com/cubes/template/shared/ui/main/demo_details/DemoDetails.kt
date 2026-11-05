package com.cubes.template.shared.ui.main.demo_details

import com.cubes.template.shared.domain.model.DemoItem

object DemoDetails {

    sealed class ViewState {
        abstract val data: ViewData

        data class Loading(override val data: ViewData) : ViewState()
        data class Error(override val data: ViewData) : ViewState()
        data class Success(override val data: ViewData) : ViewState()
    }

    data class ViewData(
        val item: DemoItem,
        val isSubmitting: Boolean = false,
        val listener: DemoDetailsViewListener,
    )
}

interface DemoDetailsViewListener {
    fun onRefresh()
    fun onBackClick()
    fun onSubmitClick()
}
