package dev.emi.emi.runtime;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix3x2fStack;

public class EmiDrawContext {
	private final Minecraft client = Minecraft.getInstance();
	private final GuiGraphics context;
	
	private EmiDrawContext(GuiGraphics context) {
		this.context = context;
	}

	public static EmiDrawContext wrap(GuiGraphics context) {
		return new EmiDrawContext(context);
	}

	public GuiGraphics raw() {
		return context;
	}

	public Matrix3x2fStack matrices() {
		return context.getMatrices();
	}

	public void push() {
		matrices().pushMatrix();
	}

	public void pop() {
		matrices().popMatrix();
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
		drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

    public void drawTexture(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int color) {
        drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256, color);
    }

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height) {
		drawTexture(texture, x, y, z, u, v, width, height, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, /* z,*/ u, v, width, height, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
	}

    public void drawTexture(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
    }

    public void drawSpriteStretched(TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
    }

	public void fill(int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}

	public void drawText(FormattedCharSequence text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(FormattedCharSequence text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	public void drawText(FormattedCharSequence text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	public void drawTextWithShadow(FormattedCharSequence text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(FormattedCharSequence text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, true);
	}

	public void drawTextWithShadow(FormattedCharSequence text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, true);
	}

	public void drawCenteredText(FormattedCharSequence text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(FormattedCharSequence text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x - client.textRenderer.getWidth(text) / 2, y, color, false);
	}

	public void drawCenteredTextWithShadow(FormattedCharSequence text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(FormattedCharSequence text, int x, int y, int color) {
		context.drawCenteredTextWithShadow(client.textRenderer, text.asOrderedText(), x, y, color);
	}

	public void enableDepthTest() {
//		RenderSystem.enableDepthTest();
	}

	public void disableDepthTest() {
//		RenderSystem.disableDepthTest();
	}

	public void enableBlend() {
//		RenderSystem.enableBlend();
	}

	public void disableBlend() {
//		RenderSystem.disableBlend();
	}

	public void resetColor() {
//		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
//		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
//		raw().setShaderColor(r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		stack.render(raw(), x, y, client.getRenderTickCounter().getTickProgress(false));
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.getRenderTickCounter().getTickProgress(false), flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		stack.render(raw(), x, y, delta, flags);
	}
}
