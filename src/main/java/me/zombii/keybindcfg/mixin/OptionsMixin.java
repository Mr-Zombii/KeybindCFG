package me.zombii.keybindcfg.mixin;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import me.zombii.keybindcfg.KeybindCFG;
import me.zombii.keybindcfg.KeybindConfig;
import me.zombii.keybindcfg.PlayerPartsConfig;
import me.zombii.keybindcfg.SoundCategoryConfig;
import net.minecraft.SharedConstants;
import net.minecraft.client.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.VideoMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.client.settings.KeyModifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

@Mixin(GameOptions.class)
public abstract class OptionsMixin {

    @Shadow protected abstract void setPlayerModelPart(PlayerModelPart part, boolean enabled);

    @Shadow @Final private Set<PlayerModelPart> enabledPlayerModelParts;

    @Shadow @Final private Object2FloatMap<SoundCategory> soundVolumeLevels;

    @Shadow public KeyBinding[] allKeys;

    @Shadow @Final private File optionsFile;

    @Shadow @Final private static Splitter COLON_SPLITTER;

    @Shadow @Final static Logger LOGGER;
    @Shadow @Final static Gson GSON;

    @Shadow
    static boolean isTrue(String value) {
        return false;
    }

    @Shadow
    static boolean isFalse(String value) {
        return false;
    }


    @Mutable
    @Shadow @Final private GraphicsMode graphicsMode;

    @Shadow protected MinecraftClient client;

    @Shadow public abstract void sendClientSettings();

    @Shadow protected abstract NbtCompound update(NbtCompound nbt);

    @Shadow @Nullable public String fullscreenResolution;

    @Shadow protected abstract void accept(GameOptions.Visitor visitor);

    @Shadow public int maxFps;

    /**
     * @author Mr_Zombii
     * @reason Fix Options Saving
     */
    @Overwrite
    public void write() {
        if (!ClientModLoader.isLoading()) {
            try {
                final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));

                try {
                    printwriter.println("version:" + SharedConstants.getGameVersion().getWorldVersion());
                    this.accept(new GameOptions.Visitor() {
                        public void print(String key) {
                            printwriter.print(key);
                            printwriter.print(':');
                        }

                        public int visitInt(String key, int current) {
                            this.print(key);
                            printwriter.println(current);
                            return current;
                        }

                        public boolean visitBoolean(String key, boolean current) {
                            if (key.startsWith("modelPart_")) return PlayerPartsConfig.savePart(key, current);
                            this.print(key);
                            printwriter.println(current);
                            return current;
                        }

                        public String visitString(String key, String current) {
                            if (key.startsWith("key_")) return KeybindConfig.saveKey(key, current);
                            this.print(key);
                            printwriter.println(current);
                            return current;
                        }

                        public double visitDouble(String key, double current) {
                            this.print(key);
                            printwriter.println(current);
                            return current;
                        }

                        public float visitFloat(String key, float current) {
                            if (key.startsWith("soundCategory_")) {
                                SoundCategoryConfig.saveCategory(key, current);
                                return current;
                            }
                            this.print(key);
                            printwriter.println(current);
                            return current;
                        }

                        public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                            this.print(key);
                            printwriter.println(encoder.apply(current));
                            return current;
                        }

                        public <T> T visitObject(String key, T current, IntFunction<T> decoder, ToIntFunction<T> encoder) {
                            this.print(key);
                            printwriter.println(encoder.applyAsInt(current));
                            return current;
                        }
                    });
                    if (this.client.getWindow().getVideoMode().isPresent()) {
                        printwriter.println("fullscreenResolution:" + ((VideoMode)this.client.getWindow().getVideoMode().get()).asString());
                    }
                } catch (Throwable var5) {
                    Throwable throwable1 = var5;

                    try {
                        printwriter.close();
                    } catch (Throwable var4) {
                        Throwable throwable = var4;
                        throwable1.addSuppressed(throwable);
                    }

                    throw new Exception(throwable1);
                }

                printwriter.close();
            } catch (Exception var6) {
                Exception exception = var6;
                LOGGER.error("Failed to save options", exception);
            }

            this.sendClientSettings();
        }
        KeybindCFG.saveConfigs();
    }

    /**
     * @author Mr_Zombii
     * @reason Fix Options Loading
     */
    @Overwrite
    public void load() {
        KeybindCFG.loadConfigs();
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            this.soundVolumeLevels.clear();
            NbtCompound compoundtag = new NbtCompound();
            BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8);

            try {
                bufferedreader.lines().forEach((line) -> {
                    try {
                        Iterator<String> iterator = COLON_SPLITTER.split(line).iterator();
                        compoundtag.putString((String)iterator.next(), (String)iterator.next());
                    } catch (Exception var3) {
                        LOGGER.warn("Skipping bad option: {}", line);
                    }

                });
            } catch (Throwable var6) {
                Throwable throwable1 = var6;
                if (bufferedreader != null) {
                    try {
                        bufferedreader.close();
                    } catch (Throwable var5) {
                        Throwable throwable = var5;
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw new RuntimeException(throwable1);
            }

            if (bufferedreader != null) {
                bufferedreader.close();
            }

            final NbtCompound compoundtag1 = this.update(compoundtag);
            if (!compoundtag1.contains("graphicsMode") && compoundtag1.contains("fancyGraphics")) {
                if (isTrue(compoundtag1.getString("fancyGraphics"))) {
                    this.graphicsMode = GraphicsMode.FANCY;
                } else {
                    this.graphicsMode = GraphicsMode.FAST;
                }
            }

            this.accept(new GameOptions.Visitor() {
                @Nullable
                private String find(String key) {
                    if (key.startsWith("key_")) return KeybindConfig.loadKey(key);
                    if (key.startsWith("modelPart_")) return PlayerPartsConfig.loadPart(key);
                    return compoundtag1.contains(key) ? compoundtag1.getString(key) : null;
                }

                public int visitInt(String key, int current) {
                    String s = this.find(key);
                    if (s != null) {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException var5) {
                            NumberFormatException numberformatexception = var5;
                            LOGGER.warn("Invalid integer value for option {} = {}", new Object[]{key, s, numberformatexception});
                        }
                    }

                    return current;
                }

                public boolean visitBoolean(String key, boolean current) {
                    String s = this.find(key);
                    return s != null ? isTrue(s) : current;
                }

                public String visitString(String key, String current) {
                    return (String)MoreObjects.firstNonNull(this.find(key), current);
                }

                public double visitDouble(String key, double current) {
                    String s = this.find(key);
                    if (s != null) {
                        if (isTrue(s)) {
                            return 1.0;
                        }

                        if (isFalse(s)) {
                            return 0.0;
                        }

                        try {
                            return Double.parseDouble(s);
                        } catch (NumberFormatException var6) {
                            NumberFormatException numberformatexception = var6;
                            LOGGER.warn("Invalid floating point value for option {} = {}", new Object[]{key, s, numberformatexception});
                        }
                    }

                    return current;
                }

                public float visitFloat(String key, float current) {
                    if (key.startsWith("soundCategory_")) {
                        return SoundCategoryConfig.loadCategory(key);
                    }
                    String s = this.find(key);
                    if (s != null) {
                        if (isTrue(s)) {
                            return 1.0F;
                        }

                        if (isFalse(s)) {
                            return 0.0F;
                        }

                        try {
                            return Float.parseFloat(s);
                        } catch (NumberFormatException var5) {
                            NumberFormatException numberformatexception = var5;
                            LOGGER.warn("Invalid floating point value for option {} = {}", new Object[]{key, s, numberformatexception});
                        }
                    }

                    return current;
                }

                public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                    String s = this.find(key);
                    return s == null ? current : decoder.apply(s);
                }

                public <T> T visitObject(String key, T current, IntFunction<T> decoder, ToIntFunction<T> encoder) {
                    String s = this.find(key);
                    if (s != null) {
                        try {
                            return decoder.apply(Integer.parseInt(s));
                        } catch (Exception var7) {
                            Exception exception1 = var7;
                            LOGGER.warn("Invalid integer value for option {} = {}", new Object[]{key, s, exception1});
                        }
                    }

                    return current;
                }
            });
            if (compoundtag1.contains("fullscreenResolution")) {
                this.fullscreenResolution = compoundtag1.getString("fullscreenResolution");
            }

            if (this.client.getWindow() != null) {
                this.client.getWindow().setFramerateLimit(maxFps);
            }

            KeyBinding.updateKeysByCode();
        } catch (Exception var7) {
            Exception exception = var7;
            LOGGER.error("Failed to load options", exception);
        }

    }

//    /**
//     * @author Mr_Zombii
//     * @reason Separate Keybindings from options.txt
//     */
//    @Overwrite
//    private void processOptionsForge(GameOptions.Visitor arg) {
//        KeyBinding[] var2 = this.allKeys;
//        int var3 = var2.length;
//
//        int i;
//        for(i = 0; i < var3; ++i) {
//            KeyBinding keymapping = var2[i];
//            String keyMapping = keymapping.getBoundKeyTranslationKey() + (keymapping.getKeyModifier() != KeyModifier.NONE ? ":" + keymapping.getKeyModifier() : "");
//
//            String keyCode = arg.visitString("key_" + keymapping.getTranslationKey(), keyMapping);
//            if (!keyMapping.equals(keyCode)) {
//                if (keyCode.indexOf(58) != -1) {
//                    String[] pts = keyCode.split(":");
//                    keymapping.setKeyModifierAndCode(KeyModifier.valueFromString(pts[1]), InputUtil.fromTranslationKey(pts[0]));
//                } else {
//                    keymapping.setKeyModifierAndCode(KeyModifier.NONE, InputUtil.fromTranslationKey(keyCode));
//                }
//            }
//        }
//
//        SoundCategory[] soundSources = SoundCategory.values();
//
//        for(i = 0; i < soundSources.length; ++i) {
//            SoundCategory soundcategory = soundSources[i];
//            arg.accept("soundCategory_" + soundcategory.getName(), this.soundVolumeLevels.get(soundcategory));
//        }
//
//        PlayerModelPart[] playerModelParts = PlayerModelPart.values();
//
//        for(i = 0; i < playerModelParts.length; ++i) {
//            PlayerModelPart playermodelpart = playerModelParts[i];
//            boolean flag = this.enabledPlayerModelParts.contains(playermodelpart);
//            boolean flag1 = arg.visitBoolean("modelPart_" + playermodelpart.getId(), flag);
//            if (flag1 != flag) {
//                this.setPlayerModelPart(playermodelpart, flag1);
//            }
//        }
//    }

}
