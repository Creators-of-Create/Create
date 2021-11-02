package com.simibubi.create.content.curiosities.toolbox;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
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

public class ToolboxBlock extends HorizontalBlock implements IWaterLoggable, ITE<ToolboxTileEntity> {

	protected final DyeColor color;

	public ToolboxBlock(Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.TAB_SEARCH && color != DyeColor.BROWN)
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
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean moving) {
		if (state.hasTileEntity() && (!newState.hasTileEntity() || !(newState.getBlock() instanceof ToolboxBlock)))
			world.removeBlockEntity(pos);
	}

	@Override
	public void attack(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		if (player instanceof FakePlayer)
			return;
		if (world.isClientSide)
			return;
		withTileEntityDo(world, pos, ToolboxTileEntity::unequipTracked);
		if (world instanceof ServerWorld) {
			ItemStack cloneItemStack = getCloneItemStack(world, pos, state);
			world.destroyBlock(pos, false);
			if (world.getBlockState(pos) != state)
				player.inventory.placeItemBackInInventory(world, cloneItemStack);
		}
	}

	@Override
	public ItemStack getCloneItemStack(IBlockReader world, BlockPos pos, BlockState state) {
		ItemStack item = new ItemStack(this);
		Optional<ToolboxTileEntity> tileEntityOptional = getTileEntityOptional(world, pos);

		CompoundNBT tag = item.getOrCreateTag();
		CompoundNBT inv = tileEntityOptional.map(tb -> tb.inventory.serializeNBT())
			.orElse(new CompoundNBT());
		tag.put("Inventory", inv);

		ITextComponent customName = tileEntityOptional.map(ToolboxTileEntity::getCustomName)
			.orElse(null);
		if (customName != null)
			item.setHoverName(customName);
		return item;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, IWorld world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			world.getLiquidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos,
		ISelectionContext context) {
		return AllShapes.TOOLBOX.get(state.getValue(FACING));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {

		if (player == null || player.isCrouching())
			return ActionResultType.PASS;

		ItemStack stack = player.getItemInHand(hand);
		DyeColor color = DyeColor.getColor(stack);
		if (color != null && color != this.color) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.TOOLBOXES.get(color).getDefaultState());
			world.setBlockAndUpdate(pos, newState);
			return ActionResultType.SUCCESS;
		}

		if (player instanceof FakePlayer)
			return ActionResultType.PASS;
		if (world.isClientSide)
			return ActionResultType.SUCCESS;

		withTileEntityDo(world, pos,
			toolbox -> NetworkHooks.openGui((ServerPlayerEntity) player, toolbox, toolbox::sendToContainer));
		return ActionResultType.SUCCESS;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.TOOLBOX.create();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
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
