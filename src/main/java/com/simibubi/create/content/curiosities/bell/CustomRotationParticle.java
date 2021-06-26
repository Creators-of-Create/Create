package com.simibubi.create.content.curiosities.bell;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class CustomRotationParticle extends SimpleAnimatedParticle {

	public CustomRotationParticle(ClientWorld worldIn, double x, double y, double z, IAnimatedSprite spriteSet, float yAccel) {
		super(worldIn, x, y, z, spriteSet, yAccel);
	}

	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		Quaternion quaternion = new Quaternion(camera.getRotation());
		if (this.particleAngle != 0.0F) {
			float angle = MathHelper.lerp(partialTicks, this.prevParticleAngle, this.particleAngle);
			quaternion.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(angle));
		}
		return quaternion;
	}

	@Override
	public void buildGeometry(IVertexBuilder builder, ActiveRenderInfo camera, float partialTicks) {
		Vector3d cameraPos = camera.getProjectedView();
		float originX = (float)(MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - cameraPos.getX());
		float originY = (float)(MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - cameraPos.getY());
		float originZ = (float)(MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - cameraPos.getZ());

		Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		float scale = this.getScale(partialTicks);

		Quaternion rotation = getCustomRotation(camera, partialTicks);
		for(int i = 0; i < 4; ++i) {
			Vector3f vertex = vertices[i];
			vertex.func_214905_a(rotation);
			vertex.mul(scale);
			vertex.add(originX, originY, originZ);
		}

		float minU = this.getMinU();
		float maxU = this.getMaxU();
		float minV = this.getMinV();
		float maxV = this.getMaxV();
		int brightness = this.getBrightnessForRender(partialTicks);
		builder.vertex(vertices[0].getX(), vertices[0].getY(), vertices[0].getZ()).texture(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).light(brightness).endVertex();
		builder.vertex(vertices[1].getX(), vertices[1].getY(), vertices[1].getZ()).texture(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).light(brightness).endVertex();
		builder.vertex(vertices[2].getX(), vertices[2].getY(), vertices[2].getZ()).texture(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).light(brightness).endVertex();
		builder.vertex(vertices[3].getX(), vertices[3].getY(), vertices[3].getZ()).texture(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).light(brightness).endVertex();
	}
}
