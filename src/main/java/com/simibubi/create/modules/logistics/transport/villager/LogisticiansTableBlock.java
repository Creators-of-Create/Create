package com.simibubi.create.modules.logistics.transport.villager;

import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LogisticiansTableBlock extends HorizontalBlock implements IWithTileEntity<LogisticiansTableTileEntity> {

	private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 2.0D, 12.0D);
	private static final VoxelShape POLE_SHAPE = Block.makeCuboidShape(5.0D, 2.0D, 5.0D, 11.0D, 14.0D, 11.0D);
	private static final VoxelShape RENDER_SHAPE = VoxelShapes.or(BASE_SHAPE, POLE_SHAPE);
	private static final VoxelShape TOP_COLLISION_SHAPE = Block.makeCuboidShape(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
	private static final VoxelShape COLLISION_SHAPE = VoxelShapes.or(RENDER_SHAPE, TOP_COLLISION_SHAPE);

	private static final VoxelShape WEST_SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D),
			Block.makeCuboidShape(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D),
			Block.makeCuboidShape(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), RENDER_SHAPE);
	private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D),
			Block.makeCuboidShape(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D),
			Block.makeCuboidShape(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), RENDER_SHAPE);
	private static final VoxelShape EAST_SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(15.0D, 10.0D, 0.0D, 10.666667D, 14.0D, 16.0D),
			Block.makeCuboidShape(10.666667D, 12.0D, 0.0D, 6.333333D, 16.0D, 16.0D),
			Block.makeCuboidShape(6.333333D, 14.0D, 0.0D, 2.0D, 18.0D, 16.0D), RENDER_SHAPE);
	private static final VoxelShape SOUTH_SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(0.0D, 10.0D, 15.0D, 16.0D, 14.0D, 10.666667D),
			Block.makeCuboidShape(0.0D, 12.0D, 10.666667D, 16.0D, 16.0D, 6.333333D),
			Block.makeCuboidShape(0.0D, 14.0D, 6.333333D, 16.0D, 18.0D, 2.0D), RENDER_SHAPE);

	public LogisticiansTableBlock() {
		super(Properties.from(Blocks.SPRUCE_LOG));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LogisticiansTableTileEntity();
	}

	@Override
	public boolean ticksRandomly(BlockState state) {
		return true;
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		for (Entity entity : worldIn.getEntitiesWithinAABB(EntityType.VILLAGER, new AxisAlignedBB(pos).grow(10),
				e -> ((VillagerEntity) e).getVillagerData().getProfession() == VillagerProfession.NONE)) {

			VillagerEntity e = (VillagerEntity) entity;
			float f = (float) e.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
			e.setVillagerData(e.getVillagerData().withProfession(LogisticianHandler.LOGISTICIAN).withLevel(2));
			e.getBrain().registerActivity(Activity.WORK, LogisticianHandler.work(f));
			e.getBrain().setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(worldIn.getDimension().getType(), pos));
			LogisticianHandler.setJobSite(e, GlobalPos.of(worldIn.getDimension().getType(), pos));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return RENDER_SHAPE;
	}

	public boolean func_220074_n(BlockState state) {
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (!(placer instanceof PlayerEntity))
			return;
		PlayerEntity player = (PlayerEntity) placer;
		for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
			ItemStack itemStack = player.inventory.getStackInSlot(slot);
			if (!AllItems.LOGISTICAL_DIAL.typeOf(itemStack))
				continue;
			if (!itemStack.hasTag())
				continue;
			withTileEntityDo(worldIn, pos, te -> te.setNetworkId(itemStack.getTag().getUniqueId("NetworkID")));
			return;
		}
	}

	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return COLLISION_SHAPE;
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(HORIZONTAL_FACING)) {
		case NORTH:
			return NORTH_SHAPE;
		case SOUTH:
			return SOUTH_SHAPE;
		case EAST:
			return EAST_SHAPE;
		case WEST:
			return WEST_SHAPE;
		default:
			return RENDER_SHAPE;
		}
	}

}
