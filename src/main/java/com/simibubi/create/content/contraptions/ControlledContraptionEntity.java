package com.simibubi.create.content.contraptions;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
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
	protected float angleDelta;

	public ControlledContraptionEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	public static ControlledContraptionEntity create(Level world, IControlContraption controller,
		Contraption contraption) {
		ControlledContraptionEntity entity =
			new ControlledContraptionEntity(AllEntityTypes.CONTROLLED_CONTRAPTION.get(), world);
		entity.controllerPos = controller.getBlockPosition();
		entity.setContraption(contraption);
		return entity;
	}

	@Override
	public void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		if (!level().isClientSide())
			return;
		for (Entity entity : getPassengers())
			positionRider(entity);
	}

	@Override
	public Vec3 getContactPointMotion(Vec3 globalContactPoint) {
		if (contraption instanceof TranslatingContraption)
			return getDeltaMovement();
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
	protected void readAdditional(CompoundTag compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);
		if (compound.contains("Controller")) // legacy
			controllerPos = NbtUtils.readBlockPos(compound.getCompound("Controller"));
		else
			controllerPos = NbtUtils.readBlockPos(compound.getCompound("ControllerRelative"))
				.offset(blockPosition());
		if (compound.contains("Axis"))
			rotationAxis = NBTHelper.readEnum(compound, "Axis", Axis.class);
		angle = compound.getFloat("Angle");
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);
		compound.put("ControllerRelative", NbtUtils.writeBlockPos(controllerPos.subtract(blockPosition())));
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
	public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	@Override
	public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
		return localPos;
	}

	public void setAngle(float angle) {
		this.angle = angle;

		if (!level().isClientSide())
			return;
		for (Entity entity : getPassengers())
			positionRider(entity);
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
	public void teleportTo(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	protected void tickContraption() {
		angleDelta = angle - prevAngle;
		prevAngle = angle;
		tickActors();

		if (controllerPos == null)
			return;
		if (!level().isLoaded(controllerPos))
			return;
		IControlContraption controller = getController();
		if (controller == null) {
			discard();
			return;
		}
		if (!controller.isAttachedTo(this)) {
			controller.attach(this);
			if (level().isClientSide)
				setPos(getX(), getY(), getZ());
		}
	}

	@Override
	protected boolean shouldActorTrigger(MovementContext context, StructureBlockInfo blockInfo, MovementBehaviour actor,
		Vec3 actorPosition, BlockPos gridPosition) {
		if (super.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition))
			return true;

		// Special activation timer for actors in the center of a bearing contraption
		if (!(contraption instanceof BearingContraption))
			return false;
		BearingContraption bc = (BearingContraption) contraption;
		Direction facing = bc.getFacing();
		Vec3 activeAreaOffset = actor.getActiveAreaOffset(context);
		if (!activeAreaOffset.multiply(VecHelper.axisAlingedPlaneOf(Vec3.atLowerCornerOf(facing.getNormal())))
			.equals(Vec3.ZERO))
			return false;
		if (!VecHelper.onSameAxis(blockInfo.pos(), BlockPos.ZERO, facing.getAxis()))
			return false;
		context.motion = Vec3.atLowerCornerOf(facing.getNormal())
			.scale(angleDelta / 360.0);
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
		if (!level().isLoaded(controllerPos))
			return null;
		BlockEntity be = level().getBlockEntity(controllerPos);
		if (!(be instanceof IControlContraption))
			return null;
		return (IControlContraption) be;
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		BlockPos offset = BlockPos.containing(getAnchorVec().add(.5, .5, .5));
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
	protected void handleStallInformation(double x, double y, double z, float angle) {
		setPosRaw(x, y, z);
		this.angle = this.prevAngle = angle;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void applyLocalTransforms(PoseStack matrixStack, float partialTicks) {
		float angle = getAngle(partialTicks);
		Axis axis = getRotationAxis();

		if (axis != null) {
			TransformStack.of(matrixStack)
					.nudge(getId())
					.center()
					.rotateDegrees(angle, axis)
					.uncenter();
		}
	}
}
