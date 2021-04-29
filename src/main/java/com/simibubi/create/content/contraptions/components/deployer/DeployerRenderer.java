package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class DeployerRenderer extends SafeTileEntityRenderer<DeployerTileEntity> {

	public DeployerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		renderItem(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);

		if (FastRenderDispatcher.available(te.getWorld())) return;

		renderComponents(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItem(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (te.heldItem.isEmpty()) return;

		BlockState deployerState = te.getBlockState();
		Vector3d offset = getHandOffset(te, partialTicks, deployerState).add(VecHelper.getCenterOf(BlockPos.ZERO));
		ms.push();
		ms.translate(offset.x, offset.y, offset.z);

		Direction facing = deployerState.get(FACING);
		boolean punching = te.mode == Mode.PUNCH;

		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		boolean displayMode = facing == Direction.UP && te.getSpeed() == 0 && !punching;

		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(yRot));
		if (!displayMode) {
			ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(zRot));
			ms.translate(0, 0, -11 / 16f);
		}

		if (punching)
			ms.translate(0, 1 / 8f, -1 / 16f);

		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();

		TransformType transform = TransformType.NONE;
		boolean isBlockItem = (te.heldItem.getItem() instanceof BlockItem)
			&& itemRenderer.getItemModelWithOverrides(te.heldItem, Minecraft.getInstance().world, null)
				.isGui3d();

		if (displayMode) {
			float scale = isBlockItem ? 1.25f : 1;
			ms.translate(0, isBlockItem ? 9 / 16f : 11 / 16f, 0);
			ms.scale(scale, scale, scale);
			transform = TransformType.GROUND;
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(AnimationTickHolder.getRenderTime(te.getWorld())));

		} else {
			float scale = punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
			ms.scale(scale, scale, scale);
			transform = punching ? TransformType.THIRD_PERSON_RIGHT_HAND : TransformType.FIXED;
		}

		itemRenderer.renderItem(te.heldItem, transform, light, overlay, ms, buffer);
		ms.pop();
	}

	protected void renderComponents(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		if (!FastRenderDispatcher.available(te.getWorld())) {
			KineticTileEntityRenderer.renderRotatingKineticBlock(te, getRenderedBlockState(te), ms, vb, light);
		}

		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();
		Vector3d offset = getHandOffset(te, partialTicks, blockState);

		SuperByteBuffer pole = PartialBufferer.get(AllBlockPartials.DEPLOYER_POLE, blockState);
		SuperByteBuffer hand = PartialBufferer.get(te.getHandPose(), blockState);

		transform(te.getWorld(), pole.translate(offset.x, offset.y, offset.z), blockState, pos, true).renderInto(ms,
				vb);
		transform(te.getWorld(), hand.translate(offset.x, offset.y, offset.z), blockState, pos, false).renderInto(ms,
				vb);
	}

	protected Vector3d getHandOffset(DeployerTileEntity te, float partialTicks, BlockState blockState) {
		float distance = te.getHandOffset(partialTicks);
		return Vector3d.of(blockState.get(FACING).getDirectionVec()).scale(distance);
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	private static SuperByteBuffer transform(World world, SuperByteBuffer buffer, BlockState deployerState,
		BlockPos pos, boolean axisDirectionMatters) {
		Direction facing = deployerState.get(FACING);

		float zRotLast =
			axisDirectionMatters && (deployerState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90
				: 0;
		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

		buffer.rotateCentered(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.UP, (float) ((yRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.SOUTH, (float) ((zRotLast) / 180 * Math.PI));
		buffer.light(WorldRenderer.getLightmapCoordinates(world, deployerState, pos));
		return buffer;
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[]{ms, msLocal};
		IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
		BlockState blockState = context.state;
		BlockPos pos = BlockPos.ZERO;
		Mode mode = NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
		World world = context.world;
		AllBlockPartials handPose = getHandPose(mode);

		SuperByteBuffer pole = PartialBufferer.get(AllBlockPartials.DEPLOYER_POLE, blockState);
		SuperByteBuffer hand = PartialBufferer.get(handPose, blockState);
		pole = transform(world, pole, blockState, pos, true);
		hand = transform(world, hand, blockState, pos, false);

		double factor;
		if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
			factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
		} else {
			Vector3d center = VecHelper.getCenterOf(new BlockPos(context.position));
			double distance = context.position.distanceTo(center);
			double nextDistance = context.position.add(context.motion)
				.distanceTo(center);
			factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
		}

		Vector3d offset = Vector3d.of(blockState.get(FACING)
			.getDirectionVec()).scale(factor);

		Matrix4f lighting = msLocal.peek()
			.getModel();
		for (MatrixStack m : matrixStacks)
			m.translate(offset.x, offset.y, offset.z);
		pole.light(lighting, ContraptionRenderDispatcher.getLightOnContraption(context))
			.renderInto(ms, builder);
		hand.light(lighting, ContraptionRenderDispatcher.getLightOnContraption(context))
			.renderInto(ms, builder);
	}

	static AllBlockPartials getHandPose(DeployerTileEntity.Mode mode) {
		return mode == DeployerTileEntity.Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING : AllBlockPartials.DEPLOYER_HAND_POINTING;
	}

}
