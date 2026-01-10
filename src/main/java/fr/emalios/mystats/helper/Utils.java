package fr.emalios.mystats.helper;

import fr.emalios.mystats.api.Snapshot;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.impl.adapter.FluidAdapter;
import fr.emalios.mystats.impl.adapter.ItemAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {


    public static void testStat(List<Snapshot> snapshots) {
        Collections.sort(snapshots);

        Map<String, List<Double>> ratesByItem = new HashMap<>();

        for (int i = 0; i < snapshots.size() - 1; i++) {
            long t1 = snapshots.get(i).getTimestamp();
            long t2 = snapshots.get(i + 1).getTimestamp();
            double deltaTime = (double) (t2 - t1);


        }

    }

    public static Map<String, Float> makeStats(Map<Long, Collection<Record>> history) {
        List<Long> timestamps = new ArrayList<>(history.keySet());
        Collections.sort(timestamps);

        Map<String, List<Double>> ratesByItem = new HashMap<>(); // item -> liste de vitesses successives

        for (int i = 0; i < timestamps.size() - 1; i++) {
            long t1 = timestamps.get(i);
            long t2 = timestamps.get(i + 1);
            double deltaTime = (double) (t2 - t1);

            // convertir chaque snapshot en Map<itemName, count>
            Map<String, Float> counts1 = history.get(t1).stream()
                    .collect(Collectors.toMap(Record::getResourceId, Record::getCount));
            Map<String, Float> counts2 = history.get(t2).stream()
                    .collect(Collectors.toMap(Record::getResourceId, Record::getCount));

            // union des items des deux snapshots
            Set<String> allItems = new HashSet<>();
            allItems.addAll(counts1.keySet());
            allItems.addAll(counts2.keySet());

            for (String item : allItems) {
                float c1 = counts1.getOrDefault(item, Float.valueOf(0));
                float c2 = counts2.getOrDefault(item, Float.valueOf(0));
                double delta = (c2 - c1) / deltaTime; // items / seconde

                ratesByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(delta);
            }
        }

        Map<String, Float> avgRate = new HashMap<>();
        for (var entry : ratesByItem.entrySet()) {
            double mean = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            double rounded = Math.round(mean * 10.0) / 10.0;
            avgRate.put(entry.getKey(), (float) rounded);
        }

        return avgRate;
    }

    /**
     * Get the Item/Fluid Capability and convert it into IHandler
     * @param level
     * @param pos
     * @return
     */
    public static List<IHandler> getIHandlers(Level level, BlockPos pos) {
        List<IHandler> handlers = new ArrayList<>();

        getCapabilityCache(level, pos, Capabilities.ItemHandler.BLOCK).ifPresent(block -> {
            if(block.getCapability() != null) handlers.add(new ItemAdapter(block));
        });
        getCapabilityCache(level, pos, Capabilities.FluidHandler.BLOCK).ifPresent(block -> {
            if(block.getCapability() != null) handlers.add(new FluidAdapter(block));
        });

        return handlers;
    }

    public static <T, C> Optional<BlockCapabilityCache<T, C>> getCapabilityCache(Level level, BlockPos pos, BlockCapability<T, C> capability) {
        return Optional.of(BlockCapabilityCache.create(
                capability, // capability to cache
                (ServerLevel) level, // level
                pos, // target position
                null // context
        ));
    }


}
