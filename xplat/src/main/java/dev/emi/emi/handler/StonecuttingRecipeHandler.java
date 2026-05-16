package dev.emi.emi.handler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.Level;

public class StonecuttingRecipeHandler implements StandardRecipeHandler<StonecutterMenu> {

	@Override
	public List<Slot> getInputSources(StonecutterMenu handler) {
		List<Slot> list = Lists.newArrayList();
		list.add(handler.getSlot(0));
		int invStart = 2;
		for (int i = invStart; i < invStart + 36; i++) { 
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(StonecutterMenu handler) {
		return List.of(handler.slots.get(0));
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.STONECUTTING;
	}

	@Override
	public @Nullable Slot getOutputSlot(StonecutterMenu handler) {
		return handler.getSlot(1);
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<StonecutterMenu> context) {
		boolean action = StandardRecipeHandler.super.craft(recipe, context);
		Minecraft client = Minecraft.getInstance();
		Level world = client.world;
		SingleRecipeInput inv = new SingleStackRecipeInput(recipe.getInputs().get(0).getEmiStacks().get(0).getItemStack());
		List<StonecutterRecipe> recipes = world.getRecipeManager().getStonecutterRecipes().entries().stream()
                .map((it)->it.recipe().recipe().get().value()).toList();
		for (int i = 0; i < recipes.size(); i++) {
			if (EmiPort.getId(recipes.get(i)) != null && EmiPort.getId(recipes.get(i)).equals(recipe.getId())) {
				StonecutterMenu sh = context.getScreenHandler();
				client.interactionManager.clickButton(sh.syncId, i);
				if (context.getDestination() == EmiCraftContext.Destination.CURSOR) {
					client.interactionManager.clickSlot(sh.syncId, 1, 0, ClickType.PICKUP, client.player);
				} else if (context.getDestination() == EmiCraftContext.Destination.INVENTORY) {
					client.interactionManager.clickSlot(sh.syncId, 1, 0, ClickType.QUICK_MOVE, client.player);
				}
				break;
			}
		}
		return action;
	}
}
