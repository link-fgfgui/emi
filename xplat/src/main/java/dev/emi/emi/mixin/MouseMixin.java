package dev.emi.emi.mixin;

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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
	@Shadow @Final
	private Minecraft minecraft;
	@Shadow
	private double xpos, ypos;
	@Shadow
	private MouseButtonInfo activeButton;

	@Shadow private double accumulatedDX;

	@Shadow private double accumulatedDY;

	@Inject(at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"),
			method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", cancellable = true)
	private void onMouseDown(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo info) {
		try {
			Screen screen = minecraft.gui.screen();
			if (screen instanceof AbstractContainerScreen<?> hs) {
				double mx = this.xpos * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double my = this.ypos * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				if (EmiScreenManager.mouseClicked(new MouseButtonEvent(mx, my, buttonInfo))) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse press", e);
		}
	}

	@Inject(at = @At(value = "INVOKE", ordinal = 0,
			target = "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z"),
			method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", cancellable = true)
	private void onMouseUp(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo info) {
		try {
			Screen screen = minecraft.gui.screen();
			if (screen instanceof AbstractContainerScreen<?> hs) {
				double mx = this.xpos * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double my = this.ypos * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				if (EmiScreenManager.mouseReleased(new MouseButtonEvent(mx, my, buttonInfo))) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse release", e);
		}
	}

	@Inject(at = @At("HEAD"),
			method = "handleAccumulatedMovement")
	private void onMouseDragged(CallbackInfo info) {
		try {
			Screen screen = minecraft.gui.screen();
			if (screen instanceof AbstractContainerScreen<?> hs && activeButton != null) {
				double mx = this.xpos * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double my = this.ypos * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				double dx = this.accumulatedDX * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double dy = this.accumulatedDY * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				if (dx != 0 || dy != 0) {
					EmiScreenManager.mouseDragged(new MouseButtonEvent(mx, my, activeButton), dx, dy);
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse drag", e);
		}
	}

	@Inject(at = @At("HEAD"),
			method = "onScroll(JDD)V", cancellable = true)
	private void onMouseScrolled(long window, double horizontal, double vertical, CallbackInfo info) {
		try {
			Screen screen = minecraft.gui.screen();
			if (screen instanceof AbstractContainerScreen<?> hs) {
				double amount = (minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * minecraft.options.mouseWheelSensitivity().get();
				double mx = xpos * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
				double my = ypos * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
				if (EmiScreenManager.mouseScrolled(mx, my, amount)) {
					info.cancel();
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse scroll", e);
		}
	}
}
