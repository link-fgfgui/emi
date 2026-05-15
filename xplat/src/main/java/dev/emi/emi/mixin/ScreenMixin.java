package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(at = @At("RETURN"), method = "init(II)V")
	private void init(int width, int height, CallbackInfo info) {
		Minecraft client = Minecraft.getInstance();
		if ((Object) this instanceof AbstractContainerScreen hs && client.gui.screen() == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	@Inject(at = @At("RETURN"), method = "resize(II)V")
	private void resize(int width, int height, CallbackInfo info) {
		Minecraft client = Minecraft.getInstance();
		if ((Object) this instanceof AbstractContainerScreen hs && client.gui.screen() == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	@Inject(at = @At("TAIL"), method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
	private void renderWithTooltipTail(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if ((Object) this instanceof AbstractContainerScreen && !EmiAgnos.isForge()) {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			context.flushDeferredTooltips();
		}
	}
}
