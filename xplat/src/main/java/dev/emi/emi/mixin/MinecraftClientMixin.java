package dev.emi.emi.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Shadow
	public ClientLevel level;

	@Inject(at = @At("RETURN"), method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;")
	private void reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> info) {
		CompletableFuture<Void> future = info.getReturnValue();
		if (future != null) {
			future.thenRunAsync(() -> {
				Minecraft client = Minecraft.getInstance();
				if (client.level != null && EmiPort.getRecipeManager() != null) {
					EmiReloadManager.reload();
				}
			}, Executors.newFixedThreadPool(1));
		}
	}

	@Inject(at = @At("HEAD"), method = "disconnectWithProgressScreen")
	private void disconnect(CallbackInfo info) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
