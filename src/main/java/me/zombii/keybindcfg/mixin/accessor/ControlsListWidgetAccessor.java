package me.zombii.keybindcfg.mixin.accessor;

import net.minecraft.client.gui.GuiKeyBindingList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiKeyBindingList.class)
public interface ControlsListWidgetAccessor {

    @Accessor("maxListLabelWidth")
    int getMaxListLabelWidth();

}