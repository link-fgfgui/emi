package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;

@Mixin(ResultSlot.class)
public interface ResultSlotAccessor {
	
	@Accessor("craftSlots")
    CraftingContainer getCraftSlots();
}
