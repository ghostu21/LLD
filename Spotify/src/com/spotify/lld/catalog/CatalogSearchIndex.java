package com.spotify.lld.catalog;

import java.util.Collections;
import java.util.Set;

public class CatalogSearchIndex {
    private final TrieNode root = new TrieNode();

    /** Index every suffix so partial-match search works. */
    public void insert(String text, String trackId) {
        String lower = text.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        for (int i = 0; i < lower.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
                node.trackIds.add(trackId);
            }
        }
    }

    public Set<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptySet();
        }
        return Collections.unmodifiableSet(node.trackIds);
    }
}
