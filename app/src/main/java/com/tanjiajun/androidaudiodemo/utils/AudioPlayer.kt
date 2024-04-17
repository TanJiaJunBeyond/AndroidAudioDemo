package com.tanjiajun.androidaudiodemo.utils

import android.media.MediaPlayer

/**
 * Created by TanJiaJun on 2024/4/9.
 */
class AudioPlayer {

    private val mediaPlayer = MediaPlayer()

    /**
     * 播放音频
     *
     * @param audioFilePath 音频文件路径
     * @param listener 音频播放监听器
     */
    suspend fun play(audioFilePath: String, listener: AudioPlayerListener? = null) {
        if (audioFilePath.isEmpty()) {
            return
        }
        withIO {
            mediaPlayer.reset()
            mediaPlayer.setOnCompletionListener {
                listener?.onCompletion()
            }
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        mediaPlayer.stop()
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaPlayer.release()
    }

    interface AudioPlayerListener {

        /**
         * 播放完毕
         */
        fun onCompletion()

    }

    private companion object {
        const val TAG = "AudioPlayer"
    }

}