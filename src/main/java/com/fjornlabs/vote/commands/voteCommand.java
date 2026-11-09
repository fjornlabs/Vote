package com.fjornlabs.vote.commands;

import com.fjornlabs.vote.config.config;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class voteCommand {

    private voteCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vote")
                .executes(voteCommand::showLinks)
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2)) // ops only
                        .executes(voteCommand::reloadConfig)));
    }

    private static int showLinks(CommandContext<CommandSourceStack> context) {
        config cfg = config.get();
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal(cfg.header)
                .withStyle(color(cfg.headerColor, ChatFormatting.GOLD), ChatFormatting.BOLD), false);

        for (config.VoteLink link : cfg.links) {
            source.sendSuccess(() -> buildLinkLine(cfg, link), false);
        }

        source.sendSuccess(() -> Component.literal(cfg.footer)
                .withStyle(color(cfg.footerColor, ChatFormatting.GRAY)), false);
        return 1;
    }

    private static MutableComponent buildLinkLine(config cfg, config.VoteLink link) {
        MutableComponent label = Component.literal("[" + link.label + "] ")
                .withStyle(color(cfg.labelColor, ChatFormatting.WHITE));

        MutableComponent button = Component.literal("[Click here]")
                .withStyle(color(cfg.clickColor, ChatFormatting.AQUA), ChatFormatting.UNDERLINE)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.url))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal(link.url).withStyle(ChatFormatting.GRAY))));

        return label.append(button);
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        config.reload();
        context.getSource().sendSuccess(() ->
                Component.literal("[Vøte] Config reloaded!").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    /** Resolves a color name from config, falling back if it's missing or invalid. */
    private static ChatFormatting color(String name, ChatFormatting fallback) {
        ChatFormatting resolved = name == null ? null : ChatFormatting.getByName(name);
        return resolved != null ? resolved : fallback;
    }
}