package com.veera.core.telephony.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFileName: String? = null

    fun startRecording(phoneNumber: String): Boolean {
        if (isRecording) return false

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeNumber = phoneNumber.replace(Regex("[^a-zA-Z0-9]"), "")
        val fileName = "Call_${safeNumber}_$timestamp.m4a"

        val sourcesToTry = listOf(
            MediaRecorder.AudioSource.VOICE_CALL,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC
        )

        var startedSuccessfully = false

        for (source in sourcesToTry) {
            var tempRecorder: MediaRecorder? = null
            try {
                Log.d("CallRecorder", "Attempting call recording using audio source: $source")
                tempRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }

                tempRecorder.apply {
                    setAudioSource(source)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val resolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/CallAppRecords")
                        }
                        
                        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            val pfd = resolver.openFileDescriptor(uri, "w")
                            if (pfd != null) {
                                setOutputFile(pfd.fileDescriptor)
                            } else {
                                throw Exception("Failed to open file descriptor")
                            }
                        } else {
                            throw Exception("Failed to create MediaStore entry")
                        }
                    } else {
                        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "CallAppRecords")
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }
                        val file = File(dir, fileName)
                        setOutputFile(file.absolutePath)
                    }

                    prepare()
                    start()
                }

                mediaRecorder = tempRecorder
                isRecording = true
                currentFileName = fileName
                startedSuccessfully = true
                Log.i("CallRecorder", "Successfully started call recording using audio source: $source")
                break
            } catch (e: Exception) {
                Log.w("CallRecorder", "Failed to start call recording with source $source: ${e.message}", e)
                try {
                    tempRecorder?.release()
                } catch (ex: Exception) {
                    Log.e("CallRecorder", "Error releasing failed recorder instance: ${ex.message}", ex)
                }
            }
        }

        if (!startedSuccessfully) {
            Log.e("CallRecorder", "All attempted audio sources for call recording failed.")
            isRecording = false
            mediaRecorder = null
            currentFileName = null
        }

        return startedSuccessfully
    }

    fun stopRecording(): Result<String> {
        if (!isRecording) return Result.failure(Exception("Not recording"))
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            val savedName = currentFileName ?: "Unknown"
            Result.success(savedName)
        } catch (e: Exception) {
            Log.e("CallRecorder", "Error stopping recorder", e)
            Result.failure(e)
        } finally {
            mediaRecorder = null
            isRecording = false
            currentFileName = null
        }
    }
    
    fun isCurrentlyRecording() = isRecording
}
