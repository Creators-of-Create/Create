package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.foundation.collision.Matrix3d;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public abstract class AbstractContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	private static final DataParameter<Boolean> STALLED =
		EntityDataManager.createKey(AbstractContraptionEntity.class, DataSerializers.BOOLEAN);

	public final Map<Entity, MutableInt> collidingEntities;

	protected Contraption contraption;
	protected boolean initialized;
	private boolean prevPosInvalid;

	public AbstractContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		prevPosInvalid = true;
		collidingEntities = new IdentityHashMap<>();
	}

	protected void setContraption(Contraption contraption) {
		this.contraption = contraption;
		if (contraption == null)
			return;
		if (world.isRemote)
			return;
		contraption.onEntityCreated(this);
	}

	protected void contraptionInitialize() {
		contraption.onEntityInitialize(world, this);
		initialized = true;
	}

	public boolean collisionEnabled() {
		return true;
	}

	public void addSittingPassenger(Entity passenger, int seatIndex) {
		passenger.startRiding(this, true);
		if (world.isRemote)
			return;
		contraption.getSeatMapping()
			.put(passenger.getUniqueID(), seatIndex);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.getSeatMapping()));
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vec3d transformedVector = getPassengerPosition(passenger, 1);
		super.removePassenger(passenger);
		if (world.isRemote)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.getUniqueID());
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.getSeatMapping()));
	}

	@Override
	public void updatePassengerPosition(Entity passenger, IMoveCallback callback) {
		if (!isPassenger(passenger))
			return;
		Vec3d transformedVector = getPassengerPosition(passenger, 1);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.x, transformedVector.y, transformedVector.z);
	}

	protected Vec3d getPassengerPosition(Entity passenger, float partialTicks) {
		UUID id = passenger.getUniqueID();
		if (passenger instanceof OrientedContraptionEntity) {
			BlockPos localPos = contraption.getBearingPosOf(id);
			if (localPos != null)
				return toGlobalVector(VecHelper.getCenterOf(localPos), partialTicks)
					.add(VecHelper.getCenterOf(BlockPos.ZERO))
					.subtract(.5f, 1, .5f);
		}

		AxisAlignedBB bb = passenger.getBoundingBox();
		double ySize = bb.getYSize();
		BlockPos seat = contraption.getSeatOf(id);
		if (seat == null)
			return null;
		Vec3d transformedVector =
			toGlobalVector(new Vec3d(seat).add(.5, passenger.getYOffset() + ySize - .15f, .5), partialTicks)
				.add(VecHelper.getCenterOf(BlockPos.ZERO))
				.subtract(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean canFitPassenger(Entity p_184219_1_) {
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
			return false;

		// Eject potential existing passenger
		Entity toDismount = null;
		for (Entry<UUID, Integer> entry : contraption.getSeatMapping()
			.entrySet()) {
			if (entry.getValue() != indexOfSeat)
				continue;
			for (Entity entity : getPassengers()) {
				if (!entry.getKey()
					.equals(entity.getUniqueID()))
					continue;
				if (entity instanceof PlayerEntity)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !world.isRemote) {
			Vec3d transformedVector = getPassengerPosition(toDismount, 1);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.setPositionAndUpdate(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (world.isRemote)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public Vec3d toGlobalVector(Vec3d localVec, float partialTicks) {
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}

	public Vec3d toLocalVector(Vec3d globalVec, float partialTicks) {
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
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

		prevPosX = getX();
		prevPosY = getY();
		prevPosZ = getZ();
		prevPosInvalid = false;

		if (!initialized)
			contraptionInitialize();
		tickContraption();
		super.tick();
	}

	protected abstract void tickContraption();

	public abstract Vec3d applyRotation(Vec3d localPos, float partialTicks);

	public abstract Vec3d reverseRotation(Vec3d localPos, float partialTicks);

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!world.isRemote)
			contraption.stalled = false;

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = AllMovementBehaviours.of(blockInfo.state);

			Vec3d actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos)
				.add(actor.getActiveAreaOffset(context)), 1);
			BlockPos gridPosition = new BlockPos(actorPosition);
			boolean newPosVisited =
				!context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);

			context.rotation = v -> applyRotation(v, 1);
			context.position = actorPosition;

			Vec3d oldMotion = context.motion;
			if (!actor.isActive(context))
				continue;
			if (newPosVisited && !context.stall) {
				actor.visitNewPosition(context, gridPosition);
				context.firstMovement = false;
			}
			if (!oldMotion.equals(context.motion))
				actor.onSpeedChanged(context, oldMotion, context.motion);
			actor.tick(context);
			contraption.stalled |= context.stall;
		}

		for (Entity entity : getPassengers()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			if (!contraption.stabilizedSubContraptions.containsKey(entity.getUniqueID()))
				continue;
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (orientedCE.contraption != null && orientedCE.contraption.stalled) {
				contraption.stalled = true;
				break;
			}
		}

		if (!world.isRemote) {
			if (!stalledPreviously && contraption.stalled)
				onContraptionStalled();
			dataManager.set(STALLED, contraption.stalled);
			return;
		}

		contraption.stalled = isStalled();
	}

	protected void onContraptionStalled() {
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionStallPacket(getEntityId(), getX(), getY(), getZ(), getStalledAngle()));
	}

	protected boolean shouldActorTrigger(MovementContext context, BlockInfo blockInfo, MovementBehaviour actor,
		Vec3d actorPosition, BlockPos gridPosition) {
		Vec3d previousPosition = context.position;
		if (previousPosition == null)
			return false;

		context.motion = actorPosition.subtract(previousPosition);
		Vec3d relativeMotion = context.motion;
		relativeMotion = reverseRotation(relativeMotion, 1);
		context.relativeMotion = relativeMotion;
		return !new BlockPos(previousPosition).equals(gridPosition)
			|| context.relativeMotion.length() > 0 && context.firstMovement;
	}

	public void move(double x, double y, double z) {
		setPosition(getX() + x, getY() + y, getZ() + z);
	}

	public Vec3d getAnchorVec() {
		return getPositionVec();
	}

	public float getYawOffset() {
		return 0;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		if (contraption == null)
			return;
		AxisAlignedBB cbox = contraption.bounds;
		if (cbox == null)
			return;
		Vec3d actualVec = getAnchorVec();
		setBoundingBox(cbox.offset(actualVec));
	}

	public static float yawFromVector(Vec3d vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vec3d vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<AbstractContraptionEntity> entityBuilder =
			(EntityType.Builder<AbstractContraptionEntity>) builder;
		return entityBuilder.size(1, 1);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(STALLED, false);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		contraption.entity = this;
		dataManager.set(STALLED, compound.getBoolean("Stalled"));
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT());
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		writeAdditional(compound);
		buffer.writeCompoundTag(compound);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditional(additionalData.readCompoundTag());
	}

	public void disassemble() {
		if (!isAlive())
			return;
		if (contraption == null)
			return;

		remove();

		StructureTransform transform = makeStructureTransform();
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionDisassemblyPacket(this.getEntityId(), transform));

		contraption.addBlocksToWorld(world, transform);
		contraption.addPassengersToWorld(world, transform, getPassengers());

		for (Entity entity : getPassengers()) {
			if (!(entity instanceof OrientedContraptionEntity))
				continue;
			UUID id = entity.getUniqueID();
			if (!contraption.stabilizedSubContraptions.containsKey(id))
				continue;
			BlockPos transformed = transform.apply(contraption.stabilizedSubContraptions.get(id)
				.getConnectedPos());
			entity.setPosition(transformed.getX(), transformed.getY(), transformed.getZ());
			((AbstractContraptionEntity) entity).disassemble();
		}

		removePassengers();
		moveCollidedEntitiesOnDisassembly(transform);
	}

	private void moveCollidedEntitiesOnDisassembly(StructureTransform transform) {
		for (Entity entity : collidingEntities.keySet()) {
			Vec3d localVec = toLocalVector(entity.getPositionVec(), 0);
			Vec3d transformed = transform.apply(localVec);
			if (world.isRemote)
				entity.setPosition(transformed.x, transformed.y + 1 / 16f, transformed.z);
			else
				entity.setPositionAndUpdate(transformed.x, transformed.y + 1 / 16f, transformed.z);
		}
	}

	protected abstract StructureTransform makeStructureTransform();

	@Override
	public void onKillCommand() {
		removePassengers();
		super.onKillCommand();
	}

	@Override
	protected void outOfWorld() {
		removePassengers();
		super.outOfWorld();
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (world != null && world.isRemote)
			return;
		getPassengers().forEach(Entity::remove);
	}

	@Override
	protected void doWaterSplashEffect() {}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return dataManager.get(STALLED);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.handleStallInformation(packet.x, packet.y, packet.z, packet.angle);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleDisassemblyPacket(ContraptionDisassemblyPacket packet) {
  		Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
		if (!(entity instanceof AbstractContraptionEntity))
			return;
		AbstractContraptionEntity ce = (AbstractContraptionEntity) entity;
		ce.moveCollidedEntitiesOnDisassembly(packet.transform);
	}

	protected abstract float getStalledAngle();

	protected abstract void handleStallInformation(float x, float y, float z, float angle);

	@Override
	@SuppressWarnings("deprecation")
	public CompoundNBT writeWithoutTypeId(CompoundNBT nbt) {
		Vec3d vec = getPositionVec();
		List<Entity> passengers = getPassengers();

		for (Entity entity : passengers) {
			// setPos has world accessing side-effects when removed == false
			entity.removed = true;

			// Gather passengers into same chunk when saving
			Vec3d prevVec = entity.getPositionVec();
			entity.setPos(vec.x, prevVec.y, vec.z);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			entity.removed = false;
		}

		CompoundNBT tag = super.writeWithoutTypeId(nbt);
		return tag;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setMotion(Vec3d motionIn) {}

	@Override
	public PushReaction getPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vec3d vec) {
		super.setMotion(vec);
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	public Vec3d getPrevPositionVec() {
		return prevPosInvalid ? getPositionVec() : new Vec3d(prevPosX, prevPosY, prevPosZ);
	}

	public abstract ContraptionRotationState getRotationState();

	public Vec3d getContactPointMotion(Vec3d globalContactPoint) {
		if (prevPosInvalid)
			return Vec3d.ZERO;
		Vec3d contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0), 1);
		return contactPoint.subtract(globalContactPoint)
			.add(getPositionVec().subtract(getPrevPositionVec()));
	}

	public boolean canCollideWith(Entity e) {
		if (e instanceof PlayerEntity && e.isSpectator())
			return false;
		if (e.noClip)
			return false;
		if (e instanceof HangingEntity)
			return false;
		if (e instanceof AbstractMinecartEntity)
			return false;
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof IProjectile)
			return false;
		if (e.getRidingEntity() != null)
			return false;

		Entity riding = this.getRidingEntity();
		while (riding != null) {
			if (riding == e)
				return false;
			riding = riding.getRidingEntity();
		}

		return e.getPushReaction() == PushReaction.NORMAL;
	}

	@Override
	public boolean isOnePlayerRiding() {
		return false;
	}

	public static class ContraptionRotationState {
		static final ContraptionRotationState NONE = new ContraptionRotationState();

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

}
