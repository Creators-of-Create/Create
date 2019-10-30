package com.simibubi.create.modules.curiosities.partialWindows;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.DistExecutor;

public class WindowInABlockTileEntity extends SyncedTileEntity {

	private BlockState partialBlock = Blocks.AIR.getDefaultState();
	private BlockState windowBlock = Blocks.AIR.getDefaultState();
	
	@OnlyIn(value = Dist.CLIENT)
	private IModelData modelData;

	public static final ModelProperty<BlockState> PARTIAL_BLOCK = new ModelProperty<>();
	public static final ModelProperty<BlockState> WINDOW_BLOCK = new ModelProperty<>();
	public static final ModelProperty<BlockPos> POSITION = new ModelProperty<>();

	public WindowInABlockTileEntity() {
		super(AllTileEntities.WINDOW_IN_A_BLOCK.type);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this::initDataMap);
	}

	@OnlyIn(value = Dist.CLIENT)
	private void initDataMap() {
		modelData = new ModelDataMap.Builder().withInitial(WINDOW_BLOCK, Blocks.AIR.getDefaultState())
				.withInitial(PARTIAL_BLOCK, Blocks.AIR.getDefaultState()).withInitial(POSITION, BlockPos.ZERO).build();
	}

	@Override
	public void read(CompoundNBT compound) {
		partialBlock = NBTUtil.readBlockState(compound.getCompound("PartialBlock"));
		windowBlock = NBTUtil.readBlockState(compound.getCompound("WindowBlock"));
		super.read(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("PartialBlock", NBTUtil.writeBlockState(getPartialBlock()));
		compound.put("WindowBlock", NBTUtil.writeBlockState(getWindowBlock()));
		return super.write(compound);
	}

	public void updateWindowConnections() {
		for (Direction side : Direction.values()) {
			BlockPos offsetPos = pos.offset(side);
			windowBlock = getWindowBlock().updatePostPlacement(side, world.getBlockState(offsetPos), world, pos,
					offsetPos);
		}
		sendData();
		markDirty();
	}

	@OnlyIn(value = Dist.CLIENT)
	@Override
	public IModelData getModelData() {
		modelData.setData(PARTIAL_BLOCK, partialBlock);
		modelData.setData(WINDOW_BLOCK, windowBlock);
		modelData.setData(POSITION, pos);
		return modelData;
	}

	public BlockState getPartialBlock() {
		return partialBlock;
	}

	public void setPartialBlock(BlockState partialBlock) {
		this.partialBlock = partialBlock;
	}

	public BlockState getWindowBlock() {
		return windowBlock;
	}

	public void setWindowBlock(BlockState windowBlock) {
		this.windowBlock = windowBlock;
	}
}
