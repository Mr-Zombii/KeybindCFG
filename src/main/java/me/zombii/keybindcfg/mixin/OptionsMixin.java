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
import me.zombii.keybindcfg.KeybindCFG;
import me.zombii.keybindcfg.KeybindConfig;
import me.zombii.keybindcfg.PlayerPartsConfig;
import me.zombii.keybindcfg.SoundCategoryConfig;
import net.minecraft.SharedConstants;
import net.minecraft.client.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraftforge.client.settings.KeyModifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(GameOptions.class)
public abstract class OptionsMixin {

    @Shadow protected abstract void setPlayerModelPart(PlayerModelPart part, boolean enabled);

    @Shadow @Final private Set<PlayerModelPart> enabledPlayerModelParts;

    @Shadow @Final private Map<SoundCategory, SimpleOption<Double>> soundVolumeLevels;

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


    @Shadow @Final private SimpleOption<GraphicsMode> graphicsMode;

    @Shadow protected MinecraftClient client;

    @Shadow public abstract void sendClientSettings();

    @Shadow protected abstract NbtCompound update(NbtCompound nbt);

    @Shadow protected abstract void accept(GameOptions.Visitor visitor);

    @Shadow @Final private SimpleOption<Integer> maxFps;

    @Shadow @Nullable public String fullscreenResolution;

    /**
     * @author Mr_Zombii
     * @reason Fix Options Saving
     */
    @Overwrite
    public void write() {
        try {
            final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));

            try {
                printwriter.println("version:" + SharedConstants.getGameVersion().getSaveVersion().getId());
                this.accept(new GameOptions.Visitor() {
                    public void print(String key) {
                        printwriter.print(key);
                        printwriter.print(':');
                    }

                    public <T> void accept(String key, SimpleOption<T> option) {
                        if (key.startsWith("soundCategory_")) {
                            SoundCategoryConfig.saveCategory(key, (Double) option.getValue());
                            return;
                        }
                        DataResult<JsonElement> dataresult = option.getCodec().encodeStart(JsonOps.INSTANCE, option.getValue());
                        dataresult.error().ifPresent((partialResult) -> {
                            LOGGER.error("Error saving option {}: {}", option, partialResult);
                        });
                        dataresult.result().ifPresent((json) -> {
                            this.print(key);
                            printwriter.println(GSON.toJson(json));
                        });
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

                    public float visitFloat(String key, float current) {
                        this.print(key);
                        printwriter.println(current);
                        return current;
                    }

                    public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                        this.print(key);
                        printwriter.println(encoder.apply(current));
                        return current;
                    }
                });
                if (client.getWindow().getVideoMode().isPresent()) {
                    printwriter.println("fullscreenResolution:" + this.client.getWindow().getVideoMode().get().asString());
                }
            } catch (Throwable var5) {
                try {
                    printwriter.close();
                } catch (Throwable var4) {
                    var5.addSuppressed(var4);
                }

                throw var5;
            }

            printwriter.close();
        } catch (Exception var6) {
            LOGGER.error("Failed to save options", var6);
        }

        KeybindCFG.saveConfigs();
        this.sendClientSettings();
    }

    /**
     * @author Mr_Zombii
     * @reason Fix Options Loading
     */
    @Inject(method = "load(Z)V", at = @At("HEAD"), cancellable = true)
    public void load(boolean limited, CallbackInfo ci) {
        KeybindCFG.loadConfigs();
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            NbtCompound compoundtag = new NbtCompound();
            BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8);

            try {
                bufferedreader.lines().forEach((line) -> {
                    try {
                        Iterator<String> iterator = COLON_SPLITTER.split(line).iterator();
                        compoundtag.putString(iterator.next(), (String)iterator.next());
                    } catch (Exception var3) {
                        LOGGER.warn("Skipping bad option: {}", line);
                    }

                });
            } catch (Throwable var7) {
                try {
                    bufferedreader.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }

                throw var7;
            }

            bufferedreader.close();

            final NbtCompound compoundtag1 = this.update(compoundtag);
            if (!compoundtag1.contains("graphicsMode") && compoundtag1.contains("fancyGraphics")) {
                if (isTrue(compoundtag1.getString("fancyGraphics"))) {
                    this.graphicsMode.setValue(GraphicsMode.FANCY);
                } else {
                    this.graphicsMode.setValue(GraphicsMode.FAST);
                }
            }

            Consumer<GameOptions.Visitor> processor = limited ? this::processOptionsForge : this::accept;
            processor.accept(new GameOptions.Visitor() {

                @Nullable
                private String find(String key) {
                    if (key.startsWith("key_")) return KeybindConfig.loadKey(key);
                    if (key.startsWith("modelPart_")) return PlayerPartsConfig.loadPart(key);
                    return compoundtag1.contains(key) ? compoundtag1.getString(key) : null;
                }

                public <T> void accept(String key, SimpleOption<T> option) {
                    if (key.startsWith("soundCategory_")) {
                        ((SimpleOption<Double>) option).setValue(SoundCategoryConfig.loadCategory(key));
                        return;
                    }
                    String s = this.find(key);
                    if (s != null) {
                        JsonReader jsonreader = new JsonReader(new StringReader(s.isEmpty() ? "\"\"" : s));
                        JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                        DataResult<T> dataresult = option.getCodec().parse(JsonOps.INSTANCE, jsonelement);
                        dataresult.error().ifPresent((partialResult) -> {
                            LOGGER.error("Error parsing option value " + s + " for option " + option + ": " + partialResult.message());
                        });
                        Optional<T> var10000 = dataresult.result();
                        Objects.requireNonNull(option);
                        var10000.ifPresent(option::setValue);
                    }

                }

                public int visitInt(String key, int current) {
                    String s = this.find(key);
                    if (s != null) {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException var5) {
                            LOGGER.warn("Invalid integer value for option {} = {}", new Object[]{key, s, var5});
                        }
                    }

                    return current;
                }

                public boolean visitBoolean(String key, boolean current) {
                    String s = this.find(key);
                    return s != null ? isTrue(s) : current;
                }

                public String visitString(String key, String current) {
                    return MoreObjects.firstNonNull(this.find(key), current);
                }

                public float visitFloat(String key, float current) {
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
                            LOGGER.warn("Invalid floating point value for option {} = {}", key, s, var5);
                        }
                    }

                    return current;
                }

                public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
                    String s = this.find(key);
                    return s == null ? current : decoder.apply(s);
                }
            });
            if (compoundtag1.contains("fullscreenResolution")) {
                this.fullscreenResolution = compoundtag1.getString("fullscreenResolution");
            }

            if (this.client.getWindow() != null) {
                this.client.getWindow().setFramerateLimit(this.maxFps.getValue());
            }

            KeyBinding.updateKeysByCode();
        } catch (Exception var8) {
            LOGGER.error("Failed to load options", var8);
        }
        ci.cancel();
    }

    /**
     * @author Mr_Zombii
     * @reason Separate Keybindings from options.txt
     */
    @Overwrite
    private void processOptionsForge(GameOptions.Visitor arg) {
        KeyBinding[] var2 = this.allKeys;
        int var3 = var2.length;

        int i;
        for(i = 0; i < var3; ++i) {
            KeyBinding keymapping = var2[i];
            String keyMapping = keymapping.getBoundKeyTranslationKey() + (keymapping.getKeyModifier() != KeyModifier.NONE ? ":" + keymapping.getKeyModifier() : "");

            String keyCode = arg.visitString("key_" + keymapping.getTranslationKey(), keyMapping);
            if (!keyMapping.equals(keyCode)) {
                if (keyCode.indexOf(58) != -1) {
                    String[] pts = keyCode.split(":");
                    keymapping.setKeyModifierAndCode(KeyModifier.valueFromString(pts[1]), InputUtil.fromTranslationKey(pts[0]));
                } else {
                    keymapping.setKeyModifierAndCode(KeyModifier.NONE, InputUtil.fromTranslationKey(keyCode));
                }
            }
        }

        SoundCategory[] soundSources = SoundCategory.values();

        for(i = 0; i < soundSources.length; ++i) {
            SoundCategory soundcategory = soundSources[i];
            arg.accept("soundCategory_" + soundcategory.getName(), this.soundVolumeLevels.get(soundcategory));
        }

        PlayerModelPart[] playerModelParts = PlayerModelPart.values();

        for(i = 0; i < playerModelParts.length; ++i) {
            PlayerModelPart playermodelpart = playerModelParts[i];
            boolean flag = this.enabledPlayerModelParts.contains(playermodelpart);
            boolean flag1 = arg.visitBoolean("modelPart_" + playermodelpart.getId(), flag);
            if (flag1 != flag) {
                this.setPlayerModelPart(playermodelpart, flag1);
            }
        }
    }

}
