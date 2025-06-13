package se2.hanabi.app.Services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import se2.hanabi.app.R

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.theme)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } else if (!(mediaPlayer?.isPlaying ?: false)) {
            mediaPlayer?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
