package com.example.lr4v

import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var tvNowPlaying: TextView
    private lateinit var etUrl: EditText

    private lateinit var btnOpenAudio: Button
    private lateinit var btnOpenVideo: Button
    private lateinit var btnLoadUrl: Button

    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null
    private var currentSourceLabel: String = "Немає завантажених файлів"

    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = false

    companion object {
        private const val KEY_CURRENT_URI = "key_current_uri"
        private const val KEY_CURRENT_SOURCE_LABEL = "key_current_source_label"
        private const val KEY_PLAYBACK_POSITION = "key_playback_position"
        private const val KEY_PLAY_WHEN_READY = "key_play_when_ready"
        private const val KEY_URL_TEXT = "key_url_text"
        private const val KEY_NOW_PLAYING = "key_now_playing"
    }

    private val openAudioLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                loadMedia(uri, "Аудіофайл", autoPlay = true)
            } else {
                showToast("Аудіофайл не вибрано")
            }
        }

    private val openVideoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                loadMedia(uri, "Відеофайл", autoPlay = true)
            } else {
                showToast("Відеофайл не вибрано")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        restoreUiState(savedInstanceState)
        initPlayer()
        setupListeners()

        if (savedInstanceState != null) {
            restorePlayerState()
        } else {
            updateNowPlaying("Nothing loaded")
        }
    }

    private fun initViews() {
        playerView = findViewById(R.id.playerView)
        tvNowPlaying = findViewById(R.id.tvNowPlaying)
        etUrl = findViewById(R.id.etUrl)

        btnOpenAudio = findViewById(R.id.btnOpenAudio)
        btnOpenVideo = findViewById(R.id.btnOpenVideo)
        btnLoadUrl = findViewById(R.id.btnLoadUrl)

        btnPlay = findViewById(R.id.btnPlay)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)
    }

    private fun restoreUiState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return

        val savedUriString = savedInstanceState.getString(KEY_CURRENT_URI)
        currentUri = if (savedUriString != null) Uri.parse(savedUriString) else null

        currentSourceLabel = savedInstanceState.getString(
            KEY_CURRENT_SOURCE_LABEL,
            "Nothing loaded"
        )

        playbackPosition = savedInstanceState.getLong(KEY_PLAYBACK_POSITION, 0L)
        playWhenReady = savedInstanceState.getBoolean(KEY_PLAY_WHEN_READY, false)

        etUrl.setText(savedInstanceState.getString(KEY_URL_TEXT, ""))

        val nowPlayingText = savedInstanceState.getString(KEY_NOW_PLAYING, "Nothing loaded")
        updateNowPlaying(nowPlayingText)
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> updateNowPlaying(currentSourceLabel)
                    Player.STATE_BUFFERING -> updateNowPlaying("$currentSourceLabel • Буферизація...")
                    Player.STATE_READY -> {
                        if (!player!!.isPlaying) {
                            if (playbackPosition == 0L) {
                                updateNowPlaying("$currentSourceLabel • Готово")
                            } else {
                                updateNowPlaying("$currentSourceLabel • Пауза")
                            }
                        }
                    }
                    Player.STATE_ENDED -> updateNowPlaying("$currentSourceLabel • Завершено")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNowPlaying(
                    if (isPlaying) {
                        "$currentSourceLabel • Відтворення"
                    } else {
                        if (currentUri == null) "Немає завантажених файлів" else "$currentSourceLabel • Пауза"
                    }
                )
            }
        })
    }

    private fun restorePlayerState() {
        val uri = currentUri ?: return

        val mediaItem = MediaItem.fromUri(uri)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            seekTo(playbackPosition)
            this.playWhenReady = this@MainActivity.playWhenReady
        }
    }

    private fun setupListeners() {
        btnOpenAudio.setOnClickListener {
            openAudioLauncher.launch(arrayOf("audio/*"))
        }

        btnOpenVideo.setOnClickListener {
            openVideoLauncher.launch(arrayOf("video/*"))
        }

        btnLoadUrl.setOnClickListener {
            loadFromUrl()
        }

        btnPlay.setOnClickListener {
            playCurrent()
        }

        btnPause.setOnClickListener {
            pauseCurrent()
        }

        btnStop.setOnClickListener {
            stopCurrent()
        }
    }

    private fun loadMedia(uri: Uri, sourceLabel: String, autoPlay: Boolean) {
        currentUri = uri
        currentSourceLabel = sourceLabel
        playbackPosition = 0L
        playWhenReady = autoPlay

        val mediaItem = MediaItem.fromUri(uri)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            seekTo(0)
            this.playWhenReady = autoPlay
        }

        updateNowPlaying(
            if (autoPlay) "$sourceLabel • Відтворення" else "$sourceLabel • Готово"
        )
        showToast("$sourceLabel завантажено")
    }

    private fun loadFromUrl() {
        val url = etUrl.text.toString().trim()

        if (url.isEmpty()) {
            showToast("Введіть посилання на медіа")
            return
        }

        if (!URLUtil.isValidUrl(url)) {
            showToast("Некоректне посилання")
            return
        }

        val uri = Uri.parse(url)
        loadMedia(uri, "Мережевий потік", autoPlay = true)
        showToast("Потік завантажено")
    }

    private fun playCurrent() {
        if (currentUri == null) {
            showToast("Немає завантажених файлів")
            return
        }

        // Змінюємо порядок викликів play і playWhenReady
        player?.play()
        player?.playWhenReady = true
        playWhenReady = true
        updateNowPlaying("$currentSourceLabel • Відтворення")
    }

    private fun pauseCurrent() {
        if (currentUri == null) {
            showToast("Немає завантажених файлів")
            return
        }

        // Змінюємо місцями оновлення позиції і паузу
        playbackPosition = player?.currentPosition ?: playbackPosition
        player?.pause()
        playWhenReady = false
        updateNowPlaying("$currentSourceLabel • Пауза")
    }

    private fun stopCurrent() {
        if (currentUri == null) {
            showToast("Немає завантажених файлів")
            return
        }

        // Змінюємо місцями скидання позиції і паузу
        playbackPosition = 0L
        player?.seekTo(0)
        player?.pause()
        playWhenReady = false
        updateNowPlaying("$currentSourceLabel • Зупинено")
    }

    private fun updateNowPlaying(text: String) {
        tvNowPlaying.text = text
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        playbackPosition = player?.currentPosition ?: playbackPosition
        playWhenReady = player?.playWhenReady ?: playWhenReady

        outState.putString(KEY_CURRENT_URI, currentUri?.toString())
        outState.putString(KEY_CURRENT_SOURCE_LABEL, currentSourceLabel)
        outState.putLong(KEY_PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
        outState.putString(KEY_URL_TEXT, etUrl.text.toString())
        outState.putString(KEY_NOW_PLAYING, tvNowPlaying.text.toString())
    }

    override fun onStart() {
        super.onStart()
        playerView.onResume()
    }

    override fun onStop() {
        super.onStop()

        playbackPosition = player?.currentPosition ?: playbackPosition
        playWhenReady = player?.playWhenReady ?: playWhenReady

        playerView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerView.player = null
        player?.release()
        player = null
    }
}