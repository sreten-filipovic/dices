package com.cubes.template.shared.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.bold
import com.cubes.template.shared.resources.extra_bold
import com.cubes.template.shared.resources.medium
import com.cubes.template.shared.resources.regular
import com.cubes.template.shared.resources.semi_bold
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

@Composable
fun BoldFont() = font(Res.font.bold)

@Composable
fun ExtraBoldFont() = font(Res.font.extra_bold)

@Composable
fun SemiBoldFont() = font(Res.font.semi_bold)

@Composable
fun MediumFont() = font(Res.font.medium)

@Composable
fun RegularFont() = font(Res.font.regular)

@Composable
private fun font(resource: FontResource): FontFamily = FontFamily(Font(resource))
