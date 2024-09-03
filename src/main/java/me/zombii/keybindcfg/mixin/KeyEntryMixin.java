package me.zombii.keybindcfg.mixin;

import com.google.common.collect.ImmutableList;
import me.zombii.keybindcfg.KeybindConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public abstract class KeyEntryMixin extends ControlsListWidget.Entry {

    @Shadow @Final private KeyBinding binding;
    @Shadow @Final private ButtonWidget editButton;
    @Mutable
    @Shadow @Final private ButtonWidget resetButton;
    @Shadow @Final private Text bindingName;
    @Unique private static MinecraftClient minecraft = MinecraftClient.getInstance();

    @Unique private ButtonWidget lockButton;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ControlsListWidget arg, KeyBinding binding, Text bindingName, CallbackInfo ci) {
        lockButton = new ButtonWidget(0, 0, 30, 20, Text.of("Lock"), button -> {
            KeybindConfig.setModifiable(this.binding.getTranslationKey(), !KeybindConfig.isModifiable(this.binding.getTranslationKey()));
            KeybindConfig.saveConfig();
            KeyBinding.updateKeysByCode();
        }) {
            @Override
            public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                if (KeybindConfig.isInDevMode) {
                    super.render(matrices, mouseX, mouseY, delta);
                }
            }

            @Override
            public void onPress() {
                if (KeybindConfig.isInDevMode) {
                    super.onPress();
                }
            }
        };
    }
    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.lockButton.mouseClicked(mouseX, mouseY, button) || this.editButton.mouseClicked(mouseX, mouseY, button) || this.resetButton.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.lockButton.mouseReleased(mouseX, mouseY, button) || this.editButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public List<? extends Element> children() {
        if (KeybindConfig.isInDevMode)
            return ImmutableList.of(this.editButton, this.resetButton, this.lockButton);
        return ImmutableList.of(this.editButton, this.resetButton);
    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
    @Overwrite
    public List<? extends Selectable> selectableChildren() {
        if (KeybindConfig.isInDevMode)
            return ImmutableList.of(this.editButton, this.resetButton, this.lockButton);
        return ImmutableList.of(this.editButton, this.resetButton);
    }

    /**
     * @author Mr_Zombii
     * @reason add more things
     */
    @Overwrite
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        if (KeybindConfig.isInDevMode) {
            this.resetButton = new ButtonWidget(0, 0, 32, 20, new TranslatableText("controls.reset"), (button) -> {
                this.binding.setToDefault();
                MinecraftClient.getInstance().options.setKeyCode(binding, binding.getDefaultKey());
                KeyBinding.updateKeysByCode();
            }) {
                protected MutableText getNarrationMessage() {
                    return new TranslatableText("narrator.controls.reset", new Object[]{bindingName});
                }
            };
        } else {
            this.resetButton = new ButtonWidget(0, 0, 50, 20, new TranslatableText("controls.reset"), (button) -> {
                this.binding.setToDefault();
                MinecraftClient.getInstance().options.setKeyCode(binding, binding.getDefaultKey());
                KeyBinding.updateKeysByCode();
            }) {
                protected MutableText getNarrationMessage() {
                    return new TranslatableText("narrator.controls.reset", new Object[]{bindingName});
                }
            };
        }

        int maxKeyNameLength = 0;
        KeyBinding selected = null;
        if (minecraft.currentScreen instanceof KeybindsScreen screen) {
            maxKeyNameLength = screen.controlsList.maxKeyNameLength;
            selected = screen.selectedKeyBinding;
        }
        boolean flag = selected == this.binding;
        float f = (float)(x + 90 - maxKeyNameLength);
        MinecraftClient.getInstance().textRenderer.draw(matrices, this.bindingName, f, (float)(y + entryHeight / 2 - 4), 16777215);
        this.resetButton.x = x + 173 + 27;
        this.resetButton.y = y;
        this.resetButton.active = KeybindConfig.isInDevMode ? !this.binding.isDefault() : KeybindConfig.isModifiable(this.binding.getTranslationKey()) ? !this.binding.isDefault() : false;
        this.resetButton.render(matrices, mouseX, mouseY, tickDelta);

        this.lockButton.x = x + 173 + 27 + 32;
        this.lockButton.y = y;
        this.lockButton.active = true;
        this.lockButton.setFGColor(KeybindConfig.isModifiable(this.binding.getTranslationKey()) ? 65280 : 16711680);
        this.lockButton.render(matrices, mouseX, mouseY, tickDelta);

        this.editButton.x = x + 105;
        this.editButton.y = y;
        this.editButton.active = KeybindConfig.isModifiable(this.binding.getTranslationKey());
        this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
        boolean flag1 = false;
        boolean keyCodeModifierConflict = true;
        if (!this.binding.isUnbound()) {
            KeyBinding[] var15 = MinecraftClient.getInstance().options.allKeys;

            for (KeyBinding keymapping : var15) {
                if (keymapping != this.binding && this.binding.equals(keymapping)) {
                    flag1 = true;
                    keyCodeModifierConflict &= keymapping.hasKeyModifierConflict(this.binding);
                }
            }
        }

        if (flag) {
            this.editButton.setMessage((new LiteralText("> ")).append(this.editButton.getMessage().shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
        } else if (flag1) {
            this.editButton.setMessage(this.editButton.getMessage().shallowCopy().formatted(keyCodeModifierConflict ? Formatting.GOLD : Formatting.RED));
        }

        this.editButton.render(matrices, mouseX, mouseY, tickDelta);

        if (this.resetButton.isHovered() || this.editButton.isHovered()) {
            if (!KeybindConfig.isInDevMode) {
                if (!KeybindConfig.isModifiable(binding.getTranslationKey())) {
                    this.resetButton.active = false;
                    MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, Text.of("The modpack developer has locked this keybind"), mouseX, mouseY);
                } else {
                    this.resetButton.active = !this.binding.isDefault();
                }
            }
        }
    }

}
