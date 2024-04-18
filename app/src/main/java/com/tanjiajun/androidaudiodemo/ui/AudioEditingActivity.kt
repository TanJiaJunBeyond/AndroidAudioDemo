package com.tanjiajun.androidaudiodemo.ui

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tanjiajun.androidaudiodemo.R
import com.tanjiajun.androidaudiodemo.ui.theme.Black50Transparency
import com.tanjiajun.androidaudiodemo.ui.theme.White
import com.tanjiajun.androidaudiodemo.utils.otherwise
import com.tanjiajun.androidaudiodemo.utils.yes
import com.tanjiajun.androidaudiodemo.viewmodel.AudioEditingViewModel

/**
 * Created by TanJiaJun on 2024/3/20.
 */
class AudioEditingActivity : ComponentActivity() {

    private lateinit var viewModel: AudioEditingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentView()
        }
    }

    @Composable
    private fun ContentView() {
        viewModel = viewModel(factory = AudioEditingViewModel.provideFactory())
        with(viewModel) {
            setSavingText(getString(R.string.saving))
            setConvertingText(getString(R.string.converting))
            setEncodingText(getString(R.string.encoding))
        }
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 10.dp,
                end = 16.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioRecordButton()
                Spacer10dp()
                AudioRecordingDurationText()
            }
            Spacer10dp()
            AudioPlayButton()
            Spacer10dp()
            SaveAsPCMFileButton()
            Spacer5dp()
            AudioPCMFileAbsolutePathText()
            Spacer10dp()
            Row {
                ConvertToWAVFileButton()
                PlayWAVFileButton()
            }
            Spacer5dp()
            AudioWAVFileAbsolutePathText()
            Spacer10dp()
            Row {
                EncodeToMP3FileButton()
                PlayMP3FileButton()
            }
            Spacer5dp()
            AudioMP3FileAbsolutePathText()
        }
        LoadingView()
    }

    @Composable
    private fun Spacer5dp() {
        Spacer(modifier = Modifier.size(5.dp))
    }

    @Composable
    private fun Spacer10dp() {
        Spacer(modifier = Modifier.size(10.dp))
    }

    @Composable
    private fun LoadingView() {
        val loadingText: String by viewModel.loadingText.collectAsState()
        if (loadingText.isEmpty()) {
            return
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black50Transparency),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer10dp()
                Text(
                    text = loadingText,
                    fontSize = 18.sp,
                    color = White
                )
            }
        }
    }

    @Composable
    private fun AudioRecordButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        Button(onClick = {
            if (isAudioRecording) {
                viewModel.audioStopRecord()
            } else {
                viewModel.audioRecord()
            }
        }) {
            Text(text = isAudioRecording
                .yes { getString(R.string.stop_recording_audio) }
                .otherwise { getString(R.string.audio_record) })
        }
    }

    @Composable
    private fun AudioRecordingDurationText() {
        val audioRecordingDuration: String by viewModel.audioRecordingTime.collectAsState()
        Text(
            text = audioRecordingDuration,
            fontSize = 18.sp
        )
    }

    @Composable
    private fun AudioPlayButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val hasRecordedAudio: Boolean by viewModel.hasRecordedAudio.collectAsState()
        if (!hasRecordedAudio) {
            return
        }
        val isPlayingPCMAudio: Boolean by viewModel.isPlayingPCMAudio.collectAsState()
        Button(onClick = {
            if (isPlayingPCMAudio) viewModel.stopPlayingPCMAudio() else viewModel.playPCMAudio()
        }) {
            Text(text = getString(if (isPlayingPCMAudio) R.string.stop_playing_audio else R.string.audio_play))
        }
    }

    @Composable
    private fun SaveAsPCMFileButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val hasRecordedAudio: Boolean by viewModel.hasRecordedAudio.collectAsState()
        if (!hasRecordedAudio) {
            return
        }
        val audioPCMFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioPCMFileAbsolutePath.isNotEmpty()) {
            return
        }
        Button(onClick = {
            val outputPCMFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?.absolutePath + "/Audio/audio.pcm"
            viewModel.saveAsPCMFile(outputPCMFilePath)
        }) {
            Text(text = getString(R.string.save_as_pcm_file))
        }
    }

    @Composable
    private fun AudioPCMFileAbsolutePathText() {
        val audioFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioFileAbsolutePath.isEmpty()) {
            return
        }
        Text(text = "${getString(R.string.pcm_file_absolute_path)}$audioFileAbsolutePath")
    }

    @Composable
    private fun ConvertToWAVFileButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val audioPCMFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioPCMFileAbsolutePath.isEmpty()) {
            return
        }
        val audioWAVFileAbsolutePath: String by viewModel.audioWAVFileAbsolutePath.collectAsState()
        if (audioWAVFileAbsolutePath.isNotEmpty()) {
            return
        }
        val isPlayingWAVFile: Boolean by viewModel.isPlayingWAVFile.collectAsState()
        if (isPlayingWAVFile) {
            return
        }
        Button(onClick = {
            val outputWAVFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?.absolutePath + "/Audio/audio.wav"
            viewModel.convertToWAVFile(outputWAVFilePath)
        }) {
            Text(text = getString(R.string.convert_to_wav_file))
        }
    }

    @Composable
    private fun PlayWAVFileButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val audioPCMFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioPCMFileAbsolutePath.isEmpty()) {
            return
        }
        val audioWAVFileAbsolutePath: String by viewModel.audioWAVFileAbsolutePath.collectAsState()
        if (audioWAVFileAbsolutePath.isEmpty()) {
            return
        }
        val isPlayingWAVFile: Boolean by viewModel.isPlayingWAVFile.collectAsState()
        Button(onClick = {
            if (isPlayingWAVFile) viewModel.stopPlayingWAVFile() else viewModel.playWAVFile()
        }) {
            Text(text = getString(if (isPlayingWAVFile) R.string.stop_play_wav_file else R.string.play_wav_file))
        }
    }

    @Composable
    private fun AudioWAVFileAbsolutePathText() {
        val audioWAVFileAbsolutePath: String by viewModel.audioWAVFileAbsolutePath.collectAsState()
        if (audioWAVFileAbsolutePath.isEmpty()) {
            return
        }
        Text(text = "${getString(R.string.wav_file_absolute_path)}$audioWAVFileAbsolutePath")
    }

    @Composable
    private fun EncodeToMP3FileButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val audioPCMFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioPCMFileAbsolutePath.isEmpty()) {
            return
        }
        val audioMP3FileAbsolutePath: String by viewModel.audioMP3FileAbsolutePath.collectAsState()
        if (audioMP3FileAbsolutePath.isNotEmpty()) {
            return
        }
        Button(onClick = {
            val outputMP3FilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?.absolutePath + "/Audio/audio.mp3"
            viewModel.encodeToMP3File(outputMP3FilePath)
        }) {
            Text(text = getString(R.string.encode_to_mp3_file))
        }
    }

    @Composable
    private fun PlayMP3FileButton() {
        val isAudioRecording: Boolean by viewModel.isAudioRecording.collectAsState()
        if (isAudioRecording) {
            return
        }
        val audioPCMFileAbsolutePath: String by viewModel.audioPCMFileAbsolutePath.collectAsState()
        if (audioPCMFileAbsolutePath.isEmpty()) {
            return
        }
        val audioMP3FileAbsolutePath: String by viewModel.audioMP3FileAbsolutePath.collectAsState()
        if (audioMP3FileAbsolutePath.isEmpty()) {
            return
        }
        val isPlayingMP3File: Boolean by viewModel.isPlayingMP3File.collectAsState()
        Button(onClick = {
            if (isPlayingMP3File) viewModel.stopPlayingMP3File() else viewModel.playMP3File()
        }) {
            Text(text = getString(if (isPlayingMP3File) R.string.stop_play_mp3_file else R.string.play_mp3_file))
        }
    }

    @Composable
    private fun AudioMP3FileAbsolutePathText() {
        val audioMP3FileAbsolutePath: String by viewModel.audioMP3FileAbsolutePath.collectAsState()
        if (audioMP3FileAbsolutePath.isEmpty()) {
            return
        }
        Text(text = "${getString(R.string.mp3_file_absolute_path)}$audioMP3FileAbsolutePath")
    }

}