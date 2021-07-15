package com.simibubi.create.content.contraptions.components.mixer;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.fluids.FluidFX;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.ITriggerable;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MechanicalMixerTileEntity extends BasinOperatingTileEntity {

	private static final Object shapelessOrMixingRecipesKey = new Object();

	public int runningTicks;
	public int processingTicks;
	public boolean running;

	public MechanicalMixerTileEntity(TileEntityType<? extends MechanicalMixerTileEntity> type) {
		super(type);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		int localTick;
		float offset = 0;
		if (running) {
			if (runningTicks < 20) {
				localTick = runningTicks;
				float num = (localTick + partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			} else if (runningTicks <= 20) {
				offset = 1;
			} else {
				localTick = 40 - runningTicks;
				float num = (localTick - partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			}
		}
		return offset + 7 / 16f;
	}

	public float getRenderedHeadRotationSpeed(float partialTicks) {
		float speed = getSpeed();
		if (running) {
			if (runningTicks < 15) {
				return speed;
			}
			if (runningTicks <= 20) {
				return speed * 2;
			}
			return speed;
		}
		return speed / 2;
	}

	@Override
	public AxisAlignedBB makeRenderBoundingBox() {
		return new AxisAlignedBB(worldPosition).expandTowards(0, -1.5, 0);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		runningTicks = compound.getInt("Ticks");
		super.fromTag(state, compound, clientPacket);

		if (clientPacket && hasLevel())
			getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Ticks", runningTicks);
		super.write(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (runningTicks >= 40) {
			running = false;
			runningTicks = 0;
			return;
		}

		float speed = Math.abs(getSpeed());
		if (running && level != null) {
			if (level.isClientSide && runningTicks == 20)
				renderParticles();

			if ((!level.isClientSide || isVirtual()) && runningTicks == 20) {
				if (processingTicks < 0) {
					processingTicks = MathHelper.clamp((MathHelper.log2((int) (512 / speed))) * 15 + 1, 1, 512);

					Optional<BasinTileEntity> basin = getBasin();
					if (basin.isPresent()) {
						Couple<SmartFluidTankBehaviour> tanks = basin.get()
							.getTanks();
						if (!tanks.getFirst()
							.isEmpty()
							|| !tanks.getSecond()
								.isEmpty())
							level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
								SoundCategory.BLOCKS, .75f, speed < 65 ? .75f : 1.5f);
					}

				} else {
					processingTicks--;
					if (processingTicks == 0) {
						runningTicks++;
						processingTicks = -1;
						applyBasinRecipe();
						sendData();
					}
				}
			}

			if (runningTicks != 20)
				runningTicks++;
		}
	}

	public void renderParticles() {
		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent() || level == null)
			return;

		for (SmartInventory inv : basin.get()
			.getInvs()) {
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stackInSlot = inv.getItem(slot);
				if (stackInSlot.isEmpty())
					continue;
				ItemParticleData data = new ItemParticleData(ParticleTypes.ITEM, stackInSlot);
				spillParticle(data);
			}
		}

		for (SmartFluidTankBehaviour behaviour : basin.get()
			.getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				spillParticle(FluidFX.getFluidParticle(tankSegment.getRenderedFluid()));
			}
		}
	}

	protected void spillParticle(IParticleData data) {
		float angle = level.random.nextFloat() * 360;
		Vector3d offset = new Vector3d(0, 0, 0.25f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		Vector3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y)
			.add(0, .25f, 0);
		Vector3d center = offset.add(VecHelper.getCenterOf(worldPosition));
		target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
		level.addParticle(data, center.x, center.y - 1.75f, center.z, target.x, target.y, target.z);
	}

	@Override
	protected List<IRecipe<?>> getMatchingRecipes() {
		List<IRecipe<?>> matchingRecipes = super.getMatchingRecipes();

		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent())
			return matchingRecipes;
		IItemHandler availableItems = basin.get()
			.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(null);
		if (availableItems == null)
			return matchingRecipes;

		for (int i = 0; i < availableItems.getSlots(); i++) {
			ItemStack stack = availableItems.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			List<MixingRecipe> list = PotionMixingRecipeManager.ALL.get(stack.getItem());
			if (list == null)
				continue;
			for (MixingRecipe mixingRecipe : list)
				if (matchBasinRecipe(mixingRecipe))
					matchingRecipes.add(mixingRecipe);
		}

		return matchingRecipes;
	}

	@Override
	protected <C extends IInventory> boolean matchStaticFilters(IRecipe<C> r) {
		return ((r.getSerializer() == IRecipeSerializer.SHAPELESS_RECIPE
			&& AllConfigs.SERVER.recipes.allowShapelessInMixer.get() && r.getIngredients()
				.size() > 1)
			|| r.getType() == AllRecipeTypes.MIXING.type);
	}

	@Override
	public void startProcessingBasin() {
		if (running && runningTicks <= 20)
			return;
		super.startProcessingBasin();
		running = true;
		runningTicks = 0;
	}

	@Override
	public boolean continueWithPreviousRecipe() {
		runningTicks = 20;
		return true;
	}

	@Override
	protected void onBasinRemoved() {
		if (!running)
			return;
		runningTicks = 40;
		running = false;
	}

	@Override
	protected Object getRecipeCacheKey() {
		return shapelessOrMixingRecipesKey;
	}

	@Override
	protected boolean isRunning() {
		return running;
	}

	@Override
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.of(AllTriggers.MIXER_MIX);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		super.tickAudio();

		// SoundEvents.BLOCK_STONE_BREAK
		boolean slow = Math.abs(getSpeed()) < 65;
		if (slow && AnimationTickHolder.getTicks() % 2 == 0) 
			return;
		if (runningTicks == 20) 
			AllSoundEvents.MIXING.playAt(level, worldPosition, .75f, 1, true);
	}

}
