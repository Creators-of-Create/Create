package com.simibubi.create.modules.schematics;

public interface ISpecialEntityItemRequirement {

	default ItemRequirement getRequiredItems() {
		return ItemRequirement.INVALID;
	}
	
}
