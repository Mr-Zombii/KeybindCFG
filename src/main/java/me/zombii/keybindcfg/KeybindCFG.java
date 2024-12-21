package me.zombii.keybindcfg;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = KeybindCFG.MODID, name = KeybindCFG.NAME, version = KeybindCFG.VERSION)
public class KeybindCFG {

    public static final String MODID = "keybindcfg";
    public static final String NAME = "Keybind Config";
    public static final String VERSION = "1.2";
    public static Logger LOGGER;

    public KeybindCFG() {
    }

    public static void loadConfigs() {
        KeybindConfig.loadConfig();
        PlayerPartsConfig.loadConfig();
        SoundCategoryConfig.loadConfig();
    }

    public static void saveConfigs() {
        KeybindConfig.saveConfig();
        PlayerPartsConfig.saveConfig();
        SoundCategoryConfig.saveConfig();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        LOGGER.info("HELLO FROM CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> " +  Minecraft.getMinecraft().getSession().getUsername());
    }

}
