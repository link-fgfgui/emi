package dev.emi.emi.screen.widget.config;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.ListWidget.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

public class GroupNameWidget extends Entry {
	protected static final Minecraft CLIENT = Minecraft.getInstance();
	public final String id;
	public final Component text;
	public final List<ConfigEntryWidget> children = Lists.newArrayList();
	public boolean collapsed = false;

	public GroupNameWidget(String id, Component text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.drawCenteredTextWithShadow(text, x + width / 2, y + 3, -1);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x + width / 2 - CLIENT.textRenderer.getWidth(text) / 2 - 20;
			if (collapsed) {
				collapse = "[+]";
			}
			context.drawTextWithShadow(EmiPort.literal(collapse), cx, y + 3, -1);
		}
	}

	@Override
	public int getHeight() {
		for (ConfigEntryWidget w : children) {
			if (w.isVisible()) {
				return 20;
			}
		}
		return 0;
	}

    @Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (isMouseOver(click.x(), click.y())) {
			collapsed = !collapsed;
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			return true;
		}
		return false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}
}
