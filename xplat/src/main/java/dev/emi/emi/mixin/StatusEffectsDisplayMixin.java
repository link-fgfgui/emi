package dev.emi.emi.mixin;

import com.google.common.collect.Ordering;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.AbstractContainerScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// TODO(Ravel): can not resolve target class StatusEffectsDisplay
// TODO(Ravel): can not resolve target class StatusEffectsDisplay
@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {

    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private AbstractContainerScreen<?> parent;

    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private Minecraft client;

    @Unique
    private static final boolean emi$hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");

    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    protected abstract int drawStatusEffectBackgrounds(DrawContext context, Font textRenderer, Component description,
                                                       Component duration, int x, int y, boolean ambient, int width);

    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    protected abstract Component getStatusEffectDescription(MobEffectInstance statusEffect);

    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    protected abstract void drawTexts(DrawContext context, Component description, Component duration, Font textRenderer,
                                      int x, int y, int width, int height, int mouseX, int mouseY);

    // TODO(Ravel): no target class
// TODO(Ravel): no target class
    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/StatusEffectsDisplay;drawStatusEffects(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/Collection;IIIII)V"),
            method = "render", cancellable = true)
    private void drawStatusEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP) {
            emi$drawCenteredEffects(context, mouseX, mouseY);
            ci.cancel();
        } else if (EmiConfig.effectLocation == EffectLocation.HIDDEN) {
            ci.cancel();
        }
    }

	@Unique
    private void emi$drawCenteredEffects(DrawContext raw, int mouseX, int mouseY) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		Collection<MobEffectInstance> effects = Ordering.natural().sortedCopy(this.client.player.getStatusEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}

        int screenX = ((AbstractContainerScreenAccessor) parent).getLeftPos();
        int screenY = ((AbstractContainerScreenAccessor) parent).getTopPos();
        int backgroundWidth = ((AbstractContainerScreenAccessor) parent).getImageWidth();

		boolean wide = size == 1;

		int y = screenY - 34;
		if (parent instanceof CreativeModeInventoryScreen || emi$hasInventoryTabs) {
			y -= 28;
			if (parent instanceof CreativeModeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}

		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (backgroundWidth - 32) / (size - 1);
		}

		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = screenX + (backgroundWidth - width) / 2;
		MobEffectInstance hovered = null;

        for (MobEffectInstance inst : effects) {
            int ew = wide ? 120 : 32;

            Font textRenderer = this.parent.getTextRenderer();
            Component description = this.getStatusEffectDescription(inst);
            Component duration = MobEffectUtil.getDurationText(inst, 1.0F, this.client.world.getTickManager()
                    .getTickRate());
            boolean isAmbient = inst.isAmbient();

            int textWidth = this.drawStatusEffectBackgrounds(context.raw(), textRenderer, description, duration, x, y, isAmbient, ew);
            raw.drawGuiTexture(RenderPipelines.GUI_TEXTURED, Gui.getEffectTexture(inst.getEffectType()), x + 7, y + 7, 18, 18);
            if (wide) {
                this.drawTexts(raw, description, duration, textRenderer, x, y, textWidth, 32, mouseX, mouseY);
            }

            if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
                hovered = inst;
            }
            x += xOff;
        }

		if (hovered != null && size > 1) {
			List<Component> list = List.of(this.getStatusEffectDescription(hovered), MobEffectUtil.getDurationText(hovered, 1.0f, client.world.getTickManager().getTickRate()));
			context.raw().drawTooltip(client.textRenderer, list, Optional.empty(), mouseX, Math.max(mouseY, 16));
		}
	}

	// TODO(Ravel): no target class
// TODO(Ravel): no target class
    @ModifyVariable(at = @At("HEAD"), method = "drawStatusEffects", ordinal = 4, argsOnly = true)
	private int squishEffects(int original) {
		return EmiConfig.effectLocation.compressed ? 32 : original;
	}

    // TODO(Ravel): no target class
// TODO(Ravel): no target class
    @ModifyVariable(at = @At("HEAD"), method = "drawStatusEffects", ordinal = 0, argsOnly = true)
	private int changeEffectSpace(int original) {
        int screenX = ((AbstractContainerScreenAccessor) parent).getLeftPos();
		return switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED, HIDDEN -> original;
			case TOP -> screenX;
			case LEFT_COMPRESSED -> screenX - 2- 32;
			case LEFT -> screenX - 2 - 120;
		};
	}

}
