package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.mixin.accessor.ResultSlotAccessor;
import dev.emi.emi.mixin.accessor.AbstractContainerScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;

public class EmiStackProviders {
	public static Map<Class<?>, List<EmiStackProvider<?>>> fromClass = Maps.newHashMap();
	public static List<EmiStackProvider<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiStackInteraction getStackAt(Screen screen, int x, int y, boolean notClick) {
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiStackProvider provider : fromClass.get(screen.getClass())) {
				EmiStackInteraction stack = provider.getStackAt(screen, x, y);
				if (!stack.isEmpty() && (notClick || stack.isClickable())) {
					return stack;
				}
			}
		}
		for (EmiStackProvider handler : generic) {
			EmiStackInteraction stack = handler.getStackAt(screen, x, y);
			if (!stack.isEmpty() && (notClick || stack.isClickable())) {
				return stack;
			}
		}
		if (notClick && screen instanceof AbstractContainerScreenAccessor handled) {
			ResultSlot s = handled.getFocusedSlot();
			if (s != null) {
				ItemStack stack = s.getStack();
				if (!stack.isEmpty()) {
					if (s instanceof ResultSlot craf) {
						// Emi be making assumptions
						try {
							CraftingContainer inv = ((ResultSlotAccessor) craf).getCraftSlots();
							CraftingInput input = CraftingInput.create(inv.getWidth(), inv.getHeight(), inv.getHeldStacks());
							Minecraft client = Minecraft.getInstance();
                            List<CraftingRecipe> list
								= EmiAgnos.getAllMatchesRecipe(client.world.getRecipeManager(), RecipeType.CRAFTING, input, client.world).map(RecipeHolder::value).toList();
							if (!list.isEmpty()) {
								ResourceLocation id = EmiPort.getId(list.get(0));
								EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
								if (recipe != null) {
									return new EmiStackInteraction(EmiStack.of(stack), recipe, false);
								}
							}
						} catch (Exception e) {
						}
					}
					return new EmiStackInteraction(EmiStack.of(stack));
				}
			}
		}
		return EmiStackInteraction.EMPTY;
	}
}
