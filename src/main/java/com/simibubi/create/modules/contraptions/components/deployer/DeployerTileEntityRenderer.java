package com.simibubi.create.modules.contraptions.components.deployer;

import static com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock.FACING;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntity.State;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class DeployerTileEntityRenderer extends TileEntityRenderer<DeployerTileEntity> {

	@Override
	public void render(DeployerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (!AllBlocks.DEPLOYER.typeOf(te.getBlockState()))
			return;

		renderItem(te, x, y, z, partialTicks);
		FilteringRenderer.renderOnTileEntity(te, x, y, z, partialTicks, destroyStage);
		renderComponents(te, x, y, z, partialTicks);
	}

	protected void renderItem(DeployerTileEntity te, double x, double y, double z, float partialTicks) {
		BlockState deployerState = te.getBlockState();
		Vec3d offset = getHandOffset(te, partialTicks, deployerState).add(VecHelper.getCenterOf(BlockPos.ZERO));
		GlStateManager.pushMatrix();
		GlStateManager.translated(offset.x + x, offset.y + y, offset.z + z);

		Direction facing = deployerState.get(FACING);
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		GlStateManager.rotatef(yRot, 0, 1, 0);
		GlStateManager.rotatef(zRot, 1, 0, 0);
		GlStateManager.translated(0, 0, -11 / 16f);
		float scale = .5f;
		GlStateManager.scaled(scale, scale, scale);

		TransformType transform = te.mode == Mode.PUNCH ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIXED;
		Minecraft.getInstance().getItemRenderer().renderItem(te.heldItem, transform);

		GlStateManager.popMatrix();
	}

	protected void renderComponents(DeployerTileEntity te, double x, double y, double z, float partialTicks) {
		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		KineticTileEntityRenderer.renderRotatingKineticBlock(te, getWorld(), getRenderedBlockState(te), x, y, z,
				buffer);

		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer pole = renderAndTransform(AllBlocks.DEPLOYER_POLE, blockState, pos, true);
		SuperByteBuffer hand = renderAndTransform(te.getHandPose(), blockState, pos, false);

		Vec3d offset = getHandOffset(te, partialTicks, blockState);
		pole.translate(x + offset.x, y + offset.y, z + offset.z).renderInto(buffer);
		hand.translate(x + offset.x, y + offset.y, z + offset.z).renderInto(buffer);

		TessellatorHelper.draw();
	}

	protected Vec3d getHandOffset(DeployerTileEntity te, float partialTicks, BlockState blockState) {
		float progress = 0;
		if (te.state == State.EXPANDING)
			progress = 1 - (te.timer - partialTicks * te.getTimerSpeed()) / 1000f;
		if (te.state == State.RETRACTING)
			progress = (te.timer - partialTicks * te.getTimerSpeed()) / 1000f;

		float handLength = te.getHandPose() == AllBlocks.DEPLOYER_HAND_POINTING ? 0
				: te.getHandPose() == AllBlocks.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
		float distance = Math.min(MathHelper.clamp(progress, 0, 1) * (te.reach + handLength), 21/16f);
		Vec3d offset = new Vec3d(blockState.get(FACING).getDirectionVec()).scale(distance);
		return offset;
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (!AllBlocks.DEPLOYER.typeOf(state))
			return Blocks.AIR.getDefaultState();
		return AllBlocks.SHAFT.block.getDefaultState().with(AXIS, ((IRotate) state.getBlock()).getRotationAxis(state));
	}

	private SuperByteBuffer renderAndTransform(AllBlocks renderBlock, BlockState deployerState, BlockPos pos,
			boolean axisDirectionMatters) {
		SuperByteBuffer buffer = CreateClient.bufferCache.renderGenericBlockModel(renderBlock.getDefault());
		Direction facing = deployerState.get(FACING);

		float zRotFirst = axisDirectionMatters
				&& (deployerState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90 : 0;
		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

		buffer.rotateCentered(Axis.Z, (float) ((zRotFirst) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Y, (float) ((yRot) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Z, (float) ((zRot) / 180 * Math.PI));
		buffer.light(deployerState.getPackedLightmapCoords(getWorld(), pos));
		return buffer;
	}

}
