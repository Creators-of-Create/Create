package com.simibubi.create.content.contraptions.fluids;

import static net.minecraft.state.properties.BlockStateProperties.LEVEL_HONEY;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockFace;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OpenEndedPipe extends FlowSource {

	World world;
	BlockPos pos;
	AxisAlignedBB aoe;

	private OpenEndFluidHandler fluidHandler;
	private BlockPos outputPos;
	private boolean wasPulling;

	private FluidStack cachedFluid;
	private List<EffectInstance> cachedEffects;

	public OpenEndedPipe(BlockFace face) {
		super(face);
		fluidHandler = new OpenEndFluidHandler();
		outputPos = face.getConnectedPos();
		pos = face.getPos();
		aoe = new AxisAlignedBB(outputPos).expandTowards(0, -1, 0);
		if (face.getFace() == Direction.DOWN)
			aoe = aoe.expandTowards(0, -1, 0);
	}

	@Override
	public void manageSource(World world) {
		this.world = world;
	}

	private FluidStack removeFluidFromSpace(boolean simulate) {
		FluidStack empty = FluidStack.EMPTY;
		if (world == null)
			return empty;
		if (!world.isAreaLoaded(outputPos, 0))
			return empty;

		BlockState state = world.getBlockState(outputPos);
		FluidState fluidState = state.getFluidState();
		boolean waterlog = state.hasProperty(WATERLOGGED);

		if (state.hasProperty(LEVEL_HONEY) && state.getValue(LEVEL_HONEY) >= 5) {
			if (!simulate)
				world.setBlock(outputPos, state.setValue(LEVEL_HONEY, 0), 3);
			return new FluidStack(AllFluids.HONEY.get()
				.getSource(), 250);
		}

		if (!waterlog && !state.getMaterial()
			.isReplaceable())
			return empty;
		if (fluidState.isEmpty() || !fluidState.isSource())
			return empty;

		FluidStack stack = new FluidStack(fluidState.getType(), 1000);

		if (simulate)
			return stack;

		AllTriggers.triggerForNearbyPlayers(AllTriggers.PIPE_SPILL, world, pos, 5);

		if (waterlog) {
			world.setBlock(outputPos, state.setValue(WATERLOGGED, false), 3);
			world.getLiquidTicks()
				.scheduleTick(outputPos, Fluids.WATER, 1);
			return stack;
		}
		world.setBlock(outputPos, fluidState.createLegacyBlock()
			.setValue(FlowingFluidBlock.LEVEL, 14), 3);
		return stack;
	}

	private boolean provideFluidToSpace(FluidStack fluid, boolean simulate) {
		if (world == null)
			return false;
		if (!world.isAreaLoaded(outputPos, 0))
			return false;

		BlockState state = world.getBlockState(outputPos);
		FluidState fluidState = state.getFluidState();
		boolean waterlog = state.hasProperty(WATERLOGGED);

		if (!waterlog && !state.getMaterial()
			.isReplaceable())
			return false;
		if (fluid.isEmpty())
			return false;
		if (!FluidHelper.hasBlockState(fluid.getFluid())) {
			if (!simulate)
				applyEffects(world, fluid);
			return true;
		}

		if (!fluidState.isEmpty() && fluidState.getType() != fluid.getFluid()) {
			FluidReactions.handlePipeSpillCollision(world, outputPos, fluid.getFluid(), fluidState);
			return false;
		}

		if (fluidState.isSource())
			return false;
		if (waterlog && fluid.getFluid() != Fluids.WATER)
			return false;
		if (simulate)
			return true;

		if (world.dimensionType()
			.ultraWarm()
			&& fluid.getFluid()
				.is(FluidTags.WATER)) {
			int i = outputPos.getX();
			int j = outputPos.getY();
			int k = outputPos.getZ();
			world.playSound(null, i, j, k, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
				2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			return true;
		}

		AllTriggers.triggerForNearbyPlayers(AllTriggers.PIPE_SPILL, world, pos, 5);

		if (waterlog) {
			world.setBlock(outputPos, state.setValue(WATERLOGGED, true), 3);
			world.getLiquidTicks()
				.scheduleTick(outputPos, Fluids.WATER, 1);
			return true;
		}
		world.setBlock(outputPos, fluid.getFluid()
			.defaultFluidState()
			.createLegacyBlock(), 3);
		return true;
	}

	private boolean canApplyEffects(World world, FluidStack fluid) {
		Fluid fluidType = fluid.getFluid();
		if (fluidType.isSame(AllFluids.POTION.get()))
			return true;
		if (Tags.Fluids.MILK.contains(fluidType))
			return true;
		return false;
	}

	private void applyEffects(World world, FluidStack fluid) {
		Fluid fluidType = fluid.getFluid();

		if (fluidType.isSame(AllFluids.POTION.get()))
			applyPotionEffects(world, fluid);

		if (Tags.Fluids.MILK.contains(fluidType)) {
			if (world.getGameTime() % 5 != 0)
				return;
			List<LivingEntity> list =
				world.getEntitiesOfClass(LivingEntity.class, aoe, LivingEntity::isAffectedByPotions);
			ItemStack curativeItem = new ItemStack(Items.MILK_BUCKET);
			for (LivingEntity livingentity : list)
				livingentity.curePotionEffects(curativeItem);
		}
	}

	private void applyPotionEffects(World world, FluidStack fluid) {
		if (cachedFluid == null || cachedEffects == null || !fluid.isFluidEqual(cachedFluid)) {
			FluidStack copy = fluid.copy();
			copy.setAmount(250);
			ItemStack bottle = PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), fluid);
			cachedEffects = PotionUtils.getMobEffects(bottle);
		}

		if (cachedEffects.isEmpty())
			return;

		List<LivingEntity> list =
			world.getEntitiesOfClass(LivingEntity.class, aoe, LivingEntity::isAffectedByPotions);
		for (LivingEntity livingentity : list) {
			for (EffectInstance effectinstance : cachedEffects) {
				Effect effect = effectinstance.getEffect();
				if (effect.isInstantenous()) {
					effect.applyInstantenousEffect(null, null, livingentity, effectinstance.getAmplifier(), 0.5D);
					continue;
				}
				livingentity.addEffect(new EffectInstance(effectinstance));
			}
		}
	}

	@Override
	public LazyOptional<IFluidHandler> provideHandler() {
		return LazyOptional.of(() -> fluidHandler);
	}

	public CompoundNBT serializeNBT() {
		CompoundNBT compound = new CompoundNBT();
		fluidHandler.writeToNBT(compound);
		compound.putBoolean("Pulling", wasPulling);
		compound.put("Location", location.serializeNBT());
		return compound;
	}

	public static OpenEndedPipe fromNBT(CompoundNBT compound, BlockPos tilePos) {
		BlockFace fromNBT = BlockFace.fromNBT(compound.getCompound("Location"));
		OpenEndedPipe oep = new OpenEndedPipe(new BlockFace(tilePos, fromNBT.getFace()));
		oep.fluidHandler.readFromNBT(compound);
		oep.wasPulling = compound.getBoolean("Pulling");
		return oep;
	}

	private class OpenEndFluidHandler extends FluidTank {

		public OpenEndFluidHandler() {
			super(1000);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			// Never allow being filled when a source is attached
			if (world == null)
				return 0;
			if (!world.isAreaLoaded(outputPos, 0))
				return 0;
			if (resource.isEmpty())
				return 0;
			if (!provideFluidToSpace(resource, true))
				return 0;

			FluidStack containedFluidStack = getFluid();
			if (!containedFluidStack.isEmpty() && !containedFluidStack.isFluidEqual(resource))
				setFluid(FluidStack.EMPTY);
			if (wasPulling)
				wasPulling = false;
			if (canApplyEffects(world, resource))
				resource = FluidHelper.copyStackWithAmount(resource, 1);

			int fill = super.fill(resource, action);
			if (action.simulate())
				return fill;
			if (getFluidAmount() == 1000 || !FluidHelper.hasBlockState(containedFluidStack.getFluid()))
				if (provideFluidToSpace(containedFluidStack, false))
					setFluid(FluidStack.EMPTY);
			return fill;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return drainInner(resource.getAmount(), resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return drainInner(maxDrain, null, action);
		}

		private FluidStack drainInner(int amount, @Nullable FluidStack filter, FluidAction action) {
			FluidStack empty = FluidStack.EMPTY;
			boolean filterPresent = filter != null;

			if (world == null)
				return empty;
			if (!world.isAreaLoaded(outputPos, 0))
				return empty;
			if (amount == 0)
				return empty;
			if (amount > 1000) {
				amount = 1000;
				if (filterPresent)
					filter = FluidHelper.copyStackWithAmount(filter, amount);
			}

			if (!wasPulling)
				wasPulling = true;

			FluidStack drainedFromInternal = filterPresent ? super.drain(filter, action) : super.drain(amount, action);
			if (!drainedFromInternal.isEmpty())
				return drainedFromInternal;

			FluidStack drainedFromWorld = removeFluidFromSpace(action.simulate());
			if (drainedFromWorld.isEmpty())
				return FluidStack.EMPTY;
			if (filterPresent && !drainedFromWorld.isFluidEqual(filter))
				return FluidStack.EMPTY;

			int remainder = drainedFromWorld.getAmount() - amount;
			drainedFromWorld.setAmount(amount);

			if (!action.simulate() && remainder > 0) {
				if (!getFluid().isEmpty() && !getFluid().isFluidEqual(drainedFromWorld))
					setFluid(FluidStack.EMPTY);
				super.fill(FluidHelper.copyStackWithAmount(drainedFromWorld, remainder), FluidAction.EXECUTE);
			}
			return drainedFromWorld;
		}

	}

	@Override
	public boolean isEndpoint() {
		return true;
	}

}
