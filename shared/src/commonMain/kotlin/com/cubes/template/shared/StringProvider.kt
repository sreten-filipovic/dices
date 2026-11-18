package com.cubes.template.shared

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

interface StringProvider {
    fun getString(stringResource: StringResource): String
    fun getString(stringResource: StringResource, vararg formatArgs: Any): String
}

class StringProviderImpl : StringProvider {
    override fun getString(stringResource: StringResource): String = runBlocking {
        org.jetbrains.compose.resources.getString(stringResource)
    }

    override fun getString(stringResource: StringResource, vararg formatArgs: Any): String = runBlocking {
        org.jetbrains.compose.resources.getString(stringResource, *formatArgs)
    }
}
