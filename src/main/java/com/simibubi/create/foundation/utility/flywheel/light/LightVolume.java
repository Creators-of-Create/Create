package com.simibubi.create.foundation.utility.flywheel.light;

import com.simibubi.create.foundation.utility.flywheel.box.Box;
import com.simibubi.create.foundation.utility.flywheel.box.MutableBox;

import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;

import net.minecraft.world.level.LightLayer;

import org.lwjgl.system.MemoryUtil;

public class LightVolume implements Box {
	protected final BlockAndTintGetter level;
	protected final MutableBox box = new MutableBox();
	protected MemoryBlock lightData;

	public LightVolume(BlockAndTintGetter level, Box sampleVolume) {
		this.level = level;
		this.setBox(sampleVolume);

		this.lightData = MemoryBlock.malloc(this.box.volume() * 2);
	}

	public Box getVolume() {
		return box;
	}

	@Override
	public int getMinX() {
		return box.getMinX();
	}

	@Override
	public int getMinY() {
		return box.getMinY();
	}

	@Override
	public int getMinZ() {
		return box.getMinZ();
	}

	@Override
	public int getMaxX() {
		return box.getMaxX();
	}

	@Override
	public int getMaxY() {
		return box.getMaxY();
	}

	@Override
	public int getMaxZ() {
		return box.getMaxZ();
	}

	public boolean isInvalid() {
		return lightData == null;
	}

	protected void setBox(Box box) {
		this.box.assign(box);
	}

	public short getPackedLight(int x, int y, int z) {
		if (box.contains(x, y, z)) {
			return MemoryUtil.memGetShort(levelPosToPtr(x, y, z));
		} else {
			return 0;
		}
	}

	public void move(Box newSampleVolume) {
		if (lightData == null) return;

		setBox(newSampleVolume);
		int neededCapacity = box.volume() * 2;
		if (neededCapacity > lightData.size()) {
			lightData = lightData.realloc(neededCapacity);
		}
		initialize();
	}

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize() {
		if (lightData == null) return;

		copyLight(getVolume());
		markDirty();
	}

	protected void markDirty() {
		// noop
	}

	public void delete() {
		lightData.free();
		lightData = null;
	}

	/**
	 * Copy all light from the level into this volume.
	 *
	 * @param levelVolume the region in the level to copy data from.
	 */
	public void copyLight(Box levelVolume) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		levelVolume.forEachContained((x, y, z) -> {
			pos.set(x, y, z);

			int block = this.level.getBrightness(LightLayer.BLOCK, pos);
			int sky = this.level.getBrightness(LightLayer.SKY, pos);

			writeLight(x - xShift, y - yShift, z - zShift, block, sky);
		});
	}

	protected void writeLight(int x, int y, int z, int block, int sky) {
		byte b = (byte) ((block & 0xF) << 4);
		byte s = (byte) ((sky & 0xF) << 4);

		long ptr = boxPosToPtr(x, y, z);
		MemoryUtil.memPutByte(ptr, b);
		MemoryUtil.memPutByte(ptr + 1, s);
	}

	/**
	 * Copy block light from the level into this volume.
	 *
	 * @param levelVolume the region in the level to copy data from.
	 */
	public void copyBlock(Box levelVolume) {
		var pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		levelVolume.forEachContained((x, y, z) -> {
			int light = this.level.getBrightness(LightLayer.BLOCK, pos.set(x, y, z));

			writeBlock(x - xShift, y - yShift, z - zShift, light);
		});
	}

	protected void writeBlock(int x, int y, int z, int block) {
		byte b = (byte) ((block & 0xF) << 4);

		MemoryUtil.memPutByte(boxPosToPtr(x, y, z), b);
	}

	/**
	 * Copy sky light from the level into this volume.
	 *
	 * @param levelVolume the region in the level to copy data from.
	 */
	public void copySky(Box levelVolume) {
		var pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		levelVolume.forEachContained((x, y, z) -> {
			int light = this.level.getBrightness(LightLayer.SKY, pos.set(x, y, z));

			writeSky(x - xShift, y - yShift, z - zShift, light);
		});
	}

	protected void writeSky(int x, int y, int z, int sky) {
		byte s = (byte) ((sky & 0xF) << 4);

		MemoryUtil.memPutByte(boxPosToPtr(x, y, z) + 1, s);
	}

	protected long levelPosToPtr(int x, int y, int z) {
		return lightData.ptr() + levelPosToPtrOffset(x, y, z);
	}

	protected long boxPosToPtr(int x, int y, int z) {
		return lightData.ptr() + boxPosToPtrOffset(x, y, z);
	}

	protected int levelPosToPtrOffset(int x, int y, int z) {
		x -= box.getMinX();
		y -= box.getMinY();
		z -= box.getMinZ();
		return boxPosToPtrOffset(x, y, z);
	}

	protected int boxPosToPtrOffset(int x, int y, int z) {
		return (x + box.sizeX() * (y + z * box.sizeY())) * 2;
	}

	public void onLightUpdate(LightLayer type, SectionPos pos) {
		if (lightData == null) return;

		MutableBox vol = MutableBox.from(pos);
		if (!vol.intersects(getVolume())) return;
		vol.intersectAssign(getVolume()); // compute the region contained by us that has dirty lighting data.

		if (type == LightLayer.BLOCK) copyBlock(vol);
		else if (type == LightLayer.SKY) copySky(vol);
		markDirty();
	}
}
