package com.simibubi.create.modules.logistics.transport.villager;

import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;

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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LogisticiansTableBlock extends HorizontalBlock implements IWithTileEntity<LogisticiansTableTileEntity> {

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

	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.LOGISTICS_TABLE_BASE;
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.LOGISTICS_TABLE.get(state.get(HORIZONTAL_FACING));
	}

}
