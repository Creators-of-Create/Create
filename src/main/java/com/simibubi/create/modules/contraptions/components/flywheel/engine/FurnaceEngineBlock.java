package com.simibubi.create.modules.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.WrappedWorld;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FurnaceEngineBlock extends EngineBlock implements ITE<FurnaceEngineTileEntity> {

	public FurnaceEngineBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isValidBaseBlock(BlockState baseBlock, IBlockReader world, BlockPos pos) {
		return baseBlock.getBlock() instanceof AbstractFurnaceBlock;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.FURNACE_ENGINE.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public AllBlockPartials getFrameModel() {
		return AllBlockPartials.FURNACE_GENERATOR_FRAME;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new FurnaceEngineTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (worldIn instanceof WrappedWorld)
			return;
		if (worldIn.isRemote)
			return;

		if (fromPos.equals(getBaseBlockPos(state, pos)))
			if (isValidPosition(state, worldIn, pos))
				withTileEntityDo(worldIn, pos, FurnaceEngineTileEntity::updateFurnace);
	}

	@SubscribeEvent
	public static void usingFurnaceEngineOnFurnacePreventsGUI(RightClickBlock event) {
		ItemStack item = event.getItemStack();
		if (!(item.getItem() instanceof BlockItem))
			return;
		BlockItem blockItem = (BlockItem) item.getItem();
		if (blockItem.getBlock() != AllBlocksNew.FURNACE_ENGINE.get())
			return;
		BlockState state = event.getWorld().getBlockState(event.getPos());
		if (event.getFace().getAxis().isVertical())
			return;
		if (state.getBlock() instanceof AbstractFurnaceBlock)
			event.setUseBlock(Result.DENY);
	}

	@Override
	public Class<FurnaceEngineTileEntity> getTileEntityClass() {
		return FurnaceEngineTileEntity.class;
	}

}
