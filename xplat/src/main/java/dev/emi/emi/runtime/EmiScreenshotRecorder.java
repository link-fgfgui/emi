package dev.emi.emi.runtime;

import java.io.File;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class EmiScreenshotRecorder {
	private static final String SCREENSHOTS_DIRNAME = "screenshots";

	public static void saveScreenshot(String path, int width, int height, Runnable renderer) {
		if (!RenderSystem.isOnRenderThread()) {
			Minecraft.getInstance().execute(() -> saveScreenshotInner(path, width, height, renderer));
		} else {
			saveScreenshotInner(path, width, height, renderer);
		}
	}

	private static void saveScreenshotInner(String path, int width, int height, Runnable renderer) {
		Minecraft client = Minecraft.getInstance();

		int scale;
		if (EmiConfig.recipeScreenshotScale < 1) {
			scale = EmiPort.getGuiScale(client);
		} else {
			scale = EmiConfig.recipeScreenshotScale;
		}

		RenderTarget framebuffer = new TextureTarget("EMI Screenshot", width * scale, height * scale, true);

		GpuTexture colorTexture = framebuffer.getColorTexture();
		if (colorTexture != null) {
			try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
					.createRenderPass(() -> "EMI Screenshot", framebuffer.getColorTextureView(), OptionalInt.of(0))) {
				Matrix4fStack view = RenderSystem.getModelViewStack();
				view.pushMatrix();
				view.identity();
				view.translate(-1.0f, 1.0f, 0.0f);
				view.scale(2f / width, -2f / height, -1f / 1000f);
				view.translate(0.0f, 0.0f, 10.0f);

				GpuBufferSlice backupProj = RenderSystem.getProjectionMatrixBuffer();
				ProjectionType backupProjType = RenderSystem.getProjectionType();

				GpuBuffer projBuf = RenderSystem.getDevice().createBuffer(() -> "EMI Projection", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 64);
				try (GpuBuffer.MappedView mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(projBuf, false, true)) {
					new Matrix4f().identity().get(mapped.data());
				}
				RenderSystem.setProjectionMatrix(projBuf.slice(), ProjectionType.ORTHOGRAPHIC);

				renderer.run();

				RenderSystem.setProjectionMatrix(backupProj, backupProjType);
				projBuf.close();
				view.popMatrix();
			}
		}

		saveScreenshotInner(client.gameDirectory, path, framebuffer,
			message -> client.execute(() -> client.gui.hud.getChat().addClientSystemMessage(message)));
	}

	private static void saveScreenshotInner(File gameDirectory, String suggestedPath, RenderTarget framebuffer, Consumer<Component> messageReceiver) {
		takeScreenshot(framebuffer, nativeImage -> {
			File screenshots = new File(gameDirectory, SCREENSHOTS_DIRNAME);
			screenshots.mkdir();

			String filename = getScreenshotFilename(screenshots, suggestedPath);
			File file = new File(screenshots, filename);

			File parent = file.getParentFile();
			parent.mkdirs();

			Util.ioPool().execute(() -> {
				try {
					nativeImage.writeToFile(file);

					Component text = EmiPort.literal(filename,
						Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent.OpenFile(file.getAbsoluteFile())));
					messageReceiver.accept(EmiPort.translatable("screenshot.success", text));
				} catch (Throwable e) {
					EmiLog.error("Failed to write screenshot", e);
					messageReceiver.accept(EmiPort.translatable("screenshot.failure", e.getMessage()));
				} finally {
					nativeImage.close();
				}
			});
		});
	}

	private static void takeScreenshot(RenderTarget framebuffer, Consumer<NativeImage> consumer) {
		int i = framebuffer.width;
		int j = framebuffer.height;
		GpuTexture gputexture = framebuffer.getColorTexture();
		if (gputexture == null) {
			return;
		}
		int usage = GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST;
		GpuBuffer gpubuffer = RenderSystem.getDevice()
			.createBuffer(() -> "EMI Screenshot buffer", usage, i * j * gputexture.getFormat().pixelSize());
		CommandEncoder commandencoder = RenderSystem.getDevice().createCommandEncoder();
		commandencoder.copyTextureToBuffer(gputexture, gpubuffer, 0, () -> {
			try (GpuBuffer.MappedView mappedview = commandencoder.mapBuffer(gpubuffer, true, false)) {
				NativeImage nativeimage = new NativeImage(i, j, false);

				for (int i1 = 0; i1 < j; i1++) {
					for (int j1 = 0; j1 < i; j1++) {
						int k1 = mappedview.data().getInt((j1 + i1 * i) * gputexture.getFormat().pixelSize());
						nativeimage.setPixelABGR(j1, j - i1 - 1, k1 | 0xFF000000);
					}
				}

				consumer.accept(nativeimage);
			}

			gpubuffer.close();
		}, 0);
	}

	private static String getScreenshotFilename(File directory, String path) {
		int i = 1;
		while ((new File(directory, path + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return path + (i == 1 ? "" : "_" + i) + ".png";
	}
}
