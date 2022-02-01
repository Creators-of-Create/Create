package com.simibubi.create.content.logistics.trains.entity;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.NonStationaryLighter;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class CarriageContraption extends Contraption {

	private Direction assemblyDirection;

	private boolean controls;
	private int bogeys;
	private BlockPos secondBogeyPos;

	private Carriage carriage;
	public int temporaryCarriageIdHolder = -1;

	public CarriageContraption() {}

	public CarriageContraption(Direction assemblyDirection) {
		this.assemblyDirection = assemblyDirection;
		this.bogeys = 0;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		if (blocks.size() == 0)
			return false;
		if (bogeys > 2 || bogeys == 0)
			throw AssemblyException.invalidBogeyCount();
		return true;
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

		if (AllBlocks.CONTROLS.has(blockState))
			controls = true;

		return super.capture(world, pos);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		NBTHelper.writeEnum(tag, "AssemblyDirection", getAssemblyDirection());
		if (spawnPacket)
			tag.putInt("CarriageId", carriage.id);
		tag.putBoolean("Controls", hasControls());
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		assemblyDirection = NBTHelper.readEnum(nbt, "AssemblyDirection", Direction.class);
		if (spawnData)
			temporaryCarriageIdHolder = nbt.getInt("CarriageId");
		controls = nbt.getBoolean("Controls");
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
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}

	public Direction getAssemblyDirection() {
		return assemblyDirection;
	}

	public void setCarriage(Carriage carriage) {
		this.carriage = carriage;
		temporaryCarriageIdHolder = carriage.id;
	}

	public Carriage getCarriage() {
		return carriage;
	}

	public boolean hasControls() {
		return controls;
	}

	public BlockPos getSecondBogeyPos() {
		return secondBogeyPos;
	}

}
