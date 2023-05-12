package com.simibubi.create.compat.computercraft.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTable;
import dan200.computercraft.api.lua.LuaValues;

public class CreateLuaTable implements LuaTable<Object, Object> {

	private final Map<Object, Object> map;

	public CreateLuaTable() {
		this.map = new HashMap<>();
	}

	public CreateLuaTable(Map<?, ?> map) {
		this.map = new HashMap<>(map);
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

	public Map<Object, Object> getMap() {
		return map;
	}

	@Nullable
	@Override
	public Object put(Object key, Object value) {
		return map.put(key, value);
	}

	public void putBoolean(String key, boolean value) {
		map.put(key, value);
	}

	public void putDouble(String key, double value) {
		map.put(key, value);
	}

	public void putString(String key, String value) {
		map.put(key, value);
	}

	public void putTable(String key, CreateLuaTable value) {
		map.put(key, value);
	}

	public void putTable(int i, CreateLuaTable value) {
		map.put(i, value);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		return map.containsValue(o);
	}

	@Override
	public Object get(Object o) {
		return map.get(o);
	}

	@NotNull
	@Override
	public Set<Object> keySet() {
		return map.keySet();
	}

	@NotNull
	@Override
	public Collection<Object> values() {
		return map.values();
	}

	@NotNull
	@Override
	public Set<Entry<Object, Object>> entrySet() {
		return map.entrySet();
	}

}
