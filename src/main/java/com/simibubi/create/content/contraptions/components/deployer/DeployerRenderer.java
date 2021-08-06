package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class DeployerRenderer extends SafeTileEntityRenderer<DeployerTileEntity> {

	public DeployerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		renderItem(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		renderComponents(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItem(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (te.heldItem.isEmpty()) return;

		BlockState deployerState = te.getBlockState();
		Vector3d offset = getHandOffset(te, partialTicks, deployerState).add(VecHelper.getCenterOf(BlockPos.ZERO));
		ms.pushPose();
		ms.translate(offset.x, offset.y, offset.z);

		Direction facing = deployerState.getValue(FACING);
		boolean punching = te.mode == Mode.PUNCH;

		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		boolean displayMode = facing == Direction.UP && te.getSpeed() == 0 && !punching;

		ms.mulPose(Vector3f.YP.rotationDegrees(yRot));
		if (!displayMode) {
			ms.mulPose(Vector3f.XP.rotationDegrees(zRot));
			ms.translate(0, 0, -11 / 16f);
		}

		if (punching)
			ms.translate(0, 1 / 8f, -1 / 16f);

		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();

		TransformType transform = TransformType.NONE;
		boolean isBlockItem = (te.heldItem.getItem() instanceof BlockItem)
			&& itemRenderer.getModel(te.heldItem, Minecraft.getInstance().level, null)
				.isGui3d();

		if (displayMode) {
			float scale = isBlockItem ? 1.25f : 1;
			ms.translate(0, isBlockItem ? 9 / 16f : 11 / 16f, 0);
			ms.scale(scale, scale, scale);
			transform = TransformType.GROUND;
			ms.mulPose(Vector3f.YP.rotationDegrees(AnimationTickHolder.getRenderTime(te.getLevel())));

		} else {
			float scale = punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
			ms.scale(scale, scale, scale);
			transform = punching ? TransformType.THIRD_PERSON_RIGHT_HAND : TransformType.FIXED;
		}

		itemRenderer.renderStatic(te.heldItem, transform, light, overlay, ms, buffer);
		ms.popPose();
	}

	protected void renderComponents(DeployerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		IVertexBuilder vb = buffer.getBuffer(RenderType.solid());
		if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
			KineticTileEntityRenderer.renderRotatingKineticBlock(te, getRenderedBlockState(te), ms, vb, light);
		}

		BlockState blockState = te.getBlockState();
		Vector3d offset = getHandOffset(te, partialTicks, blockState);

		SuperByteBuffer pole = PartialBufferer.get(AllBlockPartials.DEPLOYER_POLE, blockState);
		SuperByteBuffer hand = PartialBufferer.get(te.getHandPose(), blockState);

		transform(pole.translate(offset.x, offset.y, offset.z), blockState, true)
			.light(light)
			.renderInto(ms, vb);
		transform(hand.translate(offset.x, offset.y, offset.z), blockState, false)
			.light(light)
			.renderInto(ms, vb);
	}

	protected Vector3d getHandOffset(DeployerTileEntity te, float partialTicks, BlockState blockState) {
		float distance = te.getHandOffset(partialTicks);
		return Vector3d.atLowerCornerOf(blockState.getValue(FACING).getNormal()).scale(distance);
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	private static SuperByteBuffer transform(SuperByteBuffer buffer, BlockState deployerState, boolean axisDirectionMatters) {
		Direction facing = deployerState.getValue(FACING);

		float zRotLast =
			axisDirectionMatters && (deployerState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90
				: 0;
		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

		buffer.rotateCentered(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.UP, (float) ((yRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.SOUTH, (float) ((zRotLast) / 180 * Math.PI));
		return buffer;
	}

	public static void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		IVertexBuilder builder = buffer.getBuffer(RenderType.solid());
		BlockState blockState = context.state;
		Mode mode = NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
		PartialModel handPose = getHandPose(mode);

		SuperByteBuffer pole = PartialBufferer.get(AllBlockPartials.DEPLOYER_POLE, blockState);
		SuperByteBuffer hand = PartialBufferer.get(handPose, blockState);

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

		Vector3d offset = Vector3d.atLowerCornerOf(blockState.getValue(FACING)
			.getNormal()).scale(factor);

		MatrixStack m = matrices.getModel();
		m.pushPose();
		m.translate(offset.x, offset.y, offset.z);

		pole.transform(m);
		hand.transform(m);
		pole = transform(pole, blockState, true);
		hand = transform(hand, blockState, false);

		pole.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), builder);
		hand.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), builder);

		m.popPose();
	}

	static PartialModel getHandPose(DeployerTileEntity.Mode mode) {
		return mode == DeployerTileEntity.Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING : AllBlockPartials.DEPLOYER_HAND_POINTING;
	}

}
