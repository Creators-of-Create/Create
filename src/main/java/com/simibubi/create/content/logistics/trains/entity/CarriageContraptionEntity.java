package com.simibubi.create.content.logistics.trains.entity;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionEntity extends OrientedContraptionEntity {

	public CarriageContraptionEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Override
	public boolean isControlledByLocalInstance() {
		return true;
	}

	public static CarriageContraptionEntity create(Level world, CarriageContraption contraption) {
		CarriageContraptionEntity entity =
			new CarriageContraptionEntity(AllEntityTypes.CARRIAGE_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.setInitialOrientation(contraption.getAssemblyDirection());
		entity.startAtInitialYaw();
		return entity;
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof CarriageContraption))
			return;
		int id = ((CarriageContraption) contraption).temporaryCarriageIdHolder;
		Carriage carriage = Create.RAILWAYS.carriageById.get(id); // TODO: thread breach
		if (carriage == null) {
			discard();
			return;
		}
		if (!level.isClientSide)
			return;

		xo = getX();
		yo = getY();
		zo = getZ();

		carriage.moveEntity(this);

		double distanceTo = position().distanceTo(new Vec3(xo, yo, zo));
		carriage.bogeys.getFirst()
			.updateAngles(distanceTo);
		if (carriage.isOnTwoBogeys())
			carriage.bogeys.getSecond()
				.updateAngles(distanceTo);

		if (carriage.train.derailed)
			spawnDerailParticles(carriage);

	}

	Vec3 derailParticleOffset = VecHelper.offsetRandomly(Vec3.ZERO, Create.RANDOM, 1.5f)
		.multiply(1, .25f, 1);

	private void spawnDerailParticles(Carriage carriage) {
		if (random.nextFloat() < 1 / 20f) {
			Vec3 v = position().add(derailParticleOffset);
			level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, v.x, v.y, v.z, 0, .04, 0);
		}
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

}
