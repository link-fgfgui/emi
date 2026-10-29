package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Unmodifiable;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.recipe.RecipeIngredientRole;

/**
 * Backs {@link mezz.jei.api.gui.widgets.IRecipeExtrasBuilder#getRecipeSlots()}
 * with the already-built {@link JemiRecipeSlot}s so that JEI categories calling
 * {@code getRecipeSlots().getSlots(role)} or {@code findSlotByName(name)} receive
 * real drawables that can be queried and repositioned instead of throwing an NPE.
 */
public class JemiRecipeSlotDrawablesView implements IRecipeSlotDrawablesView {
	private final List<IRecipeSlotDrawable> slots;

	public JemiRecipeSlotDrawablesView() {
		this(List.of());
	}

	public JemiRecipeSlotDrawablesView(List<? extends IRecipeSlotDrawable> slots) {
		this.slots = slots != null ? List.copyOf(slots) : List.of();
	}

	@Override
	public @Unmodifiable List<IRecipeSlotDrawable> getSlots() {
		return slots;
	}

	@Override
	public List<IRecipeSlotDrawable> getSlots(RecipeIngredientRole role) {
		return slots.stream().filter(s -> Objects.equals(s.getRole(), role)).toList();
	}

	@Override
	public Optional<IRecipeSlotDrawable> findSlotByName(String slotName) {
		for (IRecipeSlotDrawable slot : slots) {
			if (Objects.equals(slot.getSlotName().orElse(null), slotName)) {
				return Optional.of(slot);
			}
		}
		return Optional.empty();
	}
}
