package com.example.helatrack.auth


import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android
import com.example.helatrack.BuildConfig

@OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
object SupabaseConfig {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            httpEngine = Android.create()

            httpConfig {
                // You can set timeouts here, but don't call engine()
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 15000
                }
            }

            install(Auth)
            install(Postgrest)
        }
    }
}