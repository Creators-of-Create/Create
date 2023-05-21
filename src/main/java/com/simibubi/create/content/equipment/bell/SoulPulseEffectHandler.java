package com.simibubi.create.content.equipment.bell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SoulPulseEffectHandler {

	private List<SoulPulseEffect> pulses;
	private Set<BlockPos> occupied;

	public SoulPulseEffectHandler() {
		pulses = new ArrayList<>();
		occupied = new HashSet<>();
	}

	public void tick(Level world) {
		for (SoulPulseEffect pulse : pulses) {
			List<BlockPos> spawns = pulse.tick(world);
			if (spawns == null)
				continue;

			if (pulse.canOverlap()) {
				for (BlockPos pos : spawns) {
					pulse.spawnParticles(world, pos);
				}
			} else {
				for (BlockPos pos : spawns) {
					if (occupied.contains(pos))
						continue;

					pulse.spawnParticles(world, pos);
					pulse.added.add(pos);
					occupied.add(pos);
				}
			}
		}

		for (SoulPulseEffect pulse : pulses) {
			if (pulse.finished() && !pulse.canOverlap())
				occupied.removeAll(pulse.added);
		}
		pulses.removeIf(SoulPulseEffect::finished);
	}

	public void addPulse(SoulPulseEffect pulse) {
		pulses.add(pulse);
	}

	public void refresh() {
		pulses.clear();
		occupied.clear();
	}

}
