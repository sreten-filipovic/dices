package com.cubes.template.shared.ui.common.views.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.SemiBoldFont

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = ColorPrimary,
    textColor: Color = ColorWhite,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            contentColor = textColor,
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(50.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
    ) {
        Text(
            text = text,
            fontFamily = SemiBoldFont(),
            fontSize = 18.sp,
            letterSpacing = (-0.02).sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    Button(
        text = "Button",
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
    )
}
