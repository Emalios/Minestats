package minestats.api.storage;

import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@DisplayName("PlayerRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqlitePlayerRepositoryTest {

    private SqlitePlayerRepository sqlitePlayerRepository;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        this.sqlitePlayerRepository = new SqlitePlayerRepository(new PlayerDao(conn), playerInvRepo);
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Create player")
    void addPlayerTest() {
        StatPlayer player = this.sqlitePlayerRepository.getOrCreate("create_player");

        Assertions.assertTrue(player.isPersisted());
        Assertions.assertNotNull(player.getId());
        Assertions.assertEquals("create_player", player.getName());
    }

    @Test
    @DisplayName("Get existing player")
    void getExistingPlayerTest() {
        StatPlayer p1 = this.sqlitePlayerRepository.getOrCreate("get-existing-player");
        Optional<StatPlayer> optP2 = this.sqlitePlayerRepository.findByName("get-existing-player");
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
        StatPlayer p1 = this.sqlitePlayerRepository.getOrCreate("base-player");
        Optional<StatPlayer> optP2 = this.sqlitePlayerRepository.findByName("Null");
        Assertions.assertTrue(optP2.isEmpty());
    }
}
