package com.simibubi.create.content.contraptions.actors.roller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.roller.RollerBlockEntity.RollingMode;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.trains.bogey.StandardBogeyBlock;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.entity.TravellingPoint.ITrackSelector;
import com.simibubi.create.content.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class RollerMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return super.isActive(context) && !(context.contraption instanceof PulleyContraption)
			&& VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(RollerBlock.FACING));
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return new RollerActorInstance(materialManager, simulationWorld, context);
	}

	@Override
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffers) {
		if (!ContraptionRenderDispatcher.canInstance())
			RollerRenderer.renderInContraption(context, renderWorld, matrices, buffers);
	}

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(RollerBlock.FACING)
			.getNormal())
			.scale(.45)
			.subtract(0, 2, 0);
	}

	@Override
	protected float getBlockBreakingSpeed(MovementContext context) {
		return Mth.clamp(super.getBlockBreakingSpeed(context) * 1.5f, 1 / 128f, 16f);
	}

	@Override
	public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
		return super.canBreak(world, breakingPos, state) && !state.getCollisionShape(world, breakingPos)
			.isEmpty() && !AllBlocks.TRACK.has(state);
	}

	@Override
	protected DamageSource getDamageSource(Level level) {
		return CreateDamageSources.roller(level);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		Level world = context.world;
		BlockState stateVisited = world.getBlockState(pos);
		if (!stateVisited.isRedstoneConductor(world, pos))
			damageEntities(context, pos, world);
		if (world.isClientSide)
			return;

		List<BlockPos> positionsToBreak = getPositionsToBreak(context, pos);
		if (positionsToBreak.isEmpty()) {
			triggerPaver(context, pos);
			return;
		}

		BlockPos argMax = null;
		double max = -1;
		for (BlockPos toBreak : positionsToBreak) {
			float hardness = context.world.getBlockState(toBreak)
				.getDestroySpeed(world, toBreak);
			if (hardness < max)
				continue;
			max = hardness;
			argMax = toBreak;
		}

		if (argMax == null) {
			triggerPaver(context, pos);
			return;
		}

		context.data.put("ReferencePos", NbtUtils.writeBlockPos(pos));
		context.data.put("BreakingPos", NbtUtils.writeBlockPos(argMax));
		context.stall = true;
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
		super.onBlockBroken(context, pos, brokenState);
		if (!context.data.contains("ReferencePos"))
			return;

		BlockPos referencePos = NbtUtils.readBlockPos(context.data.getCompound("ReferencePos"));
		for (BlockPos otherPos : getPositionsToBreak(context, referencePos))
			if (!otherPos.equals(pos))
				destroyBlock(context, otherPos);

		triggerPaver(context, referencePos);
		context.data.remove("ReferencePos");
	}

	@Override
	protected void destroyBlock(MovementContext context, BlockPos breakingPos) {
		BlockState blockState = context.world.getBlockState(breakingPos);
		boolean noHarvest = blockState.is(BlockTags.NEEDS_IRON_TOOL) || blockState.is(BlockTags.NEEDS_STONE_TOOL)
			|| blockState.is(BlockTags.NEEDS_DIAMOND_TOOL);

		BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> {
			if (noHarvest || context.world.random.nextBoolean())
				return;
			this.dropItem(context, stack);
		});

		super.destroyBlock(context, breakingPos);
	}

	RollerTravellingPoint rollerScout = new RollerTravellingPoint();

	protected List<BlockPos> getPositionsToBreak(MovementContext context, BlockPos visitedPos) {
		ArrayList<BlockPos> positions = new ArrayList<>();

		RollingMode mode = getMode(context);
		if (mode != RollingMode.TUNNEL_PAVE)
			return positions;

		int startingY = 1;
		if (!getStateToPaveWith(context).isAir()) {
			ItemStack filter = ItemStack.of(context.blockEntityData.getCompound("Filter"));
			if (!ItemHelper
				.extract(context.contraption.getSharedInventory(),
					stack -> FilterItem.test(context.world, stack, filter), 1, true)
				.isEmpty())
				startingY = 0;
		}

		// Train
		PaveTask profileForTracks = createHeightProfileForTracks(context);
		if (profileForTracks != null) {
			for (Couple<Integer> coords : profileForTracks.keys()) {
				float height = profileForTracks.get(coords);
				BlockPos targetPosition = BlockPos.containing(coords.getFirst(), height, coords.getSecond());
				boolean shouldPlaceSlab = height > Math.floor(height) + .45;
				if (startingY == 1 && shouldPlaceSlab && context.world.getBlockState(targetPosition.above())
					.getOptionalValue(SlabBlock.TYPE)
					.orElse(SlabType.DOUBLE) == SlabType.BOTTOM)
					startingY = 2;
				for (int i = startingY; i <= (shouldPlaceSlab ? 3 : 2); i++)
					if (testBreakerTarget(context, targetPosition.above(i), i))
						positions.add(targetPosition.above(i));
			}
			return positions;
		}

		// Otherwise
		for (int i = startingY; i <= 2; i++)
			if (testBreakerTarget(context, visitedPos.above(i), i))
				positions.add(visitedPos.above(i));

		return positions;
	}

	protected boolean testBreakerTarget(MovementContext context, BlockPos target, int columnY) {
		BlockState stateToPaveWith = getStateToPaveWith(context);
		BlockState stateToPaveWithAsSlab = getStateToPaveWithAsSlab(context);
		BlockState stateAbove = context.world.getBlockState(target);
		if (columnY == 0 && stateAbove.is(stateToPaveWith.getBlock()))
			return false;
		if (stateToPaveWithAsSlab != null && columnY == 1 && stateAbove.is(stateToPaveWithAsSlab.getBlock()))
			return false;
		return canBreak(context.world, target, stateAbove);
	}

	@Nullable
	protected PaveTask createHeightProfileForTracks(MovementContext context) {
		if (context.contraption == null)
			return null;
		if (!(context.contraption.entity instanceof CarriageContraptionEntity cce))
			return null;
		Carriage carriage = cce.getCarriage();
		if (carriage == null)
			return null;
		Train train = carriage.train;
		if (train == null || train.graph == null)
			return null;

		CarriageBogey mainBogey = carriage.bogeys.getFirst();
		TravellingPoint point = mainBogey.trailing();

		rollerScout.node1 = point.node1;
		rollerScout.node2 = point.node2;
		rollerScout.edge = point.edge;
		rollerScout.position = point.position;

		Axis axis = Axis.X;
		StructureBlockInfo info = context.contraption.getBlocks()
			.get(BlockPos.ZERO);
		if (info != null && info.state().hasProperty(StandardBogeyBlock.AXIS))
			axis = info.state().getValue(StandardBogeyBlock.AXIS);

		Direction orientation = cce.getInitialOrientation();
		Direction rollerFacing = context.state.getValue(RollerBlock.FACING);

		int step = orientation.getAxisDirection()
			.getStep();
		double widthWiseOffset = axis.choose(-context.localPos.getZ(), 0, -context.localPos.getX()) * step;
		double lengthWiseOffset = axis.choose(-context.localPos.getX(), 0, context.localPos.getZ()) * step - 1;

		if (rollerFacing == orientation.getClockWise())
			lengthWiseOffset += 1;

		double distanceToTravel = 2;
		PaveTask heightProfile = new PaveTask(widthWiseOffset, widthWiseOffset);
		ITrackSelector steering = rollerScout.steer(SteerDirection.NONE, new Vec3(0, 1, 0));

		rollerScout.traversalCallback = (edge, coords) -> {
		};
		rollerScout.travel(train.graph, lengthWiseOffset + 1, steering);

		rollerScout.traversalCallback = (edge, coords) -> TrackPaverV2.pave(heightProfile, train.graph, edge,
			coords.getFirst(), coords.getSecond());
		rollerScout.travel(train.graph, distanceToTravel, steering);

		for (Couple<Integer> entry : heightProfile.keys())
			heightProfile.put(entry.getFirst(), entry.getSecond(), context.localPos.getY() + heightProfile.get(entry));

		return heightProfile;
	}

	protected void triggerPaver(MovementContext context, BlockPos pos) {
		BlockState stateToPaveWith = getStateToPaveWith(context);
		BlockState stateToPaveWithAsSlab = getStateToPaveWithAsSlab(context);
		RollingMode mode = getMode(context);

		Vec3 directionVec = Vec3.atLowerCornerOf(context.state.getValue(RollerBlock.FACING)
			.getClockWise()
			.getNormal());
		directionVec = context.rotation.apply(directionVec);
		PaveResult paveResult = PaveResult.PASS;
		int yOffset = 0;

		List<Pair<BlockPos, Boolean>> paveSet = new ArrayList<>();
		PaveTask profileForTracks = createHeightProfileForTracks(context);
		if (profileForTracks == null)
			paveSet.add(Pair.of(pos, false));
		else
			for (Couple<Integer> coords : profileForTracks.keys()) {
				float height = profileForTracks.get(coords);
				boolean shouldPlaceSlab = height > Math.floor(height) + .45;
				BlockPos targetPosition = BlockPos.containing(coords.getFirst(), height, coords.getSecond());
				paveSet.add(Pair.of(targetPosition, shouldPlaceSlab));
			}

		if (paveSet.isEmpty())
			return;

		while (paveResult == PaveResult.PASS) {
			if (yOffset > AllConfigs.server().kinetics.rollerFillDepth.get()) {
				paveResult = PaveResult.FAIL;
				break;
			}

			Set<Pair<BlockPos, Boolean>> currentLayer = new HashSet<>();
			if (mode == RollingMode.WIDE_FILL) {
				for (Pair<BlockPos, Boolean> anchor : paveSet) {
					int radius = (yOffset + 1) / 2;
					for (int i = -radius; i <= radius; i++)
						for (int j = -radius; j <= radius; j++)
							if (BlockPos.ZERO.distManhattan(new BlockPos(i, 0, j)) <= radius)
								currentLayer.add(Pair.of(anchor.getFirst()
									.offset(i, -yOffset, j), anchor.getSecond()));
				}
			} else
				for (Pair<BlockPos, Boolean> anchor : paveSet)
					currentLayer.add(Pair.of(anchor.getFirst()
						.below(yOffset), anchor.getSecond()));

			boolean completelyBlocked = true;
			boolean anyBlockPlaced = false;

			for (Pair<BlockPos, Boolean> currentPos : currentLayer) {
				if (stateToPaveWithAsSlab != null && yOffset == 0 && currentPos.getSecond())
					tryFill(context, currentPos.getFirst()
						.above(), stateToPaveWithAsSlab);
				paveResult = tryFill(context, currentPos.getFirst(), stateToPaveWith);
				if (paveResult != PaveResult.FAIL)
					completelyBlocked = false;
				if (paveResult == PaveResult.SUCCESS)
					anyBlockPlaced = true;
			}

			if (anyBlockPlaced)
				paveResult = PaveResult.SUCCESS;
			else if (!completelyBlocked || yOffset == 0)
				paveResult = PaveResult.PASS;

			if (paveResult == PaveResult.SUCCESS && stateToPaveWith.getBlock() instanceof FallingBlock)
				paveResult = PaveResult.PASS;
			if (paveResult != PaveResult.PASS)
				break;
			if (mode == RollingMode.TUNNEL_PAVE)
				break;

			yOffset++;
		}

		if (paveResult == PaveResult.SUCCESS) {
			context.data.putInt("WaitingTicks", 2);
			context.data.put("LastPos", NbtUtils.writeBlockPos(pos));
			context.stall = true;
		}
	}

	public static BlockState getStateToPaveWith(ItemStack itemStack) {
		if (itemStack.getItem()instanceof BlockItem bi) {
			BlockState defaultBlockState = bi.getBlock()
				.defaultBlockState();
			if (defaultBlockState.hasProperty(SlabBlock.TYPE))
				defaultBlockState = defaultBlockState.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
			return defaultBlockState;
		}
		return Blocks.AIR.defaultBlockState();
	}

	protected BlockState getStateToPaveWith(MovementContext context) {
		return getStateToPaveWith(ItemStack.of(context.blockEntityData.getCompound("Filter")));
	}

	protected BlockState getStateToPaveWithAsSlab(MovementContext context) {
		BlockState stateToPaveWith = getStateToPaveWith(context);
		if (stateToPaveWith.hasProperty(SlabBlock.TYPE))
			return stateToPaveWith.setValue(SlabBlock.TYPE, SlabType.BOTTOM);

		Block block = stateToPaveWith.getBlock();
		if (block == null)
			return null;

		ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
		String namespace = rl.getNamespace();
		String blockName = rl.getPath();
		int nameLength = blockName.length();

		List<String> possibleSlabLocations = new ArrayList<>();
		possibleSlabLocations.add(blockName + "_slab");

		if (blockName.endsWith("s") && nameLength > 1)
			possibleSlabLocations.add(blockName.substring(0, nameLength - 1) + "_slab");
		if (blockName.endsWith("planks") && nameLength > 7)
			possibleSlabLocations.add(blockName.substring(0, nameLength - 7) + "_slab");

		for (String locationAttempt : possibleSlabLocations) {
			Optional<Block> result = ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(namespace, locationAttempt))
				.map(slabHolder -> slabHolder.value());
			if (result.isEmpty())
				continue;
			return result.get()
				.defaultBlockState();
		}

		return null;
	}

	protected RollingMode getMode(MovementContext context) {
		return RollingMode.values()[context.blockEntityData.getInt("ScrollValue")];
	}

	private final class RollerTravellingPoint extends TravellingPoint {

		public BiConsumer<TrackEdge, Couple<Double>> traversalCallback;

		@Override
		protected Double edgeTraversedFrom(TrackGraph graph, boolean forward, IEdgePointListener edgePointListener,
			ITurnListener turnListener, double prevPos, double totalDistance) {
			double from = forward ? prevPos : position;
			double to = forward ? position : prevPos;
			traversalCallback.accept(edge, Couple.create(from, to));
			return super.edgeTraversedFrom(graph, forward, edgePointListener, turnListener, prevPos, totalDistance);
		}

	}

	private enum PaveResult {
		FAIL, PASS, SUCCESS;
	}

	protected PaveResult tryFill(MovementContext context, BlockPos targetPos, BlockState toPlace) {
		Level level = context.world;
		if (!level.isLoaded(targetPos))
			return PaveResult.FAIL;
		BlockState existing = level.getBlockState(targetPos);
		if (existing.is(toPlace.getBlock()))
			return PaveResult.PASS;
		if (!existing.is(BlockTags.LEAVES) && !existing.canBeReplaced()
			&& !existing.getCollisionShape(level, targetPos)
				.isEmpty())
			return PaveResult.FAIL;

		ItemStack filter = ItemStack.of(context.blockEntityData.getCompound("Filter"));
		ItemStack held = ItemHelper.extract(context.contraption.getSharedInventory(),
			stack -> FilterItem.test(context.world, stack, filter), 1, false);
		if (held.isEmpty())
			return PaveResult.FAIL;

		level.setBlockAndUpdate(targetPos, toPlace);
		return PaveResult.SUCCESS;
	}

}
