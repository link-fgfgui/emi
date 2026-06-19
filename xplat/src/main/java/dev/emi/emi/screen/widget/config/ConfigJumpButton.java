package dev.emi.emi.screen.widget.config;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.SizedButtonWidget;

public class ConfigJumpButton extends SizedButtonWidget {

	public ConfigJumpButton(int x, int y, int u, int v, OnPress action, List<Component> text) {
		super(x, y, 16, 16, u, v, () -> true, action, text);
		this.texture = EmiRenderHelper.CONFIG;
	}

	@Override
	protected int getV(int mouseX, int mouseY) {
		return this.v;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		boolean hovered = this.isMouseOver(mouseX, mouseY);
		if (hovered) {
			context.setColor(0.5f, 0.6f, 1f);
		}
		context.enableDepthTest();
		context.drawTexture(texture, this.x, this.y, getU(mouseX, mouseY), getV(mouseX, mouseY), this.width, this.height);
		context.resetColor();
		if (hovered && text != null && this.active) {
			context.push();
			context.disableDepthTest();
			Minecraft client = Minecraft.getInstance();
			EmiRenderHelper.drawTooltip(client.gui.screen(), context, text.get().stream().map(EmiPort::ordered).map(ClientTooltipComponent::create).toList(), mouseX, mouseY);
			context.pop();
		}
	}
}
