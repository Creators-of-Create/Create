package com.simibubi.create.api.event;

/**
	* Normalized request kinds for Create logistics API events.
	*
	* <p>Used to classify requests across different sources (e.g. Factory Panels and Stock Tickers)
	* while preserving their specific source data and raw request types. For Stock Ticker requests,
	* {@link StockRequestEvent#getType()} retains the original {@link com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType}.
	*/
public enum CreateRequestKind {
	/** Requests originating from a Factory Panel's crafting path (non-restocker). */
	CRAFTING,
	/** Requests originating from a Factory Panel or Stock Ticker restock mode. */
	RESTOCK,
	/** Requests originating from a Redstone Requester. */
	REDSTONE,
	/** Requests originating from a Player/Stock Ticker interaction. */
	PLAYER
}
