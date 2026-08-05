package com.amazon.lld.catalog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Single node in the product search Trie.
 * <p>
 * Why: Trie edges are characters; nodes hold which product ids are reachable
 * through the path from the root to this node.
 * <p>
 * Logic: {@code children} maps next character → child node;
 * {@code productIds} are all products whose indexed text passes through this node.
 */
public class TrieNode {
    /** Outgoing edges keyed by character. */
    final Map<Character, TrieNode> children = new HashMap<>();
    /** Product ids matching the prefix/path represented by this node. */
    final Set<String> productIds = new HashSet<>();
}
