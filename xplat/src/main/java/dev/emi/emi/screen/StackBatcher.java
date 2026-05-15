package dev.emi.emi.screen;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.ItemStackRenderStateAccessor;
import dev.emi.emi.runtime.EmiLog;

public class StackBatcher {
	private static MethodHandle sodiumSpriteHandle;

	static {
		try {
			Class<?> clazz = null;
			try {
				clazz = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
			} catch (Throwable t) {
			}
			sodiumSpriteHandle = MethodHandles.lookup()
				.findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, TextureAtlasSprite.class));
			if (sodiumSpriteHandle != null) {
				EmiLog.info("Discovered Sodium");
			}
		} catch (Throwable e) {
		}
	}

	public interface Batchable {
		boolean isSideLit();
		boolean isUnbatchable();
		void setUnbatchable();
		void renderForBatch(VertexConsumerProvider vcp, GuiGraphicsExtractor draw, int x, int y, int z, float delta);
	}

	@FunctionalInterface
	public interface VertexConsumerProvider {
		VertexConsumer getBuffer(RenderType renderType);
	}

	private final StagedVertexBuffer stagedBuffer;
	private final Map<RenderType, StagedVertexBuffer.Draw> draws = new LinkedHashMap<>();
	private final VertexConsumerProvider imm;
	private final VertexConsumerProvider unlitFacade;
	private final Set<TextureAtlasSprite> spritesToUpdate = Sets.newHashSet();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;

	public static final List<RenderType> EXTRA_RENDER_LAYERS = Lists.newArrayList();

	public static boolean isEnabled() {
		return EmiConfig.useBatchedRenderer;
	}

	public StackBatcher() {
		this.stagedBuffer = new StagedVertexBuffer(() -> "EMI StackBatcher", 256);
		this.imm = new BatcherVertexConsumerProvider();
		this.unlitFacade = new UnlitFacade(imm);
	}

	public boolean isPopulated() {
		return populated;
	}

	public void repopulate() {
		dirty = true;
	}

	public void begin(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (dirty) {
			populated = false;
			dirty = false;
			spritesToUpdate.clear();
			stagedBuffer.endDraw();
			stagedBuffer.endFrame();
			draws.clear();
		}
	}

	public void render(Batchable batchable, GuiGraphicsExtractor draw, int x, int y, float delta) {
		if (!populated) {
			try {
				batchable.renderForBatch(batchable.isSideLit() ? imm : unlitFacade, draw, x-this.x, y+this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					EmiLog.error("Batchable threw exception during batched rendering. See log for info", t);
				}
				batchable.setUnbatchable();
			}
		}
	}

	public void render(EmiIngredient stack, GuiGraphicsExtractor draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, GuiGraphicsExtractor draw, int x, int y, float delta, int flags) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled() && (flags & EmiIngredient.RENDER_ICON) != 0) {
			if (!populated) {
				try {
					b.renderForBatch(b.isSideLit() ? imm : unlitFacade, draw, x-this.x, y + this.y, z, delta);
					if (sodiumSpriteHandle != null && !stack.isEmpty()) {
						ItemStack is = stack.getEmiStacks().get(0).getItemStack();
						Minecraft client = Minecraft.getInstance();
						ItemStackRenderState renderState = new ItemStackRenderState();
						client.getItemModelResolver().updateForTopItem(renderState, is, ItemDisplayContext.GUI, client.level, null, 0);
						if (((ItemStackRenderStateAccessor) renderState).emi$getActiveLayerCount() > 0) {
							ItemStackRenderState.LayerRenderState layer = ((ItemStackRenderStateAccessor) renderState).emi$getLayers()[0];
							List<BakedQuad> quads = layer.prepareQuadList();
							for (BakedQuad quad : quads) {
								if (quad != null) {
									spritesToUpdate.add(quad.materialInfo().sprite());
								}
							}
						}
					}
				} catch (Throwable t) {
					if (EmiConfig.devMode) {
						EmiLog.error("Stack threw exception during batched rendering. See log for info", t);
					}
					b.setUnbatchable();
				}
			}
			stack.render(draw, x, y, delta, flags & (~EmiIngredient.RENDER_ICON));
		} else {
			stack.render(draw, x, y, delta, flags);
		}
	}

	public void draw() {
		if (!isEnabled()) {
			return;
		}
		if (sodiumSpriteHandle != null) {
			try {
				for (TextureAtlasSprite sprite : spritesToUpdate) {
					sodiumSpriteHandle.invoke(sprite);
				}
			} catch (Throwable t) {
			}
		}
		if (!populated) {
			stagedBuffer.upload();
			populated = true;
		}
		Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.translate(x, y, 0);
		for (Map.Entry<RenderType, StagedVertexBuffer.Draw> en : draws.entrySet()) {
			StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(en.getValue());
			if (info != null) {
				PreparedRenderType prepared = en.getKey().prepare();
				prepared.drawFromBuffer(info);
			}
		}
		modelViewStack.popMatrix();
	}

	public static class ClaimedCollection {
		private Set<StackBatcher> claimed = Sets.newHashSet();
		private List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			synchronized (this) {
				StackBatcher batcher;
				if (unclaimed.isEmpty()) {
					batcher = new StackBatcher();
				} else {
					batcher = unclaimed.remove(unclaimed.size() - 1);
				}
				if (batcher == null) {
					batcher = new StackBatcher();
				}
				claimed.add(batcher);
				return batcher;
			}
		}

		public void unclaim(StackBatcher batcher) {
			synchronized (this) {
				claimed.remove(batcher);
				unclaimed.add(batcher);
			}
		}

		public void unclaimAll() {
			synchronized (this) {
				for (StackBatcher batcher : claimed) {
					unclaimed.add(batcher);
				}
				claimed.clear();
			}
		}
	}

	private class BatcherVertexConsumerProvider implements VertexConsumerProvider {
		@Override
		public VertexConsumer getBuffer(RenderType renderLayer) {
			StagedVertexBuffer.Draw draw = draws.get(renderLayer);
			if (draw == null) {
				draw = stagedBuffer.appendDraw(renderLayer.format(), renderLayer.primitiveTopology());
				draws.put(renderLayer, draw);
			}
			return stagedBuffer.getVertexBuilder(draw);
		}
	}

	private static class UnlitFacade implements VertexConsumerProvider {
		private final VertexConsumerProvider delegate;
		private final IdentityHashMap<VertexConsumer, VertexConsumer> cache = new IdentityHashMap<>();

		public UnlitFacade(VertexConsumerProvider delegate) {
			this.delegate = delegate;
		}

		@Override
		public VertexConsumer getBuffer(RenderType layer) {
			return cache.computeIfAbsent(delegate.getBuffer(layer), Consumer::new);
		}

		private static final class Consumer implements VertexConsumer {
			private final VertexConsumer delegate;

			private Consumer(VertexConsumer delegate) {
				this.delegate = delegate;
			}

			@Override
			public VertexConsumer setNormal(float x, float y, float z) {
				delegate.setNormal(0, -1, 0);
				return this;
			}

			@Override
			public VertexConsumer addVertex(float x, float y, float z) {
				delegate.addVertex(x, y, z);
				return this;
			}

			@Override
			public VertexConsumer setUv(float u, float v) {
				delegate.setUv(u, v);
				return this;
			}

			@Override
			public VertexConsumer setUv1(int u, int v) {
				delegate.setUv1(u, v);
				return this;
			}

			@Override
			public VertexConsumer setUv2(int u, int v) {
				delegate.setUv2(u, v);
				return this;
			}

			@Override
			public VertexConsumer setColor(int r, int g, int b, int a) {
				delegate.setColor(r, g, b, a);
				return this;
			}

			@Override
			public VertexConsumer setColor(int color) {
				delegate.setColor(color);
				return this;
			}

			@Override
			public VertexConsumer setLineWidth(float width) {
				delegate.setLineWidth(width);
				return this;
			}
			
		}
	}

}
