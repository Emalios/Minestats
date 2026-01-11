package fr.emalios.mystats.api.storage;

public final class Storage {

    private static PlayerRepository playerRepository;
    private static PlayerInventoryRepository playerInventoryRepository;
    private static InventoryRepository inventoryRepository;
    private static InventorySnapshotRepository inventorySnapshotRepository;
    private static InventoryPositionsRepository inventoryPositionsRepository;

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

    public static InventoryPositionsRepository inventoryPositions() {return inventoryPositionsRepository;}

    public static void register(
            PlayerRepository playerRepo,
            PlayerInventoryRepository playerInventoryRepo,
            InventoryRepository inventoryRepo,
            InventorySnapshotRepository inventorySnapshotRepo,
            InventoryPositionsRepository inventoryPositionsRepo
    ) {
        playerRepository = playerRepo;
        playerInventoryRepository = playerInventoryRepo;
        inventoryRepository = inventoryRepo;
        inventorySnapshotRepository = inventorySnapshotRepo;
        inventoryPositionsRepository = inventoryPositionsRepo;
    }

    public static void registerInventoryPositionRepo(
            InventoryPositionsRepository inventoryPositionsRepo
    ) {
        inventoryPositionsRepository = inventoryPositionsRepo;
    }

    public static void registerInventorySnapshotRepo(InventorySnapshotRepository inventorySnapshots) {
        inventorySnapshotRepository = inventorySnapshots;
    }

    public static void registerPlayerInventoriesRepo(PlayerInventoryRepository playerInventoryRepo) {
        playerInventoryRepository = playerInventoryRepo;
    }

    public static void registerPlayerRepo(PlayerRepository repository) {
        playerRepository = repository;
    }

    public static void registerInventoryRepo(InventoryRepository repository) {
        inventoryRepository = repository;
    }
}
