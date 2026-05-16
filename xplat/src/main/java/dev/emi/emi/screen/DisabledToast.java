package dev.emi.emi.screen;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public class DisabledToast implements Toast {
	private static final ResourceLocation TEXTURE = EmiPort.id("toast/advancement");
// TODO
//	@Override
//	public Visibility draw(DrawContext raw, ToastManager manager, long time) {
//		EmiDrawContext context = EmiDrawContext.wrap(raw);
//		context.resetColor();
//		raw.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
//		context.drawCenteredText(EmiPort.translatable("emi.disabled"), getWidth() / 2, 7);
//		context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), getWidth() / 2, 18);
//		if (time > 8_000 || EmiConfig.enabled) {
//			return Visibility.HIDE;
//		}
//		return Visibility.SHOW;
//	}

    @Override
    public Visibility getVisibility() {
//        EmiDrawContext context = EmiDrawContext.wrap(raw);
//        context.resetColor();
//        raw.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
//        context.drawCenteredText(EmiPort.translatable("emi.disabled"), getWidth() / 2, 7);
//        context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), getWidth() / 2, 18);
//        if (time > 8_000 || EmiConfig.enabled) {
//            return Visibility.HIDE;
//        }
        return Visibility.SHOW;
    }

    @Override
    public void update(ToastComponent manager, long time) {

    }

    @Override
    public void draw(DrawContext context, Font textRenderer, long startTime) {
//        EmiDrawContext context = EmiDrawContext.wrap(raw);
//        context.resetColor();
//        raw.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
//        context.drawCenteredText(EmiPort.translatable("emi.disabled"), getWidth() / 2, 7);
//        context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), getWidth() / 2, 18);
//        if (time > 8_000 || EmiConfig.enabled) {
//            return Visibility.HIDE;
//        }
//        return Visibility.SHOW;
    }
}
