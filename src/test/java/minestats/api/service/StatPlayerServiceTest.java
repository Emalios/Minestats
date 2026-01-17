package minestats.api.service;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.*;
import minestats.api.storage.DatabaseTest;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@DisplayName("StatPlayerService test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatPlayerServiceTest {

    private StatPlayerService statPlayerService;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        StatsAPI statsAPI = StatsAPI.getInstance();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        statsAPI.init(
                new SqlitePlayerRepository(new PlayerDao(conn), playerInvRepo),
                new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn)),
                playerInvRepo,
                new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)),
                new SqliteInventoryPositionsRepository(new InventoryPositionsDao(conn)));
        this.statPlayerService = statsAPI.getPlayerService();
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Create player")
    void addPlayerTest() {
        StatPlayer player = this.statPlayerService.getOrCreateByName("create_player");
        Assertions.assertTrue(this.statPlayerService.isLoaded(player.getName()));

        Assertions.assertTrue(player.isPersisted());
        Assertions.assertNotNull(player.getId());
        Assertions.assertEquals("create_player", player.getName());
    }

    @Test
    @DisplayName("Get existing player")
    void getExistingPlayerTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("get-existing-player");
        Assertions.assertTrue(this.statPlayerService.isLoaded(p1.getName()));
        Optional<StatPlayer> optP2 = this.statPlayerService.findByName("get-existing-player");
        Assertions.assertTrue(optP2.isPresent());
        StatPlayer p2 = optP2.get();
        Assertions.assertTrue(p1.isPersisted());
        Assertions.assertTrue(p2.isPersisted());
        Assertions.assertEquals(p1.getId(), p2.getId());
        Assertions.assertEquals(p1.getName(), p2.getName());
        Assertions.assertEquals(p1, p2);
    }

    @Test
    @DisplayName("Get non existing player")
    void getNonExistingPlayerTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("base-player");
        Optional<StatPlayer> optP2 = this.statPlayerService.findByName("Null");
        Assertions.assertTrue(optP2.isEmpty());
    }

}
