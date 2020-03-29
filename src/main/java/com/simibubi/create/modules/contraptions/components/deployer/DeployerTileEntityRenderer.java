package com.simibubi.create.modules.contraptions.components.deployer;

import static com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock.FACING;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.create.modules.contraptions.components.deployer.DeployerTileEntity.State;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class DeployerTileEntityRenderer extends SafeTileEntityRenderer<DeployerTileEntity> {

	public DeployerTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void renderWithGL(DeployerTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		renderItem(te, x, y, z, partialTicks);
		FilteringRenderer.renderOnTileEntity(te, x, y, z, partialTicks, destroyStage);
		renderComponents(te, x, y, z, partialTicks);
	}

	protected void renderItem(DeployerTileEntity te, double x, double y, double z, float partialTicks) {
		BlockState deployerState = te.getBlockState();
		Vec3d offset = getHandOffset(te, partialTicks, deployerState).add(VecHelper.getCenterOf(BlockPos.ZERO));
		RenderSystem.pushMatrix();
		RenderSystem.translated(offset.x + x, offset.y + y, offset.z + z);

		Direction facing = deployerState.get(FACING);
		boolean punching = te.mode == Mode.PUNCH;

		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		boolean displayMode = facing == Direction.UP && te.getSpeed() == 0 && !punching;

		RenderSystem.rotatef(yRot, 0, 1, 0);
		if (!displayMode) {
			RenderSystem.rotatef(zRot, 1, 0, 0);
			RenderSystem.translated(0, 0, -11 / 16f);
		}

		if (punching)
			RenderSystem.translatef(0, 1 / 8f, -1 / 16f);

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		TransformType transform = TransformType.NONE;
		boolean isBlockItem =
			(te.heldItem.getItem() instanceof BlockItem) && itemRenderer.getModelWithOverrides(te.heldItem).isGui3d();

		if (displayMode) {
			float scale = isBlockItem ? 1.25f : 1;
			RenderSystem.translated(0, isBlockItem ? 9 / 16f : 11 / 16f, 0);
			RenderSystem.scaled(scale, scale, scale);
			transform = TransformType.GROUND;
			RenderSystem.rotatef(AnimationTickHolder.getRenderTick(), 0, 1, 0);

		} else {
			float scale = punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
			RenderSystem.scaled(scale, scale, scale);
			transform = punching ? TransformType.THIRD_PERSON_RIGHT_HAND : TransformType.FIXED;
		}

		itemRenderer.renderItem(te.heldItem, transform);
		RenderSystem.popMatrix();
	}

	protected void renderComponents(DeployerTileEntity te, double x, double y, double z, float partialTicks) {
		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		KineticTileEntityRenderer.renderRotatingKineticBlock(te, getWorld(), getRenderedBlockState(te), x, y, z,
				buffer);

		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer pole = renderAndTransform(getWorld(), AllBlockPartials.DEPLOYER_POLE, blockState, pos, true);
		SuperByteBuffer hand = renderAndTransform(getWorld(), te.getHandPose(), blockState, pos, false);

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

		float handLength = te.getHandPose() == AllBlockPartials.DEPLOYER_HAND_POINTING ? 0
				: te.getHandPose() == AllBlockPartials.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
		float distance = Math.min(MathHelper.clamp(progress, 0, 1) * (te.reach + handLength), 21 / 16f);
		Vec3d offset = new Vec3d(blockState.get(FACING).getDirectionVec()).scale(distance);
		return offset;
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (!AllBlocks.DEPLOYER.typeOf(state))
			return Blocks.AIR.getDefaultState();
		return AllBlocks.SHAFT.get().getDefaultState().with(AXIS, ((IRotate) state.getBlock()).getRotationAxis(state));
	}

	private static SuperByteBuffer renderAndTransform(World world, AllBlockPartials renderBlock,
			BlockState deployerState, BlockPos pos, boolean axisDirectionMatters) {
		SuperByteBuffer buffer = renderBlock.renderOn(deployerState);
		Direction facing = deployerState.get(FACING);

		float zRotFirst =
			axisDirectionMatters && (deployerState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90
					: 0;
		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

		buffer.rotateCentered(Axis.Z, (float) ((zRotFirst) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Y, (float) ((yRot) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Z, (float) ((zRot) / 180 * Math.PI));
		buffer.light(deployerState.getPackedLightmapCoords(world, pos));
		return buffer;
	}

	public static List<SuperByteBuffer> renderListInContraption(MovementContext context) {
		BlockState blockState = context.state;
		BlockPos pos = BlockPos.ZERO;
		Mode mode = NBTHelper.readEnum(context.tileData.getString("Mode"), Mode.class);
		World world = context.world;
		AllBlockPartials handPose =
			mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING : AllBlockPartials.DEPLOYER_HAND_POINTING;

		SuperByteBuffer pole = renderAndTransform(world, AllBlockPartials.DEPLOYER_POLE, blockState, pos, true);
		SuperByteBuffer hand = renderAndTransform(world, handPose, blockState, pos, false);

		double factor;
		if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
			factor = MathHelper.sin(AnimationTickHolder.getRenderTick() * .5f) * .25f + .25f;
		} else {
			Vec3d center = VecHelper.getCenterOf(new BlockPos(context.position));
			double distance = context.position.distanceTo(center);
			double nextDistance = context.position.add(context.motion).distanceTo(center);
			factor = .5f - MathHelper.clamp(
					MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), distance, nextDistance), 0, 1);
		}

		Vec3d offset = new Vec3d(blockState.get(FACING).getDirectionVec()).scale(factor);
		pole.translate(offset.x, offset.y, offset.z);
		hand.translate(offset.x, offset.y, offset.z);

		return Arrays.asList(pole, hand);
	}

}
