package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.mixin.accessor.ResultSlotAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

public class CoercedRecipeHandler<T extends AbstractContainerMenu> implements StandardRecipeHandler<T> {
	private ResultSlot output;
	private CraftingContainer inv;

	public CoercedRecipeHandler(ResultSlot output) {
		this.output = output;
		this.inv = ((ResultSlotAccessor) output).getCraftSlots();
	}

	@Override
	public ResultSlot getOutputSlot(AbstractContainerMenu handler) {
		return output;
	}

	@Override
	public List<ResultSlot> getInputSources(AbstractContainerMenu handler) {
		Minecraft client = Minecraft.getInstance();
		List<ResultSlot> slots = Lists.newArrayList();
		if (output != null) {
			for (ResultSlot slot : handler.slots) {
				if (slot.isEnabled() && slot.canTakeItems(client.player) && slot != output) {
					slots.add(slot);
				}
			}
		}
		return slots;
	}

	@Override
	public List<ResultSlot> getCraftingSlots(AbstractContainerMenu handler) {
		List<ResultSlot> slots = Lists.newArrayList();
		int width = inv.getWidth();
		int height = inv.getHeight();
		for (int i = 0; i < 9; i++) {
			slots.add(null);
		}
		for (ResultSlot slot : handler.slots) {
			if (slot.inventory == inv && slot.getIndex() < width * height && slot.getIndex() >= 0) {
				int index = slot.getIndex();
				index = index * 3 / width;
				slots.set(index, slot);
			}
		}
		return slots;
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(inv.getWidth(), inv.getHeight());
			}
			return true;
		}
		return false;
	}
}
