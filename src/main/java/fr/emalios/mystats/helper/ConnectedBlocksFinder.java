package fr.emalios.mystats.helper;

import fr.emalios.mystats.api.models.inventory.IHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.*;

public class ConnectedBlocksFinder {

    public static Set<BlockPos> getConnectedBlocks(Level level, BlockPos startPos, List<IHandler> referenceHandlers) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        stack.push(startPos);

        while (!stack.isEmpty()) {
            BlockPos current = stack.pop();
            if (visited.contains(current)) continue;

            List<IHandler> handlers = Utils.getIHandlers(level, current);
            if (!handlersMatch(referenceHandlers, handlers)) {
                continue;
            }

            visited.add(current);

            for (BlockPos neighbor : getAdjacentPositions(current)) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }

        return visited;
    }

    private static List<BlockPos> getAdjacentPositions(BlockPos pos) {
        return List.of(
                pos.north(), pos.south(),
                pos.east(), pos.west(),
                pos.above(), pos.below()
        );
    }

    private static boolean handlersMatch(List<IHandler> reference, List<IHandler> other) {
        if (reference.size() != other.size()) return false;
        return reference.containsAll(other) && other.containsAll(reference);
    }
}
