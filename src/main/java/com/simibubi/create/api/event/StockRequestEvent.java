package com.simibubi.create.api.event;

import java.util.Objects;
import java.util.UUID;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
	* Fired server-side when a Stock Ticker or Redstone Requester is about to submit a
	* logistics request.
	*
	* <p>This event is cancelable. If canceled, Create will not submit the request to its logistics
	* network.</p>
	*
	* <p>Note: {@link #getOrder()} returns the mutable order object used by Create. Mutation is
	* unsupported and may break internal Create logic. The address may be empty if the requester has
	* no address configured. {@link #getType()} is never null.</p>
	*/
public class StockRequestEvent extends Event implements ICancellableEvent {
	private final StockCheckingBlockEntity source;
	private final UUID network;
	private final RequestType type;
	private final PackageOrderWithCrafts order;
	private final String address;
	private final CreateRequestKind kind;

	public StockRequestEvent(
		StockCheckingBlockEntity source,
		UUID network,
		RequestType type,
		PackageOrderWithCrafts order,
		String address) {
		this.source = source;
		this.network = network;
		this.type = Objects.requireNonNull(type, "type");
		this.order = order;
		this.address = address;
		this.kind = mapKind(type);
	}

	public StockCheckingBlockEntity getSource() {
		return source;
	}

	public UUID getNetwork() {
		return network;
	}

	public RequestType getType() {
		return type;
	}

	public CreateRequestKind getKind() {
		return kind;
	}

	public PackageOrderWithCrafts getOrder() {
		return order;
	}

	public String getAddress() {
		return address;
	}

	private static CreateRequestKind mapKind(RequestType type) {
		return switch (type) {
			case RESTOCK -> CreateRequestKind.RESTOCK;
			case REDSTONE -> CreateRequestKind.REDSTONE;
			case PLAYER -> CreateRequestKind.PLAYER;
		};
	}
}
