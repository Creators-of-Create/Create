package com.simibubi.create.modules.contraptions.components.fan;

import static com.simibubi.create.CreateConfig.parameters;

import com.simibubi.create.AllBlockTags;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class EncasedFanTileEntity extends GeneratingKineticTileEntity {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean isGenerator;
	protected boolean updateAirFlow;

	public EncasedFanTileEntity() {
		super(AllTileEntities.ENCASED_FAN.type);
		isGenerator = false;
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		airCurrent.rebuild();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		isGenerator = compound.getBoolean("Generating");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Generating", isGenerator);
		return super.write(compound);
	}

	@Override
	public float getAddedStressCapacity() {
		return isGenerator ? super.getAddedStressCapacity() : 0;
	}

	@Override
	public float getGeneratedSpeed() {
		return isGenerator ? CreateConfig.parameters.generatingFanSpeed.get() : 0;
	}

	public void updateGenerator() {
		boolean shouldGenerate = world.isBlockPowered(pos) && world.isBlockPresent(pos.down()) && blockBelowIsHot();
		if (shouldGenerate == isGenerator)
			return;

		isGenerator = shouldGenerate;
		updateGeneratedRotation();
	}

	public boolean blockBelowIsHot() {
		return world.getBlockState(pos.down()).getBlock().isIn(AllBlockTags.FAN_HEATERS.tag);
	}

	public float getMaxDistance() {
		float speed = Math.abs(this.getSpeed());
		float distanceFactor = Math.min(speed / parameters.fanRotationArgmax.get(), 1);
		float pushDistance = MathHelper.lerp(distanceFactor, 3, parameters.fanMaxPushDistance.get());
		float pullDistance = MathHelper.lerp(distanceFactor, 1.5f, parameters.fanMaxPullDistance.get());
		return this.getSpeed() > 0 ? pushDistance : pullDistance;
	}

	public Direction getAirFlowDirection() {
		if (getSpeed() == 0)
			return null;
		Direction facing = getBlockState().get(BlockStateProperties.FACING);
		return getSpeed() > 0 ? facing : facing.getOpposite();
	}

	@Override
	public void onSpeedChanged() {
		super.onSpeedChanged();
		updateAirFlow = true;
	}

	public void blockInFrontChanged() {
		updateAirFlow = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isRemote && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = parameters.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow = false;
			airCurrent.rebuild();
			sendData();
		}

		if (getSpeed() == 0 || isGenerator)
			return;

		if (entitySearchCooldown-- <= 0) {
			entitySearchCooldown = 5;
			airCurrent.findEntities();
		}

		airCurrent.tick();
	}

//	public void processEntity(Entity entity) {
//		if (InWorldProcessing.isFrozen())
//			return;
//
//		if (entity instanceof ItemEntity) {
//			if (world.rand.nextInt(4) == 0) {
//				Type processingType = getProcessingType();
//				if (processingType == Type.BLASTING)
//					world.addParticle(ParticleTypes.LARGE_SMOKE, entity.posX, entity.posY + .25f, entity.posZ, 0,
//							1 / 16f, 0);
//				if (processingType == Type.SMOKING)
//					world.addParticle(ParticleTypes.CLOUD, entity.posX, entity.posY + .25f, entity.posZ, 0, 1 / 16f, 0);
//				if (processingType == Type.SPLASHING)
//					world.addParticle(ParticleTypes.BUBBLE_POP, entity.posX + (world.rand.nextFloat() - .5f) * .5f,
//							entity.posY + .25f, entity.posZ + (world.rand.nextFloat() - .5f) * .5f, 0, 1 / 16f, 0);
//			}
//
//			if (world.isRemote)
//				return;
//
//			if (canProcess((ItemEntity) entity))
//				InWorldProcessing.applyProcessing((ItemEntity) entity, getProcessingType());
//
//		} else {
//			if (getProcessingType() == Type.SMOKING) {
//				entity.setFire(2);
//				entity.attackEntityFrom(damageSourceFire, 4);
//			}
//			if (getProcessingType() == Type.BLASTING) {
//				entity.setFire(10);
//				entity.attackEntityFrom(damageSourceLava, 8);
//			}
//			if (getProcessingType() == Type.SPLASHING) {
//				if (entity.isBurning()) {
//					entity.extinguish();
//					world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
//							SoundCategory.NEUTRAL, 0.7F,
//							1.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
//				}
//			}
//		}
//	}
//
//	protected boolean canProcess() {
//		return getProcessingType() != null;
//	}
//
//	protected boolean canProcess(ItemEntity entity) {
//		return canProcess() && InWorldProcessing.canProcess(entity, getProcessingType());
//	}
//
//	protected void moveEntity(Entity entity, boolean push) {
//		if ((entity instanceof ItemEntity) && AllBlocks.BELT.typeOf(world.getBlockState(entity.getPosition()))
//				&& getAirFlowDirection() != Direction.UP) {
//			return;
//		}
//
//		Vec3d center = VecHelper.getCenterOf(pos);
//		Vec3i flow = getAirFlowDirection().getDirectionVec();
//
//		float sneakModifier = entity.isSneaking() ? 4096f : 512f;
//		float acceleration = (float) (getSpeed() * 1 / sneakModifier
//				/ (entity.getPositionVec().distanceTo(center) / (push ? pushDistance : pullDistance)));
//		Vec3d previousMotion = entity.getMotion();
//		float maxAcceleration = 5;
//
//		double xIn = MathHelper.clamp(flow.getX() * acceleration - previousMotion.x, -maxAcceleration, maxAcceleration);
//		double yIn = MathHelper.clamp(flow.getY() * acceleration - previousMotion.y, -maxAcceleration, maxAcceleration);
//		double zIn = MathHelper.clamp(flow.getZ() * acceleration - previousMotion.z, -maxAcceleration, maxAcceleration);
//
//		entity.setMotion(
//				previousMotion.add(new Vec3d(xIn, yIn, zIn).mul(flow.getX(), flow.getY(), flow.getZ()).scale(1 / 8f)));
//		entity.fallDistance = 0;
//	}

}
