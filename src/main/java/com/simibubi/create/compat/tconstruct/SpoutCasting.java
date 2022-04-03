package com.simibubi.create.compat.tconstruct;

import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpoutCasting extends BlockSpoutingBehaviour {

	static Boolean TICON_PRESENT = null;

	ResourceLocation TABLE = new ResourceLocation("tconstruct", "table");
	ResourceLocation BASIN = new ResourceLocation("tconstruct", "basin");

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutTileEntity spout, FluidStack availableFluid,
		boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity te = level.getBlockEntity(pos);
		if (te == null)
			return 0;

		Storage<FluidVariant> handler = TransferUtil.getFluidStorage(te, Direction.UP);
		if (handler == null)
			return 0;

		ResourceLocation registryName = Registry.BLOCK_ENTITY_TYPE.getKey(te.getType());
		if (!registryName.equals(TABLE) && !registryName.equals(BASIN))
			return 0;

		// Do not fill if it would only partially fill the table (unless > 1000mb)
		long amount = availableFluid.getAmount();
		try (Transaction t = TransferUtil.getTransaction()) {
			long inserted = handler.insert(availableFluid.getType(), amount, t);
			if (amount < FluidConstants.BUCKET) {
				try (Transaction nested = t.openNested()) {
					if (handler.insert(availableFluid.getType(), 1, nested) == 1)
						return 0;
				}
			}

			if (!simulate) t.commit();
			return inserted;
		}
	}

	private boolean enabled() {
		if (TICON_PRESENT == null)
			TICON_PRESENT = Mods.TCONSTRUCT.isLoaded();
		if (!TICON_PRESENT)
			return false;
		return AllConfigs.SERVER.recipes.allowCastingBySpout.get();
	}

}
