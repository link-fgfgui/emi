package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.OffsetDrawable;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.TankInfo;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class JemiRecipeSlotBuilder implements IRecipeSlotBuilder {
	public final JemiIngredientAcceptor acceptor;
	public boolean large = false, defaultBackground = false;
	public int x, y;
	public Optional<String> name = Optional.empty();
	public IRecipeSlotRichTooltipCallback richTooltipCallback;
	public OffsetDrawable background, overlay;
	public Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public TankInfo tankInfo;

	public JemiRecipeSlotBuilder(RecipeIngredientRole role, int x, int y) {
		this.acceptor = new JemiIngredientAcceptor(role);
		this.x = x;
		this.y = y;
	}

	@Override
	public ContextMap getContextMap() {
		return acceptor.getContextMap();
	}

	@Override
	public IRecipeSlotBuilder add(SlotDisplay display) {
		acceptor.add(display);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay display) {
		acceptor.add(ingredientType, display);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStack stack) {
		acceptor.add(stack);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemLike item) {
		acceptor.add(item);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStackTemplate template) {
		acceptor.add(template);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid) {
		acceptor.add(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount) {
		acceptor.add(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch componentChanges) {
		acceptor.add(fluid, amount, componentChanges);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Ingredient ingredient) {
		acceptor.add(ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		acceptor.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		acceptor.add(typedIngredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		acceptor.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		acceptor.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		acceptor.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		acceptor.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		acceptor.add(fluid, amount);
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch componentChanges) {
		acceptor.add(fluid, amount, componentChanges);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		name = Optional.ofNullable(slotName);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		this.background = new OffsetDrawable(background, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		this.overlay = new OffsetDrawable(overlay, xOffset, yOffset);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		this.tankInfo = new TankInfo(width, height, capacity, showCapacity);
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType,
			IIngredientRenderer<T> ingredientRenderer) {
		if (renderers == null) {
			renderers = Maps.newHashMap();
		}
		renderers.put(ingredientType, new IngredientRenderer<T>(ingredientType, ingredientRenderer));
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		acceptor.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		acceptor.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		richTooltipCallback = tooltipCallback;
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		acceptor.add(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return this;
	}

	@Override
	public int getWidth() {
		return large ? 26 : 18;
	}

	@Override
	public int getHeight() {
		return large ? 26 : 18;
	}

	@Override
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		acceptor.addItemStacks(itemStacks);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		this.defaultBackground = true;
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		this.defaultBackground = true;
		this.large = true;
		return this;
	}
}
