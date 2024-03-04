package com.tanjiajun.androidaudiodemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.tanjiajun.androidaudiodemo.ui.theme.Black
import com.tanjiajun.androidaudiodemo.ui.theme.Purple40
import com.tanjiajun.androidaudiodemo.ui.theme.White

/**
 * Created by TanJiaJun on 2023/12/13.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentView()
        }
    }

    @Composable
    fun ContentView() {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBox, lameVersionText) = createRefs()
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
                    bottom.linkTo(parent.bottom)
                },
                text = getLameVersion(),
                fontSize = 16.sp,
                color = Black
            )
        }
    }

    /**
     * 获取当前LAME版本
     *
     * @return 当前LAME版本
     */
    private external fun getLameVersion(): String

    companion object {
        init {
            System.loadLibrary("androidaudiodemo")
        }
    }

}