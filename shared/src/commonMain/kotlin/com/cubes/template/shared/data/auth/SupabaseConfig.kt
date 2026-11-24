package com.cubes.template.shared.data.auth

object SupabaseConfig {
    const val URL = "https://ugmwmtkwxonuojbjnpmw.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_FCis4M9j3k30HRgM8TGp3Q_rMOGVFAv"

    val isConfigured: Boolean get() = URL.isNotBlank() && PUBLISHABLE_KEY.isNotBlank()
}
