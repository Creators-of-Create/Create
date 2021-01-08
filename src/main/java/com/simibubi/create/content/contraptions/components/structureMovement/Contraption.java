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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.render.FastContraptionRenderer;
import com.simibubi.create.foundation.utility.render.FastKineticRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.MagnetBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock.RopeBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.create.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public abstract class Contraption {

	public AbstractContraptionEntity entity;
	public CombinedInvWrapper inventory;
	public CombinedTankWrapper fluidInventory;
	public AxisAlignedBB bounds;
	public BlockPos anchor;
	public boolean stalled;

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

	// Client
	public Map<BlockPos, TileEntity> presentTileEntities;
	public List<TileEntity> renderedTileEntities;

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
		renderedTileEntities = new ArrayList<>();
		pendingSubContraptions = new ArrayList<>();
		stabilizedSubContraptions = new HashMap<>();
	}

	public abstract boolean assemble(World world, BlockPos pos);

	protected abstract boolean canAxisBeStabilized(Axis axis);

	protected abstract AllContraptionTypes getType();

	protected boolean customBlockPlacement(IWorld world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean customBlockRemoval(IWorld world, BlockPos pos, BlockState state) {
		return false;
	}

	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction forcedDirection,
		List<BlockPos> frontier) {
		return true;
	}

	public static Contraption fromNBT(World world, CompoundNBT nbt, boolean spawnData) {
		String type = nbt.getString("Type");
		Contraption contraption = AllContraptionTypes.fromType(type);
		contraption.readNBT(world, nbt, spawnData);
		return contraption;
	}

	public boolean searchMovedStructure(World world, BlockPos pos, @Nullable Direction forcedDirection) {
		initialPassengers.clear();
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		anchor = pos;

		if (bounds == null)
			bounds = new AxisAlignedBB(BlockPos.ZERO);

		if (!BlockMovementTraits.isBrittle(world.getBlockState(pos)))
			frontier.add(pos);
		if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
			return false;
		for (int limit = 100000; limit > 0; limit--) {
			if (frontier.isEmpty())
				return true;
			if (!moveBlock(world, frontier.remove(0), forcedDirection, frontier, visited))
				return false;
		}
		return false;
	}

	public void onEntityCreated(AbstractContraptionEntity entity) {
		this.entity = entity;

		// Create subcontraptions
		for (BlockFace blockFace : pendingSubContraptions) {
			Direction face = blockFace.getFace();
			StabilizedContraption subContraption = new StabilizedContraption(face);
			World world = entity.world;
			BlockPos pos = blockFace.getPos();
			if (!subContraption.assemble(world, pos))
				continue;
			subContraption.removeBlocksFromWorld(world, BlockPos.ZERO);
			OrientedContraptionEntity movedContraption =
				OrientedContraptionEntity.create(world, subContraption, Optional.of(face));
			BlockPos anchor = blockFace.getConnectedPos();
			movedContraption.setPosition(anchor.getX() + .5f, anchor.getY(), anchor.getZ() + .5f);
			world.addEntity(movedContraption);
			stabilizedSubContraptions.put(movedContraption.getUniqueID(), new BlockFace(toLocalPos(pos), face));
		}

		// Gather itemhandlers of mounted storage
		List<IItemHandlerModifiable> list = storage.values()
			.stream()
			.map(MountedStorage::getItemHandler)
			.collect(Collectors.toList());
		inventory = new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));

		List<IFluidHandler> fluidHandlers = fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.collect(Collectors.toList());
		fluidInventory = new CombinedTankWrapper(
			Arrays.copyOf(fluidHandlers.toArray(), fluidHandlers.size(), IFluidHandler[].class));
	}

	public void onEntityInitialize(World world, AbstractContraptionEntity contraptionEntity) {
		if (world.isRemote)
			return;

		for (OrientedContraptionEntity orientedCE : world.getEntitiesWithinAABB(OrientedContraptionEntity.class,
			contraptionEntity.getBoundingBox()
				.grow(1)))
			if (stabilizedSubContraptions.containsKey(orientedCE.getUniqueID()))
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
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.isRemote));
	}

	protected boolean moveBlock(World world, BlockPos pos, Direction forcedDirection, List<BlockPos> frontier,
		Set<BlockPos> visited) {
		visited.add(pos);
		frontier.remove(pos);

		if (!world.isBlockPresent(pos))
			return false;
		if (isAnchoringBlockAt(pos))
			return true;
		if (!BlockMovementTraits.movementNecessary(world, pos))
			return true;
		if (!movementAllowed(world, pos))
			return false;
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof AbstractChassisBlock
			&& !moveChassis(world, pos, forcedDirection, frontier, visited))
			return false;

		if (AllBlocks.ADJUSTABLE_CRATE.has(state))
			AdjustableCrateBlock.splitCrate(world, pos);

		if (AllBlocks.BELT.has(state))
			moveBelt(pos, frontier, visited, state);

		// Bearings potentially create stabilized sub-contraptions
		if (AllBlocks.MECHANICAL_BEARING.has(state))
			moveBearing(pos, frontier, visited, state);

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

		// Doors try to stay whole
		if (state.getBlock() instanceof DoorBlock) {
			BlockPos otherPartPos = pos.up(state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? 1 : -1);
			if (!visited.contains(otherPartPos))
				frontier.add(otherPartPos);
		}

		// Cart assemblers attach themselves
		BlockState stateBelow = world.getBlockState(pos.down());
		if (!visited.contains(pos.down()) && AllBlocks.CART_ASSEMBLER.has(stateBelow))
			frontier.add(pos.down());

		Map<Direction, SuperGlueEntity> superglue = SuperGlueHandler.gatherGlue(world, pos);

		// Slime blocks and super glue drag adjacent blocks if possible
		boolean isStickyBlock = state.isStickyBlock();
		for (Direction offset : Iterate.directions) {
			BlockPos offsetPos = pos.offset(offset);
			BlockState blockState = world.getBlockState(offsetPos);
			if (isAnchoringBlockAt(offsetPos))
				continue;
			if (!movementAllowed(world, offsetPos)) {
				if (offset == forcedDirection && isStickyBlock)
					return false;
				continue;
			}

			boolean wasVisited = visited.contains(offsetPos);
			boolean faceHasGlue = superglue.containsKey(offset);
			boolean blockAttachedTowardsFace =
				BlockMovementTraits.isBlockAttachedTowards(world, offsetPos, blockState, offset.getOpposite());
			boolean brittle = BlockMovementTraits.isBrittle(blockState);

			if (!wasVisited && ((isStickyBlock && !brittle) || blockAttachedTowardsFace || faceHasGlue))
				frontier.add(offsetPos);
			if (faceHasGlue)
				addGlue(superglue.get(offset));
		}

		addBlock(pos, capture(world, pos));
		return blocks.size() <= AllConfigs.SERVER.kinetics.maxBlocksMoved.get();
	}

	private void moveBearing(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
		Direction facing = state.get(MechanicalBearingBlock.FACING);
		if (!canAxisBeStabilized(facing.getAxis())) {
			BlockPos offset = pos.offset(facing);
			if (!visited.contains(offset))
				frontier.add(offset);
			return;
		}
		pendingSubContraptions.add(new BlockFace(pos, facing));
	}

	private void moveBelt(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
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
		List<SeatEntity> seatsEntities = world.getEntitiesWithinAABB(SeatEntity.class, new AxisAlignedBB(pos));
		if (!seatsEntities.isEmpty()) {
			SeatEntity seat = seatsEntities.get(0);
			List<Entity> passengers = seat.getPassengers();
			if (!passengers.isEmpty())
				initialPassengers.put(local, passengers.get(0));
		}
	}

	private void movePulley(World world, BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited) {
		int limit = AllConfigs.SERVER.kinetics.maxRopeLength.get();
		BlockPos ropePos = pos;
		while (limit-- >= 0) {
			ropePos = ropePos.down();
			if (!world.isBlockPresent(ropePos))
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

	private boolean moveMechanicalPiston(World world, BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited,
		BlockState state) {
		int limit = AllConfigs.SERVER.kinetics.maxPistonPoles.get();
		Direction direction = state.get(MechanicalPistonBlock.FACING);
		if (state.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			BlockPos searchPos = pos;
			while (limit-- >= 0) {
				searchPos = searchPos.offset(direction);
				BlockState blockState = world.getBlockState(searchPos);
				if (isExtensionPole(blockState)) {
					if (blockState.get(PistonExtensionPoleBlock.FACING)
						.getAxis() != direction.getAxis())
						break;
					if (!visited.contains(searchPos))
						frontier.add(searchPos);
					continue;
				}
				if (isPistonHead(blockState))
					if (!visited.contains(searchPos))
						frontier.add(searchPos);
				break;
			}
			if (limit <= -1)
				return false;
		}

		BlockPos searchPos = pos;
		while (limit-- >= 0) {
			searchPos = searchPos.offset(direction.getOpposite());
			BlockState blockState = world.getBlockState(searchPos);
			if (isExtensionPole(blockState)) {
				if (blockState.get(PistonExtensionPoleBlock.FACING)
					.getAxis() != direction.getAxis())
					break;
				if (!visited.contains(searchPos))
					frontier.add(searchPos);
				continue;
			}
			break;
		}

		if (limit <= -1)
			return false;
		return true;
	}

	private boolean moveChassis(World world, BlockPos pos, Direction movementDirection, List<BlockPos> frontier,
		Set<BlockPos> visited) {
		TileEntity te = world.getTileEntity(pos);
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
			blockstate = blockstate.with(ChestBlock.TYPE, ChestType.SINGLE);
		if (AllBlocks.ADJUSTABLE_CRATE.has(blockstate))
			blockstate = blockstate.with(AdjustableCrateBlock.DOUBLE, false);
		if (AllBlocks.REDSTONE_CONTACT.has(blockstate))
			blockstate = blockstate.with(RedstoneContactBlock.POWERED, true);
		if (blockstate.getBlock() instanceof AbstractButtonBlock) {
			blockstate = blockstate.with(AbstractButtonBlock.POWERED, false);
			world.getPendingBlockTicks()
				.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		if (blockstate.getBlock() instanceof PressurePlateBlock) {
			blockstate = blockstate.with(PressurePlateBlock.POWERED, false);
			world.getPendingBlockTicks()
				.scheduleTick(pos, blockstate.getBlock(), -1);
		}
		CompoundNBT compoundnbt = getTileEntityNBT(world, pos);
		TileEntity tileentity = world.getTileEntity(pos);
		return Pair.of(new BlockInfo(pos, blockstate, compoundnbt), tileentity);
	}

	protected void addBlock(BlockPos pos, Pair<BlockInfo, TileEntity> pair) {
		BlockInfo captured = pair.getKey();
		BlockPos localPos = pos.subtract(anchor);
		BlockInfo blockInfo = new BlockInfo(localPos, captured.state, captured.nbt);

		if (blocks.put(localPos, blockInfo) != null)
			return;
		bounds = bounds.union(new AxisAlignedBB(localPos));

		TileEntity te = pair.getValue();
		if (te != null && MountedStorage.canUseAsStorage(te))
			storage.put(localPos, new MountedStorage(te));
		if (te != null && MountedFluidStorage.canUseAsStorage(te))
			fluidStorage.put(localPos, new MountedFluidStorage(te));
		if (AllMovementBehaviours.contains(captured.state.getBlock()))
			actors.add(MutablePair.of(blockInfo, null));
	}

	@Nullable
	protected CompoundNBT getTileEntityNBT(World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity == null)
			return null;
		CompoundNBT nbt = tileentity.write(new CompoundNBT());
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

	protected boolean movementAllowed(World world, BlockPos pos) {
		return BlockMovementTraits.movementAllowed(world, pos);
	}

	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor);
	}

	public void readNBT(World world, CompoundNBT nbt, boolean spawnData) {
		blocks.clear();
		presentTileEntities.clear();
		renderedTileEntities.clear();

		nbt.getList("Blocks", 10)
			.forEach(c -> {
				CompoundNBT comp = (CompoundNBT) c;
				BlockInfo info = new BlockInfo(NBTUtil.readBlockPos(comp.getCompound("Pos")),
					NBTUtil.readBlockState(comp.getCompound("Block")),
					comp.contains("Data") ? comp.getCompound("Data") : null);
				blocks.put(info.pos, info);

				if (world.isRemote) {
					Block block = info.state.getBlock();
					CompoundNBT tag = info.nbt;
					MovementBehaviour movementBehaviour = AllMovementBehaviours.of(block);
					if (tag == null || (movementBehaviour != null && movementBehaviour.hasSpecialMovementRenderer()))
						return;

					tag.putInt("x", info.pos.getX());
					tag.putInt("y", info.pos.getY());
					tag.putInt("z", info.pos.getZ());

					TileEntity te = TileEntity.create(tag);
					if (te == null)
						return;
					te.setLocation(new WrappedWorld(world) {

						@Override
						public BlockState getBlockState(BlockPos pos) {
							if (!pos.equals(te.getPos()))
								return Blocks.AIR.getDefaultState();
							return info.state;
						}

					}, te.getPos());
					if (te instanceof KineticTileEntity)
						((KineticTileEntity) te).setSpeed(0);
					te.getBlockState();
					presentTileEntities.put(info.pos, te);
					renderedTileEntities.add(te);
				}
			});

		actors.clear();
		nbt.getList("Actors", 10)
			.forEach(c -> {
				CompoundNBT comp = (CompoundNBT) c;
				BlockInfo info = blocks.get(NBTUtil.readBlockPos(comp.getCompound("Pos")));
				MovementContext context = MovementContext.readNBT(world, info, comp, this);
				getActors().add(MutablePair.of(info, context));
			});

		superglue.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Superglue", NBT.TAG_COMPOUND), c -> superglue
			.add(Pair.of(NBTUtil.readBlockPos(c.getCompound("Pos")), Direction.byIndex(c.getByte("Direction")))));

		seats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Seats", NBT.TAG_COMPOUND), c -> seats.add(NBTUtil.readBlockPos(c)));

		seatMapping.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Passengers", NBT.TAG_COMPOUND),
			c -> seatMapping.put(NBTUtil.readUniqueId(c.getCompound("Id")), c.getInt("Seat")));

		stabilizedSubContraptions.clear();
		NBTHelper.iterateCompoundList(nbt.getList("SubContraptions", NBT.TAG_COMPOUND), c -> stabilizedSubContraptions
			.put(NBTUtil.readUniqueId(c.getCompound("Id")), BlockFace.fromNBT(c.getCompound("Location"))));

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

		inventory = new CombinedInvWrapper(handlers);
		fluidInventory = new CombinedTankWrapper(fluidHandlers);

		if (nbt.contains("BoundsFront"))
			bounds = NBTHelper.readAABB(nbt.getList("BoundsFront", 5));

		stalled = nbt.getBoolean("Stalled");
		anchor = NBTUtil.readBlockPos(nbt.getCompound("Anchor"));
	}

	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Type", getType().id);
		ListNBT blocksNBT = new ListNBT();
		for (BlockInfo block : this.blocks.values()) {
			CompoundNBT c = new CompoundNBT();
			c.put("Block", NBTUtil.writeBlockState(block.state));
			c.put("Pos", NBTUtil.writeBlockPos(block.pos));
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blocksNBT.add(c);
		}

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
		for (Pair<BlockPos, Direction> glueEntry : superglue) {
			CompoundNBT c = new CompoundNBT();
			c.put("Pos", NBTUtil.writeBlockPos(glueEntry.getKey()));
			c.putByte("Direction", (byte) glueEntry.getValue()
				.getIndex());
			superglueNBT.add(c);
		}

		ListNBT storageNBT = new ListNBT();
		if (!spawnPacket) {
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
			tag.put("Id", NBTUtil.writeUniqueId(e.getKey()));
			tag.putInt("Seat", e.getValue());
			return tag;
		}));

		nbt.put("SubContraptions", NBTHelper.writeCompoundList(stabilizedSubContraptions.entrySet(), e -> {
			CompoundNBT tag = new CompoundNBT();
			tag.put("Id", NBTUtil.writeUniqueId(e.getKey()));
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

		if (bounds != null) {
			ListNBT bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
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
				if (brittles != BlockMovementTraits.isBrittle(block.state))
					continue;

				BlockPos add = block.pos.add(anchor).add(offset);
				if (customBlockRemoval(world, add, block.state))
					continue;
				BlockState oldState = world.getBlockState(add);
				Block blockIn = oldState.getBlock();
				if (block.state.getBlock() != blockIn)
					iterator.remove();
				world.getWorld()
					.removeTileEntity(add);
				int flags = BlockFlags.IS_MOVING | BlockFlags.NO_NEIGHBOR_DROPS | BlockFlags.UPDATE_NEIGHBORS;
				if (blockIn instanceof IWaterLoggable && oldState.has(BlockStateProperties.WATERLOGGED)
					&& oldState.get(BlockStateProperties.WATERLOGGED)
						.booleanValue()) {
					world.setBlockState(add, Blocks.WATER.getDefaultState(), flags);
					continue;
				}
				world.setBlockState(add, Blocks.AIR.getDefaultState(), flags);
			}
		}
		for (BlockInfo block : blocks.values()) {
			BlockPos add = block.pos.add(anchor).add(offset);
			world.markAndNotifyBlock(add, null, block.state, Blocks.AIR.getDefaultState(), BlockFlags.IS_MOVING | BlockFlags.DEFAULT);
		}
	}

	public void addBlocksToWorld(World world, StructureTransform transform) {
		for (boolean nonBrittles : Iterate.trueAndFalse) {
			for (BlockInfo block : blocks.values()) {
				if (nonBrittles == BlockMovementTraits.isBrittle(block.state))
					continue;

				BlockPos targetPos = transform.apply(block.pos);
				BlockState state = transform.apply(block.state);

				if (customBlockPlacement(world, targetPos, state))
					continue;

				if (nonBrittles)
					for (Direction face : Iterate.directions)
						state = state.updatePostPlacement(face, world.getBlockState(targetPos.offset(face)), world,
							targetPos, targetPos.offset(face));

				BlockState blockState = world.getBlockState(targetPos);
				if (blockState.getBlockHardness(world, targetPos) == -1 || (state.getCollisionShape(world, targetPos)
					.isEmpty()
					&& !blockState.getCollisionShape(world, targetPos)
						.isEmpty())) {
					if (targetPos.getY() == 0)
						targetPos = targetPos.up();
					world.playEvent(2001, targetPos, Block.getStateId(state));
					Block.spawnDrops(state, world, targetPos, null);
					continue;
				}
				if (state.getBlock() instanceof IWaterLoggable && state.has(BlockStateProperties.WATERLOGGED)) {
					IFluidState ifluidstate = world.getFluidState(targetPos);
					state = state.with(BlockStateProperties.WATERLOGGED,
						Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
				}

				world.destroyBlock(targetPos, true);
				world.setBlockState(targetPos, state, 3 | BlockFlags.IS_MOVING);

				boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
				verticalRotation = verticalRotation && transform.rotation != Rotation.NONE;
				if (verticalRotation) {
					if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock)
						world.destroyBlock(targetPos, true);
				}

				TileEntity tileEntity = world.getTileEntity(targetPos);
				CompoundNBT tag = block.nbt;
				if (tileEntity != null && tag != null) {
					tag.putInt("x", targetPos.getX());
					tag.putInt("y", targetPos.getY());
					tag.putInt("z", targetPos.getZ());

					if (verticalRotation && tileEntity instanceof PulleyTileEntity) {
						tag.remove("Offset");
						tag.remove("InitialOffset");
					}

					if (tileEntity instanceof FluidTankTileEntity && tag.contains("LastKnownPos"))
						tag.put("LastKnownPos", NBTUtil.writeBlockPos(BlockPos.ZERO.down()));

					tileEntity.read(tag);

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
			}
		}

		for (int i = 0; i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		for (int i = 0; i < fluidInventory.getTanks(); i++)
			fluidInventory.drain(fluidInventory.getFluidInTank(i), FluidAction.EXECUTE);

		for (Pair<BlockPos, Direction> pair : superglue) {
			BlockPos targetPos = transform.apply(pair.getKey());
			Direction targetFacing = transform.transformFacing(pair.getValue());

			SuperGlueEntity entity = new SuperGlueEntity(world, targetPos, targetFacing);
			if (entity.onValidSurface()) {
				if (!world.isRemote)
					world.addEntity(entity);
			}
		}
	}

	public void addPassengersToWorld(World world, StructureTransform transform, List<Entity> seatedEntities) {
		for (Entity seatedEntity : seatedEntities) {
			if (getSeatMapping().isEmpty())
				continue;
			Integer seatIndex = getSeatMapping().get(seatedEntity.getUniqueID());
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
			ctx.motion = Vec3d.ZERO;
			ctx.relativeMotion = Vec3d.ZERO;
			ctx.rotation = v -> v;
		});
	}

	public void foreachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<BlockInfo, MovementContext> pair : actors)
			callBack.accept(AllMovementBehaviours.of(pair.getLeft().state), pair.getRight());
	}

	public void expandBoundsAroundAxis(Axis axis) {
		AxisAlignedBB bb = bounds;
		double maxXDiff = Math.max(bb.maxX - 1, -bb.minX);
		double maxYDiff = Math.max(bb.maxY - 1, -bb.minY);
		double maxZDiff = Math.max(bb.maxZ - 1, -bb.minZ);
		double maxDiff = 0;

		if (axis == Axis.X)
			maxDiff = Math.max(maxZDiff, maxYDiff);
		if (axis == Axis.Y)
			maxDiff = Math.max(maxZDiff, maxXDiff);
		if (axis == Axis.Z)
			maxDiff = Math.max(maxXDiff, maxYDiff);

		Vec3d vec = new Vec3d(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis)
			.getDirectionVec());
		Vec3d planeByNormal = VecHelper.axisAlingedPlaneOf(vec);
		Vec3d min = vec.mul(bb.minX, bb.minY, bb.minZ)
			.add(planeByNormal.scale(-maxDiff));
		Vec3d max = vec.mul(bb.maxX, bb.maxY, bb.maxZ)
			.add(planeByNormal.scale(maxDiff + 1));
		bounds = new AxisAlignedBB(min, max);
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

}