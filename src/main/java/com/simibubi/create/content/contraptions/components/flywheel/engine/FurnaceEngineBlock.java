package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnaceEngineBlock extends EngineBlock implements ITE<FurnaceEngineTileEntity> {

	public FurnaceEngineBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isValidBaseBlock(BlockState baseBlock, BlockGetter world, BlockPos pos) {
		return FurnaceEngineInteractions.getHandler(baseBlock).getHeatSource(baseBlock).isValid();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.FURNACE_ENGINE.get(state.getValue(FACING));
	}

	@Override
	@Environment(EnvType.CLIENT)
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

//	public static void usingFurnaceEngineOnFurnacePreventsGUI(RightClickBlock event) {
//		ItemStack item = event.getItemStack();
//		if (!(item.getItem() instanceof BlockItem))
//			return;
//		BlockItem blockItem = (BlockItem) item.getItem();
//		if (blockItem.getBlock() != AllBlocks.FURNACE_ENGINE.get())
//			return;
//		BlockState state = event.getWorld().getBlockState(event.getPos());
//		if (event.getFace().getAxis().isVertical())
//			return;
//		if (state.getBlock() instanceof AbstractFurnaceBlock)
//			event.setUseBlock(Result.DENY);
//	}

	@Override
	public Class<FurnaceEngineTileEntity> getTileEntityClass() {
		return FurnaceEngineTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends FurnaceEngineTileEntity> getTileEntityType() {
		return AllTileEntities.FURNACE_ENGINE.get();
	}


}
