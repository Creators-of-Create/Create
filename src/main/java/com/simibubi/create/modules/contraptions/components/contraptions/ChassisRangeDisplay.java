package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.foundation.utility.outliner.BlockClusterOutline;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.OutlineParticle;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ChassisTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
		BlockClusterOutline outline;
		OutlineParticle<Outline> particle;
		ChassisTileEntity te;
		int timer;

		public Entry(ChassisTileEntity te) {
			this.te = te;
			outline = new BlockClusterOutline(createSelection(te));
			particle = OutlineParticle.create(outline);
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

		for (Iterator<BlockPos> iterator = entries.keySet().iterator(); iterator.hasNext();) {
			Entry entry = entries.get(iterator.next());
			if (tickEntry(entry, hasWrench)) {
				entry.particle.remove();
				iterator.remove();
			}
		}

		for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
			GroupEntry group = iterator.next();
			if (tickEntry(group, hasWrench)) {
				iterator.remove();
				group.particle.remove();
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
				for (ChassisTileEntity included : existingGroupForPos.includedTEs) {
					Entry removed = entries.remove(included.getPos());
					if (removed != null)
						removed.particle.remove();
				}
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
			groupEntries.forEach(e -> e.particle.remove());
			groupEntries.clear();
			GroupEntry hoveredGroup = new GroupEntry(chassis);
			for (ChassisTileEntity included : hoveredGroup.includedTEs) {
				Entry remove = entries.remove(included.getPos());
				if (remove != null)
					remove.particle.remove();
			}
			groupEntries.add(hoveredGroup);
		} else {
			Entry old = entries.put(chassis.getPos(), new Entry(chassis));
			if (old != null)
				old.particle.remove();
		}
	}

	public static void renderOutlines(float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer) {
		// TODO 1.15 buffered render
		RenderSystem.lineWidth(2);
		TessellatorHelper.prepareForDrawing();
		RenderSystem.disableTexture();
		RenderSystem.enableAlphaTest();

		for (Entry entry : entries.values())
			renderPositions(entry, partialTicks);
		for (Entry groupEntry : groupEntries)
			renderPositions(groupEntry, partialTicks);

		RenderSystem.enableTexture();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		TessellatorHelper.cleanUpAfterDrawing();
		RenderSystem.lineWidth(1);
	}

	public static void renderPositions(Entry entry, float partialTicks) {
//		GlStateManager.pushMatrix();
//		RenderHelper.disableStandardItemLighting();
//		GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
//		GlStateManager.color4f(1, 1, 1, 1);
//		GlStateManager.enableTexture();
//		GlStateManager.depthMask(false);
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//
		float timer = entry.timer - partialTicks;
		float alpha = timer > 20 ? 1 : timer / 20f;
		entry.outline.setAlpha(alpha);
//		entry.outline.render(Tessellator.getInstance().getBuffer());
//
//		GlStateManager.disableBlend();
//		GlStateManager.depthMask(true);
//		GlStateManager.popMatrix();
	}

	private static GroupEntry getExistingGroupForPos(BlockPos pos) {
		for (GroupEntry groupEntry : groupEntries)
			for (ChassisTileEntity chassis : groupEntry.includedTEs)
				if (pos.equals(chassis.getPos()))
					return groupEntry;
		return null;
	}

}
