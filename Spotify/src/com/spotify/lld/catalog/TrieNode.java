package com.spotify.lld.catalog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Single node in the catalog search Trie.
 * <p>
 * Why: Trie edges are characters; nodes hold which track ids are reachable
 * through the path from the root to this node.
 * <p>
 * Logic: {@code children} maps next character → child node;
 * {@code trackIds} are all songs whose indexed text passes through this node
 * (populated during suffix insert).
 */
public class TrieNode {
    /** Outgoing edges keyed by character. */
    final Map<Character, TrieNode> children = new HashMap<>();
    /** Track ids matching the prefix/path represented by this node. */
    final Set<String> trackIds = new HashSet<>();
}
