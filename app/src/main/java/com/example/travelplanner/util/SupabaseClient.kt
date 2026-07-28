package com.example.travelplanner.util

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object Supabase {
    // Replace with your credentials from Step 1
    const val SUPABASE_URL = "https://pwcxuchtzfehdyuduthq.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_h81VL4tTRuQwOaHQLeGvvg_cLHx384I"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}