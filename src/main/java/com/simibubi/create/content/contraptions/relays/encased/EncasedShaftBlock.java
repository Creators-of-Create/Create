package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;

public class EncasedShaftBlock extends AbstractEncasedShaftBlock {

	public static final Property<Casing> CASING = EnumProperty.create("casing", Casing.class);

	public EncasedShaftBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.getDefaultState().with(CASING, Casing.ANDESITE));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(CASING);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(state.get(CASING).getCasingEntry().get().asItem());
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	@Override
	public ActionResultType onSneakWrenched(BlockState state, ItemUseContext context) {
		if (context.getWorld().isRemote)
			return ActionResultType.SUCCESS;

		KineticTileEntity.switchToBlockState(context.getWorld(), context.getPos(), AllBlocks.SHAFT.getDefaultState().with(AXIS, state.get(AXIS)));
		return ActionResultType.SUCCESS;
	}

	public enum Casing implements IStringSerializable {
		ANDESITE(AllBlocks.ANDESITE_CASING),
		BRASS(AllBlocks.BRASS_CASING),
		//COPPER(AllBlocks.COPPER_CASING)

		;

		private final BlockEntry<CasingBlock> casingEntry;

		Casing(BlockEntry<CasingBlock> casingEntry) {
			this.casingEntry = casingEntry;
		}

		public BlockEntry<CasingBlock> getCasingEntry() {
			return casingEntry;
		}

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

}
