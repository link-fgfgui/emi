package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;

public class EmiBannerDuplicateRecipe extends EmiPatternCraftingRecipe {

	public static final List<Item> BANNERS = Items.BANNER.asList();

	private final Item banner;

	public EmiBannerDuplicateRecipe(Item banner, Identifier id) {
		super(List.of(
				EmiStack.of(banner),
				EmiStack.of(banner).setRemainder(EmiStack.of(banner))),
				EmiStack.of(banner), id);
		this.banner = banner;
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(banner), x, y);
		} else if (slot == 1) {
			return new GeneratedSlotWidget(r -> getPattern(r, true), unique, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getPattern(r, false) , unique, x, y);
	}

	public EmiStack getPattern(Random random, boolean reminder) {
		ItemStack stack = new ItemStack(banner);
		int patterns = 1 + Math.max(random.nextInt(5), random.nextInt(3));
		BannerPatternLayers pattern = BannerPatternLayers.EMPTY;
		for (int i = 0; i < patterns; i++) {
			pattern = EmiPort.addRandomBanner(pattern, random);
		}

		stack.set(DataComponents.BANNER_PATTERNS, pattern);

		EmiStack emiStack = EmiStack.of(stack);
		if (reminder) {
			emiStack.setRemainder(EmiStack.of(stack));
		}
		return emiStack;
	}
}
