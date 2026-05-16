package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccessor {

	@Accessor("template")
    Ingredient getTemplate();

	@Accessor("base")
    Ingredient getBase();

	@Accessor("addition")
    Ingredient getAddition();
}
