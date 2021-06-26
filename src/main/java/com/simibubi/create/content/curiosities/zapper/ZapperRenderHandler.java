package com.simibubi.create.content.curiosities.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.CreateClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class ZapperRenderHandler extends ShootableGadgetRenderHandler {

	public List<LaserBeam> cachedBeams;

	@Override
	protected boolean appliesTo(ItemStack stack) {
		return stack.getItem() instanceof ZapperItem;
	}

	@Override
	public void tick() {
		super.tick();

		if (cachedBeams == null)
			cachedBeams = new LinkedList<>();

		cachedBeams.removeIf(b -> b.itensity < .1f);
		if (cachedBeams.isEmpty())
			return;

		cachedBeams.forEach(beam -> {
			CreateClient.OUTLINER.endChasingLine(beam, beam.start, beam.end, 1 - beam.itensity)
				.disableNormals()
				.colored(0xffffff)
				.lineWidth(beam.itensity * 1 / 8f);
		});

		cachedBeams.forEach(b -> b.itensity *= .6f);
	}

	@Override
	protected void transformTool(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {
		ms.translate(flip * -0.1f, 0.1f, -0.4f);
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(flip * 5.0F));
	}

	@Override
	protected void transformHand(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {}

	@Override
	protected void playSound(Hand hand, BlockPos position) {
		float pitch = hand == Hand.MAIN_HAND ? 0.1f : 0.9f;
		Minecraft mc = Minecraft.getInstance();
		AllSoundEvents.WORLDSHAPER_PLACE.play(mc.world, mc.player, position, 0.1f, pitch);
	}

	public void addBeam(LaserBeam beam) {
		Random r = new Random();
		double x = beam.end.x;
		double y = beam.end.y;
		double z = beam.end.z;
		ClientWorld world = Minecraft.getInstance().world;
		Supplier<Double> randomSpeed = () -> (r.nextDouble() - .5d) * .2f;
		Supplier<Double> randomOffset = () -> (r.nextDouble() - .5d) * .2f;
		for (int i = 0; i < 10; i++) {
			world.addParticle(ParticleTypes.END_ROD, x, y, z, randomSpeed.get(), randomSpeed.get(), randomSpeed.get());
			world.addParticle(ParticleTypes.FIREWORK, x + randomOffset.get(), y + randomOffset.get(),
				z + randomOffset.get(), 0, 0, 0);
		}

		cachedBeams.add(beam);
	}

	public static class LaserBeam {
		float itensity;
		Vector3d start;
		Vector3d end;

		public LaserBeam(Vector3d start, Vector3d end) {
			this.start = start;
			this.end = end;
			itensity = 1;
		}
	}

}
