package com.simibubi.create.foundation.registrate;

import com.simibubi.create.Create;
import com.tterrag.registrate.util.NonNullLazyValue;

public class CreateRegistrate extends CreateRegistrateBase<CreateRegistrate> {

	/**
	 * Create a new {@link CreateRegistrate} and register event listeners for
	 * registration and data generation. Used in lieu of adding side-effects to
	 * constructor, so that alternate initialization strategies can be done in
	 * subclasses.
	 * 
	 * @param modid The mod ID for which objects will be registered
	 * @return The {@link CreateRegistrate} instance
	 */
	public CreateRegistrate(String modid) {
		super(modid, () -> Create.baseCreativeTab);
	}

	public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(() -> new CreateRegistrate(modid));
	}
	
}
