package me.zombii.keybindcfg;

import net.minecraft.client.MinecraftClient;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KeybindConfig {

    public static boolean isInDevMode;

    private static final File file = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/keybinds.json");
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

            try {
                isInDevMode = object.get("devMode").asBoolean();
            } catch (Exception e) {
                isInDevMode = false;
                object.set("devMode", false);
            }
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

            object.set("devMode", isInDevMode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String saveKey(String key, String value) {
        JsonObject keybind = new JsonObject();
        keybind.set("value", value);

        if (object.get(key) != null) {
            JsonValue value1 = object.get(key).asObject().get("canModify");
            keybind.set("canModify", value1 == null || value1.asBoolean());
        } else {
            keybind.set("canModify", true);
        }

        object.set(key, keybind);
        return value;
    }

    public static boolean isModifiable(String key) {
        JsonValue result = object.get("key_" + key);
        if (result == null) return true;
        JsonValue bool = result.asObject().get("canModify");
        return bool == null || bool.asBoolean();
    }

    public static boolean setModifiable(String key, boolean value) {
        JsonValue result = object.get("key_" + key);
        result.asObject().set("canModify", value);
        object.set("key_" + key, result);
        return value;
    }

    public static @Nullable String loadKey(String key) {
        JsonValue result = object.get(key);
        if (result == null) return null;

        return result.asObject().get("value") == null ? null : result.asObject().get("value").asString();
    }

}
