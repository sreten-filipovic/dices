package com.cubes.template.shared.data.remote

data class EmptyStateException(override val cause: Throwable) :
    Exception("Empty data", cause)
