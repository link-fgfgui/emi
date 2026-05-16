package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(Screen.class)
public class ScreenMixin {

	// TODO(Ravel): target method init with the signature not found
    @Inject(at = @At("RETURN"), method = "init(II)V")
	private void init(int width, int height, CallbackInfo ci) {
		if ((Object) this instanceof AbstractContainerScreen<?> hs && Minecraft.getInstance().currentScreen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}

	@Inject(at = @At("RETURN"), method = "resize")
	private void resize(int width, int height, CallbackInfo ci) {
		if ((Object) this instanceof AbstractContainerScreen<?> hs && Minecraft.getInstance().currentScreen == hs) {
			EmiScreenManager.addWidgets(hs);
		}
	}
}
