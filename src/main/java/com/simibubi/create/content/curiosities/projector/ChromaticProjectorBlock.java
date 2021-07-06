package com.simibubi.create.content.curiosities.projector;

import javax.annotation.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ChromaticProjectorBlock extends Block implements ITE<ChromaticProjectorTileEntity> {
	public ChromaticProjectorBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
								  BlockRayTraceResult hit) {
		ItemStack held = player.getHeldItemMainhand();
		if (AllItems.WRENCH.isIn(held))
			return ActionResultType.PASS;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> withTileEntityDo(worldIn, pos, te -> this.displayScreen(te, player)));
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(ChromaticProjectorTileEntity te, PlayerEntity player) {
		if (player instanceof ClientPlayerEntity)
			ScreenOpener.open(new ChromaticProjectorScreen(te));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return null;//AllTileEntities.CHROMATIC_PROJECTOR.create();
	}

	@Override
	public Class<ChromaticProjectorTileEntity> getTileEntityClass() {
		return ChromaticProjectorTileEntity.class;
	}
}
