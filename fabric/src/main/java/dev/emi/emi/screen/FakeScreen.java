package dev.emi.emi.screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class FakeScreen extends Screen {
	public static final FakeScreen INSTANCE = new FakeScreen();

	protected FakeScreen() {
		super(EmiPort.literal(""));
		this.width = Integer.MAX_VALUE;
		this.height = Integer.MAX_VALUE;
	}

	public List<ClientTooltipComponent> getTooltipComponentListFromItem(ItemStack stack) {
		List<ClientTooltipComponent> list = Screen.getTooltipFromItem(client, stack)
			.stream().map(EmiPort::ordered).map(ClientTooltipComponent::of).collect(Collectors.toList());
		Optional<TooltipComponent> data = stack.getTooltipData();
		if (data.isPresent()) {
			try {
				list.add(ClientTooltipComponent.of(data.get()));
			} catch (Throwable e) {
				EmiLog.error("Exception converting TooltipComponent", e);
			}
		}
		return list;
	}
}
