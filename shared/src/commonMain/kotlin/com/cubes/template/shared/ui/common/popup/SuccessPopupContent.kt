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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ic_check
import com.cubes.template.shared.resources.ic_close
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import org.jetbrains.compose.resources.painterResource

@Composable
fun SuccessPopupContent(
    state: PopupState.Success,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

        if (!state.title.isNullOrEmpty()) {
            Text(
                text = state.title,
                color = ColorTextPrimary,
                fontSize = 16.sp,
                fontFamily = RegularFont(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .padding(horizontal = 20.dp)
            )
        }

        if (!state.subtitle.isNullOrEmpty()) {
            Text(
                text = state.subtitle,
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                color = ColorTextPrimary,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .padding(horizontal = 20.dp)
            )
        }

        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 40.dp)
                .size(48.dp),
            tint = Color.Unspecified
        )

        if (!state.message.isNullOrEmpty()) {
            Text(
                text = state.message,
                fontFamily = RegularFont(),
                fontSize = 20.sp,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .padding(horizontal = 60.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessPopupContentPreview() {
    Box(modifier = Modifier.background(Color.White)) {
        SuccessPopupContent(
            state = PopupState.Success(
                subtitle = "Success!",
                message = "Your request has been processed."
            ),
            onClose = {}
        )
    }
}
