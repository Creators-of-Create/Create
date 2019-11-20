package com.simibubi.create.modules.contraptions.receivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

public class EncasedFanParticleHandler {

	public final Map<Block, List<FanEffect>> effects = new HashMap<>();
	
	public EncasedFanParticleHandler() {
		initEffects();
	}
	
	private void initEffects() {
		List<FanEffect> standardFX = new ArrayList<>(2);
		standardFX.add(new FanEffect(ParticleTypes.BUBBLE_POP, 1 / 4f, 1 / 4f, 1 / 8f, 1));
		standardFX.add(new FanEffect(new RedstoneParticleData(1, 1, 1, 1), 1 / 2f, 1 / 32f, 1/16f, 512f));
		effects.put(Blocks.AIR, standardFX);

		List<FanEffect> waterFX = new ArrayList<>(2);
		waterFX.add(new FanEffect(new BlockParticleData(ParticleTypes.BLOCK, Blocks.WATER.getDefaultState()), 1 / 4f,
				1 / 2f, 1 / 4f, 1));
		waterFX.add(new FanEffect(ParticleTypes.SPLASH, 1 / 4f, 1 / 2f, 0.5f, 1));
		effects.put(Blocks.WATER, waterFX);

		List<FanEffect> fireFX = new ArrayList<>(2);
		fireFX.add(new FanEffect(ParticleTypes.LARGE_SMOKE, 1 / 4f, 1 / 8f, 0.125f, .5f));
		fireFX.add(new FanEffect(ParticleTypes.FLAME, 1 / 4f, 1 / 8f, 1 / 32f, 1 / 256f));
		effects.put(Blocks.FIRE, fireFX);

		List<FanEffect> lavaFX = new ArrayList<>(3);
		lavaFX.add(new FanEffect(new BlockParticleData(ParticleTypes.BLOCK, Blocks.LAVA.getDefaultState()), 1 / 4f,
				1 / 2f, 1 / 4f, 1));
		lavaFX.add(new FanEffect(ParticleTypes.LAVA, 1 / 2f, 1 / 32f, 0, .25f));
		lavaFX.add(new FanEffect(ParticleTypes.FLAME, 1 / 4f, 1 / 32f, 1 / 32f, 1 / 256f));
		effects.put(Blocks.LAVA, lavaFX);
	}
	
	public void makeParticles(EncasedFanTileEntity te) {
		Direction direction = te.getAirFlow();
		Vec3i directionVec = direction.getDirectionVec();

		boolean hasFx = false;
		BlockState frontBlock = te.frontBlock;
		if (frontBlock != null) {
			if (effects.containsKey(frontBlock.getBlock())) {
				hasFx = true;
				for (FanEffect fx : effects.get(frontBlock.getBlock()))
					fx.render(directionVec, true, te);
			}
		}

		if (!hasFx)
			for (FanEffect fx : effects.get(Blocks.AIR))
				fx.render(directionVec, true, te);

		for (FanEffect fx : effects.get(Blocks.AIR))
			fx.render(directionVec, false, te);
	}
	
	protected static class FanEffect {
		private IParticleData particle;
		private float density;
		private float chance;
		private float spread;
		private float speed;
		private Random r;

		public FanEffect(IParticleData particle, float density, float chance, float spread, float speed) {
			r = new Random();
			this.particle = particle;
			this.density = density;
			this.chance = chance;
			this.spread = spread;
			this.speed = speed;
		}

		public void render(Vec3i directionVec, boolean front, EncasedFanTileEntity te) {
			render(directionVec, front ? .5f : -te.pullDistance, front ? te.pushDistance : -.5f, te);
		}

		private void render(Vec3i directionVec, float start, float end, EncasedFanTileEntity te) {
			float x = directionVec.getX();
			float y = directionVec.getY();
			float z = directionVec.getZ();
			float speed = this.speed * Math.abs(te.getSpeed()) / 512f;

			for (float offset = start; offset < end; offset += density) {
				if (r.nextFloat() > chance)
					continue;
				float xs = rollOffset() * spread;
				float ys = rollOffset() * spread;
				float zs = rollOffset() * spread;
				float xs2 = rollOffset() * spread;
				float ys2 = rollOffset() * spread;
				float zs2 = rollOffset() * spread;
				te.getWorld().addParticle(particle, te.getPos().getX() + .5f + x * offset + xs2,
						te.getPos().getY() + .5f + y * offset + ys2, te.getPos().getZ() + .5f + z * offset + zs2,
						x * speed + xs, y * speed + ys, z * speed + zs);
			}
		}

		private float rollOffset() {
			return (r.nextFloat() - .5f) * 2;
		}
	}

}
