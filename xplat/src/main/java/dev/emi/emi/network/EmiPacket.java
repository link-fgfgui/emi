package dev.emi.emi.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface EmiPacket extends CustomPacketPayload {
	void write(RegistryFriendlyByteBuf buf);
	
	void apply(Player player);
}
