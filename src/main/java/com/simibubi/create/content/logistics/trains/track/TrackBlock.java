package com.simibubi.create.content.logistics.trains.track;

import static com.simibubi.create.AllShapes.TRACK_ASC;
import static com.simibubi.create.AllShapes.TRACK_CROSS_DIAG_ORTHO;
import static com.simibubi.create.AllShapes.TRACK_CROSS_ORTHO_DIAG;
import static com.simibubi.create.AllShapes.TRACK_DIAG;
import static com.simibubi.create.AllShapes.TRACK_ORTHO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.curiosities.girder.GirderBlock;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.TrackPropagator;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;
import com.simibubi.create.foundation.block.render.DestroyProgressRenderingHandler;
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IBlockRenderProperties;

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

	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
		consumer.accept(new RenderProperties());
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
		TrackPropagator.onRailAdded(p_60463_, p_60464_, p_60462_);
	}

	@Override
	public Collection<DiscoveredLocation> getConnected(BlockGetter world, BlockPos pos, BlockState state,
		boolean linear, TrackNodeLocation connectedTo) {
		Collection<DiscoveredLocation> list;

		if (getTrackAxes(world, pos, state).size() > 1) {
			Vec3 center = Vec3.atBottomCenterOf(pos)
				.add(0, getElevationAtCenter(world, pos, state), 0);
			TrackShape shape = state.getValue(TrackBlock.SHAPE);
			list = new ArrayList<>();
			for (Vec3 axis : getTrackAxes(world, pos, state))
				for (boolean fromCenter : Iterate.trueAndFalse)
					ITrackBlock.addToListIfConnected(connectedTo, list,
						(d, b) -> axis.scale(b ? 0 : fromCenter ? -d : d)
							.add(center),
						b -> shape.getNormal(), null);
		} else
			list = ITrackBlock.super.getConnected(world, pos, state, linear, connectedTo);

		if (!state.getValue(HAS_TURN))
			return list;
		if (linear)
			return list;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof TrackTileEntity trackTE))
			return list;

		Map<BlockPos, BezierConnection> connections = trackTE.getConnections();
		connections.forEach((connectedPos, bc) -> ITrackBlock.addToListIfConnected(connectedTo, list,
			(d, b) -> d == 1 ? Vec3.atLowerCornerOf(bc.tePositions.get(b)) : bc.starts.get(b), bc.normals::get, bc));
		return list;
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
			Create.RAILWAYS.signalEdgeGroups.clear();
			CreateClient.RAILWAYS.signalEdgeGroups.clear();
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
				if (world.getBlockEntity(entry.getKey()) instanceof StationTileEntity station)
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
				if (girderState.getBlock() instanceof GirderBlock girderBlock
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
		return getFullShape(state);
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter pLevel, BlockPos pPos) {
		return getFullShape(state);
	}

	private VoxelShape getFullShape(BlockState state) {
		switch (state.getValue(SHAPE)) {
		case AE:
			return TRACK_ASC.get(Direction.EAST);
		case AW:
			return TRACK_ASC.get(Direction.WEST);
		case AN:
			return TRACK_ASC.get(Direction.NORTH);
		case AS:
			return TRACK_ASC.get(Direction.SOUTH);
		case CR_D:
			return AllShapes.TRACK_CROSS_DIAG;
		case CR_NDX:
			return TRACK_CROSS_ORTHO_DIAG.get(Direction.SOUTH);
		case CR_NDZ:
			return TRACK_CROSS_DIAG_ORTHO.get(Direction.SOUTH);
		case CR_O:
			return AllShapes.TRACK_CROSS;
		case CR_PDX:
			return TRACK_CROSS_DIAG_ORTHO.get(Direction.EAST);
		case CR_PDZ:
			return TRACK_CROSS_ORTHO_DIAG.get(Direction.EAST);
		case ND:
			return TRACK_DIAG.get(Direction.SOUTH);
		case PD:
			return TRACK_DIAG.get(Direction.EAST);
		case XO:
			return TRACK_ORTHO.get(Direction.EAST);
		case ZO:
			return TRACK_ORTHO.get(Direction.SOUTH);
		case NONE:
		default:
		}
		return AllShapes.TRACK_FALLBACK;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
		CollisionContext pContext) {
		switch (pState.getValue(SHAPE)) {
		case AE, AW, AN, AS:
			return Shapes.empty();
		default:
			return AllShapes.TRACK_COLLISION;
		}
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
		if (context.getLevel().isClientSide)
			TrackRemoval.wrenched(context.getClickedPos());
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		if (context.getLevel().isClientSide)
			TrackRemoval.sneakWrenched(context.getClickedPos());
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
		TransformStack.cast(ms)
			.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(direction)));
		return AllBlockPartials.TRACK_ASSEMBLING_OVERLAY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareTrackOverlay(BlockGetter world, BlockPos pos, BlockState state,
		BezierTrackPointLocation bezierPoint, AxisDirection direction, PoseStack ms, RenderedTrackOverlayType type) {
		TransformStack msr = TransformStack.cast(ms);

		Vec3 axis = null;
		Vec3 diff = null;
		Vec3 normal = null;
		Vec3 offset = null;

		if (bezierPoint != null && world.getBlockEntity(pos) instanceof TrackTileEntity trackTE) {
			BezierConnection bc = trackTE.connections.get(bezierPoint.curveTarget());
			if (bc != null) {
				double length = Mth.floor(bc.getLength() * 2);
				int seg = bezierPoint.segment() + 1;
				double t = seg / length;
				double tpre = (seg - 1) / length;
				double tpost = (seg + 1) / length;

				offset = bc.getPosition(t);
				normal = bc.getNormal(t);
				diff = bc.getPosition(tpost)
					.subtract(bc.getPosition(tpre))
					.normalize();

				msr.translate(offset.subtract(Vec3.atBottomCenterOf(pos)));
				msr.translate(0, -4 / 16f, 0);
			}
		}

		if (normal == null) {
			axis = state.getValue(SHAPE)
				.getAxes()
				.get(0);
			diff = axis.scale(direction.getStep())
				.normalize();
			normal = getUpNormal(world, pos, state);
		}

		Vec3 angles = TrackRenderer.getModelAngles(normal, diff);

		msr.centre()
			.rotateYRadians(angles.y)
			.rotateXRadians(angles.x)
			.unCentre();

		if (axis != null)
			msr.translate(0, axis.y != 0 ? 7 / 16f : 0, axis.y != 0 ? direction.getStep() * 2.5f / 16f : 0);
		else {
			msr.translate(0, 4 / 16f, 0);
			if (direction == AxisDirection.NEGATIVE)
				msr.rotateCentered(Direction.UP, Mth.PI);
		}

		msr.scale(type == RenderedTrackOverlayType.STATION ? 1 + 1 / 512f : 1);
		return type == RenderedTrackOverlayType.STATION ? AllBlockPartials.TRACK_STATION_OVERLAY
			: type == RenderedTrackOverlayType.SIGNAL ? AllBlockPartials.TRACK_SIGNAL_OVERLAY
				: AllBlockPartials.TRACK_SIGNAL_DUAL_OVERLAY;
	}

	@Override
	public boolean trackEquals(BlockState state1, BlockState state2) {
		return state1.getBlock() == this && state2.getBlock() == this
			&& state1.setValue(HAS_TURN, false) == state2.setValue(HAS_TURN, false);
	}

	public static class RenderProperties extends ReducedDestroyEffects implements DestroyProgressRenderingHandler {
		@Override
		public boolean renderDestroyProgress(ClientLevel level, LevelRenderer renderer, int breakerId, BlockPos pos,
			int progress, BlockState blockState) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof TrackTileEntity track)
				for (BlockPos trackPos : track.connections.keySet())
					renderer.destroyBlockProgress(trackPos.hashCode(), trackPos, progress);
			return false;
		}
	}

}
