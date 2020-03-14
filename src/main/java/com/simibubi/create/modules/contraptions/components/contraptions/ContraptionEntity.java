package com.simibubi.create.modules.contraptions.components.contraptions;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.ImmutableSet;
import com.simibubi.create.AllEntities;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.BearingContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.LinearActuatorTileEntity;

import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	protected Contraption contraption;
	protected float initialAngle;
	protected BlockPos controllerPos;
	protected IControlContraption controllerTE;
	protected Vec3d motionBeforeStall;
	protected boolean stationary;

	final List<Entity> collidingEntities = new ArrayList<>();

	private static final DataParameter<Boolean> STALLED =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.BOOLEAN);

	public float prevYaw;
	public float prevPitch;
	public float prevRoll;

	public float yaw;
	public float pitch;
	public float roll;

	// Mounted Contraptions
	public float targetYaw;
	public float targetPitch;

	public ContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		motionBeforeStall = Vec3d.ZERO;
		stationary = entityTypeIn == AllEntities.STATIONARY_CONTRAPTION.type;
	}

	public static ContraptionEntity createMounted(World world, Contraption contraption, float initialAngle) {
		ContraptionEntity entity = new ContraptionEntity(AllEntities.CONTRAPTION.type, world);
		entity.contraption = contraption;
		entity.initialAngle = initialAngle;
		entity.prevYaw = initialAngle;
		entity.yaw = initialAngle;
		entity.targetYaw = initialAngle;
		if (contraption != null)
			contraption.gatherStoredItems();
		return entity;
	}

	public static ContraptionEntity createStationary(World world, Contraption contraption) {
		ContraptionEntity entity = new ContraptionEntity(AllEntities.STATIONARY_CONTRAPTION.type, world);
		entity.contraption = contraption;
		if (contraption != null)
			contraption.gatherStoredItems();
		return entity;
	}

	public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
		this.controllerPos = controller.getPos();
		this.controllerTE = controller;
		return this;
	}

	public boolean collisionEnabled() {
		return stationary && controllerTE instanceof LinearActuatorTileEntity;
	}

	@Override
	public void tick() {
		if (contraption == null) {
			remove();
			return;
		}

		attachToController();

		Entity mountedEntity = getRidingEntity();
		if (mountedEntity != null) {
			tickAsPassenger(mountedEntity);
			return;
		}

		if (getMotion().length() < 1 / 4098f)
			setMotion(Vec3d.ZERO);

		move(getMotion().x, getMotion().y, getMotion().z);
		if (ContraptionCollider.collideBlocks(this))
			controllerTE.collided();

		tickActors(new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ));

		prevYaw = yaw;
		prevPitch = pitch;
		prevRoll = roll;

		super.tick();
	}

	public void collisionTick() {
		ContraptionCollider.collideEntities(this);
	}

	public void tickAsPassenger(Entity e) {
		Entity riding = e;
		while (riding.getRidingEntity() != null)
			riding = riding.getRidingEntity();
		Vec3d movementVector = riding.getMotion();
		if (riding instanceof BoatEntity)
			movementVector = new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ);
		Vec3d motion = movementVector.normalize();

		if (motion.length() > 0) {
			targetYaw = yawFromVector(motion);
			if (targetYaw < 0)
				targetYaw += 360;
			if (yaw < 0)
				yaw += 360;
		}

		prevYaw = yaw;
		yaw = angleLerp(0.4f, yaw, targetYaw);

		boolean wasStalled = isStalled();
		tickActors(movementVector);
		if (isStalled()) {
			if (!wasStalled)
				motionBeforeStall = riding.getMotion();
			riding.setMotion(0, 0, 0);
		}

		if (wasStalled && !isStalled()) {
			riding.setMotion(motionBeforeStall);
			motionBeforeStall = Vec3d.ZERO;
		}

		super.tick();
	}

	public void tickActors(Vec3d movementVector) {
		float anglePitch = getPitch(1);
		float angleYaw = getYaw(1);
		float angleRoll = getRoll(1);
		Vec3d rotationVec = new Vec3d(angleRoll, angleYaw, anglePitch);
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		boolean stalledPreviously = contraption.stalled;

		if (!world.isRemote)
			contraption.stalled = false;

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.actors) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = Contraption.getMovement(blockInfo.state);

			Vec3d actorPosition = new Vec3d(blockInfo.pos);
			actorPosition = actorPosition.add(actor.getActiveAreaOffset(context));
			actorPosition = VecHelper.rotate(actorPosition, angleRoll, angleYaw, anglePitch);
			actorPosition = actorPosition.add(rotationOffset).add(posX, posY, posZ);

			boolean newPosVisited = false;
			BlockPos gridPosition = new BlockPos(actorPosition);

			if (!stalledPreviously) {
				Vec3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vec3d relativeMotion = context.motion;
					relativeMotion = VecHelper.rotate(relativeMotion, -angleRoll, -angleYaw, -anglePitch);
					context.relativeMotion = relativeMotion;
					newPosVisited = !new BlockPos(previousPosition).equals(gridPosition)
							|| context.relativeMotion.length() > 0 && context.firstMovement;
				}

				if (getContraption() instanceof BearingContraption) {
					BearingContraption bc = (BearingContraption) getContraption();
					Direction facing = bc.getFacing();
					Vec3d activeAreaOffset = actor.getActiveAreaOffset(context);
					if (activeAreaOffset.mul(VecHelper.planeByNormal(new Vec3d(facing.getDirectionVec())))
							.equals(Vec3d.ZERO)) {
						if (VecHelper.onSameAxis(blockInfo.pos, BlockPos.ZERO, facing.getAxis())) {
							context.motion = new Vec3d(facing.getDirectionVec()).scale(
									facing.getAxis().getCoordinate(roll - prevRoll, yaw - prevYaw, pitch - prevPitch));
							context.relativeMotion = context.motion;
							int timer = context.data.getInt("StationaryTimer");
							if (timer > 0) {
								context.data.putInt("StationaryTimer", timer - 1);
							} else {
								context.data.putInt("StationaryTimer", 20);
								newPosVisited = true;
							}
						}
					}

				}
			}

			context.rotation = rotationVec;
			context.position = actorPosition;

			if (actor.isActive(context)) {
				if (newPosVisited && !context.stall) {
					actor.visitNewPosition(context, gridPosition);
					context.firstMovement = false;
				}
				actor.tick(context);
				contraption.stalled |= context.stall;
			}
		}

		if (!world.isRemote) {
			if (!stalledPreviously && contraption.stalled) {
				setMotion(Vec3d.ZERO);
				if (controllerTE != null)
					controllerTE.onStall();
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
						new ContraptionStallPacket(getEntityId(), posX, posY, posZ, yaw, pitch, roll));
			}
			dataManager.set(STALLED, contraption.stalled);
		} else {
			contraption.stalled = isStalled();
		}
	}

	public void move(double x, double y, double z) {
		setPosition(posX + x, posY + y, posZ + z);
	}

	public void rotateTo(double roll, double yaw, double pitch) {
		rotate(getShortestAngleDiff(this.roll, roll), getShortestAngleDiff(this.yaw, yaw),
				getShortestAngleDiff(this.pitch, pitch));
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
	}

	public void rotate(double roll, double yaw, double pitch) {
		this.yaw += yaw;
		this.pitch += pitch;
		this.roll += roll;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		Entity e = getRidingEntity();
		if (e != null && e instanceof AbstractMinecartEntity) {
			Entity riding = e;
			while (riding.getRidingEntity() != null)
				riding = riding.getRidingEntity();
			x = riding.posX - .5;
			z = riding.posZ - .5;
		}

		this.posX = x;
		this.posY = y;
		this.posZ = z;

		if (this.isAddedToWorld() && !this.world.isRemote && world instanceof ServerWorld)
			((ServerWorld) this.world).chunkCheck(this); // Forge - Process chunk registration after moving.
		if (contraption != null) {
			AxisAlignedBB cbox = contraption.getBoundingBox();
			if (cbox != null)
				this.setBoundingBox(cbox.offset(x, y, z));
		}
	}

	@Override
	public void stopRiding() {
		if (!world.isRemote)
			if (isAlive())
				disassemble();
		super.stopRiding();
	}

	public static float yawFromVector(Vec3d vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vec3d vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public float getYaw(float partialTicks) {
		return (getRidingEntity() == null ? 1 : -1)
				* (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw)) + initialAngle;
	}

	public float getPitch(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

	public float getRoll(float partialTicks) {
		return partialTicks == 1.0F ? roll : angleLerp(partialTicks, prevRoll, roll);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<ContraptionEntity> entityBuilder = (EntityType.Builder<ContraptionEntity>) builder;
		return entityBuilder.size(1, 1);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(STALLED, false);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		targetYaw = yaw = prevYaw = initialAngle;
		dataManager.set(STALLED, compound.getBoolean("Stalled"));
		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vec3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vec3d.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setMotion(Vec3d.ZERO);
		}
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
	}

	public void attachToController() {
		if (controllerPos != null && (controllerTE == null || !controllerTE.isValid())) {
			if (!world.isBlockPresent(controllerPos))
				return;
			TileEntity te = world.getTileEntity(controllerPos);
			if (te == null || !(te instanceof IControlContraption)) {
				remove();
				return;
			}
			IControlContraption controllerTE = (IControlContraption) te;
			this.controllerTE = controllerTE;
			controllerTE.attach(this);

			if (world.isRemote)
				setPosition(posX, posY, posZ);
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("Contraption", getContraption().writeNBT());
		compound.putFloat("InitialAngle", initialAngle);
		if (!stationary)
			compound.put("CachedMotion",
					newDoubleNBTList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));
		compound.putBoolean("Stalled", isStalled());
		if (controllerPos != null)
			compound.put("Controller", NBTUtil.writeBlockPos(controllerPos));
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
		if (getContraption() != null) {
			BlockPos offset = new BlockPos(getPositionVec().add(.5, .5, .5));
			Vec3d rotation = new Vec3d(getRoll(1), getYaw(1), getPitch(1));
			getContraption().disassemble(world, offset, rotation);
			preventMovedEntitiesFromGettingStuck();
		}
		remove();
	}

	public void preventMovedEntitiesFromGettingStuck() {
		Vec3d stuckTest = new Vec3d(0, -2, 0);
		for (Entity e : collidingEntities) {
			e.fallDistance = 0;
			e.onGround = true;

			Vec3d vec = stuckTest;
			AxisAlignedBB axisalignedbb = e.getBoundingBox().offset(0, 2, 0);
			ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
			VoxelShape voxelshape = e.world.getWorldBorder().getShape();
			Stream<VoxelShape> stream =
				VoxelShapes.compare(voxelshape, VoxelShapes.create(axisalignedbb.shrink(1.0E-7D)), IBooleanFunction.AND)
						? Stream.empty()
						: Stream.of(voxelshape);
			Stream<VoxelShape> stream1 =
				this.world.getEmptyCollisionShapes(e, axisalignedbb.expand(vec), ImmutableSet.of());
			ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
			Vec3d vec3d = vec.lengthSquared() == 0.0D ? vec
					: collideBoundingBoxHeuristically(this, vec, axisalignedbb, e.world, iselectioncontext,
							reuseablestream);
			boolean flag = vec.x != vec3d.x;
			boolean flag1 = vec.y != vec3d.y;
			boolean flag2 = vec.z != vec3d.z;
			boolean flag3 = e.onGround || flag1 && vec.y < 0.0D;
			if (this.stepHeight > 0.0F && flag3 && (flag || flag2)) {
				Vec3d vec3d1 = collideBoundingBoxHeuristically(e, new Vec3d(vec.x, (double) e.stepHeight, vec.z),
						axisalignedbb, e.world, iselectioncontext, reuseablestream);
				Vec3d vec3d2 = collideBoundingBoxHeuristically(e, new Vec3d(0.0D, (double) e.stepHeight, 0.0D),
						axisalignedbb.expand(vec.x, 0.0D, vec.z), e.world, iselectioncontext, reuseablestream);
				if (vec3d2.y < (double) e.stepHeight) {
					Vec3d vec3d3 = collideBoundingBoxHeuristically(e, new Vec3d(vec.x, 0.0D, vec.z),
							axisalignedbb.offset(vec3d2), e.world, iselectioncontext, reuseablestream).add(vec3d2);
					if (horizontalMag(vec3d3) > horizontalMag(vec3d1)) {
						vec3d1 = vec3d3;
					}
				}

				if (horizontalMag(vec3d1) > horizontalMag(vec3d)) {
					vec3d = vec3d1.add(collideBoundingBoxHeuristically(e, new Vec3d(0.0D, -vec3d1.y + vec.y, 0.0D),
							axisalignedbb.offset(vec3d1), e.world, iselectioncontext, reuseablestream));
				}
			}

			vec = vec3d.subtract(stuckTest);
			if (vec.equals(Vec3d.ZERO))
				continue;
			e.setPosition(e.posX + vec.x, e.posY + vec.y, e.posZ + vec.z);
		}
	}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return dataManager.get(STALLED);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
			int posRotationIncrements, boolean teleport) {
		// Stationary Anchors are responsible for keeping position and motion in sync
		// themselves.
		if (stationary)
			return;
		super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
		if (!(entity instanceof ContraptionEntity))
			return;
		ContraptionEntity ce = (ContraptionEntity) entity;
		if (ce.getRidingEntity() == null) {
			ce.posX = packet.x;
			ce.posY = packet.y;
			ce.posZ = packet.z;
		}
		ce.yaw = packet.yaw;
		ce.pitch = packet.pitch;
		ce.roll = packet.roll;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setMotion(Vec3d motionIn) {
	}

	@Override
	public PushReaction getPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vec3d vec) {
		super.setMotion(vec);
	}

}
