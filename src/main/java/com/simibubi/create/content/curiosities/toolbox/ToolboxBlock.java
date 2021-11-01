package com.simibubi.create.content.curiosities.toolbox;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

import javanet.minimport com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.BlockHelper;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

ecraft.world.level.block.state.properties.BlockStatePropertiesocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

public class ToolboxBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, ITE<ToolboxTileEntity> {

	protected final DyeColor color;

	public ToolboxBlock(Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> p_149666_2_) {
		if (group != CreativeModeTab.TAB_SEARCH && color != DyeColor.BROWN)
			return;
		super.fillItemCategory(group, p_149666_2_);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED)
			.add(FACING));
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (worldIn.isClientSide)
			return;
		if (stack == null)
			return;
		withTileEntityDo(worldIn, pos, te -> {
			te.readInventory(stack.getOrCreateTag()
				.getCompound("Inventory"));
			if (stack.hasCustomHoverName())
				te.setCustomName(stack.getHoverName());
		});
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moving) {
		if (state.hasTileEntity() && (!newState.hasTileEntity() || !(newState.getBlock() instanceof ToolboxBlock)))
			world.removeBlockEntity(pos);
	}

	@Override
	public void attack(BlockState state, Level world, BlockPos pos, Player player) {
		if (player instanceof FakePlayer)
			return;
		if (world.isClientSide)
			return;
		withTileEntityDo(world, pos, ToolboxTileEntity::unequipTracked);
		if (world instanceof ServerLevel) {
			ItemStack cloneItemStack = getCloneItemStack(world, pos, state);
			world.destroyBlock(pos, false);
			if (world.getBlockState(pos) != state)
				player.inventory.placeItemBackInInventory(world, cloneItemStack);
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		ItemStack item = new ItemStack(this);
		Optional<ToolboxTileEntity> tileEntityOptional = getTileEntityOptional(world, pos);

		CompoundTag tag = item.getOrCreateTag();
		CompoundTag inv = tileEntityOptional.map(tb -> tb.inventory.serializeNBT())
			.orElse(new CompoundTag());
		tag.put("Inventory", inv);

		Component customName = tileEntityOptional.map(ToolboxTileEntity::getCustomName)
			.orElse(null);
		if (customName != null)
			item.setHoverName(customName);
		return item;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			world.getLiquidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos,
		CollisionContext context) {
		return AllShapes.TOOLBOX.get(state.getValue(FACING));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		if (player == null || player.isCrouching())
			return InteractionResult.PASS;

		ItemStack stack = player.getItemInHand(hand);
		DyeColor color = DyeColor.getColor(stack);
		if (color != null && color != this.color) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.TOOLBOXES.get(color).getDefaultState());
			world.setBlockAndUpdate(pos, newState);
			return InteractionResult.SUCCESS;
		}

		if (player instanceof FakePlayer)
			return InteractionResult.PASS;
		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		withTileEntityDo(world, pos,
			toolbox -> NetworkHooks.openGui((ServerPlayer) player, toolbox, toolbox::sendToContainer));
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.TOOLBOX.create();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState ifluidstate = context.getLevel()
			.getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()
			.getOpposite())
			.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	@Override
	public Class<ToolboxTileEntity> getTileEntityClass() {
		return ToolboxTileEntity.class;
	}

	public DyeColor getColor() {
		return color;
	}

	public static Ingredient getMainBox() {
		return Ingredient.of(AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
			.get());
	}

}
