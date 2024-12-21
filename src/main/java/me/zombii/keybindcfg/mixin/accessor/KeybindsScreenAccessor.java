package me.zombii.keybindcfg.mixin.accessor;

import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiControls.class)
public interface KeybindsScreenAccessor {

    @Accessor("keyBindingList")
    GuiKeyBindingList getKeyBindingList();

}
