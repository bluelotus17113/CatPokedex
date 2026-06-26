package com.catpokedex

import android.app.Application
import com.catpokedex.data.CatDatabase
import com.catpokedex.data.ZenSoundManager

class CatPokedexApp : Application() {
    val database by lazy { CatDatabase.getDatabase(this) }
    val soundManager by lazy { ZenSoundManager(this) }

    override fun onTerminate() {
        super.onTerminate()
        soundManager.release()
    }
}
