package com.simibubi.create.foundation.render.light;

import com.simibubi.create.foundation.render.RenderWork;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

// TODO: Don't immediately destroy light volumes.
//  There's a high chance that a contraption will stop and soon after start again.
//  By caching lightvolumes based on their volumes/locations, we can save having
//  to reread all the lighting data in those cases.
public class LightVolume {

    private final GridAlignedBB sampleVolume;
    private final GridAlignedBB textureVolume;
    private ByteBuffer lightData;

    private boolean bufferDirty;

    private int glTexture;

    public LightVolume(GridAlignedBB textureVolume, GridAlignedBB sampleVolume) {
        // the gpu requires that all textures have power of 2 side lengths
        if (!textureVolume.hasPowerOf2Sides())
            throw new IllegalArgumentException("LightVolume must have power of 2 side lengths");

        this.textureVolume = textureVolume;
        this.sampleVolume = sampleVolume;

        this.glTexture = GL11.glGenTextures();
        this.lightData = MemoryUtil.memAlloc(this.textureVolume.volume() * 2); // TODO: maybe figure out how to pack light coords into a single byte
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


    public void notifyLightUpdate(ILightReader world, LightType type, SectionPos location) {
        GridAlignedBB changedVolume = GridAlignedBB.fromSection(location);
        changedVolume.intersectAssign(sampleVolume); // compute the region contained by us that has dirty lighting data.

        if (!changedVolume.empty()) {
            if (type == LightType.BLOCK) copyBlock(world, changedVolume);
            else if (type == LightType.SKY) copySky(world, changedVolume);
        }
    }

    /**
     * Completely (re)populate this volume with block and sky lighting data.
     * This is expensive and should be avoided.
     */
    public void initialize(ILightReader world) {
        BlockPos.Mutable pos = new BlockPos.Mutable();

        int shiftX = textureVolume.minX;
        int shiftY = textureVolume.minY;
        int shiftZ = textureVolume.minZ;

        textureVolume.forEachContained((x, y, z) -> {
            pos.setPos(x, y, z);

            int blockLight = world.getLightLevel(LightType.BLOCK, pos);
            int skyLight = world.getLightLevel(LightType.SKY, pos);

            writeLight(x - shiftX, y - shiftY, z - shiftZ, blockLight, skyLight);
        });

        bufferDirty = true;
    }

    /**
     * Copy block light from the world into this volume.
     * @param worldVolume the region in the world to copy data from.
     */
    public void copyBlock(ILightReader world, GridAlignedBB worldVolume) {
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
     * @param worldVolume the region in the world to copy data from.
     */
    public void copySky(ILightReader world, GridAlignedBB worldVolume) {
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

    public void use() {
        // just in case something goes wrong or we accidentally call this before this volume is properly disposed of.
        if (glTexture == 0 || lightData == null) return;

        GL12.glBindTexture(GL12.GL_TEXTURE_3D, glTexture);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MIN_FILTER, GL13.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MAG_FILTER, GL13.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_S, GL20.GL_MIRRORED_REPEAT);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_R, GL20.GL_MIRRORED_REPEAT);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_T, GL20.GL_MIRRORED_REPEAT);
        if (bufferDirty) {
            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL40.GL_RG8, textureVolume.sizeX(), textureVolume.sizeY(), textureVolume.sizeZ(), 0, GL40.GL_RG, GL40.GL_UNSIGNED_BYTE, lightData);
            bufferDirty = false;
        }
    }

    public void release() {
        GL12.glBindTexture(GL12.GL_TEXTURE_3D, 0);
    }

    public void delete() {
        RenderWork.enqueue(() -> {
            GL15.glDeleteTextures(glTexture);
            glTexture = 0;
            MemoryUtil.memFree(lightData);
            lightData = null;
        });
    }

    private void writeLight(int x, int y, int z, int block, int sky) {
        byte b = (byte) ((block & 0xF) << 4);
        byte s = (byte) ((sky & 0xF) << 4);

        int i = index(x, y, z);
        lightData.put(i, b);
        lightData.put(i + 1, s);
    }

    private void writeBlock(int x, int y, int z, int block) {
        byte b = (byte) ((block & 0xF) << 4);

        lightData.put(index(x, y, z), b);
    }

    private void writeSky(int x, int y, int z, int sky) {
        byte b = (byte) ((sky & 0xF) << 4);

        lightData.put(index(x, y, z) + 1, b);
    }

    private int index(int x, int y, int z) {
        return (x + textureVolume.sizeX() * (y + z * textureVolume.sizeY())) * 2;
    }
}
