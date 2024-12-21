package me.zombii.keybindcfg.mixin;

import me.zombii.keybindcfg.KeybindConfig;
import me.zombii.keybindcfg.mixin.accessor.ControlsListWidgetAccessor;
import me.zombii.keybindcfg.mixin.accessor.KeybindsScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.*;

@Mixin(GuiKeyBindingList.KeyEntry.class)
public abstract class KeyEntryMixin {

//    @Inject(method = "update", at = @At("HEAD"))
//    private void updateInject(CallbackInfo ci) {
//        if (KeybindConfig.isInDevMode) {
//            resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
//                this.binding.setToDefault();
//                minecraft.options.setKeyCode(binding, binding.getDefaultKey());
//                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
//                    ((KeybindsScreenAccessor)screen).getControlsList().update();
//                }
//            }).dimensions(0, 0, 32, 20).narrationSupplier((textSupplier) -> {
//                return Text.translatable("narrator.controls.reset", new Object[]{bindingName});
//            }).build();
//            lockButton = ButtonWidget.builder(Text.of("Lock"), (button) -> {
//                KeybindConfig.setModifiable(this.binding.getTranslationKey(), !KeybindConfig.isModifiable(this.binding.getTranslationKey()));
//                KeybindConfig.saveConfig();
//                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
//                    ((KeybindsScreenAccessor)screen).getControlsList().update();
//                }
//            }).dimensions(0, 0, 30, 20).build();
//        } else {
//            lockButton = null;
//            resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), (button) -> {
//                this.binding.setToDefault();
//                minecraft.options.setKeyCode(binding, binding.getDefaultKey());
//                if (minecraft.currentScreen instanceof KeybindsScreen screen) {
//                    ((KeybindsScreenAccessor)screen).getControlsList().update();
//                }
//            }).dimensions(0, 0, 50, 20).narrationSupplier((textSupplier) -> {
//                return Text.translatable("narrator.controls.reset", String.valueOf(bindingName));
//            }).build();
//        }
//    }

//    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setMessage(Lnet/minecraft/text/Text;)V", ordinal = 0, shift = At.Shift.AFTER))
//    private void update0(CallbackInfo ci) {
//        this.editButton.active = KeybindConfig.isModifiable(binding.getTranslationKey());
//    }

//    @Inject(method = "update", at = @At("TAIL"))
//    private void update(CallbackInfo ci) {
//        MutableText mutablecomponent = Text.empty();
//        if (!this.binding.isUnbound()) {
//            KeyBinding[] var2 = minecraft.options.allKeys;
//
//            for (KeyBinding keymapping : var2) {
//                if (keymapping != this.binding && this.binding.equals(keymapping) || keymapping.hasKeyModifierConflict(this.binding)) {
//                    if (this.duplicate) {
//                        mutablecomponent.append(", ");
//                    }
//
//                    this.duplicate = true;
//                    mutablecomponent.append(Text.translatable(keymapping.getTranslationKey()));
//                }
//            }
//        }
//
//        if (!KeybindConfig.isModifiable(binding.getTranslationKey())) {
//            this.resetButton.active = false;
//            this.editButton.setTooltip(Tooltip.of(Text.of("The modpack developer has locked this keybind")));
//            this.resetButton.setTooltip(Tooltip.of(Text.of("The modpack developer has locked this keybind")));
//        } else {
//            if (this.duplicate) {
//                this.editButton.setMessage(Text.literal("[ ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED));
//                this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", new Object[]{mutablecomponent})));
//            } else {
//                this.editButton.setTooltip(null);
//            }
//            this.resetButton.setTooltip(null);
//            this.resetButton.active = !this.binding.isDefault();
//        }
//
//        if (minecraft.currentScreen instanceof KeybindsScreen screen) {
//            if (screen.selectedKeyBinding == this.binding) {
//                this.editButton.setMessage(Text.literal("> ").append(this.editButton.getMessage().copy().formatted(new Formatting[]{Formatting.WHITE, Formatting.UNDERLINE})).append(" <").formatted(Formatting.YELLOW));
//            }
//        }
//    }

//    /**
//     * @author Mr_Zombii
//     * @reason Add keybind disabling
//     */
//    @Overwrite
//    protected void update() {
//
//        this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
//        this.editButton.active = KeybindConfig.isModifiable(binding.getTranslationKey());
//
//        this.duplicate = false;
//
//
//
//
//
//
//
//    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
//    @Overwrite
//    public List<? extends Element> children() {
//        if (KeybindConfig.isInDevMode)
//            return ImmutableList.of(this.editButton, this.resetButton, lockButton);
//        return ImmutableList.of(this.editButton, this.resetButton);
//    }

    /**
     * @author Mr_Zombii
     * @reason add the lock button
     */
//    @Overwrite
//    public List<? extends Selectable> selectableChildren() {
//        if (KeybindConfig.isInDevMode)
//            return ImmutableList.of(this.editButton, this.resetButton, lockButton);
//        return ImmutableList.of(this.editButton, this.resetButton);
//    }

    @Shadow @Final private GuiButton btnChangeKeyBinding;

    @Mutable
    @Shadow @Final private GuiButton btnReset;

    @Shadow @Final private KeyBinding keybinding;

    @Shadow @Final private String keyDesc;
    private GuiButton lockButton;

    /**
     * @author Mr_Zombii
     * @reason add more things
     */
//    @Overwrite
//    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
////    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//        int maxKeyNameLength = 0;
////        if (minecraft.currentScreen instanceof KeybindsScreen screen) maxKeyNameLength = ((ControlsListWidgetAccessor)((KeybindsScreenAccessor)screen).getControlsList()).getMaxKeyNameLength();
//        int k = x + 90 - maxKeyNameLength;
//        context.drawText(minecraft.textRenderer, this.bindingName, k, y + entryHeight / 2 - 4, 16777215, false);
//        this.editButton.setX(x + 105);
//        this.editButton.setY(y);
//
//        if (this.duplicate) {
//            int j = this.editButton.getX() - 6;
//            context.fill(j, y + 2, j + 3, y + entryHeight + 2, Formatting.RED.getColorValue() | -16777216);
//        }
//
//        this.editButton.render(context, mouseX, mouseY, tickDelta);
//
//        this.resetButton.setX(x + 173 + 27);
//        this.resetButton.setY(y);
//        this.resetButton.render(context, mouseX, mouseY, tickDelta);
//
//        if (lockButton != null) {
//            this.lockButton.setX(x + 173 + 27 + 32);
//            this.lockButton.setY(y);
//            this.lockButton.setFGColor(KeybindConfig.isModifiable(this.binding.getTranslationKey()) ? 65280 : 16711680);
//            this.lockButton.render(context, mouseX, mouseY, tickDelta);
//        }
//    }

    /**
     * @author Mr Zombii
     * @reason To Downgrade za mod
     */
    @Overwrite
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        if (KeybindConfig.isInDevMode) {
            this.btnReset = new GuiButton(0, 0, 0, 32, 20, I18n.format("controls.reset"));
            lockButton =  new GuiButton(0, 0, 0, 30, 20, "Lock");
            lockButton.x = x + 173 + 27 + 32;
            lockButton.y = y;
        } else {
            this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
            lockButton = null;
        }

        Minecraft minecraft = Minecraft.getMinecraft();

        GuiControls controls = ((GuiControls) Minecraft.getMinecraft().currentScreen);

        boolean flag = controls.buttonId == this.keybinding;
        minecraft.fontRenderer.drawString(this.keyDesc, x + 90 - ((ControlsListWidgetAccessor) ((KeybindsScreenAccessor)controls).getKeyBindingList()).getMaxListLabelWidth(), y + slotHeight / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, 16777215);
        this.btnReset.x = x + 173 + 27;
        this.btnReset.y = y;
        if (KeybindConfig.isModifiable(keybinding.getKeyDescription())) {
            btnChangeKeyBinding.enabled = true;
            btnReset.enabled = !this.keybinding.isSetToDefaultValue();
            if (lockButton != null) lockButton.displayString = TextFormatting.GREEN + "Lock";
        } else {
            btnChangeKeyBinding.enabled = false;
            btnReset.enabled = false;
            if (lockButton != null) lockButton.displayString = TextFormatting.RED + "Lock";
        }
        this.btnReset.drawButton(minecraft, mouseX, mouseY, partialTicks);
        this.btnChangeKeyBinding.x = x + 105;
        this.btnChangeKeyBinding.y = y;
        this.btnChangeKeyBinding.displayString = this.keybinding.getDisplayName();
        boolean flag1 = false;
        boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G

        if (this.keybinding.getKeyCode() != 0)
        {
            for (KeyBinding keybinding : minecraft.gameSettings.keyBindings)
            {
                if (keybinding != this.keybinding && keybinding.conflicts(this.keybinding))
                {
                    flag1 = true;
                    keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
                }
            }
        }

        if (flag)
        {
            this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
        }
        else if (flag1)
        {
            this.btnChangeKeyBinding.displayString = (keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED) + this.btnChangeKeyBinding.displayString;
        }

        this.btnChangeKeyBinding.drawButton(minecraft, mouseX, mouseY, partialTicks);
        if (lockButton != null) this.lockButton.drawButton(minecraft, mouseX, mouseY, partialTicks);
    }

    /**
     * @author Mr Zombii
     * @reason To Downgrade za mod
     */
    @Overwrite
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        Minecraft minecraft = Minecraft.getMinecraft();
        
        if (this.btnChangeKeyBinding.mousePressed(minecraft, mouseX, mouseY))
        {
            if (KeybindConfig.isModifiable(keybinding.getKeyDescription()) || KeybindConfig.isInDevMode)
                ((GuiControls) Minecraft.getMinecraft().currentScreen).buttonId = this.keybinding;
            return true;
        }
        else if (this.btnReset.mousePressed(minecraft, mouseX, mouseY))
        {
            if (KeybindConfig.isModifiable(keybinding.getKeyDescription()) || KeybindConfig.isInDevMode) {
                this.keybinding.setToDefault();
                minecraft.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());
                KeyBinding.resetKeyBindingArrayAndHash();
            }
            return true;
        }
        else if (KeybindConfig.isInDevMode && this.lockButton.mousePressed(minecraft, mouseX, mouseY))
        {
            KeybindConfig.setModifiable(keybinding.getKeyDescription(), !KeybindConfig.isModifiable(keybinding.getKeyDescription()));
            KeybindConfig.saveConfig();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @author Mr Zombii
     * @reason To Downgrade za mod
     */
    @Overwrite
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        this.btnChangeKeyBinding.mouseReleased(x, y);
        this.btnReset.mouseReleased(x, y);
    }

}
