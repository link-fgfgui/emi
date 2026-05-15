package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.EffectsInInventoryInvoker;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;

@Mixin(EffectsInInventory.class)
public abstract class AbstractInventoryScreenMixin {
	@Unique
	private static boolean hasInventoryTabs = EmiAgnos.isModLoaded("inventorytabs");

	@Shadow
	private AbstractContainerScreen<?> screen;

	@Shadow
	private Minecraft minecraft;

	@Shadow
	private Component getEffectName(MobEffectInstance effect) {
		throw new UnsupportedOperationException();
	}

	@Inject(at = @At("HEAD"), method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", cancellable = true)
	private void drawStatusEffects(GuiGraphicsExtractor draw, int mouseX, int mouseY, CallbackInfo info) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			emi$drawCenteredEffects(draw, mouseX, mouseY);
			info.cancel();
		} else if (EmiConfig.effectLocation == EffectLocation.HIDDEN) {
			info.cancel();
		}
	}

	private void emi$drawCenteredEffects(GuiGraphicsExtractor raw, int mouseX, int mouseY) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		Collection<MobEffectInstance> effects = Ordering.natural().sortedCopy(this.minecraft.player.getActiveEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}
		boolean wide = size == 1;
		HandledScreenAccessor acc = (HandledScreenAccessor) this.screen;
		int y = acc.getY() - 34;
		if (this.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen || hasInventoryTabs) {
			y -= 28;
			if (this.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen && EmiAgnos.isForge()) {
				y -= 22;
			}
		}
		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (acc.getBackgroundWidth() - 32) / (size - 1);
		}
		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = acc.getX() + (acc.getBackgroundWidth() - width) / 2;
		MobEffectInstance hovered = null;
		int restoreY = acc.getY();
		try {
			acc.setY(y);
			for (MobEffectInstance inst : effects) {
				int ew = wide ? 120 : 32;
				List<MobEffectInstance> single = List.of(inst);
				((EffectsInInventoryInvoker) (Object) this).emi$invokeRenderBackground(context.raw(), this.screen.getFont(), this.getEffectName(inst), MobEffectUtil.formatDuration(inst, 1.0f, minecraft.level.tickRateManager().tickrate()), x, 32, inst.isAmbient(), ew);
				((EffectsInInventoryInvoker) (Object) this).emi$invokeRenderText(context.raw(), this.getEffectName(inst), MobEffectUtil.formatDuration(inst, 1.0f, minecraft.level.tickRateManager().tickrate()), this.screen.getFont(), x, 32, ew, 33, mouseX, mouseY);
				context.raw().blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, net.minecraft.client.gui.Hud.getMobEffectSprite(inst.getEffect()), x + 7, 32 + 7, 18, 18);
				if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
					hovered = inst;
				}
				x += xOff;
			}
		} finally {
			acc.setY(restoreY);
		}
		if (hovered != null && size > 1) {
			List<Component> list = List.of(this.getEffectName(hovered), MobEffectUtil.formatDuration(hovered, 1.0f, minecraft.level.tickRateManager().tickrate()));
			List<ClientTooltipComponent> components = list.stream()
					.map(Component::getVisualOrderText)
					.map(ClientTooltipComponent::create)
					.toList();
			context.deferTooltip(() -> context.raw().tooltip(minecraft.font, components, mouseX, Math.max(mouseY, 16), DefaultTooltipPositioner.INSTANCE, null));
		}
	}
}
