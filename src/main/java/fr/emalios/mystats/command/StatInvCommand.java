package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import fr.emalios.mystats.api.models.StatPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
