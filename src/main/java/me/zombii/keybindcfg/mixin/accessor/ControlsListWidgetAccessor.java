package me.zombii.keybindcfg.mixin.accessor;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ControlsListWidget.class)
public interface ControlsListWidgetAccessor {

    @Accessor("maxKeyNameLength")
    int getMaxKeyNameLength();

}
