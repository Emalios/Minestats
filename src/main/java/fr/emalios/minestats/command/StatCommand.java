package fr.emalios.minestats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static fr.emalios.minestats.command.StatDbCommand.db;
import static fr.emalios.minestats.command.StatInvCommand.inv;

public class StatCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(db())
                .then(inv());
    }



}
