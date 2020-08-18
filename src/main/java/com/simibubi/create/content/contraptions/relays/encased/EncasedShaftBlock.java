package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.CasingBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;

import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class EncasedShaftBlock extends RotatedPillarKineticBlock {

	public static final IProperty<Casing> CASING = EnumProperty.create("casing", Casing.class);

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
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.getPlayer() != null && context.getPlayer()
			.isSneaking())
			return super.getStateForPlacement(context);
		Axis preferredAxis = getPreferredAxis(context);
		return this.getDefaultState()
			.with(AXIS, preferredAxis == null ? context.getNearestLookingDirection()
				.getAxis() : preferredAxis);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
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
		public String getName() {
			return Lang.asId(name());
		}
	}

}
