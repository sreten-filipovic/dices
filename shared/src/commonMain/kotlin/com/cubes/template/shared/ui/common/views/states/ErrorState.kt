package com.cubes.template.shared.ui.common.views.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ic_refresh
import com.cubes.template.shared.ui.common.ColorPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun ErrorState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_refresh),
            contentDescription = "Refresh",
            colorFilter = ColorFilter.tint(ColorPrimary),
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = onRefresh)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    ErrorState(onRefresh = {})
}
