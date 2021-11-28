package com.simibubi.create.foundation.ponder.instruction;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

public class EmitParticlesInstruction extends TickingInstruction {

	private Vec3 anchor;
	private Emitter emitter;
	private float runsPerTick;

	@FunctionalInterface
	public static interface Emitter {

		public static <T extends ParticleOptions> Emitter simple(T data, Vec3 motion) {
			return (w, x, y, z) -> w.addParticle(data, x, y, z, motion.x, motion.y, motion.z);
		}

		public static <T extends ParticleOptions> Emitter withinBlockSpace(T data, Vec3 motion) {
			return (w, x, y, z) -> w.addParticle(data, Math.floor(x) + Create.RANDOM.nextFloat(),
					Math.floor(y) + Create.RANDOM.nextFloat(), Math.floor(z) + Create.RANDOM.nextFloat(), motion.x,
					motion.y, motion.z);
		}

		static ParticleEngine paticleManager() {
			return Minecraft.getInstance().particleEngine;
		}

		public void create(PonderWorld world, double x, double y, double z);

	}

	public EmitParticlesInstruction(Vec3 anchor, Emitter emitter, float runsPerTick, int ticks) {
		super(false, ticks);
		this.anchor = anchor;
		this.emitter = emitter;
		this.runsPerTick = runsPerTick;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int runs = (int) runsPerTick;
		if (Create.RANDOM.nextFloat() < (runsPerTick - runs))
			runs++;
		for (int i = 0; i < runs; i++)
			emitter.create(scene.getWorld(), anchor.x, anchor.y, anchor.z);
	}

}
