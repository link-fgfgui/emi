package dev.emi.emi.mixin.accessor;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(HoeItem.class)
public interface HoeItemAccessor {

	@Accessor("TILLABLES")
	public static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> getTillingActions() {
		throw new AssertionError();
	}
}
