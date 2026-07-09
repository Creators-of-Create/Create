package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.CrashReportCategory.Entry;
import net.minecraft.SystemReport;

@Mixin(SystemReport.class)
public interface SystemReportAccessor {
	@Accessor
	static String getOPERATING_SYSTEM() {
		throw new AssertionError();
	}

	@Accessor
	static String getJAVA_VERSION() {
		throw new AssertionError();
	}

	@Accessor
	List<Entry> getEntries();
}
