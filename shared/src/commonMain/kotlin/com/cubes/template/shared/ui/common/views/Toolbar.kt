package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ic_back
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorDivider
import com.cubes.template.shared.ui.common.ColorTextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun Toolbar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    backgroundColor: Color = ColorDivider,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(70.dp)
    ) {
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(50.dp)
                    .clickable { onBackClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp)
                .align(Alignment.Center),
            textAlign = TextAlign.Center,
            color = ColorTextPrimary,
            fontSize = 18.sp,
            fontFamily = BoldFont(),
            letterSpacing = (-0.02).sp
        )
    }
}

@Preview
@Composable
private fun ToolbarPreview() {
    Toolbar(title = "Title", onBackClick = {})
}
