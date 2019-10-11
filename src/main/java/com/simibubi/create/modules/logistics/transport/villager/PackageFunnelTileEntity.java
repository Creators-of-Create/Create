package com.simibubi.create.modules.logistics.transport.villager;

import static com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity.ShippingInventory.RECEIVING;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;
import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingTileEntity;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity.ShippingInventory;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PackageFunnelTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public static final int INSERTION_DELAY = 10;

	protected int initialize;
	protected List<String> addresses;
	protected boolean waitingForSpace;
	protected int cooldown;
	protected AxisAlignedBB bb;

	public PackageFunnelTileEntity() {
		super(AllTileEntities.PACKAGE_FUNNEL.type);
		addresses = new ArrayList<String>();
		waitingForSpace = false;
		cooldown = 10;
	}
	
	public void slotOpened() {
		waitingForSpace = false;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		initialize = 2;
		bb = new AxisAlignedBB(getPos());
	}

	@Override
	public void tick() {
		
		// Initialize AFTER the actors to get a working network reference
		if (initialize > -1)
			initialize--;
		if (initialize == 0) {
			initialize();
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		if (waitingForSpace)
			return;

		tryInsert();
		cooldown = INSERTION_DELAY;
	}

	protected void initialize() {
		initialize = -1;
		refreshAddressList();
	}

	public void refreshAddressList() {
		addresses.clear();
		LogisticalCasingTileEntity casingTE = getCasingTE();
		if (casingTE == null)
			return;
		for (LogisticalActorTileEntity actor : casingTE.getControllers()) {
			addresses.add(actor.address);
			if (actor.getNetwork() != null)
				actor.getNetwork().addPackageTarget(this);
		}
	}

	public LogisticalCasingTileEntity getCasingTE() {
		BlockPos casingPos = pos.offset(getBlockState().get(BlockStateProperties.FACING).getOpposite());
		TileEntity te = world.getTileEntity(casingPos);
		if (te == null || !AllTileEntities.LOGISTICAL_CASING.typeOf(te))
			return null;
		LogisticalCasingTileEntity casingTE = (LogisticalCasingTileEntity) te;
		return casingTE;
	}

	private void tryInsert() {
		for (CardboardBoxEntity e : world.getEntitiesWithinAABB(CardboardBoxEntity.class, bb, e -> {
			if (e.isPassenger())
				return false;
			for (String address : addresses)
				if (LogisticalNetwork.matchAddresses(address, e.getAddress()))
					return true;
			return false;
		})) {
			LogisticalCasingTileEntity casingTE = getCasingTE();
			if (casingTE == null)
				return;
			for (LogisticalActorTileEntity actor : casingTE.getControllers()) {
				if (!(actor instanceof LogisticalInventoryControllerTileEntity))
					continue;
				if (LogisticalNetwork.matchAddresses(actor.address, e.getAddress())) {
					LogisticalInventoryControllerTileEntity invTe = (LogisticalInventoryControllerTileEntity) actor;
					if (!invTe.isReceiver())
						continue;
					ShippingInventory inventory = invTe.getInventory();
					if (!inventory.getStackInSlot(RECEIVING).isEmpty()) {
						waitingForSpace = true;
						return;
					}
					inventory.insertItem(RECEIVING, e.getBox(), false);
					e.remove();
					return;
				}
			}
			return;
		}
	}

	@Override
	public void remove() {
		BlockPos casingPos = pos.offset(getBlockState().get(BlockStateProperties.FACING).getOpposite());
		TileEntity te = world.getTileEntity(casingPos);
		if (te == null || !AllTileEntities.LOGISTICAL_CASING.typeOf(te))
			return;
		LogisticalCasingTileEntity casingTE = (LogisticalCasingTileEntity) te;
		for (LogisticalActorTileEntity actor : casingTE.getControllers()) {
			if (actor.getNetwork() != null)
				actor.getNetwork().removePackageTarget(this);
		}
		super.remove();
	}

	public List<String> getAddressList() {
		return addresses;
	}

}
