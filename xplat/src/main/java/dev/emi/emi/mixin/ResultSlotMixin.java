package dev.emi.emi.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiSidebars;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.level.Level;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
	@Shadow @Final
	private CraftingContainer craftSlots;
	@Shadow @Final
	private Player player;
	
	// TODO(Ravel): target method onCrafted with the signature not found
    @Inject(at = @At("HEAD"), method = "onCrafted(Lnet/minecraft/item/ItemStack;)V")
	private void onCrafted(ItemStack stack, CallbackInfo info) {
		Level world = player.getEntityWorld();
		if (world.isClient()) {
			Optional<CraftingRecipe> opt = EmiAgnos.getFirstMatchRecipe(world.getRecipeManager(), RecipeType.CRAFTING, craftSlots.createPositionedRecipeInput().input(), world).map(RecipeHolder::value);
			if (opt.isPresent()) {
				EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(EmiPort.getId(opt.get()));
				if (recipe != null) {
					EmiSidebars.craft(recipe);
				}
			}
		}
	}
}
