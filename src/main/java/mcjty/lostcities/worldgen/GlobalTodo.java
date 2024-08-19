package mcjty.lostcities.worldgen;

import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.TodoQueue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GlobalTodo {

    record TodoQueues(TodoQueue<Consumer<ServerWorld>> todo) {                            // Todo blocks that require POI
        // Return true if all queues are empty
        public boolean isEmpty() {
            return todo.isEmpty();
        }

        // Dump size information for the queues to stdout
        public void dumpSizes() {
            System.out.println("----------------");
            System.out.println("todo = " + todo.getSize());
        }
    }

    private final Map<ChunkPos, TodoQueues> todoQueues = new HashMap<>();
    private final static Map<RegistryKey<World>, GlobalTodo> instances = new HashMap<>();

    public static GlobalTodo get(World world) {
        return instances.computeIfAbsent(world.dimension(), k -> new GlobalTodo());
    }

    public void addTodo(BlockPos pos, Consumer<ServerWorld> code) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TodoQueues queues = todoQueues.computeIfAbsent(chunkPos, k -> new TodoQueues(new TodoQueue<>()));
        queues.todo.add(pos, code);
    }

    public void executeAndClearTodo(ServerWorld level) {
        int todoSize = Config.TODO_QUEUE_SIZE.get();

        // @todo process chunks based on their distance to the player
        Set<ChunkPos> todoToRemove = new HashSet<>();
        Map<ChunkPos, TodoQueues> copy = new HashMap<>(this.todoQueues);
        for (Map.Entry<ChunkPos, TodoQueues> entry : copy.entrySet()) {
            TodoQueues queues = entry.getValue();
            ChunkPos cp = entry.getKey();
            todoSize -= queues.todo.forEach(todoSize, (pos, code) -> code.accept(level));
            if (queues.isEmpty()) {
                todoToRemove.add(cp);
            }
            if (todoSize <= 0) {
                break;
            }
        }

        // Remove all empty todo queues
        todoToRemove.forEach(todoQueues::remove);
    }
}