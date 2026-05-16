package dev.emi.emi;

import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiLog;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Multiversion quarantine, to avoid excessive git pain
 */
public final class EmiPort {
	private static final net.minecraft.util.math.random.Random RANDOM = net.minecraft.util.math.random.Random.create();

	public static MutableComponent literal(String s) {
		return MutableComponent.literal(s);
	}

	public static MutableComponent literal(String s, ChatFormatting formatting) {
		return MutableComponent.literal(s).formatted(formatting);
	}

	public static MutableComponent literal(String s, ChatFormatting... formatting) {
		return MutableComponent.literal(s).formatted(formatting);
	}

	public static MutableComponent literal(String s, Style style) {
		return MutableComponent.literal(s).setStyle(style);
	}
	
	public static MutableComponent translatable(String s) {
		return MutableComponent.translatable(s);
	}
	
	public static MutableComponent translatable(String s, ChatFormatting formatting) {
		return MutableComponent.translatable(s).formatted(formatting);
	}
	
	public static MutableComponent translatable(String s, Object... objects) {
		return MutableComponent.translatable(s, objects);
	}

	public static MutableComponent append(MutableComponent text, MutableComponent appended) {
		return text.append(appended);
	}

	public static FormattedCharSequence ordered(MutableComponent text) {
		return text.asOrderedText();
	}

	public static Collection<ResourceLocation> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.findResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	public static InputStream getInputStream(Resource resource) {
		try {
			return resource.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	public static BannerPatternLayers addRandomBanner(BannerPatternLayers patterns, Random random) {
		var bannerRegistry = Minecraft.getInstance().world.getRegistryManager().getOrThrow(Registries.BANNER_PATTERN);
		return new BannerPatternLayers.Builder().addAll(patterns).add(bannerRegistry.getEntry(random.nextInt(bannerRegistry.size())).get(),
			DyeColor.values()[random.nextInt(DyeColor.values().length)]).build();
	}

	public static boolean canTallFlowerDuplicate(TallFlowerBlock tallFlowerBlock) {
		try {
			return tallFlowerBlock.isFertilizable(null, null, null) && tallFlowerBlock.canGrow(null, null, null, null);
		} catch(Exception e) {
			return false;
		}
	}

//	public static void setShader(VertexBuffer buf, Matrix4f mat) {
//		buf.bind();
//		buf.draw(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
//	}

//	public static List<BakedQuad> getQuads(BakedModel model) {
//		return model.getQuads(null, null, RANDOM);
//	}

	public static void draw(BufferBuilder bufferBuilder) {
//		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	public static int getGuiScale(Minecraft client) {
		return (int) client.getWindow().getScaleFactor();
	}

	public static void setPositionTexShader() {
//		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
	}

	public static void setPositionColorTexShader() {
//		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
	}

	public static Registry<Item> getItemRegistry() {
		return BuiltInRegistries.ITEM;
	}

	public static Registry<Block> getBlockRegistry() {
		return BuiltInRegistries.BLOCK;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return BuiltInRegistries.FLUID;
	}

	public static Registry<Potion> getPotionRegistry() {
		return BuiltInRegistries.POTION;
	}

	public static Registry<Enchantment> getEnchantmentRegistry() {
		Minecraft client = Minecraft.getInstance();
		return client.world.getRegistryManager().getOrThrow(Registries.ENCHANTMENT);
	}

	public static Button newButton(int x, int y, int w, int h, MutableComponent name, OnPress action) {
		return Button.builder(name, action).position(x, y).size(w, h).build();
	}

	public static ItemStack getOutput(Recipe<?> recipe) {
        // TODO: review this
        if (recipe.getDisplays().size() > 1) {
            EmiLog.warn("Recipe " + recipe + " has more than one display, still not known how to handle this");
            return ItemStack.EMPTY;
        }

        SlotDisplay slotDisplay = recipe.getDisplays().getFirst().result();
        if (slotDisplay instanceof SlotDisplay.StackSlotDisplay(ItemStack stack)) {
            return stack;
        } else if (slotDisplay instanceof SlotDisplay.SmithingTrimSlotDisplay smithing) {
            Level world = Minecraft.getInstance().world;
            return smithing.getFirst(SlotDisplayContexts.createParameters(Objects.requireNonNull(world)));
        } else {
            EmiLog.warn("Recipe " + recipe + " has an unsupported result slot display: " + slotDisplay.getClass()
                    .getName() + ": " + slotDisplay);
            return ItemStack.EMPTY;
        }
	}

	public static void focus(EditBox widget, boolean focused) {
		// Also ensure a current focus-element in the screen is cleared if it changes
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.currentScreen != null) {
			var currentFocus = client.currentScreen.getFocused();
			if (!focused && currentFocus == widget || focused && currentFocus != widget) {
				client.currentScreen.setFocused(null);
			}
		}
		widget.setFocused(focused);
	}

	public static Stream<Item> getDisabledItems() {
		Minecraft client = Minecraft.getInstance();
		FeatureFlagSet fs = client.world.getEnabledFeatures();
		return getItemRegistry().stream().filter(i -> !i.isEnabled(fs));
	}

	public static ResourceLocation getId(Recipe<?> recipe) {
		return EmiRecipes.recipeIds.get(recipe);
	}

	public static @Nullable RecipeHolder<?> getRecipe(ResourceLocation id) {
		Minecraft client = Minecraft.getInstance();
		if (client.world != null && id != null) {
			RecipeManager manager = client.world.getRecipeManager();
			if (manager != null) {
				return EmiAgnos.getRecipe(manager, id);
			}
		}
		return null;
	}

	public static Comparison compareStrict() {
		return Comparison.compareComponents();
	}

	public static ItemStack setPotion(ItemStack stack, Potion potion) {
		stack.apply(DataComponents.POTION_CONTENTS, PotionContents.DEFAULT, getPotionRegistry().getEntry(potion), PotionContents::with);
		return stack;
	}

	public static DataComponentPatch emptyExtraData() {
		return DataComponentPatch.EMPTY;
	}

	public static ResourceLocation id(String id) {
		return ResourceLocation.of(id);
	}

	public static ResourceLocation id(String namespace, String path) {
		return ResourceLocation.of(namespace, path);
	}

	public static void applyModelViewMatrix() {
//		RenderSystem.applyModelViewMatrix();
	}
}
