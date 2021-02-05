package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Ex: Pistons, bearings <br>
 * Controlled Contraption Entities can rotate around one axis and translate.
 * <br>
 * They are bound to an {@link IControlContraption}
 */
public class ControlledContraptionEntity extends AbstractContraptionEntity {

	protected BlockPos controllerPos;
	protected Axis rotationAxis;
	protected float prevAngle;
	protected float angle;

	public ControlledContraptionEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public static ControlledContraptionEntity create(World world, IControlContraption controller,
		Contraption contraption) {
		ControlledContraptionEntity entity =
			new ControlledContraptionEntity(AllEntityTypes.CONTROLLED_CONTRAPTION.get(), world);
		entity.controllerPos = controller.getBlockPosition();
		entity.setContraption(contraption);
		return entity;
	}

	@Override
		public Vec3d getContactPointMotion(Vec3d globalContactPoint) {
			if (contraption instanceof TranslatingContraption)
				return getMotion();
			return super.getContactPointMotion(globalContactPoint);
		}

	@Override
	protected void setContraption(Contraption contraption) {
		super.setContraption(contraption);
		if (contraption instanceof BearingContraption)
			rotationAxis = ((BearingContraption) contraption).getFacing()
				.getAxis();

	}

	@Override
	protected void readAdditional(CompoundNBT compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);
		controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		if (compound.contains("Axis"))
			rotationAxis = NBTHelper.readEnum(compound, "Axis", Axis.class);
		angle = compound.getFloat("Angle");
	}

	@Override
	protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);
		compound.put("Controller", NBTUtil.writeBlockPos(controllerPos));
		if (rotationAxis != null)
			NBTHelper.writeEnum(compound, "Axis", rotationAxis);
		compound.putFloat("Angle", angle);
	}

	@Override
	public ContraptionRotationState getRotationState() {
		ContraptionRotationState crs = new ContraptionRotationState();
		if (rotationAxis == Axis.X)
			crs.xRotation = angle;
		if (rotationAxis == Axis.Y)
			crs.yRotation = angle;
		if (rotationAxis == Axis.Z)
			crs.zRotation = angle;
		return crs;
	}

	@Override
	public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	@Override
	public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle(float partialTicks) {
		return partialTicks == 1.0F ? angle : angleLerp(partialTicks, prevAngle, angle);
	}

	public void setRotationAxis(Axis rotationAxis) {
		this.rotationAxis = rotationAxis;
	}

	public Axis getRotationAxis() {
		return rotationAxis;
	}

	@Override
	public void setPositionAndUpdate(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	protected void tickContraption() {
		prevAngle = angle;
		tickActors();
		
		if (controllerPos == null)
			return;
		if (!world.isBlockPresent(controllerPos))
			return;
		IControlContraption controller = getController();
		if (controller == null) {
			remove();
			return;
		}
		if (!controller.isAttachedTo(this)) {
			controller.attach(this);
			if (world.isRemote)
				setPosition(getX(), getY(), getZ());
		}

		Vec3d motion = getMotion();
		move(motion.x, motion.y, motion.z);
		if (ContraptionCollider.collideBlocks(this))
			getController().collided();
	}

	@Override
	protected boolean shouldActorTrigger(MovementContext context, BlockInfo blockInfo, MovementBehaviour actor,
		Vec3d actorPosition, BlockPos gridPosition) {
		if (super.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition))
			return true;

		// Special activation timer for actors in the center of a bearing contraption
		if (!(contraption instanceof BearingContraption))
			return false;
		BearingContraption bc = (BearingContraption) contraption;
		Direction facing = bc.getFacing();
		Vec3d activeAreaOffset = actor.getActiveAreaOffset(context);
		if (!activeAreaOffset.mul(VecHelper.axisAlingedPlaneOf(new Vec3d(facing.getDirectionVec())))
			.equals(Vec3d.ZERO))
			return false;
		if (!VecHelper.onSameAxis(blockInfo.pos, BlockPos.ZERO, facing.getAxis()))
			return false;
		context.motion = new Vec3d(facing.getDirectionVec()).scale(angle - prevAngle);
		context.relativeMotion = context.motion;
		int timer = context.data.getInt("StationaryTimer");
		if (timer > 0) {
			context.data.putInt("StationaryTimer", timer - 1);
			return false;
		}

		context.data.putInt("StationaryTimer", 20);
		return true;
	}

	protected IControlContraption getController() {
		if (controllerPos == null)
			return null;
		if (!world.isBlockPresent(controllerPos))
			return null;
		TileEntity te = world.getTileEntity(controllerPos);
		if (!(te instanceof IControlContraption))
			return null;
		return (IControlContraption) te;
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		BlockPos offset = new BlockPos(getAnchorVec().add(.5, .5, .5));
		float xRot = rotationAxis == Axis.X ? angle : 0;
		float yRot = rotationAxis == Axis.Y ? angle : 0;
		float zRot = rotationAxis == Axis.Z ? angle : 0;
		return new StructureTransform(offset, xRot, yRot, zRot);
	}

	@Override
	protected void onContraptionStalled() {
		IControlContraption controller = getController();
		if (controller != null)
			controller.onStall();
		super.onContraptionStalled();
	}

	@Override
	protected float getStalledAngle() {
		return angle;
	}

	@Override
	protected void handleStallInformation(float x, float y, float z, float angle) {
		setPos(x, y, z);
		this.angle = angle;
	}
}
