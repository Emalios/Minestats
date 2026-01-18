package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static fr.emalios.mystats.command.StatDbCommand.db;
import static fr.emalios.mystats.command.StatInvCommand.inv;

public class StatCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(db())
                .then(inv());
    }



}
