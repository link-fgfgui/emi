package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	
	@Accessor("hoveredSlot")
    Slot getHoveredSlot();

	@Accessor("leftPos")
	int getLeftPos();

	@Accessor("topPos")
	int getTopPos();

    @Accessor("topPos")
    void setTopPos(int y);

	@Accessor("imageWidth")
	int getImageWidth();

	@Accessor("imageHeight")
	int getImageHeight();

}
