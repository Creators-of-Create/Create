package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftTileEntity;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class GantryContraptionEntity extends AbstractContraptionEntity {

	Direction movementAxis;
	double clientOffsetDiff;
	double axisMotion;

	public GantryContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
	}

	public static GantryContraptionEntity create(World world, Contraption contraption, Direction movementAxis) {
		GantryContraptionEntity entity = new GantryContraptionEntity(AllEntityTypes.GANTRY_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.movementAxis = movementAxis;
		return entity;
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof GantryContraption))
			return;

		double prevAxisMotion = axisMotion;
		if (world.isRemote) {
			clientOffsetDiff *= .75f;
			updateClientMotion();
		}

		checkPinionShaft();
		tickActors();
		Vec3d movementVec = getMotion();

		if (ContraptionCollider.collideBlocks(this)) {
			if (!world.isRemote)
				disassemble();
			return;
		}

		if (!isStalled() && ticksExisted > 2)
			move(movementVec.x, movementVec.y, movementVec.z);

		if (Math.signum(prevAxisMotion) != Math.signum(axisMotion) && prevAxisMotion != 0)
			contraption.stop(world);
		if (!world.isRemote && (prevAxisMotion != axisMotion || ticksExisted % 3 == 0))
			sendPacket();
	}

	protected void checkPinionShaft() {
		Vec3d movementVec;
		Direction facing = ((GantryContraption) contraption).getFacing();
		Vec3d currentPosition = getAnchorVec().add(.5, .5, .5);
		BlockPos gantryShaftPos = new BlockPos(currentPosition).offset(facing.getOpposite());

		TileEntity te = world.getTileEntity(gantryShaftPos);
		if (!(te instanceof GantryShaftTileEntity) || !AllBlocks.GANTRY_SHAFT.has(te.getBlockState())) {
			if (!world.isRemote) {
				setContraptionMotion(Vec3d.ZERO);
				disassemble();
			}
			return;
		}

		BlockState blockState = te.getBlockState();
		Direction direction = blockState.get(GantryShaftBlock.FACING);
		GantryShaftTileEntity gantryShaftTileEntity = (GantryShaftTileEntity) te;

		float pinionMovementSpeed = gantryShaftTileEntity.getPinionMovementSpeed();
		movementVec = new Vec3d(direction.getDirectionVec()).scale(pinionMovementSpeed);

		if (blockState.get(GantryShaftBlock.POWERED) || pinionMovementSpeed == 0) {
			setContraptionMotion(Vec3d.ZERO);
			if (!world.isRemote)
				disassemble();
			return;
		}

		Vec3d nextPosition = currentPosition.add(movementVec);
		double currentCoord = direction.getAxis()
			.getCoordinate(currentPosition.x, currentPosition.y, currentPosition.z);
		double nextCoord = direction.getAxis()
			.getCoordinate(nextPosition.x, nextPosition.y, nextPosition.z);

		if ((MathHelper.floor(currentCoord) + .5f < nextCoord != (pinionMovementSpeed * direction.getAxisDirection()
			.getOffset() < 0)))
			if (!gantryShaftTileEntity.canAssembleOn()) {
				setContraptionMotion(Vec3d.ZERO);
				if (!world.isRemote)
					disassemble();
				return;
			}

		if (world.isRemote)
			return;
		
		axisMotion = pinionMovementSpeed;
		setContraptionMotion(movementVec);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
		NBTHelper.writeEnum(compound, "GantryAxis", movementAxis);
		super.writeAdditional(compound, spawnPacket);
	}

	protected void readAdditional(CompoundNBT compound, boolean spawnData) {
		movementAxis = NBTHelper.readEnum(compound, "GantryAxis", Direction.class);
		super.readAdditional(compound, spawnData);
	}

	@Override
	public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
		return localPos;
	}

	@Override
	public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
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
	public void setPositionAndUpdate(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	@Override
	protected void handleStallInformation(float x, float y, float z, float angle) {
		setPos(x, y, z);
		clientOffsetDiff = 0;
	}

	@Override
	public ContraptionRotationState getRotationState() {
		return ContraptionRotationState.NONE;
	}

	@Override
	public void doLocalTransforms(float partialTicks, MatrixStack[] matrixStacks) { }

	public void updateClientMotion() {
		float modifier = movementAxis.getAxisDirection()
			.getOffset();
		setContraptionMotion(new Vec3d(movementAxis.getDirectionVec())
			.scale((axisMotion + clientOffsetDiff * modifier / 2f) * ServerSpeedProvider.get()));
	}

	public double getAxisCoord() {
		Vec3d anchorVec = getAnchorVec();
		return movementAxis.getAxis()
			.getCoordinate(anchorVec.x, anchorVec.y, anchorVec.z);
	}

	public void sendPacket() {
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new GantryContraptionUpdatePacket(getEntityId(), getAxisCoord(), axisMotion));
	}

	@OnlyIn(Dist.CLIENT)
	public static void handlePacket(GantryContraptionUpdatePacket packet) {
		Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
		if (!(entity instanceof GantryContraptionEntity))
			return;
		GantryContraptionEntity ce = (GantryContraptionEntity) entity;
		ce.axisMotion = packet.motion;
		ce.clientOffsetDiff = packet.coord - ce.getAxisCoord();
	}

}
