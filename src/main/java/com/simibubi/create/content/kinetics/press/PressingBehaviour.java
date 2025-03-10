package com.simibubi.create.content.kinetics.press;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.itemprocessing.ICanProcessItems;
import com.simibubi.create.content.itemprocessing.specifics.IProduceParticles;
import com.simibubi.create.content.itemprocessing.specifics.press.PressProcessingSpecifics;
import com.simibubi.create.content.processing.ProcessingMode;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PressingBehaviour extends ICanProcessItems<PressProcessingSpecifics> implements IProduceParticles {

	public static final int CYCLE = 240;
	public static final int ENTITY_SCAN = 10;

	private final List<ItemStack> particleItems = new ArrayList<>();

	private final PressProcessingSpecifics specifics;

	/**
	 * to be shard with client
	 */
	private boolean finished;

	/**
	 * Current processing mode
	 */
	private ProcessingMode mode;


	public <T extends SmartBlockEntity & PressProcessingSpecifics> PressingBehaviour(T be) {
		super(PressingBehaviour.CYCLE, PressingBehaviour.ENTITY_SCAN, be, be);
		this.specifics = be;
		mode = ProcessingMode.WORLD;
		BeltPressingCallbacks callbacks = new BeltPressingCallbacks();
		whenItemEnters(( s, i) -> callbacks.onItemReceived(s, i, this));
		whileItemHeld((s, i) -> callbacks.whenItemHeld(s, i, this));
	}

	@Override
	public void read(CompoundTag compound, Provider registries, boolean clientPacket) {
		setProcessing(compound.getBoolean("Running"));
		mode = ProcessingMode.values()[compound.getInt("Mode")];
		finished = compound.getBoolean("Finished");
		setProcessTicks(compound.getInt("Ticks"));

		super.read(compound, registries, clientPacket);

		if (clientPacket) {
			NBTHelper.iterateCompoundList(compound.getList("ParticleItems", Tag.TAG_COMPOUND),
				c -> particleItems.add(ItemStack.parseOptional(registries, c)));
			spawnParticles();
		}
	}

	@Override
	public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
		compound.putBoolean("Running", isProcessing());
		compound.putInt("Mode", mode.ordinal());
		compound.putBoolean("Finished", finished);
		compound.putInt("Ticks", getFinishedTicks());
		super.write(compound, registries, clientPacket);

		if (clientPacket) {
			compound.put("ParticleItems", NBTHelper.writeCompoundList(particleItems, s -> (CompoundTag) s.saveOptional(registries)));
			particleItems.clear();
		}
	}

	@Override
	public void onProcessStarted(ProcessingMode mode) {
		this.mode = mode;
		particleItems.clear();
		blockEntity.sendData();
	}

	public float modeToHeadOffset() {
		return switch (mode) {
			case BELT:
				yield 19f / 16f;
			case BASIN:
				yield 22f / 16f;
			case WORLD:
				yield 1;
		};
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (!isProcessing())
			return 0;
		int runningTicks = Math.abs(this.getFinishedTicks());
		float ticks = Mth.lerp(partialTicks, this.getPrevFinishedTicks(), runningTicks);
		if (runningTicks < (CYCLE * 2) / 3)
			return (float) Mth.clamp(Math.pow(ticks / CYCLE * 2, 3), 0, 1);
		return Mth.clamp((CYCLE - ticks) / CYCLE * 3, 0, 1);
	}

	public boolean inWorld() {
		return mode == ProcessingMode.WORLD;
	}

	public boolean onBasin() {
		return mode == ProcessingMode.BASIN;
	}

	@Override
	public void modifyProcessingTicks(Level level, int prevTicks, int ticks, int cycle) {
//		if (prevTicks < CYCLE / 2 && ticks >= CYCLE / 2) {
//			setFinishedTicks(CYCLE / 2);
//			// Pause the ticks until a packet is received
//			if (level.isClientSide && !blockEntity.isVirtual())
//				setProcessTicks(-(CYCLE / 2));
//		}
	}

	@Override
	public void onProcessTick(Level level, int finishedTicks, int cycle) {
		BlockPos worldPosition = getPos();

		if (finishedTicks == cycle / 2 && specifics.getKineticSpeed() != 0) {
			if (inWorld())
				applyInWorld();
			if (onBasin())
				applyOnBasin();

			if (level.getBlockState(worldPosition.below(2))
				.getSoundType() == SoundType.WOOL)
				AllSoundEvents.MECHANICAL_PRESS_ACTIVATION_ON_BELT.playOnServer(level, worldPosition);
			else
				AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(level, worldPosition, .5f,
					.75f + (Math.abs(specifics.getKineticSpeed()) / 1024f));

			if (!level.isClientSide)
				blockEntity.sendData();
		}
	}

	@Override
	public void clearParticles() {
		particleItems.clear();
	}

	@Override
	public void addParticle(Particle particle) {
	}

	@Override
	public void addParticleItem(ItemStack itemStack) {
		particleItems.add(itemStack);
	}

	@Override
	public void onProcessedFinish() {
		this.finished = true;
		blockEntity.sendData();
	}

	protected void applyOnBasin() {
		Level level = getWorld();
		if (level.isClientSide)
			return;
		particleItems.clear();
		if (specifics.tryProcessInBasin(false))
			blockEntity.sendData();
	}

	protected void applyInWorld() {
		Level level = getWorld();
		BlockPos worldPosition = getPos();
		AABB bb = new AABB(worldPosition.below(1));
		boolean bulk = specifics.canProcessInBulk();

		particleItems.clear();

		if (level.isClientSide)
			return;

		for (Entity entity : level.getEntities(null, bb)) {
			if (!(entity instanceof ItemEntity itemEntity))
				continue;
			if (!entity.isAlive() || !entity.onGround())
				continue;

			if (specifics.tryProcessItemInWorld(itemEntity, false))
				blockEntity.sendData();
			if (!bulk)
				break;
		}
	}

	@Override
	public int getScaledProcessingTicks() {
		float speed = specifics.getKineticSpeed();
		if (speed == 0)
			return 0;
		return (int) Mth.lerp(Mth.clamp(Math.abs(speed) / 512f, 0, 1), 1, 60);
	}

	protected void spawnParticles() {
		if (particleItems.isEmpty())
			return;

		BlockPos worldPosition = getPos();

		if (mode == ProcessingMode.BASIN)
			particleItems
				.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(worldPosition.below(2)), stack));
		if (mode == ProcessingMode.BELT)
			particleItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.below(2))
				.add(0, 8 / 16f, 0), stack));
		if (mode == ProcessingMode.WORLD)
			particleItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.below(1))
				.add(0, -1 / 4f, 0), stack));

		particleItems.clear();
	}

	public void makePressingParticleEffect(Vec3 pos, ItemStack stack) {
		makePressingParticleEffect(pos, stack, specifics.getParticleAmount());
	}

	public void makePressingParticleEffect(Vec3 pos, ItemStack stack, int amount) {
		Level level = getWorld();
		if (level == null || !level.isClientSide)
			return;
		for (int i = 0; i < amount; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f)
				.multiply(1, 0, 1);
			motion = motion.add(0, amount != 1 ? 0.125f : 1 / 16f, 0);
			level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z, motion.x,
				motion.y, motion.z);
		}
	}

	public void makeCompactingParticleEffect(Vec3 pos, ItemStack stack) {
		Level level = getWorld();
		if (level == null || !level.isClientSide)
			return;
		for (int i = 0; i < 20; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .175f)
				.multiply(1, 0, 1);
			level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, motion.x,
				motion.y + .25f, motion.z);
		}
	}
}
