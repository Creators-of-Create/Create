package com.simibubi.create.modules.contraptions.receivers;

import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class EncasedFanTileEntity extends KineticTileEntity implements ITickableTileEntity {

	public static final int PUSH_DISTANCE_MAX = 20;
	public static final int PULL_DISTANCE_MAX = 5;
	public static final int DISTANCE_ARGMAX = 6400;

	public static final float PUSH_FORCE_MAX = 20;
	public static final float PULL_FORCE_MAX = 10;
	public static final int FORCE_ARGMAX = 6400;

	public static final int BLOCK_CHECK_UPDATE_DELAY = 100;
	public static final Map<Block, List<FanEffect>> effects = new HashMap<>();

	private static DamageSource damageSourceFire = new DamageSource("create.fan_fire").setDifficultyScaled()
			.setFireDamage();
	private static DamageSource damageSourceLava = new DamageSource("create.fan_lava").setDifficultyScaled()
			.setFireDamage();

	protected BlockState frontBlock;
	protected BlockState backBlock;
	protected float pushDistance;
	protected float pullDistance;
	protected float pushForce;
	protected float pullForce;
	protected AxisAlignedBB frontBB;
	protected AxisAlignedBB backBB;
	protected int blockCheckCooldown;
	protected boolean findLoadedItems;
	public List<ProcessedItem> items;

	public class ProcessedItem {
		private UUID loadedUUID;
		private ItemEntity entity;
		private int processingTimeLeft;

		public ProcessedItem(UUID uuid, int timeLeft) {
			loadedUUID = uuid;
			processingTimeLeft = timeLeft;
		}

		public ProcessedItem(ItemEntity item) {
			entity = item;
			processingTimeLeft = 100;
		}

		public void tick() {
			world.addParticle(new RedstoneParticleData(1, 0, 1, 1), entity.posX, entity.posY, entity.posZ, 0, 0, 0);
			processingTimeLeft--;

			if (processingTimeLeft <= 0) {
				entity.setItem(new ItemStack(Items.COAL));
			}
		}

	}

	protected static class FanEffect {
		private IParticleData particle;
		private float density;
		private float chance;
		private float spread;
		private float speed;
		private Random r;

		public FanEffect(IParticleData particle, float density, float chance, float spread, float speed) {
			r = new Random();
			this.particle = particle;
			this.density = density;
			this.chance = chance;
			this.spread = spread;
			this.speed = speed;
		}

		public void render(Vec3i directionVec, boolean front, EncasedFanTileEntity te) {
			render(directionVec, front ? .5f : -te.pullDistance, front ? te.pushDistance : -.5f, te);
		}

		private void render(Vec3i directionVec, float start, float end, EncasedFanTileEntity te) {
			float x = directionVec.getX();
			float y = directionVec.getY();
			float z = directionVec.getZ();
			float speed = this.speed * Math.abs(te.speed) / 512f;

			for (float offset = start; offset < end; offset += density) {
				if (r.nextFloat() > chance)
					continue;
				float xs = rollOffset() * spread;
				float ys = rollOffset() * spread;
				float zs = rollOffset() * spread;
				float xs2 = rollOffset() * spread;
				float ys2 = rollOffset() * spread;
				float zs2 = rollOffset() * spread;
				te.world.addParticle(particle, te.pos.getX() + .5f + x * offset + xs2,
						te.pos.getY() + .5f + y * offset + ys2, te.pos.getZ() + .5f + z * offset + zs2, x * speed + xs,
						y * speed + ys, z * speed + zs);
			}
		}

		private float rollOffset() {
			return (r.nextFloat() - .5f) * 2;
		}
	}

	public EncasedFanTileEntity() {
		super(AllTileEntities.ENCASED_FAN.type);
		blockCheckCooldown = BLOCK_CHECK_UPDATE_DELAY;
		frontBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		backBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		items = new ArrayList<>();
		if (effects.isEmpty())
			initEffects();
	}

	private static void initEffects() {
		effects.clear();

		List<FanEffect> standardFX = new ArrayList<>(2);
		standardFX.add(new FanEffect(ParticleTypes.BUBBLE_POP, 1 / 4f, 1 / 8f, 1 / 8f, 1));
		standardFX.add(new FanEffect(new RedstoneParticleData(1, 1, 1, 1), 1 / 2f, 1 / 32f, 0f, 512f));
		effects.put(Blocks.AIR, standardFX);

		List<FanEffect> waterFX = new ArrayList<>(2);
		waterFX.add(new FanEffect(new BlockParticleData(ParticleTypes.BLOCK, Blocks.WATER.getDefaultState()), 1 / 4f,
				1 / 2f, 1 / 4f, 1));
		waterFX.add(new FanEffect(ParticleTypes.SPLASH, 1 / 4f, 1 / 2f, 0.5f, 1));
		effects.put(Blocks.WATER, waterFX);

		List<FanEffect> fireFX = new ArrayList<>(2);
		fireFX.add(new FanEffect(ParticleTypes.LARGE_SMOKE, 1 / 4f, 1 / 8f, 0.125f, .5f));
		fireFX.add(new FanEffect(ParticleTypes.FLAME, 1 / 4f, 1 / 8f, 1 / 32f, 1 / 256f));
		effects.put(Blocks.FIRE, fireFX);

		List<FanEffect> lavaFX = new ArrayList<>(3);
		lavaFX.add(new FanEffect(new BlockParticleData(ParticleTypes.BLOCK, Blocks.LAVA.getDefaultState()), 1 / 4f,
				1 / 2f, 1 / 4f, 1));
		lavaFX.add(new FanEffect(ParticleTypes.LAVA, 1 / 2f, 1 / 32f, 0, .25f));
		lavaFX.add(new FanEffect(ParticleTypes.FLAME, 1 / 4f, 1 / 32f, 1 / 32f, 1 / 256f));
		effects.put(Blocks.LAVA, lavaFX);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		pushDistance = tag.getFloat("PushDistance");
		pullDistance = tag.getFloat("PullDistance");
		pushForce = tag.getFloat("PushForce");
		pullForce = tag.getFloat("PullForce");
		updateBothNeighbours();
		updateBBs();
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		super.writeToClient(tag);
		tag.putFloat("PushDistance", pushDistance);
		tag.putFloat("PullDistance", pullDistance);
		tag.putFloat("PushForce", pushForce);
		tag.putFloat("PullForce", pullForce);
		return tag;
	}

	@Override
	public void onLoad() {
		blockCheckCooldown = 0;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		ListNBT itemsNBT = compound.getList("Items", 10);
		items.clear();
		for (INBT iNBT : itemsNBT) {
			CompoundNBT itemNBT = (CompoundNBT) iNBT;
			items.add(new ProcessedItem(NBTUtil.readUniqueId(itemNBT.getCompound("UUID")), itemNBT.getInt("TimeLeft")));
		}
		findLoadedItems = true;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT itemsNBT = new ListNBT();
		for (ProcessedItem item : items) {
			CompoundNBT itemNBT = new CompoundNBT();
			itemNBT.put("UUID", NBTUtil.writeUniqueId(item.entity.getUniqueID()));
			itemNBT.putInt("TimeLeft", item.processingTimeLeft);
			itemsNBT.add(itemNBT);
		}
		compound.put("Items", itemsNBT);
		return super.write(compound);
	}

	protected void updateReachAndForce() {
		if (getWorld() == null)
			return;
		if (world.isRemote)
			return;

		float speed = Math.abs(this.speed);
		float distanceFactor = Math.min(speed / DISTANCE_ARGMAX, 1);
		float forceFactor = Math.min(speed / FORCE_ARGMAX, 1);

		pushDistance = MathHelper.lerp(distanceFactor, 3, PUSH_DISTANCE_MAX);
		pullDistance = MathHelper.lerp(distanceFactor, 1.5f, PULL_DISTANCE_MAX);
		pushForce = MathHelper.lerp(forceFactor, 1, PUSH_FORCE_MAX);
		pullForce = MathHelper.lerp(forceFactor, 1, PULL_FORCE_MAX);

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

	public void updateBothNeighbours() {
		Axis axis = getBlockState().get(AXIS);
		Direction frontFacing = Direction.getFacingFromAxis(POSITIVE, axis);
		Direction backFacing = Direction.getFacingFromAxis(NEGATIVE, axis);
		BlockPos front = pos.offset(frontFacing);
		BlockPos back = pos.offset(backFacing);
		if (world.isBlockPresent(front))
			setNeighbour(frontFacing, world.getBlockState(front));
		if (world.isBlockPresent(back))
			setNeighbour(backFacing, world.getBlockState(back));
	}

	public void setNeighbour(Direction direction, BlockState neighbourState) {
		if (direction.getAxisDirection() == NEGATIVE)
			backBlock = neighbourState;
		else
			frontBlock = neighbourState;
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
	}

	@Override
	public void tick() {
		if (speed == 0)
			return;

		List<Entity> frontEntities = world.getEntitiesWithinAABBExcludingEntity(null, frontBB);
		for (Entity entity : frontEntities) {
			moveEntity(entity, true);
			if (!(entity instanceof ItemEntity)) {
				if (frontBlock != null && frontBlock.getBlock() == Blocks.FIRE) {
					entity.setFire(2);
					entity.attackEntityFrom(damageSourceFire, 4);
				}
				if (frontBlock != null && frontBlock.getBlock() == Blocks.LAVA) {
					entity.setFire(10);
					entity.attackEntityFrom(damageSourceLava, 8);
				}
			} else {
				boolean missing = true;
				for (ProcessedItem processed : items) {
					if (processed.entity == entity) {
						processed.tick();
						missing = false;
						break;
					}
				}
				if (missing) {
					items.add(new ProcessedItem((ItemEntity) entity));
				}
			}
		}
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, backBB)) {
			moveEntity(entity, false);
		}

		if (world.isRemote) {
			makeParticles();
			return;
		}

		if (blockCheckCooldown-- <= 0) {
			blockCheckCooldown = BLOCK_CHECK_UPDATE_DELAY;
			updateReachAndForce();
		}

		if (findLoadedItems) {
			findLoadedItems = false;
			Iterator<ProcessedItem> iterator = items.iterator();
			while (iterator.hasNext()) {
				ProcessedItem item = iterator.next();
				for (Entity entity : frontEntities) {
					if (!(entity instanceof ItemEntity))
						continue;
					if (entity.getUniqueID().equals(item.loadedUUID))
						item.entity = (ItemEntity) entity;
				}
				if (item.entity == null)
					iterator.remove();
			}
		}

		Iterator<ProcessedItem> iterator = items.iterator();
		while (iterator.hasNext())
			if (!iterator.next().entity.getBoundingBox().intersects(frontBB))
				iterator.remove();

	}

	protected void moveEntity(Entity entity, boolean push) {
		if ((entity instanceof ItemEntity) && AllBlocks.BELT.typeOf(world.getBlockState(entity.getPosition()))) {
			return;
		}

		Vec3d center = VecHelper.getCenterOf(pos);
		Vec3i flow = getAirFlow().getDirectionVec();
		float modifier = entity.isSneaking() ? 4096f : 512f;
		float s = (float) (speed * 1 / modifier
				/ (entity.getPositionVec().distanceTo(center) / (push ? pushDistance : pullDistance)));
		Vec3d motion = entity.getMotion();
		double xIn = flow.getX() * s - motion.x;
		double yIn = flow.getY() * s - motion.y;
		double zIn = flow.getZ() * s - motion.z;
		entity.setMotion(motion.add(new Vec3d(xIn, yIn, zIn).mul(flow.getX(), flow.getY(), flow.getZ()).scale(1 / 8f)));
		entity.fallDistance = 0;
	}

	protected void makeParticles() {
		Direction direction = getAirFlow();
		Vec3i directionVec = direction.getDirectionVec();

		boolean hasFx = false;
		if (frontBlock != null) {
			if (effects.containsKey(frontBlock.getBlock())) {
				hasFx = true;
				for (FanEffect fx : effects.get(frontBlock.getBlock()))
					fx.render(directionVec, true, this);
			}
		}
		if (backBlock != null && !hasFx) {
			if (effects.containsKey(backBlock.getBlock())) {
				hasFx = true;
				for (FanEffect fx : effects.get(backBlock.getBlock()))
					fx.render(directionVec, true, this);
			}
		}

		if (!hasFx)
			for (FanEffect fx : effects.get(Blocks.AIR))
				fx.render(directionVec, true, this);

		for (FanEffect fx : effects.get(Blocks.AIR))
			fx.render(directionVec, false, this);
	}

}
