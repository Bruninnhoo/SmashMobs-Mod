package net.brunodev.smashmobs.server;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
 
public class SmashPositionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/smashmobs_positions.json");
 
    public static class Data {
        public int lobbyX = 0, lobbyY = 100, lobbyZ = 0;
        public int arenaX = 0, arenaY = 100, arenaZ = 0;
    }
 
    private static Data instance = new Data();
 
    static {
        load();
    }
 
    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(FILE)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            if (loaded != null) instance = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static void save() {
        try {
            if (FILE.getParentFile() != null && !FILE.getParentFile().exists()) {
                FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(instance, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static Vec3 getLobbyVec() {
        return new Vec3(instance.lobbyX + 0.5, instance.lobbyY, instance.lobbyZ + 0.5);
    }
 
    public static Vec3 getArenaVec() {
        return new Vec3(instance.arenaX + 0.5, instance.arenaY, instance.arenaZ + 0.5);
    }
 
    public static void setLobbyPos(BlockPos pos) {
        instance.lobbyX = pos.getX();
        instance.lobbyY = pos.getY();
        instance.lobbyZ = pos.getZ();
        save();
    }
 
    public static void setArenaPos(BlockPos pos) {
        instance.arenaX = pos.getX();
        instance.arenaY = pos.getY();
        instance.arenaZ = pos.getZ();
        save();
    }
}
