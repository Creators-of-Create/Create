package com.simibubi.create.compat.computercraft.implementation.luaObjects;

import java.util.Map;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.content.logistics.BigItemStack;

public class LuaBigItemStack implements LuaComparable {
	private final BigItemStack stack;

	public LuaBigItemStack(BigItemStack stack) {
		this.stack = stack;
	}

	@Override
	public Map<?, ?> getTableRepresentation() {
		Map<String, Object> details = ComputerUtil.getDetails(stack.stack);
		// Add count to the details
		details.put("count", stack.count);
		return details;
	}
}
