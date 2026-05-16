// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	@Shadow
	protected int imageWidth, backgroundHeight, x, y;

	private AbstractContainerScreenMixin() { super(null); }

	// TODO(Ravel): target method renderBackground with the signature not found
    @Dynamic
	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
			shift = Shift.AFTER),
		method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	private void renderBackground(DrawContext raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		EmiScreenManager.drawBackground(context, mouseX, mouseY, delta);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
			shift = Shift.AFTER),
		method = "renderMain")
	private void renderForeground(DrawContext raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (EmiAgnos.isForge()) {
			return;
		}
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.matrices().translate(-x, -y/*, 0.0*/);
		EmiPort.setPositionTexShader();
		EmiScreenManager.render(context, mouseX, mouseY, delta);
		EmiScreenManager.drawForeground(context, mouseX, mouseY, delta);
		context.pop();
	}
}