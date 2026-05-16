package dev.emi.emi.platform.fabric;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import dev.emi.emi.runtime.EmiLog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class EmiMainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		EmiMain.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> EmiCommands.registerCommands(dispatcher));

		EmiNetwork.initServer(ServerPlayNetworking::send);

		registerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
		registerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);

		PayloadTypeRegistry.playS2C().register(EmiNetwork.PING, StreamCodec.ofStatic((buf, v) -> v.write(buf), PingS2CPacket::new));
		PayloadTypeRegistry.playS2C().register(EmiNetwork.COMMAND, StreamCodec.ofStatic((buf, v) -> v.write(buf), CommandS2CPacket::new));
		PayloadTypeRegistry.playS2C().register(EmiNetwork.CHESS, StreamCodec.ofStatic((buf, v) -> v.write(buf), EmiChessPacket.S2C::new));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EmiNetwork.sendToClient(handler.player, new PingS2CPacket());
		});

        // Run through vanilla recipe serializers and sync them
        for (var entry : BuiltInRegistries.RECIPE_SERIALIZER.getEntrySet()) {
            ResourceKey<RecipeSerializer<?>> resourceKey = entry.getKey();
            if (resourceKey.getValue().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                RecipeSerializer<?> serializer = entry.getValue();
                try {
                    RecipeSynchronization.synchronizeRecipeSerializer(serializer);
                } catch (RuntimeException e) {
                    EmiLog.error("Failed to synchronize recipe serializer", e);
                }
            }
        }
	}

	private <T extends EmiPacket> void registerPacketReader(CustomPacketPayload.Type<T> id, StreamDecoder<RegistryFriendlyByteBuf, T> decode) {
		PayloadTypeRegistry.playC2S().register(id, StreamCodec.ofStatic((buf, v) -> v.write(buf), decode));
		ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
			context.server().execute(() -> {
				((EmiPacket) payload).apply(context.player());
			});
		});
	}
}