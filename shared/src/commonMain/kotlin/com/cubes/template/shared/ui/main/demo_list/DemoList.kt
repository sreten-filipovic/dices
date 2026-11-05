package com.cubes.template.shared.ui.main.demo_list

import com.cubes.template.shared.domain.model.DemoItem

/**
 * Screen contract: state, data and user actions in one place.
 *
 * - ViewState: Loading / Error / Success, each carrying the same ViewData so the UI keeps
 *   previously shown data while reloading.
 * - ViewData: everything the Composable needs to render, plus the listener.
 * - ViewListener: every user action; implemented by the ViewModel.
 */
object DemoList {

    sealed class ViewState {
        abstract val data: ViewData

        data class Loading(override val data: ViewData) : ViewState()
        data class Error(override val data: ViewData) : ViewState()
        data class Success(override val data: ViewData) : ViewState()
    }

    data class ViewData(
        val items: List<DemoItem> = emptyList(),
        val query: String = "",
        val listener: DemoListViewListener,
    ) {
        val filteredItems: List<DemoItem>
            get() = if (query.isBlank()) items
            else items.filter { it.title.contains(query, ignoreCase = true) }
    }
}

interface DemoListViewListener {
    fun onRefresh()
    fun onQueryChanged(query: String)
    fun onItemClick(item: DemoItem)
    fun onOpenDocsClick()
}

/** Navigation this screen can trigger. Implemented by the Main navigator. */
interface DemoListScreenNavigator {
    fun showDemoDetails(item: DemoItem)
    fun openUrl(url: String)
}
