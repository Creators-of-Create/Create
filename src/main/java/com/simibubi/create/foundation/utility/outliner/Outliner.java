package com.simibubi.create.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Outliner {

	Map<Object, OutlineEntry> outlines;

	// Facade

	public OutlineParams showAABB(Object slot, AxisAlignedBB bb, ExpireType type) {
		createAABBOutlineIfMissing(slot, bb, type);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.prevBB = outline.targetBB = bb;
		return outline.getParams();
	}

	public OutlineParams chaseAABB(Object slot, AxisAlignedBB bb, ExpireType type) {
		createAABBOutlineIfMissing(slot, bb, type);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.targetBB = bb;
		return outline.getParams();
	}

	public OutlineParams showCluster(Object slot, Iterable<BlockPos> selection, ExpireType type) {
		BlockClusterOutline outline = new BlockClusterOutline(selection);
		OutlineEntry entry = new OutlineEntry(outline, type);
		outlines.put(slot, entry);
		return entry.getOutline()
			.getParams();
	}

	public void keepCluster(Object slot) {
		if (outlines.containsKey(slot))
			outlines.get(slot).ticksTillRemoval = 1;
	}

	public void remove(Object slot) {
		outlines.remove(slot);
	}

	// Utility

	private void createAABBOutlineIfMissing(Object slot, AxisAlignedBB bb, ExpireType type) {
		if (!outlines.containsKey(slot)) {
			ChasingAABBOutline outline = new ChasingAABBOutline(bb);
			outlines.put(slot, new OutlineEntry(outline, type));
		}
	}

	private ChasingAABBOutline getAndRefreshAABB(Object slot) {
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		return (ChasingAABBOutline) entry.getOutline();
	}

	// Maintenance

	public Outliner() {
		outlines = new HashMap<>();
	}

	public void tickOutlines() {
		Set<Object> toClear = new HashSet<>();

		outlines.forEach((key, entry) -> {
			entry.ticksTillRemoval--;
			entry.getOutline()
				.tick();
			if (entry.isAlive())
				return;
			toClear.add(key);
		});

		toClear.forEach(outlines::remove);
	}

	public void renderOutlines(MatrixStack ms, IRenderTypeBuffer buffer) {
		outlines.forEach((key, entry) -> {
			Outline outline = entry.getOutline();
			outline.params.alpha = 1;
			if (entry.type != ExpireType.IMMEDIATE && entry.ticksTillRemoval < 0) {

				int prevTicks = entry.ticksTillRemoval + 1;
				float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / (float) entry.type.fadeTicks);
				float currentAlpha = 1 + (entry.ticksTillRemoval / (float) entry.type.fadeTicks);
				float alpha = MathHelper.lerp(Minecraft.getInstance()
					.getRenderPartialTicks(), lastAlpha, currentAlpha);

				outline.params.alpha = alpha * alpha * alpha;
				if (outline.params.alpha < 1 / 8f)
					return;
			}
			outline.render(ms, buffer);
		});
	}

	public enum ExpireType {
		IMMEDIATE(0), FADE(8), FADE_EXPAND(10);

		private int fadeTicks;

		private ExpireType(int fadeTicks) {
			this.fadeTicks = fadeTicks;
		}
	}

	private class OutlineEntry {

		private Outline outline;
		private int ticksTillRemoval;
		private ExpireType type;

		public OutlineEntry(Outline outline, ExpireType type) {
			this.outline = outline;
			this.type = type;
			ticksTillRemoval = 1;
		}

		public boolean isAlive() {
			return ticksTillRemoval >= -type.fadeTicks;
		}

		public Outline getOutline() {
			return outline;
		}

	}

}
