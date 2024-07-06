package me.zombii.keybindcfg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(KeybindCFG.MODID)
public class KeybindCFG {

    public static final String MODID = "keybindcfg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KeybindCFG() {
        loadConfigs();
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

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", MinecraftClient.getInstance().getSession().getUsername());
        }
    }
}
