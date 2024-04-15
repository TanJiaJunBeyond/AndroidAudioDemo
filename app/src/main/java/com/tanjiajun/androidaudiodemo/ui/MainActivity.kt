package com.tanjiajun.androidaudiodemo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tanjiajun.androidaudiodemo.R
import com.tanjiajun.androidaudiodemo.ui.theme.Black
import com.tanjiajun.androidaudiodemo.ui.theme.Purple40
import com.tanjiajun.androidaudiodemo.ui.theme.White
import com.tanjiajun.androidaudiodemo.utils.LAMEUtils
import com.tanjiajun.androidaudiodemo.utils.toastShort

/**
 * Created by TanJiaJun on 2023/12/13.
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults: Map<String, Boolean> ->
            when {
                grantResults[Manifest.permission.READ_EXTERNAL_STORAGE] == false ->
                    toastShort(getString(R.string.need_read_external_storage_permission))

                grantResults[Manifest.permission.READ_MEDIA_AUDIO] == false ->
                    toastShort(getString(R.string.need_read_media_audio_permission))

                grantResults[Manifest.permission.RECORD_AUDIO] == false ->
                    toastShort(getString(R.string.need_record_audio_permission))

                else ->
                    navigateToAudioEditingPage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentView()
        }
    }

    @Composable
    private fun ContentView() {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBox, lameVersionText, enterPageButton) = createRefs()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(Purple40)
                    .constrainAs(topBox) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = packageManager.getApplicationLabel(applicationInfo).toString(),
                    fontSize = 18.sp,
                    color = White
                )
            }
            Text(
                modifier = Modifier.constrainAs(lameVersionText) {
                    start.linkTo(parent.start)
                    top.linkTo(topBox.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(enterPageButton.top)
                },
                text = LAMEUtils.getLameVersion(),
                fontSize = 16.sp,
                color = Black
            )
            Button(
                modifier = Modifier.constrainAs(enterPageButton) {
                    start.linkTo(parent.start)
                    top.linkTo(lameVersionText.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
                onClick = {
                    requestPermission()
                }) {
                Text(text = getString(R.string.enter_audio_editing_page))
            }
            createVerticalChain(lameVersionText, enterPageButton, chainStyle = ChainStyle.Packed)
        }
    }

    private fun checkPermissions(permissions: Array<String>): List<String> =
        mutableListOf<String>().apply {
            permissions
                .filter {
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        it
                    ) == PackageManager.PERMISSION_DENIED
                }
                .forEach { add(it) }
        }

    private fun requestPermission() {
        val permissions: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.RECORD_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                )
            }
        when {
            checkPermissions(permissions).isEmpty() ->
                navigateToAudioEditingPage()

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ->
                toastShort(getString(R.string.need_read_external_storage_permission))

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) ->
                toastShort(getString(R.string.need_read_media_audio_permission))

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) ->
                toastShort(getString(R.string.need_record_audio_permission))

            else ->
                requestPermissionsLauncher.launch(permissions)
        }
    }

    private fun navigateToAudioEditingPage() {
        startActivity(Intent(this, AudioEditingActivity::class.java))
    }

}