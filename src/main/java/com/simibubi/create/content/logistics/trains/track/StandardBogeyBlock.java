package com.simibubi.create.content.logistics.trains.track;

import java.util.EnumSet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StandardBogeyBlock extends Block implements IBogeyBlock, ITE<StandardBogeyTileEntity> {

	public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	private boolean large;

	public StandardBogeyBlock(Properties p_i48440_1_, boolean large) {
		super(p_i48440_1_);
		this.large = large;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(AXIS);
		super.createBlockStateDefinition(builder);
	}

	static final EnumSet<Direction> STICKY_X = EnumSet.of(Direction.EAST, Direction.WEST);
	static final EnumSet<Direction> STICKY_Z = EnumSet.of(Direction.SOUTH, Direction.NORTH);

	@Override
	public EnumSet<Direction> getStickySurfaces(BlockGetter world, BlockPos pos, BlockState state) {
		return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Axis.X ? STICKY_X : STICKY_Z;
	}

	@Override
	public double getWheelPointSpacing() {
		return large ? .75f : 2;
	}

	@Override
	public double getWheelRadius() {
		return (large ? 12.5 : 6.5) / 16d;
	}

	@Override
	public Vec3 getConnectorAnchorOffset() {
		return new Vec3(0, 7 / 32f, 1);
	}

	@Override
	public boolean allowsSingleBogeyCarriage() {
		return !large;
	}

	@Override
	public BlockState getMatchingBogey(Direction upDirection, boolean axisAlongFirst) {
		if (upDirection != Direction.UP)
			return null;
		return defaultBlockState().setValue(AXIS, axisAlongFirst ? Axis.X : Axis.Z);
	}

	@Override
	public boolean isTrackAxisAlongFirstCoordinate(BlockState state) {
		return state.getValue(AXIS) == Axis.X;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(BlockState state, float wheelAngle, PoseStack ms, float partialTicks, MultiBufferSource buffers,
		int light, int overlay) {
		if (state != null) {
			ms.translate(.5f, .5f, .5f);
			if (state.getValue(AXIS) == Axis.X)
				ms.mulPose(Vector3f.YP.rotationDegrees(90));
		}

		ms.translate(0, -1.5, 0);

		VertexConsumer vb = buffers.getBuffer(RenderType.solid());
		BlockState air = Blocks.AIR.defaultBlockState();

		if (!large) {
			CachedBufferer.partial(AllBlockPartials.BOGEY_FRAME, air)
				.light(light)
				.renderInto(ms, vb);

			for (int side : Iterate.positiveAndNegative) {
				ms.pushPose();
				CachedBufferer.partial(AllBlockPartials.SMALL_BOGEY_WHEELS, air)
					.translate(0, 12 / 16f, side)
					.rotateX(wheelAngle)
					.light(light)
					.renderInto(ms, vb);
				ms.popPose();
			}
			return;
		}

		CachedBufferer.partial(AllBlockPartials.BOGEY_DRIVE, air)
			.light(light)
			.renderInto(ms, vb);
		CachedBufferer.partial(AllBlockPartials.BOGEY_PISTON, air)
			.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
			.light(light)
			.renderInto(ms, vb);

		ms.pushPose();
		CachedBufferer.partial(AllBlockPartials.LARGE_BOGEY_WHEELS, air)
			.translate(0, 1, 0)
			.rotateX(wheelAngle)
			.light(light)
			.renderInto(ms, vb);
		CachedBufferer.partial(AllBlockPartials.BOGEY_PIN, air)
			.translate(0, 1, 0)
			.rotateX(wheelAngle)
			.translate(0, 1 / 4f, 0)
			.rotateX(-wheelAngle)
			.light(light)
			.renderInto(ms, vb);
		ms.popPose();

	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		switch (pRotation) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			return pState.cycle(AXIS);
		default:
			return pState;
		}
	}

	@Override
	public Class<StandardBogeyTileEntity> getTileEntityClass() {
		return StandardBogeyTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends StandardBogeyTileEntity> getTileEntityType() {
		return AllTileEntities.BOGEY.get();
	}

}
