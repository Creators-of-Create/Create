package com.simibubi.create.modules.contraptions.components.contraptions;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllEntities;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
	}

	protected ContraptionEntity(World world) {
		this(AllEntities.CONTRAPTION.type, world);
	}

	public ContraptionEntity(World world, Contraption contraption, float initialAngle) {
		this(world);
		this.contraption = contraption;
		this.initialAngle = initialAngle;
		this.prevYaw = initialAngle;
		this.yaw = initialAngle;
		this.targetYaw = initialAngle;
		if (contraption != null)
			contraption.gatherStoredItems();
	}

	public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
		this.controllerPos = controller.getPos();
		this.controllerTE = controller;
		return this;
	}

	@Override
	public void tick() {
		if (contraption == null) {
			remove();
			return;
		}

		attachToController();

		Entity e = getRidingEntity();
		if (e != null) {
			Vec3d movementVector = e.getMotion();
			Vec3d motion = movementVector.normalize();
			if (motion.length() > 0) {
				targetYaw = yawFromVector(motion);
				targetPitch = (float) ((Math.atan(motion.y) * 73.0D) / Math.PI * 180);
				if (targetYaw < 0)
					targetYaw += 360;
				if (yaw < 0)
					yaw += 360;
			}

			if (Math.abs(getShortestAngleDiff(yaw, targetYaw)) >= 175) {
				initialAngle += 180;
				yaw += 180;
				prevYaw = yaw;
			} else {
				float speed = 0.2f;
				prevYaw = yaw;
				yaw = angleLerp(speed, yaw, targetYaw);
				prevPitch = pitch;
				pitch = angleLerp(speed, pitch, targetPitch);
			}

			boolean wasStalled = isStalled();
			tickActors(movementVector);
			if (isStalled()) {
				if (!wasStalled)
					motionBeforeStall = e.getMotion();
				e.setMotion(0, 0, 0);
			}

			if (wasStalled && !isStalled()) {
				e.setMotion(motionBeforeStall);
				motionBeforeStall = Vec3d.ZERO;
			}

			super.tick();
			return;
		}

		prevYaw = yaw;
		prevPitch = pitch;
		prevRoll = roll;

		tickActors(new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ));
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

			boolean newPosVisited = context.position == null;
			BlockPos gridPosition = new BlockPos(actorPosition);

			if (!stalledPreviously) {
				Vec3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vec3d relativeMotion = context.motion;
					relativeMotion = VecHelper.rotate(relativeMotion, -angleRoll, -angleYaw, -anglePitch);
					context.relativeMotion = relativeMotion;
					newPosVisited = !new BlockPos(previousPosition).equals(gridPosition);
				}
			}

			context.rotation = rotationVec;
			context.position = actorPosition;

			if (actor.isActive(context)) {
				if (newPosVisited && !context.stall)
					actor.visitNewPosition(context, gridPosition);
				actor.tick(context);
				contraption.stalled |= context.stall;
			}

		}

		if (!world.isRemote) {
			if (!stalledPreviously && contraption.stalled) {
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

	public void moveTo(double x, double y, double z) {
		move(x - posX, y - posY, z - posZ);
	}

	public void move(double x, double y, double z) {

		// Collision and stuff

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

		// Collision and stuff

		this.yaw += yaw;
		this.pitch += pitch;
		this.roll += roll;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		Entity e = getRidingEntity();
		if (e != null && e instanceof AbstractMinecartEntity) {
			x -= .5;
			z -= .5;
		}

		this.posX = x;
		this.posY = y;
		this.posZ = z;
		if (this.isAddedToWorld() && !this.world.isRemote && world instanceof ServerWorld)
			((ServerWorld) this.world).chunkCheck(this); // Forge - Process chunk registration after moving.
		if (contraption != null) {
			AxisAlignedBB cbox = contraption.getCollisionBoxFront();
			if (cbox != null)
				this.setBoundingBox(cbox.offset(x, y, z));
		}
	}

	@Override
	public void stopRiding() {
		super.stopRiding();
		if (!world.isRemote)
			disassemble();
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

	private float angleLerp(float pct, float current, float target) {
		return current + getShortestAngleDiff(current, target) * pct;
	}

	private float getShortestAngleDiff(double current, double target) {
		current = current % 360;
		target = target % 360;
		return (float) (((((target - current) % 360) + 540) % 360) - 180);
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
		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty())
			motionBeforeStall = new Vec3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		prevYaw = initialAngle;
		yaw = initialAngle;
		targetYaw = initialAngle;
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
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("Contraption", getContraption().writeNBT());
		compound.putFloat("InitialAngle", initialAngle);
		compound.put("CachedMotion", newDoubleNBTList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));
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
		if (getContraption() != null)
			getContraption().disassemble(world, new BlockPos(getPositionVec().add(.5, .5, .5)), yaw, pitch);
		remove();
	}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return dataManager.get(STALLED);
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

}
