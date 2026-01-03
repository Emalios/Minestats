package fr.emalios.mystats.api.storage;

public final class Storage {

    private static PlayerRepository playerRepository;
    private static PlayerInventoryRepository playerInventoryRepository;
    private static InventoryRepository inventoryRepository;
    private static InventorySnapshotRepository inventorySnapshotRepository;

    private Storage() {}

    public static PlayerRepository players() {
        return playerRepository;
    }

    public static PlayerInventoryRepository playerInventories() {
        return playerInventoryRepository;
    }

    public static InventoryRepository inventories() {
        return inventoryRepository;
    }

    public static InventorySnapshotRepository inventorySnapshots() {
        return inventorySnapshotRepository;
    }

    public static void register(
            PlayerRepository playerRepo,
            PlayerInventoryRepository playerInventoryRepo,
            InventoryRepository inventoryRepo,
            InventorySnapshotRepository inventorySnapshotRepo
    ) {
        playerRepository = playerRepo;
        playerInventoryRepository = playerInventoryRepo;
        inventoryRepository = inventoryRepo;
        inventorySnapshotRepository = inventorySnapshotRepo;
    }

    public static void registerPlayers(PlayerRepository repository) {
        playerRepository = repository;
    }
}
