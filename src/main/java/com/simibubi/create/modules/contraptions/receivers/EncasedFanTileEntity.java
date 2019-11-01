package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.CreateConfig.parameters;
import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlockTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.CreateClient;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.logistics.InWorldProcessing;
import com.simibubi.create.modules.logistics.InWorldProcessing.Type;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class EncasedFanTileEntity extends KineticTileEntity {

	private static DamageSource damageSourceFire = new DamageSource("create.fan_fire").setDifficultyScaled()
			.setFireDamage();
	private static DamageSource damageSourceLava = new DamageSource("create.fan_lava").setDifficultyScaled()
			.setFireDamage();
	private static EncasedFanParticleHandler particleHandler;

	protected float pushDistance;
	protected float pullDistance;
	protected AxisAlignedBB frontBB;
	protected AxisAlignedBB backBB;

	protected int blockCheckCooldown;
	protected boolean findFrontBlock;
	protected BlockState frontBlock;
	protected boolean isGenerator;

	public EncasedFanTileEntity() {
		super(AllTileEntities.ENCASED_FAN.type);
		blockCheckCooldown = -1;
		findFrontBlock = true;
		frontBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		backBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		particleHandler = CreateClient.fanParticles;
		isGenerator = false;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		updateFrontBlock();
		updateBBs();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		isGenerator = compound.getBoolean("Generating");
		pushDistance = compound.getFloat("PushDistance");
		pullDistance = compound.getFloat("PullDistance");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Generating", isGenerator);
		compound.putFloat("PushDistance", pushDistance);
		compound.putFloat("PullDistance", pullDistance);
		return super.write(compound);
	}

	@Override
	public boolean isSource() {
		return isGenerator;
	}

	@Override
	public float getAddedStressCapacity() {
		return 50;
	}

	public void updateGenerator() {
		boolean shouldGenerate = world.isBlockPowered(pos) && world.isBlockPresent(pos.down()) && blockBelowIsHot();
		if (shouldGenerate == isGenerator)
			return;

		isGenerator = shouldGenerate;
		if (isGenerator) {
			notifyStressCapacityChange(getAddedStressCapacity());
			removeSource();
		} else {
			notifyStressCapacityChange(0);
		}
		applyNewSpeed(isGenerator ? CreateConfig.parameters.generatingFanSpeed.get() : 0);
		sendData();
	}

	public boolean blockBelowIsHot() {
		return world.getBlockState(pos.down()).getBlock().isIn(AllBlockTags.FAN_HEATERS.tag);
	}

	protected void updateReachAndForce() {
		if (getWorld() == null)
			return;
		if (world.isRemote)
			return;

		float speed = Math.abs(this.speed);
		float distanceFactor = Math.min(speed / parameters.fanRotationArgmax.get(), 1);

		pushDistance = MathHelper.lerp(distanceFactor, 3, parameters.fanMaxPushDistance.get());
		pullDistance = MathHelper.lerp(distanceFactor, 1.5f, parameters.fanMaxPullDistance.get());

		Direction direction = getAirFlow();
		if (speed != 0) {
			for (int distance = 1; distance <= pushDistance; distance++) {
				if (!EncasedFanBlock.canAirPassThrough(world, getPos().offset(direction, distance), direction)) {
					pushDistance = distance - 1;
					break;
				}
			}
			for (int distance = 1; distance <= pullDistance; distance++) {
				if (!EncasedFanBlock.canAirPassThrough(world, getPos().offset(direction, -distance), direction)) {
					pullDistance = distance - 1;
					break;
				}
			}
			updateBBs();
		} else {
			frontBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
			backBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}

		sendData();
	}

	protected void updateBBs() {
		Direction flow = getAirFlow();
		if (flow == null)
			return;
		Vec3i flowVec = flow.getDirectionVec();
		float remainder = pushDistance - (int) pushDistance;
		frontBB = new AxisAlignedBB(pos.offset(flow), pos.offset(flow, (int) pushDistance))
				.expand(flowVec.getX() * remainder + 1, flowVec.getY() * remainder + 1, flowVec.getZ() * remainder + 1)
				.grow(.25f);
		remainder = pullDistance - (int) pullDistance;
		backBB = new AxisAlignedBB(pos.offset(flow, -(int) pullDistance), pos.offset(flow, -1))
				.expand(-flowVec.getX() * remainder + 1, -flowVec.getY() * remainder + 1,
						-flowVec.getZ() * remainder + 1)
				.grow(.25f);
	}

	public void updateFrontBlock() {
		Direction facing = getAirFlow();
		if (facing == null) {
			frontBlock = Blocks.AIR.getDefaultState();
			return;
		}
		BlockPos front = pos.offset(facing);
		if (world.isBlockPresent(front))
			frontBlock = world.getBlockState(front);
		updateReachAndForce();
	}

	public Direction getAirFlow() {
		if (speed == 0)
			return null;
		return Direction.getFacingFromAxisDirection(getBlockState().get(AXIS), speed > 0 ? POSITIVE : NEGATIVE);
	}

	@Override
	public void onSpeedChanged() {
		updateReachAndForce();
		updateFrontBlock();
	}

	@Override
	public void reActivateSource() {
		source = Optional.empty();
		applyNewSpeed(isGenerator ? CreateConfig.parameters.generatingFanSpeed.get() : 0);
	}

	@Override
	public void tick() {
		super.tick();

		if (speed == 0 || isGenerator)
			return;

		List<Entity> frontEntities = world.getEntitiesWithinAABBExcludingEntity(null, frontBB);
		for (Entity entity : frontEntities) {
			moveEntity(entity, true);
			processEntity(entity);
		}

		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, backBB)) {
			moveEntity(entity, false);
		}

		if (findFrontBlock) {
			findFrontBlock = false;
			updateFrontBlock();
		}

		if (!world.isRemote && blockCheckCooldown-- <= 0) {
			blockCheckCooldown = parameters.fanBlockCheckRate.get();
			updateReachAndForce();
		}

		if (world.isRemote) {
			particleHandler.makeParticles(this);
			return;
		}

	}

	public void processEntity(Entity entity) {
		if (InWorldProcessing.isFrozen())
			return;

		if (entity instanceof ItemEntity) {
			if (world.rand.nextInt(4) == 0) {
				Type processingType = getProcessingType();
				if (processingType == Type.BLASTING)
					world.addParticle(ParticleTypes.LARGE_SMOKE, entity.posX, entity.posY + .25f, entity.posZ, 0,
							1 / 16f, 0);
				if (processingType == Type.SMOKING)
					world.addParticle(ParticleTypes.CLOUD, entity.posX, entity.posY + .25f, entity.posZ, 0, 1 / 16f, 0);
				if (processingType == Type.SPLASHING)
					world.addParticle(ParticleTypes.BUBBLE_POP, entity.posX + (world.rand.nextFloat() - .5f) * .5f,
							entity.posY + .25f, entity.posZ + (world.rand.nextFloat() - .5f) * .5f, 0, 1 / 16f, 0);
			}

			if (world.isRemote)
				return;

			if (canProcess((ItemEntity) entity))
				InWorldProcessing.process((ItemEntity) entity, getProcessingType());

		} else {
			if (getProcessingType() == Type.SMOKING) {
				entity.setFire(2);
				entity.attackEntityFrom(damageSourceFire, 4);
			}
			if (getProcessingType() == Type.BLASTING) {
				entity.setFire(10);
				entity.attackEntityFrom(damageSourceLava, 8);
			}
			if (getProcessingType() == Type.SPLASHING) {
				if (entity.isBurning()) {
					entity.extinguish();
					world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
							SoundCategory.NEUTRAL, 0.7F,
							1.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
				}
			}
		}
	}

	protected boolean canProcess() {
		return getProcessingType() != null;
	}

	protected boolean canProcess(ItemEntity entity) {
		return canProcess() && InWorldProcessing.canProcess(entity, getProcessingType());
	}

	protected InWorldProcessing.Type getProcessingType() {
		if (frontBlock == null)
			return null;

		Block block = frontBlock.getBlock();

		if (block == Blocks.FIRE)
			return Type.SMOKING;
		if (block == Blocks.WATER)
			return Type.SPLASHING;
		if (block == Blocks.LAVA)
			return Type.BLASTING;

		return null;
	}

	protected void moveEntity(Entity entity, boolean push) {
		if ((entity instanceof ItemEntity) && AllBlocks.BELT.typeOf(world.getBlockState(entity.getPosition()))
				&& getAirFlow() != Direction.UP) {
			return;
		}

		Vec3d center = VecHelper.getCenterOf(pos);
		Vec3i flow = getAirFlow().getDirectionVec();

		float sneakModifier = entity.isSneaking() ? 4096f : 512f;
		float acceleration = (float) (speed * 1 / sneakModifier
				/ (entity.getPositionVec().distanceTo(center) / (push ? pushDistance : pullDistance)));
		Vec3d previousMotion = entity.getMotion();
		float maxAcceleration = 5;

		double xIn = MathHelper.clamp(flow.getX() * acceleration - previousMotion.x, -maxAcceleration, maxAcceleration);
		double yIn = MathHelper.clamp(flow.getY() * acceleration - previousMotion.y, -maxAcceleration, maxAcceleration);
		double zIn = MathHelper.clamp(flow.getZ() * acceleration - previousMotion.z, -maxAcceleration, maxAcceleration);

		entity.setMotion(
				previousMotion.add(new Vec3d(xIn, yIn, zIn).mul(flow.getX(), flow.getY(), flow.getZ()).scale(1 / 8f)));
		entity.fallDistance = 0;
	}

}
