package com.simibubi.create.foundation.render.backend.light;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.RenderWork;
import com.simibubi.create.foundation.render.backend.gl.GlTexture;
import com.simibubi.create.foundation.render.backend.gl.versioned.RGPixelFormat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

public class LightVolume {

	private GridAlignedBB sampleVolume;
	private GridAlignedBB textureVolume;
	private ByteBuffer lightData;

	private boolean bufferDirty;
	private boolean removed;

	private final GlTexture glTexture;

	private final RGPixelFormat pixelFormat;

	public LightVolume(GridAlignedBB sampleVolume) {
		setSampleVolume(sampleVolume);

		pixelFormat = Backend.compat.pixelFormat;

		this.glTexture = new GlTexture(GL20.GL_TEXTURE_3D);
		this.lightData = MemoryUtil.memAlloc(this.textureVolume.volume() * pixelFormat.byteCount());

		// allocate space for the texture
		GL20.glActiveTexture(GL20.GL_TEXTURE4);
		glTexture.bind();

		int sizeX = textureVolume.sizeX();
		int sizeY = textureVolume.sizeY();
		int sizeZ = textureVolume.sizeZ();
		GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, pixelFormat.internalFormat(), sizeX, sizeY, sizeZ, 0, pixelFormat.format(), GL20.GL_UNSIGNED_BYTE, 0);

		glTexture.unbind();
		GL20.glActiveTexture(GL20.GL_TEXTURE0);
	}

	private void setSampleVolume(GridAlignedBB sampleVolume) {
		this.sampleVolume = sampleVolume;
		this.textureVolume = sampleVolume.copy();
		this.textureVolume.nextPowerOf2Centered();
	}

	public GridAlignedBB getTextureVolume() {
		return GridAlignedBB.copy(textureVolume);
	}

	public GridAlignedBB getSampleVolume() {
		return GridAlignedBB.copy(sampleVolume);
	}

	public int getMinX() {
		return textureVolume.minX;
	}

	public int getMinY() {
		return textureVolume.minY;
	}

	public int getMinZ() {
		return textureVolume.minZ;
	}

	public int getMaxX() {
		return textureVolume.maxX;
	}

	public int getMaxY() {
		return textureVolume.maxY;
	}

	public int getMaxZ() {
		return textureVolume.maxZ;
	}

	public int getSizeX() {
		return textureVolume.sizeX();
	}

	public int getSizeY() {
		return textureVolume.sizeY();
	}

	public int getSizeZ() {
		return textureVolume.sizeZ();
	}

	public void move(IBlockDisplayReader world, GridAlignedBB newSampleVolume) {
		if (textureVolume.contains(newSampleVolume)) {
			if (newSampleVolume.intersects(sampleVolume)) {
				GridAlignedBB newArea = newSampleVolume.intersect(sampleVolume);
				sampleVolume = newSampleVolume;

				copyLight(world, newArea);
			} else {
				sampleVolume = newSampleVolume;
				initialize(world);
			}
		} else {
			setSampleVolume(newSampleVolume);
			int volume = textureVolume.volume();
			if (volume * 2 > lightData.capacity()) {
				lightData = MemoryUtil.memRealloc(lightData, volume * 2);
			}
			initialize(world);
		}
	}

	public void notifyLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changedVolume) {
		if (removed)
			return;

		if (!changedVolume.intersects(sampleVolume))
			return;
		changedVolume = changedVolume.intersect(sampleVolume); // compute the region contained by us that has dirty lighting data.

		if (type == LightType.BLOCK) copyBlock(world, changedVolume);
		else if (type == LightType.SKY) copySky(world, changedVolume);
	}

	public void notifyLightPacket(IBlockDisplayReader world, int chunkX, int chunkZ) {
		if (removed) return;

		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);
		if (!changedVolume.intersects(sampleVolume))
			return;
		changedVolume.intersectAssign(sampleVolume); // compute the region contained by us that has dirty lighting data.

		copyLight(world, changedVolume);
	}

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize(IBlockDisplayReader world) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		int shiftX = textureVolume.minX;
		int shiftY = textureVolume.minY;
		int shiftZ = textureVolume.minZ;

		sampleVolume.forEachContained((x, y, z) -> {
			pos.setPos(x, y, z);

			int blockLight = world.getLightLevel(LightType.BLOCK, pos);
			int skyLight = world.getLightLevel(LightType.SKY, pos);

			writeLight(x - shiftX, y - shiftY, z - shiftZ, blockLight, skyLight);
		});

		bufferDirty = true;
	}

	/**
	 * Copy block light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyBlock(IBlockDisplayReader world, GridAlignedBB worldVolume) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		int xShift = textureVolume.minX;
		int yShift = textureVolume.minY;
		int zShift = textureVolume.minZ;

		worldVolume.forEachContained((x, y, z) -> {
			pos.setPos(x, y, z);

			int light = world.getLightLevel(LightType.BLOCK, pos);

			writeBlock(x - xShift, y - yShift, z - zShift, light);
		});

		bufferDirty = true;
	}

	/**
	 * Copy sky light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copySky(IBlockDisplayReader world, GridAlignedBB worldVolume) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		int xShift = textureVolume.minX;
		int yShift = textureVolume.minY;
		int zShift = textureVolume.minZ;

		worldVolume.forEachContained((x, y, z) -> {
			pos.setPos(x, y, z);

			int light = world.getLightLevel(LightType.SKY, pos);

			writeSky(x - xShift, y - yShift, z - zShift, light);
		});

		bufferDirty = true;
	}

	/**
	 * Copy all light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyLight(IBlockDisplayReader world, GridAlignedBB worldVolume) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		int xShift = textureVolume.minX;
		int yShift = textureVolume.minY;
		int zShift = textureVolume.minZ;

		worldVolume.forEachContained((x, y, z) -> {
			pos.setPos(x, y, z);

			int block = world.getLightLevel(LightType.BLOCK, pos);
			int sky = world.getLightLevel(LightType.SKY, pos);

			writeLight(x - xShift, y - yShift, z - zShift, block, sky);
		});

		bufferDirty = true;
	}

	public void bind() {
		// just in case something goes wrong or we accidentally call this before this volume is properly disposed of.
		if (lightData == null || removed) return;

		GL13.glActiveTexture(GL20.GL_TEXTURE4);
		glTexture.bind();
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MIN_FILTER, GL13.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MAG_FILTER, GL13.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_S, GL20.GL_MIRRORED_REPEAT);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_R, GL20.GL_MIRRORED_REPEAT);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_T, GL20.GL_MIRRORED_REPEAT);

		uploadTexture();
	}

	private void uploadTexture() {
		if (bufferDirty) {
			GL20.glPixelStorei(GL20.GL_UNPACK_ROW_LENGTH, 0);
			GL20.glPixelStorei(GL20.GL_UNPACK_SKIP_PIXELS, 0);
			GL20.glPixelStorei(GL20.GL_UNPACK_SKIP_ROWS, 0);
			GL20.glPixelStorei(GL20.GL_UNPACK_SKIP_IMAGES, 0);
			GL20.glPixelStorei(GL20.GL_UNPACK_IMAGE_HEIGHT, 0);
			GL20.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 2);
			int sizeX = textureVolume.sizeX();
			int sizeY = textureVolume.sizeY();
			int sizeZ = textureVolume.sizeZ();

			GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, 0, sizeX, sizeY, sizeZ, pixelFormat.format(), GL20.GL_UNSIGNED_BYTE, lightData);

			GL20.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4); // 4 is the default
			bufferDirty = false;
		}
	}

	public void unbind() {
		glTexture.unbind();
	}

	public void delete() {
		removed = true;
		RenderWork.enqueue(() -> {
			glTexture.delete();
			MemoryUtil.memFree(lightData);
			lightData = null;
		});
	}

	private void writeLight(int x, int y, int z, int block, int sky) {
		byte b = (byte) ((block & 0xF) << 4);
		byte s = (byte) ((sky & 0xF) << 4);

		int i = posToIndex(x, y, z);
		lightData.put(i, b);
		lightData.put(i + 1, s);
	}

	private void writeBlock(int x, int y, int z, int block) {
		byte b = (byte) ((block & 0xF) << 4);

		lightData.put(posToIndex(x, y, z), b);
	}

	private void writeSky(int x, int y, int z, int sky) {
		byte b = (byte) ((sky & 0xF) << 4);

		lightData.put(posToIndex(x, y, z) + 1, b);
	}

	private int posToIndex(int x, int y, int z) {
		return (x + textureVolume.sizeX() * (y + z * textureVolume.sizeY())) * pixelFormat.byteCount();
	}
}
