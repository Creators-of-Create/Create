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

import com.jozufozu.flywheel.backend.IFlywheelWorld;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.simibubi.create.AllBlocks;
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
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.create.content.logistics.block.inventories.CreativeCrateTileEntity;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.ICoordinate;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.NBTProcessors;
import com.simibubi.create.foundation.utility.UniqueLinkedList;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.palette.HashMapPalette;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.GameData;

public abstract class Contraption {

	public Optional<List<AxisAlignedBB>> simplifiedEntityColliders;
	public AbstractContraptionEntity entity;
	public ContraptionInvWrapper inventory;
	public CombinedTankWrapper fluidInventory;
	public AxisAlignedBB bounds;
	public BlockPos anchor;
	public boolean stalled;
	public boolean hasUniversalCreativeCrate;

	protected Map<BlockPos, BlockInfo> blocks;
	protected Map<BlockPos, MountedStorage> storage;
	protected Map<BlockPos, MountedFluidStorage> fluidStorage;
	protected List<MutablePair<BlockInfo, MovementContext>> actors;
	protected Set<Pair<BlockPos, Direction>> superglue;
	protected List<BlockPos> seats;
	protected Map<UUID, Integer> seatMapping;
	protected Map<UUID, BlockFace> stabilizedSubContraptions;

	private List<SuperGlueEntity> glueToRemove;
	private Map<BlockPos, Entity> initialPassengers;
	private List<BlockFace> pendingSubContraptions;

	private CompletableFuture<Void> simplifiedEntityColliderProvider;

	// Client
	public Map<BlockPos, TileEntity> presentTileEntities;
	public List<TileEntity> maybeInstancedTileEntities;
	public List<TileEntity> specialRenderedTileEntities;

	protected ContraptionWorld world;

	public Contraption() {
		blocks = new HashMap<>();
		storage = new HashMap<>();
		seats = new ArrayList<>();
		actors = new ArrayList<>();
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

	public abstract boolean assemble(World world, BlockPos pos) throws AssemblyException;

	public abstract boolean canBeStabilized(Direction facing, BlockPos localPos);

	protected abstract ContraptionType getType();

	protected boolean customBlockPlacement(IWorld world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean customBlockRemoval(IWorld world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction forcedDirection,
		Queue<BlockPos> frontier) throws AssemblyException {
		return true;
	}

	public static Contraption fromNBT(World world, CompoundNBT nbt, boolean spawnData) {
		String type = nbt.getString("Type");
		Contraption contraption = ContraptionType.fromType(type);
		contraption.readNBT(world, nbt, spawnData);
		contraption.world = new ContraptionWorld(world, contraption);
		contraption.gatherBBsOffThread();
		return contraption;
	}

	public boolean searchMovedStructure(World world, BlockPos pos, @Nullable Direction forcedDirection)
		throws AssemblyException {
		initialPassengers.clear();
		Queue<BlockPos> frontier = new UniqueLinkedList<>();
		Set<BlockPos> visited = new HashSet<>();
		anchor = pos;

		if (bounds == null)
			bounds = new AxisAlignedBB(BlockPos.ZERO);

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
			World world = entity.level;
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
		inventory = new ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));

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

	public void onEntityInitialize(World world, AbstractContraptionEntity contraptionEntity) {
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

	public void onEntityTick(World world) {
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.isClientSide));
	}

	/** move the first block in frontier queue */
	protected boolean moveBlock(World world, @Nullable Direction forcedDirection, Queue<BlockPos> frontier,
		Set<BlockPos> visited) throws AssemblyException {
		BlockPos pos = frontier.poll();
		if (pos == null)
			return false;
		visited.add(pos);

		if (World.isOutsideBuildHeight(pos))
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

		if (AllBlocks.ADJUSTABLE_CRATE.has(state))
			AdjustableCrateBlock.splitCrate(world, pos);

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
			if (faceHasGlue)
				addGlue(superglue.get(offset));
		}

		addBlock(pos, capture(world, pos));
		if (blocks.size() <= AllConfigs.SERVER.kinetics.maxBlocksMoved.get())
			return true;
		else
			throw AssemblyException.structureTooLarge();
	}

	protected void movePistonHead(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
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
				if (pistonFacing == direction && blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
					frontier.add(offset);
			}
		}
		if (state.getValue(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
			BlockPos attached = pos.relative(direction);
			if (!visited.contains(attached))
				frontier.add(attached);
		}
	}

	protected void movePistonPole(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
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

	protected void moveGantryPinion(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
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

	protected void moveGantryShaft(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		for (Direction d : Iterate.directions) {
			BlockPos offset = pos.relative(d);
			if (!visited.contains(offset)) {
				BlockState offsetState = world.getBlockState(offset);
				Direction facing = state.getValue(GantryShaftBlock.FACING);
				if (d.getAxis() == facing.getAxis() && AllBlocks.GANTRY_SHAFT.has(offsetState)
					&& offsetState.getValue(GantryShaftBlock.FACING) == facing)
					frontier.add(offset);
				else if (AllBlocks.GANTRY_CARRIAGE.has(offsetState) && offsetState.getValue(GantryCarriageBlock.FACING) == d)
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

	private void moveSeat(World world, BlockPos pos) {
		BlockPos local = toLocalPos(pos);
		getSeats().add(local);
		List<SeatEntity> seatsEntities = world.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos));
		if (!seatsEntities.isEmpty()) {
			SeatEntity seat = seatsEntities.get(0);
			List<Entity> passengers = seat.getPassengers();
			if (!passengers.isEmpty())
				initialPassengers.put(local, passengers.get(0));
		}
	}

	private void movePulley(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited) {
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

	private boolean moveMechanicalPiston(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited,
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

	private boolean moveChassis(World world, BlockPos pos, Direction movementDirection, Queue<BlockPos> frontier,
		Set<BlockPos> visited) {
		TileEntity te = world.getBlockEntity(pos);
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

	protected Pair<BlockInfo, TileEntity> capture(World world, BlockPos pos) {
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.getBlock() instanceof ChestBlock)
			blockstate = blockstate.setValue(ChestBlock.TYPE, ChestType.SINGLE);
		if (AllBlocks.ADJUSTABLE_CRATE.has(blockstate))
			blockstate = blockstate.setValue(AdjustableCrateBlock.DOUBLE, false);
		if (AllBlocks.REDSTONE_CONTACT.has(blockstate))
			blockstate = blockstate.setValue(RedstoneContactBlock.POWERED, true);
		if (blockstate.getBlock() instanceof AbstractButtonBlock) {
			blockstate = blockstate.setValue(AbstractButtonBlock.POWERED, false);
			world.getBlockTicks()
				.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		if (blockstate.getBlock() instanceof PressurePlateBlock) {
			blockstate = blockstate.setValue(PressurePlateBlock.POWERED, false);
			world.getBlockTicks()
				.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		CompoundNBT compoundnbt = getTileEntityNBT(world, pos);
		TileEntity tileentity = world.getBlockEntity(pos);
		return Pair.of(new BlockInfo(pos, blockstate, compoundnbt), tileentity);
	}

	protected void addBlock(BlockPos pos, Pair<BlockInfo, TileEntity> pair) {
		BlockInfo captured = pair.getKey();
		BlockPos localPos = pos.subtract(anchor);
		BlockInfo blockInfo = new BlockInfo(localPos, captured.state, captured.nbt);

		if (blocks.put(localPos, blockInfo) != null)
			return;
		bounds = bounds.minmax(new AxisAlignedBB(localPos));

		TileEntity te = pair.getValue();
		if (te != null && MountedStorage.canUseAsStorage(te))
			storage.put(localPos, new MountedStorage(te));
		if (te != null && MountedFluidStorage.canUseAsStorage(te))
			fluidStorage.put(localPos, new MountedFluidStorage(te));
		if (AllMovementBehaviours.contains(captured.state.getBlock()))
			actors.add(MutablePair.of(blockInfo, null));
		if (te instanceof CreativeCrateTileEntity
			&& ((CreativeCrateTileEntity) te).getBehaviour(FilteringBehaviour.TYPE)
				.getFilter()
				.isEmpty())
			hasUniversalCreativeCrate = true;
	}

	@Nullable
	protected CompoundNBT getTileEntityNBT(World world, BlockPos pos) {
		TileEntity tileentity = world.getBlockEntity(pos);
		if (tileentity == null)
			return null;
		CompoundNBT nbt = tileentity.save(new CompoundNBT());
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");

		if (tileentity instanceof FluidTankTileEntity && nbt.contains("Controller"))
			nbt.put("Controller",
				NBTUtil.writeBlockPos(toLocalPos(NBTUtil.readBlockPos(nbt.getCompound("Controller")))));

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

	protected boolean movementAllowed(BlockState state, World world, BlockPos pos) {
		return BlockMovementChecks.isMovementAllowed(state, world, pos);
	}

	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor);
	}

	public void readNBT(World world, CompoundNBT nbt, boolean spawnData) {
		blocks.clear();
		presentTileEntities.clear();
		specialRenderedTileEntities.clear();

		INBT blocks = nbt.get("Blocks");
		// used to differentiate between the 'old' and the paletted serialization
		boolean usePalettedDeserialization =
			blocks != null && blocks.getId() == 10 && ((CompoundNBT) blocks).contains("Palette");
		readBlocksCompound(blocks, world, usePalettedDeserialization);

		actors.clear();
		nbt.getList("Actors", 10)
			.forEach(c -> {
				CompoundNBT comp = (CompoundNBT) c;
				BlockInfo info = this.blocks.get(NBTUtil.readBlockPos(comp.getCompound("Pos")));
				MovementContext context = MovementContext.readNBT(world, info, comp, this);
				getActors().add(MutablePair.of(info, context));
			});

		superglue.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Superglue", NBT.TAG_COMPOUND), c -> superglue
			.add(Pair.of(NBTUtil.readBlockPos(c.getCompound("Pos")), Direction.from3DDataValue(c.getByte("Direction")))));

		seats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Seats", NBT.TAG_COMPOUND), c -> seats.add(NBTUtil.readBlockPos(c)));

		seatMapping.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Passengers", NBT.TAG_COMPOUND),
			c -> seatMapping.put(NBTUtil.loadUUID(NBTHelper.getINBT(c, "Id")), c.getInt("Seat")));

		stabilizedSubContraptions.clear();
		NBTHelper.iterateCompoundList(nbt.getList("SubContraptions", NBT.TAG_COMPOUND),
			c -> stabilizedSubContraptions.put(c.getUUID("Id"), BlockFace.fromNBT(c.getCompound("Location"))));

		storage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Storage", NBT.TAG_COMPOUND), c -> storage
			.put(NBTUtil.readBlockPos(c.getCompound("Pos")), MountedStorage.deserialize(c.getCompound("Data"))));

		fluidStorage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", NBT.TAG_COMPOUND), c -> fluidStorage
			.put(NBTUtil.readBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		if (spawnData)
			fluidStorage.forEach((pos, mfs) -> {
				TileEntity tileEntity = presentTileEntities.get(pos);
				if (!(tileEntity instanceof FluidTankTileEntity))
					return;
				FluidTankTileEntity tank = (FluidTankTileEntity) tileEntity;
				IFluidTank tankInventory = tank.getTankInventory();
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
		anchor = NBTUtil.readBlockPos(nbt.getCompound("Anchor"));
	}

	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Type", getType().id);

		CompoundNBT blocksNBT = writeBlocksCompound();

		ListNBT actorsNBT = new ListNBT();
		for (MutablePair<BlockInfo, MovementContext> actor : getActors()) {
			CompoundNBT compound = new CompoundNBT();
			compound.put("Pos", NBTUtil.writeBlockPos(actor.left.pos));
			AllMovementBehaviours.of(actor.left.state)
				.writeExtraData(actor.right);
			actor.right.writeToNBT(compound);
			actorsNBT.add(compound);
		}

		ListNBT superglueNBT = new ListNBT();
		ListNBT storageNBT = new ListNBT();
		if (!spawnPacket) {
			for (Pair<BlockPos, Direction> glueEntry : superglue) {
				CompoundNBT c = new CompoundNBT();
				c.put("Pos", NBTUtil.writeBlockPos(glueEntry.getKey()));
				c.putByte("Direction", (byte) glueEntry.getValue()
					.get3DDataValue());
				superglueNBT.add(c);
			}

			for (BlockPos pos : storage.keySet()) {
				CompoundNBT c = new CompoundNBT();
				MountedStorage mountedStorage = storage.get(pos);
				if (!mountedStorage.isValid())
					continue;
				c.put("Pos", NBTUtil.writeBlockPos(pos));
				c.put("Data", mountedStorage.serialize());
				storageNBT.add(c);
			}
		}

		ListNBT fluidStorageNBT = new ListNBT();
		for (BlockPos pos : fluidStorage.keySet()) {
			CompoundNBT c = new CompoundNBT();
			MountedFluidStorage mountedStorage = fluidStorage.get(pos);
			if (!mountedStorage.isValid())
				continue;
			c.put("Pos", NBTUtil.writeBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			fluidStorageNBT.add(c);
		}

		nbt.put("Seats", NBTHelper.writeCompoundList(getSeats(), NBTUtil::writeBlockPos));
		nbt.put("Passengers", NBTHelper.writeCompoundList(getSeatMapping().entrySet(), e -> {
			CompoundNBT tag = new CompoundNBT();
			tag.put("Id", NBTUtil.createUUID(e.getKey()));
			tag.putInt("Seat", e.getValue());
			return tag;
		}));

		nbt.put("SubContraptions", NBTHelper.writeCompoundList(stabilizedSubContraptions.entrySet(), e -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putUUID("Id", e.getKey());
			tag.put("Location", e.getValue()
				.serializeNBT());
			return tag;
		}));

		nbt.put("Blocks", blocksNBT);
		nbt.put("Actors", actorsNBT);
		nbt.put("Superglue", superglueNBT);
		nbt.put("Storage", storageNBT);
		nbt.put("FluidStorage", fluidStorageNBT);
		nbt.put("Anchor", NBTUtil.writeBlockPos(anchor));
		nbt.putBoolean("Stalled", stalled);
		nbt.putBoolean("BottomlessSupply", hasUniversalCreativeCrate);

		if (bounds != null) {
			ListNBT bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
	}

	private CompoundNBT writeBlocksCompound() {
		CompoundNBT compound = new CompoundNBT();
		HashMapPalette<BlockState> palette = new HashMapPalette<>(GameData.getBlockStateIDMap(), 16, (i, s) -> {
			throw new IllegalStateException("Palette Map index exceeded maximum");
		}, NBTUtil::readBlockState, NBTUtil::writeBlockState);
		ListNBT blockList = new ListNBT();

		for (BlockInfo block : this.blocks.values()) {
			int id = palette.idFor(block.state);
			CompoundNBT c = new CompoundNBT();
			c.putLong("Pos", block.pos.asLong());
			c.putInt("State", id);
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blockList.add(c);
		}

		ListNBT paletteNBT = new ListNBT();
		palette.write(paletteNBT);
		compound.put("Palette", paletteNBT);
		compound.put("BlockList", blockList);

		return compound;
	}

	private void readBlocksCompound(INBT compound, World world, boolean usePalettedDeserialization) {
		HashMapPalette<BlockState> palette = null;
		ListNBT blockList;
		if (usePalettedDeserialization) {
			CompoundNBT c = ((CompoundNBT) compound);
			palette = new HashMapPalette<>(GameData.getBlockStateIDMap(), 16, (i, s) -> {
				throw new IllegalStateException("Palette Map index exceeded maximum");
			}, NBTUtil::readBlockState, NBTUtil::writeBlockState);
			palette.read(c.getList("Palette", 10));

			blockList = c.getList("BlockList", 10);
		} else {
			blockList = (ListNBT) compound;
		}

		HashMapPalette<BlockState> finalPalette = palette;
		blockList.forEach(e -> {
			CompoundNBT c = (CompoundNBT) e;

			BlockInfo info = usePalettedDeserialization ? readBlockInfo(c, finalPalette) : legacyReadBlockInfo(c);

			this.blocks.put(info.pos, info);

			if (world.isClientSide) {
				Block block = info.state.getBlock();
				CompoundNBT tag = info.nbt;
				MovementBehaviour movementBehaviour = AllMovementBehaviours.of(block);
				if (tag == null)
					return;

				tag.putInt("x", info.pos.getX());
				tag.putInt("y", info.pos.getY());
				tag.putInt("z", info.pos.getZ());

				TileEntity te = TileEntity.loadStatic(info.state, tag);
				if (te == null)
					return;
				te.setLevelAndPosition(new ContraptionTileWorld(world, te, info), te.getBlockPos());
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

	private static BlockInfo readBlockInfo(CompoundNBT blockListEntry, HashMapPalette<BlockState> palette) {
		return new BlockInfo(BlockPos.of(blockListEntry.getLong("Pos")),
			Objects.requireNonNull(palette.valueFor(blockListEntry.getInt("State"))),
			blockListEntry.contains("Data") ? blockListEntry.getCompound("Data") : null);
	}

	private static BlockInfo legacyReadBlockInfo(CompoundNBT blockListEntry) {
		return new BlockInfo(NBTUtil.readBlockPos(blockListEntry.getCompound("Pos")),
			NBTUtil.readBlockState(blockListEntry.getCompound("Block")),
			blockListEntry.contains("Data") ? blockListEntry.getCompound("Data") : null);
	}

	public void removeBlocksFromWorld(World world, BlockPos offset) {
		storage.values()
			.forEach(MountedStorage::removeStorageFromWorld);
		fluidStorage.values()
			.forEach(MountedFluidStorage::removeStorageFromWorld);
		glueToRemove.forEach(SuperGlueEntity::remove);

		for (boolean brittles : Iterate.trueAndFalse) {
			for (Iterator<BlockInfo> iterator = blocks.values()
				.iterator(); iterator.hasNext();) {
				BlockInfo block = iterator.next();
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
				int flags = BlockFlags.IS_MOVING | BlockFlags.NO_NEIGHBOR_DROPS | BlockFlags.UPDATE_NEIGHBORS
					| BlockFlags.BLOCK_UPDATE | BlockFlags.RERENDER_MAIN_THREAD;
				if (blockIn instanceof IWaterLoggable && oldState.hasProperty(BlockStateProperties.WATERLOGGED)
					&& oldState.getValue(BlockStateProperties.WATERLOGGED)) {
					world.setBlock(add, Blocks.WATER.defaultBlockState(), flags);
					continue;
				}
				world.setBlock(add, Blocks.AIR.defaultBlockState(), flags);
			}
		}
		for (BlockInfo block : blocks.values()) {
			BlockPos add = block.pos.offset(anchor)
				.offset(offset);
//			if (!shouldUpdateAfterMovement(block))
//				continue;

			int flags = BlockFlags.IS_MOVING | BlockFlags.DEFAULT;
			world.sendBlockUpdated(add, block.state, Blocks.AIR.defaultBlockState(), flags);

			// when the blockstate is set to air, the block's POI data is removed, but markAndNotifyBlock tries to
			// remove it again, so to prevent an error from being logged by double-removal we add the POI data back now
			// (code copied from ServerWorld.onBlockStateChange)
			ServerWorld serverWorld = (ServerWorld) world;
			PointOfInterestType.forState(block.state).ifPresent(poiType -> {
				world.getServer().execute(() -> {
					serverWorld.getPoiManager().add(add, poiType);
					DebugPacketSender.sendPoiAddedPacket(serverWorld, add);
				});
			});

			world.markAndNotifyBlock(add, world.getChunkAt(add), block.state, Blocks.AIR.defaultBlockState(), flags, 512);
			block.state.updateIndirectNeighbourShapes(world, add, flags & -2);
		}
	}

	public void addBlocksToWorld(World world, StructureTransform transform) {
		for (boolean nonBrittles : Iterate.trueAndFalse) {
			for (BlockInfo block : blocks.values()) {
				if (nonBrittles == BlockMovementChecks.isBrittle(block.state))
					continue;

				BlockPos targetPos = transform.apply(block.pos);
				BlockState state = transform.apply(block.state);

				if (customBlockPlacement(world, targetPos, state))
					continue;

				if (nonBrittles)
					for (Direction face : Iterate.directions)
						state = state.updateShape(face, world.getBlockState(targetPos.relative(face)), world,
							targetPos, targetPos.relative(face));

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
				if (state.getBlock() instanceof IWaterLoggable && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
					FluidState FluidState = world.getFluidState(targetPos);
					state = state.setValue(BlockStateProperties.WATERLOGGED, FluidState.getType() == Fluids.WATER);
				}

				world.destroyBlock(targetPos, true);
				world.setBlock(targetPos, state, 3 | BlockFlags.IS_MOVING);

				boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
				verticalRotation = verticalRotation && transform.rotation != Rotation.NONE;
				if (verticalRotation) {
					if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock)
						world.destroyBlock(targetPos, true);
				}

				TileEntity tileEntity = world.getBlockEntity(targetPos);
				CompoundNBT tag = block.nbt;
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

					if (tileEntity instanceof FluidTankTileEntity && tag.contains("LastKnownPos"))
						tag.put("LastKnownPos", NBTUtil.writeBlockPos(BlockPos.ZERO.below()));

					tileEntity.load(block.state, tag);

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
		for (BlockInfo block : blocks.values()) {
			if (!shouldUpdateAfterMovement(block))
				continue;
			BlockPos targetPos = transform.apply(block.pos);
			world.markAndNotifyBlock(targetPos, world.getChunkAt(targetPos), block.state, block.state,
				BlockFlags.IS_MOVING | BlockFlags.DEFAULT, 512);
		}

		for (int i = 0; i < inventory.getSlots(); i++) {
			if (!inventory.isSlotExternal(i))
				inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
		for (int i = 0; i < fluidInventory.getTanks(); i++)
			fluidInventory.drain(fluidInventory.getFluidInTank(i), FluidAction.EXECUTE);

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

	public void addPassengersToWorld(World world, StructureTransform transform, List<Entity> seatedEntities) {
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

	public void startMoving(World world) {
		for (MutablePair<BlockInfo, MovementContext> pair : actors) {
			MovementContext context = new MovementContext(world, pair.left, this);
			AllMovementBehaviours.of(pair.left.state)
				.startMoving(context);
			pair.setRight(context);
		}
	}

	public void stop(World world) {
		foreachActor(world, (behaviour, ctx) -> {
			behaviour.stopMoving(ctx);
			ctx.position = null;
			ctx.motion = Vector3d.ZERO;
			ctx.relativeMotion = Vector3d.ZERO;
			ctx.rotation = v -> v;
		});
	}

	public void foreachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<BlockInfo, MovementContext> pair : actors)
			callBack.accept(AllMovementBehaviours.of(pair.getLeft().state), pair.getRight());
	}

	protected boolean shouldUpdateAfterMovement(BlockInfo info) {
		if (PointOfInterestType.forState(info.state)
			.isPresent())
			return false;
		return true;
	}

	public void expandBoundsAroundAxis(Axis axis) {
		Set<BlockPos> blocks = getBlocks().keySet();

		int radius = (int) (Math.ceil(Math.sqrt(getRadius(blocks, axis))));

		GridAlignedBB betterBounds = GridAlignedBB.ofRadius(radius);

		GridAlignedBB contraptionBounds = GridAlignedBB.from(bounds);
		if (axis == Direction.Axis.X) {
			betterBounds.maxX = contraptionBounds.maxX;
			betterBounds.minX = contraptionBounds.minX;
		} else if (axis == Direction.Axis.Y) {
			betterBounds.maxY = contraptionBounds.maxY;
			betterBounds.minY = contraptionBounds.minY;
		} else if (axis == Direction.Axis.Z) {
			betterBounds.maxZ = contraptionBounds.maxZ;
			betterBounds.minZ = contraptionBounds.minZ;
		}

		bounds = betterBounds.toAABB();
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

	public Map<BlockPos, BlockInfo> getBlocks() {
		return blocks;
	}

	public List<MutablePair<BlockInfo, MovementContext>> getActors() {
		return actors;
	}

	public void updateContainedFluid(BlockPos localPos, FluidStack containedFluid) {
		MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
		if (mountedFluidStorage != null)
			mountedFluidStorage.updateFluid(containedFluid);
	}

	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new EmptyLighter(this);
	}

	private void gatherBBsOffThread() {
		getContraptionWorld();
		simplifiedEntityColliderProvider = CompletableFuture.supplyAsync(() -> {
			VoxelShape combinedShape = VoxelShapes.empty();
			for (Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
				BlockInfo info = entry.getValue();
				BlockPos localPos = entry.getKey();
				VoxelShape collisionShape = info.state.getCollisionShape(world, localPos);
				if (collisionShape.isEmpty())
					continue;
				combinedShape = VoxelShapes.joinUnoptimized(combinedShape,
					collisionShape.move(localPos.getX(), localPos.getY(), localPos.getZ()), IBooleanFunction.OR);
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

	private static class ContraptionTileWorld extends WrappedWorld implements IFlywheelWorld {

		private final TileEntity te;
		private final BlockInfo info;

		public ContraptionTileWorld(World world, TileEntity te, BlockInfo info) {
			super(world);
			this.te = te;
			this.info = info;
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			if (!pos.equals(te.getBlockPos()))
				return Blocks.AIR.defaultBlockState();
			return info.state;
		}

		@Override
		public boolean isLoaded(BlockPos pos) {
			return pos.equals(te.getBlockPos());
		}
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
}
