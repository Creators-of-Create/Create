package com.simibubi.create.api.event;

import java.util.UUID;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
	* Fired server-side when a Factory Panel is about to submit a logistics request for a
	* network.
	*
	* <p>This event is cancelable. If canceled, Create will not submit the request to its
	* logistics network. External mods can use this to route or fulfill the request elsewhere.</p>
	*
	* <p>Note: {@link #getOrder()} returns the mutable order object used by Create. Mutation is
	* unsupported and may break internal Create logic. The address may be empty if the panel has no
	* address configured.</p>
	*/
public class FactoryPanelRequestEvent extends Event implements ICancellableEvent {
	private final FactoryPanelBehaviour panel;
	private final UUID network;
	private final PackageOrderWithCrafts order;
	private final String address;
	private final CreateRequestKind kind;

	public FactoryPanelRequestEvent(
		FactoryPanelBehaviour panel,
		UUID network,
		PackageOrderWithCrafts order,
		String address,
		CreateRequestKind kind) {
		this.panel = panel;
		this.network = network;
		this.order = order;
		this.address = address;
		this.kind = kind;
	}

	public FactoryPanelBehaviour getPanel() {
		return panel;
	}

	public UUID getNetwork() {
		return network;
	}

	public PackageOrderWithCrafts getOrder() {
		return order;
	}

	public String getAddress() {
		return address;
	}

	public CreateRequestKind getKind() {
		return kind;
	}
}
