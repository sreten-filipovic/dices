package com.cubes.template.shared.ui.main.demo_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.demo_list_title
import com.cubes.template.shared.resources.demo_open_docs
import com.cubes.template.shared.resources.demo_search_hint
import com.cubes.template.shared.resources.no_data
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import com.cubes.template.shared.ui.common.views.states.EmptyState
import com.cubes.template.shared.ui.common.views.states.ErrorState
import com.cubes.template.shared.ui.common.views.states.LoadingState
import org.jetbrains.compose.resources.stringResource

/** Stateful wrapper: collects state from the ViewModel. */
@Composable
fun DemoListView(viewModel: DemoListViewModel) {
    val viewState by viewModel.viewState.collectAsState()
    DemoListScreen(viewState = viewState)
}

/** Stateless screen: previewable, no ViewModel dependency. */
@Composable
fun DemoListScreen(viewState: DemoList.ViewState) {
    val data = viewState.data
    val listener = data.listener

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(title = stringResource(Res.string.demo_list_title))

        OutlinedTextField(
            value = data.query,
            onValueChange = listener::onQueryChanged,
            placeholder = { Text(stringResource(Res.string.demo_search_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        when (viewState) {
            is DemoList.ViewState.Loading -> LoadingState()
            is DemoList.ViewState.Error -> ErrorState(onRefresh = listener::onRefresh)
            is DemoList.ViewState.Success -> {
                val items = data.filteredItems
                if (items.isEmpty()) {
                    EmptyState(text = stringResource(Res.string.no_data))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            DemoListItemView(item = item, onClick = { listener.onItemClick(item) })
                        }
                        item {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                text = stringResource(Res.string.demo_open_docs),
                                onClick = listener::onOpenDocsClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoListItemView(item: DemoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = item.title,
            fontFamily = SemiBoldFont(),
            fontSize = 16.sp,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.description,
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoListScreenSuccessPreview() {
    AppTheme {
        DemoListScreen(viewState = PreviewHelper.demoListSuccess())
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoListScreenLoadingPreview() {
    AppTheme {
        DemoListScreen(viewState = PreviewHelper.demoListLoading())
    }
}
