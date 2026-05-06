package com.otterdays.helphelper;

import com.otterdays.helphelper.network.OpenHelpPayload;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

final class HelpHelperCommands {
    private HelpHelperCommands() {
    }

    static void register() {
        CommandRegistrationCallback.EVENT.register(
            (CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext,
                Commands.CommandSelection selection) ->
                dispatcher.register(Commands.literal("help")
                    .requires(cs -> cs.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ALL)))
                    .executes(ctx -> {
                        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                            ctx.getSource().sendFailure(Component.literal("Help Helper GUI is player-only."));
                            return 0;
                        }

                        ServerPlayNetworking.send(player, new OpenHelpPayload(
                            HelpCommandsSupport.collectFor(ctx.getSource())));
                        return 1;
                    })));
    }
}
