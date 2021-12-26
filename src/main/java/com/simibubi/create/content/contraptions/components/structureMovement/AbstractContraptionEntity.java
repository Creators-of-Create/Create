package com.simibubi.create.content.contraptions.components.structureMovement;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;
import com.simibubi.create.lib.mixin.common.accessor.EntityAccessor;
import com.simibubi.create.lib.util.EntityHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractContraptionEntity extends Entity implements ExtraSpawnDataEntity {

	private static final EntityDataAccessor<Boolean> STALLED =
		SynchedEntityData.defineId(AbstractContraptionEntity.class, EntityDataSerializers.BOOLEAN);

	public final Map<Entity, MutableInt> collidingEntities;

	protected Contraption contraption;
	protected boolean initialized;
	protected boolean prevPosInvalid;
	private boolean ticking;

	public AbstractContraptionEntity(EntityType<?> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
		prevPosInvalid = true;
		collidingEntities = new IdentityHashMap<>();
	}

	protected void setContraption(Contraption contraption) {
		this.contraption = contraption;
		if (contraption == null)
			return;
		if (level.isClientSide)
			return;
		contraption.onEntityCreated(this);
	}

	public boolean supportsTerrainCollision() {
		return contraption instanceof TranslatingContraption;
	}

	protected void contraptionInitialize() {
		contraption.onEntityInitialize(level, this);
		initialized = true;
	}

	public boolean collisionEnabled() {
		return true;
	}

	public void addSittingPassenger(Entity passenger, int seatIndex) {
		passenger.startRiding(this, true);
		if (level.isClientSide)
			return;
		contraption.getSeatMapping()
			.put(passenger.getUUID(), seatIndex);
		AllPackets.channel.sendToClientsTracking(
			new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping()), this);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vec3 transformedVector = getPassengerPosition(passenger, 1);
		super.removePassenger(passenger);
		if (level.isClientSide)
			return;
		if (transformedVector != null)
			EntityHelper.getExtraCustomData(passenger)
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.getUUID());
		AllPackets.channel.sendToClientsTracking(
			new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping()), this);
	}

	@Override
	public void positionRider(Entity passenger, MoveFunction callback) {
		if (!hasPassenger(passenger))
			return;
		Vec3 transformedVector = getPassengerPosition(passenger, 1);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.x, transformedVector.y, transformedVector.z);
	}

	protected Vec3 getPassengerPosition(Entity passenger, float partialTicks) {
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
			.add(.5, passenger.getMyRidingOffset() + ySize - .15f, .5), partialTicks)
				.add(VecHelper.getCenterOf(BlockPos.ZERO))
				.subtract(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean canAddPassenger(Entity p_184219_1_) {
		if (p_184219_1_ instanceof OrientedContraptionEntity)
			return true;
		return contraption.getSeatMapping()
			.size() < contraption.getSeats()
				.size();
	}

	public boolean handlePlayerInteraction(Player player, BlockPos localPos, Direction side,
		InteractionHand interactionHand) {
		int indexOfSeat = contraption.getSeats()
			.indexOf(localPos);
		if (indexOfSeat == -1)
			return contraption.interactors.containsKey(localPos) && contraption.interactors.get(localPos)
				.handlePlayerInteraction(player, interactionHand, localPos, this);

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

		if (toDismount != null && !level.isClientSide) {
			Vec3 transformedVector = getPassengerPosition(toDismount, 1);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.teleportTo(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (level.isClientSide)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public Vec3 toGlobalVector(Vec3 localVec, float partialTicks) {
		Vec3 rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}

	public Vec3 toLocalVector(Vec3 globalVec, float partialTicks) {
		Vec3 rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		globalVec = globalVec.subtract(getAnchorVec())
			.subtract(rotationOffset);
		globalVec = reverseRotation(globalVec, partialTicks);
		globalVec = globalVec.add(rotationOffset);
		return globalVec;
	}

	@Override
	public final void tick() {
		if (contraption == null) {
			discard();
			return;
		}

		collidingEntities.entrySet().removeIf(e -> e.getValue().incrementAndGet() > 3);

		xo = getX();
		yo = getY();
		zo = getZ();
		prevPosInvalid = false;

		if (!initialized)
			contraptionInitialize();
		contraption.onEntityTick(level);
		tickContraption();
		super.tick();
	}

	protected abstract void tickContraption();

	public abstract Vec3 applyRotation(Vec3 localPos, float partialTicks);

	public abstract Vec3 reverseRotation(Vec3 localPos, float partialTicks);

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!level.isClientSide)
			contraption.stalled = false;

		ticking = true;
		for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			StructureBlockInfo blockInfo = pair.left;
			MovementBehaviour actor = AllMovementBehaviours.of(blockInfo.state);

			Vec3 oldMotion = context.motion;
			Vec3 actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos)
				.add(actor.getActiveAreaOffset(context)), 1);
			BlockPos gridPosition = new BlockPos(actorPosition);
			boolean newPosVisited =
				!context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);

			context.rotation = v -> applyRotation(v, 1);
			context.position = actorPosition;
			if (!actor.isActive(context))
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
			contraption.stop(level);
			return;
		}
		ticking = false;

		for (Entity entity : getPassengers()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			if (!contraption.stabilizedSubContraptions.containsKey(entity.getUUID()))
				continue;
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (orientedCE.contraption != null && orientedCE.contraption.stalled) {
				contraption.stalled = true;
				break;
			}
		}

		if (!level.isClientSide) {
			if (!stalledPreviously && contraption.stalled)
				onContraptionStalled();
			entityData.set(STALLED, contraption.stalled);
			return;
		}

		contraption.stalled = isStalled();
	}

	protected void onContraptionStalled() {
		AllPackets.channel.sendToClientsTracking(
			new ContraptionStallPacket(getId(), getX(), getY(), getZ(), getStalledAngle()), this);
	}

	protected boolean shouldActorTrigger(MovementContext context, StructureBlockInfo blockInfo, MovementBehaviour actor,
		Vec3 actorPosition, BlockPos gridPosition) {
		Vec3 previousPosition = context.position;
		if (previousPosition == null)
			return false;

		context.motion = actorPosition.subtract(previousPosition);
		Vec3 relativeMotion = context.motion;
		relativeMotion = reverseRotation(relativeMotion, 1);
		context.relativeMotion = relativeMotion;
		return !new BlockPos(previousPosition).equals(gridPosition)
			|| context.relativeMotion.length() > 0 && context.firstMovement;
	}

	public void move(double x, double y, double z) {
		setPos(getX() + x, getY() + y, getZ() + z);
	}

	public Vec3 getAnchorVec() {
		return position();
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

	public static FabricEntityTypeBuilder<?> build(FabricEntityTypeBuilder<?> builder) {
//		@SuppressWarnings("unchecked")
//		EntityType.Builder<AbstractContraptionEntity> entityBuilder =
//			(EntityType.Builder<AbstractContraptionEntity>) builder;
		return builder.dimensions(EntityDimensions.fixed(1, 1));
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(STALLED, false);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this, this == null ? 0 : getId());
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		writeAdditional(compound, true);

		try {
			ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
			NbtIo.write(compound, dataOutput);
			byte[] byteArray = dataOutput.toByteArray();
			int estimatedPacketSize = byteArray.length;
			if (estimatedPacketSize > 2_000_000) {
				Create.LOGGER.warn("Could not send Contraption Spawn Data (Packet too big): "
					+ getContraption().getType().id + " @" + position() + " (" + getUUID().toString() + ")");
				buffer.writeNbt(new CompoundTag());
				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
			buffer.writeNbt(new CompoundTag());
			return;
		}

		buffer.writeNbt(compound);
	}

	@Override
	protected final void addAdditionalSaveData(CompoundTag compound) {
		writeAdditional(compound, false);
	}

	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT(spawnPacket));
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		readAdditional(additionalData.readNbt(), true);
	}

	@Override
	protected final void readAdditionalSaveData(CompoundTag compound) {
		readAdditional(compound, false);
	}

	protected void readAdditional(CompoundTag compound, boolean spawnData) {
		if (compound.isEmpty())
			return;

		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(level, compound.getCompound("Contraption"), spawnData);
		contraption.entity = this;
		entityData.set(STALLED, compound.getBoolean("Stalled"));
	}

	public void disassemble() {
		if (!isAlive())
			return;
		if (contraption == null)
			return;

		discard();

		StructureTransform transform = makeStructureTransform();
		AllPackets.channel.sendToClientsTracking(
			new ContraptionDisassemblyPacket(this.getId(), transform), this);

		contraption.addBlocksToWorld(level, transform);
		contraption.addPassengersToWorld(level, transform, getPassengers());

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

		ejectPassengers();
		moveCollidedEntitiesOnDisassembly(transform);
		AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, blockPosition());
	}

	private void moveCollidedEntitiesOnDisassembly(StructureTransform transform) {
		for (Entity entity : collidingEntities.keySet()) {
			Vec3 localVec = toLocalVector(entity.position(), 0);
			Vec3 transformed = transform.apply(localVec);
			if (level.isClientSide)
				entity.setPos(transformed.x, transformed.y + 1 / 16f, transformed.z);
			else
				entity.teleportTo(transformed.x, transformed.y + 1 / 16f, transformed.z);
		}
	}

	@Override
	public void remove(RemovalReason p_146834_) {
		if (p_146834_ == RemovalReason.DISCARDED) onRemovedFromWorld();

		if (!level.isClientSide && !isRemoved() && contraption != null)
			if (!ticking)
				contraption.stop(level);
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
	protected void outOfWorld() {
		ejectPassengers();
		super.outOfWorld();
	}

//	@Override
	public void onRemovedFromWorld() {
//		super.onRemovedFromWorld();
		if (level != null && level.isClientSide)
			return;
		getPassengers().forEach(Entity::discard);
	}

	@Override
	protected void doWaterSplashEffect() {}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return entityData.get(STALLED);
	}

	@Environment(EnvType.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.handleStallInformation(packet.x, packet.y, packet.z, packet.angle);
	}

	@Environment(EnvType.CLIENT)
	static void handleDisassemblyPacket(ContraptionDisassemblyPacket packet) {
		Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.moveCollidedEntitiesOnDisassembly(packet.transform);
	}

	protected abstract float getStalledAngle();

	protected abstract void handleStallInformation(float x, float y, float z, float angle);

	@Override
	public CompoundTag saveWithoutId(CompoundTag nbt) {
		Vec3 vec = position();
		List<Entity> passengers = getPassengers();

		for (Entity entity : passengers) {
			// setPos has world accessing side-effects when removed == null
			String srg = "f_146795_"; // removalReason
			((EntityAccessor)entity).create$setRemovalReason(RemovalReason.UNLOADED_TO_CHUNK);
//			ObfuscationReflectionHelper.setPrivateValue(Entity.class, entity, RemovalReason.UNLOADED_TO_CHUNK, srg);

			// Gather passengers into same chunk when saving
			Vec3 prevVec = entity.position();
			entity.setPosRaw(vec.x, prevVec.y, vec.z);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			((EntityAccessor)entity).create$setRemovalReason(null);
//			ObfuscationReflectionHelper.setPrivateValue(Entity.class, entity, null, srg);
		}

		CompoundTag tag = super.saveWithoutId(nbt);
		return tag;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setDeltaMovement(Vec3 motionIn) {}

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
		Vec3 contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0), 1);
		return contactPoint.subtract(globalContactPoint)
			.add(position().subtract(getPrevPositionVec()));
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

	@Environment(EnvType.CLIENT)
	public abstract void doLocalTransforms(float partialTicks, PoseStack[] matrixStacks);

	public static class ContraptionRotationState {
		public static final ContraptionRotationState NONE = new ContraptionRotationState();

		float xRotation = 0;
		float yRotation = 0;
		float zRotation = 0;
		float secondYRotation = 0;
		Matrix3d matrix;

		public Matrix3d asMatrix() {
			if (matrix != null)
				return matrix;

			matrix = new Matrix3d().asIdentity();
			if (xRotation != 0)
				matrix.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-xRotation)));
			if (yRotation != 0)
				matrix.multiply(new Matrix3d().asYRotation(AngleHelper.rad(yRotation)));
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
	public void setSecondsOnFire(int p_70015_1_) {
		// Contraptions no longer catch fire
	}

}
