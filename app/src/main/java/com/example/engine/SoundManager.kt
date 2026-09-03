package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true

    fun playDiceRoll() {
        if (!isSoundEnabled) return
        scope.launch {
            // Rapid series of click / rattling pulses
            playNoiseClicks(count = 7, intervalMs = 35)
        }
    }

    fun playTokenMove() {
        if (!isSoundEnabled) return
        scope.launch {
            // Soft wood pop tone
            playTone(freq = 520.0, durationMs = 60, amplitude = 0.6)
        }
    }

    fun playTokenCapture() {
        if (!isSoundEnabled) return
        scope.launch {
            // Descending slide / boom
            playSweep(startFreq = 800.0, endFreq = 200.0, durationMs = 220)
        }
    }

    fun playTokenEnterGoal() {
        if (!isSoundEnabled) return
        scope.launch {
            // Joyful ascending ding
            playTone(freq = 659.25, durationMs = 80, amplitude = 0.5) // E5
            playTone(freq = 880.0, durationMs = 120, amplitude = 0.6)  // A5
        }
    }

    fun playVictoryFanfare() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (note in notes) {
                playTone(freq = note, durationMs = 140, amplitude = 0.7)
            }
        }
    }

    fun playSixRolled() {
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 700.0, durationMs = 70, amplitude = 0.6)
            playTone(freq = 1050.0, durationMs = 110, amplitude = 0.7)
        }
    }

    private fun playTone(freq: Double, durationMs: Int, amplitude: Double = 0.5) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                // Add decay envelope
                val envelope = (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0)
                val sample = sin(2.0 * Math.PI * freq * time) * amplitude * envelope
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio generation failures gracefully
        }
    }

    private fun playSweep(startFreq: Double, endFreq: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val time = i.toDouble() / sampleRate
                val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
                val sample = sin(2.0 * Math.PI * currentFreq * time) * 0.6 * envelope
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {}
    }

    private fun playNoiseClicks(count: Int, intervalMs: Long) {
        for (k in 0 until count) {
            val f = 400.0 + (k * 60)
            playTone(freq = f, durationMs = 25, amplitude = 0.4)
            try {
                Thread.sleep(intervalMs)
            } catch (_: Exception) {}
        }
    }
}
