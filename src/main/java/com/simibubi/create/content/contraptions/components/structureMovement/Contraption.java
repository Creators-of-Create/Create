package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.components.actors.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.actors.controls.ContraptionControlsMovement;
import com.simibubi.create.content.contraptions.components.steam.PoweredShaftBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.MagnetBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.RopeBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.render.EmptyLighter;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.curiosities.deco.SlidingDoorBlock;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.content.logistics.block.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.BBHelper;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.ICoordinate;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.NBTProcessors;
import com.simibubi.create.foundation.utility.UniqueLinkedList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.GameData;

public abstract class Contraption {

	public Optional<List<AABB>> simplifiedEntityColliders;
	public AbstractContraptionEntity entity;

	public AABB bounds;
	public BlockPos anchor;
	public boolean stalled;
	public boolean hasUniversalCreativeCrate;
	public boolean disassembled;

	protected Map<BlockPos, StructureBlockInfo> blocks;
	protected List<MutablePair<StructureBlockInfo, MovementContext>> actors;
	protected Map<BlockPos, MovingInteractionBehaviour> interactors;
	protected List<ItemStack> disabledActors;

	protected List<AABB> superglue;
	protected List<BlockPos> seats;
	protected Map<UUID, Integer> seatMapping;
	protected Map<UUID, BlockFace> stabilizedSubContraptions;
	protected MountedStorageManager storage;

	private Set<SuperGlueEntity> glueToRemove;
	private Map<BlockPos, Entity> initialPassengers;
	private List<BlockFace> pendingSubContraptions;

	private CompletableFuture<Void> simplifiedEntityColliderProvider;

	// Client
	public Map<BlockPos, IModelData> modelData;
	public Map<BlockPos, BlockEntity> presentBlockEntities;
	public List<BlockEntity> maybeInstancedBlockEntities;
	public List<BlockEntity> specialRenderedBlockEntities;

	protected ContraptionWorld world;
	public boolean deferInvalidate;

	public Contraption() {
		blocks = new HashMap<>();
		seats = new ArrayList<>();
		actors = new ArrayList<>();
		disabledActors = new ArrayList<>();
		modelData = new HashMap<>();
		interactors = new HashMap<>();
		superglue = new ArrayList<>();
		seatMapping = new HashMap<>();
		glueToRemove = new HashSet<>();
		initialPassengers = new HashMap<>();
		presentBlockEntities = new HashMap<>();
		maybeInstancedBlockEntities = new ArrayList<>();
		specialRenderedBlockEntities = new ArrayList<>();
		pendingSubContraptions = new ArrayList<>();
		stabilizedSubContraptions = new HashMap<>();
		simplifiedEntityColliders = Optional.empty();
		storage = new MountedStorageManager();
	}

	public ContraptionWorld getContraptionWorld() {
		if (world == null)
			world = new ContraptionWorld(entity.level, this);
		return world;
	}

	public abstract boolean assemble(Level world, BlockPos pos) throws AssemblyException;

	public abstract boolean canBeStabilized(Direction facing, BlockPos localPos);

	protected abstract ContraptionType getType();

	protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean customBlockRemoval(LevelAccessor world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean addToInitialFrontier(Level world, BlockPos pos, Direction forcedDirection,
		Queue<BlockPos> frontier) throws AssemblyException {
		return true;
	}

	public static Contraption fromNBT(Level world, CompoundTag nbt, boolean spawnData) {
		String type = nbt.getString("Type");
		Contraption contraption = ContraptionType.fromType(type);
		contraption.readNBT(world, nbt, spawnData);
		contraption.world = new ContraptionWorld(world, contraption);
		contraption.gatherBBsOffThread();
		return contraption;
	}

	public boolean searchMovedStructure(Level world, BlockPos pos, @Nullable Direction forcedDirection)
		throws AssemblyException {
		initialPassengers.clear();
		Queue<BlockPos> frontier = new UniqueLinkedList<>();
		Set<BlockPos> visited = new HashSet<>();
		anchor = pos;

		if (bounds == null)
			bounds = new AABB(BlockPos.ZERO);

		if (!BlockMovementChecks.isBrittle(world.getBlockState(pos)))
			frontier.add(pos);
		if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
			return false;
		for (int limit = 100000; limit > 0; limit--) {
			if (frontier.isEmpty())
				return true;
			if (!moveBlock(world, forcedDirection, frontier, visited))
				return false;
		}
		throw AssemblyException.structureTooLarge();
	}

	public void onEntityCreated(AbstractContraptionEntity entity) {
		this.entity = entity;

		// Create subcontraptions
		for (BlockFace blockFace : pendingSubContraptions) {
			Direction face = blockFace.getFace();
			StabilizedContraption subContraption = new StabilizedContraption(face);
			Level world = entity.level;
			BlockPos pos = blockFace.getPos();
			try {
				if (!subContraption.assemble(world, pos))
					continue;
			} catch (AssemblyException e) {
				continue;
			}
			subContraption.removeBlocksFromWorld(world, BlockPos.ZERO);
			OrientedContraptionEntity movedContraption = OrientedContraptionEntity.create(world, subContraption, face);
			BlockPos anchor = blockFace.getConnectedPos();
			movedContraption.setPos(anchor.getX() + .5f, anchor.getY(), anchor.getZ() + .5f);
			world.addFreshEntity(movedContraption);
			stabilizedSubContraptions.put(movedContraption.getUUID(), new BlockFace(toLocalPos(pos), face));
		}

		storage.createHandlers();
		gatherBBsOffThread();
	}

	public void onEntityRemoved(AbstractContraptionEntity entity) {
		if (simplifiedEntityColliderProvider != null) {
			simplifiedEntityColliderProvider.cancel(false);
			simplifiedEntityColliderProvider = null;
		}
	}

	public void onEntityInitialize(Level world, AbstractContraptionEntity contraptionEntity) {
		if (world.isClientSide)
			return;

		for (OrientedContraptionEntity orientedCE : world.getEntitiesOfClass(OrientedContraptionEntity.class,
			contraptionEntity.getBoundingBox()
				.inflate(1)))
			if (stabilizedSubContraptions.containsKey(orientedCE.getUUID()))
				orientedCE.startRiding(contraptionEntity);

		for (BlockPos seatPos : getSeats()) {
			Entity passenger = initialPassengers.get(seatPos);
			if (passenger == null)
				continue;
			int seatIndex = getSeats().indexOf(seatPos);
			if (seatIndex == -1)
				continue;
			contraptionEntity.addSittingPassenger(passenger, seatIndex);
		}
	}

	/** move the first block in frontier queue */
	protected boolean moveBlock(Level world, @Nullable Direction forcedDirection, Queue<BlockPos> frontier,
		Set<BlockPos> visited) throws AssemblyException {
		BlockPos pos = frontier.poll();
		if (pos == null)
			return false;
		visited.add(pos);

		if (world.isOutsideBuildHeight(pos))
			return true;
		if (!world.isLoaded(pos))
			throw AssemblyException.unloadedChunk(pos);
		if (isAnchoringBlockAt(pos))
			return true;
		BlockState state = world.getBlockState(pos);
		if (!BlockMovementChecks.isMovementNecessary(state, world, pos))
			return true;
		if (!movementAllowed(state, world, pos))
			throw AssemblyException.unmovableBlock(pos, state);
		if (state.getBlock() instanceof AbstractChassisBlock
			&& !moveChassis(world, pos, forcedDirection, frontier, visited))
			return false;

		if (AllBlocks.BELT.has(state))
			moveBelt(pos, frontier, visited, state);

		if (AllBlocks.WINDMILL_BEARING.has(state) && world.getBlockEntity(pos)instanceof WindmillBearingBlockEntity wbte)
			wbte.disassembleForMovement();

		if (AllBlocks.GANTRY_CARRIAGE.has(state))
			moveGantryPinion(world, pos, frontier, visited, state);

		if (AllBlocks.GANTRY_SHAFT.has(state))
			moveGantryShaft(world, pos, frontier, visited, state);

		if (AllBlocks.STICKER.has(state) && state.getValue(StickerBlock.EXTENDED)) {
			Direction offset = state.getValue(StickerBlock.FACING);
			BlockPos attached = pos.relative(offset);
			if (!visited.contains(attached)
				&& !BlockMovementChecks.isNotSupportive(world.getBlockState(attached), offset.getOpposite()))
				frontier.add(attached);
		}

		// Double Chest halves stick together
		if (state.hasProperty(ChestBlock.TYPE) && state.hasProperty(ChestBlock.FACING)
			&& state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			Direction offset = ChestBlock.getConnectedDirection(state);
			BlockPos attached = pos.relative(offset);
			if (!visited.contains(attached))
				frontier.add(attached);
		}

		// Bogeys tend to have sticky sides
		if (state.getBlock()instanceof AbstractBogeyBlock<?> bogey)
			for (Direction d : bogey.getStickySurfaces(world, pos, state))
				if (!visited.contains(pos.relative(d)))
					frontier.add(pos.relative(d));

		// Bearings potentially create stabilized sub-contraptions
		if (AllBlocks.MECHANICAL_BEARING.has(state))
			moveBearing(pos, frontier, visited, state);

		// WM Bearings attach their structure when moved
		if (AllBlocks.WINDMILL_BEARING.has(state))
			moveWindmillBearing(pos, frontier, visited, state);

		// Seats transfer their passenger to the contraption
		if (state.getBlock() instanceof SeatBlock)
			moveSeat(world, pos);

		// Pulleys drag their rope and their attached structure
		if (state.getBlock() instanceof PulleyBlock)
			movePulley(world, pos, frontier, visited);

		// Pistons drag their attaches poles and extension
		if (state.getBlock() instanceof MechanicalPistonBlock)
			if (!moveMechanicalPiston(world, pos, frontier, visited, state))
				return false;
		if (isExtensionPole(state))
			movePistonPole(world, pos, frontier, visited, state);
		if (isPistonHead(state))
			movePistonHead(world, pos, frontier, visited, state);

		// Cart assemblers attach themselves
		BlockPos posDown = pos.below();
		BlockState stateBelow = world.getBlockState(posDown);
		if (!visited.contains(posDown) && AllBlocks.CART_ASSEMBLER.has(stateBelow))
			frontier.add(posDown);

		// Slime blocks and super glue drag adjacent blocks if possible
		for (Direction offset : Iterate.directions) {
			BlockPos offsetPos = pos.relative(offset);
			BlockState blockState = world.getBlockState(offsetPos);
			if (isAnchoringBlockAt(offsetPos))
				continue;
			if (!movementAllowed(blockState, world, offsetPos)) {
				if (offset == forcedDirection)
					throw AssemblyException.unmovableBlock(pos, state);
				continue;
			}

			boolean wasVisited = visited.contains(offsetPos);
			boolean faceHasGlue = SuperGlueEntity.isGlued(world, pos, offset, glueToRemove);
			boolean blockAttachedTowardsFace =
				BlockMovementChecks.isBlockAttachedTowards(blockState, world, offsetPos, offset.getOpposite());
			boolean brittle = BlockMovementChecks.isBrittle(blockState);
			boolean canStick = !brittle && state.canStickTo(blockState) && blockState.canStickTo(state);
			if (canStick) {
				if (state.getPistonPushReaction() == PushReaction.PUSH_ONLY
					|| blockState.getPistonPushReaction() == PushReaction.PUSH_ONLY) {
					canStick = false;
				}
				if (BlockMovementChecks.isNotSupportive(state, offset)) {
					canStick = false;
				}
				if (BlockMovementChecks.isNotSupportive(blockState, offset.getOpposite())) {
					canStick = false;
				}
			}

			if (!wasVisited && (canStick || blockAttachedTowardsFace || faceHasGlue
				|| (offset == forcedDirection && !BlockMovementChecks.isNotSupportive(state, forcedDirection))))
				frontier.add(offsetPos);
		}

		addBlock(pos, capture(world, pos));
		if (blocks.size() <= AllConfigs.server().kinetics.maxBlocksMoved.get())
			return true;
		else
			throw AssemblyException.structureTooLarge();
	}

	protected void movePistonHead(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		Direction direction = state.getValue(MechanicalPistonHeadBlock.FACING);
		BlockPos offset = pos.relative(direction.getOpposite());
		if (!visited.contains(offset)) {
			BlockState blockState = world.getBlockState(offset);
			if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
				.getAxis() == direction.getAxis())
				frontier.add(offset);
			if (blockState.getBlock() instanceof MechanicalPistonBlock) {
				Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
				if (pistonFacing == direction
					&& blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
					frontier.add(offset);
			}
		}
		if (state.getValue(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
			BlockPos attached = pos.relative(direction);
			if (!visited.contains(attached))
				frontier.add(attached);
		}
	}

	protected void movePistonPole(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		for (Direction d : Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING)
			.getAxis())) {
			BlockPos offset = pos.relative(d);
			if (!visited.contains(offset)) {
				BlockState blockState = world.getBlockState(offset);
				if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
					.getAxis() == d.getAxis())
					frontier.add(offset);
				if (isPistonHead(blockState) && blockState.getValue(MechanicalPistonHeadBlock.FACING)
					.getAxis() == d.getAxis())
					frontier.add(offset);
				if (blockState.getBlock() instanceof MechanicalPistonBlock) {
					Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
					if (pistonFacing == d || pistonFacing == d.getOpposite()
						&& blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
						frontier.add(offset);
				}
			}
		}
	}

	protected void moveGantryPinion(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		BlockPos offset = pos.relative(state.getValue(GantryCarriageBlock.FACING));
		if (!visited.contains(offset))
			frontier.add(offset);
		Axis rotationAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
		for (Direction d : Iterate.directionsInAxis(rotationAxis)) {
			offset = pos.relative(d);
			BlockState offsetState = world.getBlockState(offset);
			if (AllBlocks.GANTRY_SHAFT.has(offsetState) && offsetState.getValue(GantryShaftBlock.FACING)
				.getAxis() == d.getAxis())
				if (!visited.contains(offset))
					frontier.add(offset);
		}
	}

	protected void moveGantryShaft(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		for (Direction d : Iterate.directions) {
			BlockPos offset = pos.relative(d);
			if (!visited.contains(offset)) {
				BlockState offsetState = world.getBlockState(offset);
				Direction facing = state.getValue(GantryShaftBlock.FACING);
				if (d.getAxis() == facing.getAxis() && AllBlocks.GANTRY_SHAFT.has(offsetState)
					&& offsetState.getValue(GantryShaftBlock.FACING) == facing)
					frontier.add(offset);
				else if (AllBlocks.GANTRY_CARRIAGE.has(offsetState)
					&& offsetState.getValue(GantryCarriageBlock.FACING) == d)
					frontier.add(offset);
			}
		}
	}

	private void moveWindmillBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
		Direction facing = state.getValue(WindmillBearingBlock.FACING);
		BlockPos offset = pos.relative(facing);
		if (!visited.contains(offset))
			frontier.add(offset);
	}

	private void moveBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
		Direction facing = state.getValue(MechanicalBearingBlock.FACING);
		if (!canBeStabilized(facing, pos.subtract(anchor))) {
			BlockPos offset = pos.relative(facing);
			if (!visited.contains(offset))
				frontier.add(offset);
			return;
		}
		pendingSubContraptions.add(new BlockFace(pos, facing));
	}

	private void moveBelt(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
		BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
		BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
		if (nextPos != null && !visited.contains(nextPos))
			frontier.add(nextPos);
		if (prevPos != null && !visited.contains(prevPos))
			frontier.add(prevPos);
	}

	private void moveSeat(Level world, BlockPos pos) {
		BlockPos local = toLocalPos(pos);
		getSeats().add(local);
		List<SeatEntity> seatsEntities = world.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
		if (!seatsEntities.isEmpty()) {
			SeatEntity seat = seatsEntities.get(0);
			List<Entity> passengers = seat.getPassengers();
			if (!passengers.isEmpty())
				initialPassengers.put(local, passengers.get(0));
		}
	}

	private void movePulley(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited) {
		int limit = AllConfigs.server().kinetics.maxRopeLength.get();
		BlockPos ropePos = pos;
		while (limit-- >= 0) {
			ropePos = ropePos.below();
			if (!world.isLoaded(ropePos))
				break;
			BlockState ropeState = world.getBlockState(ropePos);
			Block block = ropeState.getBlock();
			if (!(block instanceof RopeBlock) && !(block instanceof MagnetBlock)) {
				if (!visited.contains(ropePos))
					frontier.add(ropePos);
				break;
			}
			addBlock(ropePos, capture(world, ropePos));
		}
	}

	private boolean moveMechanicalPiston(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) throws AssemblyException {
		Direction direction = state.getValue(MechanicalPistonBlock.FACING);
		PistonState pistonState = state.getValue(MechanicalPistonBlock.STATE);
		if (pistonState == PistonState.MOVING)
			return false;

		BlockPos offset = pos.relative(direction.getOpposite());
		if (!visited.contains(offset)) {
			BlockState poleState = world.getBlockState(offset);
			if (AllBlocks.PISTON_EXTENSION_POLE.has(poleState) && poleState.getValue(PistonExtensionPoleBlock.FACING)
				.getAxis() == direction.getAxis())
				frontier.add(offset);
		}

		if (pistonState == PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(state)) {
			offset = pos.relative(direction);
			if (!visited.contains(offset))
				frontier.add(offset);
		}

		return true;
	}

	private boolean moveChassis(Level world, BlockPos pos, Direction movementDirection, Queue<BlockPos> frontier,
		Set<BlockPos> visited) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof ChassisBlockEntity))
			return false;
		ChassisBlockEntity chassis = (ChassisBlockEntity) be;
		chassis.addAttachedChasses(frontier, visited);
		List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(movementDirection, false);
		if (includedBlockPositions == null)
			return false;
		for (BlockPos blockPos : includedBlockPositions)
			if (!visited.contains(blockPos))
				frontier.add(blockPos);
		return true;
	}

	protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
		BlockState blockstate = world.getBlockState(pos);
		if (AllBlocks.REDSTONE_CONTACT.has(blockstate))
			blockstate = blockstate.setValue(RedstoneContactBlock.POWERED, true);
		if (AllBlocks.POWERED_SHAFT.has(blockstate))
			blockstate = BlockHelper.copyProperties(blockstate, AllBlocks.SHAFT.getDefaultState());
		if (blockstate.getBlock() instanceof ControlsBlock)
			blockstate = blockstate.setValue(ControlsBlock.OPEN, true);
		if (blockstate.hasProperty(SlidingDoorBlock.VISIBLE))
			blockstate = blockstate.setValue(SlidingDoorBlock.VISIBLE, false);
		if (blockstate.getBlock() instanceof ButtonBlock) {
			blockstate = blockstate.setValue(ButtonBlock.POWERED, false);
			world.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		if (blockstate.getBlock() instanceof PressurePlateBlock) {
			blockstate = blockstate.setValue(PressurePlateBlock.POWERED, false);
			world.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		CompoundTag compoundnbt = getBlockEntityNBT(world, pos);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof PoweredShaftBlockEntity)
			blockEntity = AllBlockEntityTypes.BRACKETED_KINETIC.create(pos, blockstate);
		return Pair.of(new StructureBlockInfo(pos, blockstate, compoundnbt), blockEntity);
	}

	protected void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> pair) {
		StructureBlockInfo captured = pair.getKey();
		BlockPos localPos = pos.subtract(anchor);
		StructureBlockInfo structureBlockInfo = new StructureBlockInfo(localPos, captured.state, captured.nbt);

		if (blocks.put(localPos, structureBlockInfo) != null)
			return;
		bounds = bounds.minmax(new AABB(localPos));

		BlockEntity be = pair.getValue();
		storage.addBlock(localPos, be);

		if (AllMovementBehaviours.getBehaviour(captured.state) != null)
			actors.add(MutablePair.of(structureBlockInfo, null));

		MovingInteractionBehaviour interactionBehaviour = AllInteractionBehaviours.getBehaviour(captured.state);
		if (interactionBehaviour != null)
			interactors.put(localPos, interactionBehaviour);

		if (be instanceof CreativeCrateBlockEntity
			&& ((CreativeCrateBlockEntity) be).getBehaviour(FilteringBehaviour.TYPE)
				.getFilter()
				.isEmpty())
			hasUniversalCreativeCrate = true;
	}

	@Nullable
	protected CompoundTag getBlockEntityNBT(Level world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null)
			return null;
		CompoundTag nbt = blockEntity.saveWithFullMetadata();
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");

		if ((blockEntity instanceof FluidTankBlockEntity || blockEntity instanceof ItemVaultBlockEntity)
			&& nbt.contains("Controller"))
			nbt.put("Controller",
				NbtUtils.writeBlockPos(toLocalPos(NbtUtils.readBlockPos(nbt.getCompound("Controller")))));

		return nbt;
	}

	protected BlockPos toLocalPos(BlockPos globalPos) {
		return globalPos.subtract(anchor);
	}

	protected boolean movementAllowed(BlockState state, Level world, BlockPos pos) {
		return BlockMovementChecks.isMovementAllowed(state, world, pos);
	}

	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor);
	}

	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		blocks.clear();
		presentBlockEntities.clear();
		specialRenderedBlockEntities.clear();

		Tag blocks = nbt.get("Blocks");
		// used to differentiate between the 'old' and the paletted serialization
		boolean usePalettedDeserialization =
			blocks != null && blocks.getId() == 10 && ((CompoundTag) blocks).contains("Palette");
		readBlocksCompound(blocks, world, usePalettedDeserialization);

		actors.clear();
		nbt.getList("Actors", 10)
			.forEach(c -> {
				CompoundTag comp = (CompoundTag) c;
				StructureBlockInfo info = this.blocks.get(NbtUtils.readBlockPos(comp.getCompound("Pos")));
				if (info == null)
					return;
				MovementContext context = MovementContext.readNBT(world, info, comp, this);
				getActors().add(MutablePair.of(info, context));
			});

		disabledActors = NBTHelper.readItemList(nbt.getList("DisabledActors", Tag.TAG_COMPOUND));
		for (ItemStack stack : disabledActors)
			setActorsActive(stack, false);

		superglue.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Superglue", Tag.TAG_COMPOUND),
			c -> superglue.add(SuperGlueEntity.readBoundingBox(c)));

		seats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Seats", Tag.TAG_COMPOUND), c -> seats.add(NbtUtils.readBlockPos(c)));

		seatMapping.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Passengers", Tag.TAG_COMPOUND),
			c -> seatMapping.put(NbtUtils.loadUUID(NBTHelper.getINBT(c, "Id")), c.getInt("Seat")));

		stabilizedSubContraptions.clear();
		NBTHelper.iterateCompoundList(nbt.getList("SubContraptions", Tag.TAG_COMPOUND),
			c -> stabilizedSubContraptions.put(c.getUUID("Id"), BlockFace.fromNBT(c.getCompound("Location"))));

		interactors.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Interactors", Tag.TAG_COMPOUND), c -> {
			BlockPos pos = NbtUtils.readBlockPos(c.getCompound("Pos"));
			StructureBlockInfo structureBlockInfo = getBlocks().get(pos);
			if (structureBlockInfo == null)
				return;
			MovingInteractionBehaviour behaviour = AllInteractionBehaviours.getBehaviour(structureBlockInfo.state);
			if (behaviour != null)
				interactors.put(pos, behaviour);
		});

		storage.read(nbt, presentBlockEntities, spawnData);

		if (nbt.contains("BoundsFront"))
			bounds = NBTHelper.readAABB(nbt.getList("BoundsFront", 5));

		stalled = nbt.getBoolean("Stalled");
		hasUniversalCreativeCrate = nbt.getBoolean("BottomlessSupply");
		anchor = NbtUtils.readBlockPos(nbt.getCompound("Anchor"));
	}

	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", getType().id);

		CompoundTag blocksNBT = writeBlocksCompound();

		ListTag actorsNBT = new ListTag();
		for (MutablePair<StructureBlockInfo, MovementContext> actor : getActors()) {
			MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(actor.left.state);
			if (behaviour == null)
				continue;
			CompoundTag compound = new CompoundTag();
			compound.put("Pos", NbtUtils.writeBlockPos(actor.left.pos));
			behaviour.writeExtraData(actor.right);
			actor.right.writeToNBT(compound);
			actorsNBT.add(compound);
		}

		ListTag disabledActorsNBT = NBTHelper.writeItemList(disabledActors);

		ListTag superglueNBT = new ListTag();
		if (!spawnPacket) {
			for (AABB glueEntry : superglue) {
				CompoundTag c = new CompoundTag();
				SuperGlueEntity.writeBoundingBox(c, glueEntry);
				superglueNBT.add(c);
			}
		}

		(spawnPacket ? getStorageForSpawnPacket() : storage).write(nbt, spawnPacket);

		ListTag interactorNBT = new ListTag();
		for (BlockPos pos : interactors.keySet()) {
			CompoundTag c = new CompoundTag();
			c.put("Pos", NbtUtils.writeBlockPos(pos));
			interactorNBT.add(c);
		}

		nbt.put("Seats", NBTHelper.writeCompoundList(getSeats(), NbtUtils::writeBlockPos));
		nbt.put("Passengers", NBTHelper.writeCompoundList(getSeatMapping().entrySet(), e -> {
			CompoundTag tag = new CompoundTag();
			tag.put("Id", NbtUtils.createUUID(e.getKey()));
			tag.putInt("Seat", e.getValue());
			return tag;
		}));

		nbt.put("SubContraptions", NBTHelper.writeCompoundList(stabilizedSubContraptions.entrySet(), e -> {
			CompoundTag tag = new CompoundTag();
			tag.putUUID("Id", e.getKey());
			tag.put("Location", e.getValue()
				.serializeNBT());
			return tag;
		}));

		nbt.put("Blocks", blocksNBT);
		nbt.put("Actors", actorsNBT);
		nbt.put("DisabledActors", disabledActorsNBT);
		nbt.put("Interactors", interactorNBT);
		nbt.put("Superglue", superglueNBT);
		nbt.put("Anchor", NbtUtils.writeBlockPos(anchor));
		nbt.putBoolean("Stalled", stalled);
		nbt.putBoolean("BottomlessSupply", hasUniversalCreativeCrate);

		if (bounds != null) {
			ListTag bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
	}

	protected MountedStorageManager getStorageForSpawnPacket() {
		return storage;
	}

	private CompoundTag writeBlocksCompound() {
		CompoundTag compound = new CompoundTag();
		HashMapPalette<BlockState> palette = new HashMapPalette<>(GameData.getBlockStateIDMap(), 16, (i, s) -> {
			throw new IllegalStateException("Palette Map index exceeded maximum");
		});
		ListTag blockList = new ListTag();

		for (StructureBlockInfo block : this.blocks.values()) {
			int id = palette.idFor(block.state);
			CompoundTag c = new CompoundTag();
			c.putLong("Pos", block.pos.asLong());
			c.putInt("State", id);
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blockList.add(c);
		}

		ListTag paletteNBT = new ListTag();
		for (int i = 0; i < palette.getSize(); ++i)
			paletteNBT.add(NbtUtils.writeBlockState(palette.values.byId(i)));

		compound.put("Palette", paletteNBT);
		compound.put("BlockList", blockList);

		return compound;
	}

	private void readBlocksCompound(Tag compound, Level world, boolean usePalettedDeserialization) {
		HashMapPalette<BlockState> palette = null;
		ListTag blockList;
		if (usePalettedDeserialization) {
			CompoundTag c = ((CompoundTag) compound);
			palette = new HashMapPalette<>(GameData.getBlockStateIDMap(), 16, (i, s) -> {
				throw new IllegalStateException("Palette Map index exceeded maximum");
			});

			ListTag list = c.getList("Palette", 10);
			palette.values.clear();
			for (int i = 0; i < list.size(); ++i)
				palette.values.add(NbtUtils.readBlockState(list.getCompound(i)));

			blockList = c.getList("BlockList", 10);
		} else {
			blockList = (ListTag) compound;
		}

		HashMapPalette<BlockState> finalPalette = palette;
		blockList.forEach(e -> {
			CompoundTag c = (CompoundTag) e;

			StructureBlockInfo info =
				usePalettedDeserialization ? readStructureBlockInfo(c, finalPalette) : legacyReadStructureBlockInfo(c);

			this.blocks.put(info.pos, info);

			if (!world.isClientSide)
				return;

			CompoundTag tag = info.nbt;
			if (tag == null)
				return;

			tag.putInt("x", info.pos.getX());
			tag.putInt("y", info.pos.getY());
			tag.putInt("z", info.pos.getZ());

			BlockEntity be = BlockEntity.loadStatic(info.pos, info.state, tag);
			if (be == null)
				return;
			be.setLevel(world);
			modelData.put(info.pos, be.getModelData());
			if (be instanceof KineticBlockEntity kte)
				kte.setSpeed(0);
			be.getBlockState();

			MovementBehaviour movementBehaviour = AllMovementBehaviours.getBehaviour(info.state);
			if (movementBehaviour == null || !movementBehaviour.hasSpecialInstancedRendering())
				maybeInstancedBlockEntities.add(be);

			if (movementBehaviour != null && !movementBehaviour.renderAsNormalBlockEntity())
				return;

			presentBlockEntities.put(info.pos, be);
			specialRenderedBlockEntities.add(be);
		});
	}

	private static StructureBlockInfo readStructureBlockInfo(CompoundTag blockListEntry,
		HashMapPalette<BlockState> palette) {
		return new StructureBlockInfo(BlockPos.of(blockListEntry.getLong("Pos")),
			Objects.requireNonNull(palette.valueFor(blockListEntry.getInt("State"))),
			blockListEntry.contains("Data") ? blockListEntry.getCompound("Data") : null);
	}

	private static StructureBlockInfo legacyReadStructureBlockInfo(CompoundTag blockListEntry) {
		return new StructureBlockInfo(NbtUtils.readBlockPos(blockListEntry.getCompound("Pos")),
			NbtUtils.readBlockState(blockListEntry.getCompound("Block")),
			blockListEntry.contains("Data") ? blockListEntry.getCompound("Data") : null);
	}

	public void removeBlocksFromWorld(Level world, BlockPos offset) {
		storage.removeStorageFromWorld();

		glueToRemove.forEach(glue -> {
			superglue.add(glue.getBoundingBox()
				.move(Vec3.atLowerCornerOf(offset.offset(anchor))
					.scale(-1)));
			glue.discard();
		});

		List<BoundingBox> minimisedGlue = new ArrayList<>();
		for (int i = 0; i < superglue.size(); i++)
			minimisedGlue.add(null);

		for (boolean brittles : Iterate.trueAndFalse) {
			for (Iterator<StructureBlockInfo> iterator = blocks.values()
				.iterator(); iterator.hasNext();) {
				StructureBlockInfo block = iterator.next();
				if (brittles != BlockMovementChecks.isBrittle(block.state))
					continue;

				for (int i = 0; i < superglue.size(); i++) {
					AABB aabb = superglue.get(i);
					if (aabb == null
						|| !aabb.contains(block.pos.getX() + .5, block.pos.getY() + .5, block.pos.getZ() + .5))
						continue;
					if (minimisedGlue.get(i) == null)
						minimisedGlue.set(i, new BoundingBox(block.pos));
					else
						minimisedGlue.set(i, BBHelper.encapsulate(minimisedGlue.get(i), block.pos));
				}

				BlockPos add = block.pos.offset(anchor)
					.offset(offset);
				if (customBlockRemoval(world, add, block.state))
					continue;
				BlockState oldState = world.getBlockState(add);
				Block blockIn = oldState.getBlock();
				boolean blockMismatch = block.state.getBlock() != blockIn;
				blockMismatch &= !AllBlocks.POWERED_SHAFT.is(blockIn) || !AllBlocks.SHAFT.has(block.state);
				if (blockMismatch)
					iterator.remove();
				world.removeBlockEntity(add);
				int flags = Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE
					| Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;
				if (blockIn instanceof SimpleWaterloggedBlock && oldState.hasProperty(BlockStateProperties.WATERLOGGED)
					&& oldState.getValue(BlockStateProperties.WATERLOGGED)) {
					world.setBlock(add, Blocks.WATER.defaultBlockState(), flags);
					continue;
				}
				world.setBlock(add, Blocks.AIR.defaultBlockState(), flags);
			}
		}

		superglue.clear();
		for (BoundingBox box : minimisedGlue) {
			if (box == null)
				continue;
			AABB bb = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
			if (bb.getSize() > 1.01)
				superglue.add(bb);
		}

		for (StructureBlockInfo block : blocks.values()) {
			BlockPos add = block.pos.offset(anchor)
				.offset(offset);
//			if (!shouldUpdateAfterMovement(block))
//				continue;

			int flags = Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL;
			world.sendBlockUpdated(add, block.state, Blocks.AIR.defaultBlockState(), flags);

			// when the blockstate is set to air, the block's POI data is removed, but
			// markAndNotifyBlock tries to
			// remove it again, so to prevent an error from being logged by double-removal
			// we add the POI data back now
			// (code copied from ServerWorld.onBlockStateChange)
			ServerLevel serverWorld = (ServerLevel) world;
			PoiType.forState(block.state)
				.ifPresent(poiType -> {
					world.getServer()
						.execute(() -> {
							serverWorld.getPoiManager()
								.add(add, poiType);
							DebugPackets.sendPoiAddedPacket(serverWorld, add);
						});
				});

			world.markAndNotifyBlock(add, world.getChunkAt(add), block.state, Blocks.AIR.defaultBlockState(), flags,
				512);
			block.state.updateIndirectNeighbourShapes(world, add, flags & -2);
		}
	}

	public void addBlocksToWorld(Level world, StructureTransform transform) {
		if (disassembled)
			return;
		disassembled = true;

		for (boolean nonBrittles : Iterate.trueAndFalse) {
			for (StructureBlockInfo block : blocks.values()) {
				if (nonBrittles == BlockMovementChecks.isBrittle(block.state))
					continue;

				BlockPos targetPos = transform.apply(block.pos);
				BlockState state = transform.apply(block.state);

				if (customBlockPlacement(world, targetPos, state))
					continue;

				if (nonBrittles)
					for (Direction face : Iterate.directions)
						state = state.updateShape(face, world.getBlockState(targetPos.relative(face)), world, targetPos,
							targetPos.relative(face));

				BlockState blockState = world.getBlockState(targetPos);
				if (blockState.getDestroySpeed(world, targetPos) == -1 || (state.getCollisionShape(world, targetPos)
					.isEmpty()
					&& !blockState.getCollisionShape(world, targetPos)
						.isEmpty())) {
					if (targetPos.getY() == world.getMinBuildHeight())
						targetPos = targetPos.above();
					world.levelEvent(2001, targetPos, Block.getId(state));
					Block.dropResources(state, world, targetPos, null);
					continue;
				}
				if (state.getBlock() instanceof SimpleWaterloggedBlock
					&& state.hasProperty(BlockStateProperties.WATERLOGGED)) {
					FluidState FluidState = world.getFluidState(targetPos);
					state = state.setValue(BlockStateProperties.WATERLOGGED, FluidState.getType() == Fluids.WATER);
				}

				world.destroyBlock(targetPos, true);

				if (AllBlocks.SHAFT.has(state))
					state = ShaftBlock.pickCorrectShaftType(state, world, targetPos);
				if (state.hasProperty(SlidingDoorBlock.VISIBLE))
					state = state.setValue(SlidingDoorBlock.VISIBLE, !state.getValue(SlidingDoorBlock.OPEN))
						.setValue(SlidingDoorBlock.POWERED, false);

				world.setBlock(targetPos, state, Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL);

				boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
				verticalRotation = verticalRotation && transform.rotation != Rotation.NONE;
				if (verticalRotation) {
					if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock
						|| state.getBlock() instanceof DoorBlock)
						world.destroyBlock(targetPos, true);
				}

				BlockEntity blockEntity = world.getBlockEntity(targetPos);

				CompoundTag tag = block.nbt;
				if (blockEntity != null)
					tag = NBTProcessors.process(blockEntity, tag, false);
				if (blockEntity != null && tag != null) {
					tag.putInt("x", targetPos.getX());
					tag.putInt("y", targetPos.getY());
					tag.putInt("z", targetPos.getZ());

					if (verticalRotation && blockEntity instanceof PulleyBlockEntity) {
						tag.remove("Offset");
						tag.remove("InitialOffset");
					}

					if (blockEntity instanceof IMultiBlockEntityContainer && tag.contains("LastKnownPos"))
						tag.put("LastKnownPos", NbtUtils.writeBlockPos(BlockPos.ZERO.below(Integer.MAX_VALUE - 1)));

					blockEntity.load(tag);
					storage.addStorageToWorld(block, blockEntity);
				}

				transform.apply(blockEntity);
			}
		}

		for (StructureBlockInfo block : blocks.values()) {
			if (!shouldUpdateAfterMovement(block))
				continue;
			BlockPos targetPos = transform.apply(block.pos);
			world.markAndNotifyBlock(targetPos, world.getChunkAt(targetPos), block.state, block.state,
				Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL, 512);
		}

		for (AABB box : superglue) {
			box = new AABB(transform.apply(new Vec3(box.minX, box.minY, box.minZ)),
				transform.apply(new Vec3(box.maxX, box.maxY, box.maxZ)));
			if (!world.isClientSide)
				world.addFreshEntity(new SuperGlueEntity(world, box));
		}

		storage.clear();
	}

	public void addPassengersToWorld(Level world, StructureTransform transform, List<Entity> seatedEntities) {
		for (Entity seatedEntity : seatedEntities) {
			if (getSeatMapping().isEmpty())
				continue;
			Integer seatIndex = getSeatMapping().get(seatedEntity.getUUID());
			if (seatIndex == null)
				continue;
			BlockPos seatPos = getSeats().get(seatIndex);
			seatPos = transform.apply(seatPos);
			if (!(world.getBlockState(seatPos)
				.getBlock() instanceof SeatBlock))
				continue;
			if (SeatBlock.isSeatOccupied(world, seatPos))
				continue;
			SeatBlock.sitDown(world, seatPos, seatedEntity);
		}
	}

	public void startMoving(Level world) {
		disabledActors.clear();

		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
			MovementContext context = new MovementContext(world, pair.left, this);
			MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(pair.left.state);
			if (behaviour != null)
				behaviour.startMoving(context);
			pair.setRight(context);
			if (behaviour instanceof ContraptionControlsMovement) 
				disableActorOnStart(context);
		}

		for (ItemStack stack : disabledActors)
			setActorsActive(stack, false);
	}
	
	protected void disableActorOnStart(MovementContext context) {
		if (!ContraptionControlsMovement.isDisabledInitially(context))
			return;
		ItemStack filter = ContraptionControlsMovement.getFilter(context);
		if (filter == null)
			return;
		if (isActorTypeDisabled(filter))
			return;
		disabledActors.add(filter);
	}

	public boolean isActorTypeDisabled(ItemStack filter) {
		return disabledActors.stream()
			.anyMatch(i -> ContraptionControlsMovement.isSameFilter(i, filter));
	}

	public void setActorsActive(ItemStack referenceStack, boolean enable) {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
			MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(pair.left.state);
			if (behaviour == null)
				continue;
			ItemStack behaviourStack = behaviour.canBeDisabledVia(pair.right);
			if (behaviourStack == null)
				continue;
			if (!referenceStack.isEmpty() && !ContraptionControlsMovement.isSameFilter(referenceStack, behaviourStack))
				continue;
			pair.right.disabled = !enable;
			if (!enable)
				behaviour.onDisabledByControls(pair.right);
		}
	}

	public List<ItemStack> getDisabledActors() {
		return disabledActors;
	}

	public void stop(Level world) {
		forEachActor(world, (behaviour, ctx) -> {
			behaviour.stopMoving(ctx);
			ctx.position = null;
			ctx.motion = Vec3.ZERO;
			ctx.relativeMotion = Vec3.ZERO;
			ctx.rotation = v -> v;
		});
	}

	public void forEachActor(Level world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
			MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(pair.getLeft().state);
			if (behaviour == null)
				continue;
			callBack.accept(behaviour, pair.getRight());
		}
	}

	protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
		if (PoiType.forState(info.state)
			.isPresent())
			return false;
		if (info.state.getBlock() instanceof SlidingDoorBlock)
			return false;
		return true;
	}

	public void expandBoundsAroundAxis(Axis axis) {
		Set<BlockPos> blocks = getBlocks().keySet();

		int radius = (int) (Math.ceil(Math.sqrt(getRadius(blocks, axis))));

		int maxX = radius + 2;
		int maxY = radius + 2;
		int maxZ = radius + 2;
		int minX = -radius - 1;
		int minY = -radius - 1;
		int minZ = -radius - 1;

		if (axis == Direction.Axis.X) {
			maxX = (int) bounds.maxX;
			minX = (int) bounds.minX;
		} else if (axis == Direction.Axis.Y) {
			maxY = (int) bounds.maxY;
			minY = (int) bounds.minY;
		} else if (axis == Direction.Axis.Z) {
			maxZ = (int) bounds.maxZ;
			minZ = (int) bounds.minZ;
		}

		bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public Map<UUID, Integer> getSeatMapping() {
		return seatMapping;
	}

	public BlockPos getSeatOf(UUID entityId) {
		if (!getSeatMapping().containsKey(entityId))
			return null;
		int seatIndex = getSeatMapping().get(entityId);
		if (seatIndex >= getSeats().size())
			return null;
		return getSeats().get(seatIndex);
	}

	public BlockPos getBearingPosOf(UUID subContraptionEntityId) {
		if (stabilizedSubContraptions.containsKey(subContraptionEntityId))
			return stabilizedSubContraptions.get(subContraptionEntityId)
				.getConnectedPos();
		return null;
	}

	public void setSeatMapping(Map<UUID, Integer> seatMapping) {
		this.seatMapping = seatMapping;
	}

	public List<BlockPos> getSeats() {
		return seats;
	}

	public Map<BlockPos, StructureBlockInfo> getBlocks() {
		return blocks;
	}

	public List<MutablePair<StructureBlockInfo, MovementContext>> getActors() {
		return actors;
	}

	@Nullable
	public MutablePair<StructureBlockInfo, MovementContext> getActorAt(BlockPos localPos) {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
			if (localPos.equals(pair.left.pos))
				return pair;
		return null;
	}

	public Map<BlockPos, MovingInteractionBehaviour> getInteractors() {
		return interactors;
	}

	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		// TODO: move lighters to registry
		return new EmptyLighter(this);
	}

	public void invalidateColliders() {
		simplifiedEntityColliders = Optional.empty();
		gatherBBsOffThread();
	}

	private void gatherBBsOffThread() {
		getContraptionWorld();
		simplifiedEntityColliderProvider = CompletableFuture.supplyAsync(() -> {
			VoxelShape combinedShape = Shapes.empty();
			for (Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
				StructureBlockInfo info = entry.getValue();
				BlockPos localPos = entry.getKey();
				VoxelShape collisionShape = info.state.getCollisionShape(world, localPos, CollisionContext.empty());
				if (collisionShape.isEmpty())
					continue;
				combinedShape = Shapes.joinUnoptimized(combinedShape,
					collisionShape.move(localPos.getX(), localPos.getY(), localPos.getZ()), BooleanOp.OR);
			}
			return combinedShape.optimize()
				.toAabbs();
		})
			.thenAccept(r -> {
				simplifiedEntityColliders = Optional.of(r);
				simplifiedEntityColliderProvider = null;
			});
	}

	public static float getRadius(Set<BlockPos> blocks, Direction.Axis axis) {
		switch (axis) {
		case X:
			return getMaxDistSqr(blocks, BlockPos::getY, BlockPos::getZ);
		case Y:
			return getMaxDistSqr(blocks, BlockPos::getX, BlockPos::getZ);
		case Z:
			return getMaxDistSqr(blocks, BlockPos::getX, BlockPos::getY);
		}

		throw new IllegalStateException("Impossible axis");
	}

	public static float getMaxDistSqr(Set<BlockPos> blocks, ICoordinate one, ICoordinate other) {
		float maxDistSq = -1;
		for (BlockPos pos : blocks) {
			float a = one.get(pos);
			float b = other.get(pos);

			float distSq = a * a + b * b;

			if (distSq > maxDistSq)
				maxDistSq = distSq;
		}

		return maxDistSq;
	}

	public IItemHandlerModifiable getSharedInventory() {
		return storage.getItems();
	}

	public IItemHandlerModifiable getSharedFuelInventory() {
		return storage.getFuelItems();
	}

	public IFluidHandler getSharedFluidTanks() {
		return storage.getFluids();
	}

	public Collection<StructureBlockInfo> getRenderedBlocks() {
		return blocks.values();
	}

	public Collection<BlockEntity> getSpecialRenderedTEs() {
		return specialRenderedBlockEntities;
	}

	public boolean isHiddenInPortal(BlockPos localPos) {
		return false;
	}

	public Optional<List<AABB>> getSimplifiedEntityColliders() {
		return simplifiedEntityColliders;
	}

	public void handleContraptionFluidPacket(BlockPos localPos, FluidStack containedFluid) {
		storage.updateContainedFluid(localPos, containedFluid);
	}

	public static class ContraptionInvWrapper extends CombinedInvWrapper {
		protected final boolean isExternal;

		public ContraptionInvWrapper(boolean isExternal, IItemHandlerModifiable... itemHandler) {
			super(itemHandler);
			this.isExternal = isExternal;
		}

		public ContraptionInvWrapper(IItemHandlerModifiable... itemHandler) {
			this(false, itemHandler);
		}

		public boolean isSlotExternal(int slot) {
			if (isExternal)
				return true;
			IItemHandlerModifiable handler = getHandlerFromIndex(getIndexForSlot(slot));
			return handler instanceof ContraptionInvWrapper && ((ContraptionInvWrapper) handler).isSlotExternal(slot);
		}
	}

	public void tickStorage(AbstractContraptionEntity entity) {
		storage.entityTick(entity);
	}

	public boolean containsBlockBreakers() {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
			MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(pair.getLeft().state);
			if (behaviour instanceof BlockBreakingMovementBehaviour || behaviour instanceof HarvesterMovementBehaviour)
				return true;
		}
		return false;
	}

}
