package com.example.quran.ui

import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.quran.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import android.os.Handler
import android.os.Looper

class MiniPlayerFragment : BottomSheetDialogFragment() {

    private lateinit var btnClose: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var audioFile: File? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (isPlaying) {
                    seekBar.progress = player.currentPosition
                    tvCurrentTime.text = formatTime(player.currentPosition)
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mini_audio_player, container, false)

        btnClose = view.findViewById(R.id.btnClose)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        seekBar = view.findViewById(R.id.audioSeekBar)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)

        btnClose.setOnClickListener { closePlayer() }
        btnPlayPause.setOnClickListener { togglePlayPause() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
                if (isPlaying) {
                    handler.post(updateSeekBarRunnable)
                }
            }
        })

        return view
    }

    fun playAudio(surahId: Int, ayatId: Int) {
        val audioUrl = "https://everyayah.com/data/AbdulSamad_64kbps_QuranExplorer.Com/${"%03d".format(surahId)}${"%03d".format(ayatId)}.mp3"
        streamAudio(audioUrl)
    }

    private fun streamAudio(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(url)  // Set the URL as the data source for streaming
                    prepareAsync()  // Prepare asynchronously to avoid blocking the UI thread
                    setOnPreparedListener {
                        start()
                        this@MiniPlayerFragment.isPlaying = true
                        btnPlayPause.setImageResource(R.drawable.baseline_pause_circle_24)
                        tvTotalTime.text = formatTime(duration)
                        seekBar.max = duration
                        seekBar.progress = currentPosition
                        handler.post(updateSeekBarRunnable)
                    }
                    setOnErrorListener { _, _, _ ->
                        Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to stream audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (isPlaying) {
                it.pause()
                isPlaying = false
                btnPlayPause.setImageResource(R.drawable.baseline_play_circle_24)
                handler.removeCallbacks(updateSeekBarRunnable)
            } else {
                it.start()
                isPlaying = true
                btnPlayPause.setImageResource(R.drawable.baseline_pause_circle_24)
                handler.post(updateSeekBarRunnable)
            }
        }
    }

    private fun closePlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        isPlaying = false
        handler.removeCallbacks(updateSeekBarRunnable)
        audioFile?.delete() // Delete the downloaded audio file
        audioFile = null
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / (1000 * 60))
        val seconds = (milliseconds % (1000 * 60) / 1000)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        closePlayer()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        closePlayer()
        mediaPlayer = null
    }
}

