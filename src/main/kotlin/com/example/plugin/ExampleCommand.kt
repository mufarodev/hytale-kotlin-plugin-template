package com.example.plugin

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
class ExampleCommand(private val pluginName: String, private val pluginVersion: String) : CommandBase(
    "test",
    "Prints a test message from the $pluginName plugin."
) {
    init {
        this.setPermissionGroup(GameMode.Adventure) // Command can be used by anyone, not just OP
    }

    override fun executeSync(ctx: CommandContext) {
        ctx.sendMessage(Message.raw("Hello from the $pluginName v$pluginVersion plugin!"))
    }
}

