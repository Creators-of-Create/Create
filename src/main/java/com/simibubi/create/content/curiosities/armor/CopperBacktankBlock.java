package com.simibubi.create.content.curiosities.armor;

import java.util.Optional;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;

public class CopperBacktankBlock extends HorizontalKineticBlock
	implements ITE<CopperBacktankTileEntity>, SimpleWaterloggedBlock {

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
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState p_180641_1_, Level world, BlockPos pos) {
		return getTileEntityOptional(world, pos).map(CopperBacktankTileEntity::getComparatorOutput)
			.orElse(0);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
		LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) 
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState ifluidstate = context.getLevel()
			.getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.UP;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand p_225533_5_,
		BlockHitResult p_225533_6_) {
		if (player == null)
			return InteractionResult.PASS;
		if (player instanceof FakePlayer)
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (player.getMainHandItem()
			.getItem() instanceof BlockItem)
			return InteractionResult.PASS;
		if (!player.getItemBySlot(EquipmentSlot.CHEST)
			.isEmpty())
			return InteractionResult.PASS;
		if (!world.isClientSide) {
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .75f, 1);
			player.setItemSlot(EquipmentSlot.CHEST, getCloneItemStack(world, pos, state));
			world.destroyBlock(pos, false);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_) {
		ItemStack item = AllItems.COPPER_BACKTANK.asStack();
		Optional<CopperBacktankTileEntity> tileEntityOptional = getTileEntityOptional(p_185473_1_, p_185473_2_);

		int air = tileEntityOptional.map(CopperBacktankTileEntity::getAirLevel)
			.orElse(0);
		CompoundTag tag = item.getOrCreateTag();
		tag.putInt("Air", air);

		ListTag enchants = tileEntityOptional.map(CopperBacktankTileEntity::getEnchantmentTag)
			.orElse(new ListTag());
		if (!enchants.isEmpty()) {
			ListTag enchantmentTagList = item.getEnchantmentTags();
			enchantmentTagList.addAll(enchants);
			tag.put("Enchantments", enchantmentTagList);
		}

		Component customName = tileEntityOptional.map(CopperBacktankTileEntity::getCustomName)
			.orElse(null);
		if (customName != null)
			item.setHoverName(customName);
		return item;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.BACKTANK;
	}

	@Override
	public Class<CopperBacktankTileEntity> getTileEntityClass() {
		return CopperBacktankTileEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends CopperBacktankTileEntity> getTileEntityType() {
		return AllTileEntities.COPPER_BACKTANK.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
