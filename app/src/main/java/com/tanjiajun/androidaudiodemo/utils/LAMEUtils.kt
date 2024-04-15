package com.tanjiajun.androidaudiodemo.utils

/**
 * Created by TanJiaJun on 2024/3/28.
 */
object LAMEUtils {

    init {
        System.loadLibrary("MP3Encoder")
    }

    /**
     * 获取当前LAME版本
     *
     * @return 当前LAME版本
     */
    external fun getLameVersion(): String

    /**
     * 初始化LAME
     *
     * @param inputPCMFilePath 输入的PCM文件路径
     * @param outputMP3FilePath 输出的MP3文件路径
     * @param sampleRateInHz 采样率，单位：赫兹
     * @param channelCount 声道数
     * @param bitRate 比特率
     * @return 是否初始化成功
     */
    external fun init(
        inputPCMFilePath: String,
        outputMP3FilePath: String,
        sampleRateInHz: Int,
        channelCount: Int,
        bitRate: Int
    ): Boolean

    /**
     * 编码
     */
    external fun encode()

    /**
     * 销毁
     */
    external fun destroy()

}