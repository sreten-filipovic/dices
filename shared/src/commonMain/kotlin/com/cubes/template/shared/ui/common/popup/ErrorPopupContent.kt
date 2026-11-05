package com.cubes.template.shared.ui.common.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.error_occurred
import com.cubes.template.shared.resources.ic_close
import com.cubes.template.shared.resources.ic_warning
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.MediumFont
import com.cubes.template.shared.ui.common.RegularFont
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorPopupContent(
    state: PopupState.Error,
    onClose: () -> Unit
) {
    val message = state.message?.takeIf { it.isNotEmpty() } ?: stringResource(Res.string.error_occurred)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 60.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier
                    .size(27.dp)
                    .clickable { onClose() },
                tint = Color.Unspecified
            )
        }

        Icon(
            painter = painterResource(Res.drawable.ic_warning),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 20.dp, top = 20.dp, bottom = 30.dp)
                .size(32.dp),
            tint = Color.Unspecified
        )

        if (!state.title.isNullOrEmpty()) {
            Text(
                text = state.title,
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                color = ColorTextPrimary,
                lineHeight = 28.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (!state.subtitle.isNullOrEmpty()) {
            Text(
                text = state.subtitle,
                fontFamily = MediumFont(),
                fontSize = 20.sp,
                color = ColorTextPrimary,
                lineHeight = 25.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 5.dp)
            )
        }

        Text(
            text = message,
            fontFamily = RegularFont(),
            fontSize = 16.sp,
            color = ColorTextPrimary,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 30.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPopupContentPreview() {
    Box(modifier = Modifier.background(Color.White)) {
        ErrorPopupContent(
            state = PopupState.Error(
                title = "Something went wrong",
                subtitle = "Request failed",
                message = "We could not process your request. Please try again later."
            ),
            onClose = {}
        )
    }
}
