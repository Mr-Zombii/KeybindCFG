package me.zombii.keybindcfg.mixin;

import com.google.common.collect.ImmutableList;
import me.zombii.keybindcfg.KeybindConfig;
import me.zombii.keybindcfg.mixin.accessor.ControlsListWidgetAccessor;
import me.zombii.keybindcfg.mixin.accessor.KeybindsScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public abstract class KeyEntryMixin {

    @Shadow @Final private KeyBinding binding;
    @Shadow @Final private ButtonWidget editButton;
    @Shadow private boolean duplicate;
    @Mutable
    @Shadow @Final private ButtonWidget resetButton;
    @Unique private ButtonWidget lockButton;
    @Shadow @Final private Text bindingName;
    @Unique private static MinecraftClient minecraft = MinecraftClient.getInstance();

    /**
     * @author Mr_Zombii
     * @reason Add keybind disabling
     */
    @Overwrite
    protected void update() {
        if (KeybindConfig.isInDevMode) {
            resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
                this.binding.setToDefault();
                minecraft.options.setKeyCode(binding, binding.getDefaultKey());
                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
                    ((KeybindsScreenAccessor)screen).getControlsList().update();
                }
            }).dimensions(0, 0, 32, 20).narrationSupplier((textSupplier) -> {
                return Text.translatable("narrator.controls.reset", new Object[]{bindingName});
            }).build();
            lockButton = ButtonWidget.builder(Text.of("Lock"), (button) -> {
                KeybindConfig.setModifiable(this.binding.getTranslationKey(), !KeybindConfig.isModifiable(this.binding.getTranslationKey()));
                KeybindConfig.saveConfig();
                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
                    ((KeybindsScreenAccessor)screen).getControlsList().update();
                }
            }).dimensions(0, 0, 30, 20).build();
        } else {
            lockButton = null;
            resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
                this.binding.setToDefault();
                minecraft.options.setKeyCode(binding, binding.getDefaultKey());
                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
                    ((KeybindsScreenAccessor)screen).getControlsList().update();
                }
            }).dimensions(0, 0, 50, 20).narrationSupplier((textSupplier) -> {
                return Text.translatable("narrator.controls.reset", String.valueOf(bindingName));
            }).build();
        }
        this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
        this.editButton.active = KeybindConfig.isModifiable(binding.getTranslationKey());

        this.duplicate = false;
        MutableText mutablecomponent = Text.empty();
        if (!this.binding.isUnbound()) {
            KeyBinding[] var2 = minecraft.options.allKeys;

            for (KeyBinding keymapping : var2) {
                if (keymapping != this.binding && this.binding.equals(keymapping) || keymapping.hasKeyModifierConflict(this.binding)) {
                    if (this.duplicate) {
                        mutablecomponent.append(", ");
                    }

                    this.duplicate = true;
                    mutablecomponent.append(Text.translatable(keymapping.getTranslationKey()));
                }
            }
        }

        if (!KeybindConfig.isModifiable(binding.getTranslationKey())) {
            this.resetButton.active = false;
            this.editButton.setTooltip(Tooltip.of(Text.of("The modpack developer has locked this keybind")));
            this.resetButton.setTooltip(Tooltip.of(Text.of("The modpack developer has locked this keybind")));
        } else {
            if (this.duplicate) {
                this.editButton.setMessage(Text.literal("[ ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED));
                this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", new Object[]{mutablecomponent})));
            } else {
                this.editButton.setTooltip(null);
            }
            this.resetButton.setTooltip(null);
            this.resetButton.active = !this.binding.isDefault();
        }



        if (minecraft.currentScreen instanceof KeybindsScreen screen) {
            if (screen.selectedKeyBinding == this.binding) {
                this.editButton.setMessage(Text.literal("> ").append(this.editButton.getMessage().copy().formatted(new Formatting[]{Formatting.WHITE, Formatting.UNDERLINE})).append(" <").formatted(Formatting.YELLOW));
            }
        }
    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public List<? extends Element> children() {
        if (KeybindConfig.isInDevMode)
            return ImmutableList.of(this.editButton, this.resetButton, lockButton);
        return ImmutableList.of(this.editButton, this.resetButton);
    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public List<? extends Selectable> selectableChildren() {
        if (KeybindConfig.isInDevMode)
            return ImmutableList.of(this.editButton, this.resetButton, lockButton);
        return ImmutableList.of(this.editButton, this.resetButton);
    }

    /**
     * @author Mr_Zombii
     * @reason add more things
     */
    @Overwrite
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int maxKeyNameLength = 0;
        if (minecraft.currentScreen instanceof KeybindsScreen screen) maxKeyNameLength = ((ControlsListWidgetAccessor)((KeybindsScreenAccessor)screen).getControlsList()).getMaxKeyNameLength();
        int k = x + 90 - maxKeyNameLength;
        context.drawText(minecraft.textRenderer, this.bindingName, k, y + entryHeight / 2 - 4, 16777215, false);
        this.editButton.setX(x + 105);
        this.editButton.setY(y);

        if (this.duplicate) {
            int j = this.editButton.getX() - 6;
            context.fill(j, y + 2, j + 3, y + entryHeight + 2, Formatting.RED.getColorValue() | -16777216);
        }

        this.editButton.render(context, mouseX, mouseY, tickDelta);

        this.resetButton.setX(x + 173 + 27);
        this.resetButton.setY(y);
        this.resetButton.render(context, mouseX, mouseY, tickDelta);

        if (lockButton != null) {
            this.lockButton.setX(x + 173 + 27 + 32);
            this.lockButton.setY(y);
            this.lockButton.setFGColor(KeybindConfig.isModifiable(this.binding.getTranslationKey()) ? 65280 : 16711680);
            this.lockButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

}
