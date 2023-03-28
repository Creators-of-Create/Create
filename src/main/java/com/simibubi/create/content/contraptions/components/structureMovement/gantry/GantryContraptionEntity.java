package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlockEntity;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

public class GantryContraptionEntity extends AbstractContraptionEntity {

	Direction movementAxis;
	double clientOffsetDiff;
	double axisMotion;

	public double sequencedOffsetLimit;

	public GantryContraptionEntity(EntityType<?> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
		sequencedOffsetLimit = -1;
	}

	public static GantryContraptionEntity create(Level world, Contraption contraption, Direction movementAxis) {
		GantryContraptionEntity entity = new GantryContraptionEntity(AllEntityTypes.GANTRY_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.movementAxis = movementAxis;
		return entity;
	}

	public void limitMovement(double maxOffset) {
		sequencedOffsetLimit = maxOffset;
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof GantryContraption))
			return;

		double prevAxisMotion = axisMotion;
		if (level.isClientSide) {
			clientOffsetDiff *= .75f;
			updateClientMotion();
		}

		checkPinionShaft();
		tickActors();
		Vec3 movementVec = getDeltaMovement();

		if (ContraptionCollider.collideBlocks(this)) {
			if (!level.isClientSide)
				disassemble();
			return;
		}

		if (!isStalled() && tickCount > 2) {
			if (sequencedOffsetLimit >= 0)
				movementVec = VecHelper.clampComponentWise(movementVec, (float) sequencedOffsetLimit);
			move(movementVec.x, movementVec.y, movementVec.z);
			if (sequencedOffsetLimit > 0)
				sequencedOffsetLimit = Math.max(0, sequencedOffsetLimit - movementVec.length());
		}

		if (Math.signum(prevAxisMotion) != Math.signum(axisMotion) && prevAxisMotion != 0)
			contraption.stop(level);
		if (!level.isClientSide && (prevAxisMotion != axisMotion || tickCount % 3 == 0))
			sendPacket();
	}

	@Override
	public void disassemble() {
		sequencedOffsetLimit = -1;
		super.disassemble();
	}

	protected void checkPinionShaft() {
		Vec3 movementVec;
		Direction facing = ((GantryContraption) contraption).getFacing();
		Vec3 currentPosition = getAnchorVec().add(.5, .5, .5);
		BlockPos gantryShaftPos = new BlockPos(currentPosition).relative(facing.getOpposite());

		BlockEntity be = level.getBlockEntity(gantryShaftPos);
		if (!(be instanceof GantryShaftBlockEntity) || !AllBlocks.GANTRY_SHAFT.has(be.getBlockState())) {
			if (!level.isClientSide) {
				setContraptionMotion(Vec3.ZERO);
				disassemble();
			}
			return;
		}

		BlockState blockState = be.getBlockState();
		Direction direction = blockState.getValue(GantryShaftBlock.FACING);
		GantryShaftBlockEntity gantryShaftBlockEntity = (GantryShaftBlockEntity) be;

		float pinionMovementSpeed = gantryShaftBlockEntity.getPinionMovementSpeed();
		if (blockState.getValue(GantryShaftBlock.POWERED) || pinionMovementSpeed == 0) {
			setContraptionMotion(Vec3.ZERO);
			if (!level.isClientSide)
				disassemble();
			return;
		}

		if (sequencedOffsetLimit >= 0)
			pinionMovementSpeed = (float) Mth.clamp(pinionMovementSpeed, -sequencedOffsetLimit, sequencedOffsetLimit);
		movementVec = Vec3.atLowerCornerOf(direction.getNormal())
			.scale(pinionMovementSpeed);

		Vec3 nextPosition = currentPosition.add(movementVec);
		double currentCoord = direction.getAxis()
			.choose(currentPosition.x, currentPosition.y, currentPosition.z);
		double nextCoord = direction.getAxis()
			.choose(nextPosition.x, nextPosition.y, nextPosition.z);

		if ((Mth.floor(currentCoord) + .5f < nextCoord != (pinionMovementSpeed * direction.getAxisDirection()
			.getStep() < 0)))
			if (!gantryShaftBlockEntity.canAssembleOn()) {
				setContraptionMotion(Vec3.ZERO);
				if (!level.isClientSide)
					disassemble();
				return;
			}

		if (level.isClientSide)
			return;

		axisMotion = pinionMovementSpeed;
		setContraptionMotion(movementVec);
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		NBTHelper.writeEnum(compound, "GantryAxis", movementAxis);
		if (sequencedOffsetLimit >= 0)
			compound.putDouble("SequencedOffsetLimit", sequencedOffsetLimit);
		super.writeAdditional(compound, spawnPacket);
	}

	protected void readAdditional(CompoundTag compound, boolean spawnData) {
		movementAxis = NBTHelper.readEnum(compound, "GantryAxis", Direction.class);
		sequencedOffsetLimit =
			compound.contains("SequencedOffsetLimit") ? compound.getDouble("SequencedOffsetLimit") : -1;
		super.readAdditional(compound, spawnData);
	}

	@Override
	public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
		return localPos;
	}

	@Override
	public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
		return localPos;
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		return new StructureTransform(new BlockPos(getAnchorVec().add(.5, .5, .5)), 0, 0, 0);
	}

	@Override
	protected float getStalledAngle() {
		return 0;
	}

	@Override
	public void teleportTo(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	@Override
	protected void handleStallInformation(double x, double y, double z, float angle) {
		setPosRaw(x, y, z);
		clientOffsetDiff = 0;
	}

	@Override
	public ContraptionRotationState getRotationState() {
		return ContraptionRotationState.NONE;
	}

	@Override
	public void applyLocalTransforms(PoseStack matrixStack, float partialTicks) {}

	public void updateClientMotion() {
		float modifier = movementAxis.getAxisDirection()
			.getStep();
		Vec3 motion = Vec3.atLowerCornerOf(movementAxis.getNormal())
			.scale((axisMotion + clientOffsetDiff * modifier / 2f) * ServerSpeedProvider.get());
		if (sequencedOffsetLimit >= 0)
			motion = VecHelper.clampComponentWise(motion, (float) sequencedOffsetLimit);
		setContraptionMotion(motion);
	}

	public double getAxisCoord() {
		Vec3 anchorVec = getAnchorVec();
		return movementAxis.getAxis()
			.choose(anchorVec.x, anchorVec.y, anchorVec.z);
	}

	public void sendPacket() {
		AllPackets.getChannel()
			.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
				new GantryContraptionUpdatePacket(getId(), getAxisCoord(), axisMotion, sequencedOffsetLimit));
	}

	@OnlyIn(Dist.CLIENT)
	public static void handlePacket(GantryContraptionUpdatePacket packet) {
		Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
		if (!(entity instanceof GantryContraptionEntity))
			return;
		GantryContraptionEntity ce = (GantryContraptionEntity) entity;
		ce.axisMotion = packet.motion;
		ce.clientOffsetDiff = packet.coord - ce.getAxisCoord();
		ce.sequencedOffsetLimit = packet.sequenceLimit;
	}

}
