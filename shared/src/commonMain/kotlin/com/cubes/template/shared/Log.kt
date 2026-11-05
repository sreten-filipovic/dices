package com.cubes.template.shared

import co.touchlab.kermit.Logger

fun log(msg: String) = Logger.i(msg)
fun log(tag: String, msg: String) = Logger.i("$tag: $msg")
