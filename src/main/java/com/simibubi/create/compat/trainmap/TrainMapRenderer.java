package com.simibubi.create.compat.trainmap;

import java.util.HashSet;
import java.util.Set;

import org.joml.Matrix3x2fStack;

import com.mojang.blaze3d.platform.NativeImage;
import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.config.CClient;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.createmod.catnip.api.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class TrainMapRenderer implements AutoCloseable {

	public static final TrainMapRenderer INSTANCE = new TrainMapRenderer();
	public static final int WIDTH = 128, HEIGHT = 128;
	private Object2ObjectMap<Couple<Integer>, TrainMapInstance> maps = new Object2ObjectOpenHashMap<>();

	public int trackingVersion;
	public ResourceKey<Level> trackingDim;
	public CClient.TrainMapTheme trackingTheme;

	//

	private TrainMapInstance previouslyAccessed;

	public void startDrawing() {
		previouslyAccessed = null;
		maps.values()
			.forEach(tmi -> {
				tmi.getImage()
					.fillRect(0, 0, WIDTH, HEIGHT, 0);
				tmi.untouched = true;
			});
	}

	public Object2ObjectMap<Couple<Integer>, TrainMapInstance> getMaps() {
		return maps;
	}

	public void setPixel(int xCoord, int zCoord, int color) {
		TrainMapInstance instance = getOrCreateAt(xCoord, zCoord);
		xCoord = Mth.positiveModulo(xCoord, WIDTH);
		zCoord = Mth.positiveModulo(zCoord, HEIGHT);
		instance.getImage()
			.setPixelABGR(xCoord, zCoord, color);
	}

	public int getPixel(int xCoord, int zCoord) {
		Couple<Integer> sectionKey = toSectionKey(xCoord, zCoord);
		if (!maps.containsKey(sectionKey))
			return 0;

		TrainMapInstance instance = getOrCreateAt(xCoord, zCoord);
		xCoord = Mth.positiveModulo(xCoord, WIDTH);
		zCoord = Mth.positiveModulo(zCoord, HEIGHT);
		return instance.getImage()
			.getPixel(xCoord, zCoord);
	}

	public void setPixels(int xCoordFrom, int zCoordFrom, int xCoordTo, int zCoordTo, int color) {
		for (int x = Math.min(xCoordFrom, xCoordTo); x <= Math.max(xCoordFrom, xCoordTo); x++)
			for (int z = Math.min(zCoordFrom, zCoordTo); z <= Math.max(zCoordFrom, zCoordTo); z++)
				setPixel(x, z, color);
	}

	public void blendPixel(int xCoord, int zCoord, int color, int alpha) {
		TrainMapInstance instance = getOrCreateAt(xCoord, zCoord);
		xCoord = Mth.positiveModulo(xCoord, WIDTH);
		zCoord = Mth.positiveModulo(zCoord, HEIGHT);
		instance.getImage()
			.setPixelABGR(xCoord, zCoord, blendAbgr(instance.getImage().getPixel(xCoord, zCoord), abgrWithAlpha(alpha, color)));
	}

	private static int abgrWithAlpha(int alpha, int color) {
		return (alpha & 0xff) << 24 | (color & 0x00ffffff);
	}

	private static int blendAbgr(int dst, int src) {
		int srcA = src >>> 24;
		int invA = 255 - srcA;
		int b = (((src) & 0xff) * srcA + ((dst) & 0xff) * invA) / 255;
		int g = (((src >>> 8) & 0xff) * srcA + ((dst >>> 8) & 0xff) * invA) / 255;
		int r = (((src >>> 16) & 0xff) * srcA + ((dst >>> 16) & 0xff) * invA) / 255;
		int a = Math.min(255, srcA + (dst >>> 24) * invA / 255);
		return a << 24 | r << 16 | g << 8 | b;
	}

	public void blendPixels(int xCoordFrom, int zCoordFrom, int xCoordTo, int zCoordTo, int color, int alpha) {
		for (int x = Math.min(xCoordFrom, xCoordTo); x <= Math.max(xCoordFrom, xCoordTo); x++)
			for (int z = Math.min(zCoordFrom, zCoordTo); z <= Math.max(zCoordFrom, zCoordTo); z++)
				blendPixel(x, z, color, alpha);
	}

	public void finishDrawing() {
		previouslyAccessed = null;
		Set<Couple<Integer>> stale = new HashSet<>();

		maps.forEach((key, tmi) -> {
			if (!tmi.untouched)
				return;
			tmi.close();
			stale.add(key);
		});

		stale.forEach(key -> {
			TrainMapInstance tmi = maps.remove(key);
			if (tmi != null)
				tmi.close();
		});
	}

	public boolean is(int x, int z, int color) {
		return (getPixel(x, z) & 0xFFFFFF) == (color & 0xFFFFFF);
	}

	public boolean isEmpty(int x, int z) {
		return getPixel(x, z) == 0;
	}

	public int alphaAt(int x, int z) {
		int pixel = getPixel(x, z);
		return ((pixel & 0xFFFFFF) != 0) ? ((pixel >>> 24) & 0xFF) : 0;
	}

	//

	public void render(GuiGraphicsExtractor graphics, boolean linearFiltering, Rect2i bounds) {
		Matrix3x2fStack pose = graphics.pose();
		maps.forEach((key, tmi) -> {
			if (tmi.canBeSkipped(bounds))
				return;
			int x = key.getFirst();
			int y = key.getSecond();
			pose.pushMatrix();
			pose.translate(x * WIDTH, y * HEIGHT);
			tmi.draw(graphics, linearFiltering);
			pose.popMatrix();
		});
	}

	public TrainMapInstance getOrCreateAt(int xCoord, int zCoord) {
		Couple<Integer> sectionKey = toSectionKey(xCoord, zCoord);
		if (previouslyAccessed != null && previouslyAccessed.sectionKey.equals(sectionKey))
			return previouslyAccessed;
		return maps.compute(sectionKey, (key, instance) -> instance == null ? new TrainMapInstance(key) : instance);
	}

	public Couple<Integer> toSectionKey(int xCoord, int zCoord) {
		return Couple.create(Mth.floor(xCoord / (float) WIDTH), Mth.floor(zCoord / (float) HEIGHT));
	}

	public void resetData() {
		for (TrainMapInstance instance : maps.values())
			instance.close();
		maps.clear();
	}

	public void close() {
		this.resetData();
	}

	public class TrainMapInstance implements AutoCloseable {

		private DynamicTexture texture;
		private boolean requiresUpload;
		private boolean linearFiltering;
		private Rect2i bounds;

		private boolean untouched;
		private Couple<Integer> sectionKey;

		public Identifier location;

		public TrainMapInstance(Couple<Integer> sectionKey) {
			TextureManager textureManager = Minecraft.getInstance()
				.getTextureManager();

			this.sectionKey = sectionKey;
			untouched = false;
			requiresUpload = true;
			texture = new DynamicTexture("create train map", 128, 128, true);
			linearFiltering = false;
			location = Create.asResource("trainmap/" + sectionKey.getFirst() + "_" + sectionKey.getSecond());
			textureManager.register(location, texture);
			bounds = new Rect2i(sectionKey.getFirst() * WIDTH, sectionKey.getSecond() * HEIGHT, WIDTH, HEIGHT);
		}

		public boolean canBeSkipped(Rect2i bounds) {
			return bounds.getX() + bounds.getWidth() < this.bounds.getX()
				|| this.bounds.getX() + this.bounds.getWidth() < bounds.getX()
				|| bounds.getY() + bounds.getHeight() < this.bounds.getY()
				|| this.bounds.getY() + this.bounds.getHeight() < bounds.getY();
		}

		public NativeImage getImage() {
			untouched = false;
			requiresUpload = true;
			return texture.getPixels();
		}

		public void draw(GuiGraphicsExtractor graphics, boolean linearFiltering) {
			if (texture.getPixels() == null)
				return;

			if (requiresUpload) {
				texture.upload();
				requiresUpload = false;
			}

			this.linearFiltering = linearFiltering;
			graphics.blit(RenderPipelines.GUI_TEXTURED, location, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
		}

		public void close() {
			texture.close();
		}

	}
}
