package me.zombii.keybindcfg;

import me.zombii.keybindcfg.util.NativeArrayUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyModifier;
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

    public static final File file = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath() + "/keybinds_1.12.2.json");
    private static JsonObject object = new JsonObject();
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
            object = JsonObject.readHjson(new String(NativeArrayUtil.readNBytes(stream, Integer.MAX_VALUE))).asObject();
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

    public static void saveKey(String key, int modifier, int code) {
        JsonObject keybind = new JsonObject();
        keybind.set("modifier", modifier);
        keybind.set("code", code);

        if (object.get(key) != null) {
            JsonValue value1 = object.get(key).asObject().get("canModify");
            keybind.set("canModify", value1 == null || value1.asBoolean());
        } else {
            keybind.set("canModify", true);
        }

        object.set(key, keybind);
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

    public static KeyModifier loadKeyModifier(String key) {
        JsonValue result = object.get(key);
        if (result == null) return KeyModifier.NONE;

        return KeyModifier.values()[result.asObject().get("modifier") == null ? 3 : result.asObject().get("modifier").asInt()];
    }

    public static int loadKeyCode(String key) {
        JsonValue result = object.get(key);
        if (result == null) return 0;

        return result.asObject().get("code") == null ? 0 : result.asObject().get("code").asInt();
    }
}
