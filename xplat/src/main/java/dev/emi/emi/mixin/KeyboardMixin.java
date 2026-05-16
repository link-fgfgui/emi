// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Shadow @Final
	private Minecraft minecraft;
	
	@Inject(at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyInput;)Z"),
		method = "keyPress", cancellable = true)
	public void onKey(long window, int action, KeyInput input, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof AbstractContainerScreen<?> hs) {
				if (action == 1 || action == 2) {
					if (EmiScreenManager.keyPressed(input)) {
						info.cancel();
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling key press", e);
		}
	}
	
	@Inject(at = @At("HEAD"),
		method = "charTyped", cancellable = true)
	public void onChar(long window, CharInput input, CallbackInfo info) {
		try {
			if (window == client.getWindow().getHandle()) {
				Screen screen = client.currentScreen;
				if (screen instanceof AbstractContainerScreen<?> hs && this.client.getOverlay() == null) {
					boolean consume = false;
					if (Character.charCount(input.codepoint()) == 1) {
						consume = EmiScreenManager.search.charTyped(input) || consume;
					} else {
						for (char c : Character.toChars(input.codepoint())) {
							consume = EmiScreenManager.search.charTyped(input) || consume;
						}
					}
					if (consume) {
						info.cancel();
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling char", e);
		}
	}
}
