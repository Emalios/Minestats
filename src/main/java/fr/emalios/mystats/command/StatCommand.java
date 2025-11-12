package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import fr.emalios.mystats.core.db.DatabaseSchema;
import fr.emalios.mystats.core.db.DatabaseWorker;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.sql.SQLException;

public class StatCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(db());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> db() {
        return Commands.literal("db")
                .then(show())
                .then(init())
                .then(destroy());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> show() {
        return Commands.literal("show")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        DatabaseSchema.showDbStructures();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destroy() {
        return Commands.literal("destroy")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        DatabaseSchema.deleteDb();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> init() {
        return Commands.literal("init")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        DatabaseSchema.createAll();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
    }

}
