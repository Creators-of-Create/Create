package com.simibubi.create.content.equipment.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllSoundEvents;

import net.createmod.catnip.CatnipClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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
			CatnipClient.OUTLINER.endChasingLine(beam, beam.start, beam.end, 1 - beam.itensity, false)
				.disableLineNormals()
				.colored(0xffffff)
				.lineWidth(beam.itensity * 1 / 8f);
		});

		cachedBeams.forEach(b -> b.itensity *= .6f);
	}

	@Override
	protected void transformTool(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {
		ms.translate(flip * -0.1f, 0.1f, -0.4f);
		ms.mulPose(Vector3f.YP.rotationDegrees(flip * 5.0F));
	}

	@Override
	protected void transformHand(PoseStack ms, float flip, float equipProgress, float recoil, float pt) {}

	@Override
	protected void playSound(InteractionHand hand, Vec3 position) {
		float pitch = hand == InteractionHand.MAIN_HAND ? 0.1f : 0.9f;
		Minecraft mc = Minecraft.getInstance();
		AllSoundEvents.WORLDSHAPER_PLACE.play(mc.level, mc.player, position, 0.1f, pitch);
	}

	public void addBeam(LaserBeam beam) {
		Random r = new Random();
		double x = beam.end.x;
		double y = beam.end.y;
		double z = beam.end.z;
		ClientLevel world = Minecraft.getInstance().level;
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
		Vec3 start;
		Vec3 end;

		public LaserBeam(Vec3 start, Vec3 end) {
			this.start = start;
			this.end = end;
			itensity = 1;
		}
	}

}
