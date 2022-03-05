package com.simibubi.create.content.schematics.block;

import javax.annotation.Nullable;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import io.github.fabricators_of_create.porting_lib.util.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SchematicannonBlock extends Block implements ITE<SchematicannonTileEntity> {

	public SchematicannonBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.SCHEMATICANNON_SHAPE;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		if (entity != null) {
			withTileEntityDo(level, pos, be -> {
				be.defaultYaw = (-Mth.floor((entity.getYRot() + (entity.isShiftKeyDown() ? 180.0F : 0.0F)) * 16.0F / 360.0F + 0.5F) & 15) * 360.0F / 16.0F;
			});
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		withTileEntityDo(worldIn, pos,
				te -> NetworkUtil.openGui((ServerPlayer) player, te, te::sendToContainer));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		withTileEntityDo(worldIn, pos, te -> te.neighbourCheckCooldown = 0);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public Class<SchematicannonTileEntity> getTileEntityClass() {
		return SchematicannonTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends SchematicannonTileEntity> getTileEntityType() {
		return AllTileEntities.SCHEMATICANNON.get();
	}

}
