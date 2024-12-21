package me.zombii.keybindcfg.mixin;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.zombii.keybindcfg.KeybindCFG;
import me.zombii.keybindcfg.KeybindConfig;
import me.zombii.keybindcfg.PlayerPartsConfig;
import me.zombii.keybindcfg.SoundCategoryConfig;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.zombii.keybindcfg.KeybindCFG.LOGGER;
import static net.minecraft.client.settings.GameSettings.COLON_SPLITTER;

@Mixin(GameSettings.class)
public abstract class OptionsMixin {

    @Shadow public abstract void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable);

    @Shadow @Final private Map<SoundCategory, Float> soundLevels;

    @Shadow protected abstract float parseFloat(String str);

    @Shadow public KeyBinding[] keyBindings;

    @Shadow public int narrator;

    @Shadow public boolean autoJump;

    @Shadow public boolean enableWeakAttacks;

    @Shadow public boolean realmsNotifications;

    @Shadow public boolean showSubtitles;

    @Shadow public EnumHandSide mainHand;

    @Shadow public boolean entityShadows;

    @Shadow public boolean useNativeTransport;

    @Shadow public boolean reducedDebugInfo;

    @Shadow public boolean forceUnicodeFont;

    @Shadow public int mipmapLevels;

    @Shadow public float chatWidth;

    @Shadow public float chatScale;

    @Shadow public float chatHeightFocused;

    @Shadow public float chatHeightUnfocused;

    @Shadow public boolean heldItemTooltips;

    @Shadow public int overrideWidth;

    @Shadow public int overrideHeight;

    @Shadow public boolean touchscreen;

    @Shadow public boolean pauseOnLostFocus;

    @Shadow public boolean advancedItemTooltips;

    @Shadow public boolean hideServerAddress;

    @Shadow public boolean useVbo;

    @Shadow public boolean enableVsync;

    @Shadow public boolean fullScreen;

    @Shadow public boolean snooperEnabled;

    @Shadow public float chatOpacity;

    @Shadow public boolean chatLinksPrompt;

    @Shadow public boolean chatLinks;

    @Shadow public boolean chatColours;

    @Shadow public EntityPlayer.EnumChatVisibility chatVisibility;

    @Shadow public String language;

    @Shadow public String lastServer;

    @Shadow public List<String> incompatibleResourcePacks;

    @Shadow @Final private static Gson GSON;

    @Shadow @Final private static Type TYPE_LIST_STRING;

    @Shadow public List<String> resourcePacks;

    @Shadow public int attackIndicator;

    @Shadow public int clouds;

    @Shadow public int ambientOcclusion;

    @Shadow public TutorialSteps tutorialStep;

    @Shadow public boolean fancyGraphics;

    @Shadow public EnumDifficulty difficulty;

    @Shadow public boolean fboEnable;

    @Shadow public int limitFramerate;

    @Shadow public boolean anaglyph;

    @Shadow public boolean viewBobbing;

    @Shadow public int particleSetting;

    @Shadow public int guiScale;

    @Shadow public int renderDistanceChunks;

    @Shadow public boolean invertMouse;

    @Shadow public float saturation;

    @Shadow public float gammaSetting;

    @Shadow public float fovSetting;

    @Shadow public float mouseSensitivity;

    @Shadow protected abstract NBTTagCompound dataFix(NBTTagCompound p_189988_1_);

    @Shadow private File optionsFile;

    @Shadow public abstract void sendSettingsToServer();

    @Shadow @Final private Set<EnumPlayerModelParts> setModelParts;

    @Shadow public abstract float getSoundLevel(SoundCategory category);



    /**
     * @author Mr_Zombii
     * @reason Fix Options Loading
     */
    @Overwrite
    public void loadOptions()
    {
        FileInputStream fileInputStream = null; // Forge: fix MC-151173
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            this.soundLevels.clear();
            List<String> list = IOUtils.readLines(fileInputStream = new FileInputStream(this.optionsFile), StandardCharsets.UTF_8); // Forge: fix MC-117449, MC-151173

            boolean reset = !KeybindConfig.file.exists();
            KeybindConfig.loadConfig();
            for (KeyBinding keybinding : this.keyBindings) {
                if (reset) {
                    keybinding.setKeyModifierAndCode(
                            KeybindConfig.loadKeyModifier("key_" + keybinding.getKeyDescription()),
                            KeybindConfig.loadKeyCode("key_" + keybinding.getKeyDescription())
                    );
                } else keybinding.setToDefault();
            }
            if (reset) KeybindConfig.saveConfig();

            SoundCategoryConfig.loadConfig();
            for (SoundCategory soundcategory : SoundCategory.values()) {
                soundLevels.put(soundcategory, SoundCategoryConfig.loadCategory("soundCategory_" + soundcategory.getName()));
            }

            PlayerPartsConfig.loadConfig();
            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                setModelPartEnabled(enumplayermodelparts, PlayerPartsConfig.loadPart("modelPart_" + enumplayermodelparts.getPartName()));
            }

            NBTTagCompound nbttagcompound = new NBTTagCompound();

            for (String s : list) {
                try {
                    String[] items = s.split(":");
                    if (items.length < 2) nbttagcompound.setString(items[0], "");
                    else nbttagcompound.setString(items[0], items[1]);
                }
                catch (Exception var10) {
                    LOGGER.warn("Skipping bad option: {}", s);
                }
            }

            nbttagcompound = this.dataFix(nbttagcompound);

            for (String s1 : nbttagcompound.getKeySet())
            {
                String s2 = nbttagcompound.getString(s1);

                try
                {
                    if ("mouseSensitivity".equals(s1)) {
                        this.mouseSensitivity = this.parseFloat(s2);
                    }

                    if ("fov".equals(s1)) {
                        this.fovSetting = this.parseFloat(s2) * 40.0F + 70.0F;
                    }

                    if ("gamma".equals(s1)) {
                        this.gammaSetting = this.parseFloat(s2);
                    }

                    if ("saturation".equals(s1)) {
                        this.saturation = this.parseFloat(s2);
                    }

                    if ("invertYMouse".equals(s1)) {
                        this.invertMouse = "true".equals(s2);
                    }

                    if ("renderDistance".equals(s1)) {
                        this.renderDistanceChunks = Integer.parseInt(s2);
                    }

                    if ("guiScale".equals(s1)) {
                        this.guiScale = Integer.parseInt(s2);
                    }

                    if ("particles".equals(s1)) {
                        this.particleSetting = Integer.parseInt(s2);
                    }

                    if ("bobView".equals(s1)) {
                        this.viewBobbing = "true".equals(s2);
                    }

                    if ("anaglyph3d".equals(s1)) {
                        this.anaglyph = "true".equals(s2);
                    }

                    if ("maxFps".equals(s1)) {
                        this.limitFramerate = Integer.parseInt(s2);
                    }

                    if ("fboEnable".equals(s1)) {
                        this.fboEnable = "true".equals(s2);
                    }

                    if ("difficulty".equals(s1)) {
                        this.difficulty = EnumDifficulty.byId(Integer.parseInt(s2));
                    }

                    if ("fancyGraphics".equals(s1)) {
                        this.fancyGraphics = "true".equals(s2);
                    }

                    if ("tutorialStep".equals(s1)) {
                        this.tutorialStep = TutorialSteps.getTutorial(s2);
                    }

                    if ("ao".equals(s1)) {
                        if ("true".equals(s2))
                        {
                            this.ambientOcclusion = 2;
                        }
                        else if ("false".equals(s2))
                        {
                            this.ambientOcclusion = 0;
                        }
                        else
                        {
                            this.ambientOcclusion = Integer.parseInt(s2);
                        }
                    }

                    if ("renderClouds".equals(s1)) {
                        switch (s2) {
                            case "true":
                                this.clouds = 2;
                                break;
                            case "false":
                                this.clouds = 0;
                                break;
                            case "fast":
                                this.clouds = 1;
                                break;
                        }
                    }

                    if ("attackIndicator".equals(s1)) {
                        switch (s2) {
                            case "0":
                                this.attackIndicator = 0;
                                break;
                            case "1":
                                this.attackIndicator = 1;
                                break;
                            case "2":
                                this.attackIndicator = 2;
                                break;
                        }
                    }

                    if ("resourcePacks".equals(s1)) {
                        this.resourcePacks = JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (this.resourcePacks == null) {
                            this.resourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(s1)) {
                        this.incompatibleResourcePacks = JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (this.incompatibleResourcePacks == null) {
                            this.incompatibleResourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("lastServer".equals(s1)) {
                        this.lastServer = s2;
                    }

                    if ("lang".equals(s1)) {
                        this.language = s2;
                    }

                    if ("chatVisibility".equals(s1)) {
                        this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(s2));
                    }

                    if ("chatColors".equals(s1)) {
                        this.chatColours = "true".equals(s2);
                    }

                    if ("chatLinks".equals(s1)) {
                        this.chatLinks = "true".equals(s2);
                    }

                    if ("chatLinksPrompt".equals(s1)) {
                        this.chatLinksPrompt = "true".equals(s2);
                    }

                    if ("chatOpacity".equals(s1)) {
                        this.chatOpacity = this.parseFloat(s2);
                    }

                    if ("snooperEnabled".equals(s1)) {
                        this.snooperEnabled = "true".equals(s2);
                    }

                    if ("fullscreen".equals(s1)) {
                        this.fullScreen = "true".equals(s2);
                    }

                    if ("enableVsync".equals(s1)) {
                        this.enableVsync = "true".equals(s2);
                    }

                    if ("useVbo".equals(s1)) {
                        this.useVbo = "true".equals(s2);
                    }

                    if ("hideServerAddress".equals(s1)) {
                        this.hideServerAddress = "true".equals(s2);
                    }

                    if ("advancedItemTooltips".equals(s1)) {
                        this.advancedItemTooltips = "true".equals(s2);
                    }

                    if ("pauseOnLostFocus".equals(s1)) {
                        this.pauseOnLostFocus = "true".equals(s2);
                    }

                    if ("touchscreen".equals(s1)) {
                        this.touchscreen = "true".equals(s2);
                    }

                    if ("overrideHeight".equals(s1)) {
                        this.overrideHeight = Integer.parseInt(s2);
                    }

                    if ("overrideWidth".equals(s1)) {
                        this.overrideWidth = Integer.parseInt(s2);
                    }

                    if ("heldItemTooltips".equals(s1)) {
                        this.heldItemTooltips = "true".equals(s2);
                    }

                    if ("chatHeightFocused".equals(s1)) {
                        this.chatHeightFocused = this.parseFloat(s2);
                    }

                    if ("chatHeightUnfocused".equals(s1)) {
                        this.chatHeightUnfocused = this.parseFloat(s2);
                    }

                    if ("chatScale".equals(s1)) {
                        this.chatScale = this.parseFloat(s2);
                    }

                    if ("chatWidth".equals(s1)) {
                        this.chatWidth = this.parseFloat(s2);
                    }

                    if ("mipmapLevels".equals(s1)) {
                        this.mipmapLevels = Integer.parseInt(s2);
                    }

                    if ("forceUnicodeFont".equals(s1)) {
                        this.forceUnicodeFont = "true".equals(s2);
                    }

                    if ("reducedDebugInfo".equals(s1)) {
                        this.reducedDebugInfo = "true".equals(s2);
                    }

                    if ("useNativeTransport".equals(s1)) {
                        this.useNativeTransport = "true".equals(s2);
                    }

                    if ("entityShadows".equals(s1)) {
                        this.entityShadows = "true".equals(s2);
                    }

                    if ("mainHand".equals(s1)) {
                        this.mainHand = "left".equals(s2) ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
                    }

                    if ("showSubtitles".equals(s1)) {
                        this.showSubtitles = "true".equals(s2);
                    }

                    if ("realmsNotifications".equals(s1)) {
                        this.realmsNotifications = "true".equals(s2);
                    }

                    if ("enableWeakAttacks".equals(s1)) {
                        this.enableWeakAttacks = "true".equals(s2);
                    }

                    if ("autoJump".equals(s1)) {
                        this.autoJump = "true".equals(s2);
                    }

                    if ("narrator".equals(s1)) {
                        this.narrator = Integer.parseInt(s2);
                    }
                } catch (Exception var11) {
                    LOGGER.warn("Skipping bad option: {}:{}", s1, s2);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load options", exception);
        }
        finally {
            IOUtils.closeQuietly(fileInputStream);
        } // Forge: fix MC-151173
    }

    /**
     * @author Mr Zombii
     * @reason Fix Saving
     */
    @Overwrite
    public void saveOptions()
    {
        if (net.minecraftforge.fml.client.FMLClientHandler.instance().isLoading()) return;
        PrintWriter printwriter = null;

        try
        {
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));
            printwriter.println("version:1343");
            printwriter.println("invertYMouse:" + this.invertMouse);
            printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + this.gammaSetting);
            printwriter.println("saturation:" + this.saturation);
            printwriter.println("renderDistance:" + this.renderDistanceChunks);
            printwriter.println("guiScale:" + this.guiScale);
            printwriter.println("particles:" + this.particleSetting);
            printwriter.println("bobView:" + this.viewBobbing);
            printwriter.println("anaglyph3d:" + this.anaglyph);
            printwriter.println("maxFps:" + this.limitFramerate);
            printwriter.println("fboEnable:" + this.fboEnable);
            printwriter.println("difficulty:" + this.difficulty.getId());
            printwriter.println("fancyGraphics:" + this.fancyGraphics);
            printwriter.println("ao:" + this.ambientOcclusion);

            switch (this.clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;
                case 1:
                    printwriter.println("renderClouds:fast");
                    break;
                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + GSON.toJson(this.resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + GSON.toJson(this.incompatibleResourcePacks));
            printwriter.println("lastServer:" + this.lastServer);
            printwriter.println("lang:" + this.language);
            printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + this.chatColours);
            printwriter.println("chatLinks:" + this.chatLinks);
            printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
            printwriter.println("chatOpacity:" + this.chatOpacity);
            printwriter.println("snooperEnabled:" + this.snooperEnabled);
            printwriter.println("fullscreen:" + this.fullScreen);
            printwriter.println("enableVsync:" + this.enableVsync);
            printwriter.println("useVbo:" + this.useVbo);
            printwriter.println("hideServerAddress:" + this.hideServerAddress);
            printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printwriter.println("touchscreen:" + this.touchscreen);
            printwriter.println("overrideWidth:" + this.overrideWidth);
            printwriter.println("overrideHeight:" + this.overrideHeight);
            printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printwriter.println("chatScale:" + this.chatScale);
            printwriter.println("chatWidth:" + this.chatWidth);
            printwriter.println("mipmapLevels:" + this.mipmapLevels);
            printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
            printwriter.println("reducedDebugInfo:" + this.reducedDebugInfo);
            printwriter.println("useNativeTransport:" + this.useNativeTransport);
            printwriter.println("entityShadows:" + this.entityShadows);
            printwriter.println("mainHand:" + (this.mainHand == EnumHandSide.LEFT ? "left" : "right"));
            printwriter.println("attackIndicator:" + this.attackIndicator);
            printwriter.println("showSubtitles:" + this.showSubtitles);
            printwriter.println("realmsNotifications:" + this.realmsNotifications);
            printwriter.println("enableWeakAttacks:" + this.enableWeakAttacks);
            printwriter.println("autoJump:" + this.autoJump);
            printwriter.println("narrator:" + this.narrator);
            printwriter.println("tutorialStep:" + this.tutorialStep.getName());

            for (KeyBinding keybinding : this.keyBindings)
            {
                KeybindConfig.saveKey("key_" + keybinding.getKeyDescription(), keybinding.getKeyModifier().ordinal(), keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values())
            {
                SoundCategoryConfig.saveCategory("soundCategory_" + soundcategory.getName(), this.getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
            {
                PlayerPartsConfig.savePart("modelPart_" + enumplayermodelparts.getPartName(), this.setModelParts.contains(enumplayermodelparts));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save options", exception);
        }
        finally
        {
            IOUtils.closeQuietly(printwriter);
        }

        KeybindCFG.saveConfigs();
        this.sendSettingsToServer();
    }

}
