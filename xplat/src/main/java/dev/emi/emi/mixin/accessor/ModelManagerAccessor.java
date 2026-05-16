package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.model.ModelManager;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelManager.class)
public interface ModelManagerAccessor {

//	@Accessor("models")
//    Map<ModelIdentifier, BakedModel> getModels();
}
