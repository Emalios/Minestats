package fr.emalios.minestats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class StatInvCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> inv() {
        return Commands.literal("inv")
                .then(show());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> show() {
        return Commands.literal("show")
                .executes(ctx -> {
                    ServerPlayer serverPlayer = ctx.getSource().getPlayer();

                    return 0;
                });
    }

}
