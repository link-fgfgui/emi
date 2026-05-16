package dev.emi.emi.data;

import java.util.List;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;

public record EmiAlias(List<EmiIngredient> stacks, List<String> keys) {

	public static record Baked(List<EmiIngredient> stacks, List<Component> text) {
	}
}
