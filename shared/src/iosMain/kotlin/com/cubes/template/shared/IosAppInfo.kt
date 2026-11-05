package com.cubes.template.shared

import platform.Foundation.NSBundle

class IosAppInfo : AppInfo {

    override val versionName: String
        get() = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: ""

    override val versionCode: String
        get() = NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: ""
}
