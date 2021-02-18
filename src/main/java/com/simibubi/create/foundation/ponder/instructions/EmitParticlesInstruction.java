package com.simibubi.create.foundation.ponder.instructions;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.Vec3d;

public class EmitParticlesInstruction extends TickingInstruction {

	private Vec3d anchor;
	private Emitter emitter;
	private float runsPerTick;

	@FunctionalInterface
	public static interface Emitter {

		public static <T extends IParticleData> Emitter simple(T data, Vec3d motion) {
			return (w, x, y, z) -> w.addParticle(data, x, y, z, motion.x, motion.y, motion.z);
		}

		public static <T extends IParticleData> Emitter withinBlockSpace(T data, Vec3d motion) {
			return (w, x, y, z) -> w.addParticle(data, Math.floor(x) + Create.random.nextFloat(),
				Math.floor(y) + Create.random.nextFloat(), Math.floor(z) + Create.random.nextFloat(), motion.x,
				motion.y, motion.z);
		}

		static ParticleManager paticleManager() {
			return Minecraft.getInstance().particles;
		}

		public void create(PonderWorld world, double x, double y, double z);

	}

	public EmitParticlesInstruction(Vec3d anchor, Emitter emitter, float runsPerTick, int ticks) {
		super(false, ticks);
		this.anchor = anchor;
		this.emitter = emitter;
		this.runsPerTick = runsPerTick;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int runs = (int) runsPerTick;
		if (Create.random.nextFloat() < (runsPerTick - runs))
			runs++;
		for (int i = 0; i < runs; i++)
			emitter.create(scene.getWorld(), anchor.x, anchor.y, anchor.z);
	}

}
