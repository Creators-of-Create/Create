package com.simibubi.create.content.equipment.bell;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CustomRotationParticle extends SimpleAnimatedParticle {

	protected boolean mirror;
	protected int loopLength;

	public CustomRotationParticle(ClientLevel worldIn, double x, double y, double z, SpriteSet spriteSet, float yAccel) {
		super(worldIn, x, y, z, spriteSet, yAccel);
	}

	public void selectSpriteLoopingWithAge(SpriteSet sprite) {
		int loopFrame = age % loopLength;
		this.setSprite(sprite.get(loopFrame, loopLength));
	}

	public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
		Quaternionf quaternion = new Quaternionf(camera.rotation());
		if (roll != 0.0F) {
			float angle = Mth.lerp(partialTicks, oRoll, roll);
			quaternion.mul(Axis.ZP.rotation(angle));
		}
		return quaternion;
	}

	@Override
	public void render(VertexConsumer builder, Camera camera, float partialTicks) {
		Vec3 cameraPos = camera.getPosition();
		float originX = (float) (Mth.lerp(partialTicks, xo, x) - cameraPos.x());
		float originY = (float) (Mth.lerp(partialTicks, yo, y) - cameraPos.y());
		float originZ = (float) (Mth.lerp(partialTicks, zo, z) - cameraPos.z());

		Vector3f[] vertices = new Vector3f[] {
				new Vector3f(-1.0F, -1.0F, 0.0F),
				new Vector3f(-1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, -1.0F, 0.0F)
		};
		float scale = getQuadSize(partialTicks);

		Quaternionf rotation = getCustomRotation(camera, partialTicks);
		for(int i = 0; i < 4; ++i) {
			Vector3f vertex = vertices[i];
			vertex.rotate(rotation);
			vertex.mul(scale);
			vertex.add(originX, originY, originZ);
		}

		float minU = mirror ? getU1() : getU0();
		float maxU = mirror ? getU0() : getU1();
		float minV = getV0();
		float maxV = getV1();
		int brightness = ShadersModHandler.isShaderPackInUse() ? LightTexture.pack(12, 15) : getLightColor(partialTicks);
		builder.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
	}
}
