package com.tanjiajun.androidaudiodemo.utils

import android.media.AudioFormat

/**
 * Created by TanJiaJun on 2024/3/20.
 */
object AudioUtils {

    /**
     * 得到比特率，采样率 * 位深度 * 声道数 = 比特率
     *
     * @param sampleRateInHz 采样率，单位：赫兹
     * @param bitDepth 位深度
     * @param channelCount 声道数
     * @return 比特率
     */
    @JvmStatic
    fun getBitRate(
        sampleRateInHz: Int,
        bitDepth: Int,
        channelCount: Int
    ): Int =
        sampleRateInHz * bitDepth * channelCount

    /**
     * 得到音频时长，单位：秒。
     *
     * @param byteLength 字节长度
     * @param sampleRateInHz 采样率，单位：赫兹
     * @param bitDepth 位深度
     * @param channelCount 声道数
     * @return 音频时长，单位：秒
     */
    @JvmStatic
    fun getAudioDurationInSec(
        byteLength: Long,
        sampleRateInHz: Int,
        bitDepth: Int,
        channelCount: Int
    ): Long {
        val bitRate = sampleRateInHz * bitDepth * channelCount
        return byteLength * 8 / bitRate
    }

    /**
     * 通过声道配置得到声道数。
     *
     * @param channelConfig 声道配置
     * @return 声道数
     */
    @JvmStatic
    fun getChannelCountByChannelConfig(channelConfig: Int): Int =
        when (channelConfig) {
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.CHANNEL_IN_LEFT,
            AudioFormat.CHANNEL_IN_LEFT_PROCESSED,
            AudioFormat.CHANNEL_IN_RIGHT,
            AudioFormat.CHANNEL_IN_RIGHT_PROCESSED -> 1

            AudioFormat.CHANNEL_IN_STEREO -> 2

            else -> throw IllegalArgumentException("Bad channel config $channelConfig.")
        }

    /**
     * 通过音频格式得到位深度
     *
     * @param audioFormat 音频格式
     * @return 位深度
     */
    @JvmStatic
    fun getBitDepthByAudioFormat(audioFormat: Int): Int =
        when (audioFormat) {
            AudioFormat.ENCODING_PCM_8BIT -> 8

            AudioFormat.ENCODING_PCM_16BIT -> 16

            AudioFormat.ENCODING_PCM_FLOAT -> 32

            AudioFormat.ENCODING_INVALID -> throw IllegalArgumentException("Bad audio format $audioFormat.")

            else -> throw IllegalArgumentException("Bad audio format $audioFormat.")
        }

}