package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import dev.emi.emi.api.widget.Bounds;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.tags.TagKey;

@SuppressWarnings("unchecked")
public class JemiRecipeSlot implements IRecipeSlotDrawable {
	public final RecipeIngredientRole role;
	public final boolean large, defaultBackground;
	public int x, y;
	public final Optional<String> name;
	public final IRecipeSlotRichTooltipCallback richTooltipCallback;
	public final OffsetDrawable background, overlay;
	public final Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public final TankInfo tankInfo;
	public final EmiIngredient stack;
	public SlotWidget widget;
	public int highlight = 0;

	public JemiRecipeSlot(JemiRecipeSlotBuilder builder) {
		this.role = builder.acceptor.role;
		this.large = builder.large;
		this.defaultBackground = builder.defaultBackground;
		this.x = builder.x;
		this.y = builder.y;
		this.name = builder.name;
		this.richTooltipCallback = builder.richTooltipCallback;
		this.background = builder.background;
		this.overlay = builder.overlay;
		this.renderers = builder.renderers;
		this.tankInfo = builder.tankInfo;
		this.stack = builder.acceptor.build();
	}

	public JemiRecipeSlot(RecipeIngredientRole role, EmiStack stack) {
		this.role = role;
		this.large = false;
		this.defaultBackground = false;
		this.x = 0;
		this.y = 0;
		this.name = Optional.empty();
		this.richTooltipCallback = null;
		this.background = null;
		this.overlay = null;
		this.renderers = null;
		this.tankInfo = null;
		this.stack = stack;
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return (Stream<T>) getAllIngredients().filter(t -> t.getType() == ingredientType).map(t -> t.getIngredient());
	}

	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return getAllIngredients().toList();
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return stack.getEmiStacks().stream().map(JemiUtil::getTyped).filter(Optional::isPresent).map(Optional::get);
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		Optional<ITypedIngredient<?>> ing = getDisplayedIngredient();
		if (ing.isPresent() && ing.get().getType() == ingredientType) {
			return (Optional<T>) Optional.of(ing.get().getIngredient());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return JemiUtil.getTyped(stack.getEmiStacks().get(0));
	}

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		return getAllIngredients();
	}

	@Override
	public Optional<String> getSlotName() {
		return name;
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return stack instanceof TagEmiIngredient tag ? Optional.of(tag.key) : Optional.empty();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(GuiGraphicsExtractor raw, int color) {
		this.highlight = color;
	}

	@Override
	public void setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		int size = large ? 26 : 18;
		return new Rect2i(x - 1, y - 1, size, size);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (widget != null) {
			Bounds b = widget.getBounds();
			return mouseX >= b.x() && mouseX < b.x() + b.width()
				&& mouseY >= b.y() && mouseY < b.y() + b.height();
		}
		return false;
	}

	@Override
	public void draw(GuiGraphicsExtractor raw) {
		// EMI renders slot contents via JemiSlotWidget; nothing to do here.
	}

	@Override
	public void draw(GuiGraphicsExtractor raw, boolean highlight) {
		// EMI renders slot contents via JemiSlotWidget; nothing to do here.
	}

	@Override
	public void drawHoverOverlays(GuiGraphicsExtractor raw) {
		// No-op
	}

	@Override
	public List<Component> getTooltip() {
		return List.of();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltipBuilder) {
		// No-op; EMI handles tooltips via JemiSlotWidget.
	}

	@Override
	public void drawTooltip(GuiGraphicsExtractor raw, int mouseX, int mouseY) {
		// No-op
	}

	@Override
	public IIngredientAcceptor<?> createDisplayOverrides() {
		return new JemiIngredientAcceptor(role);
	}

	@Override
	public void clearDisplayOverrides() {
		// No-op
	}

	public static record OffsetDrawable(IDrawable drawable, int xOff, int yOff){
	}

	public static record IngredientRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer){
	}

	public static record TankInfo(int width, int height, long capacity, boolean showCapacity) {
	}
}
