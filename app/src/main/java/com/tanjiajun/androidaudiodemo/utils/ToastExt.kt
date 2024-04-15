package com.tanjiajun.androidaudiodemo.utils

import android.widget.Toast
import com.tanjiajun.androidaudiodemo.AndroidAudioDemoApplication

/**
 * Created by TanJiaJun on 2020-02-13.
 */
fun toastShort(text: String) =
    Toast.makeText(AndroidAudioDemoApplication.instance, text, Toast.LENGTH_SHORT).show()

fun toastLong(text: String) =
    Toast.makeText(AndroidAudioDemoApplication.instance, text, Toast.LENGTH_LONG).show()
