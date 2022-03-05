package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryCarriageBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.MagnetBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.RopeBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.render.EmptyLighter;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.content.logistics.block.vault.ItemVaultTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.tileEntity.IMultiTileContainer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.ICoordinate;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.NBTProcessors;
import com.simibubi.create.foundation.utility.UniqueLinkedList;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.HashMapPaletteAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.CombinedInvWrapper;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandlerModifiable;
import io.github.fabricators_of_create.porting_lib.util.LevelUtil;
import io.github.fabricators_of_create.porting_lib.util.StickinessUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.IdMapper;
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
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Contraption {

	public Optional<List<AABB>> simplifiedEntityColliders;
	public AbstractContraptionEntity entity;
	public ContraptionInvWrapper inventory;
	public CombinedTankWrapper fluidInventory;
	public AABB bounds;
	public BlockPos anchor;
	public boolean stalled;
	public boolean hasUniversalCreativeCrate;

	protected Map<BlockPos, StructureBlockInfo> blocks;
	protected Map<BlockPos, MountedStorage> storage;
	protected Map<BlockPos, MountedFluidStorage> fluidStorage;
	protected List<MutablePair<StructureBlockInfo, MovementContext>> actors;
	protected Map<BlockPos, MovingInteractionBehaviour> interactors;
	protected Set<Pair<BlockPos, Direction>> superglue;
	protected List<BlockPos> seats;
	protected Map<UUID, Integer> seatMapping;
	protected Map<UUID, BlockFace> stabilizedSubContraptions;

	private List<SuperGlueEntity> glueToRemove;
	private Map<BlockPos, Entity> initialPassengers;
	private List<BlockFace> pendingSubContraptions;

	private CompletableFuture<Void> simplifiedEntityColliderProvider;

	// Client
	public Map<BlockPos, BlockEntity> presentTileEntities;
	public List<BlockEntity> maybeInstancedTileEntities;
	public List<BlockEntity> specialRenderedTileEntities;

	protected ContraptionWorld world;

	public Contraption() {
		blocks = new HashMap<>();
		storage = new HashMap<>();
		seats = new ArrayList<>();
		actors = new ArrayList<>();
		interactors = new HashMap<>();
		superglue = new HashSet<>();
		seatMapping = new HashMap<>();
		fluidStorage = new HashMap<>();
		glueToRemove = new ArrayList<>();
		initialPassengers = new HashMap<>();
		presentTileEntities = new HashMap<>();
		maybeInstancedTileEntities = new ArrayList<>();
		specialRenderedTileEntities = new ArrayList<>();
		pendingSubContraptions = new ArrayList<>();
		stabilizedSubContraptions = new HashMap<>();
		simplifiedEntityColliders = Optional.empty();
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

		// Gather itemhandlers of mounted storage
		List<IItemHandlerModifiable> list = storage.values()
			.stream()
			.map(MountedStorage::getItemHandler)
			.collect(Collectors.toList());
		inventory =
			new ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));

		List<IFluidHandler> fluidHandlers = fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.collect(Collectors.toList());
		fluidInventory = new CombinedTankWrapper(
			Arrays.copyOf(fluidHandlers.toArray(), fluidHandlers.size(), IFluidHandler[].class));
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

	public void onEntityTick(Level world) {
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.isClientSide));
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

		Map<Direction, SuperGlueEntity> superglue = SuperGlueHandler.gatherGlue(world, pos);

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
			boolean faceHasGlue = superglue.containsKey(offset);
			boolean blockAttachedTowardsFace =
				BlockMovementChecks.isBlockAttachedTowards(blockState, world, offsetPos, offset.getOpposite());
			boolean brittle = BlockMovementChecks.isBrittle(blockState);
			boolean canStick = !brittle && StickinessUtil.canStickTo(state, blockState) && StickinessUtil.canStickTo(blockState, state);
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
			if (faceHasGlue)
				addGlue(superglue.get(offset));
		}

		addBlock(pos, capture(world, pos));
		if (blocks.size() <= AllConfigs.SERVER.kinetics.maxBlocksMoved.get())
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
		int limit = AllConfigs.SERVER.kinetics.maxRopeLength.get();
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
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof ChassisTileEntity))
			return false;
		ChassisTileEntity chassis = (ChassisTileEntity) te;
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
		if (blockstate.getBlock() instanceof ButtonBlock) {
			blockstate = blockstate.setValue(ButtonBlock.POWERED, false);
			world.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		if (blockstate.getBlock() instanceof PressurePlateBlock) {
			blockstate = blockstate.setValue(PressurePlateBlock.POWERED, false);
			world.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		CompoundTag compoundnbt = getTileEntityNBT(world, pos);
		BlockEntity tileentity = world.getBlockEntity(pos);
		return Pair.of(new StructureBlockInfo(pos, blockstate, compoundnbt), tileentity);
	}

	protected void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> pair) {
		StructureBlockInfo captured = pair.getKey();
		BlockPos localPos = pos.subtract(anchor);
		StructureBlockInfo StructureBlockInfo = new StructureBlockInfo(localPos, captured.state, captured.nbt);

		if (blocks.put(localPos, StructureBlockInfo) != null)
			return;
		bounds = bounds.minmax(new AABB(localPos));

		BlockEntity te = pair.getValue();
		if (te != null && MountedStorage.canUseAsStorage(te))
			storage.put(localPos, new MountedStorage(te));
		if (te != null && MountedFluidStorage.canUseAsStorage(te))
			fluidStorage.put(localPos, new MountedFluidStorage(te));
		if (AllMovementBehaviours.contains(captured.state.getBlock()))
			actors.add(MutablePair.of(StructureBlockInfo, null));
		if (AllInteractionBehaviours.contains(captured.state.getBlock()))
			interactors.put(localPos, AllInteractionBehaviours.of(captured.state.getBlock()));
		if (te instanceof CreativeCrateTileEntity
			&& ((CreativeCrateTileEntity) te).getBehaviour(FilteringBehaviour.TYPE)
				.getFilter()
				.isEmpty())
			hasUniversalCreativeCrate = true;
	}

	@Nullable
	protected CompoundTag getTileEntityNBT(Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity == null)
			return null;
		CompoundTag nbt = tileentity.saveWithFullMetadata();
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");

		if ((tileentity instanceof FluidTankTileEntity || tileentity instanceof ItemVaultTileEntity)
			&& nbt.contains("Controller"))
			nbt.put("Controller",
				NbtUtils.writeBlockPos(toLocalPos(NbtUtils.readBlockPos(nbt.getCompound("Controller")))));

		return nbt;
	}

	protected void addGlue(SuperGlueEntity entity) {
		BlockPos pos = entity.getHangingPosition();
		Direction direction = entity.getFacingDirection();
		this.superglue.add(Pair.of(toLocalPos(pos), direction));
		glueToRemove.add(entity);
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
		presentTileEntities.clear();
		specialRenderedTileEntities.clear();

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
				MovementContext context = MovementContext.readNBT(world, info, comp, this);
				getActors().add(MutablePair.of(info, context));
			});

		superglue.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Superglue", Tag.TAG_COMPOUND), c -> superglue.add(
			Pair.of(NbtUtils.readBlockPos(c.getCompound("Pos")), Direction.from3DDataValue(c.getByte("Direction")))));

		seats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Seats", Tag.TAG_COMPOUND), c -> seats.add(NbtUtils.readBlockPos(c)));

		seatMapping.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Passengers", Tag.TAG_COMPOUND),
			c -> seatMapping.put(NbtUtils.loadUUID(NBTHelper.getINBT(c, "Id")), c.getInt("Seat")));

		stabilizedSubContraptions.clear();
		NBTHelper.iterateCompoundList(nbt.getList("SubContraptions", Tag.TAG_COMPOUND),
			c -> stabilizedSubContraptions.put(c.getUUID("Id"), BlockFace.fromNBT(c.getCompound("Location"))));

		storage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Storage", Tag.TAG_COMPOUND), c -> storage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedStorage.deserialize(c.getCompound("Data"))));

		fluidStorage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), c -> fluidStorage
			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		interactors.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Interactors", Tag.TAG_COMPOUND), c -> {
			BlockPos pos = NbtUtils.readBlockPos(c.getCompound("Pos"));
			MovingInteractionBehaviour behaviour = AllInteractionBehaviours.of(getBlocks().get(pos).state.getBlock());
			if (behaviour != null)
				interactors.put(pos, behaviour);
		});

		if (spawnData)
			fluidStorage.forEach((pos, mfs) -> {
				BlockEntity tileEntity = presentTileEntities.get(pos);
				if (!(tileEntity instanceof FluidTankTileEntity))
					return;
				FluidTankTileEntity tank = (FluidTankTileEntity) tileEntity;
				FluidTank tankInventory = tank.getTankInventory();
				if (tankInventory instanceof FluidTank)
					((FluidTank) tankInventory).setFluid(mfs.tank.getFluid());
				tank.getFluidLevel()
					.start(tank.getFillState());
				mfs.assignTileEntity(tank);
			});

		IItemHandlerModifiable[] handlers = new IItemHandlerModifiable[storage.size()];
		int index = 0;
		for (MountedStorage mountedStorage : storage.values())
			handlers[index++] = mountedStorage.getItemHandler();

		IFluidHandler[] fluidHandlers = new IFluidHandler[fluidStorage.size()];
		index = 0;
		for (MountedFluidStorage mountedStorage : fluidStorage.values())
			fluidHandlers[index++] = mountedStorage.getFluidHandler();

		inventory = new ContraptionInvWrapper(handlers);
		fluidInventory = new CombinedTankWrapper(fluidHandlers);

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
			CompoundTag compound = new CompoundTag();
			compound.put("Pos", NbtUtils.writeBlockPos(actor.left.pos));
			AllMovementBehaviours.of(actor.left.state)
				.writeExtraData(actor.right);
			actor.right.writeToNBT(compound);
			actorsNBT.add(compound);
		}

		ListTag superglueNBT = new ListTag();
		ListTag storageNBT = new ListTag();
		if (!spawnPacket) {
			for (Pair<BlockPos, Direction> glueEntry : superglue) {
				CompoundTag c = new CompoundTag();
				c.put("Pos", NbtUtils.writeBlockPos(glueEntry.getKey()));
				c.putByte("Direction", (byte) glueEntry.getValue()
					.get3DDataValue());
				superglueNBT.add(c);
			}

			for (BlockPos pos : storage.keySet()) {
				CompoundTag c = new CompoundTag();
				MountedStorage mountedStorage = storage.get(pos);
				if (!mountedStorage.isValid())
					continue;
				c.put("Pos", NbtUtils.writeBlockPos(pos));
				c.put("Data", mountedStorage.serialize());
				storageNBT.add(c);
			}
		}

		ListTag fluidStorageNBT = new ListTag();
		for (BlockPos pos : fluidStorage.keySet()) {
			CompoundTag c = new CompoundTag();
			MountedFluidStorage mountedStorage = fluidStorage.get(pos);
			if (!mountedStorage.isValid())
				continue;
			c.put("Pos", NbtUtils.writeBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			fluidStorageNBT.add(c);
		}

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
		nbt.put("Interactors", interactorNBT);
		nbt.put("Superglue", superglueNBT);
		nbt.put("Storage", storageNBT);
		nbt.put("FluidStorage", fluidStorageNBT);
		nbt.put("Anchor", NbtUtils.writeBlockPos(anchor));
		nbt.putBoolean("Stalled", stalled);
		nbt.putBoolean("BottomlessSupply", hasUniversalCreativeCrate);

		if (bounds != null) {
			ListTag bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
	}

	private CompoundTag writeBlocksCompound() {
		CompoundTag compound = new CompoundTag();
		HashMapPalette<BlockState> palette = new HashMapPalette<>(new IdMapper<>(), 16, (i, s) -> {
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
		for(int i = 0; i < palette.getSize(); ++i)
			paletteNBT.add(NbtUtils.writeBlockState(((HashMapPaletteAccessor<BlockState>)palette).create$getValues().byId(i)));
		compound.put("Palette", paletteNBT);
		compound.put("BlockList", blockList);

		return compound;
	}

	private void readBlocksCompound(Tag compound, Level world, boolean usePalettedDeserialization) {
		HashMapPalette<BlockState> palette = null;
		ListTag blockList;
		if (usePalettedDeserialization) {
			CompoundTag c = ((CompoundTag) compound);
			palette = new HashMapPalette<>(new IdMapper<>(), 16, (i, s) -> {
				throw new IllegalStateException("Palette Map index exceeded maximum");
			});

			ListTag list = c.getList("Palette", 10);
			((HashMapPaletteAccessor)palette).create$getValues().clear();
			for (int i = 0; i < list.size(); ++i)
				((HashMapPaletteAccessor)palette).create$getValues().add(NbtUtils.readBlockState(list.getCompound(i)));

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

			if (world.isClientSide) {
				Block block = info.state.getBlock();
				CompoundTag tag = info.nbt;
				MovementBehaviour movementBehaviour = AllMovementBehaviours.of(block);
				if (tag == null)
					return;

				tag.putInt("x", info.pos.getX());
				tag.putInt("y", info.pos.getY());
				tag.putInt("z", info.pos.getZ());

				BlockEntity te = BlockEntity.loadStatic(info.pos, info.state, tag);
				if (te == null)
					return;
				te.setLevel(world);
				if (te instanceof KineticTileEntity)
					((KineticTileEntity) te).setSpeed(0);
				te.getBlockState();

				if (movementBehaviour == null || !movementBehaviour.hasSpecialInstancedRendering())
					maybeInstancedTileEntities.add(te);

				if (movementBehaviour != null && !movementBehaviour.renderAsNormalTileEntity())
					return;

				presentTileEntities.put(info.pos, te);
				specialRenderedTileEntities.add(te);
			}

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
		storage.values()
			.forEach(MountedStorage::removeStorageFromWorld);
		fluidStorage.values()
			.forEach(MountedFluidStorage::removeStorageFromWorld);
		glueToRemove.forEach(SuperGlueEntity::discard);

		for (boolean brittles : Iterate.trueAndFalse) {
			for (Iterator<StructureBlockInfo> iterator = blocks.values()
				.iterator(); iterator.hasNext();) {
				StructureBlockInfo block = iterator.next();
				if (brittles != BlockMovementChecks.isBrittle(block.state))
					continue;

				BlockPos add = block.pos.offset(anchor)
					.offset(offset);
				if (customBlockRemoval(world, add, block.state))
					continue;
				BlockState oldState = world.getBlockState(add);
				Block blockIn = oldState.getBlock();
				if (block.state.getBlock() != blockIn)
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

			LevelUtil.markAndNotifyBlock(world, add, world.getChunkAt(add), block.state, Blocks.AIR.defaultBlockState(), flags,
					512);
			block.state.updateIndirectNeighbourShapes(world, add, flags & -2);
		}
	}

	public void addBlocksToWorld(Level world, StructureTransform transform) {
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
					if (targetPos.getY() == 0)
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
				world.setBlock(targetPos, state, Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL);

				boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
				verticalRotation = verticalRotation && transform.rotation != Rotation.NONE;
				if (verticalRotation) {
					if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock)
						world.destroyBlock(targetPos, true);
				}

				BlockEntity tileEntity = world.getBlockEntity(targetPos);
				CompoundTag tag = block.nbt;
				if (tileEntity != null)
					tag = NBTProcessors.process(tileEntity, tag, false);
				if (tileEntity != null && tag != null) {
					tag.putInt("x", targetPos.getX());
					tag.putInt("y", targetPos.getY());
					tag.putInt("z", targetPos.getZ());

					if (verticalRotation && tileEntity instanceof PulleyTileEntity) {
						tag.remove("Offset");
						tag.remove("InitialOffset");
					}

					if (tileEntity instanceof IMultiTileContainer && tag.contains("LastKnownPos"))
						tag.put("LastKnownPos", NbtUtils.writeBlockPos(BlockPos.ZERO.below(Integer.MAX_VALUE - 1)));

					tileEntity.load(tag);

					if (storage.containsKey(block.pos)) {
						MountedStorage mountedStorage = storage.get(block.pos);
						if (mountedStorage.isValid())
							mountedStorage.addStorageToWorld(tileEntity);
					}

					if (fluidStorage.containsKey(block.pos)) {
						MountedFluidStorage mountedStorage = fluidStorage.get(block.pos);
						if (mountedStorage.isValid())
							mountedStorage.addStorageToWorld(tileEntity);
					}
				}

				transform.apply(tileEntity);
			}
		}
		for (StructureBlockInfo block : blocks.values()) {
			if (!shouldUpdateAfterMovement(block))
				continue;
			BlockPos targetPos = transform.apply(block.pos);
			LevelUtil.markAndNotifyBlock(world, targetPos, world.getChunkAt(targetPos), block.state, block.state,
					Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL, 512);
		}

		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.isSlotExternal(i))
				inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
		for (int i = 0; i < fluidInventory.getTanks(); i++)
			fluidInventory.drain(fluidInventory.getFluidInTank(i), false);

		for (Pair<BlockPos, Direction> pair : superglue) {
			BlockPos targetPos = transform.apply(pair.getKey());
			Direction targetFacing = transform.transformFacing(pair.getValue());

			SuperGlueEntity entity = new SuperGlueEntity(world, targetPos, targetFacing);
			if (entity.onValidSurface()) {
				if (!world.isClientSide)
					world.addFreshEntity(entity);
			}
		}
	}

	public void addPassengersToWorld(Level world, StructureTransform transform, List<Entity> seatedEntities) {
		for (Entity seatedEntity : seatedEntities) {
			if (getSeatMapping().isEmpty())
				continue;
			Integer seatIndex = getSeatMapping().get(seatedEntity.getUUID());
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
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
			MovementContext context = new MovementContext(world, pair.left, this);
			AllMovementBehaviours.of(pair.left.state)
				.startMoving(context);
			pair.setRight(context);
		}
	}

	public void stop(Level world) {
		foreachActor(world, (behaviour, ctx) -> {
			behaviour.stopMoving(ctx);
			ctx.position = null;
			ctx.motion = Vec3.ZERO;
			ctx.relativeMotion = Vec3.ZERO;
			ctx.rotation = v -> v;
		});
	}

	public void foreachActor(Level world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
			callBack.accept(AllMovementBehaviours.of(pair.getLeft().state), pair.getRight());
	}

	protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
		if (PoiType.forState(info.state)
			.isPresent())
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

	public void addExtraInventories(Entity entity) {}

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

	public Map<BlockPos, MovingInteractionBehaviour> getInteractors() {
		return interactors;
	}

	public void updateContainedFluid(BlockPos localPos, FluidStack containedFluid) {
		MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
		if (mountedFluidStorage != null)
			mountedFluidStorage.updateFluid(containedFluid);
	}

	@Environment(EnvType.CLIENT)
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
				VoxelShape collisionShape = info.state.getCollisionShape(world, localPos);
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

	// TODO: unused?
//	private static class ContraptionTileWorld extends WrappedWorld implements IFlywheelWorld {
//
//		private final BlockEntity te;
//		private final StructureBlockInfo info;
//
//		public ContraptionTileWorld(Level world, BlockEntity te, StructureBlockInfo info) {
//			super(world);
//			this.te = te;
//			this.info = info;
//		}
//
//		@Override
//		public BlockState getBlockState(BlockPos pos) {
//			if (!pos.equals(te.getBlockPos()))
//				return Blocks.AIR.defaultBlockState();
//			return info.state;
//		}
//
//		@Override
//		public boolean isLoaded(BlockPos pos) {
//			return pos.equals(te.getBlockPos());
//		}
//	}

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
}
