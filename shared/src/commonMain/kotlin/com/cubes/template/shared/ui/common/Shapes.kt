package com.cubes.template.shared.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ShapeSmall = RoundedCornerShape(4.dp)
val ShapeMedium = RoundedCornerShape(6.dp)
val ShapeLarge = ShapeSmall
val ShapeSheet = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = ShapeSmall,
    medium = ShapeMedium,
    large = ShapeLarge,
    extraLarge = ShapeLarge,
)
