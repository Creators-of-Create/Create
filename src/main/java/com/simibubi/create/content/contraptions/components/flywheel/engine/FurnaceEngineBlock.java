package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnaceEngineBlock extends EngineBlock implements ITE<FurnaceEngineTileEntity> {

	public FurnaceEngineBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isValidBaseBlock(BlockState baseBlock, BlockGetter world, BlockPos pos) {
		return baseBlock.getBlock() instanceof AbstractFurnaceBlock;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.FURNACE_ENGINE.get(state.getValue(FACING));
	}

	@Override
	public PartialModel getFrameModel() {
		return AllBlockPartials.FURNACE_GENERATOR_FRAME;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (worldIn instanceof WrappedWorld)
			return;
		if (worldIn.isClientSide)
			return;

		if (fromPos.equals(getBaseBlockPos(state, pos)))
			if (canSurvive(state, worldIn, pos))
				withTileEntityDo(worldIn, pos, FurnaceEngineTileEntity::updateFurnace);
	}

	public static InteractionResult usingFurnaceEngineOnFurnacePreventsGUI(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack item = player.getItemInHand(hand);
		if (!(item.getItem() instanceof BlockItem))
			return InteractionResult.PASS;
		BlockItem blockItem = (BlockItem) item.getItem();
		if (blockItem.getBlock() != AllBlocks.FURNACE_ENGINE.get())
			return InteractionResult.PASS;
		BlockState state = world.getBlockState(hitResult.getBlockPos());
		if (hitResult.getDirection().getAxis().isVertical())
			return InteractionResult.PASS;
		if (state.getBlock() instanceof AbstractFurnaceBlock)
			return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}

	@Override
	public Class<FurnaceEngineTileEntity> getTileEntityClass() {
		return FurnaceEngineTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends FurnaceEngineTileEntity> getTileEntityType() {
		return AllTileEntities.FURNACE_ENGINE.get();
	}


}
