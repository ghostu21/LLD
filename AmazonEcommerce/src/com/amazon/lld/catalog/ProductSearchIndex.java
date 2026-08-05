package com.amazon.lld.catalog;

import java.util.Collections;
import java.util.Set;

/**
 * In-memory Trie index for product names (suffix trie).
 * <p>
 * Why: avoid O(n) linear scans; prefix/partial queries should be ~O(k) in
 * query length, independent of catalog size.
 * <p>
 * Logic: {@link #insert} indexes every suffix of the normalized name so
 * substrings like "phone" still hit "Smartphone". Each Trie node accumulates
 * matching product ids. {@link #search} walks the query path and returns the
 * ids stored on the terminal node.
 */
public class ProductSearchIndex {
    private final TrieNode root = new TrieNode();

    /**
     * Indexes {@code text} so any substring query can find {@code productId}.
     * <p>
     * Logic:
     * <ol>
     *   <li>Normalize to lowercase alphanumeric + spaces</li>
     *   <li>For each suffix start index i, walk characters i..end</li>
     *   <li>Create child nodes as needed and add productId on every node along the path</li>
     * </ol>
     */
    public void insert(String text, String productId) {
        String lower = text.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        for (int i = 0; i < lower.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
                node.productIds.add(productId);
            }
        }
    }

    /**
     * Finds all product ids whose indexed name contains {@code prefix} as a path.
     * <p>
     * Logic: walk from root following each query char; if any edge is missing,
     * return empty; otherwise return the productIds collected on that node.
     */
    public Set<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptySet();
        }
        return Collections.unmodifiableSet(node.productIds);
    }
}
