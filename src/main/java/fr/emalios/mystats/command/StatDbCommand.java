package fr.emalios.mystats.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.sql.SQLException;

public class StatDbCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> db() {
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
                    StatManager.getInstance().scan();
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destroy() {
        return Commands.literal("destroy")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    try {
                        DatabaseInitializer.deleteDb();
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
                    Database.getInstance().init(Const.DB_FILENAME);
                    return 0;
                });
    }

}
