package com.android.veera

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {

    private lateinit var telecomManager: TelecomManager
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setContent {
            PhoneCallUI(
                onAnswer = {
                    answerCall()
                    Log.d("MainActivity", "Answer click")

                           },
                onReject = { rejectCall() },
                onToggleSpeaker = { toggleSpeaker() },
                onToggleMic = { toggleMic() }
            )
        }
    }

    private fun answerCall() {
        try {
            telecomManager.acceptRingingCall()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun rejectCall() {
        try {
            telecomManager.endCall()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun toggleSpeaker() {
        audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
    }

    private fun toggleMic() {
        audioManager.isMicrophoneMute = !audioManager.isMicrophoneMute
    }

}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Phone Call UI Preview"
)
@Composable
fun PreviewPhoneCallUI() {
    PhoneCallUI(
        onAnswer = {},
        onReject = {},
        onToggleSpeaker = {},
        onToggleMic = {}
    )
}

@Composable
fun PhoneCallUI(
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMic: () -> Unit
) {
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isMicMuted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Incoming Call",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "+91 98765 43210",
                color = Color.LightGray,
                fontSize = 20.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(
                    onClick = onAnswer,
                    colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
                ) {
                    Text("Answer")
                }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(Color(0xFFF44336))
                ) {
                    Text("Reject")
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Button(onClick = {
                    isSpeakerOn = !isSpeakerOn
                    onToggleSpeaker()
                }) {
                    Text(if (isSpeakerOn) "Speaker ON" else "Speaker OFF")
                }
                Button(onClick = {
                    isMicMuted = !isMicMuted
                    onToggleMic()
                }) {
                    Text(if (isMicMuted) "Mic OFF" else "Mic ON")
                }
            }
        }
    }
}
