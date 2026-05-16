package dev.emi.emi.screen.tooltip;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

public interface EmiTooltipComponent extends ClientTooltipComponent {

	default void drawTooltip(EmiDrawContext context, TooltipRenderData tooltip) {
	}

	default void drawTooltipText(TextRenderData text) {
	}

    @Override
    default void drawItems(Font textRenderer, int x, int y, int width, int height, DrawContext raw) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.push();
        context.matrices().translate(x, y/*, 0*/);
        Minecraft client = Minecraft.getInstance();
        drawTooltip(context, new TooltipRenderData(textRenderer, client.getItemRenderer(), x, y));
        context.pop();
    }

    @Override
    default void drawText(DrawContext raw, Font textRenderer, int x, int y) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.push();
        context.matrices().translate(x, y/*, 0*/);
        drawTooltipText(new TextRenderData(context, textRenderer, x, y));
        context.pop();
    }

	public static class TextRenderData {
        private final EmiDrawContext context;
		public final Font renderer;
		public final int x, y;
		
		public TextRenderData(EmiDrawContext context, Font renderer, int x, int y) {
            this.context = context;
            this.renderer = renderer;
			this.x = x;
			this.y = y;
		}

		public void draw(String text, int x, int y, int color, boolean shadow) {
			draw(EmiPort.literal(text), x, y, color, shadow);
		}

		public void draw(Component text, int x, int y, int color, boolean shadow) {
            if (shadow) {
                context.drawTextWithShadow(text, x, y, color);
            } else {
                context.drawText(text, x + this.x, y + this.y, color);
            }
		}
	}

	public static class TooltipRenderData {
		public final Font text;
		public final ItemRenderer item;
		public final int x, y;

		public TooltipRenderData(Font text, ItemRenderer item, int x, int y) {
			this.text = text;
			this.item = item;
			this.x = x;
			this.y = y;
		}
	}
}
