package com.simibubi.create.content.curiosities.armor;

import java.util.Optional;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import net.minecraft.block.AbstractBlock.Properties;

public class CopperBacktankBlock extends HorizontalKineticBlock
	implements ITE<CopperBacktankTileEntity>, IWaterLoggable {

	public CopperBacktankBlock(Properties properties) {
		super(properties);
		registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
			: Fluids.EMPTY.defaultFluidState();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState pBlockState, World world, BlockPos pos) {
		return getTileEntityOptional(world, pos).map(CopperBacktankTileEntity::getComparatorOutput)
			.orElse(0);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
		IWorld world, BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.getLiquidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState ifluidstate = context.getLevel()
			.getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.UP;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (worldIn.isClientSide)
			return;
		if (stack == null)
			return;
		withTileEntityDo(worldIn, pos, te -> {
			te.setCapacityEnchantLevel(EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), stack));
			te.setAirLevel(stack.getOrCreateTag()
				.getInt("Air"));
			if (stack.isEnchanted())
				te.setEnchantmentTag(stack.getEnchantmentTags());
			if (stack.hasCustomHoverName())
				te.setCustomName(stack.getHoverName());
		});
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand pHandIn,
		BlockRayTraceResult pHit) {
		if (player == null)
			return ActionResultType.PASS;
		if (player instanceof FakePlayer)
			return ActionResultType.PASS;
		if (player.isShiftKeyDown())
			return ActionResultType.PASS;
		if (player.getMainHandItem()
			.getItem() instanceof BlockItem)
			return ActionResultType.PASS;
		if (!player.getItemBySlot(EquipmentSlotType.CHEST)
			.isEmpty())
			return ActionResultType.PASS;
		if (!world.isClientSide) {
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, .75f, 1);
			player.setItemSlot(EquipmentSlotType.CHEST, getCloneItemStack(world, pos, state));
			world.destroyBlock(pos, false);
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public ItemStack getCloneItemStack(IBlockReader pWorldIn, BlockPos pPos, BlockState pState) {
		ItemStack item = AllItems.COPPER_BACKTANK.asStack();
		Optional<CopperBacktankTileEntity> tileEntityOptional = getTileEntityOptional(pWorldIn, pPos);

		int air = tileEntityOptional.map(CopperBacktankTileEntity::getAirLevel)
			.orElse(0);
		CompoundNBT tag = item.getOrCreateTag();
		tag.putInt("Air", air);

		ListNBT enchants = tileEntityOptional.map(CopperBacktankTileEntity::getEnchantmentTag)
			.orElse(new ListNBT());
		if (!enchants.isEmpty()) {
			ListNBT enchantmentTagList = item.getEnchantmentTags();
			enchantmentTagList.addAll(enchants);
			tag.put("Enchantments", enchantmentTagList);
		}

		ITextComponent customName = tileEntityOptional.map(CopperBacktankTileEntity::getCustomName)
			.orElse(null);
		if (customName != null)
			item.setHoverName(customName);
		return item;
	}

	@Override
	public VoxelShape getShape(BlockState pState, IBlockReader pWorldIn, BlockPos pPos,
		ISelectionContext pContext) {
		return AllShapes.BACKTANK;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.COPPER_BACKTANK.create();
	}

	@Override
	public Class<CopperBacktankTileEntity> getTileEntityClass() {
		return CopperBacktankTileEntity.class;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
