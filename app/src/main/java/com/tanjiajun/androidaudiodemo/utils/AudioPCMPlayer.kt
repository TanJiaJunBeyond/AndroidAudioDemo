package com.tanjiajun.androidaudiodemo.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * Created by TanJiaJun on 2024/4/13.
 */
class AudioPCMPlayer private constructor(
    private val audioTrack: AudioTrack,
    private val audioFormat: AudioPCMPlayerFormat
) {

    /**
     * 播放位深度为16bit的PCM音频
     *
     * @param audioData 音频数据
     * @param listener 音频播放监听器
     */
    suspend fun play16BitPCMAudio(
        audioData: List<ShortArray>,
        listener: AudioPCMPlayListener? = null
    ) {
        if (audioFormat != AudioPCMPlayerFormat.PCM_16BIT) {
            return
        }
        withIO {
            audioTrack.play()
            audioData.forEach {
                audioTrack.write(it, 0, it.size)
            }
            withMain {
                listener?.onCompletion()
            }
        }
    }

    /**
     * 播放位深度为32bit的PCM音频
     *
     * @param audioData 音频数据
     * @param listener 音频播放监听器
     */
    suspend fun play32BitPCMAudio(
        audioData: List<FloatArray>,
        listener: AudioPCMPlayListener? = null
    ) {
        if (audioFormat != AudioPCMPlayerFormat.PCM_FLOAT) {
            return
        }
        withIO {
            audioTrack.play()
            audioData.forEach {
                audioTrack.write(it, 0, it.size, AudioTrack.WRITE_BLOCKING)
            }
            withMain {
                listener?.onCompletion()
            }
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        audioTrack.stop()
    }

    /**
     * 释放资源
     */
    fun release() {
        audioTrack.release()
    }

    class Builder {

        private var minBufferSize: Int = 0
        private lateinit var audioTrack: AudioTrack
        private var sampleRateInHz: Int = 44100
        private var audioFormat: AudioPCMPlayerFormat = AudioPCMPlayerFormat.PCM_16BIT
        private var channelConfig: AudioPCMPlayerChannelConfig = AudioPCMPlayerChannelConfig.STEREO

        fun setSampleRateInHz(sampleRateInHz: Int): Builder {
            this.sampleRateInHz = sampleRateInHz
            return this
        }

        fun setAudioFormat(audioFormat: AudioPCMPlayerFormat): Builder {
            this.audioFormat = audioFormat
            return this
        }

        fun setChannelConfig(channelConfig: AudioPCMPlayerChannelConfig): Builder {
            this.channelConfig = channelConfig
            return this
        }

        fun build(): AudioPCMPlayer {
            minBufferSize =
                AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig.value, audioFormat.value)
            audioTrack = AudioTrack.Builder()
                .setBufferSizeInBytes(minBufferSize)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRateInHz)
                        .setEncoding(audioFormat.value)
                        .setChannelMask(channelConfig.value)
                        .build()
                )
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            return AudioPCMPlayer(
                audioTrack,
                audioFormat
            )
        }

    }

    enum class AudioPCMPlayerChannelConfig(val value: Int) {

        @Description("单声道")
        MONO(AudioFormat.CHANNEL_OUT_MONO),

        @Description("立体声声道")
        STEREO(AudioFormat.CHANNEL_OUT_STEREO)

    }

    enum class AudioPCMPlayerFormat(val value: Int) {

        @Description("PCM每个采样16位，保证由设备支持")
        PCM_16BIT(AudioFormat.ENCODING_PCM_16BIT),

        @Description("PCM每个采样单精度浮点")
        PCM_FLOAT(AudioFormat.ENCODING_PCM_FLOAT)

    }

    interface AudioPCMPlayListener {

        /**
         * 播放完毕
         */
        fun onCompletion()

    }

    private companion object {
        const val TAG = "AudioPCMPlayer"
    }

}