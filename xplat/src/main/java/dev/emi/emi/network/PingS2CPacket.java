package dev.emi.emi.network;

import dev.emi.emi.platform.EmiClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PingS2CPacket implements EmiPacket {

	public PingS2CPacket() {
	}

	public PingS2CPacket(FriendlyByteBuf buf) {
	}

	@Override
	public void write(RegistryFriendlyByteBuf buf) {
	}

	@Override
	public void apply(Player player) {
		EmiClient.onServer = true;
	}

	@Override
	public Id<PingS2CPacket> getId() {
		return EmiNetwork.PING;
	}
}
