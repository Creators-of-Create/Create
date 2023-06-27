package com.simibubi.create.foundation.utility.ghost;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GhostBlocks {

	public static double getBreathingAlpha() {
		double period = 2500;
		double timer = System.currentTimeMillis() % period;
		double offset = Mth.cos((float) ((2d/period) * Math.PI * timer));
		return 0.55d - 0.2d * offset;
	}

	final Map<Object, Entry> ghosts;

	public GhostBlockParams showGhostState(Object slot, BlockState state) {
		return showGhostState(slot, state, 1);
	}

	public GhostBlockParams showGhostState(Object slot, BlockState state, int ttl) {
		Entry e = refresh(slot, GhostBlockRenderer.transparent(), GhostBlockParams.of(state), ttl);
		return e.params;
	}

	public GhostBlockParams showGhost(Object slot, GhostBlockRenderer ghost, GhostBlockParams params, int ttl) {
		Entry e = refresh(slot, ghost, params, ttl);
		return e.params;
	}

	private Entry refresh(Object slot, GhostBlockRenderer ghost, GhostBlockParams params, int ttl) {
		if (!ghosts.containsKey(slot))
			ghosts.put(slot, new Entry(ghost, params, ttl));

		Entry e = ghosts.get(slot);
		e.ticksToLive = ttl;
		e.params = params;
		e.ghost = ghost;
		return e;
	}

	public GhostBlocks() {
		ghosts = new HashMap<>();
	}

	public void tickGhosts() {
		ghosts.forEach((slot, entry) -> entry.ticksToLive--);
		ghosts.entrySet().removeIf(e -> !e.getValue().isAlive());
	}

	public void renderAll(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		ghosts.forEach((slot, entry) -> {
			GhostBlockRenderer ghost = entry.ghost;
			ghost.render(ms, buffer, camera, entry.params);
		});
	}

	static class Entry {

		private GhostBlockRenderer ghost;
		private GhostBlockParams params;
		private int ticksToLive;

		public Entry(GhostBlockRenderer ghost, GhostBlockParams params) {
			this(ghost, params, 1);
		}

		public Entry(GhostBlockRenderer ghost, GhostBlockParams params, int ttl) {
			this.ghost = ghost;
			this.params = params;
			this.ticksToLive = ttl;
		}

		public boolean isAlive() {
			return ticksToLive >= 0;
		}
	}
}
