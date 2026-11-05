package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorPrimary

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    color: Color = ColorPrimary,
    strokeWidth: Dp = 3.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    LoadingIndicator()
}
