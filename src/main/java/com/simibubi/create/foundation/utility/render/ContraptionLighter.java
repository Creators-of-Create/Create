package com.simibubi.create.foundation.utility.render;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ContraptionLighter {

    private int minX;
    private int minY;
    private int minZ;

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private ByteBuffer lightVolume;

    private boolean dirty;

    private int texture;

    public ContraptionLighter(Contraption contraption) {
        texture = GL11.glGenTextures();

        AxisAlignedBB bounds = contraption.bounds;

        int minX = (int) Math.floor(bounds.minX) - 1;
        int minY = (int) Math.floor(bounds.minY) - 1;
        int minZ = (int) Math.floor(bounds.minZ) - 1;
        int maxX = (int) Math.ceil(bounds.maxX) + 1;
        int maxY = (int) Math.ceil(bounds.maxY) + 1;
        int maxZ = (int) Math.ceil(bounds.maxZ) + 1;

        sizeX = nextPowerOf2(maxX - minX);
        sizeY = nextPowerOf2(maxY - minY);
        sizeZ = nextPowerOf2(maxZ - minZ);

        lightVolume = GLAllocation.createDirectByteBuffer(sizeX * sizeY * sizeZ * 2);

        update(contraption);
    }

    public static int nextPowerOf2(int a)  {
        int h = Integer.highestOneBit(a);
        return (h == a) ? h : (h << 1);
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public void delete() {
        RenderWork.enqueue(() -> {
            GL15.glDeleteTextures(texture);
            texture = 0;
            MemoryUtil.memFree(lightVolume);
            lightVolume = null;
        });
    }

    private void setupPosition(Contraption c) {
        Vec3d positionVec = c.entity.getPositionVec();
        minX = (int) (Math.floor(positionVec.x) - sizeX / 2);
        minY = (int) (Math.floor(positionVec.y) - 1);
        minZ = (int) (Math.floor(positionVec.z) - sizeZ / 2);
    }

    public void update(Contraption c) {
        if (lightVolume == null) return;

        setupPosition(c);

        World world = c.entity.world;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    pos.setPos(minX + x, minY + y, minZ + z);

                    int blockLight = world.getLightLevel(LightType.BLOCK, pos);
                    int skyLight = world.getLightLevel(LightType.SKY, pos);

                    writeLight(x, y, z, blockLight, skyLight);
                }
            }
        }

        dirty = true;
    }

    private void writeLight(int x, int y, int z, int block, int sky) {
        int i = (x + sizeX * (y + z * sizeY)) * 2;

        byte b = (byte) ((block & 0xF) << 4);
        byte s = (byte) ((sky & 0xF) << 4);

        lightVolume.put(i, b);
        lightVolume.put(i + 1, s);
    }

    public void use() {
        if (texture == 0 || lightVolume == null) return;

        GL12.glBindTexture(GL12.GL_TEXTURE_3D, texture);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MIN_FILTER, GL13.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_MAG_FILTER, GL13.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_R, GL13.GL_CLAMP);
        GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL13.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP);
        if (dirty) {
            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL40.GL_RG8, sizeX, sizeY, sizeZ, 0, GL40.GL_RG, GL40.GL_UNSIGNED_BYTE, lightVolume);
            dirty = false;
        }
    }

    public void release() {
        GL12.glBindTexture(GL12.GL_TEXTURE_3D, 0);
    }
}
