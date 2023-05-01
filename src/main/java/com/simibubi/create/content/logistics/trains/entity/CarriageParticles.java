package com.simibubi.create.content.logistics.trains.entity;

import java.util.Random;

import com.simibubi.create.content.logistics.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CarriageParticles {

	CarriageContraptionEntity entity;
	boolean arrived;
	int depressurise;

	double prevMotion;
	LerpedFloat brakes;

	public CarriageParticles(CarriageContraptionEntity entity) {
		this.entity = entity;
		arrived = true;
		depressurise = 0;
		prevMotion = 0;
		brakes = LerpedFloat.linear();
	}

	public void tick(DimensionalCarriageEntity dce) {
		Minecraft mc = Minecraft.getInstance();
		Entity camEntity = mc.cameraEntity;
		if (camEntity == null)
			return;
		Vec3 leadingAnchor = dce.leadingAnchor();
		if (leadingAnchor == null || !leadingAnchor.closerThan(camEntity.position(), 64))
			return;

		Random r = entity.level.random;
		Vec3 contraptionMotion = entity.position()
			.subtract(entity.getPrevPositionVec());
		double length = contraptionMotion.length();
		if (arrived && length > 0.01f)
			arrived = false;
		arrived |= entity.isStalled();

		boolean stopped = length < .002f;
		if (stopped) {
			if (!arrived) {
				arrived = true;
				depressurise = (int) (20 * entity.getCarriage().train.accumulatedSteamRelease / 10f);
			}
		} else
			depressurise = 0;

		if (depressurise > 0)
			depressurise--;

		brakes.chase(prevMotion > length + length / 512f ? 1 : 0, .25f, Chaser.exp(.625f));
		brakes.tickChaser();
		prevMotion = length;

		Level level = entity.level;
		Vec3 position = entity.getPosition(0);
		float viewYRot = entity.getViewYRot(0);
		float viewXRot = entity.getViewXRot(0);
		int bogeySpacing = entity.getCarriage().bogeySpacing;

		for (CarriageBogey bogey : entity.getCarriage().bogeys) {
			if (bogey == null)
				continue;

			boolean spark = depressurise == 0 || depressurise > 10;

			float cutoff = length < 1 / 8f ? 0 : 1 / 8f;

			if (length > 1 / 6f)
				cutoff = Math.max(cutoff, brakes.getValue() * 1.15f);

			for (int j : Iterate.positiveAndNegative) {
				if (r.nextFloat() > cutoff && (spark || r.nextInt(4) == 0))
					continue;
				for (int i : Iterate.positiveAndNegative) {
					if (r.nextFloat() > cutoff && (spark || r.nextInt(4) == 0))
						continue;

					Vec3 v = Vec3.ZERO.add(j * 1.15, spark ? -.6f : .32, i);
					Vec3 m = Vec3.ZERO.add(j * (spark ? .5 : .25), spark ? .49 : -.29, 0);

					m = VecHelper.rotate(m, bogey.pitch.getValue(0), Axis.X);
					m = VecHelper.rotate(m, bogey.yaw.getValue(0), Axis.Y);

					v = VecHelper.rotate(v, bogey.pitch.getValue(0), Axis.X);
					v = VecHelper.rotate(v, bogey.yaw.getValue(0), Axis.Y);

					v = VecHelper.rotate(v, -viewYRot - 90, Axis.Y);
					v = VecHelper.rotate(v, viewXRot, Axis.X);
					v = VecHelper.rotate(v, -180, Axis.Y);

					v = v.add(0, 0, bogey.isLeading ? 0 : -bogeySpacing);
					v = VecHelper.rotate(v, 180, Axis.Y);
					v = VecHelper.rotate(v, -viewXRot, Axis.X);
					v = VecHelper.rotate(v, viewYRot + 90, Axis.Y);
					v = v.add(position);

					m = m.add(contraptionMotion.scale(.75f));

					level.addParticle(spark ? bogey.getStyle().contactParticle : bogey.getStyle().smokeParticle, v.x, v.y, v.z, m.x, m.y, m.z);
				}
			}
		}

	}

}
