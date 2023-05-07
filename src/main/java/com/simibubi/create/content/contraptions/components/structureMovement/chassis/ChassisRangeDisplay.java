package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ChassisRangeDisplay {

	private static final int DISPLAY_TIME = 200;
	private static GroupEntry lastHoveredGroup = null;

	private static class Entry {
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			timer = DISPLAY_TIME;
			CreateClient.OUTLINER.showCluster(getOutlineKey(), createSelection(te))
					.colored(0xFFFFFF)
					.disableNormals()
					.lineWidth(1 / 16f)
					.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
		}

		protected Object getOutlineKey() {
			return Pair.of(te.getBlockPos(), 1);
		}

		protected Set<BlockPos> createSelection(ChassisTileEntity chassis) {
			Set<BlockPos> positions = new HashSet<>();
			List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(null, true);
			if (includedBlockPositions == null)
				return Collections.emptySet();
			positions.addAll(includedBlockPositions);
			return positions;
		}

	}

	private static class GroupEntry extends Entry {

		List<ChassisTileEntity> includedTEs;

		public GroupEntry(ChassisTileEntity te) {
			super(te);
		}

		@Override
		protected Object getOutlineKey() {
			return this;
		}

		@Override
		protected Set<BlockPos> createSelection(ChassisTileEntity chassis) {
			Set<BlockPos> list = new HashSet<>();
			includedTEs = te.collectChassisGroup();
			if (includedTEs == null)
				return list;
			for (ChassisTileEntity chassisTileEntity : includedTEs)
				list.addAll(super.createSelection(chassisTileEntity));
			return list;
		}

	}

	static Map<BlockPos, Entry> entries = new HashMap<>();
	static List<GroupEntry> groupEntries = new ArrayList<>();

	public static void tick() {
		Player player = Minecraft.getInstance().player;
		Level world = Minecraft.getInstance().level;
		boolean hasWrench = AllItems.WRENCH.isIn(player.getMainHandItem());

		for (Iterator<BlockPos> iterator = entries.keySet()
			.iterator(); iterator.hasNext();) {
			BlockPos pos = iterator.next();
			Entry entry = entries.get(pos);
			if (tickEntry(entry, hasWrench))
				iterator.remove();
			CreateClient.OUTLINER.keep(entry.getOutlineKey());
		}

		for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
			GroupEntry group = iterator.next();
			if (tickEntry(group, hasWrench)) {
				iterator.remove();
				if (group == lastHoveredGroup)
					lastHoveredGroup = null;
			}
			CreateClient.OUTLINER.keep(group.getOutlineKey());
		}

		if (!hasWrench)
			return;

		HitResult over = Minecraft.getInstance().hitResult;
		if (!(over instanceof BlockHitResult))
			return;
		BlockHitResult ray = (BlockHitResult) over;
		BlockPos pos = ray.getBlockPos();
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity == null || tileEntity.isRemoved())
			return;
		if (!(tileEntity instanceof ChassisTileEntity))
			return;

		boolean ctrl = AllKeys.ctrlDown();
		ChassisTileEntity chassisTileEntity = (ChassisTileEntity) tileEntity;

		if (ctrl) {
			GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
			if (existingGroupForPos != null) {
				for (ChassisTileEntity included : existingGroupForPos.includedTEs)
					entries.remove(included.getBlockPos());
				existingGroupForPos.timer = DISPLAY_TIME;
				return;
			}
		}

		if (!entries.containsKey(pos) || ctrl)
			display(chassisTileEntity);
		else {
			if (!ctrl)
				entries.get(pos).timer = DISPLAY_TIME;
		}
	}

	private static boolean tickEntry(Entry entry, boolean hasWrench) {
		ChassisTileEntity chassisTileEntity = entry.te;
		Level teWorld = chassisTileEntity.getLevel();
		Level world = Minecraft.getInstance().level;

		if (chassisTileEntity.isRemoved() || teWorld == null || teWorld != world
			|| !world.isLoaded(chassisTileEntity.getBlockPos())) {
			return true;
		}

		if (!hasWrench && entry.timer > 20) {
			entry.timer = 20;
			return false;
		}

		entry.timer--;
		if (entry.timer == 0)
			return true;
		return false;
	}

	public static void display(ChassisTileEntity chassis) {

		// Display a group and kill any selections of its contained chassis blocks
		if (AllKeys.ctrlDown()) {
			GroupEntry hoveredGroup = new GroupEntry(chassis);

			for (ChassisTileEntity included : hoveredGroup.includedTEs)
				CreateClient.OUTLINER.remove(included.getBlockPos());

			groupEntries.forEach(entry -> CreateClient.OUTLINER.remove(entry.getOutlineKey()));
			groupEntries.clear();
			entries.clear();
			groupEntries.add(hoveredGroup);
			return;
		}

		// Display an individual chassis and kill any group selections that contained it
		BlockPos pos = chassis.getBlockPos();
		GroupEntry entry = getExistingGroupForPos(pos);
		if (entry != null)
			CreateClient.OUTLINER.remove(entry.getOutlineKey());

		groupEntries.clear();
		entries.clear();
		entries.put(pos, new Entry(chassis));

	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.getBlockPos()))
					return groupEntry;
		return null;
	}

}
