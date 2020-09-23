package com.simibubi.create.foundation.block;

import java.util.function.Consumer;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;

import com.simibubi.create.foundation.utility.WorldHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public interface ITE<T extends TileEntity> {

	Class<T> getTileEntityClass();

	default void withTileEntityDo(IBlockReader world, BlockPos pos, Consumer<T> action) {
		try {
			action.accept(getTileEntity(world, pos));
		} catch (TileEntityException e) {}
	}

	@SuppressWarnings("unchecked")
	default T getTileEntity(IBlockReader worldIn, BlockPos pos) throws TileEntityException {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		Class<T> expectedClass = getTileEntityClass();

		IWorld world = null;
		if (worldIn instanceof IWorld)
			world = (IWorld) worldIn;

		if (tileEntity == null)
			throw new MissingTileEntityException(world, pos, expectedClass);
		if (!expectedClass.isInstance(tileEntity))
			throw new InvalidTileEntityException(world, pos, expectedClass, tileEntity.getClass());

		return (T) tileEntity;
	}

	static class TileEntityException extends Throwable {
		private static final long serialVersionUID = 1L;

		public TileEntityException(IWorld world, BlockPos pos, Class<?> teClass) {
			super(makeBaseMessage(world, pos, teClass));
		}

		public TileEntityException(String message) {
			super(message);
			report(this);
		}

		static String makeBaseMessage(IWorld world, BlockPos pos, Class<?> expectedTeClass) {
			return String.format("[%s] @(%d, %d, %d), expecting a %s", getDimensionName(world), pos.getX(), pos.getY(),
					pos.getZ(), expectedTeClass.getSimpleName());
		}

		static String getDimensionName(IWorld world) {
			String notAvailable = "Dim N/A";
			if (world == null)
				return notAvailable;
			ResourceLocation registryName = WorldHelper.getDimensionID(world);
			if (registryName == null)
				return notAvailable;
			return registryName.toString();
		}
	}

	static class MissingTileEntityException extends TileEntityException {
		private static final long serialVersionUID = 1L;

		public MissingTileEntityException(IWorld world, BlockPos pos, Class<?> teClass) {
			super("Missing TileEntity: " + makeBaseMessage(world, pos, teClass));
		}

	}

	static class InvalidTileEntityException extends TileEntityException {
		private static final long serialVersionUID = 1L;

		public InvalidTileEntityException(IWorld world, BlockPos pos, Class<?> expectedTeClass, Class<?> foundTeClass) {
			super("Wrong TileEntity: " + makeBaseMessage(world, pos, expectedTeClass) + ", found "
					+ foundTeClass.getSimpleName());
		}
	}

	static void report(TileEntityException e) {
		if (AllConfigs.COMMON.logTeErrors.get())
			Create.logger.debug("TileEntityException thrown!", e);
	}

}
