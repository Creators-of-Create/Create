package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.NonStationaryLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionLighter;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CarriageContraption extends Contraption {

	private Direction assemblyDirection;
	private boolean forwardControls;
	private boolean backwardControls;
	public Couple<Boolean> blazeBurnerConductors;
	public Map<BlockPos, Couple<Boolean>> conductorSeats;

	// during assembly only
	private int bogeys;
	private boolean sidewaysControls;
	private BlockPos secondBogeyPos;
	private List<BlockPos> assembledBlazeBurners;

	public CarriageContraption() {
		conductorSeats = new HashMap<>();
		assembledBlazeBurners = new ArrayList<>();
		blazeBurnerConductors = Couple.create(false, false);
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
			throw new AssemblyException(Lang.translate("train_assembly.too_many_bogeys", bogeys));
		if (sidewaysControls)
			throw new AssemblyException(Lang.translate("train_assembly.sideways_controls"));

		for (BlockPos blazePos : assembledBlazeBurners)
			for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
				if (inControl(blazePos, direction))
					blazeBurnerConductors.set(direction != assemblyDirection, true);
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
		if (!AllBlocks.CONTROLS.has(info.state))
			return false;
		return info.state.getValue(ControlsBlock.FACING) == direction.getOpposite();
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);

		if (blockState.getBlock() instanceof IBogeyBlock) {
			bogeys++;
			if (bogeys == 2)
				secondBogeyPos = pos;
			return Pair.of(new StructureBlockInfo(pos, blockState, null), null);
		}

		if (AllBlocks.BLAZE_BURNER.has(blockState)
			&& blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE)
			assembledBlazeBurners.add(toLocalPos(pos));

		if (AllBlocks.CONTROLS.has(blockState)) {
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
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		NBTHelper.writeEnum(tag, "AssemblyDirection", getAssemblyDirection());
		tag.putBoolean("FrontControls", forwardControls);
		tag.putBoolean("BackControls", backwardControls);
		tag.putBoolean("FrontBlazeConductor", blazeBurnerConductors.getFirst());
		tag.putBoolean("BackBlazeConductor", blazeBurnerConductors.getSecond());
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
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		assemblyDirection = NBTHelper.readEnum(nbt, "AssemblyDirection", Direction.class);
		forwardControls = nbt.getBoolean("FrontControls");
		backwardControls = nbt.getBoolean("BackControls");
		blazeBurnerConductors =
			Couple.create(nbt.getBoolean("FrontBlazeConductor"), nbt.getBoolean("BackBlazeConductor"));
		conductorSeats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("ConductorSeats", Tag.TAG_COMPOUND),
			c -> conductorSeats.put(NbtUtils.readBlockPos(c.getCompound("Pos")),
				Couple.create(c.getBoolean("Forward"), c.getBoolean("Backward"))));
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return false;
	}

	@Override
	protected ContraptionType getType() {
		return ContraptionType.CARRIAGE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
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

}
