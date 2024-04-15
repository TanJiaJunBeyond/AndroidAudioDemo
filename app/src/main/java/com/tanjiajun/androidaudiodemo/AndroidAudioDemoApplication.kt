package com.tanjiajun.androidaudiodemo

import android.app.Application

/**
 * Created by TanJiaJun on 2024/3/21.
 */
class AndroidAudioDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AndroidAudioDemoApplication
    }

}