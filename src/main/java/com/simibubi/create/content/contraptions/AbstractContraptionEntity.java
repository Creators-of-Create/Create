package com.simibubi.create.content.contraptions;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsStopControllingPacket;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.data.ContraptionSyncLimiting;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.mixin.accessor.ServerLevelAccessor;

import io.netty.handler.codec.DecoderException;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public abstract class AbstractContraptionEntity extends Entity implements IEntityWithComplexSpawn {

	private static final EntityDataAccessor<Boolean> STALLED =
		SynchedEntityData.defineId(AbstractContraptionEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> CONTROLLED_BY =
		SynchedEntityData.defineId(AbstractContraptionEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	public final Map<Entity, MutableInt> collidingEntities;

	protected Contraption contraption;
	protected boolean initialized;
	protected boolean prevPosInvalid;
	private boolean skipActorStop;

	/*
	 * staleTicks are a band-aid to prevent a frame or two of missing blocks between
	 * contraption discard and off-thread block placement on disassembly
	 *
	 * FIXME this timeout should be longer but then also cancelled early based on a
	 * chunk rebuild listener
	 */
	public int staleTicks = 3;

	public AbstractContraptionEntity(EntityType<?> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
		prevPosInvalid = true;
		collidingEntities = new IdentityHashMap<>();
	}

	protected void setContraption(Contraption contraption) {
		this.contraption = contraption;
		if (contraption == null)
			return;
		if (level().isClientSide)
			return;
		contraption.onEntityCreated(this);
	}

	@Override
	public void move(MoverType pType, Vec3 pPos) {
		if (pType == MoverType.SHULKER)
			return;
		if (pType == MoverType.SHULKER_BOX)
			return;
		if (pType == MoverType.PISTON)
			return;
		super.move(pType, pPos);
	}

	public boolean supportsTerrainCollision() {
		return contraption instanceof TranslatingContraption && !(contraption instanceof ElevatorContraption);
	}

	protected void contraptionInitialize() {
		contraption.onEntityInitialize(level(), this);
		initialized = true;
	}

	public boolean collisionEnabled() {
		return true;
	}

	public void registerColliding(Entity collidingEntity) {
		collidingEntities.put(collidingEntity, new MutableInt());
	}

	public void addSittingPassenger(Entity passenger, int seatIndex) {
		for (Entity entity : getPassengers()) {
			BlockPos seatOf = contraption.getSeatOf(entity.getUUID());
			if (seatOf != null && seatOf.equals(contraption.getSeats()
				.get(seatIndex))) {
				if (entity instanceof Player)
					return;
				if (!(passenger instanceof Player))
					return;
				entity.stopRiding();
			}
		}
		passenger.startRiding(this, true);
		if (passenger instanceof TamableAnimal ta)
			ta.setInSittingPose(true);
		if (level().isClientSide)
			return;
		contraption.getSeatMapping()
			.put(passenger.getUUID(), seatIndex);

		CatnipServices.NETWORK.sendToClientsTrackingEntity(this, new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping()));
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vec3 transformedVector = getPassengerPosition(passenger, 1);
		super.removePassenger(passenger);
		if (passenger instanceof TamableAnimal ta)
			ta.setInSittingPose(false);
		if (level().isClientSide)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.getUUID());

		CatnipServices.NETWORK.sendToClientsTrackingEntity(this,
				new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping(), passenger.getId()));
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity entityLiving) {
		Vec3 position = super.getDismountLocationForPassenger(entityLiving);
		CompoundTag data = entityLiving.getPersistentData();
		if (!data.contains("ContraptionDismountLocation"))
			return position;

		position = VecHelper.readNBT(data.getList("ContraptionDismountLocation", Tag.TAG_DOUBLE));
		data.remove("ContraptionDismountLocation");
		entityLiving.setOnGround(false);

		if (!data.contains("ContraptionMountLocation"))
			return position;

		Vec3 prevPosition = VecHelper.readNBT(data.getList("ContraptionMountLocation", Tag.TAG_DOUBLE));
		data.remove("ContraptionMountLocation");
		if (entityLiving instanceof Player player && !prevPosition.closerThan(position, 5000))
			AllAdvancements.LONG_TRAVEL.awardTo(player);
		return position;
	}

	@Override
	public void positionRider(Entity passenger, MoveFunction callback) {
		if (!hasPassenger(passenger))
			return;
		Vec3 transformedVector = getPassengerPosition(passenger, 1);
		if (transformedVector == null)
			return;

		float offset = -1 / 8f;
		if (passenger instanceof AbstractContraptionEntity)
			offset = 0.0f;
		callback.accept(passenger, transformedVector.x,
			transformedVector.y + SeatEntity.getCustomEntitySeatOffset(passenger) + offset, transformedVector.z);
	}

	public Vec3 getPassengerPosition(Entity passenger, float partialTicks) {
		if (contraption == null)
			return null;

		UUID id = passenger.getUUID();
		if (passenger instanceof OrientedContraptionEntity) {
			BlockPos localPos = contraption.getBearingPosOf(id);
			if (localPos != null)
				return toGlobalVector(VecHelper.getCenterOf(localPos), partialTicks)
					.add(VecHelper.getCenterOf(BlockPos.ZERO))
					.subtract(.5f, 1, .5f);
		}

		AABB bb = passenger.getBoundingBox();
		double ySize = bb.getYsize();
		BlockPos seat = contraption.getSeatOf(id);
		if (seat == null)
			return null;

		Vec3 transformedVector = toGlobalVector(Vec3.atLowerCornerOf(seat)
			.add(.5,
				-passenger.getVehicleAttachmentPoint(this).y + ySize + .125
					- SeatEntity.getCustomEntitySeatOffset(passenger),
				.5),
			partialTicks).add(VecHelper.getCenterOf(BlockPos.ZERO))
				.subtract(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean canAddPassenger(@NotNull Entity pPassenger) {
		if (pPassenger instanceof OrientedContraptionEntity)
			return true;
		return contraption.getSeatMapping()
			.size() < contraption.getSeats()
			.size();
	}

	public Component getContraptionName() {
		return getName();
	}

	public Optional<UUID> getControllingPlayer() {
		return entityData.get(CONTROLLED_BY);
	}

	public void setControllingPlayer(@Nullable UUID playerId) {
		entityData.set(CONTROLLED_BY, Optional.ofNullable(playerId));
	}

	public boolean startControlling(BlockPos controlsLocalPos, Player player) {
		return false;
	}

	public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
		return true;
	}

	public void stopControlling(BlockPos controlsLocalPos) {
		getControllingPlayer().map(level()::getPlayerByUUID)
			.map(p -> (p instanceof ServerPlayer) ? ((ServerPlayer) p) : null)
			.ifPresent(p -> CatnipServices.NETWORK.sendToClient(p, ControlsStopControllingPacket.INSTANCE));
		setControllingPlayer(null);
	}

	public boolean handlePlayerInteraction(Player player, BlockPos localPos, Direction side,
										   InteractionHand interactionHand) {
		int indexOfSeat = contraption.getSeats()
			.indexOf(localPos);
		if (indexOfSeat == -1 || AllItems.WRENCH.isIn(player.getItemInHand(interactionHand))) {
			if (contraption.interactors.containsKey(localPos))
				return contraption.interactors.get(localPos)
					.handlePlayerInteraction(player, interactionHand, localPos, this);
			return contraption.storage.handlePlayerStorageInteraction(contraption, player, localPos);
		}
		if (player.isPassenger())
			return false;

		// Eject potential existing passenger
		Entity toDismount = null;
		for (Entry<UUID, Integer> entry : contraption.getSeatMapping()
			.entrySet()) {
			if (entry.getValue() != indexOfSeat)
				continue;
			for (Entity entity : getPassengers()) {
				if (!entry.getKey()
					.equals(entity.getUUID()))
					continue;
				if (entity instanceof Player)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !level().isClientSide) {
			Vec3 transformedVector = getPassengerPosition(toDismount, 1);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.teleportTo(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (level().isClientSide)
			return true;
		addSittingPassenger(SeatBlock.getLeashed(level(), player)
			.or(player), indexOfSeat);
		return true;
	}

	public Vec3 toGlobalVector(Vec3 localVec, float partialTicks) {
		return toGlobalVector(localVec, partialTicks, false);
	}

	public Vec3 toGlobalVector(Vec3 localVec, float partialTicks, boolean prevAnchor) {
		Vec3 anchor = prevAnchor ? getPrevAnchorVec() : getAnchorVec();
		Vec3 rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.add(rotationOffset)
			.add(anchor);
		return localVec;
	}

	public Vec3 toLocalVector(Vec3 localVec, float partialTicks) {
		return toLocalVector(localVec, partialTicks, false);
	}

	public Vec3 toLocalVector(Vec3 globalVec, float partialTicks, boolean prevAnchor) {
		Vec3 anchor = prevAnchor ? getPrevAnchorVec() : getAnchorVec();
		Vec3 rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		globalVec = globalVec.subtract(anchor)
			.subtract(rotationOffset);
		globalVec = reverseRotation(globalVec, partialTicks);
		globalVec = globalVec.add(rotationOffset);
		return globalVec;
	}

	@Override
	public void tick() {
		if (contraption == null) {
			discard();
			return;
		}

		collidingEntities.entrySet()
			.removeIf(e -> e.getValue()
				.incrementAndGet() > 3);

		xo = getX();
		yo = getY();
		zo = getZ();
		prevPosInvalid = false;

		if (!initialized)
			contraptionInitialize();

		contraption.tickStorage(this);
		tickContraption();
		super.tick();

		if (!(level() instanceof ServerLevelAccessor sl))
			return;

		for (Entity entity : getPassengers()) {
			if (entity instanceof Player)
				continue;
			if (entity.isAlwaysTicking())
				continue;
			if (sl.create$getEntityTickList()
				.contains(entity))
				continue;
			positionRider(entity);
		}
	}

	public void alignPassenger(Entity passenger) {
		Vec3 motion = getContactPointMotion(passenger.getEyePosition());
		if (Mth.equal(motion.length(), 0))
			return;
		if (passenger instanceof ArmorStand)
			return;
		if (!(passenger instanceof LivingEntity living))
			return;
		float prevAngle = living.getYRot();
		float angle = AngleHelper.deg(-Mth.atan2(motion.x, motion.z));
		angle = AngleHelper.angleLerp(0.4f, prevAngle, angle);
		if (level().isClientSide) {
			living.lerpTo(0, 0, 0, 0, 0, 0);
			living.lerpHeadTo(0, 0);
			living.setYRot(angle);
			living.setXRot(0);
			living.yBodyRot = angle;
			living.yHeadRot = angle;
		} else
			living.setYRot(angle);
	}

	public void setBlock(BlockPos localPos, StructureBlockInfo newInfo) {
		contraption.blocks.put(localPos, newInfo);
		CatnipServices.NETWORK.sendToClientsTrackingEntity(this, new ContraptionBlockChangedPacket(getId(), localPos, newInfo.state()));
	}

	protected abstract void tickContraption();

	public abstract Vec3 applyRotation(Vec3 localPos, float partialTicks);

	public abstract Vec3 reverseRotation(Vec3 localPos, float partialTicks);

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!level().isClientSide)
			contraption.stalled = false;

		skipActorStop = true;
		for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			StructureBlockInfo blockInfo = pair.left;
			MovementBehaviour actor = MovementBehaviour.REGISTRY.get(blockInfo.state());

			if (actor == null)
				continue;

			Vec3 oldMotion = context.motion;
			Vec3 actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos())
				.add(actor.getActiveAreaOffset(context)), 1);
			BlockPos gridPosition = BlockPos.containing(actorPosition);
			boolean newPosVisited =
				!context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);

			context.rotation = v -> applyRotation(v, 1);
			context.position = actorPosition;
			if (!isActorActive(context, actor) && !actor.mustTickWhileDisabled())
				continue;
			if (newPosVisited && !context.stall) {
				actor.visitNewPosition(context, gridPosition);
				if (!isAlive())
					break;
				context.firstMovement = false;
			}
			if (!oldMotion.equals(context.motion)) {
				actor.onSpeedChanged(context, oldMotion, context.motion);
				if (!isAlive())
					break;
			}
			actor.tick(context);
			if (!isAlive())
				break;
			contraption.stalled |= context.stall;
		}
		if (!isAlive()) {
			contraption.stop(level());
			return;
		}
		skipActorStop = false;

		for (Entity entity : getPassengers()) {
			if (!(entity instanceof OrientedContraptionEntity orientedCE))
				continue;
			if (!contraption.stabilizedSubContraptions.containsKey(entity.getUUID()))
				continue;
			if (orientedCE.contraption != null && orientedCE.contraption.stalled) {
				contraption.stalled = true;
				break;
			}
		}

		if (!level().isClientSide) {
			if (!stalledPreviously && contraption.stalled)
				onContraptionStalled();
			entityData.set(STALLED, contraption.stalled);
			return;
		}

		contraption.stalled = isStalled();
	}

	public void refreshPSIs() {
		for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			StructureBlockInfo blockInfo = pair.left;
			MovementBehaviour actor = MovementBehaviour.REGISTRY.get(blockInfo.state());
			if (actor instanceof PortableStorageInterfaceMovement && isActorActive(context, actor))
				if (context.position != null)
					actor.visitNewPosition(context, BlockPos.containing(context.position));
		}
	}

	protected boolean isActorActive(MovementContext context, MovementBehaviour actor) {
		return actor.isActive(context);
	}

	protected void onContraptionStalled() {
		CatnipServices.NETWORK.sendToClientsTrackingEntity(this, new ContraptionStallPacket(getId(), getX(), getY(), getZ(), getStalledAngle()));
	}

	protected boolean shouldActorTrigger(MovementContext context, StructureBlockInfo blockInfo, MovementBehaviour actor,
										 Vec3 actorPosition, BlockPos gridPosition) {
		Vec3 previousPosition = context.position;
		if (previousPosition == null)
			return false;

		context.motion = actorPosition.subtract(previousPosition);

		if (!level().isClientSide() && context.contraption.entity instanceof CarriageContraptionEntity cce
			&& cce.getCarriage() != null) {
			Train train = cce.getCarriage().train;
			double actualSpeed = train.speedBeforeStall != null ? train.speedBeforeStall : train.speed;
			context.motion = context.motion.normalize()
				.scale(Math.abs(actualSpeed));
		}

		Vec3 relativeMotion = context.motion;
		relativeMotion = reverseRotation(relativeMotion, 1);
		context.relativeMotion = relativeMotion;

		boolean ignoreMotionForFirstMovement =
			context.contraption instanceof CarriageContraption || actor instanceof PortableStorageInterfaceMovement;

		return !BlockPos.containing(previousPosition)
			.equals(gridPosition)
			|| (context.relativeMotion.length() > 0 || ignoreMotionForFirstMovement) && context.firstMovement;
	}

	public void move(double x, double y, double z) {
		setPos(getX() + x, getY() + y, getZ() + z);
	}

	public Vec3 getAnchorVec() {
		return position();
	}

	public Vec3 getPrevAnchorVec() {
		return getPrevPositionVec();
	}

	public float getYawOffset() {
		return 0;
	}

	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		if (contraption == null)
			return;
		AABB cbox = contraption.bounds;
		if (cbox == null)
			return;
		Vec3 actualVec = getAnchorVec();
		setBoundingBox(cbox.move(actualVec));
	}

	public static float yawFromVector(Vec3 vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vec3 vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<AbstractContraptionEntity> entityBuilder =
			(EntityType.Builder<AbstractContraptionEntity>) builder;
		return entityBuilder.sized(1, 1);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(STALLED, false);
		builder.define(CONTROLLED_BY, Optional.empty());
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		CompoundTag compound = new CompoundTag();
		writeAdditional(compound, registryFriendlyByteBuf.registryAccess(), true);

		ContraptionSyncLimiting.writeSafe(compound, registryFriendlyByteBuf);
	}

	@Override
	protected final void addAdditionalSaveData(CompoundTag compound) {
		writeAdditional(compound, registryAccess(), false);
	}

	protected void writeAdditional(CompoundTag compound, HolderLookup.Provider registries, boolean spawnPacket) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT(registries, spawnPacket));
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		CompoundTag nbt = readAnySizeNbt(registryFriendlyByteBuf);
		if (nbt != null) {
			readAdditional(nbt, true);
		}
	}

	@Override
	protected final void readAdditionalSaveData(CompoundTag compound) {
		readAdditional(compound, false);
	}

	@Nullable
	private static CompoundTag readAnySizeNbt(RegistryFriendlyByteBuf buf) {
		Tag tag = buf.readNbt(NbtAccounter.unlimitedHeap());
		if (tag != null && !(tag instanceof CompoundTag)) {
			throw new DecoderException("Not a compound tag: " + tag);
		} else {
			return (CompoundTag) tag;
		}
	}

	protected void readAdditional(CompoundTag compound, boolean spawnData) {
		if (compound.isEmpty())
			return;

		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(level(), compound.getCompound("Contraption"), spawnData);
		contraption.entity = this;
		entityData.set(STALLED, compound.getBoolean("Stalled"));
	}

	public void disassemble() {
		if (!isAlive())
			return;
		if (contraption == null)
			return;

		StructureTransform transform = makeStructureTransform();

		contraption.stop(level());
		CatnipServices.NETWORK.sendToClientsTrackingEntity(this, new ContraptionDisassemblyPacket(this.getId(), transform));

		contraption.addBlocksToWorld(level(), transform);
		contraption.addPassengersToWorld(level(), transform, getPassengers());

		for (Entity entity : getPassengers()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			UUID id = entity.getUUID();
			if (!contraption.stabilizedSubContraptions.containsKey(id))
				continue;
			BlockPos transformed = transform.apply(contraption.stabilizedSubContraptions.get(id)
				.getConnectedPos());
			entity.setPos(transformed.getX(), transformed.getY(), transformed.getZ());
			((AbstractContraptionEntity) entity).disassemble();
		}

		skipActorStop = true;
		discard();

		ejectPassengers();
		moveCollidedEntitiesOnDisassembly(transform);
		AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level(), blockPosition());
	}

	private void moveCollidedEntitiesOnDisassembly(StructureTransform transform) {
		for (Entity entity : collidingEntities.keySet()) {
			Vec3 localVec = toLocalVector(entity.position(), 0);
			Vec3 transformed = transform.apply(localVec);
			if (level().isClientSide)
				entity.setPos(transformed.x, transformed.y + 1 / 16f, transformed.z);
			else
				entity.teleportTo(transformed.x, transformed.y + 1 / 16f, transformed.z);
		}
	}

	@Override
	public void remove(RemovalReason p_146834_) {
		if (!level().isClientSide && !isRemoved() && contraption != null && !skipActorStop)
			contraption.stop(level());
		if (contraption != null)
			contraption.onEntityRemoved(this);
		super.remove(p_146834_);
	}

	protected abstract StructureTransform makeStructureTransform();

	@Override
	public void kill() {
		ejectPassengers();
		super.kill();
	}

	@Override
	protected void onBelowWorld() {
		ejectPassengers();
		super.onBelowWorld();
	}

	@Override
	public void onRemovedFromLevel() {
		super.onRemovedFromLevel();
	}

	@Override
	protected void doWaterSplashEffect() {
	}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return entityData.get(STALLED);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce)
			ce.handleStallInformation(packet.x(), packet.y(), packet.z(), packet.angle());
	}

	@OnlyIn(Dist.CLIENT)
	static void handleBlockChangedPacket(ContraptionBlockChangedPacket packet) {
		if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce)
			ce.handleBlockChange(packet.localPos(), packet.newState());
	}

	@OnlyIn(Dist.CLIENT)
	static void handleDisassemblyPacket(ContraptionDisassemblyPacket packet) {
		if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce)
			ce.moveCollidedEntitiesOnDisassembly(packet.transform());
	}

	protected abstract float getStalledAngle();

	protected abstract void handleStallInformation(double x, double y, double z, float angle);

	@OnlyIn(Dist.CLIENT)
	protected void handleBlockChange(BlockPos localPos, BlockState newState) {
		if (contraption == null || !contraption.blocks.containsKey(localPos))
			return;
		StructureBlockInfo info = contraption.blocks.get(localPos);
		contraption.blocks.put(localPos, new StructureBlockInfo(info.pos(), newState, info.nbt()));
		if (info.state() != newState && !(newState.getBlock() instanceof SlidingDoorBlock))
			contraption.resetClientContraption();
		contraption.invalidateColliders();
	}

	@Override
	public CompoundTag saveWithoutId(CompoundTag nbt) {
		Vec3 vec = position();
		List<Entity> passengers = getPassengers();

		for (Entity entity : passengers) {
			// setPos has world accessing side-effects when removed == null
			entity.removalReason = RemovalReason.UNLOADED_TO_CHUNK;

			// Gather passengers into same chunk when saving
			Vec3 prevVec = entity.position();
			entity.setPosRaw(vec.x, prevVec.y, vec.z);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			entity.removalReason = null;
		}

		CompoundTag tag = super.saveWithoutId(nbt);
		return tag;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setDeltaMovement(Vec3 motionIn) {
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vec3 vec) {
		super.setDeltaMovement(vec);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	public Vec3 getPrevPositionVec() {
		return prevPosInvalid ? position() : new Vec3(xo, yo, zo);
	}

	public abstract ContraptionRotationState getRotationState();

	public Vec3 getContactPointMotion(Vec3 globalContactPoint) {
		if (prevPosInvalid)
			return Vec3.ZERO;

		Vec3 contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0, true), 1, true);
		Vec3 contraptionLocalMovement = contactPoint.subtract(globalContactPoint);
		Vec3 contraptionAnchorMovement = position().subtract(getPrevPositionVec());
		return contraptionLocalMovement.add(contraptionAnchorMovement);
	}

	public boolean canCollideWith(Entity e) {
		if (e instanceof Player && e.isSpectator())
			return false;
		if (e.noPhysics)
			return false;
		if (e instanceof HangingEntity)
			return false;
		if (e instanceof AbstractMinecart)
			return !(contraption instanceof MountedContraption);
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof Projectile)
			return false;
		if (e.getVehicle() != null)
			return false;

		Entity riding = this.getVehicle();
		while (riding != null) {
			if (riding == e)
				return false;
			riding = riding.getVehicle();
		}

		return e.getPistonPushReaction() == PushReaction.NORMAL;
	}

	@Override
	public boolean hasExactlyOnePlayerPassenger() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public abstract void applyLocalTransforms(PoseStack matrixStack, float partialTicks);

	public static class ContraptionRotationState {
		public static final ContraptionRotationState NONE = new ContraptionRotationState();

		public float xRotation = 0;
		public float yRotation = 0;
		public float zRotation = 0;
		public float secondYRotation = 0;

		Matrix3d matrix;

		public Matrix3d asMatrix() {
			if (matrix != null)
				return matrix;

			matrix = new Matrix3d().asIdentity();
			if (xRotation != 0)
				matrix.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-xRotation)));
			if (yRotation != 0)
				matrix.multiply(new Matrix3d().asYRotation(AngleHelper.rad(-yRotation)));
			if (zRotation != 0)
				matrix.multiply(new Matrix3d().asZRotation(AngleHelper.rad(-zRotation)));
			return matrix;
		}

		public boolean hasVerticalRotation() {
			return xRotation != 0 || zRotation != 0;
		}

		public float getYawOffset() {
			return secondYRotation;
		}

	}

	@Override
	protected boolean updateInWaterStateAndDoFluidPushing() {
		/*
		 * Override this with an empty method to reduce enormous calculation time when
		 * contraptions are in water WARNING: THIS HAS A BUNCH OF SIDE EFFECTS! - Fluids
		 * will not try to change contraption movement direction - this.inWater and
		 * this.isInWater() will return unreliable data - entities riding a contraption
		 * will not cause water splashes (seats are their own entity so this should be
		 * fine) - fall distance is not reset when the contraption is in water -
		 * this.eyesInWater and this.canSwim() will always be false - swimming state
		 * will never be updated
		 */
		return false;
	}

	@Override
	public void igniteForTicks(int ticks) {
		// Contraptions no longer catch fire
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	// Contraptions shouldn't activate pressure plates and tripwires
	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	public boolean isReadyForRender() {
		return initialized;
	}

	public boolean isAliveOrStale() {
		return isAlive() || level().isClientSide() ? staleTicks > 0 : false;
	}

	public boolean isPrevPosInvalid() {
		return prevPosInvalid;
	}
}
