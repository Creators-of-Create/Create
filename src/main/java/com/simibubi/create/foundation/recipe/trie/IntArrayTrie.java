package com.simibubi.create.foundation.recipe.trie;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A one-to-many, lookup-optimized data structure for storing {@code int[]} -> {@code V} mappings,
 * which provides subset-to-many lookup.
 */
public class IntArrayTrie<V> {
	static class TrieNode<V> {
		final Int2ObjectMap<TrieNode<V>> children = new Int2ObjectOpenHashMap<>();
		final List<V> values = new ArrayList<>();
	}

	private final TrieNode<V> root = new TrieNode<>();

	private int maxDepth = 0;
	private int nodeCount = 1; // count root
	private int valueCount = 0;

	public int getMaxDepth() {
		return maxDepth;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public int getValueCount() {
		return valueCount;
	}

	/**
	 * Insert a key-value pair into the trie.
	 *
	 * @param key   a sorted array of integers
	 * @param value the value to associate with the key
	 */
	public void insert(int[] key, V value) {
		TrieNode<V> currentNode = root;
		for (int k : key) {
			currentNode = currentNode.children.computeIfAbsent(k, k1 -> {
				nodeCount++;
				return new TrieNode<>();
			});
		}
		currentNode.values.add(value);

		maxDepth = Math.max(maxDepth, key.length);
		valueCount++;
	}

	/**
	 * Look up all values associated with a subset of {@code pool}.
	 *
	 * @param pool the set of allowable keys. It SHOULD have O(1) lookup time.
	 * @return all associated values
	 */
	public List<V> lookup(IntSet pool) {
		List<V> result = new ArrayList<>();
		dfs(root, pool, result);
		return result;
	}

	private static <V> void dfs(TrieNode<V> node, IntSet pool, List<V> out) {
		out.addAll(node.values);

		// With node.children length n and pool length m,
		// The time complexity is O(min(n, m)), assuming O(1) lookup time for pool.
		if (node.children.size() > pool.size()) {
			for (int key : pool) {
				TrieNode<V> child = node.children.get(key);
				if (child != null) {
					dfs(child, pool, out);
				}
			}
		} else {
			for (var entry : node.children.int2ObjectEntrySet()) {
				if (pool.contains(entry.getIntKey())) {
					dfs(entry.getValue(), pool, out);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "IntArrayTrie{" +
			"maxDepth=" + maxDepth +
			", nodeCount=" + nodeCount +
			", valueCount=" + valueCount +
			'}';
	}
}
