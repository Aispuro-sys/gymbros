package com.talos.forge

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import com.talos.forge.data.ApiClient
import com.talos.forge.data.SessionManager

class TalosApp : Application(), ImageLoaderFactory {
    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        ApiClient.init(sessionManager)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }
}
