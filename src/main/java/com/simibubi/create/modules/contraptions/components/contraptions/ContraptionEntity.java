package com.simibubi.create.modules.contraptions.components.contraptions;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.IHaveMovementBehavior.MovementContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	protected Contraption contraption;
	protected float initialAngle;
	protected BlockPos controllerPos;
	protected IControlContraption controllerTE;

	public float movementSpeedModifier;

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
		movementSpeedModifier = 1;
	}

	public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
		this.controllerPos = controller.getPos();
		this.controllerTE = controller;
		return this;
	}

	@Override
	public void tick() {
		attachToController();

		Entity e = getRidingEntity();
		if (e != null) {
			Vec3d movementVector = e.getMotion();
			Vec3d motion = movementVector.normalize();
			if (motion.length() > 0) {
				targetYaw = yawFromMotion(motion);
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

			tickActors(movementVector);
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
		movementSpeedModifier = 1;

		float anglePitch = getPitch(1);
		float angleYaw = getYaw(1);
		float angleRoll = getRoll(1);
		Vec3d rotationVec = new Vec3d(angleRoll, angleYaw, anglePitch);
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.actors) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			IHaveMovementBehavior actor = (IHaveMovementBehavior) blockInfo.state.getBlock();

			Vec3d actorPosition = new Vec3d(blockInfo.pos);
			actorPosition = actorPosition.add(actor.getActiveAreaOffset(context));
			actorPosition = VecHelper.rotate(actorPosition, angleRoll, angleYaw, anglePitch);
			actorPosition = actorPosition.add(rotationOffset).add(posX, posY, posZ);

			Vec3d previousPosition = context.position;
			BlockPos gridPosition = new BlockPos(actorPosition);
			boolean newPosVisited = true;

			if (previousPosition != null) {
				context.motion = actorPosition.subtract(previousPosition);
				Vec3d relativeMotion = context.motion;
				relativeMotion = VecHelper.rotate(relativeMotion, -angleRoll, -angleYaw, -anglePitch);
				context.relativeMotion = relativeMotion;
				newPosVisited = !new BlockPos(previousPosition).equals(gridPosition);
			}

			context.rotation = rotationVec;
			context.position = actorPosition;
			
			if (actor.isActive(context)) {
				if (newPosVisited)
					actor.visitNewPosition(context, gridPosition);
				actor.tick(context);
			}

			if (movementSpeedModifier > context.movementSpeedModifier)
				movementSpeedModifier = context.movementSpeedModifier;
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

	public static float yawFromMotion(Vec3d motion) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(motion.z, motion.x)) / Math.PI * 180);
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
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		prevYaw = initialAngle;
		yaw = initialAngle;
		targetYaw = initialAngle;
	}

	public void attachToController() {
		if (controllerPos != null && controllerTE == null) {
			if (!world.isBlockPresent(controllerPos))
				return;
			TileEntity te = world.getTileEntity(controllerPos);
			if (te == null || !(te instanceof IControlContraption))
				remove();
			IControlContraption controllerTE = (IControlContraption) te;
			this.controllerTE = controllerTE;
			controllerTE.attach(this);
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("Contraption", getContraption().writeNBT());
		compound.putFloat("InitialAngle", initialAngle);
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

}
