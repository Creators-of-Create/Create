package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity> {

	private ResourceLocation regular = new ResourceLocation(Create.ID, "textures/entity/super_glue/slime.png");

	private Vec3d[] quad1;
	private Vec3d[] quad2;
	private float[] u = { 0, 1, 1, 0 };
	private float[] v = { 0, 0, 1, 1 };

	public SuperGlueRenderer(EntityRendererManager renderManager) {
		super(renderManager);
		initQuads();
	}

	@Override
	public ResourceLocation getEntityTexture(SuperGlueEntity entity) {
		return regular;
	}

	@Override
	public void render(SuperGlueEntity entity, float p_225623_2_, float p_225623_3_, MatrixStack ms,
		IRenderTypeBuffer buffer, int light) {
		super.render(entity, p_225623_2_, p_225623_3_, ms, buffer, light);

		PlayerEntity player = Minecraft.getInstance().player;
		boolean visible = entity.isVisible();
		boolean holdingGlue = AllItems.SUPER_GLUE.isIn(player.getHeldItemMainhand())
			|| AllItems.SUPER_GLUE.isIn(player.getHeldItemOffhand());

		if (!visible && !holdingGlue)
			return;

		IVertexBuilder builder = buffer.getBuffer(RenderType.getEntityCutout(getEntityTexture(entity)));
		light = getBrightnessForRender(entity);
		Direction face = entity.getFacingDirection();

		ms.push();
		MatrixStacker.of(ms)
			.rotateY(AngleHelper.horizontalAngle(face))
			.rotateX(AngleHelper.verticalAngle(face));
		Entry peek = ms.peek();

		Vec3d[][] quads = { quad1, quad2 };
		for (Vec3d[] quad : quads) {
			for (int i = 0; i < 4; i++) {
				Vec3d vertex = quad[i];
				builder.vertex(peek.getModel(), (float) vertex.x, (float) vertex.y, (float) vertex.z)
					.color(255, 255, 255, 255)
					.texture(u[i], v[i])
					.overlay(OverlayTexture.DEFAULT_UV)
					.light(light)
					.normal(peek.getNormal(), face.getXOffset(), face.getYOffset(), face.getZOffset())
					.endVertex();
			}
			face = face.getOpposite();
		}
		ms.pop();
	}

	private void initQuads() {
		Vec3d diff = new Vec3d(Direction.SOUTH.getDirectionVec());
		Vec3d extension = diff.normalize()
			.scale(1 / 32f - 1 / 128f);
		Vec3d plane = VecHelper.axisAlingedPlaneOf(diff);
		Axis axis = Direction.getFacingFromVector(diff.x, diff.y, diff.z)
			.getAxis();

		Vec3d start = Vec3d.ZERO.subtract(extension);
		Vec3d end = Vec3d.ZERO.add(extension);

		plane = plane.scale(1 / 2f);
		Vec3d a1 = plane.add(start);
		Vec3d b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a2 = plane.add(start);
		Vec3d b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a3 = plane.add(start);
		Vec3d b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a4 = plane.add(start);
		Vec3d b4 = plane.add(end);

		quad1 = new Vec3d[] { a2, a3, a4, a1 };
		quad2 = new Vec3d[] { b3, b2, b1, b4 };
	}

	private int getBrightnessForRender(SuperGlueEntity entity) {
		BlockPos blockpos = entity.getHangingPosition();
		BlockPos blockpos2 = blockpos.offset(entity.getFacingDirection()
			.getOpposite());

		World world = entity.getEntityWorld();
		int light = world.isBlockPresent(blockpos) ? WorldRenderer.getLightmapCoordinates(world, blockpos) : 15;
		int light2 = world.isBlockPresent(blockpos2) ? WorldRenderer.getLightmapCoordinates(world, blockpos2) : 15;
		return Math.max(light, light2);
	}

}
