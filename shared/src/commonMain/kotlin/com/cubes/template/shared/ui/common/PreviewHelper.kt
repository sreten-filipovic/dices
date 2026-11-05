package com.cubes.template.shared.ui.common

import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.ui.main.demo_details.DemoDetails
import com.cubes.template.shared.ui.main.demo_details.DemoDetailsViewListener
import com.cubes.template.shared.ui.main.demo_list.DemoList
import com.cubes.template.shared.ui.main.demo_list.DemoListViewListener

/**
 * Fake data + no-op listeners for @Preview functions. Keep one builder per screen state.
 */
object PreviewHelper {

    fun demoItems() = List(5) { index ->
        DemoItem(
            id = index + 1,
            title = "Demo item ${index + 1}",
            description = "Short description of demo item number ${index + 1} used in previews.",
        )
    }

    fun demoListLoading() = DemoList.ViewState.Loading(
        DemoList.ViewData(listener = noOpDemoListListener())
    )

    fun demoListSuccess() = DemoList.ViewState.Success(
        DemoList.ViewData(items = demoItems(), listener = noOpDemoListListener())
    )

    fun demoDetailsSuccess() = DemoDetails.ViewState.Success(
        DemoDetails.ViewData(item = demoItems().first(), listener = noOpDemoDetailsListener())
    )

    private fun noOpDemoListListener() = object : DemoListViewListener {
        override fun onRefresh() = Unit
        override fun onQueryChanged(query: String) = Unit
        override fun onItemClick(item: DemoItem) = Unit
        override fun onOpenDocsClick() = Unit
    }

    private fun noOpDemoDetailsListener() = object : DemoDetailsViewListener {
        override fun onRefresh() = Unit
        override fun onBackClick() = Unit
        override fun onSubmitClick() = Unit
    }
}
