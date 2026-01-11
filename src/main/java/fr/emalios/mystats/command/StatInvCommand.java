package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.helper.Utils;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

import java.sql.SQLException;

public class StatInvCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> inv() {
        return Commands.literal("inv")
                .then(show());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> show() {
        return Commands.literal("show")
                .executes(ctx -> {
                    ServerPlayer serverPlayer = ctx.getSource().getPlayer();
                    StatPlayer statPlayer = Storage.players().getOrCreate(serverPlayer.getName().getString());
                    var inventories = Storage.playerInventories().findByPlayer(statPlayer);
                    serverPlayer.sendSystemMessage(Component.literal("Your inventories:"));
                    inventories.forEach(inventory -> {
                        System.out.println();
                        /*
                        int x = inventory.getX();
                        int y = inventory.getY();
                        int z = inventory.getZ();
                        String builder = "- " + Utils.getBlockName(serverPlayer.level(), new BlockPos(x, y, z)) + " in " + inventory.getWorld() + " at " +
                                inventory.getX() + ";" + inventory.getY() + ";" + inventory.getZ();
                        serverPlayer.sendSystemMessage(Component.literal(builder));

                         */
                    });
                    return 0;
                });
    }

}
