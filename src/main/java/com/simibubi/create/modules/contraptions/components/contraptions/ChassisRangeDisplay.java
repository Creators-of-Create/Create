package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.LinearChassisBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

public class ChassisRangeDisplay {

	private static final VoxelShape BLOCK_OUTLINE = Block.makeCuboidShape(-.5f, -.5f, -.5f, 16.5f, 16.5f, 16.5f);
	private static final int DISPLAY_TIME = 200;
	private static GroupEntry lastHoveredGroup = null;

	private static class Entry {
		VoxelShape shape;
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			this.shape = createSelection(te);
			timer = DISPLAY_TIME;
		}

		protected VoxelShape createSelection(ChassisTileEntity chassis) {
			List<BlockPos> positions = chassis.getIncludedBlockPositions(null, true);
			VoxelShape shape = VoxelShapes.empty();
			if (positions == null)
				return shape;
			for (BlockPos blockPos : positions)
				shape =
					VoxelShapes.or(shape, BLOCK_OUTLINE.withOffset(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
			return shape;
		}

	}

	private static class GroupEntry extends Entry {

		List<ChassisTileEntity> includedTEs;

		public GroupEntry(ChassisTileEntity te) {
			super(te);
		}

		@Override
		protected VoxelShape createSelection(ChassisTileEntity chassis) {
			VoxelShape shape = VoxelShapes.empty();
			includedTEs = te.collectChassisGroup();
			if (includedTEs == null)
				return shape;
			
			// outlining algo is not very scalable -> display only single chassis if group gets too large
			if (LinearChassisBlock.isChassis(chassis.getBlockState()) && includedTEs.size() > 32)
				includedTEs = Arrays.asList(chassis);
			if (AllBlocks.ROTATION_CHASSIS.typeOf(chassis.getBlockState()) && includedTEs.size() > 8)
				includedTEs = Arrays.asList(chassis);
			
			for (ChassisTileEntity chassisTileEntity : includedTEs)
				shape = VoxelShapes.or(shape, super.createSelection(chassisTileEntity));
			return shape;
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

		if (hasWrench) {
			RayTraceResult over = Minecraft.getInstance().objectMouseOver;
			if (!(over instanceof BlockRayTraceResult))
				return;
			BlockRayTraceResult ray = (BlockRayTraceResult) over;
			BlockPos pos = ray.getPos();
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity == null || tileEntity.isRemoved())
				return;
			if (tileEntity instanceof ChassisTileEntity) {
				ChassisTileEntity chassisTileEntity = (ChassisTileEntity) tileEntity;
				if (AllKeys.ctrlDown()) {
					GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
					if (existingGroupForPos != null) {
						for (ChassisTileEntity included : existingGroupForPos.includedTEs)
							entries.remove(included.getPos());
						existingGroupForPos.timer = DISPLAY_TIME;
						return;
					}
				}
				if (!entries.containsKey(pos) || AllKeys.ctrlDown())
					display(chassisTileEntity);
				else {
					deselect();
					if (!AllKeys.ctrlDown())
						entries.get(pos).timer = DISPLAY_TIME;
				}
			}
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

		for (Entry entry : entries.values()) {
			float timer = entry.timer - partialTicks;
			float alpha = timer > 20 ? 1 : timer / 20f;
			WorldRenderer.drawShape(entry.shape, 0, 0, 0, 1, .7f, 0, alpha);
		}
		for (Entry entry : groupEntries) {
			float timer = entry.timer - partialTicks;
			float alpha = timer > 20 ? 1 : timer / 20f;
			WorldRenderer.drawShape(entry.shape, 0, 0, 0, 1, .7f, 0, alpha);
		}

		GlStateManager.enableTexture();
		TessellatorHelper.cleanUpAfterDrawing();
		GlStateManager.lineWidth(1);
	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.getPos()))
					return groupEntry;
		return null;
	}

}
