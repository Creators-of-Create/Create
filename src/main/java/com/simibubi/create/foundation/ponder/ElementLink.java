package com.simibubi.create.foundation.ponder;

import java.util.UUID;

import com.simibubi.create.foundation.ponder.element.PonderElement;

public class ElementLink<T extends PonderElement> {

	private Class<T> elementClass;
	private UUID id;

	public ElementLink(Class<T> elementClass) {
		this(elementClass, UUID.randomUUID());
	}

	public ElementLink(Class<T> elementClass, UUID id) {
		this.elementClass = elementClass;
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	public T cast(PonderElement e) {
		return elementClass.cast(e);
	}

}
