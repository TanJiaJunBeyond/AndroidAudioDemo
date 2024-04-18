package com.tanjiajun.androidaudiodemo.viewmodel

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tanjiajun.androidaudiodemo.utils.AudioFormatConverter
import com.tanjiajun.androidaudiodemo.utils.AudioPCMPlayer
import com.tanjiajun.androidaudiodemo.utils.AudioPlayer
import com.tanjiajun.androidaudiodemo.utils.AudioRecorder
import com.tanjiajun.androidaudiodemo.utils.AudioUtils
import com.tanjiajun.androidaudiodemo.utils.LAMEUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Created by TanJiaJun on 2024/3/21.
 */
class AudioEditingViewModel : ViewModel() {

    private val _loadingText = MutableStateFlow("")
    val loadingText: StateFlow<String> = _loadingText.asStateFlow()

    private val _isAudioRecording = MutableStateFlow(false)
    val isAudioRecording: StateFlow<Boolean> = _isAudioRecording.asStateFlow()

    private val _audioRecordingTime = MutableStateFlow("00:00")
    val audioRecordingTime: StateFlow<String> = _audioRecordingTime.asStateFlow()

    private val _hasRecordedAudio = MutableStateFlow(false)
    val hasRecordedAudio: StateFlow<Boolean> = _hasRecordedAudio.asStateFlow()

    private val _isPlayingPCMAudio = MutableStateFlow(false)
    val isPlayingPCMAudio: StateFlow<Boolean> = _isPlayingPCMAudio.asStateFlow()

    private val _audioPCMFileAbsolutePath = MutableStateFlow("")
    val audioPCMFileAbsolutePath: StateFlow<String> = _audioPCMFileAbsolutePath.asStateFlow()

    private val _audioWAVFileAbsolutePath = MutableStateFlow("")
    val audioWAVFileAbsolutePath: StateFlow<String> = _audioWAVFileAbsolutePath.asStateFlow()

    private val _isPlayingWAVFile = MutableStateFlow(false)
    val isPlayingWAVFile: StateFlow<Boolean> = _isPlayingWAVFile.asStateFlow()

    private val _audioMP3FileAbsolutePath = MutableStateFlow("")
    val audioMP3FileAbsolutePath: StateFlow<String> = _audioMP3FileAbsolutePath.asStateFlow()

    private val _isPlayingMP3File = MutableStateFlow(false)
    val isPlayingMP3File: StateFlow<Boolean> = _isPlayingMP3File.asStateFlow()

    private var savingText: String = ""
    private var convertingText: String = ""
    private var encodingText: String = ""

    private val sampleRateInHz = 44100

    private val audioRecorderFormat = AudioRecorder.AudioRecorderFormat.PCM_16BIT
    private val audioRecorderChannelConfig = AudioRecorder.AudioRecorderChannelConfig.STEREO

    private val audioPCMPlayerFormat = AudioPCMPlayer.AudioPCMPlayerFormat.PCM_16BIT
    private val audioPCMPlayerChannelConfig = AudioPCMPlayer.AudioPCMPlayerChannelConfig.STEREO

    private val audioRecorder: AudioRecorder by lazy {
        AudioRecorder.Builder()
            .setAudioSource(AudioRecorder.AudioRecorderSource.MIC)
            .setSampleRateInHz(sampleRateInHz)
            .setAudioFormat(audioRecorderFormat)
            .setChannelConfig(audioRecorderChannelConfig)
            .addNoiseSuppressor()
            .addAcousticEchoCanceler()
            .addAutomaticGainControl()
            .setAudioRecordListener(object : AudioRecorder.AudioRecordListener {
                override fun onRecording(durationInSecond: Long) {
                    _audioRecordingTime.value = DateUtils.formatElapsedTime(durationInSecond)
                }
            })
            .build()
    }

    private val audioPCMPlayer: AudioPCMPlayer by lazy {
        AudioPCMPlayer.Builder()
            .setSampleRateInHz(sampleRateInHz)
            .setAudioFormat(audioPCMPlayerFormat)
            .setChannelConfig(audioPCMPlayerChannelConfig)
            .build()
    }

    private val audioPlayer: AudioPlayer by lazy { AudioPlayer() }

    fun setSavingText(savingText: String) {
        this.savingText = savingText
    }

    fun setConvertingText(convertingText: String) {
        this.convertingText = convertingText
    }

    fun setEncodingText(encodingText: String) {
        this.encodingText = encodingText
    }

    fun audioRecord() {
        viewModelScope.launch {
            audioPCMPlayer.stop()
            _isPlayingPCMAudio.value = false

            _audioPCMFileAbsolutePath.value = ""
            _audioWAVFileAbsolutePath.value = ""
            _isPlayingWAVFile.value = false
            _audioMP3FileAbsolutePath.value = ""
            _isPlayingMP3File.value = false
            audioPlayer.stop()

            _isAudioRecording.value = true
            audioRecorder.record()
        }
    }

    fun audioStopRecord() {
        _isAudioRecording.value = false
        _hasRecordedAudio.value = audioRecorder.hasRecordedAudio()
        audioRecorder.stop()
    }

    fun playPCMAudio() {
        val recordedDataFor16BitPCM: List<ShortArray> = audioRecorder.getRecordedDataFor16BitPCM()
        val recordedDataFor32BitPCM: List<FloatArray> = audioRecorder.getRecordedDataFor32BitPCM()
        if (recordedDataFor16BitPCM.isEmpty() && recordedDataFor32BitPCM.isEmpty()) {
            return
        }
        viewModelScope.launch {
            audioPlayer.stop()
            _isPlayingWAVFile.value = false
            _isPlayingMP3File.value = false

            _isPlayingPCMAudio.value = true
            if (audioPCMPlayerFormat == AudioPCMPlayer.AudioPCMPlayerFormat.PCM_16BIT) {
                audioPCMPlayer.play16BitPCMAudio(
                    audioData = recordedDataFor16BitPCM,
                    listener = object : AudioPCMPlayer.AudioPCMPlayListener {
                        override fun onCompletion() {
                            _isPlayingPCMAudio.value = false
                        }
                    })
            } else {
                audioPCMPlayer.play32BitPCMAudio(
                    audioData = recordedDataFor32BitPCM,
                    listener = object : AudioPCMPlayer.AudioPCMPlayListener {
                        override fun onCompletion() {
                            _isPlayingPCMAudio.value = false
                        }
                    }
                )
            }
        }
    }

    fun stopPlayingPCMAudio() {
        audioPCMPlayer.stop()
        _isPlayingPCMAudio.value = false
    }

    fun saveAsPCMFile(outputPCMFilePath: String) {
        viewModelScope.launch {
            _loadingText.value = savingText
            _audioPCMFileAbsolutePath.value =
                audioRecorder.saveDataAsPCM(outputPCMFilePath)?.absolutePath ?: ""
            _loadingText.value = ""
        }
    }

    fun convertToWAVFile(outputWAVFilePath: String) {
        viewModelScope.launch {
            _loadingText.value = convertingText
            _audioWAVFileAbsolutePath.value =
                AudioFormatConverter.convertPCMToWAV(
                    inputPCMFilePath = _audioPCMFileAbsolutePath.value,
                    outputWAVFilePath = outputWAVFilePath,
                    sampleRateInHz = sampleRateInHz,
                    bitDepth = AudioUtils.getBitDepthByAudioFormat(audioRecorderFormat.value),
                    channelCount = AudioUtils.getChannelCountByChannelConfig(
                        audioRecorderChannelConfig.value
                    )
                )?.absolutePath ?: ""
            _loadingText.value = ""
        }
    }

    fun playWAVFile() {
        viewModelScope.launch {
            audioPCMPlayer.stop()
            _isPlayingPCMAudio.value = false

            _isPlayingMP3File.value = false
            audioPlayer.play(_audioWAVFileAbsolutePath.value,
                object : AudioPlayer.AudioPlayerListener {
                    override fun onCompletion() {
                        _isPlayingWAVFile.value = false
                    }
                })
            _isPlayingWAVFile.value = true
        }
    }

    fun stopPlayingWAVFile() {
        audioPlayer.stop()
        _isPlayingWAVFile.value = false
    }

    fun encodeToMP3File(outputMP3FilePath: String) {
        viewModelScope.launch {
            _loadingText.value = encodingText
            _audioMP3FileAbsolutePath.value =
                AudioFormatConverter.encodePCMToMP3(
                    inputPCMFilePath = _audioPCMFileAbsolutePath.value,
                    outputMP3FilePath = outputMP3FilePath,
                    sampleRateInHz = sampleRateInHz,
                    bitDepth = AudioUtils.getBitDepthByAudioFormat(audioRecorderFormat.value),
                    channelCount = AudioUtils.getChannelCountByChannelConfig(
                        audioRecorderChannelConfig.value
                    )
                )?.absolutePath ?: ""
            _loadingText.value = ""
        }
    }

    fun playMP3File() {
        viewModelScope.launch {
            audioPCMPlayer.stop()
            _isPlayingPCMAudio.value = false

            _isPlayingWAVFile.value = false
            audioPlayer.play(_audioMP3FileAbsolutePath.value,
                object : AudioPlayer.AudioPlayerListener {
                    override fun onCompletion() {
                        _isPlayingMP3File.value = false
                    }
                })
            _isPlayingMP3File.value = true
        }
    }

    fun stopPlayingMP3File() {
        audioPlayer.stop()
        _isPlayingMP3File.value = false
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
        audioPCMPlayer.release()
        LAMEUtils.destroy()
        audioPlayer.release()
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AudioEditingViewModel() as T
            }
        }
    }

}