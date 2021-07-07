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

public class CopperBacktankBlock extends HorizontalKineticBlock
	implements ITE<CopperBacktankTileEntity>, IWaterLoggable {

	public CopperBacktankBlock(Properties properties) {
		super(properties);
		setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
			: Fluids.EMPTY.getDefaultState();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState,
		IWorld world, BlockPos pos, BlockPos neighbourPos) {
		if (state.get(BlockStateProperties.WATERLOGGED)) {
			world.getPendingFluidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState ifluidstate = context.getWorld()
			.getFluidState(context.getPos());
		return super.getStateForPlacement(context).with(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER));
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
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (worldIn.isRemote)
			return;
		if (stack == null)
			return;
		withTileEntityDo(worldIn, pos, te -> {
			te.setCapacityEnchantLevel(EnchantmentHelper.getEnchantmentLevel(AllEnchantments.CAPACITY.get(), stack));
			te.setAirLevel(stack.getOrCreateTag()
				.getInt("Air"));
			if (stack.isEnchanted())
				te.setEnchantmentTag(stack.getEnchantmentTagList());
			if (stack.hasDisplayName())
				te.setCustomName(stack.getDisplayName());
		});
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand p_225533_5_,
		BlockRayTraceResult p_225533_6_) {
		if (player == null)
			return ActionResultType.PASS;
		if (player instanceof FakePlayer)
			return ActionResultType.PASS;
		if (player.isSneaking())
			return ActionResultType.PASS;
		if (player.getHeldItemMainhand()
			.getItem() instanceof BlockItem)
			return ActionResultType.PASS;
		if (!player.getItemStackFromSlot(EquipmentSlotType.CHEST)
			.isEmpty())
			return ActionResultType.PASS;
		if (!world.isRemote) {
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .75f, 1);
			player.setItemStackToSlot(EquipmentSlotType.CHEST, getItem(world, pos, state));
			world.destroyBlock(pos, false);
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public ItemStack getItem(IBlockReader p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_) {
		ItemStack item = AllItems.COPPER_BACKTANK.asStack();
		Optional<CopperBacktankTileEntity> tileEntityOptional = getTileEntityOptional(p_185473_1_, p_185473_2_);

		int air = tileEntityOptional.map(CopperBacktankTileEntity::getAirLevel)
			.orElse(0);
		CompoundNBT tag = item.getOrCreateTag();
		tag.putInt("Air", air);

		ListNBT enchants = tileEntityOptional.map(CopperBacktankTileEntity::getEnchantmentTag)
			.orElse(new ListNBT());
		if (!enchants.isEmpty()) {
			ListNBT enchantmentTagList = item.getEnchantmentTagList();
			enchantmentTagList.addAll(enchants);
			tag.put("Enchantments", enchantmentTagList);
		}

		ITextComponent customName = tileEntityOptional.map(CopperBacktankTileEntity::getCustomName)
			.orElse(null);
		if (customName != null)
			item.setDisplayName(customName);
		return item;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
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
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
