package com.cubes.template.shared.ui.common

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.error_invalid_email
import com.cubes.template.shared.resources.error_invalid_phone
import com.cubes.template.shared.resources.error_required_field
import com.cubes.template.shared.resources.password_min_length
import com.cubes.template.shared.ui.common.ValidationResult.Fail
import com.cubes.template.shared.ui.common.ValidationResult.Success

class Validator(private val stringProvider: StringProvider) {

    companion object {
        fun String.isPhoneNumber(): Boolean =
            this.trim().startsWith("+") || this.trim().startsWith("00")

        fun String.isEmail(): Boolean =
            Regex("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$").matches(this.trim())
    }

    fun validateRequired(value: String): ValidationResult =
        if (value.isBlank()) Fail(stringProvider.getString(Res.string.error_required_field)) else Success

    fun validatePhone(value: String): ValidationResult {
        if (value.isBlank()) return Fail(stringProvider.getString(Res.string.error_required_field))
        return if (value.isPhoneNumber()) Success else Fail(stringProvider.getString(Res.string.error_invalid_phone))
    }

    fun validateEmail(value: String): ValidationResult {
        if (value.isBlank()) return Fail(stringProvider.getString(Res.string.error_required_field))
        return if (value.isEmail()) Success else Fail(stringProvider.getString(Res.string.error_invalid_email))
    }

    fun validatePassword(value: String, minLength: Int = 8): ValidationResult {
        if (value.isBlank()) return Fail(stringProvider.getString(Res.string.error_required_field))
        return if (value.length >= minLength) Success
        else Fail(stringProvider.getString(Res.string.password_min_length, minLength))
    }
}
