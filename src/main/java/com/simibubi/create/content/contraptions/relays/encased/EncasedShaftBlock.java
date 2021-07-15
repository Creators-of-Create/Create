package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.IBlockReader;

import net.minecraft.block.AbstractBlock.Properties;

public class EncasedShaftBlock extends AbstractEncasedShaftBlock implements ISpecialBlockItemRequirement {

	private BlockEntry<CasingBlock> casing;

	public static EncasedShaftBlock andesite(Properties properties) {
		return new EncasedShaftBlock(properties, AllBlocks.ANDESITE_CASING);
	}

	public static EncasedShaftBlock brass(Properties properties) {
		return new EncasedShaftBlock(properties, AllBlocks.BRASS_CASING);
	}

	protected EncasedShaftBlock(Properties properties, BlockEntry<CasingBlock> casing) {
		super(properties);
		this.casing = casing;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	public BlockEntry<CasingBlock> getCasing() {
		return casing;
	}

	@Override
	public ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		if (context.getLevel().isClientSide)
			return ActionResultType.SUCCESS;
		context.getLevel().levelEvent(2001, context.getClickedPos(), Block.getId(state));
		KineticTileEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), AllBlocks.SHAFT.getDefaultState().setValue(AXIS, state.getValue(AXIS)));
		return ActionResultType.SUCCESS;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
		return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), te);
	}

}
