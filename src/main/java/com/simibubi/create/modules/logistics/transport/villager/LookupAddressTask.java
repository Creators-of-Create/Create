package com.simibubi.create.modules.logistics.transport.villager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class LookupAddressTask extends PackageDeliveryTask {

	public LookupAddressTask() {
		super(10);
	}

	@Override
	protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
		if (!super.shouldExecute(worldIn, owner))
			return false;
		CardboardBoxEntity box = getBox(owner);
		GlobalPos rememberedAddress = LogisticianHandler.getRememberedAddress(owner, box.getAddress());
		if (rememberedAddress != null)
			return false;
		return LogisticianHandler.getJobSite(owner).getPos().withinDistance(owner.getPosition(), 1.5);
	}

	@Override
	protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
		LogisticianHandler.ponder(entityIn, "Let's have a look...");
		BlockPos tablePos = LogisticianHandler.getJobSite(entityIn).getPos();
		if (!AllBlocks.LOGISTICIANS_TABLE.typeOf(worldIn.getBlockState(tablePos)))
			return;
		TileEntity te = worldIn.getTileEntity(tablePos);
		if (te == null || !(te instanceof LogisticiansTableTileEntity))
			return;
		LogisticiansTableTileEntity lte = (LogisticiansTableTileEntity) te;
		String address = getBox(entityIn).getAddress();
		if (lte.getNetwork() == null)
			return;
		for (PackageFunnelTileEntity packageFunnelTileEntity : lte.getNetwork().packageTargets) {
			for (String string : packageFunnelTileEntity.addresses) {
				if (string.toLowerCase().equals(address.toLowerCase())) {
					GlobalPos globalPos = GlobalPos.of(worldIn.getDimension().getType(), packageFunnelTileEntity.getPos());
					LogisticianHandler.rememberAddress(entityIn, address, globalPos);
					LogisticianHandler.ponder(entityIn, "I see!");
					return;
				}
			}
		}
		LogisticianHandler.ponder(entityIn, "Hmm. Can't find that one");
	}

}
