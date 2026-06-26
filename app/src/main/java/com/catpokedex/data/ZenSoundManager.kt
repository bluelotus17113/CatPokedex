package com.catpokedex.data

import android.content.Context

class ZenSoundManager(context: Context) {
    // Placeholder - sounds will be added when .ogg files are placed in res/raw/
    // The app works silently without sound files

    fun play(soundId: Int) {
        // No sounds loaded yet
    }

    fun release() {
        // Nothing to release
    }

    companion object {
        const val SOUND_CAPTURE = 0
        const val SOUND_SAVE = 1
        const val SOUND_DELETE = 2
        const val SOUND_NAVIGATE = 3
        const val SOUND_OPEN = 4
        const val SOUND_SHUTTER = 5
        const val SOUND_ERROR = 6
    }
}
