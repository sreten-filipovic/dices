package com.cubes.template.shared

import android.content.Context

class AndroidAppInfo(
    private val context: Context
) : AppInfo {

    override val versionName: String
        get() = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName ?: ""

    override val versionCode: String
        get() = context.packageManager
            .getPackageInfo(context.packageName, 0).longVersionCode.toString()
}
