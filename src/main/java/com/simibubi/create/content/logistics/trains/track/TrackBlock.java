package com.simibubi.create.content.logistics.trains.track;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.curiosities.girder.GirderBlock;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackPropagator;
import com.simibubi.create.content.logistics.trains.management.StationTileEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrackBlock extends Block implements EntityBlock, IWrenchable, ITrackBlock {

	public static final EnumProperty<TrackShape> SHAPE = EnumProperty.create("shape", TrackShape.class);
	public static final BooleanProperty HAS_TURN = BooleanProperty.create("turn");

	public TrackBlock(Properties p_49795_) {
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(SHAPE, TrackShape.ZO)
			.setValue(HAS_TURN, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_49915_) {
		super.createBlockStateDefinition(p_49915_.add(SHAPE, HAS_TURN));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState stateForPlacement = super.getStateForPlacement(ctx);
		if (ctx.getPlayer() == null)
			return stateForPlacement;

		Vec3 lookAngle = ctx.getPlayer()
			.getLookAngle();
		lookAngle = lookAngle.multiply(1, 0, 1);
		if (Mth.equal(lookAngle.length(), 0))
			lookAngle = VecHelper.rotate(new Vec3(0, 0, 1), -ctx.getPlayer()
				.getYRot(), Axis.Y);

		lookAngle = lookAngle.normalize();

		TrackShape best = TrackShape.ZO;
		double bestValue = Float.MAX_VALUE;
		for (TrackShape shape : TrackShape.values()) {
			if (shape.isJunction())
				continue;
			Vec3 axis = shape.getAxes()
				.get(0);
			double distance = Math.min(axis.distanceToSqr(lookAngle), axis.normalize()
				.scale(-1)
				.distanceToSqr(lookAngle));
			if (distance > bestValue)
				continue;
			bestValue = distance;
			best = shape;
		}

		Level level = ctx.getLevel();
		Vec3 bestAxis = best.getAxes()
			.get(0);
		if (bestAxis.lengthSqr() == 1)
			for (boolean neg : Iterate.trueAndFalse) {
				BlockPos offset = ctx.getClickedPos()
					.offset(new BlockPos(bestAxis.scale(neg ? -1 : 1)));

				if (level.getBlockState(offset)
					.isFaceSturdy(level, offset, Direction.UP)) {
					if (best == TrackShape.XO)
						best = neg ? TrackShape.AW : TrackShape.AE;
					if (best == TrackShape.ZO)
						best = neg ? TrackShape.AN : TrackShape.AS;
				}
			}

		return stateForPlacement.setValue(SHAPE, best);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState pState) {
		return PushReaction.BLOCK;
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		if (pOldState.getBlock() == this && pState.setValue(HAS_TURN, true) == pOldState.setValue(HAS_TURN, true))
			return;
		if (pLevel.isClientSide)
			return;
		LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
		if (!blockTicks.hasScheduledTick(pPos, this))
			pLevel.scheduleTick(pPos, this, 1);
		updateGirders(pState, pLevel, pPos, blockTicks);
	}

	@Override
	public void tick(BlockState p_60462_, ServerLevel p_60463_, BlockPos p_60464_, Random p_60465_) {
		for (Vec3 axis : getTrackAxes(p_60463_, p_60464_, p_60462_))
			TrackPropagator.onRailAdded(p_60463_, p_60464_, p_60462_, axis.normalize());
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		boolean removeTE = false;
		if (pState.getValue(HAS_TURN) && (!pState.is(pNewState.getBlock()) || !pNewState.getValue(HAS_TURN))) {
			BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
			if (blockEntity instanceof TrackTileEntity && !pLevel.isClientSide)
				((TrackTileEntity) blockEntity).removeInboundConnections();
			removeTE = true;
		}

		if (pNewState.getBlock() != this || pState.setValue(HAS_TURN, true) != pNewState.setValue(HAS_TURN, true))
			TrackPropagator.onRailRemoved(pLevel, pPos, pState);
		if (removeTE)
			pLevel.removeBlockEntity(pPos);
		if (!pLevel.isClientSide)
			updateGirders(pState, pLevel, pPos, pLevel.getBlockTicks());
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		ItemStack itemInHand = player.getItemInHand(hand);

		// debug remove all graphs
		if (Blocks.SPONGE.asItem() == itemInHand.getItem()) {
			Create.RAILWAYS.trackNetworks.clear();
			CreateClient.RAILWAYS.trackNetworks.clear();
			return InteractionResult.SUCCESS;
		}

		if (itemInHand.isEmpty()) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			for (Entry<BlockPos, BoundingBox> entry : StationTileEntity.assemblyAreas.get(world)
				.entrySet()) {
				if (!entry.getValue()
					.isInside(pos))
					continue;
				if (world.getBlockEntity(entry.getKey())instanceof StationTileEntity station)
					station.trackClicked(player, this, state, pos);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private void updateGirders(BlockState pState, Level pLevel, BlockPos pPos, LevelTickAccess<Block> blockTicks) {
		for (Vec3 vec3 : getTrackAxes(pLevel, pPos, pState)) {
			if (vec3.length() > 1 || vec3.y != 0)
				continue;
			for (int side : Iterate.positiveAndNegative) {
				BlockPos girderPos = pPos.below()
					.offset(vec3.z * side, 0, vec3.x * side);
				BlockState girderState = pLevel.getBlockState(girderPos);
				if (girderState.getBlock()instanceof GirderBlock girderBlock
					&& !blockTicks.hasScheduledTick(girderPos, girderBlock))
					pLevel.scheduleTick(girderPos, girderBlock, 1);
			}
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
		return reader.getBlockState(pos.below())
			.getBlock() != this;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
		return AllShapes.TRACK.get(Direction.UP);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState state) {
		if (!state.getValue(HAS_TURN))
			return null;
		return AllTileEntities.TRACK.create(p_153215_, state);
	}

	@Override
	public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state) {
		return state.getValue(SHAPE)
			.getNormal();
	}

	@Override
	public List<Vec3> getTrackAxes(BlockGetter world, BlockPos pos, BlockState state) {
		return state.getValue(SHAPE)
			.getAxes();
	}

	@Override
	public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis) {
		boolean vertical = axis.y != 0;
		return VecHelper.getCenterOf(pos)
			.add(0, (vertical ? 0 : -.5f), 0)
			.add(axis.scale(.5));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState overlay(BlockGetter world, BlockPos pos, BlockState existing, BlockState placed) {
		if (placed.getBlock() != this)
			return existing;

		TrackShape existingShape = existing.getValue(SHAPE);
		TrackShape placedShape = placed.getValue(SHAPE);
		TrackShape combinedShape = null;

		for (boolean flip : Iterate.trueAndFalse) {
			TrackShape s1 = flip ? existingShape : placedShape;
			TrackShape s2 = flip ? placedShape : existingShape;
			if (s1 == TrackShape.XO && s2 == TrackShape.ZO)
				combinedShape = TrackShape.CR_O;
			if (s1 == TrackShape.PD && s2 == TrackShape.ND)
				combinedShape = TrackShape.CR_D;
			if (s1 == TrackShape.XO && s2 == TrackShape.PD)
				combinedShape = TrackShape.CR_PDX;
			if (s1 == TrackShape.ZO && s2 == TrackShape.PD)
				combinedShape = TrackShape.CR_PDZ;
			if (s1 == TrackShape.XO && s2 == TrackShape.ND)
				combinedShape = TrackShape.CR_NDX;
			if (s1 == TrackShape.ZO && s2 == TrackShape.ND)
				combinedShape = TrackShape.CR_NDZ;
		}

		if (combinedShape != null)
			existing = existing.setValue(SHAPE, combinedShape);
		return existing;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation pRotation) {
		return state.setValue(SHAPE, state.getValue(SHAPE)
			.rotate(pRotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror pMirror) {
		return state.setValue(SHAPE, state.getValue(SHAPE)
			.mirror(pMirror));
	}

	@Override
	public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state) {
		return AllBlocks.SMALL_BOGEY.getDefaultState()
			.setValue(BlockStateProperties.HORIZONTAL_AXIS, state.getValue(SHAPE) == TrackShape.XO ? Axis.X : Axis.Z);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction,
		PoseStack ms) {
		new MatrixTransformStack(ms).rotateCentered(Direction.UP,
			AngleHelper.rad(AngleHelper.horizontalAngle(direction)));
		return AllBlockPartials.TRACK_ASSEMBLY_OVERLAY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareStationOverlay(BlockGetter world, BlockPos pos, BlockState state,
		AxisDirection direction, PoseStack ms) {
		Vec3 axis = state.getValue(SHAPE)
			.getAxes()
			.get(0);
		Vec3 directionVec = axis.scale(direction.getStep())
			.normalize();
		Vec3 normal = getUpNormal(world, pos, state);
		Vec3 angles = TrackRenderer.getModelAngles(normal, directionVec);
		new MatrixTransformStack(ms).centre()
			.rotateYRadians(angles.y + Math.PI)
			.rotateXRadians(-angles.x)
			.unCentre();

		return axis.lengthSqr() > 1 ? axis.y != 0 ? AllBlockPartials.TRACK_STATION_OVERLAY_ASCENDING
			: AllBlockPartials.TRACK_STATION_OVERLAY_DIAGONAL : AllBlockPartials.TRACK_STATION_OVERLAY;
	}

	@Override
	public boolean trackEquals(BlockState state1, BlockState state2) {
		return state1.getBlock() == this && state2.getBlock() == this
			&& state1.setValue(HAS_TURN, false) == state2.setValue(HAS_TURN, false);
	}

}
