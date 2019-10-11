package com.simibubi.create.modules.logistics.management.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.type.CountedItemsList;
import com.simibubi.create.foundation.type.CountedItemsList.ItemStackEntry;
import com.simibubi.create.modules.logistics.management.LogisticalNetwork;
import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity;
import com.simibubi.create.modules.logistics.management.controller.LogisticalInventoryControllerTileEntity;
import com.simibubi.create.modules.logistics.management.index.IndexContainerUpdatePacket.Type;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.PacketDistributor;

public class LogisticalIndexTileEntity extends LogisticalActorTileEntity implements INamedContainerProvider {

	// Server
	public int nextPush;
	public Set<ServerPlayerEntity> playersEntered = new HashSet<>();
	protected Set<ServerPlayerEntity> playersUsing = new HashSet<>();
	protected List<Pair<String, CountedItemsList>> controllersToUpdate = new LinkedList<>();

	// Both
	public String lastOrderAddress = null;
	protected Map<String, CountedItemsList> controllers = new HashMap<>();
	
	// Client 
	public boolean update = false;
	public List<String> availableReceivers = new ArrayList<>();

	public LogisticalIndexTileEntity() {
		super(AllTileEntities.LOGISTICAL_INDEX.type);
		nextPush = 0;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (lastOrderAddress != null)
			compound.putString("LastAdress", lastOrderAddress);
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		if (compound.contains("LastAdress"))
			lastOrderAddress = compound.getString("LastAdress");
		super.read(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		ListNBT receivers = new ListNBT();
		availableReceivers.forEach(s -> receivers.add(new StringNBT(s)));
		tag.put("Receivers", receivers);
		return super.writeToClient(tag);
	}
	
	@Override
	public void readClientUpdate(CompoundNBT tag) {
		availableReceivers.clear();
		for (INBT inbt : tag.getList("Receivers", NBT.TAG_STRING))
			availableReceivers.add(((StringNBT) inbt).getString());
		update = true;
		super.readClientUpdate(tag);
	}
	
	public void syncReceivers() {
		if (network == null)
			return;
		availableReceivers.clear();
		for (LogisticalActorTileEntity logisticalControllerTileEntity : network.receivers)
			availableReceivers.add(logisticalControllerTileEntity.address);
		sendData();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		syncReceivers();
	}

	@Override
	public void tick() {
		super.tick();

		if (nextPush == 1)
			pushItems();

		if (nextPush > 0)
			nextPush--;
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new LogisticalIndexContainer(id, inv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName().toString());
	}

	public void sendToContainer(PacketBuffer buffer) {
		buffer.writeBlockPos(getPos());
		buffer.writeCompoundTag(getUpdateTag());
	}

	public void addPlayer(ServerPlayerEntity player) {
		nextPush = 5;
		playersEntered.add(player);
		playersUsing.add(player);
	}

	public void removePlayer(ServerPlayerEntity player) {
		playersUsing.remove(player);
		if (playersUsing.isEmpty())
			controllers.clear();
	}

	public void handleUpdatedController(String address, CountedItemsList updates) {
		if (playersUsing.isEmpty())
			return;
		controllersToUpdate.add(Pair.of(address, updates));
		if (nextPush == 0)
			nextPush = 5;
	}

	private void pushItems() {
		LogisticalNetwork network = this.getNetwork();
		if (network == null)
			return;

		// First player to open
		if (!playersEntered.isEmpty() && playersUsing.size() == playersEntered.size()) {
			controllers.clear();
			for (LogisticalActorTileEntity te : network.suppliers) {
				if (!(te instanceof LogisticalInventoryControllerTileEntity))
					continue;
				CountedItemsList allItems = ((LogisticalInventoryControllerTileEntity) te).getAllItems();
				controllers.put(te.address, allItems);
			}
		}

		// Initial Packets
		if (!playersEntered.isEmpty()) {
			controllers.forEach((address, items) -> {
				for (ServerPlayerEntity player : playersUsing)
					AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player),
							new IndexContainerUpdatePacket(Type.INITIAL, address, items, pos));
			});
			playersEntered.clear();
		}

		// pending Incremental Updates
		if (!playersUsing.isEmpty() && !controllersToUpdate.isEmpty()) {
			for (Pair<String, CountedItemsList> pair : controllersToUpdate) {
				CountedItemsList list = controllers.getOrDefault(pair.getKey(), new CountedItemsList());
				pair.getValue().getFlattenedList().forEach(list::add);
			}
			for (ServerPlayerEntity player : playersUsing)
				AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player),
						new IndexContainerUpdatePacket(Type.UPDATE, controllersToUpdate, pos));
			controllersToUpdate.clear();
		}
	}

	public void index(List<Pair<String, CountedItemsList>> items) {
		items.forEach(pair -> {
			controllers.put(pair.getKey(), pair.getValue());
		});
		update = true;
	}

	public void update(List<Pair<String, CountedItemsList>> items) {
		for (Pair<String, CountedItemsList> pair : items) {
			if (!controllers.containsKey(pair.getKey()))
				return;
			CountedItemsList list = controllers.get(pair.getKey());
			for (ItemStackEntry entry : pair.getValue().getFlattenedList()) {
				list.setItemCount(entry.stack, entry.amount);
			}
		}
		update = true;
	}

}
