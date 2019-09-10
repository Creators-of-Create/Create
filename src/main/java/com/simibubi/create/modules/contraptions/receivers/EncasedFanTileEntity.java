package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.CreateConfig.parameters;
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
import com.simibubi.create.Create;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class EncasedFanTileEntity extends KineticTileEntity implements ITickableTileEntity {

	public static final Map<Block, List<FanEffect>> effects = new HashMap<>();
	private static DamageSource damageSourceFire = new DamageSource("create.fan_fire").setDifficultyScaled()
			.setFireDamage();
	private static DamageSource damageSourceLava = new DamageSource("create.fan_lava").setDifficultyScaled()
			.setFireDamage();

	protected float pushDistance;
	protected float pullDistance;
	protected AxisAlignedBB frontBB;
	protected AxisAlignedBB backBB;
	
	protected int blockCheckCooldown;
	protected BlockState frontBlock;
	
	protected boolean findLoadedItems;
	protected boolean findFrontBlock;
	public List<ProcessedItem> items;

	public class ProcessedItem {
		private UUID loadedUUID;
		private int loadedTime;
		private ItemEntity entity;

		public ProcessedItem(UUID uuid, int timeLeft) {
			loadedUUID = uuid;
			loadedTime = timeLeft;
		}

		public ProcessedItem(ItemEntity item) {
			entity = item;
		}

		public void tick() {
			if (world.rand.nextInt(4) == 0) {
				Type processingType = getProcessingType();
				if (processingType == Type.BLASTING)
					world.addParticle(ParticleTypes.LARGE_SMOKE, entity.posX, entity.posY + .25f, entity.posZ, 0, 1/16f, 0);
				if (processingType == Type.SMOKING)
					world.addParticle(ParticleTypes.CLOUD, entity.posX, entity.posY + .25f, entity.posZ, 0, 1/16f, 0);
			}

			if (world.isRemote)
				return;

			Create.itemProcessingHandler.getProcessing(entity).process(entity);
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
		blockCheckCooldown = -1;
		findFrontBlock = true;
		frontBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		backBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		items = new ArrayList<>();
//		if (effects.isEmpty())
			initEffects();
	}

	private static void initEffects() {
		effects.clear();

		List<FanEffect> standardFX = new ArrayList<>(2);
		standardFX.add(new FanEffect(ParticleTypes.BUBBLE_POP, 1 / 4f, 1 / 4f, 1 / 3f, 1));
		standardFX.add(new FanEffect(new RedstoneParticleData(1, 1, 1, 1), 1 / 2f, 1 / 32f, 1/16f, 512f));
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
		updateFrontBlock();
		updateBBs();
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		super.writeToClient(tag);
		return tag;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);

		pushDistance = compound.getFloat("PushDistance");
		pullDistance = compound.getFloat("PullDistance");

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
		compound.putFloat("PushDistance", pushDistance);
		compound.putFloat("PullDistance", pullDistance);

		ListNBT itemsNBT = new ListNBT();
		for (ProcessedItem item : items) {
			CompoundNBT itemNBT = new CompoundNBT();
			itemNBT.put("UUID", NBTUtil.writeUniqueId(item.entity.getUniqueID()));
			itemNBT.putInt("TimeLeft", Create.itemProcessingHandler.getProcessing(item.entity).timeRemaining);
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
			}
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

		updateProcessedItems(frontEntities);

		if (world.isRemote) {
			makeParticles();
			return;
		}

		discoverEntitiesAfterLoad(frontEntities);
	}

	public void updateProcessedItems(List<Entity> frontEntities) {
		ArrayList<ProcessedItem> prevItems = new ArrayList<>(items);
		Iterator<ProcessedItem> itemIter = prevItems.iterator();

		if (canProcess()) {
			while (itemIter.hasNext()) {
				Iterator<Entity> entityIter = frontEntities.iterator();
				ProcessedItem item = itemIter.next();

				while (entityIter.hasNext()) {
					Entity e = entityIter.next();
					if (!(e instanceof ItemEntity)) {
						entityIter.remove();
						continue;
					}

					if (item.entity == e && e.isAlive()) {
						item.tick();
						entityIter.remove();
						itemIter.remove();
						continue;
					}
				}
			}
			// Add remaining
			for (Entity entity : frontEntities) {
				if (entity instanceof ItemEntity && canProcess((ItemEntity) entity)) {
					items.add(new ProcessedItem((ItemEntity) entity));
					if (!world.isRemote)
						Create.itemProcessingHandler.startProcessing((ItemEntity) entity,
								new InWorldProcessing(getProcessingType(), 100));
				}
			}
		}

		for (ProcessedItem lostItem : prevItems) {
			items.remove(lostItem);
			if (!world.isRemote && lostItem.entity != null)
				Create.itemProcessingHandler.stopProcessing(lostItem.entity);
		}
	}

	public void discoverEntitiesAfterLoad(List<Entity> frontEntities) {
		if (findLoadedItems) {
			findLoadedItems = false;
			Iterator<ProcessedItem> iterator = items.iterator();
			while (iterator.hasNext()) {
				ProcessedItem item = iterator.next();
				if (!canProcess())
					iterator.remove();

				for (Entity entity : frontEntities) {
					if (!(entity instanceof ItemEntity))
						continue;
					if (entity.getUniqueID().equals(item.loadedUUID)) {
						item.entity = (ItemEntity) entity;
						if (!world.isRemote && canProcess((ItemEntity) entity))
							Create.itemProcessingHandler.startProcessing((ItemEntity) entity,
									new InWorldProcessing(getProcessingType(), item.loadedTime));
					}
				}
				if (item.entity == null)
					iterator.remove();
			}
		}
	}

	protected boolean canProcess() {
		return getProcessingType() != null;
	}

	protected boolean canProcess(ItemEntity entity) {
		return canProcess() && new InWorldProcessing(getProcessingType(), 0).canProcess(entity);
	}

	protected InWorldProcessing.Type getProcessingType() {
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
		float modifier = entity.isSneaking() ? 4096f : 512f;
		float s = (float) (speed * 1 / modifier
				/ (entity.getPositionVec().distanceTo(center) / (push ? pushDistance : pullDistance)));
		Vec3d motion = entity.getMotion();
		float maxSpeedModifier = 5;
		double xIn = MathHelper.clamp(flow.getX() * s - motion.x, -maxSpeedModifier, maxSpeedModifier);
		double yIn = MathHelper.clamp(flow.getY() * s - motion.y, -maxSpeedModifier, maxSpeedModifier);
		double zIn = MathHelper.clamp(flow.getZ() * s - motion.z, -maxSpeedModifier, maxSpeedModifier);
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

		if (!hasFx)
			for (FanEffect fx : effects.get(Blocks.AIR))
				fx.render(directionVec, true, this);

		for (FanEffect fx : effects.get(Blocks.AIR))
			fx.render(directionVec, false, this);
	}

}
