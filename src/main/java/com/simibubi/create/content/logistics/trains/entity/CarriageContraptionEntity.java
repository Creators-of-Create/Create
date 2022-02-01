package com.simibubi.create.content.logistics.trains.entity;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;

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
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

}
