package com.simibubi.create.modules.contraptions.components.crusher;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CrushingWheelControllerBlock extends Block implements IHaveNoBlockItem {

	public static final BooleanProperty VALID = BooleanProperty.create("valid");

	public CrushingWheelControllerBlock() {
		super(Properties.from(Blocks.AIR));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CrushingWheelControllerTileEntity();
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(VALID);
		super.fillStateContainer(builder);
	}

	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!state.get(VALID) || CrushingWheelControllerTileEntity.isFrozen())
			return;
		CrushingWheelControllerTileEntity te = (CrushingWheelControllerTileEntity) worldIn.getTileEntity(pos);
		if (te == null)
			return;
		if (te.processingEntity == entityIn)
			entityIn.setMotionMultiplier(state, new Vec3d(0.25D, (double) 0.05F, 0.25D));
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (CrushingWheelControllerTileEntity.isFrozen())
			return;
		TileEntity tileEntity = worldIn.getTileEntity(entityIn.getPosition().down());
		if (tileEntity == null)
			return;
		if (!(tileEntity instanceof CrushingWheelControllerTileEntity))
			return;
		CrushingWheelControllerTileEntity te = (CrushingWheelControllerTileEntity) tileEntity;
		if (te.isOccupied())
			return;
		if ((entityIn instanceof PlayerEntity) && ((PlayerEntity) entityIn).isCreative()) 
			return;

		te.startCrushing(entityIn);
	}

	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!stateIn.get(VALID))
			return;
		if (rand.nextInt(1) != 0)
			return;
		double d0 = (double) ((float) pos.getX() + rand.nextFloat());
		double d1 = (double) ((float) pos.getY() + rand.nextFloat());
		double d2 = (double) ((float) pos.getZ() + rand.nextFloat());
		worldIn.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		updateSpeed(stateIn, worldIn.getWorld(), currentPos);
		return stateIn;
	}

	public void updateSpeed(BlockState state, World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null || !(tileEntity instanceof CrushingWheelControllerTileEntity))
			return;

		CrushingWheelControllerTileEntity te = (CrushingWheelControllerTileEntity) tileEntity;
		if (!state.get(VALID) || CrushingWheelControllerTileEntity.isFrozen()) {
			if (te.crushingspeed != 0) {
				te.crushingspeed = 0;
				te.sendData();
			}
			return;
		}

		for (Direction d : Direction.values()) {
			if (d.getAxis().isVertical())
				continue;
			BlockState neighbour = world.getBlockState(pos.offset(d));
			if (!AllBlocks.CRUSHING_WHEEL.typeOf(neighbour))
				continue;
			if (neighbour.get(BlockStateProperties.AXIS) == d.getAxis())
				continue;
			KineticTileEntity wheelTe = (KineticTileEntity) world.getTileEntity(pos.offset(d));
			te.crushingspeed = Math.abs(wheelTe.getSpeed() / 50f);
			te.sendData();
			break;
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		if (!state.get(VALID))
			return VoxelShapes.fullCube();

		Entity entity = context.getEntity();
		if (entity != null) {
			if (entity != null) {
				CompoundNBT data = entity.getPersistentData();
				if (data.contains("BypassCrushingWheel")) {
					if (pos.equals(NBTUtil.readBlockPos(data.getCompound("BypassCrushingWheel"))))
						return VoxelShapes.empty();
				}
			}

			if (new AxisAlignedBB(pos).contains(entity.getPositionVec()))
				return VoxelShapes.empty();

			CrushingWheelControllerTileEntity te = (CrushingWheelControllerTileEntity) worldIn.getTileEntity(pos);
			if (te == null)
				return VoxelShapes.fullCube();
			if (te.processingEntity == entity)
				return VoxelShapes.empty();
		}
		return VoxelShapes.fullCube();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.getTileEntity(pos) == null)
			return;

		CrushingWheelControllerTileEntity te = (CrushingWheelControllerTileEntity) worldIn.getTileEntity(pos);
		for (int slot = 0; slot < te.inventory.getSizeInventory(); slot++) {
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					te.inventory.getStackInSlot(slot));
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}

	}

}
