package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
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
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
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

import net.minecraft.block.AbstractBlock.Properties;

public class FluidTankBlock extends Block implements IWrenchable, ITE<FluidTankTileEntity> {

	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
	public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

	private boolean creative;

	public static FluidTankBlock regular(Properties p_i48440_1_) {
		return new FluidTankBlock(p_i48440_1_, false);
	}

	public static FluidTankBlock creative(Properties p_i48440_1_) {
		return new FluidTankBlock(p_i48440_1_, true);
	}

	protected FluidTankBlock(Properties p_i48440_1_, boolean creative) {
		super(p_i48440_1_);
		this.creative = creative;
		registerDefaultState(defaultBlockState().setValue(TOP, true)
			.setValue(BOTTOM, true)
			.setValue(SHAPE, Shape.WINDOW));
	}

	public static boolean isTank(BlockState state) {
		return state.getBlock() instanceof FluidTankBlock;
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
		if (oldState.getBlock() == state.getBlock())
			return;
		if (moved)
			return;
		withTileEntityDo(world, pos, FluidTankTileEntity::updateConnectivity);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(TOP, BOTTOM, SHAPE);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, pos);
		if (tankAt == null)
			return 0;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null || !controllerTE.window)
			return 0;
		return tankAt.luminosity;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		withTileEntityDo(context.getLevel(), context.getClickedPos(), FluidTankTileEntity::toggleWindows);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);
		boolean onClient = world.isClientSide;

		if (heldItem.isEmpty())
			return ActionResultType.PASS;
		if (!player.isCreative() && !creative)
			return ActionResultType.PASS;

		FluidExchange exchange = null;
		FluidTankTileEntity te = FluidTankConnectivityHandler.anyTankAt(world, pos);
		if (te == null)
			return ActionResultType.FAIL;

		LazyOptional<IFluidHandler> tankCapability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		if (!tankCapability.isPresent())
			return ActionResultType.PASS;
		IFluidHandler fluidTank = tankCapability.orElse(null);
		FluidStack prevFluidInTank = fluidTank.getFluidInTank(0)
			.copy();

		if (FluidHelper.tryEmptyItemIntoTE(world, player, hand, heldItem, te))
			exchange = FluidExchange.ITEM_TO_TANK;
		else if (FluidHelper.tryFillItemFromTE(world, player, hand, heldItem, te))
			exchange = FluidExchange.TANK_TO_ITEM;

		if (exchange == null) {
			if (EmptyingByBasin.canItemBeEmptied(world, heldItem)
				|| GenericItemFilling.canItemBeFilled(world, heldItem))
				return ActionResultType.SUCCESS;
			return ActionResultType.PASS;
		}

		SoundEvent soundevent = null;
		BlockState fluidState = null;
		FluidStack fluidInTank = tankCapability.map(fh -> fh.getFluidInTank(0))
			.orElse(FluidStack.EMPTY);

		if (exchange == FluidExchange.ITEM_TO_TANK) {
			if (creative && !onClient) {
				FluidStack fluidInItem = EmptyingByBasin.emptyItem(world, heldItem, true)
					.getFirst();
				if (!fluidInItem.isEmpty() && fluidTank instanceof CreativeSmartFluidTank)
					((CreativeSmartFluidTank) fluidTank).setContainedFluid(fluidInItem);
			}

			Fluid fluid = fluidInTank.getFluid();
			fluidState = fluid.defaultFluidState()
				.createLegacyBlock();
			FluidAttributes attributes = fluid.getAttributes();
			soundevent = attributes.getEmptySound();
			if (soundevent == null)
				soundevent =
					fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		}
		if (exchange == FluidExchange.TANK_TO_ITEM) {
			if (creative && !onClient)
				if (fluidTank instanceof CreativeSmartFluidTank)
					((CreativeSmartFluidTank) fluidTank).setContainedFluid(FluidStack.EMPTY);

			Fluid fluid = prevFluidInTank.getFluid();
			fluidState = fluid.defaultFluidState()
				.createLegacyBlock();
			soundevent = fluid.getAttributes()
				.getFillSound();
			if (soundevent == null)
				soundevent =
					fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
		}

		if (soundevent != null && !onClient) {
			float pitch = MathHelper
				.clamp(1 - (1f * fluidInTank.getAmount() / (FluidTankTileEntity.getCapacityMultiplier() * 16)), 0, 1);
			pitch /= 1.5f;
			pitch += .5f;
			pitch += (world.random.nextFloat() - .5f) / 4f;
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

						Vector3d vec = ray.getLocation();
						vec = new Vector3d(vec.x, controllerTE.getBlockPos()
							.getY() + level * (controllerTE.height - .5f) + .25f, vec.z);
						Vector3d motion = player.position()
							.subtract(vec)
							.scale(1 / 20f);
						vec = vec.add(motion);
						world.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
						return ActionResultType.SUCCESS;
					}

					controllerTE.sendDataImmediately();
					controllerTE.setChanged();
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
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && (state.getBlock() != newState.getBlock() || !newState.hasTileEntity())) {
			TileEntity te = world.getBlockEntity(pos);
			if (!(te instanceof FluidTankTileEntity))
				return;
			FluidTankTileEntity tankTE = (FluidTankTileEntity) te;
			world.removeBlockEntity(pos);
			FluidTankConnectivityHandler.splitTank(tankTE);
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return creative ? AllTileEntities.CREATIVE_FLUID_TANK.create() : AllTileEntities.FLUID_TANK.create();
	}

	@Override
	public Class<FluidTankTileEntity> getTileEntityClass() {
		return FluidTankTileEntity.class;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE)
			return state;
		boolean x = mirror == Mirror.FRONT_BACK;
		switch (state.getValue(SHAPE)) {
		case WINDOW_NE:
			return state.setValue(SHAPE, x ? Shape.WINDOW_NW : Shape.WINDOW_SE);
		case WINDOW_NW:
			return state.setValue(SHAPE, x ? Shape.WINDOW_NE : Shape.WINDOW_SW);
		case WINDOW_SE:
			return state.setValue(SHAPE, x ? Shape.WINDOW_SW : Shape.WINDOW_NE);
		case WINDOW_SW:
			return state.setValue(SHAPE, x ? Shape.WINDOW_SE : Shape.WINDOW_NW);
		default:
			return state;
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		for (int i = 0; i < rotation.ordinal(); i++)
			state = rotateOnce(state);
		return state;
	}

	private BlockState rotateOnce(BlockState state) {
		switch (state.getValue(SHAPE)) {
		case WINDOW_NE:
			return state.setValue(SHAPE, Shape.WINDOW_SE);
		case WINDOW_NW:
			return state.setValue(SHAPE, Shape.WINDOW_NE);
		case WINDOW_SE:
			return state.setValue(SHAPE, Shape.WINDOW_SW);
		case WINDOW_SW:
			return state.setValue(SHAPE, Shape.WINDOW_NW);
		default:
			return state;
		}
	}

	public enum Shape implements IStringSerializable {
		PLAIN, WINDOW, WINDOW_NW, WINDOW_SW, WINDOW_NE, WINDOW_SE;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	// Tanks are less noisy when placed in batch
	public static final SoundType SILENCED_METAL =
		new SoundType(0.1F, 1.5F, SoundEvents.METAL_BREAK, SoundEvents.METAL_STEP,
			SoundEvents.METAL_PLACE, SoundEvents.METAL_HIT, SoundEvents.METAL_FALL);

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		SoundType soundType = super.getSoundType(state, world, pos, entity);
		if (entity != null && entity.getPersistentData()
			.contains("SilenceTankSound"))
			return SILENCED_METAL;
		return soundType;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		return getTileEntityOptional(worldIn, pos).map(FluidTankTileEntity::getControllerTE)
			.map(te -> ComparatorUtil.fractionToRedstoneLevel(te.getFillState()))
			.orElse(0);
	}

}
