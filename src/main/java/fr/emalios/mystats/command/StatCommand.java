package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import fr.emalios.mystats.impl.adapter.StatManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.sql.SQLException;

import static fr.emalios.mystats.command.StatDbCommand.db;
import static fr.emalios.mystats.command.StatInvCommand.inv;

public class StatCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(db())
                .then(inv());
    }



}
