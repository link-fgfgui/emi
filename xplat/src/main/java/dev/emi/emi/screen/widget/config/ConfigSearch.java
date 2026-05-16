package dev.emi.emi.screen.widget.config;

import dev.emi.emi.EmiPort;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class ConfigSearch {
	public final ConfigSearchWidgetField field;

	public ConfigSearch(int x, int y, int width, int height) {
		Minecraft client = Minecraft.getInstance();

		field = new ConfigSearchWidgetField(client.textRenderer, x, y, width, height, EmiPort.literal(""));
		field.setChangedListener(s -> {
			if (s.length() > 0) {
				field.setSuggestion("");
			} else {
				field.setSuggestion(I18n.translate("emi.search_config"));
			}
		});
		field.setSuggestion(I18n.translate("emi.search_config"));
	}

	public void setText(String query) {
		field.setText(query);
	}

	public String getSearch() {
		return field.getText();
	}
	
	private class ConfigSearchWidgetField extends EditBox {

		public ConfigSearchWidgetField(Font textRenderer, int x, int y, int width, int height, Component text) {
			super(textRenderer, x, y, width, height, text);
		}

        @Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (click.button() == 1 && isMouseOver(click.x(), click.y())) {
				this.setText("");
				EmiPort.focus(this, true);
				return true;
			}
            return super.mouseClicked(click, doubled);
		}
	}
}
