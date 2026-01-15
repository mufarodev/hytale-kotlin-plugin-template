package com.example.mod

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit

class TestMod(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Hello from ${this.name} version ${this.manifest.version}")
    }

    override fun setup() {
        LOGGER.atInfo().log("Setting up plugin ${this.name}")
    }
}

