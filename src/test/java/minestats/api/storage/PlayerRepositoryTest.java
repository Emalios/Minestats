package minestats.api.storage;

import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerRepository;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;

@DisplayName("PlayerRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerRepositoryTest {

    private PlayerRepository repository;

    @BeforeAll
    void setup() {
        Connection conn = DatabaseTest.getConnection();

        PlayerDao playerDao = new PlayerDao(conn);
        repository = new SqlitePlayerRepository(playerDao);

        Storage.registerPlayers(repository);
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Create player")
    void addPlayerTest() {
        StatPlayer player = Storage.players().getOrCreate("Test");

        Assertions.assertTrue(player.isPersisted());
        Assertions.assertNotNull(player.getId());
        Assertions.assertEquals("Test", player.getName());
    }

    @Test
    @DisplayName("Get existing player")
    void getExistingPlayerTest() {
        StatPlayer p1 = Storage.players().getOrCreate("Dev");
        StatPlayer p2 = Storage.players().getOrCreate("Dev");
        Assertions.assertTrue(p1.isPersisted());
        Assertions.assertTrue(p2.isPersisted());
        Assertions.assertEquals(p1.getId(), p2.getId());
    }

    @Test
    @DisplayName("Get null player")
    void getNullPlayerTest() {
        StatPlayer p1 = Storage.players().("DevTest");
        Assertions.assertFalse(p1.isPersisted());
    }
}
