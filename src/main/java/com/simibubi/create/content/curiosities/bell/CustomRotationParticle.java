package com.simibubi.create.content.curiosities.bell;

import com.jozufozu.flywheel.backend.OptifineHandler;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class CustomRotationParticle extends SimpleAnimatedParticle {

	protected boolean mirror;
	protected int loopLength;

	public CustomRotationParticle(ClientWorld worldIn, double x, double y, double z, IAnimatedSprite spriteSet, float yAccel) {
		super(worldIn, x, y, z, spriteSet, yAccel);
	}

	public void selectSpriteLoopingWithAge(IAnimatedSprite sprite) {
		int loopFrame = age % loopLength;
		this.setSprite(sprite.get(loopFrame, loopLength));
	}

	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		Quaternion quaternion = new Quaternion(camera.rotation());
		if (roll != 0.0F) {
			float angle = MathHelper.lerp(partialTicks, oRoll, roll);
			quaternion.mul(Vector3f.ZP.rotation(angle));
		}
		return quaternion;
	}

	@Override
	public void render(IVertexBuilder builder, ActiveRenderInfo camera, float partialTicks) {
		Vector3d cameraPos = camera.getPosition();
		float originX = (float) (MathHelper.lerp(partialTicks, xo, x) - cameraPos.x());
		float originY = (float) (MathHelper.lerp(partialTicks, yo, y) - cameraPos.y());
		float originZ = (float) (MathHelper.lerp(partialTicks, zo, z) - cameraPos.z());

		Vector3f[] vertices = new Vector3f[] {
				new Vector3f(-1.0F, -1.0F, 0.0F),
				new Vector3f(-1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, -1.0F, 0.0F)
		};
		float scale = getQuadSize(partialTicks);

		Quaternion rotation = getCustomRotation(camera, partialTicks);
		for(int i = 0; i < 4; ++i) {
			Vector3f vertex = vertices[i];
			vertex.transform(rotation);
			vertex.mul(scale);
			vertex.add(originX, originY, originZ);
		}

		float minU = mirror ? getU1() : getU0();
		float maxU = mirror ? getU0() : getU1();
		float minV = getV0();
		float maxV = getV1();
		int brightness = OptifineHandler.usingShaders() ? LightTexture.pack(12, 15 ) : getLightColor(partialTicks);
		builder.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
		builder.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(rCol, gCol, bCol, alpha).uv2(brightness).endVertex();
	}
}
