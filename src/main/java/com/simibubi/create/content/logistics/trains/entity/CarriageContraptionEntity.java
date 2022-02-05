package com.simibubi.create.content.logistics.trains.entity;

import java.util.Collection;

import com.google.common.base.Strings;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsBlock;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

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
	public Component getContraptionName() {
		Carriage carriage = getCarriage();
		if (carriage != null)
			return carriage.train.name;
		Component contraptionName = super.getContraptionName();
		return contraptionName;
	}

	@Override
	public boolean startControlling(BlockPos controlsLocalPos) {
		Carriage carriage = getCarriage();
		if (carriage == null)
			return false;
		if (carriage.train.derailed)
			return false;

		Train train = carriage.train;
		if (train.runtime.getSchedule() != null && !train.runtime.paused)
			train.status.manualControls();
		train.navigation.cancelNavigation();
		train.runtime.paused = true;
		return true;
	}

	double navDistanceTotal = 0;

	@Override
	public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
		Carriage carriage = getCarriage();
		if (carriage == null)
			return false;
		if (carriage.train.derailed)
			return false;

		StructureBlockInfo info = contraption.getBlocks()
			.get(controlsLocalPos);
		Direction initialOrientation = getInitialOrientation().getCounterClockWise();
		boolean inverted = false;
		if (info != null && info.state.hasProperty(ControlsBlock.FACING))
			inverted = !info.state.getValue(ControlsBlock.FACING)
				.equals(initialOrientation);

		int targetSpeed = 0;
		if (heldControls.contains(0))
			targetSpeed++;
		if (heldControls.contains(1))
			targetSpeed--;

		int targetSteer = 0;
		if (heldControls.contains(2))
			targetSteer++;
		if (heldControls.contains(3))
			targetSteer--;

		if (inverted) {
			targetSpeed *= -1;
//			targetSteer *= -1;
		}

		boolean spaceDown = heldControls.contains(4);
		GlobalStation currentStation = carriage.train.getCurrentStation();
		if (currentStation != null && spaceDown) {
			player.displayClientMessage(new TextComponent("<i> Arrived at ").withStyle(ChatFormatting.GREEN)
				.append(new TextComponent(currentStation.name).withStyle(ChatFormatting.WHITE)), true);
			return true;
		}

		if (currentStation != null && targetSpeed != 0) {
			stationMessage = false;
			player.displayClientMessage(new TextComponent("<i> Departing from ").withStyle(ChatFormatting.YELLOW)
				.append(new TextComponent(currentStation.name).withStyle(ChatFormatting.WHITE)), true);
		}

		if (currentStation == null && targetSpeed >= 0) {
			Navigation nav = carriage.train.navigation;
			if (nav.destination != null) {
				if (!spaceDown)
					nav.cancelNavigation();
				if (spaceDown) {
					double f = (nav.distanceToDestination / navDistanceTotal);
					int progress = (int) (Mth.clamp(1 - ((1 - f) * (1 - f)), 0, 1) * 30);
					boolean arrived = progress == 0;
					TextComponent whiteComponent =
						new TextComponent(Strings.repeat("|", progress) + (arrived ? " ->" : " <-"));
					TextComponent greenComponent =
						new TextComponent((arrived ? "<- " : "-> ") + Strings.repeat("|", 30 - progress));
					int mixedColor = Color.mixColors(0xff_91EA44, 0xff_FFC244, progress / 30f);
					int targetColor = arrived ? 0xff_91EA44 : 0xff_ffffff;
					player.displayClientMessage(greenComponent.withStyle(st -> st.withColor(mixedColor))
						.append(whiteComponent.withStyle(st -> st.withColor(targetColor))), true);
					return true;
				}
			}

			GlobalStation lookAhead = nav.findNearestApproachable();
			if (lookAhead != null) {
				if (spaceDown) {
					nav.startNavigation(lookAhead, false);
					navDistanceTotal = nav.distanceToDestination;
					return true;
				}
				if (level.isClientSide)
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> displayApproachStationMessage(lookAhead));
			} else {
				if (level.isClientSide)
					DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::cleanUpApproachStationMessage);
			}
		}

		carriage.train.manualSteer =
			targetSteer < 0 ? SteerDirection.RIGHT : targetSteer > 0 ? SteerDirection.LEFT : SteerDirection.NONE;
		carriage.train.targetSpeed = Train.topSpeed * targetSpeed;
		if (inverted ^ targetSpeed < 0)
			carriage.train.targetSpeed /= 8;
		boolean counteringAcceleration = Math.abs(Math.signum(targetSpeed) - Math.signum(carriage.train.speed)) > 1.5f;
		carriage.train.manualTick = true;
		carriage.train.approachTargetSpeed(counteringAcceleration ? 2 : 1);
		return true;
	}

	boolean stationMessage = false;

	@OnlyIn(Dist.CLIENT)
	private void displayApproachStationMessage(GlobalStation station) {
		Minecraft instance = Minecraft.getInstance();
		instance.player.displayClientMessage(Lang.translate("contraption.controls.approach_station",
			instance.options.keyJump.getTranslatedKeyMessage(), station.name), true);
		stationMessage = true;
	}

	@OnlyIn(Dist.CLIENT)
	private void cleanUpApproachStationMessage() {
		if (!stationMessage)
			return;
		Minecraft instance = Minecraft.getInstance();
		instance.player.displayClientMessage(new TextComponent(""), true);
		stationMessage = false;
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof CarriageContraption))
			return;
		Carriage carriage = getCarriage();
		if (carriage == null) {
			discard();
			return;
		}

		tickActors();

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

	public Carriage getCarriage() {
		int id = ((CarriageContraption) contraption).temporaryCarriageIdHolder;
		Carriage carriage = Create.RAILWAYS.carriageById.get(id); // TODO: thread breach
		return carriage;
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
