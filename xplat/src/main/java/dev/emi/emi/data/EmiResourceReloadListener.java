package dev.emi.emi.data;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.resources.ResourceLocation;

public interface EmiResourceReloadListener extends PreparableReloadListener {
	
	ResourceLocation getEmiId();
}
