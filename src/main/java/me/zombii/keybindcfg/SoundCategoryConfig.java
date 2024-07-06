package me.zombii.keybindcfg;

import net.minecraft.client.MinecraftClient;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundCategoryConfig {

    private static final File file = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/soundCategories.json");
    private static JsonObject object;
    static boolean hasLoadedBefore;

    public static void loadConfig() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            object = JsonObject.readHjson(new String(stream.readAllBytes())).asObject();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveConfig() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (!hasLoadedBefore) {
                loadConfig();
                hasLoadedBefore = true;
            }
        }
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(object.toString(Stringify.FORMATTED).getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static double saveCategory(String key, double value) {
        object.set(key, value);
        return value;
    }

    public static double loadCategory(String key) {
        JsonValue result = object.get(key);
        if (result == null) return 1.0;
        return result.asDouble();
    }

}
