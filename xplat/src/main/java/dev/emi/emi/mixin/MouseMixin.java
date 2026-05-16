// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package dev.emi.emi.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseInput;
import com.mojang.blaze3d.platform.Window;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow @Final
	private Minecraft minecraft;
	@Shadow
	private double xpos, y;
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private @Nullable MouseInput activeButton;

    @Shadow
    public abstract double getScaledX(Window window);

    @Shadow
    public abstract double getScaledY(Window window);

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z"),
            method = "onPress", cancellable = true)
	private void onMouseDown(long window, MouseInput input, int action, CallbackInfo info, @Local(ordinal = 0) Click click, @Local(ordinal = 1) boolean bl2) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof AbstractContainerScreen<?>) {
				if (EmiScreenManager.mouseClicked(click, bl2)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse press", e);
		}
	}

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/gui/Click;)Z"),
            method = "onPress", cancellable = true)
	private void onMouseUp(long window, MouseInput input, int action, CallbackInfo info, @Local(ordinal = 0) Click click) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof AbstractContainerScreen<?>) {
				if (EmiScreenManager.mouseReleased(click)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse release", e);
		}
	}

    @Inject(at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/gui/Click;DD)Z"),
            method = "handleAccumulatedMovement", cancellable = true)
	private void onMouseDragged(CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof AbstractContainerScreen<?>) {
                Window window = this.client.getWindow();
                Click click = new Click(this.getScaledX(window), this.getScaledY(window), this.activeButton);
                double dx = MouseHandler.scaleX(window, this.cursorDeltaX);
                double dy = MouseHandler.scaleY(window, this.cursorDeltaY);
				EmiScreenManager.mouseDragged(click, dx, dy);
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse drag", e);
		}
	}

	@Inject(at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"),
		method = "onScroll(JDD)V", cancellable = true)
	private void onMouseScrolled(long window, double horizontal, double vertical, CallbackInfo info) {
		try {
			Screen screen = client.currentScreen;
			if (screen instanceof AbstractContainerScreen<?> hs) {
				double amount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(vertical) : vertical) * client.options.getMouseWheelSensitivity().getValue();
				double mx = x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
				double my = y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
				if (EmiScreenManager.mouseScrolled(mx, my, amount)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse scroll", e);
		}
	}
}
