package com.simibubi.create.compat.computercraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaValues;
import dan200.computercraft.api.lua.ObjectLuaTable;

public class CreateLuaTable extends ObjectLuaTable {

	public CreateLuaTable(Map<?, ?> map) {
		super(map);
	}

	public boolean getBoolean(String key) throws LuaException {
		Object value = get(key);

		if (!(value instanceof Boolean))
			throw LuaValues.badField(key, "boolean", LuaValues.getType(value));

		return (Boolean) value;
	}

	public String getString(String key) throws LuaException {
		Object value = get(key);

		if (!(value instanceof String))
			throw LuaValues.badField(key, "string", LuaValues.getType(value));

		return (String) value;
	}

	public CreateLuaTable getTable(String key) throws LuaException {
		Object value = get(key);

		if (!(value instanceof Map<?, ?>))
			throw LuaValues.badField(key, "table", LuaValues.getType(value));

		return new CreateLuaTable((Map<?, ?>) value);
	}

	public Optional<Boolean> getOptBoolean(String key) throws LuaException {
		Object value = get(key);

		if (value == null)
			return Optional.empty();

		if (!(value instanceof Boolean))
			throw LuaValues.badField(key, "boolean", LuaValues.getType(value));

		return Optional.of((Boolean) value);
	}

	public Set<String> stringKeySet() throws LuaException {
		Set<String> stringSet = new HashSet<>();

		for (Object key : keySet()) {
			if (!(key instanceof String))
				throw new LuaException("key " + key + " is not string (got " + LuaValues.getType(key) + ")");

			stringSet.add((String) key);
		}

		return Collections.unmodifiableSet(stringSet);
	}

	public Collection<CreateLuaTable> tableValues() throws LuaException {
		List<CreateLuaTable> tables = new ArrayList<>();

		for (int i = 1; i <= size(); i++) {
			Object value = get((double) i);

			if (!(value instanceof Map<?, ?>))
				throw new LuaException("value " + value + " is not table (got " + LuaValues.getType(value) + ")");

			tables.add(new CreateLuaTable((Map<?, ?>) value));
		}

		return Collections.unmodifiableList(tables);
	}

}
