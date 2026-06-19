package dev.emi.emi;

import dev.emi.emi.mixin.accessor.SmithingTransformRecipeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.Random;
import java.util.stream.Stream;

public class EmiPortClient {
    public static BannerPatternLayers addRandomBanner(BannerPatternLayers patterns, Random random) {
        Minecraft client = Minecraft.getInstance();
        var bannerRegistry = client.level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        return new BannerPatternLayers.Builder().addAll(patterns).add(bannerRegistry.get(random.nextInt(bannerRegistry.size())).orElseThrow(),
                DyeColor.values()[random.nextInt(DyeColor.values().length)]).build();
    }

    public static Registry<Enchantment> getEnchantmentRegistry() {
        Minecraft client = Minecraft.getInstance();
        return client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }

    public static ItemStack getOutput(Recipe<?> recipe) {
        if (recipe instanceof CraftingRecipe crafting) {
            try {
                ItemStack stack = crafting.assemble(CraftingInput.EMPTY);
                if (!stack.isEmpty()) {
                    return stack;
                }
            } catch (Exception e) {
            }
        } else if (recipe instanceof SingleItemRecipe single) {
            return single.assemble(new SingleRecipeInput(ItemStack.EMPTY));
        } else if (recipe instanceof SmithingTransformRecipe smithing) {
            ItemStackTemplate result = ((SmithingTransformRecipeAccessor) smithing).getResult();
            return result.create();
        } else if (recipe instanceof SmithingRecipe smithing) {
            try {
                ItemStack templateStack = smithing.templateIngredient()
                        .flatMap(i -> i.items().findFirst().map(h -> new ItemStack(h.value())))
                        .orElse(ItemStack.EMPTY);
                ItemStack baseStack = smithing.baseIngredient()
                        .items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
                ItemStack additionStack = smithing.additionIngredient()
                        .flatMap(i -> i.items().findFirst().map(h -> new ItemStack(h.value())))
                        .orElse(ItemStack.EMPTY);
                ItemStack result = smithing.assemble(new SmithingRecipeInput(templateStack, baseStack, additionStack));
                if (!result.isEmpty()) return result;
            } catch (Exception e) {
            }
        }
        Minecraft client = Minecraft.getInstance();
        for (var display : recipe.display()) {
            ItemStack stack = display.result().resolveForFirstStack(SlotDisplayContext.fromLevel(client.level));
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void focus(EditBox widget, boolean focused) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.gui.screen() != null) {
            var currentFocus = client.gui.screen().getFocused();
            if (!focused && currentFocus == widget || focused && currentFocus != widget) {
                client.gui.screen().setFocused(null);
            }
        }
        widget.setFocused(focused);
    }

    public static Stream<Item> getDisabledItems() {
        Minecraft client = Minecraft.getInstance();
        FeatureFlagSet fs = client.level.enabledFeatures();
        return EmiPort.getItemRegistry().stream().filter(i -> !i.isEnabled(fs));
    }
}
