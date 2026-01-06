package minestats.api.storage;

import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerRepository;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.Optional;

@DisplayName("PlayerRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqlitePlayerRepositoryTest {

    private PlayerRepository repository;

    @BeforeAll
    void setup() {
        Connection conn = DatabaseTest.getConnection();

        PlayerDao playerDao = new PlayerDao(conn);
        repository = new SqlitePlayerRepository(playerDao);

        Storage.registerPlayerRepo(repository);
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
        Optional<StatPlayer> optP2 = Storage.players().findByName("Dev");
        Assertions.assertTrue(optP2.isPresent());
        StatPlayer p2 = optP2.get();
        Assertions.assertTrue(p1.isPersisted());
        Assertions.assertTrue(p2.isPersisted());
        Assertions.assertEquals(p1.getId(), p2.getId());
        Assertions.assertEquals(p1.getName(), p2.getName());
        Assertions.assertEquals(p1, p2);
    }
}
