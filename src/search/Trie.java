package search;

import datastructures.CustomList;
import datastructures.IntList;

/**
 * Manually implemented Trie (Prefix Tree) data structure.
 * Supports arbitrary Unicode characters (English, Telugu, Hindi, Tamil).
 *
 * Design:
 * - Does NOT use java.util.Map or HashMap.
 * - Each TrieNode maintains an expandable array of (character, childNode) branches.
 * - Directly supports prefix searches, exact word lookups, and autocomplete suggestions.
 *
 * Time Complexity:
 * - Insertion:         O(m) where m is length of the word
 * - Search:            O(m)
 * - Prefix Search:     O(m + k) where k is total characters in returned suggestions
 *
 * Space Complexity:
 * - O(N * Sigma) where N is total nodes and Sigma is branch fanout.
 */
public class Trie {

    public static class TrieNode {
        private char[] keys;
        private TrieNode[] children;
        private int childCount;

        private boolean isEndOfWord;
        private String word;
        private final IntList documentIds;

        public TrieNode() {
            this.keys = new char[4];
            this.children = new TrieNode[4];
            this.childCount = 0;
            this.isEndOfWord = false;
            this.word = null;
            this.documentIds = new IntList();
        }

        public TrieNode getChild(char c) {
            for (int i = 0; i < childCount; i++) {
                if (keys[i] == c) {
                    return children[i];
                }
            }
            return null;
        }

        public TrieNode addChild(char c) {
            TrieNode existing = getChild(c);
            if (existing != null) {
                return existing;
            }

            if (childCount >= keys.length) {
                int newCap = keys.length * 2;
                char[] newKeys = new char[newCap];
                TrieNode[] newChildren = new TrieNode[newCap];
                for (int i = 0; i < childCount; i++) {
                    newKeys[i] = keys[i];
                    newChildren[i] = children[i];
                }
                keys = newKeys;
                children = newChildren;
            }

            TrieNode newNode = new TrieNode();
            keys[childCount] = c;
            children[childCount] = newNode;
            childCount++;
            return newNode;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public void setEndOfWord(boolean endOfWord) {
            isEndOfWord = endOfWord;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public IntList getDocumentIds() {
            return documentIds;
        }

        public void addDocumentId(int docId) {
            if (!documentIds.contains(docId)) {
                documentIds.add(docId);
            }
        }

        public int getChildCount() {
            return childCount;
        }

        public TrieNode getChildAt(int index) {
            return children[index];
        }
    }

    private final TrieNode root;
    private int totalWordCount;

    public Trie() {
        this.root = new TrieNode();
        this.totalWordCount = 0;
    }

    /**
     * Inserts a word along with its source document ID into the Trie.
     */
    public void insert(String word, int docId) {
        if (word == null || word.length() == 0) {
            return;
        }

        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            current = current.addChild(c);
        }

        if (!current.isEndOfWord()) {
            current.setEndOfWord(true);
            current.setWord(word);
            totalWordCount++;
        }
        current.addDocumentId(docId);
    }

    /**
     * Searches whether an exact word exists in the Trie.
     */
    public boolean search(String word) {
        if (word == null || word.length() == 0) {
            return false;
        }
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord();
    }

    /**
     * Checks if any word starts with the given prefix.
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return false;
        }
        return findNode(prefix) != null;
    }

    /**
     * Finds and returns all words starting with the specified prefix (Autocomplete).
     */
    public CustomList<String> getWordsWithPrefix(String prefix) {
        CustomList<String> results = new CustomList<>();
        if (prefix == null || prefix.length() == 0) {
            return results;
        }

        TrieNode prefixNode = findNode(prefix);
        if (prefixNode == null) {
            return results;
        }

        collectWords(prefixNode, results);
        return results;
    }

    /**
     * Returns all unique Document IDs containing words starting with the given prefix.
     */
    public IntList getDocumentIdsWithPrefix(String prefix) {
        IntList docIds = new IntList();
        if (prefix == null || prefix.length() == 0) {
            return docIds;
        }

        TrieNode prefixNode = findNode(prefix);
        if (prefixNode == null) {
            return docIds;
        }

        collectDocumentIds(prefixNode, docIds);
        return docIds;
    }

    private TrieNode findNode(String str) {
        TrieNode current = root;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            current = current.getChild(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private void collectWords(TrieNode node, CustomList<String> list) {
        if (node == null) {
            return;
        }
        if (node.isEndOfWord() && node.getWord() != null) {
            list.add(node.getWord());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectWords(node.getChildAt(i), list);
        }
    }

    private void collectDocumentIds(TrieNode node, IntList list) {
        if (node == null) {
            return;
        }
        if (node.isEndOfWord()) {
            IntList nodeDocs = node.getDocumentIds();
            for (int i = 0; i < nodeDocs.size(); i++) {
                int id = nodeDocs.get(i);
                if (!list.contains(id)) {
                    list.add(id);
                }
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectDocumentIds(node.getChildAt(i), list);
        }
    }

    public int getTotalWordCount() {
        return totalWordCount;
    }
}
