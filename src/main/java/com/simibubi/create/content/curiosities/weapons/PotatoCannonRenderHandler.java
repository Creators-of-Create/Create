package com.simibubi.create.content.curiosities.weapons;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetRenderHandler;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class PotatoCannonRenderHandler extends ShootableGadgetRenderHandler {

	private float nextPitch;

	@Override
	protected void playSound(Hand hand, Vector3d position) {
		PotatoProjectileEntity.playLaunchSound(Minecraft.getInstance().level, position, nextPitch);
	}

	@Override
	protected boolean appliesTo(ItemStack stack) {
		return AllItems.POTATO_CANNON.get()
			.isCannon(stack);
	}

	public void beforeShoot(float nextPitch, Vector3d location, Vector3d motion, ItemStack stack) {
		this.nextPitch = nextPitch;
		if (stack.isEmpty())
			return;
		ClientWorld world = Minecraft.getInstance().level;
		for (int i = 0; i < 2; i++) {
			Vector3d m = VecHelper.offsetRandomly(motion.scale(0.1f), Create.RANDOM, .025f);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), location.x, location.y, location.z, m.x,
				m.y, m.z);

			Vector3d m2 = VecHelper.offsetRandomly(motion.scale(2f), Create.RANDOM, .5f);
			world.addParticle(new AirParticleData(1, 1 / 4f), location.x, location.y, location.z, m2.x, m2.y, m2.z);
		}
	}

	@Override
	protected void transformTool(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {
		ms.translate(flip * -.1f, 0, .14f);
		ms.scale(.75f, .75f, .75f);
		MatrixTransformStack.of(ms)
			.rotateX(recoil * 80);
	}

	@Override
	protected void transformHand(MatrixStack ms, float flip, float equipProgress, float recoil, float pt) {
		ms.translate(flip * -.09, -.275, -.25);
		MatrixTransformStack.of(ms)
			.rotateZ(flip * -10);
	}

}
