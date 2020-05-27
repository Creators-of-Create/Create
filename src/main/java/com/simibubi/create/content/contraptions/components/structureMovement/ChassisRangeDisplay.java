package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItemsNew;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ChassisRangeDisplay {

	private static final int DISPLAY_TIME = 200;
	private static GroupEntry lastHoveredGroup = null;

	private static class Entry {
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			timer = DISPLAY_TIME;
			CreateClient.outliner.showCluster(getOutlineKey(), createSelection(te))
				.colored(0xFFFFFF)
				.disableNormals()
				.lineWidth(1 / 16f)
				.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
		}

		protected Object getOutlineKey() {
			return Pair.of(te.getPos(), new Integer(1));
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
		PlayerEntity player = Minecraft.getInstance().player;
		World world = Minecraft.getInstance().world;
		boolean hasWrench = AllItemsNew.typeOf(AllItemsNew.WRENCH, player.getHeldItemMainhand());

		for (Iterator<BlockPos> iterator = entries.keySet()
			.iterator(); iterator.hasNext();) {
			BlockPos pos = iterator.next();
			Entry entry = entries.get(pos);
			if (tickEntry(entry, hasWrench))
				iterator.remove();
			CreateClient.outliner.keep(entry.getOutlineKey());
		}

		for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
			GroupEntry group = iterator.next();
			if (tickEntry(group, hasWrench)) {
				iterator.remove();
				if (group == lastHoveredGroup)
					lastHoveredGroup = null;
			}
			CreateClient.outliner.keep(group.getOutlineKey());
		}

		if (!hasWrench)
			return;

		RayTraceResult over = Minecraft.getInstance().objectMouseOver;
		if (!(over instanceof BlockRayTraceResult))
			return;
		BlockRayTraceResult ray = (BlockRayTraceResult) over;
		BlockPos pos = ray.getPos();
		TileEntity tileEntity = world.getTileEntity(pos);
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
					entries.remove(included.getPos());
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
		World teWorld = chassisTileEntity.getWorld();
		World world = Minecraft.getInstance().world;

		if (chassisTileEntity.isRemoved() || teWorld == null || teWorld != world
			|| !world.isBlockPresent(chassisTileEntity.getPos())) {
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
				CreateClient.outliner.remove(included.getPos());

			groupEntries.forEach(entry -> CreateClient.outliner.remove(entry.getOutlineKey()));
			groupEntries.clear();
			entries.clear();
			groupEntries.add(hoveredGroup);
			return;
		}

		// Display an individual chassis and kill any group selections that contained it
		BlockPos pos = chassis.getPos();
		GroupEntry entry = getExistingGroupForPos(pos);
		if (entry != null)
			CreateClient.outliner.remove(entry.getOutlineKey());

		groupEntries.clear();
		entries.clear();
		entries.put(pos, new Entry(chassis));

	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.getPos()))
					return groupEntry;
		return null;
	}

}
