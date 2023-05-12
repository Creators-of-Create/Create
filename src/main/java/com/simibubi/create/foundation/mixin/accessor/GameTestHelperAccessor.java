package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameTestHelper.class)
public interface GameTestHelperAccessor {
	@Accessor
	GameTestInfo getTestInfo();
	@Accessor
	boolean getFinalCheckAdded();
	@Accessor
	void setFinalCheckAdded(boolean value);
}
