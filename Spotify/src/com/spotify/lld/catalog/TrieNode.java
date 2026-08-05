package com.spotify.lld.catalog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrieNode {
    final Map<Character, TrieNode> children = new HashMap<>();
    final Set<String> trackIds = new HashSet<>();
}
