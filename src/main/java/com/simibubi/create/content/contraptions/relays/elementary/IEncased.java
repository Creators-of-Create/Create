package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.base.CasingBlock;

/**
 * Implement this interface to indicate that this block is an EncasedBlock
 */
public interface IEncased {
	CasingBlock getCasing();
	void setCasing(CasingBlock casing);
}
