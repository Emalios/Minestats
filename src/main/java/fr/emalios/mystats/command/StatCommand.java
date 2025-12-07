package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import fr.emalios.mystats.impl.adapter.StatManager;
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
                .then(scan())
                .then(show())
                .then(init())
                .then(destroy());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> show() {
        return Commands.literal("show")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        DatabaseInitializer.showDbStructures();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> scan() {
        return Commands.literal("scan")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        StatManager.getInstance().scan();
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
                        DatabaseInitializer.olddeleteDb();
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
                        DatabaseInitializer.createAll();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return 0;
                });
    }

}
