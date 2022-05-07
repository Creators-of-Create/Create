package com.simibubi.create.content.logistics.trains.entity;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.base.Strings;
import com.simibubi.create.AllEntityDataSerializers;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsBlock;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.GlobalStation;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionEntity extends OrientedContraptionEntity {

	private static final EntityDataAccessor<CarriageSyncData> CARRIAGE_DATA =
		SynchedEntityData.defineId(CarriageContraptionEntity.class, AllEntityDataSerializers.CARRIAGE_DATA);
	private static final EntityDataAccessor<Optional<UUID>> TRACK_GRAPH =
		SynchedEntityData.defineId(CarriageContraptionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> SCHEDULED =
		SynchedEntityData.defineId(CarriageContraptionEntity.class, EntityDataSerializers.BOOLEAN);

	public UUID trainId;
	public int carriageIndex;

	private Carriage carriage;
	public boolean validForRender;
	public boolean movingBackwards;

	public boolean leftTickingChunks;
	public boolean firstPositionUpdate;

	public CarriageContraptionEntity(EntityType<?> type, Level world) {
		super(type, world);
		validForRender = false;
		firstPositionUpdate = true;
	}

	@Override
	public boolean isControlledByLocalInstance() {
		return true;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(CARRIAGE_DATA, new CarriageSyncData());
		entityData.define(TRACK_GRAPH, Optional.empty());
		entityData.define(SCHEDULED, false);
	}

	public void syncCarriage() {
		CarriageSyncData carriageData = getCarriageData();
		if (carriageData == null)
			return;
		if (carriage == null)
			return;
		carriageData.update(this, carriage);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);

		if (!level.isClientSide)
			return;

		if (TRACK_GRAPH.equals(key))
			updateTrackGraph();

		if (CARRIAGE_DATA.equals(key)) {
			CarriageSyncData carriageData = getCarriageData();
			if (carriageData == null)
				return;
			if (carriage == null)
				return;
			carriageData.apply(this, carriage);
		}
	}

	private CarriageSyncData getCarriageData() {
		return entityData.get(CARRIAGE_DATA);
	}

	public boolean hasSchedule() {
		return entityData.get(SCHEDULED);
	}

	public static CarriageContraptionEntity create(Level world, CarriageContraption contraption) {
		CarriageContraptionEntity entity =
			new CarriageContraptionEntity(AllEntityTypes.CARRIAGE_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.setInitialOrientation(contraption.getAssemblyDirection()
			.getClockWise());
		entity.startAtInitialYaw();
		return entity;
	}

	@Override
	public void tick() {
		super.tick();

		if (contraption instanceof CarriageContraption cc)
			for (Entity entity : getPassengers()) {
				if (entity instanceof Player)
					continue;
				BlockPos seatOf = cc.getSeatOf(entity.getUUID());
				if (seatOf == null)
					continue;
				if (cc.conductorSeats.get(seatOf) == null)
					continue;
				alignPassenger(entity);
			}
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof CarriageContraption cc))
			return;
		if (carriage == null) {
			if (level.isClientSide) {
				Train train = Create.RAILWAYS.sided(level).trains.get(trainId);
				if (train == null || train.carriages.size() <= carriageIndex)
					return;
				carriage = train.carriages.get(carriageIndex);
				if (carriage != null)
					carriage.entity = new WeakReference<>(this);
				updateTrackGraph();
			} else
				discard();
			return;
		}

		tickActors();
		contraption.stalled = false;
		for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			context.stall = false;
		}

		CarriageSyncData carriageData = getCarriageData();

		if (!level.isClientSide) {
			entityData.set(SCHEDULED, carriage.train.runtime.getSchedule() != null);
			boolean shouldCarriageSyncThisTick =
				carriage.train.shouldCarriageSyncThisTick(level.getGameTime(), getType().updateInterval());
			if (shouldCarriageSyncThisTick && carriageData.isDirty()) {
				entityData.set(CARRIAGE_DATA, null);
				entityData.set(CARRIAGE_DATA, carriageData);
				carriageData.setDirty(false);
			}
			return;
		}

		if (!carriage.pointsInitialised)
			return;

		carriageData.approach(this, carriage, 1f / getType().updateInterval());

		if (!carriage.train.derailed)
			carriage.updateContraptionAnchors();

		xo = getX();
		yo = getY();
		zo = getZ();

		carriage.alignEntity(this);

		double distanceTo = 0;
		if (!firstPositionUpdate) {
			Vec3 diff = position().subtract(xo, yo, zo);
			Vec3 relativeDiff = VecHelper.rotate(diff, yaw, Axis.Y);
			double signum = Math.signum(-relativeDiff.x);
			distanceTo = diff.length() * signum;
			movingBackwards = signum < 0;
		}

		carriage.bogeys.getFirst()
			.updateAngles(this, distanceTo);
		if (carriage.isOnTwoBogeys())
			carriage.bogeys.getSecond()
				.updateAngles(this, distanceTo);

		if (carriage.train.derailed)
			spawnDerailParticles(carriage);

		firstPositionUpdate = false;
		validForRender = true;
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
	public void onClientRemoval() {
		super.onClientRemoval();
		entityData.set(CARRIAGE_DATA, new CarriageSyncData());
		if (carriage != null) {
			carriage.pointsInitialised = false;
			carriage.leadingBogey().couplingAnchors = Couple.create(null, null);
			carriage.trailingBogey().couplingAnchors = Couple.create(null, null);
		}
		firstPositionUpdate = true;
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);
		compound.putUUID("TrainId", trainId);
		compound.putInt("CarriageIndex", carriageIndex);
	}

	@Override
	protected void readAdditional(CompoundTag compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);
		trainId = compound.getUUID("TrainId");
		carriageIndex = compound.getInt("CarriageIndex");
	}

	@Override
	public Component getContraptionName() {
		if (carriage != null)
			return carriage.train.name;
		Component contraptionName = super.getContraptionName();
		return contraptionName;
	}

	public Couple<Boolean> checkConductors() {
		Couple<Boolean> sides = Couple.create(false, false);
		if (!(contraption instanceof CarriageContraption cc))
			return sides;

		sides.setFirst(cc.blazeBurnerConductors.getFirst());
		sides.setSecond(cc.blazeBurnerConductors.getSecond());

		for (Entity entity : getPassengers()) {
			BlockPos seatOf = cc.getSeatOf(entity.getUUID());
			if (seatOf == null)
				continue;
			Couple<Boolean> validSides = cc.conductorSeats.get(seatOf);
			if (validSides == null)
				continue;
			sides.setFirst(sides.getFirst() || validSides.getFirst());
			sides.setSecond(sides.getSecond() || validSides.getSecond());
		}

		return sides;
	}

	@Override
	public boolean startControlling(BlockPos controlsLocalPos, Player player) {
		if (carriage == null)
			return false;
		if (carriage.train.derailed)
			return false;
		if (carriage.train.heldForAssembly) {
			player.displayClientMessage(Lang.translate("schedule.train_still_assembling"), true);
			return false;
		}

		Train train = carriage.train;
		if (train.runtime.getSchedule() != null && !train.runtime.paused)
			train.status.manualControls();
		train.navigation.cancelNavigation();
		train.runtime.paused = true;
		train.navigation.waitingForSignal = null;
		return true;
	}
	
	@Override
	public Component getDisplayName() {
		if (carriage == null)
			return Lang.translate("train");
		return carriage.train.name;
	}

	double navDistanceTotal = 0;

	@Override
	public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
		if (carriage == null)
			return false;
		if (carriage.train.derailed)
			return false;
		if (level.isClientSide)
			return true;
		if (player.isSpectator())
			return false;
		if (!toGlobalVector(VecHelper.getCenterOf(controlsLocalPos), 1).closerThan(player.position(), 8))
			return false;
		if (heldControls.contains(5))
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
			targetSteer *= -1;
		}

		boolean slow = inverted ^ targetSpeed < 0;
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

		if (currentStation == null) {
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
					int mixedColor = Color.mixColors(0x00_91EA44, 0x00_FFC244, progress / 30f);
					int targetColor = arrived ? 0x00_91EA44 : 0x00_ffffff;
					player.displayClientMessage(greenComponent.withStyle(st -> st.withColor(mixedColor))
						.append(whiteComponent.withStyle(st -> st.withColor(targetColor))), true);
					return true;
				}
			}

			double directedSpeed = targetSpeed != 0 ? targetSpeed : carriage.train.speed;
			GlobalStation lookAhead = nav.findNearestApproachable(
				!carriage.train.doubleEnded || (directedSpeed != 0 ? directedSpeed > 0 : !inverted));

			if (lookAhead != null) {
				if (spaceDown) {
					nav.startNavigation(lookAhead, -1, false);
					navDistanceTotal = nav.distanceToDestination;
					return true;
				}
				displayApproachStationMessage(player, lookAhead);
			} else
				cleanUpApproachStationMessage(player);
		}

		carriage.train.manualSteer =
			targetSteer < 0 ? SteerDirection.RIGHT : targetSteer > 0 ? SteerDirection.LEFT : SteerDirection.NONE;
		double topSpeed = AllConfigs.SERVER.trains.getTopSpeedMPT();
		carriage.train.targetSpeed = topSpeed * targetSpeed;
		if (slow)
			carriage.train.targetSpeed /= 8;
		boolean counteringAcceleration = Math.abs(Math.signum(targetSpeed) - Math.signum(carriage.train.speed)) > 1.5f;
		carriage.train.manualTick = true;
		carriage.train.approachTargetSpeed(counteringAcceleration ? 2 : 1);
		return true;
	}

	boolean stationMessage = false;

	private void displayApproachStationMessage(Player player, GlobalStation station) {
		player.displayClientMessage(
			Lang.translate("contraption.controls.approach_station", new KeybindComponent("key.jump"), station.name),
			true);
		stationMessage = true;
	}

	private void cleanUpApproachStationMessage(Player player) {
		if (!stationMessage)
			return;
		player.displayClientMessage(new TextComponent(""), true);
		stationMessage = false;
	}

	private void updateTrackGraph() {
		if (carriage == null)
			return;
		Optional<UUID> optional = entityData.get(TRACK_GRAPH);
		if (optional.isEmpty()) {
			carriage.train.graph = null;
			carriage.train.derailed = true;
			return;
		}

		TrackGraph graph = CreateClient.RAILWAYS.sided(level).trackNetworks.get(optional.get());
		if (graph == null)
			return;
		carriage.train.graph = graph;
		carriage.train.derailed = false;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	public Carriage getCarriage() {
		return carriage;
	}

	public void setCarriage(Carriage carriage) {
		this.carriage = carriage;
		this.trainId = carriage.train.id;
		this.carriageIndex = carriage.train.carriages.indexOf(carriage);
	}

	public void setGraph(@Nullable UUID graphId) {
		entityData.set(TRACK_GRAPH, Optional.ofNullable(graphId));
		prevPosInvalid = true;
	}

}