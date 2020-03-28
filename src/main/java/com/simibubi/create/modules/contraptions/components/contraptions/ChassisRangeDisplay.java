package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ChassisTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
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
		Set<BlockPos> includedPositions;
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			includedPositions = createSelection(te);
			timer = DISPLAY_TIME;
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

	public static void clientTick() {
		PlayerEntity player = Minecraft.getInstance().player;
		World world = Minecraft.getInstance().world;
		boolean hasWrench = AllItems.WRENCH.typeOf(player.getHeldItemMainhand());

		for (Iterator<BlockPos> iterator = entries.keySet().iterator(); iterator.hasNext();)
			if (tickEntry(entries.get(iterator.next()), hasWrench))
				iterator.remove();

		for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
			GroupEntry group = iterator.next();
			if (tickEntry(group, hasWrench)) {
				iterator.remove();
				if (group == lastHoveredGroup)
					lastHoveredGroup = null;
			}
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
			deselect();
			if (!ctrl)
				entries.get(pos).timer = DISPLAY_TIME;
		}
	}

	private static void deselect() {
		for (Entry entry : entries.values())
			if (entry.timer > 10)
				entry.timer = 10;
		for (Entry entry : groupEntries)
			if (entry.timer > 10)
				entry.timer = 10;
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
		deselect();
		if (AllKeys.ctrlDown()) {
			groupEntries.clear();
			GroupEntry hoveredGroup = new GroupEntry(chassis);
			for (ChassisTileEntity included : hoveredGroup.includedTEs)
				entries.remove(included.getPos());
			groupEntries.add(hoveredGroup);
		} else {
			entries.put(chassis.getPos(), new Entry(chassis));
		}
	}

	public static void renderOutlines(float partialTicks) {
		GlStateManager.lineWidth(2);
		TessellatorHelper.prepareForDrawing();
		GlStateManager.disableTexture();
		GlStateManager.enableAlphaTest();

		for (Entry entry : entries.values())
			renderPositions(entry, partialTicks);
		for (Entry groupEntry : groupEntries)
			renderPositions(groupEntry, partialTicks);

		GlStateManager.enableTexture();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		TessellatorHelper.cleanUpAfterDrawing();
		GlStateManager.lineWidth(1);
	}

	public static void renderPositions(Entry entry, float partialTicks) {
		TessellatorHelper.begin();
		BlockPos size = new BlockPos(1, 1, 1);
		float timer = entry.timer - partialTicks;
		float alpha = timer > 20 ? .5f : timer / 40f;
		GlStateManager.color4f(1, .7f, 0, alpha);
		Set<BlockPos> includedPositions = entry.includedPositions;
		GlStateManager.depthMask(false);
		for (BlockPos pos : includedPositions)
			TessellatorHelper.cube(Tessellator.getInstance().getBuffer(), pos, size, 1 / 16f - 1 / 64f, true, false);
		TessellatorHelper.draw();
		GlStateManager.depthMask(true);
	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.getPos()))
					return groupEntry;
		return null;
	}

}
