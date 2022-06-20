package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WindmillBearingBlock extends BearingBlock implements ITE<WindmillBearingTileEntity> {

	public WindmillBearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return InteractionResult.FAIL;
		if (player.getItemInHand(handIn)
			.isEmpty()) {
			if (worldIn.isClientSide)
				return InteractionResult.SUCCESS;
			withTileEntityDo(worldIn, pos, te -> {
				if (te.running) {
					te.disassemble();
					return;
				}
				te.assembleNextTick = true;
			});
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public Class<WindmillBearingTileEntity> getTileEntityClass() {
		return WindmillBearingTileEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends WindmillBearingTileEntity> getTileEntityType() {
		return AllTileEntities.WINDMILL_BEARING.get();
	}
	
	public static Couple<Integer> getSpeedRange() {
		return Couple.create(1, 16);
	}

}
