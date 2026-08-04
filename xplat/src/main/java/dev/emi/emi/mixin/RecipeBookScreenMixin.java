package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;

/**
 * {@link AbstractRecipeBookScreen#extractRenderState} overrides the parent
 * without calling super, so the TAIL injection in {@link HandledScreenMixin}
 * never fires for recipe book screens. Mirror that injection here so the EMI
 * dragged stack and deferred tooltips render above slot items on crafting
 * table / furnace / brewing stand etc. screens.
 */
@Mixin(AbstractRecipeBookScreen.class)
public class RecipeBookScreenMixin {

	@Inject(at = @At("TAIL"),
			method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
	private void emi$renderTail(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (EmiAgnos.isForge()) {
			return;
		}
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		// At TAIL of AbstractRecipeBookScreen.extractRenderState, extractContents,
		// extractCarriedItem, extractSnapbackItem and extractTooltip have all run,
		// so the dragged stack's nextStratum() lands above slot items and the
		// vanilla carried item — matching NeoForge's ScreenEvent.Render.Post path.
		EmiScreenManager.drawForeground(context, mouseX, mouseY, delta);
		context.pop();
		context.flushDeferredTooltips();
	}
}
