package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;

import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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

	@Shadow
	private int extractBackground(final GuiGraphicsExtractor graphics, final Font font, final Component effectName, final Component duration, final int x0, final int y0, final boolean isAmbient, final int maxTextureWidth) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void extractText(final GuiGraphicsExtractor graphics, final Component effectText, final Component duration, final Font font, final int x0, final int y0, final int textureWidth, final int yStep, final int mouseX, final int mouseY) {
		throw new UnsupportedOperationException();
	}

	@Inject(at = @At("HEAD"), method = "extractEffects")
	private void drawStatusEffects(final GuiGraphicsExtractor graphics, final Collection<MobEffectInstance> activeEffects, final int x0, final int yStep, final int mouseX, final int mouseY, final int maxWidth, CallbackInfo ci) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			emi$drawCenteredEffects(graphics, mouseX, mouseY);
		}
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "java/util/Collection.size()I", ordinal = 0),
			method = "extractRenderState", name = "activeEffects")
	private Collection<MobEffectInstance> drawStatusEffects(Collection<MobEffectInstance> original) {
		if (EmiConfig.effectLocation == EffectLocation.HIDDEN) {
			return List.of();
		}
		return original;
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
				this.extractBackground(context.raw(), this.screen.getFont(), this.getEffectName(inst), MobEffectUtil.formatDuration(inst, 1.0f, minecraft.level.tickRateManager().tickrate()), x, y, inst.isAmbient(), ew);
				this.extractText(context.raw(), this.getEffectName(inst), MobEffectUtil.formatDuration(inst, 1.0f, minecraft.level.tickRateManager().tickrate()), this.screen.getFont(), x, y, ew, 33, mouseX, mouseY);
				context.raw().blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, net.minecraft.client.gui.Hud.getMobEffectSprite(inst.getEffect()), x + 7, y + 7, 18, 18);
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

	@ModifyArgs(
			method = "extractRenderState",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/Collection;IIIII)V")
	)
	private void adaptEmiEffectLayout(Args args) {
		int originalXo = args.get(2);
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			args.set(1, List.of());
		}
		int newXo = switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED, HIDDEN -> originalXo;
			case TOP -> ((HandledScreenAccessor) this.screen).getX();
			case LEFT_COMPRESSED -> ((HandledScreenAccessor) this.screen).getX() - 2 - 32;
			case LEFT -> ((HandledScreenAccessor) this.screen).getX() - 2 - 120;
		};
		args.set(2, newXo);

		if (EmiConfig.effectLocation.compressed) {
			args.set(6, 32);
		}
	}
}
