package com.simibubi.create.content.contraptions.fluids;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OpenEndedPipe {

	World world;
	BlockPos pos;
	AxisAlignedBB aoe;

	private OpenEndFluidHandler fluidHandler;
	private BlockPos outputPos;
	private boolean wasPulling;
	private boolean stale;

	private FluidStack cachedFluid;
	private List<EffectInstance> cachedEffects;

	public OpenEndedPipe(BlockFace face) {
		fluidHandler = new OpenEndFluidHandler();
		outputPos = face.getConnectedPos();
		pos = face.getPos();
		aoe = new AxisAlignedBB(outputPos).expand(0, -1, 0);
		if (face.getFace() == Direction.DOWN)
			aoe = aoe.expand(0, -1, 0);
	}

	public void tick(World world, boolean pulling) {
		this.world = world;
		if (!world.isAreaLoaded(outputPos, 0))
			return;
		if (pulling != wasPulling) {
			if (pulling)
				fluidHandler.clear();
			wasPulling = pulling;
		}

		BlockState state = world.getBlockState(outputPos);
		IFluidState fluidState = state.getFluidState();
		boolean waterlog = state.has(BlockStateProperties.WATERLOGGED);

		if (!waterlog && !state.getMaterial()
			.isReplaceable())
			return;

		if (pulling) {
			if (fluidState.isEmpty() || !fluidState.isSource())
				return;
			if (!fluidHandler.tryCollectFluid(fluidState.getFluid()))
				return;
			if (waterlog) {
				world.setBlockState(outputPos, state.with(BlockStateProperties.WATERLOGGED, false), 3);
				world.getPendingFluidTicks()
					.scheduleTick(outputPos, Fluids.WATER, 1);
				return;
			}
			world.setBlockState(outputPos, fluidState.getBlockState()
				.with(FlowingFluidBlock.LEVEL, 14), 3);
			return;
		}

		FluidStack fluid = fluidHandler.getFluid();
		if (fluid.isEmpty())
			return;
		if (!FluidHelper.hasBlockState(fluid.getFluid())) {
			fluidHandler.drain(fluid.getAmount() > 1 ? fluid.getAmount() - 1 : 1, FluidAction.EXECUTE);
			if (fluidHandler.isEmpty())
				updatePumpIfNecessary();
			if (!fluid.getFluid()
				.isEquivalentTo(AllFluids.POTION.get()))
				return;
			applyPotionEffects(world, fluid);
			return;
		}

		Fluid providedFluid = fluidHandler.tryProvidingFluid();
		if (providedFluid == null)
			return;
		if (!fluidState.isEmpty() && fluidState.getFluid() != providedFluid) {
			FluidReactions.handlePipeSpillCollision(world, outputPos, providedFluid, fluidState);
			return;
		}
		if (fluidState.isSource())
			return;

		if (world.dimension.doesWaterVaporize() && providedFluid.getFluid()
			.isIn(FluidTags.WATER)) {
			int i = outputPos.getX();
			int j = outputPos.getY();
			int k = outputPos.getZ();
			world.playSound(null, i, j, k, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
				2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			return;
		}

		if (waterlog) {
			if (providedFluid.getFluid() != Fluids.WATER)
				return;
			world.setBlockState(outputPos, state.with(BlockStateProperties.WATERLOGGED, true), 3);
			world.getPendingFluidTicks()
				.scheduleTick(outputPos, Fluids.WATER, 1);
			return;
		}
		world.setBlockState(outputPos, providedFluid.getDefaultState()
			.getBlockState(), 3);
	}

	private void applyPotionEffects(World world, FluidStack fluid) {
		if (cachedFluid == null || cachedEffects == null || !fluid.isFluidEqual(cachedFluid)) {
			FluidStack copy = fluid.copy();
			copy.setAmount(250);
			ItemStack bottle = PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), fluid);
			cachedEffects = PotionUtils.getEffectsFromStack(bottle);
		}

		if (cachedEffects.isEmpty())
			return;

		List<LivingEntity> list =
			this.world.getEntitiesWithinAABB(LivingEntity.class, aoe, LivingEntity::canBeHitWithPotion);
		for (LivingEntity livingentity : list) {
			for (EffectInstance effectinstance : cachedEffects) {
				Effect effect = effectinstance.getPotion();
				if (effect.isInstant()) {
					effect.affectEntity(null, null, livingentity, effectinstance.getAmplifier(), 0.5D);
					continue;
				}
				livingentity.addPotionEffect(new EffectInstance(effectinstance));
			}
		}

	}

	public LazyOptional<IFluidHandler> getCapability() {
		return LazyOptional.of(() -> fluidHandler);
	}

	public CompoundNBT writeToNBT(CompoundNBT compound) {
		fluidHandler.writeToNBT(compound);
		compound.putBoolean("Pulling", wasPulling);
		return compound;
	}

	public void readNBT(CompoundNBT compound) {
		fluidHandler.readFromNBT(compound);
		wasPulling = compound.getBoolean("Pulling");
	}

	public void markStale() {
		stale = true;
	}

	public void unmarkStale() {
		stale = false;
	}

	public boolean isStale() {
		return stale;
	}

	private void updatePumpIfNecessary() {
		if (world == null)
			return;
		if (!PumpBlock.isPump(world.getBlockState(pos)))
			return;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof PumpTileEntity)
			((PumpTileEntity) tileEntity).sendData();
	}

	private class OpenEndFluidHandler extends FluidTank {

		public OpenEndFluidHandler() {
			super(1500);
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

			FluidStack prevFluid = getFluid();
			BlockState state = world.getBlockState(outputPos);
			IFluidState fluidState = state.getFluidState();
			if (!fluidState.isEmpty() && fluidState.getFluid() != resource.getFluid()) {
				FluidReactions.handlePipeSpillCollision(world, outputPos, resource.getFluid(), fluidState);
				return 0;
			}
			if (fluidState.isSource())
				return 0;
			if (!(state.has(BlockStateProperties.WATERLOGGED) && resource.getFluid() == Fluids.WATER)
				&& !state.getMaterial()
					.isReplaceable())
				return 0;

			// Never allow being filled above 1000
			FluidStack insertable = resource.copy();
			insertable.setAmount(Math.min(insertable.getAmount(), Math.max(1000 - getFluidAmount(), 0)));
			int fill = super.fill(insertable, action);

			if (!getFluid().isFluidEqual(prevFluid))
				updatePumpIfNecessary();

			return fill;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			boolean wasEmpty = isEmpty();
			FluidStack drain = super.drain(resource, action);
			if (action.execute() && !wasEmpty && isEmpty())
				updatePumpIfNecessary();
			return drain;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			boolean wasEmpty = isEmpty();
			FluidStack drain = super.drain(maxDrain, action);
			if (action.execute() && !wasEmpty && isEmpty())
				updatePumpIfNecessary();
			return drain;
		}

		public boolean tryCollectFluid(Fluid fluid) {
			for (boolean simulate : Iterate.trueAndFalse)
				if (super.fill(new FluidStack(fluid, 1000),
					simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE) != 1000)
					return false;
			updatePumpIfNecessary();
			return true;
		}

		@Nullable
		public Fluid tryProvidingFluid() {
			Fluid fluid = getFluid().getFluid();
			for (boolean simulate : Iterate.trueAndFalse)
				if (drain(1000, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount() != 1000)
					return null;
			updatePumpIfNecessary();
			return fluid;
		}

		public void clear() {
			boolean wasEmpty = isEmpty();
			setFluid(FluidStack.EMPTY);
			if (!wasEmpty)
				updatePumpIfNecessary();
		}

	}

}
