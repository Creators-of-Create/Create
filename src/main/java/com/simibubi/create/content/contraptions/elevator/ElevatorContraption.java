package com.simibubi.create.content.contraptions.elevator;

import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.redstone.contact.RedstoneContactBlock;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.network.PacketDistributor;

public class ElevatorContraption extends PulleyContraption {

	protected ColumnCoords column;
	protected int contactYOffset;
	public boolean arrived;

	private int namesListVersion = -1;
	public List<IntAttached<Couple<String>>> namesList = ImmutableList.of();
	public int clientYTarget;
	
	public int maxContactY;
	public int minContactY;

	// during assembly only
	private int contacts;

	public ElevatorContraption() {
		super();
	}

	public ElevatorContraption(int initialOffset) {
		super(initialOffset);
	}

	@Override
	public void tickStorage(AbstractContraptionEntity entity) {
		super.tickStorage(entity);

		if (entity.tickCount % 10 != 0)
			return;

		ColumnCoords coords = getGlobalColumn();
		ElevatorColumn column = ElevatorColumn.get(entity.level, coords);

		if (column == null)
			return;
		if (column.namesListVersion == namesListVersion)
			return;

		namesList = column.compileNamesList();
		namesListVersion = column.namesListVersion;
		AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
			new ElevatorFloorListPacket(entity, namesList));
	}

	@Override
	protected void disableActorOnStart(MovementContext context) {}

	public ColumnCoords getGlobalColumn() {
		return column.relative(anchor);
	}

	public Integer getCurrentTargetY(Level level) {
		ColumnCoords coords = getGlobalColumn();
		ElevatorColumn column = ElevatorColumn.get(level, coords);
		if (column == null)
			return null;
		int targetedYLevel = column.targetedYLevel;
		if (isTargetUnreachable(targetedYLevel))
			return null;
		return targetedYLevel;
	}
	
	public boolean isTargetUnreachable(int contactY) {
		return contactY < minContactY || contactY > maxContactY;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!searchMovedStructure(world, pos, null))
			return false;
		if (blocks.size() <= 0)
			return false;
		if (contacts == 0)
			throw new AssemblyException(Lang.translateDirect("gui.assembly.exception.no_contacts"));
		if (contacts > 1)
			throw new AssemblyException(Lang.translateDirect("gui.assembly.exception.too_many_contacts"));
		
		ElevatorColumn column = ElevatorColumn.get(world, getGlobalColumn());
		if (column != null && column.isActive())
			throw new AssemblyException(Lang.translateDirect("gui.assembly.exception.column_conflict"));
		
		startMoving(world);
		return true;
	}

	@Override
	protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);

		if (!AllBlocks.REDSTONE_CONTACT.has(blockState))
			return super.capture(world, pos);

		Direction facing = blockState.getValue(RedstoneContactBlock.FACING);
		if (facing.getAxis() == Axis.Y)
			return super.capture(world, pos);

		contacts++;
		BlockPos local = toLocalPos(pos.relative(facing));
		column = new ColumnCoords(local.getX(), local.getZ(), facing.getOpposite());
		contactYOffset = local.getY();

		return super.capture(world, pos);
	}

	public int getContactYOffset() {
		return contactYOffset;
	}

	public void broadcastFloorData(Level level, BlockPos contactPos) {
		ElevatorColumn column = ElevatorColumn.get(level, getGlobalColumn());
		if (!(world.getBlockEntity(contactPos) instanceof ElevatorContactBlockEntity ecbe))
			return;
		if (column != null)
			column.floorReached(level, ecbe.shortName);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putBoolean("Arrived", arrived);
		tag.put("Column", column.write());
		tag.putInt("ContactY", contactYOffset);
		tag.putInt("MaxContactY", maxContactY);
		tag.putInt("MinContactY", minContactY);
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		arrived = nbt.getBoolean("Arrived");
		column = ColumnCoords.read(nbt.getCompound("Column"));
		contactYOffset = nbt.getInt("ContactY");
		maxContactY = nbt.getInt("MaxContactY");
		minContactY = nbt.getInt("MinContactY");
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	public ContraptionType getType() {
		return ContraptionType.ELEVATOR;
	}

	public void setClientYTarget(int clientYTarget) {
		if (this.clientYTarget == clientYTarget)
			return;

		this.clientYTarget = clientYTarget;
		syncControlDisplays();
	}

	public void syncControlDisplays() {
		if (namesList.isEmpty())
			return;
		for (int i = 0; i < namesList.size(); i++)
			if (namesList.get(i)
				.getFirst() == clientYTarget)
				setAllControlsToFloor(i);
	}

	public void setAllControlsToFloor(int floorIndex) {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
			if (pair.right != null && pair.right.temporaryData instanceof ElevatorFloorSelection efs)
				efs.currentIndex = floorIndex;
	}

}
