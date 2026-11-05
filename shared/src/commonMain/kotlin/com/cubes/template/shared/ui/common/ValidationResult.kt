package com.cubes.template.shared.ui.common

import com.cubes.template.shared.ui.common.ValidationResult.Fail
import com.cubes.template.shared.ui.common.ValidationResult.Success

sealed class ValidationResult {

    data object Success : ValidationResult()
    data class Fail(val msg: String) : ValidationResult()

    inline fun ifFailed(block: (Fail) -> Unit) {
        if (this is Fail) block(this)
    }

    fun msg(): String = when (this) {
        is Fail -> this.msg
        Success -> ""
    }
}
