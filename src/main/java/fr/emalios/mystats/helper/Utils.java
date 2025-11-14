package fr.emalios.mystats.helper;

import fr.emalios.mystats.core.dao.SnapshotItemDao;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static Map<String, Integer> getInventoryContent(IItemHandler inv) {
        Map<String, Integer> stacks = new HashMap<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack current = inv.getStackInSlot(i);
            if(current.isEmpty()) continue;
            stacks.merge(current.getItem().toString(), current.getCount(), Integer::sum);
        }
        return stacks;
    }

    public static Map<String, Double> makeStats(Map<Long, List<SnapshotItemDao.ItemRecord>> history) {
        List<Long> timestamps = new ArrayList<>(history.keySet());
        Collections.sort(timestamps);

        Map<String, List<Double>> ratesByItem = new HashMap<>(); // item -> liste de vitesses successives

        for (int i = 0; i < timestamps.size() - 1; i++) {
            long t1 = timestamps.get(i);
            long t2 = timestamps.get(i + 1);
            double deltaTime = (double) (t2 - t1);

            // convertir chaque snapshot en Map<itemName, count>
            Map<String, Integer> counts1 = history.get(t1).stream()
                    .collect(Collectors.toMap(SnapshotItemDao.ItemRecord::itemName, SnapshotItemDao.ItemRecord::count));
            Map<String, Integer> counts2 = history.get(t2).stream()
                    .collect(Collectors.toMap(SnapshotItemDao.ItemRecord::itemName, SnapshotItemDao.ItemRecord::count));

            // union des items des deux snapshots
            Set<String> allItems = new HashSet<>();
            allItems.addAll(counts1.keySet());
            allItems.addAll(counts2.keySet());

            for (String item : allItems) {
                int c1 = counts1.getOrDefault(item, 0);
                int c2 = counts2.getOrDefault(item, 0);
                double delta = (c2 - c1) / deltaTime; // items / seconde

                ratesByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(delta);
            }
        }

        // Moyenne de la vitesse pour chaque item sur toute la période
        Map<String, Double> avgRate = new HashMap<>();
        System.out.println(ratesByItem.keySet());
        for (var entry : ratesByItem.entrySet()) {
            double mean = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double rounded = Math.round(mean * 10.0) / 10.0; // arrondi à un chiffre après la virgule
            avgRate.put(entry.getKey(), rounded);
        }

        return avgRate;
    }

}
