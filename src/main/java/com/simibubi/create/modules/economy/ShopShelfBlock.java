package com.simibubi.create.modules.economy;

import com.simibubi.create.foundation.block.IWithoutBlockItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ShopShelfBlock extends HorizontalBlock implements IWithoutBlockItem {

	public static final VoxelShape TOP_SHAPE = makeCuboidShape(0, 14, 0, 16, 16, 16);//todo review

	public static final VoxelShape BODY_SOUTH_SHAPE = makeCuboidShape(0, 0, 0, 16, 14, 14);
	public static final VoxelShape BODY_NORTH_SHAPE = makeCuboidShape(0, 0, 2, 16, 14, 16);
	public static final VoxelShape BODY_EAST_SHAPE = makeCuboidShape(0, 0, 0, 14, 14, 16);
	public static final VoxelShape BODY_WEST_SHAPE = makeCuboidShape(2, 0, 0, 16, 14, 16);

	public ShopShelfBlock() {
		super(Properties.from(Blocks.SPRUCE_PLANKS));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ShopShelfTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		ShopShelfTileEntity te = (ShopShelfTileEntity) worldIn.getTileEntity(pos);
		te.setOwner(placer.getUniqueID());
		worldIn.notifyBlockUpdate(pos, state, state, 2);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		ShopShelfTileEntity te = (ShopShelfTileEntity) worldIn.getTileEntity(pos);
		if (te == null)
			return false;
		if (!worldIn.isRemote)
			NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer);
		return true;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape body = VoxelShapes.empty();
		switch (state.get(HORIZONTAL_FACING)) {
		case EAST:
			body = BODY_EAST_SHAPE;
			break;
		case NORTH:
			body = BODY_NORTH_SHAPE;
			break;
		case SOUTH:
			body = BODY_SOUTH_SHAPE;
			break;
		case WEST:
			body = BODY_WEST_SHAPE;
			break;
		default:
			break;
		}

		return VoxelShapes.or(TOP_SHAPE, body);
	}

}
