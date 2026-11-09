package com.fjornlabs.vote;

import com.fjornlabs.vote.commands.voteCommand;
import com.fjornlabs.vote.config.config;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(Vote.MOD_ID)
@EventBusSubscriber(modid = Vote.MOD_ID)
public final class Vote {

    public static final String MOD_ID = "vote";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Vote() {
        // Load (or generate) vote.json at startup so the file exists right away
        config.get();
        LOGGER.info("[Vøte] Loaded.");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        voteCommand.register(event.getDispatcher());
    }
}