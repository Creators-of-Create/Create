package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidTankBlock extends Block implements IWrenchable, ITE<FluidTankTileEntity> {

	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
	public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

	public FluidTankBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
		setDefaultState(getDefaultState().with(TOP, true)
			.with(BOTTOM, true)
			.with(SHAPE, Shape.WINDOW));
	}

	public static boolean isTank(BlockState state) {
		return state.getBlock() instanceof FluidTankBlock;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean p_220082_5_) {
		if (oldState.getBlock() == state.getBlock())
			return;
		withTileEntityDo(world, pos, FluidTankTileEntity::updateConnectivity);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(TOP, BOTTOM, SHAPE);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.tankAt(world, pos);
		if (tankAt == null)
			return 0;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null || !controllerTE.window)
			return 0;
		return tankAt.luminosity;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		withTileEntityDo(context.getWorld(), context.getPos(), FluidTankTileEntity::toggleWindows);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		ItemStack heldItem = player.getHeldItem(hand);

		ItemStack copy = heldItem.copy();
		copy.setCount(1);
		LazyOptional<IFluidHandlerItem> capability =
			copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		if (!capability.isPresent())
			return ActionResultType.PASS;

		TileEntity te = world.getTileEntity(pos);
		LazyOptional<IFluidHandler> tankCapability =
			te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ray.getFace());
		if (!tankCapability.isPresent())
			return ActionResultType.PASS;
		boolean onClient = world.isRemote;

		IFluidHandlerItem fluidItem = capability.orElse(null);
		IFluidHandler fluidTank = tankCapability.orElse(null);
		FluidStack prevFluidInTank = fluidTank.getFluidInTank(0)
			.copy();

		FluidExchange exchange = FluidHelper.exchange(fluidTank, fluidItem, FluidExchange.TANK_TO_ITEM, 1000);

		FluidStack fluidInTank = fluidTank.getFluidInTank(0);
		if (!player.isCreative() && !onClient) {
			if (heldItem.getCount() > 1) {
				heldItem.shrink(1);
				player.addItemStackToInventory(fluidItem.getContainer());
			} else {
				player.setHeldItem(hand, fluidItem.getContainer());
			}
		}

		SoundEvent soundevent = null;
		BlockState fluidState = null;

		if (exchange == FluidExchange.ITEM_TO_TANK) {
			Fluid fluid = fluidInTank.getFluid();
			fluidState = fluid.getDefaultState()
				.getBlockState();
			FluidAttributes attributes = fluid.getAttributes();
			soundevent = attributes.getEmptySound();
			if (soundevent == null)
				soundevent =
					fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
		}
		if (exchange == FluidExchange.TANK_TO_ITEM) {
			Fluid fluid = prevFluidInTank.getFluid();
			fluidState = fluid.getDefaultState()
				.getBlockState();
			soundevent = fluid.getAttributes()
				.getFillSound();
			if (soundevent == null)
				soundevent =
					fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
		}

		if (soundevent != null && !onClient) {
			float pitch = MathHelper
				.clamp(1 - (1f * fluidInTank.getAmount() / (FluidTankTileEntity.getCapacityMultiplier() * 16)), 0, 1);
			pitch /= 1.5f;
			pitch += .5f;
			pitch += (world.rand.nextFloat() - .5f) / 4f;
			world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, .5f, pitch);
		}

		if (!fluidInTank.isFluidStackIdentical(prevFluidInTank)) {
			if (te instanceof FluidTankTileEntity) {
				FluidTankTileEntity controllerTE = ((FluidTankTileEntity) te).getControllerTE();
				if (controllerTE != null) {
					if (fluidState != null && onClient) {
						BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, fluidState);
						float level = (float) fluidInTank.getAmount() / fluidTank.getTankCapacity(0);

						boolean reversed = fluidInTank.getFluid()
							.getAttributes()
							.isLighterThanAir();
						if (reversed)
							level = 1 - level;

						Vector3d vec = ray.getHitVec();
						vec = new Vector3d(vec.x, controllerTE.getPos()
							.getY() + level * (controllerTE.height - .5f) + .25f, vec.z);
						Vector3d motion = player.getPositionVec()
							.subtract(vec)
							.scale(1 / 20f);
						vec = vec.add(motion);
						world.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
						return ActionResultType.SUCCESS;
					}

					controllerTE.sendData();
					controllerTE.markDirty();
				}
			}
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && (state.getBlock() != newState.getBlock() || !newState.hasTileEntity())) {
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof FluidTankTileEntity))
				return;
			FluidTankTileEntity tankTE = (FluidTankTileEntity) te;
			world.removeTileEntity(pos);
			FluidTankConnectivityHandler.splitTank(tankTE);
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FLUID_TANK.create();
	}

	@Override
	public Class<FluidTankTileEntity> getTileEntityClass() {
		return FluidTankTileEntity.class;
	}

	public enum Shape implements IStringSerializable {
		PLAIN, WINDOW, WINDOW_NW, WINDOW_SW, WINDOW_NE, WINDOW_SE;

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	// Tanks are less noisy when placed in batch
	public static final SoundType SILENCED_METAL =
		new SoundType(0.1F, 1.5F, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_METAL_STEP,
			SoundEvents.BLOCK_METAL_PLACE, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_METAL_FALL);

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		SoundType soundType = super.getSoundType(state, world, pos, entity);
		if (entity != null && entity.getPersistentData()
			.contains("SilenceTankSound"))
			return SILENCED_METAL;
		return soundType;
	}
}
