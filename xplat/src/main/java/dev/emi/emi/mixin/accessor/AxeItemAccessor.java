package dev.emi.emi.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.AxeItem;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
	
	@Accessor("STRIPPABLES")
	static Map<Block, Block> getStrippedBlocks() {
		throw new UnsupportedOperationException();
	}
}
