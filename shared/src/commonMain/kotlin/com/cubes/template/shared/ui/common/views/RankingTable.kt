package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBronze
import com.cubes.template.shared.ui.common.ColorGold
import com.cubes.template.shared.ui.common.ColorGrayLight
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorSilver
import com.cubes.template.shared.ui.common.ColorSuccess
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.SemiBoldFont

@Composable
fun RankingSection(title: String, rows: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = BoldFont(),
            fontSize = 18.sp,
            color = ColorTextPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
        )
        rows()
    }
}

@Composable
fun RankingRow(place: Int, name: String, detail: String?, value: String) {
    val isFirst = place == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFirst) ColorSuccess.copy(alpha = 0.12f) else ColorBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceBadge(place)
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            fontFamily = if (isFirst) BoldFont() else SemiBoldFont(),
            fontSize = 16.sp,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (detail != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = detail, fontFamily = SemiBoldFont(), fontSize = 14.sp, color = ColorTextSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text = value, fontFamily = BoldFont(), fontSize = 15.sp, color = ColorPrimary)
    }
}

@Composable
private fun PlaceBadge(place: Int) {
    val color = when (place) {
        1 -> ColorGold
        2 -> ColorSilver
        3 -> ColorBronze
        else -> ColorGrayLight
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = place.toString(),
            fontFamily = BoldFont(),
            fontSize = 15.sp,
            color = if (place <= 3) ColorWhite else ColorTextPrimary,
        )
    }
}
