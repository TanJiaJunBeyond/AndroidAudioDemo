package com.tanjiajun.androidaudiodemo.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import java.io.File
import java.io.FileOutputStream


/**
 * Created by TanJiaJun on 2024/3/20.
 *
 * 录音器
 */
class AudioRecorder private constructor(
    private val minBufferSize: Int = 0,
    private val audioRecord: AudioRecord,
    private val sampleRateInHz: Int,
    private val audioFormat: AudioRecorderFormat,
    private val channelConfig: AudioRecorderChannelConfig,
    private val noiseSuppressor: NoiseSuppressor?,
    private val acousticEchoCanceler: AcousticEchoCanceler?,
    private val listener: AudioRecordListener? = null
) {

    private val shortArrays: MutableList<ShortArray> by lazy { mutableListOf() }
    private val floatArrays: MutableList<FloatArray> by lazy { mutableListOf() }
    private var byteLength: Long = 0L

    /**
     * 录制一段音频
     */
    suspend fun record() {
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            throw RuntimeException("Cannot be call record() while recording.")
        }
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
            return
        }
        withIO {
            audioRecord.startRecording()
            while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                var byteCount = 0
                if (audioFormat == AudioRecorderFormat.PCM_16BIT) {
                    val shortArray = ShortArray(minBufferSize)
                    byteCount = audioRecord.read(shortArray, 0, shortArray.size) * 2
                    withMain {
                        shortArrays.add(shortArray)
                    }
                } else {
                    val floatArray = FloatArray(minBufferSize)
                    byteCount = audioRecord.read(
                        floatArray,
                        0,
                        floatArray.size,
                        AudioRecord.READ_BLOCKING
                    ) * 4
                    withMain {
                        floatArrays.add(floatArray)
                    }
                }
                if (byteCount <= 0) {
                    return@withIO
                }
                withMain {
                    byteLength += byteCount
                    val durationInSecond: Long = AudioUtils.getDurationInSecond(
                        byteLength = byteLength,
                        sampleRateInHz = sampleRateInHz,
                        bitDepth = AudioUtils.getBitDepthByAudioFormat(audioFormat.value),
                        channelCount = AudioUtils.getChannelCountByChannelConfig(channelConfig.value)
                    )
                    listener?.onRecording(durationInSecond)
                }
            }
        }
    }

    fun getRecordedDataFor16BitPCM(): List<ShortArray> =
        shortArrays.toList()

    fun getRecordedDataFor32BitPCM(): List<FloatArray> =
        floatArrays.toList()

    fun isRecording(): Boolean =
        audioRecord.state == AudioRecord.RECORDSTATE_RECORDING

    fun hasRecordedAudio(): Boolean =
        shortArrays.isNotEmpty() || floatArrays.isNotEmpty()

    /**
     * 暂停录音
     */
    fun stop() {
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
            return
        }
        audioRecord.stop()
    }

    private fun clearRecordedData() {
        if (audioFormat == AudioRecorderFormat.PCM_16BIT) {
            shortArrays.clear()
        } else {
            floatArrays.clear()
        }
    }

    /**
     * 重置
     */
    fun reset() {
        clearRecordedData()
        byteLength = 0L
    }

    /**
     * 释放资源
     */
    fun release() {
        clearRecordedData()
        byteLength = 0L
        audioRecord.release()
        noiseSuppressor?.release()
        acousticEchoCanceler?.release()
    }

    /**
     * 将录音数据保存成文件
     *
     * @return 输出的文件
     */
    suspend fun saveDataAsPCM(outputPCMFilePath: String): File? {
        if (outputPCMFilePath.isEmpty()) {
            return null
        }
        return withIO {
            val outputPCMFile = File(outputPCMFilePath)
            if (outputPCMFile.exists()) {
                outputPCMFile.delete()
            } else {
                outputPCMFile.parentFile?.mkdirs()
                outputPCMFile.createNewFile()
            }
            FileOutputStream(outputPCMFile).use { fileOutputStream ->
                if (audioFormat == AudioRecorderFormat.PCM_16BIT) {
                    shortArrays.forEach {
                        fileOutputStream.write(convertShortArrayToByteArray(it))
                    }
                } else {
                    floatArrays.forEach {
                        fileOutputStream.write(convertFloatArrayToByteArray(it))
                    }
                }
            }
            outputPCMFile
        }
    }

    private fun convertShortArrayToByteArray(src: ShortArray): ByteArray =
        ByteArray(src.size * 2).apply {
            src.forEachIndexed { index, value: Short ->
                set(index * 2, value.toByte())
                set(index * 2 + 1, (value.toInt() shr 8).toByte())
            }
        }

    private fun convertFloatArrayToByteArray(src: FloatArray): ByteArray =
        convertShortArrayToByteArray(ShortArray(src.size).apply {
            src.forEachIndexed { index, value: Float ->
                set(index, (value * 32768).toInt().toShort())
            }
        })

    class Builder {

        private var minBufferSize: Int = 0
        private lateinit var audioRecord: AudioRecord
        private var audioSource: AudioRecorderSource = AudioRecorderSource.MIC
        private var sampleRateInHz: Int = 44100
        private var audioFormat: AudioRecorderFormat = AudioRecorderFormat.PCM_16BIT
        private var channelConfig: AudioRecorderChannelConfig = AudioRecorderChannelConfig.STEREO
        private var addNoiseSuppressor: Boolean = false
        private var addAcousticEchoCanceler: Boolean = false
        private var addAutomaticGainControl: Boolean = false
        private var noiseSuppressor: NoiseSuppressor? = null
        private var acousticEchoCanceler: AcousticEchoCanceler? = null
        private var automaticGainControl: AutomaticGainControl? = null
        private var listener: AudioRecordListener? = null

        /**
         * 设置音频来源
         */
        fun setAudioSource(audioSource: AudioRecorderSource): Builder {
            this.audioSource = audioSource
            return this
        }

        /**
         * 设置采样率
         */
        fun setSampleRateInHz(sampleRateInHz: Int): Builder {
            this.sampleRateInHz = sampleRateInHz
            return this
        }

        /**
         * 设置音频格式
         */
        fun setAudioFormat(audioFormat: AudioRecorderFormat): Builder {
            this.audioFormat = audioFormat
            return this
        }

        /**
         * 设置声道配置
         */
        fun setChannelConfig(channelConfig: AudioRecorderChannelConfig): Builder {
            this.channelConfig = channelConfig
            return this
        }

        /**
         * 添加噪音抑制器
         */
        fun addNoiseSuppressor(): Builder {
            addNoiseSuppressor = true
            return this
        }

        /**
         * 添加声学回声消除器
         */
        fun addAcousticEchoCanceler(): Builder {
            addAcousticEchoCanceler = true
            return this
        }

        /**
         * 添加自动增益控制
         */
        fun addAutomaticGainControl(): Builder {
            addAutomaticGainControl = true
            return this
        }

        /**
         * 设置录音监听者
         */
        fun setAudioRecordListener(listener: AudioRecordListener): Builder {
            this.listener = listener
            return this
        }

        private fun handleNoiseSuppress(audioSessionId: Int) {
            if (addNoiseSuppressor && NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                noiseSuppressor?.run { enabled = true }
            }
        }

        private fun handleAcousticEchoCancel(audioSessionId: Int) {
            if (addAcousticEchoCanceler && AcousticEchoCanceler.isAvailable()) {
                acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
                acousticEchoCanceler?.run { enabled = true }
            }
        }

        private fun handleAutomaticGainControl(audioSessionId: Int) {
            if (addAutomaticGainControl && AutomaticGainControl.isAvailable()) {
                automaticGainControl = AutomaticGainControl.create(audioSessionId)
                automaticGainControl?.run { enabled = true }
            }
        }

        @SuppressLint("MissingPermission")
        fun build(): AudioRecorder {
            minBufferSize =
                AudioRecord.getMinBufferSize(
                    sampleRateInHz,
                    channelConfig.value,
                    audioFormat.value
                )
            audioRecord = AudioRecord(
                audioSource.value,
                sampleRateInHz,
                channelConfig.value,
                audioFormat.value,
                minBufferSize
            ).apply {
                handleNoiseSuppress(audioSessionId)
                handleAcousticEchoCancel(audioSessionId)
                handleAutomaticGainControl(audioSessionId)
            }
            return AudioRecorder(
                minBufferSize,
                audioRecord,
                sampleRateInHz,
                audioFormat,
                channelConfig,
                noiseSuppressor,
                acousticEchoCanceler,
                listener
            )
        }

    }

    enum class AudioRecorderSource(val value: Int) {

        @Description("麦克风音频源")
        MIC(MediaRecorder.AudioSource.MIC),

    }

    enum class AudioRecorderFormat(val value: Int) {

        @Description("PCM每个采样16位，保证由设备支持")
        PCM_16BIT(AudioFormat.ENCODING_PCM_16BIT),

        @Description("PCM每个采样单精度浮点")
        PCM_FLOAT(AudioFormat.ENCODING_PCM_FLOAT)

    }

    enum class AudioRecorderChannelConfig(val value: Int) {

        @Description("单声道")
        MONO(AudioFormat.CHANNEL_IN_MONO),

        @Description("立体声声道")
        STEREO(AudioFormat.CHANNEL_IN_STEREO)

    }

    interface AudioRecordListener {

        /**
         * 正在录音
         *
         * @param durationInSecond 时长，单位：秒
         */
        fun onRecording(durationInSecond: Long)

    }

    private companion object {
        const val TAG = "AudioRecorder"
    }

}