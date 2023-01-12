/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer

import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerNotificationManager
import com.example.exoplayer.databinding.ActivityPlayerBinding


/**
 * A fullscreen activity to play audio or video streams.
 */
class PlayerActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private var exoPlayer: ExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val notificationId = 1234

    private val mediaDescriptionAdapter: PlayerNotificationManager.MediaDescriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence =
                "Prueba reproductor de audio"

            override fun createCurrentContentIntent(player: Player): PendingIntent {
                val intent = Intent(this@PlayerActivity, PlayerActivity::class.java)
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                return PendingIntent.getActivity(
                    this@PlayerActivity,
                    (System.currentTimeMillis() / 1000).toInt(),
                    intent,
                    flags
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence =
                "Este reproductor de audio funciona en segundo plano"

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        val trackSelector: TrackSelector = DefaultTrackSelector(this)

        val exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        viewBinding.playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
        exoPlayer.setMediaItem(mediaItem)

        playerNotificationManager =
            PlayerNotificationManager.Builder(this, notificationId, "AUDIO_CHANNEL_ID")
                .setChannelNameResourceId(R.string.app_name)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setChannelImportance(IMPORTANCE_HIGH)
                    }
                }
                .setMediaDescriptionAdapter(mediaDescriptionAdapter).build()
        playerNotificationManager?.setPlayer(exoPlayer)
        exoPlayer.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerNotificationManager?.setPlayer(null)
        exoPlayer?.let {
            it.release()
            exoPlayer = null
        }
    }
}