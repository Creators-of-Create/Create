package com.simibubi.create.foundation.utility.render;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.lighting.WorldLightManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL40;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Set;

public class ContraptionLighter {

    int minX;
    int minY;
    int minZ;

    int sizeX;
    int sizeY;
    int sizeZ;

    ByteBuffer lightVolume;

    int texture;

    public ContraptionLighter(Contraption contraption) {
        texture = GL11.glGenTextures();

        AxisAlignedBB bounds = contraption.bounds;

        int minX = (int) Math.floor(bounds.minX);
        int minY = (int) Math.floor(bounds.minY);
        int minZ = (int) Math.floor(bounds.minZ);
        int maxX = (int) Math.ceil(bounds.maxX);
        int maxY = (int) Math.ceil(bounds.maxY);
        int maxZ = (int) Math.ceil(bounds.maxZ);

        sizeX = maxX - minX;
        sizeY = maxY - minY;
        sizeZ = maxZ - minZ;

        lightVolume = GLAllocation.createDirectByteBuffer(sizeX * sizeY * sizeZ * 2);
    }

    public void delete() {
        GL11.glDeleteTextures(texture);
    }

    public void tick() {

    }

    public void addLightData(World world, BlockPos pos) {

        int contraptionX = pos.getX() - minX;
        int contraptionY = pos.getY() - minY;
        int contraptionZ = pos.getZ() - minZ;

        if (contraptionX < 0 || contraptionX >= sizeX || contraptionY < 0 || contraptionY >= sizeY || contraptionZ < 0 || contraptionZ >= sizeZ)
            return;

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        writeLight(contraptionX, contraptionY, contraptionZ, blockLight, skyLight);
    }

    private void writeLight(int x, int y, int z, int block, int sky) {
        int i = (x + y * sizeX + z * sizeX * sizeY) * 2;

        lightVolume.put(i, (byte) (block * 16));
        lightVolume.put(i + 1, (byte) (sky * 16));
    }

    public void use() {
        GL12.glBindTexture(GL12.GL_TEXTURE_3D, texture);
        lightVolume.rewind();
        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL40.GL_RG, sizeX, sizeY, sizeZ, 0, GL40.GL_RG, GL40.GL_UNSIGNED_BYTE, lightVolume);
    }
}
