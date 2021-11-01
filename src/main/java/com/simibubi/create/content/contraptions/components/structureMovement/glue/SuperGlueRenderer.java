package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity> {

	private ResourceLocation regular = Create.asResource("textures/entity/super_glue/slime.png");

	private float[] insideQuad;
	private float[] outsideQuad;

	public SuperGlueRenderer(EntityRenderDispatcher renderManager) {
		super(renderManager);
		initQuads();
	}

	@Override
	public ResourceLocation getTextureLocation(SuperGlueEntity entity) {
		return regular;
	}

	@Override
	public boolean shouldRender(SuperGlueEntity entity, Frustum frustum, double x, double y, double z) {
		if (super.shouldRender(entity, frustum, x, y, z)) {
			Player player = Minecraft.getInstance().player;
			boolean visible = entity.isVisible();
			boolean holdingGlue = AllItems.SUPER_GLUE.isIn(player.getMainHandItem())
				|| AllItems.SUPER_GLUE.isIn(player.getOffhandItem());

			if (visible || holdingGlue)
				return true;
		}
		return false;
	}

	@Override
	public void render(SuperGlueEntity entity, float yaw, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light) {
		super.render(entity, yaw, partialTicks, ms, buffer, light);

		VertexConsumer builder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
		light = getBrightnessForRender(entity);
		Direction face = entity.getFacingDirection();

		ms.pushPose();
		MatrixTransformStack.of(ms)
			.rotateY(AngleHelper.horizontalAngleNew(face))
			.rotateX(AngleHelper.verticalAngle(face));
		Pose peek = ms.last();

		renderQuad(builder, peek, insideQuad, light, -1);
		renderQuad(builder, peek, outsideQuad, light, 1);

		ms.popPose();
	}

	private void initQuads() {
		Vec3 diff = Vec3.atLowerCornerOf(Direction.SOUTH.getNormal());
		Vec3 extension = diff.normalize()
			.scale(1 / 32f - 1 / 128f);

		Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
		Axis axis = Direction.getNearest(diff.x, diff.y, diff.z)
			.getAxis();

		Vec3 start = Vec3.ZERO.subtract(extension);
		Vec3 end = Vec3.ZERO.add(extension);

		plane = plane.scale(1 / 2f);
		Vec3 a1 = plane.add(start);
		Vec3 b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a2 = plane.add(start);
		Vec3 b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a3 = plane.add(start);
		Vec3 b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3 a4 = plane.add(start);
		Vec3 b4 = plane.add(end);

		insideQuad = new float[] {
				(float) a1.x, (float) a1.y, (float) a1.z, 1, 0,
				(float) a2.x, (float) a2.y, (float) a2.z, 1, 1,
				(float) a3.x, (float) a3.y, (float) a3.z, 0, 1,
				(float) a4.x, (float) a4.y, (float) a4.z, 0, 0,
		};
		outsideQuad = new float[] {
				(float) b4.x, (float) b4.y, (float) b4.z, 0, 0,
				(float) b3.x, (float) b3.y, (float) b3.z, 0, 1,
				(float) b2.x, (float) b2.y, (float) b2.z, 1, 1,
				(float) b1.x, (float) b1.y, (float) b1.z, 1, 0,
		};
	}

	private int getBrightnessForRender(SuperGlueEntity entity) {
		BlockPos blockpos = entity.getHangingPosition();
		BlockPos blockpos2 = blockpos.relative(entity.getFacingDirection()
			.getOpposite());

		Level world = entity.getCommandSenderWorld();
		int light = world.isLoaded(blockpos) ? LevelRenderer.getLightColor(world, blockpos) : 15;
		int light2 = world.isLoaded(blockpos2) ? LevelRenderer.getLightColor(world, blockpos2) : 15;
		return Math.max(light, light2);
	}

	// Vertex format: pos x, pos y, pos z, u, v
	private void renderQuad(VertexConsumer builder, Pose matrix, float[] data, int light, float normalZ) {
		for (int i = 0; i < 4; i++) {
			builder.vertex(matrix.pose(), data[5 * i], data[5 * i + 1], data[5 * i + 2])
				.color(255, 255, 255, 255)
				.uv(data[5 * i + 3], data[5 * i + 4])
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(matrix.normal(), 0.0f, 0.0f, normalZ)
				.endVertex();
		}
	}

}
