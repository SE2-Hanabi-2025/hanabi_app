package se2.hanabi.app.Services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import se2.hanabi.app.R

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand called")
        if (mediaPlayer == null) {
            Log.d("MusicService", "Creating MediaPlayer")
            mediaPlayer = MediaPlayer.create(this, R.raw.theme)
            if (mediaPlayer == null) {
                Log.e("MusicService", "Failed to create MediaPlayer. Resource missing or invalid.")
            } else {
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
                Log.d("MusicService", "MediaPlayer started")
            }
        } else if (!(mediaPlayer?.isPlaying ?: false)) {
            Log.d("MusicService", "Restarting MediaPlayer")
            mediaPlayer?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("MusicService", "onDestroy called")
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
