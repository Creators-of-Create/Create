package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	public BlockEntry<CasingBlock> getCasing() {
		return casing;
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		if (context.getLevel().isClientSide)
			return InteractionResult.SUCCESS;
		context.getLevel().levelEvent(2001, context.getClickedPos(), Block.getId(state));
		KineticTileEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), AllBlocks.SHAFT.getDefaultState().setValue(AXIS, state.getValue(AXIS)));
		return InteractionResult.SUCCESS;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), te);
	}

}
