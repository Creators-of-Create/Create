package com.simibubi.create.content.contraptions.components.structureMovement;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.blaze3d.matrix.MatrixStack;
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

import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public abstract class AbstractContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	private static final DataParameter<Boolean> STALLED =
			EntityDataManager.defineId(AbstractContraptionEntity.class, DataSerializers.BOOLEAN);

	public final Map<Entity, MutableInt> collidingEntities;

	protected Contraption contraption;
	protected boolean initialized;
	protected boolean prevPosInvalid;
	private boolean ticking;

	public AbstractContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
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
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping()));
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vector3d transformedVector = getPassengerPosition(passenger, 1);
		super.removePassenger(passenger);
		if (level.isClientSide)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.getUUID());
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping()));
	}

	@Override
	public void positionRider(Entity passenger, IMoveCallback callback) {
		if (!hasPassenger(passenger))
			return;
		Vector3d transformedVector = getPassengerPosition(passenger, 1);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.x, transformedVector.y, transformedVector.z);
	}

	protected Vector3d getPassengerPosition(Entity passenger, float partialTicks) {
		UUID id = passenger.getUUID();
		if (passenger instanceof OrientedContraptionEntity) {
			BlockPos localPos = contraption.getBearingPosOf(id);
			if (localPos != null)
				return toGlobalVector(VecHelper.getCenterOf(localPos), partialTicks)
					.add(VecHelper.getCenterOf(BlockPos.ZERO))
					.subtract(.5f, 1, .5f);
		}

		AxisAlignedBB bb = passenger.getBoundingBox();
		double ySize = bb.getYsize();
		BlockPos seat = contraption.getSeatOf(id);
		if (seat == null)
			return null;
		Vector3d transformedVector = toGlobalVector(Vector3d.atLowerCornerOf(seat)
			.add(.5, passenger.getMyRidingOffset() + ySize - .15f, .5), partialTicks).add(VecHelper.getCenterOf(BlockPos.ZERO))
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

	public boolean handlePlayerInteraction(PlayerEntity player, BlockPos localPos, Direction side,
		Hand interactionHand) {
		int indexOfSeat = contraption.getSeats()
			.indexOf(localPos);
		if (indexOfSeat == -1)
			return contraption.interactors.containsKey(localPos)
				&& contraption.interactors.get(localPos).handlePlayerInteraction(player, interactionHand, localPos, this);

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
				if (entity instanceof PlayerEntity)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !level.isClientSide) {
			Vector3d transformedVector = getPassengerPosition(toDismount, 1);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.teleportTo(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (level.isClientSide)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public Vector3d toGlobalVector(Vector3d localVec, float partialTicks) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}

	public Vector3d toLocalVector(Vector3d globalVec, float partialTicks) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		globalVec = globalVec.subtract(getAnchorVec())
			.subtract(rotationOffset);
		globalVec = reverseRotation(globalVec, partialTicks);
		globalVec = globalVec.add(rotationOffset);
		return globalVec;
	}

	@Override
	public final void tick() {
		if (contraption == null) {
			remove();
			return;
		}

		for (Iterator<Entry<Entity, MutableInt>> iterator = collidingEntities.entrySet()
			.iterator(); iterator.hasNext();)
			if (iterator.next()
				.getValue()
				.incrementAndGet() > 3)
				iterator.remove();

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

	public abstract Vector3d applyRotation(Vector3d localPos, float partialTicks);

	public abstract Vector3d reverseRotation(Vector3d localPos, float partialTicks);

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!level.isClientSide)
			contraption.stalled = false;

		ticking = true;
		for (MutablePair<BlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = AllMovementBehaviours.of(blockInfo.state);

			Vector3d oldMotion = context.motion;
			Vector3d actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos)
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
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionStallPacket(getId(), getX(), getY(), getZ(), getStalledAngle()));
	}

	protected boolean shouldActorTrigger(MovementContext context, BlockInfo blockInfo, MovementBehaviour actor,
		Vector3d actorPosition, BlockPos gridPosition) {
		Vector3d previousPosition = context.position;
		if (previousPosition == null)
			return false;

		context.motion = actorPosition.subtract(previousPosition);
		Vector3d relativeMotion = context.motion;
		relativeMotion = reverseRotation(relativeMotion, 1);
		context.relativeMotion = relativeMotion;
		return !new BlockPos(previousPosition).equals(gridPosition)
			|| context.relativeMotion.length() > 0 && context.firstMovement;
	}

	public void move(double x, double y, double z) {
		setPos(getX() + x, getY() + y, getZ() + z);
	}

	public Vector3d getAnchorVec() {
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
		AxisAlignedBB cbox = contraption.bounds;
		if (cbox == null)
			return;
		Vector3d actualVec = getAnchorVec();
		setBoundingBox(cbox.move(actualVec));
	}

	public static float yawFromVector(Vector3d vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vector3d vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<AbstractContraptionEntity> entityBuilder =
			(EntityType.Builder<AbstractContraptionEntity>) builder;
		return entityBuilder.sized(1, 1);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(STALLED, false);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		writeAdditional(compound, true);

		try {
			ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
			CompressedStreamTools.write(compound, dataOutput);
			byte[] byteArray = dataOutput.toByteArray();
			int estimatedPacketSize = byteArray.length;
			if (estimatedPacketSize > 2_000_000) {
				Create.LOGGER.warn("Could not send Contraption Spawn Data (Packet too big): "
						+ getContraption().getType().id + " @" + position() + " (" + getUUID().toString() + ")");
				buffer.writeNbt(new CompoundNBT());
				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
			buffer.writeNbt(new CompoundNBT());
			return;
		}

		buffer.writeNbt(compound);
	}

	@Override
	protected final void addAdditionalSaveData(CompoundNBT compound) {
		writeAdditional(compound, false);
	}

	protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT(spawnPacket));
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditional(additionalData.readNbt(), true);
	}

	@Override
	protected final void readAdditionalSaveData(CompoundNBT compound) {
		readAdditional(compound, false);
	}

	protected void readAdditional(CompoundNBT compound, boolean spawnData) {
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

		remove();

		StructureTransform transform = makeStructureTransform();
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionDisassemblyPacket(this.getId(), transform));

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
			Vector3d localVec = toLocalVector(entity.position(), 0);
			Vector3d transformed = transform.apply(localVec);
			if (level.isClientSide)
				entity.setPos(transformed.x, transformed.y + 1 / 16f, transformed.z);
			else
				entity.teleportTo(transformed.x, transformed.y + 1 / 16f, transformed.z);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove(boolean keepData) {
		if (!level.isClientSide && !removed && contraption != null) {
			if (!ticking)
				contraption.stop(level);
		}
		if (contraption != null)
			contraption.onEntityRemoved(this);
		super.remove(keepData);
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

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (level != null && level.isClientSide)
			return;
		getPassengers().forEach(Entity::remove);
	}

	@Override
	protected void doWaterSplashEffect() {}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return entityData.get(STALLED);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.handleStallInformation(packet.x, packet.y, packet.z, packet.angle);
	}

	@OnlyIn(Dist.CLIENT)
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
	@SuppressWarnings("deprecation")
	public CompoundNBT saveWithoutId(CompoundNBT nbt) {
		Vector3d vec = position();
		List<Entity> passengers = getPassengers();

		for (Entity entity : passengers) {
			// setPos has world accessing side-effects when removed == false
			entity.removed = true;

			// Gather passengers into same chunk when saving
			Vector3d prevVec = entity.position();
			entity.setPosRaw(vec.x, prevVec.y, vec.z);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			entity.removed = false;
		}

		CompoundNBT tag = super.saveWithoutId(nbt);
		return tag;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setDeltaMovement(Vector3d motionIn) {}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vector3d vec) {
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

	public Vector3d getPrevPositionVec() {
		return prevPosInvalid ? position() : new Vector3d(xo, yo, zo);
	}

	public abstract ContraptionRotationState getRotationState();

	public Vector3d getContactPointMotion(Vector3d globalContactPoint) {
		if (prevPosInvalid)
			return Vector3d.ZERO;
		Vector3d contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0), 1);
		return contactPoint.subtract(globalContactPoint)
			.add(position().subtract(getPrevPositionVec()));
	}

	public boolean canCollideWith(Entity e) {
		if (e instanceof PlayerEntity && e.isSpectator())
			return false;
		if (e.noPhysics)
			return false;
		if (e instanceof HangingEntity)
			return false;
		if (e instanceof AbstractMinecartEntity)
			return !(contraption instanceof MountedContraption);
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof ProjectileEntity)
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
	public boolean hasOnePlayerPassenger() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public abstract void doLocalTransforms(float partialTicks, MatrixStack[] matrixStacks);

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
		 * Override this with an empty method to reduce enormous calculation time when contraptions are in water
		 * WARNING: THIS HAS A BUNCH OF SIDE EFFECTS!
		 * - Fluids will not try to change contraption movement direction
		 * - this.inWater and this.isInWater() will return unreliable data
		 * - entities riding a contraption will not cause water splashes (seats are their own entity so this should be fine)
		 * - fall distance is not reset when the contraption is in water
		 * - this.eyesInWater and this.canSwim() will always be false
		 * - swimming state will never be updated
		 */
		return false;
	}

	@Override
	public void setSecondsOnFire(int p_70015_1_) {
		// Contraptions no longer catch fire
	}

}
