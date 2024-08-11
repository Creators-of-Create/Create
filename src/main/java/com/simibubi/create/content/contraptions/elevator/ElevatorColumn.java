package com.simibubi.create.content.contraptions.elevator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ElevatorColumn {

	public static WorldAttached<Map<ColumnCoords, ElevatorColumn>> LOADED_COLUMNS =
		new WorldAttached<>($ -> new HashMap<>());

	protected LevelAccessor level;
	protected ColumnCoords coords;
	protected List<Integer> contacts;
	protected int targetedYLevel;
	protected boolean isActive;
	protected boolean targetAvailable;

	@Nullable
	public static ElevatorColumn get(LevelAccessor level, ColumnCoords coords) {
		return LOADED_COLUMNS.get(level)
			.get(coords);
	}

	public static ElevatorColumn getOrCreate(LevelAccessor level, ColumnCoords coords) {
		return LOADED_COLUMNS.get(level)
			.computeIfAbsent(coords, c -> new ElevatorColumn(level, c));
	}

	public ElevatorColumn(LevelAccessor level, ColumnCoords coords) {
		this.level = level;
		this.coords = coords;
		contacts = new ArrayList<>();
		targetAvailable = false;
	}

	public void markDirty() {
		for (BlockPos pos : getContacts()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof ElevatorContactBlockEntity ecbe)
				ecbe.setChanged();
		}
	}

	public void floorReached(LevelAccessor level, String name) {
		getContacts().stream()
			.forEach(p -> {
				if (level.getBlockEntity(p) instanceof ElevatorContactBlockEntity ecbe)
					ecbe.updateDisplayedFloor(name);
			});
	}

	public int namesListVersion;

	public List<IntAttached<Couple<String>>> compileNamesList() {
		return getContacts().stream()
			.map(p -> {
				if (level.getBlockEntity(p) instanceof ElevatorContactBlockEntity ecbe)
					return IntAttached.with(p.getY(), ecbe.getNames());
				return null;
			})
			.filter(Objects::nonNull)
			.toList();
	}

	public void namesChanged() {
		namesListVersion++;
	}

	public Collection<BlockPos> getContacts() {
		return contacts.stream()
			.map(this::contactAt)
			.toList();
	}

	public void gatherAll() {
		BlockPos.betweenClosedStream(contactAt(level.getMinBuildHeight()), contactAt(level.getMaxBuildHeight()))
			.filter(p -> coords.equals(ElevatorContactBlock.getColumnCoords(level, p)))
			.forEach(p -> level.setBlock(p,
				BlockHelper.copyProperties(level.getBlockState(p), AllBlocks.ELEVATOR_CONTACT.getDefaultState()), 3));
	}

	public BlockPos contactAt(int y) {
		return new BlockPos(coords.x, y, coords.z);
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		markDirty();
		checkEmpty();
	}

	public boolean isActive() {
		return isActive;
	}

	public void target(int yLevel) {
		targetedYLevel = yLevel;
		targetAvailable = true;
	}
	
	public boolean isTargetAvailable() {
		return targetAvailable;
	}

	public int getTargetedYLevel() {
		return targetedYLevel;
	}

	public void initNames(Level level) {
		Integer prevLevel = null;

		for (int i = 0; i < contacts.size(); i++) {
			Integer y = contacts.get(i);

			BlockPos pos = contactAt(y);
			if (!(level.getBlockEntity(pos) instanceof ElevatorContactBlockEntity ecbe))
				continue;

			Integer currentLevel = null;

			if (!ecbe.shortName.isBlank()) {
				Integer tryValueOf = tryValueOf(ecbe.shortName);
				if (tryValueOf != null)
					currentLevel = tryValueOf;
				if (currentLevel == null)
					continue;
			}

			if (prevLevel != null)
				currentLevel = prevLevel + 1;

			Integer nextLevel = null;

			for (int peekI = i + 1; peekI < contacts.size(); peekI++) {
				BlockPos peekPos = contactAt(contacts.get(peekI));
				if (!(level.getBlockEntity(peekPos) instanceof ElevatorContactBlockEntity peekEcbe))
					continue;
				Integer tryValueOf = tryValueOf(peekEcbe.shortName);
				if (tryValueOf == null)
					continue;
				if (currentLevel != null && currentLevel >= tryValueOf) {
					peekEcbe.shortName = "";
					break;
				}
				nextLevel = tryValueOf;
				break;
			}

			if (currentLevel == null)
				currentLevel = nextLevel != null ? nextLevel - 1 : 0;

			ecbe.updateName(String.valueOf(currentLevel), ecbe.longName);
			prevLevel = currentLevel;
		}

	}

	private static Integer tryValueOf(String floorName) {
		try {
			return Integer.valueOf(floorName, 10);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public void add(BlockPos contactPos) {
		int coord = contactPos.getY();
		if (contacts.contains(coord))
			return;
		int index = 0;
		for (; index < contacts.size(); index++)
			if (contacts.get(index) > coord)
				break;
		contacts.add(index, coord);
		namesChanged();
	}

	public void remove(BlockPos contactPos) {
		contacts.remove((Object) contactPos.getY());
		checkEmpty();
		namesChanged();
	}

	private void checkEmpty() {
		if (contacts.isEmpty() && !isActive())
			LOADED_COLUMNS.get(level)
				.remove(coords);
	}

	public static record ColumnCoords(int x, int z, Direction side) {

		public ColumnCoords relative(BlockPos anchor) {
			return new ColumnCoords(x + anchor.getX(), z + anchor.getZ(), side);
		}

		public CompoundTag write() {
			CompoundTag tag = new CompoundTag();
			tag.putInt("X", x);
			tag.putInt("Z", z);
			NBTHelper.writeEnum(tag, "Side", side);
			return tag;
		}

		public static ColumnCoords read(CompoundTag tag) {
			int x = tag.getInt("X");
			int z = tag.getInt("Z");
			Direction side = NBTHelper.readEnum(tag, "Side", Direction.class);
			return new ColumnCoords(x, z, side);
		}

	}

}
