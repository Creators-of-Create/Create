package com.simibubi.create.modules.contraptions.receivers.constructs.mounted;

import com.simibubi.create.AllEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.receivers.constructs.Contraption;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	private Contraption contraption;
	protected float initialAngle;

	protected BlockPos controllerPos;
	protected IControlContraption controllerTE;

	// Not synchronizing any of these yet
	public float targetYaw;
	public float targetPitch;
	public float contraptionYaw;
	public float contraptionPitch;

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
		this.prevRotationYaw = initialAngle;
		this.contraptionYaw = initialAngle;
		this.targetYaw = initialAngle;
	}

	public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
		this.controllerPos = controller.getPos();
		this.controllerTE = controller;
		return this;
	}

	@Override
	public void tick() {
		super.tick();
		attachToController();
		Entity e = getRidingEntity();
		if (e == null)
			return;
		else {
			Vec3d movementVector = e.getMotion();
			Vec3d motion = movementVector.normalize();
			if (motion.length() > 0) {
				targetYaw = yawFromMotion(motion);
				targetPitch = (float) ((Math.atan(motion.y) * 73.0D) / Math.PI * 180);
				if (targetYaw < 0)
					targetYaw += 360;
				if (contraptionYaw < 0)
					contraptionYaw += 360;
			}

			float speed = 0.2f;
			prevRotationYaw = contraptionYaw;
			contraptionYaw = angleLerp(speed, contraptionYaw, targetYaw);
			prevRotationPitch = contraptionPitch;
			contraptionPitch = angleLerp(speed, contraptionPitch, targetPitch);

			tickActors(movementVector);
		}
	}

	public void tickActors(Vec3d movementVector) {
		getContraption().getActors().forEach(pair -> {
			MovementContext context = pair.right;
			float deg = -contraptionYaw + initialAngle;
			context.motion = VecHelper.rotate(movementVector, deg, Axis.Y);

			if (context.world == null)
				context.world = world;

			Vec3d offset = new Vec3d(pair.left.pos.subtract(getContraption().getAnchor()));
			world.addParticle(ParticleTypes.BUBBLE, offset.x, offset.y, offset.z, 0, 0, 0);

			offset = VecHelper.rotate(offset, deg, Axis.Y);
			world.addParticle(ParticleTypes.CRIT, offset.x, offset.y, offset.z, 0, 0, 0);

			offset = offset.add(new Vec3d(getPosition()).add(0.5, 0, 0.5));
			world.addParticle(ParticleTypes.NOTE, offset.x, offset.y, offset.z, 0, 10, 0);

			if (world.isRemote)
				return;

			BlockPos gridPos = new BlockPos(offset);
			if (context.currentGridPos.equals(gridPos))
				return;
			context.currentGridPos = gridPos;

			IHaveMovementBehavior actor = (IHaveMovementBehavior) pair.left.state.getBlock();
			actor.visitPosition(context);
		});
	}

	@Override
	public void setPosition(double x, double y, double z) {
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

	public static float yawFromMotion(Vec3d motion) {
		return (float) ((Math.PI / 2 - Math.atan2(motion.z, motion.x)) / Math.PI * 180);
	}

	public float getYaw(float partialTicks) {
		float yaw = contraptionYaw;
		return (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, this.prevRotationYaw, yaw)) - initialAngle;
	}

	public float getPitch(float partialTicks) {
		float pitch = contraptionPitch;
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, this.prevRotationPitch, pitch);
	}

	private float angleLerp(float pct, float current, float target) {
		current = current % 360;
		target = target % 360;
		float shortest_angle = ((((target - current) % 360) + 540) % 360) - 180;
		return current + shortest_angle * pct;
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
		contraption = Contraption.fromNBT(compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		prevRotationYaw = initialAngle;
		contraptionYaw = initialAngle;
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
			getContraption().disassemble(world, new BlockPos(getPositionVec().add(.5, .5, .5)), contraptionYaw,
					contraptionPitch);
		remove();
	}

	public Contraption getContraption() {
		return contraption;
	}

}
