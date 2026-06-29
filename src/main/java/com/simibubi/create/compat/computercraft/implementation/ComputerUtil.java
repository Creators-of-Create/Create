package com.simibubi.create.compat.computercraft.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.simibubi.create.compat.computercraft.implementation.luaObjects.LuaComparable;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import net.createmod.catnip.data.Glob;

import net.neoforged.neoforge.items.IItemHandler;

public class ComputerUtil {

	public static int bigItemStackToLuaTableFilter(BigItemStack entry, Map<?, ?> filter) throws LuaException {
		Map<String, Object> details = VanillaDetailRegistries.ITEM_STACK.getDetails(entry.stack);

		// Count needs to be replaced because BigItemStack can have a different count than the stack
		details.put("count", entry.count);

		// If name is in filter and doesn't have : in it add minecraft: namespace
		if (filter.containsKey("name") && filter.get("name") instanceof String name) {
			if (!name.contains(":")) {
				details.put("name", "minecraft:" + name);
			}
		}

		if (!deepEquals(new HashMap<>(filter), details))
			return 0;
		return entry.count;
	}

	private static boolean deepEquals(Object fVal, Object iVal) throws LuaException {
		// Checks all String, Number, Boolean and null values
		if (Objects.equals(iVal, fVal)) return true;

		// Lua Objects can implement LuaComparable to provide a table representation for lazy filtering
		if (iVal instanceof LuaComparable iStack) {
			return deepEquals(fVal, iStack.getTableRepresentation());
		}

		// If both are numbers, compare them as doubles because lua numbers are always doubles
		if (fVal instanceof Number fn && iVal instanceof Number in)
			return Double.compare(fn.doubleValue(), in.doubleValue()) == 0;

		// Other comparisons for Not, Type, Numbers, and Strings
		// Example: count = { _op =  ">=", value = 10 }
		if (fVal instanceof Map<?, ?> fMap && fMap.get("_op") instanceof String op &&
			fMap.get("value") != null) {
			// Value to use operator on
			Object fValue = fMap.get("value");
			switch (op) {
				case "not" -> { // Not operator
					return !deepEquals(fValue, iVal);
				}
				case "any", "all" -> { // Any / All operator
					final String errorMsg = op + " operator requires a list of values";

					if (!(fValue instanceof Map<?, ?> valueMap))
						throw new LuaException(errorMsg);

					List<?> values = toOrderedList(valueMap);
					if (values == null)
						throw new LuaException(errorMsg);

					boolean isAll = op.equals("all");
					for (Object v : values) {
						boolean match = deepEquals(v, iVal);
						if (isAll) {
							if (!match) return false;
						} else {
							if (match) return true;
						}
					}
					return isAll;
				}
				case "type" -> { // Type operator
					if (!(fValue instanceof String type)) {
						throw new LuaException("Type operator requires a string value");
					}
					if (iVal == null) return type.equals("nil");
					return switch (type) {
						case "nil" -> iVal == null;
						case "number" -> iVal instanceof Number;
						case "string" -> iVal instanceof String;
						case "boolean" -> iVal instanceof Boolean;
						case "table" -> iVal instanceof Map<?, ?> || iVal instanceof List<?>;
						case "list" -> iVal instanceof List<?>;  // Additional check for list
						case "map" -> iVal instanceof Map<?, ?>; // Additional check for map
						case "object" -> iVal instanceof LuaComparable; // For compatible objects
						default -> throw new LuaException("Unknown type: " + type);
					};
				}
			}

			// Number comparison
			if (iVal instanceof Number in && fValue instanceof Number val)
				return switch (op) {
					case ">" -> in.doubleValue() > val.doubleValue();
					case ">=" -> in.doubleValue() >= val.doubleValue();
					case "<" -> in.doubleValue() < val.doubleValue();
					case "<=" -> in.doubleValue() <= val.doubleValue();
					case "==" -> in.doubleValue() == val.doubleValue();
					case "~=" -> in.doubleValue() != val.doubleValue();
					default -> throw new LuaException("Unknown operator: " + op);
				};

			// String matching
			if (iVal instanceof String inStr && fValue instanceof String fStr) {
				return switch (op) {
					case "glob" -> inStr.matches(Glob.toRegexPattern(fStr, ""));
					case "regex" -> inStr.matches(fStr);
					default -> throw new LuaException("Unknown operator: " + op);
				};
			}

			throw new LuaException("Operator " + op + " not supported for type " +
				(fValue == null ? "null" : fValue.getClass().getSimpleName()));
		}

		// Convert to collections
		Collection fColl = Collection.of(fVal);
		Collection iColl = Collection.of(iVal);
		// If one is not a collection, return false
		if (fColl == null || iColl == null)
			return false;

		// Compare as list or map
		if (iColl.isList() && fColl.isList()) return matchList(fColl, iColl);
		if (iColl.isMap() && fColl.isMap()) return matchMap(fColl, iColl);
		return false;
	}

	private static boolean matchList(Collection f, Collection i) throws LuaException {
		switch (f.mode) {
			case EXACT -> {
				if (f.list.size() != i.list.size()) return false;
				for (int k = 0; k < f.list.size(); k++)
					if (!deepEquals(f.list.get(k), i.list.get(k)))
						return false;
				return true;
			}
			case CONTAINS -> {
				outer:
				for (Object fVal : f.list) {
					for (Iterator<?> it = i.list.iterator(); it.hasNext(); ) {
						Object iVal = it.next();
						if (deepEquals(fVal, iVal)) {
							it.remove();
							continue outer;
						}
					}
					return false;
				}
				return true;
			}
			case CONTAINED -> {
				outer:
				for (Object iVal : i.list) {
					for (Iterator<?> it = f.list.iterator(); it.hasNext(); ) {
						Object fVal = it.next();
						if (deepEquals(fVal, iVal)) {
							it.remove();
							continue outer;
						}
					}
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private static boolean matchMap(Collection f, Collection i) throws LuaException {
		switch (f.mode) {
			case EXACT -> {
				if (!f.map.keySet().equals(i.map.keySet())) return false;
				for (var e : f.map.entrySet()) {
					if (!deepEquals(e.getValue(), i.map.get(e.getKey())))
						return false;
				}
				return true;
			}
			case CONTAINS -> {
				for (var e : f.map.entrySet()) {
					if (!i.map.containsKey(e.getKey())
						|| !deepEquals(e.getValue(), i.map.get(e.getKey())))
						return false;
				}
				return true;
			}
			case CONTAINED -> {
				for (var e : i.map.entrySet()) {
					if (!f.map.containsKey(e.getKey())
						|| !deepEquals(f.map.get(e.getKey()), e.getValue()))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	// Is a wrapper for lua tables so they can be processed as lists or maps
	private record Collection(MatchMode mode, List<?> list, Map<?, ?> map) {
		boolean isList() {
			return list != null;
		}

		boolean isMap() {
			return map != null;
		}

		// Returns null if not a list or map
		static Collection of(Object o) throws LuaException {
			if (o instanceof Map<?, ?> m) {
				MatchMode mode = MatchMode.parse(m.get("_mode"));
				m.remove("_mode");
				// Null if not an array-like map
				List<Object> lst = toOrderedList(m);
				return new Collection(mode, lst, m);
			}
			// List for CC, never from filter
			if (o instanceof List<?> raw) {
				return new Collection(MatchMode.CONTAINS, raw, null);
			}
			return null;
		}
	}

	// Allows user to specify match mode in filter for the specific list or map
	// Exact: 1:1 match, list must be same size and order
	// Contains: All elements in filter must be in the item, order doesn't matter, args removed from filter as they are found
	// Contained: All elements in item must be in the filter, order doesn't matter, args removed from item as they are found
	private enum MatchMode {
		EXACT, CONTAINS, CONTAINED;

		static MatchMode parse(Object t) throws LuaException {
			if (!(t instanceof String s)) return CONTAINS;
			return switch (s.toLowerCase()) {
				case "exact" -> EXACT;
				case "contains" -> CONTAINS;
				case "contained" -> CONTAINED;
				default -> throw new LuaException(
					"Invalid match mode: " + s + ", expected 'exact', 'contained' or 'contains'");
			};
		}
	}

	// All arrays from lua are passed as maps so we check if it is an array-like map
	private static boolean isArrayLike(Map<?, ?> map) {
		int n = map.size();
		if (n == 0) return true;

		boolean[] seen = new boolean[n];
		for (Object keyObj : map.keySet()) {
			if (!(keyObj instanceof Number)) return false;
			int k = ((Number) keyObj).intValue() - 1;
			if (k != (double) k) return false; // not an whole number
			if (k < 0 || k >= n || seen[k]) return false;
			seen[k] = true;
		}

		for (boolean ok : seen)
			if (!ok) return false;

		return true;
	}

	private static List<Object> toOrderedList(Map<?, ?> m) {
		if (!isArrayLike(m)) {
			return null;
		}
		int n = m.size();
		List<Object> out = new ArrayList<>(Collections.nCopies(n, null));
		for (var e : m.entrySet())
			out.set(((Number) e.getKey()).intValue() - 1, e.getValue());
		return out;
	}

	public static Map<Integer, Map<String, ?>> list(IItemHandler inventory) {
		Map<Integer, Map<String, ?>> result = new HashMap<>();
		var size = inventory.getSlots();
		for (var i = 0; i < size; i++) {
			var stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) result.put(i + 1, VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stack));
		}

		return result;
	}

	public static Map<String, ?> getItemDetail(IItemHandler inventory, int slot) throws LuaException {

		int maxSlots = inventory.getSlots();
		if (slot < 1 || slot > maxSlots)
			throw new LuaException(String.format("Slot " + slot + " out of range, available slots between " + 1 + " and " + maxSlots));
		var stack = inventory.getStackInSlot(slot - 1);
		return stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
	}

	public static Map<String, ?> getItemDetail(InventorySummary inventorySummary, int slot) throws LuaException {
		List<BigItemStack> stacks = inventorySummary.getStacks();
		int maxSlots = stacks.size();
		if (slot < 1 || slot > maxSlots)
			throw new LuaException(String.format("Slot " + slot + " out of range, available slots between " + 1 + " and " + maxSlots));
		BigItemStack entry = stacks.get(slot - 1);
		Map<String, Object> details = new HashMap<>(VanillaDetailRegistries.ITEM_STACK.getDetails(entry.stack));
		details.put("count", entry.count);

		return
			entry.stack.isEmpty() ? null : details;
	}
}
