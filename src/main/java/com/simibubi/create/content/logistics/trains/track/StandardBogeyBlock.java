package com.simibubi.create.content.logistics.trains.track;

import java.util.EnumSet;

import com.jozufozu.flywheel.api.MaterialManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.entity.BogeyInstance;
import com.simibubi.create.content.logistics.trains.entity.CarriageBogey;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StandardBogeyBlock extends Block
	implements IBogeyBlock, IBE<StandardBogeyBlockEntity>, ProperWaterloggedBlock, ISpecialBlockItemRequirement {

	public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	private final boolean large;

	public StandardBogeyBlock(Properties p_i48440_1_, boolean large) {
		super(p_i48440_1_);
		this.large = large;
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(AXIS, WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

	static final EnumSet<Direction> STICKY_X = EnumSet.of(Direction.EAST, Direction.WEST);
	static final EnumSet<Direction> STICKY_Z = EnumSet.of(Direction.SOUTH, Direction.NORTH);

	@Override
	public EnumSet<Direction> getStickySurfaces(BlockGetter world, BlockPos pos, BlockState state) {
		return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Axis.X ? STICKY_X : STICKY_Z;
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public double getWheelPointSpacing() {
		return 2;
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
		return true;
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

		ms.translate(0, -1.5 - 1 / 128f, 0);

		VertexConsumer vb = buffers.getBuffer(RenderType.cutoutMipped());
		BlockState air = Blocks.AIR.defaultBlockState();

		for (int i : Iterate.zeroAndOne)
			CachedBufferer.block(AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Axis.Z))
				.translate(-.5f, .25f, i * -1)
				.centre()
				.rotateZ(wheelAngle)
				.unCentre()
				.light(light)
				.renderInto(ms, vb);

		if (large) {
			renderLargeBogey(wheelAngle, ms, light, vb, air);
		} else {
			renderBogey(wheelAngle, ms, light, vb, air);
		}
	}

	private void renderBogey(float wheelAngle, PoseStack ms, int light, VertexConsumer vb, BlockState air) {
		CachedBufferer.partial(AllPartialModels.BOGEY_FRAME, air)
			.scale(1 - 1 / 512f)
			.light(light)
			.renderInto(ms, vb);

		for (int side : Iterate.positiveAndNegative) {
			ms.pushPose();
			CachedBufferer.partial(AllPartialModels.SMALL_BOGEY_WHEELS, air)
				.translate(0, 12 / 16f, side)
				.rotateX(wheelAngle)
				.light(light)
				.renderInto(ms, vb);
			ms.popPose();
		}
	}

	private void renderLargeBogey(float wheelAngle, PoseStack ms, int light, VertexConsumer vb, BlockState air) {
		for (int i : Iterate.zeroAndOne)
			CachedBufferer.block(AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Axis.X))
				.translate(-.5f, .25f, .5f + i * -2)
				.centre()
				.rotateX(wheelAngle)
				.unCentre()
				.light(light)
				.renderInto(ms, vb);

		CachedBufferer.partial(AllPartialModels.BOGEY_DRIVE, air)
			.scale(1 - 1 / 512f)
			.light(light)
			.renderInto(ms, vb);
		CachedBufferer.partial(AllPartialModels.BOGEY_PISTON, air)
			.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
			.light(light)
			.renderInto(ms, vb);

		ms.pushPose();
		CachedBufferer.partial(AllPartialModels.LARGE_BOGEY_WHEELS, air)
			.translate(0, 1, 0)
			.rotateX(wheelAngle)
			.light(light)
			.renderInto(ms, vb);
		CachedBufferer.partial(AllPartialModels.BOGEY_PIN, air)
			.translate(0, 1, 0)
			.rotateX(wheelAngle)
			.translate(0, 1 / 4f, 0)
			.rotateX(-wheelAngle)
			.light(light)
			.renderInto(ms, vb);
		ms.popPose();
	}

	@Override
	public BogeyInstance createInstance(MaterialManager materialManager, CarriageBogey bogey) {
		if (large) {
			return new BogeyInstance.Drive(bogey, materialManager);
		} else {
			return new BogeyInstance.Frame(bogey, materialManager);
		}
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return switch (pRotation) {
		case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> pState.cycle(AXIS);
		default -> pState;
		};
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
		Player player) {
		return AllBlocks.RAILWAY_CASING.asStack();
	}

	@Override
	public Class<StandardBogeyBlockEntity> getBlockEntityClass() {
		return StandardBogeyBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StandardBogeyBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.BOGEY.get();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		return new ItemRequirement(ItemUseType.CONSUME, AllBlocks.RAILWAY_CASING.asStack());
	}

}
