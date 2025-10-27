package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllContraptionTypes;
import com.simibubi.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;

public class CarriageContraption extends Contraption {

	private Direction assemblyDirection;
	private boolean forwardControls;
	private boolean backwardControls;

	public Couple<Boolean> blockConductors;
	public Map<BlockPos, Couple<Boolean>> conductorSeats;
	public ArrivalSoundQueue soundQueue;

	protected MountedStorageManager storageProxy;

	// during assembly only
	private int bogeys;
	private boolean sidewaysControls;
	private BlockPos secondBogeyPos;
	private List<BlockPos> assembledBlockConductors;

	// render
	public int portalCutoffMin;
	public int portalCutoffMax;

	static final MountedStorageManager fallbackStorage;

	static {
		fallbackStorage = new MountedStorageManager();
		fallbackStorage.initialize();
	}

	public CarriageContraption() {
		conductorSeats = new HashMap<>();
		assembledBlockConductors = new ArrayList<>();
		blockConductors = Couple.create(false, false);
		soundQueue = new ArrivalSoundQueue();
		portalCutoffMin = Integer.MIN_VALUE;
		portalCutoffMax = Integer.MAX_VALUE;
		storage = new TrainCargoManager();
	}

	public void setSoundQueueOffset(int offset) {
		soundQueue.offset = offset;
	}

	public CarriageContraption(Direction assemblyDirection) {
		this();
		this.assemblyDirection = assemblyDirection;
		this.bogeys = 0;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		if (blocks.size() <= 1)
			return false;
		if (bogeys == 0)
			return false;
		if (bogeys > 2)
			throw new AssemblyException(CreateLang.translateDirect("train_assembly.too_many_bogeys", bogeys));
		if (sidewaysControls)
			throw new AssemblyException(CreateLang.translateDirect("train_assembly.sideways_controls"));

		for (BlockPos blazePos : assembledBlockConductors)
			for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
				if (inControl(blazePos, direction))
					blockConductors.set(direction != assemblyDirection, true);
		for (BlockPos seatPos : getSeats())
			for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
				if (inControl(seatPos, direction))
					conductorSeats.computeIfAbsent(seatPos, p -> Couple.create(false, false))
						.set(direction != assemblyDirection, true);

		return true;
	}

	public boolean inControl(BlockPos pos, Direction direction) {
		BlockPos controlsPos = pos.relative(direction);
		if (!blocks.containsKey(controlsPos))
			return false;
		StructureBlockInfo info = blocks.get(controlsPos);
		if (!AllBlocks.TRAIN_CONTROLS.has(info.state()))
			return false;
		return info.state()
			.getValue(ControlsBlock.FACING) == direction.getOpposite();
	}

	public void swapStorageAfterAssembly(CarriageContraptionEntity cce) {
		// Ensure that the entity does not hold its inventory data, because the global
		// carriage manages it instead
		Carriage carriage = cce.getCarriage();
		if (carriage.storage == null) {
			carriage.storage = (TrainCargoManager) storage;
			storage = new MountedStorageManager();
		}
		storageProxy = carriage.storage;
	}

	public void returnStorageForDisassembly(MountedStorageManager storage) {
		this.storage = storage;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);

		if (ArrivalSoundQueue.isPlayable(blockState)) {
			int anchorCoord = VecHelper.getCoordinate(anchor, assemblyDirection.getAxis());
			int posCoord = VecHelper.getCoordinate(pos, assemblyDirection.getAxis());
			soundQueue.add((posCoord - anchorCoord) * assemblyDirection.getAxisDirection()
				.getStep(), toLocalPos(pos));
		}

		if (blockState.getBlock() instanceof AbstractBogeyBlock<?>) {
			bogeys++;
			if (bogeys == 2)
				secondBogeyPos = pos;
		}

		MovingInteractionBehaviour behaviour = MovingInteractionBehaviour.REGISTRY.get(blockState);
		if (behaviour instanceof ConductorBlockInteractionBehavior conductor && conductor.isValidConductor(blockState)) {
			assembledBlockConductors.add(toLocalPos(pos));
		}

		if (AllBlocks.TRAIN_CONTROLS.has(blockState)) {
			Direction facing = blockState.getValue(ControlsBlock.FACING);
			if (facing.getAxis() != assemblyDirection.getAxis())
				sidewaysControls = true;
			else {
				boolean forwards = facing == assemblyDirection;
				if (forwards)
					forwardControls = true;
				else
					backwardControls = true;
			}
		}

		return super.capture(world, pos);
	}

	@Override
	public CompoundTag writeNBT(HolderLookup.Provider registries, boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(registries, spawnPacket);
		NBTHelper.writeEnum(tag, "AssemblyDirection", getAssemblyDirection());
		tag.putBoolean("FrontControls", forwardControls);
		tag.putBoolean("BackControls", backwardControls);
		tag.putBoolean("FrontBlazeConductor", blockConductors.getFirst());
		tag.putBoolean("BackBlazeConductor", blockConductors.getSecond());
		ListTag list = NBTHelper.writeCompoundList(conductorSeats.entrySet(), e -> {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
			compoundTag.putBoolean("Forward", e.getValue()
				.getFirst());
			compoundTag.putBoolean("Backward", e.getValue()
				.getSecond());
			return compoundTag;
		});
		tag.put("ConductorSeats", list);
		soundQueue.serialize(tag);
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		assemblyDirection = NBTHelper.readEnum(nbt, "AssemblyDirection", Direction.class);
		forwardControls = nbt.getBoolean("FrontControls");
		backwardControls = nbt.getBoolean("BackControls");
		blockConductors =
			Couple.create(nbt.getBoolean("FrontBlazeConductor"), nbt.getBoolean("BackBlazeConductor"));
		conductorSeats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("ConductorSeats", Tag.TAG_COMPOUND),
			c -> conductorSeats.put(NBTHelper.readBlockPos(c, "Pos"),
				Couple.create(c.getBoolean("Forward"), c.getBoolean("Backward"))));
		soundQueue.deserialize(nbt);
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}

	@Override
	public ContraptionType getType() {
		return AllContraptionTypes.CARRIAGE.value();
	}

	public Direction getAssemblyDirection() {
		return assemblyDirection;
	}

	public boolean hasForwardControls() {
		return forwardControls;
	}

	public boolean hasBackwardControls() {
		return backwardControls;
	}

	public BlockPos getSecondBogeyPos() {
		return secondBogeyPos;
	}

	@Override
	public Optional<List<AABB>> getSimplifiedEntityColliders() {
		if (notInPortal())
			return super.getSimplifiedEntityColliders();
		return Optional.empty();
	}

	@Override
	public boolean isHiddenInPortal(BlockPos localPos) {
		if (notInPortal())
			return super.isHiddenInPortal(localPos);
		Direction facing = assemblyDirection;
		Axis axis = facing.getClockWise()
			.getAxis();
		int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection()
			.getStep();
		return !withinVisible(coord) || atSeam(coord);
	}

	public boolean isHiddenInPortal(int posAlongMovementAxis) {
		if (notInPortal())
			return false;
		return !withinVisible(posAlongMovementAxis) || atSeam(posAlongMovementAxis);
	}

	public boolean notInPortal() {
		return portalCutoffMin == Integer.MIN_VALUE && portalCutoffMax == Integer.MAX_VALUE;
	}

	public boolean atSeam(BlockPos localPos) {
		Direction facing = assemblyDirection;
		Axis axis = facing.getClockWise()
			.getAxis();
		int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection()
			.getStep();
		return atSeam(coord);
	}

	public boolean withinVisible(BlockPos localPos) {
		Direction facing = assemblyDirection;
		Axis axis = facing.getClockWise()
			.getAxis();
		int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection()
			.getStep();
		return withinVisible(coord);
	}

	public boolean atSeam(int posAlongMovementAxis) {
		return posAlongMovementAxis == portalCutoffMin || posAlongMovementAxis == portalCutoffMax;
	}

	public boolean withinVisible(int posAlongMovementAxis) {
		return posAlongMovementAxis > portalCutoffMin && posAlongMovementAxis < portalCutoffMax;
	}

	@Override
	public MountedStorageManager getStorage() {
		return storageProxy == null ? fallbackStorage : storageProxy;
	}

	@Override
	public void writeStorage(CompoundTag nbt, HolderLookup.Provider registries, boolean spawnPacket) {
		if (!spawnPacket)
			return;
		if (storageProxy != null)
			storageProxy.write(nbt, registries, spawnPacket);
	}

	@Override
	protected ClientContraption createClientContraption() {
		return new CarriageClientContraption(this);
	}

	public class CarriageClientContraption extends ClientContraption {
		// Parallel array to renderedBlockEntityView. Marks BEs that are outside the portal.
		public final BitSet scratchBlockEntitiesOutsidePortal = new BitSet();

		public CarriageClientContraption(CarriageContraption contraption) {
			super(contraption);
		}

		@Override
		public RenderedBlocks getRenderedBlocks() {
			if (notInPortal())
				return super.getRenderedBlocks();

			Map<BlockPos, BlockState> values = new HashMap<>();
			blocks.forEach((pos, info) -> {
				if (withinVisible(pos)) {
					values.put(pos, info.state());
				} else if (atSeam(pos)) {
					values.put(pos, Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
				}
			});
			return new RenderedBlocks(pos -> values.getOrDefault(pos, Blocks.AIR.defaultBlockState()), values.keySet());
		}

		@Override
		public BlockEntity readBlockEntity(Level level, StructureBlockInfo info, boolean legacy) {
			if (info.state().getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.captureBlockEntityForTrain())
				return null; // Bogeys are typically rendered by the carriage contraption, not the BE

			return super.readBlockEntity(level, info, legacy);
		}

		@Override
		public BitSet getAndAdjustShouldRenderBlockEntities() {
			if (notInPortal()) {
				return super.getAndAdjustShouldRenderBlockEntities();
			}

			scratchBlockEntitiesOutsidePortal.clear();
			scratchBlockEntitiesOutsidePortal.or(shouldRenderBlockEntities);

			for (var i = 0; i < renderedBlockEntityView.size(); i++) {
				var be = renderedBlockEntityView.get(i);
				if (isHiddenInPortal(be.getBlockPos())) {
					scratchBlockEntitiesOutsidePortal.clear(i);
				}
			}

			return scratchBlockEntitiesOutsidePortal;
		}
	}

}
