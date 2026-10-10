package search;

import datastructures.CustomList;
import datastructures.CustomQueue;

/**
 * Aho-Corasick Multi-Pattern String Matching Algorithm.
 *
 * How it works:
 * 1. Trie Construction (Goto function):
 *    All target patterns are inserted into a Trie.
 *
 * 2. Failure Links Construction (Failure function):
 *    Using Breadth-First Search (BFS) with a queue, failure links are established.
 *    If the current state cannot transition on character 'c', the failure link points
 *    to the longest proper suffix of the current prefix that exists as a prefix in the Trie.
 *
 * 3. Output Links (Match collection):
 *    Each node inherits output matches from its failure state, ensuring overlapping
 *    and nested patterns (e.g. "Hyderabad" and "bad") are simultaneously discovered.
 *
 * 4. Text Traversal:
 *    Text is traversed linearly in a single pass O(n). At each character, transitions
 *    follow Trie edges or fallback along failure links.
 *
 * Characteristics:
 * - Simultaneous search for multiple keywords in a single linear pass.
 * - Essential for multi-keyword retrieval and document indexing.
 *
 * Time Complexity:
 * - Preprocessing (Trie + BFS Failures): O(sum of pattern lengths)
 * - Text Traversal:                     O(n + z) where z is match count
 *
 * Space Complexity:
 * - O(sum of pattern lengths) auxiliary space.
 */
public class AhoCorasick {

    public static class PatternMatch {
        private final String pattern;
        private final int startIndex;
        private final int endIndex;

        public PatternMatch(String pattern, int startIndex, int endIndex) {
            this.pattern = pattern;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getPattern() {
            return pattern;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        @Override
        public String toString() {
            return "'" + pattern + "' at [" + startIndex + ".." + endIndex + "]";
        }
    }

    public static class ACNode {
        private char[] keys;
        private ACNode[] children;
        private int childCount;

        private ACNode fail;
        private final CustomList<String> output;

        public ACNode() {
            this.keys = new char[4];
            this.children = new ACNode[4];
            this.childCount = 0;
            this.fail = null;
            this.output = new CustomList<>();
        }

        public ACNode getChild(char c) {
            for (int i = 0; i < childCount; i++) {
                if (keys[i] == c) {
                    return children[i];
                }
            }
            return null;
        }

        public ACNode addChild(char c) {
            ACNode existing = getChild(c);
            if (existing != null) {
                return existing;
            }

            if (childCount >= keys.length) {
                int newCap = keys.length * 2;
                char[] newKeys = new char[newCap];
                ACNode[] newChildren = new ACNode[newCap];
                for (int i = 0; i < childCount; i++) {
                    newKeys[i] = keys[i];
                    newChildren[i] = children[i];
                }
                keys = newKeys;
                children = newChildren;
            }

            ACNode newNode = new ACNode();
            keys[childCount] = c;
            children[childCount] = newNode;
            childCount++;
            return newNode;
        }

        public int getChildCount() {
            return childCount;
        }

        public ACNode getChildAt(int index) {
            return children[index];
        }

        public char getKeyAt(int index) {
            return keys[index];
        }

        public CustomList<String> getOutput() {
            return output;
        }
    }

    private final ACNode root;
    private final CustomList<String> patterns;
    private boolean isBuilt;

    public AhoCorasick() {
        this.root = new ACNode();
        this.patterns = new CustomList<>();
        this.isBuilt = false;
    }

    /**
     * Adds a search pattern to the Aho-Corasick automaton.
     */
    public void addPattern(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return;
        }
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
            isBuilt = false;
        }
    }

    /**
     * Compiles the Trie and constructs the BFS failure and output links.
     */
    public void buildAutomaton() {
        // Step 1: Insert all patterns into Trie
        for (int p = 0; p < patterns.size(); p++) {
            String pat = patterns.get(p);
            ACNode current = root;
            for (int i = 0; i < pat.length(); i++) {
                char c = pat.charAt(i);
                current = current.addChild(c);
            }
            current.output.add(pat);
        }

        // Step 2: Build failure links using BFS with CustomQueue
        CustomQueue<ACNode> queue = new CustomQueue<>();

        // Level 1: Root's direct children fail back to root
        for (int i = 0; i < root.getChildCount(); i++) {
            ACNode child = root.getChildAt(i);
            child.fail = root;
            queue.enqueue(child);
        }

        // BFS traversal
        while (!queue.isEmpty()) {
            ACNode curr = queue.dequeue();

            for (int i = 0; i < curr.getChildCount(); i++) {
                char c = curr.getKeyAt(i);
                ACNode child = curr.getChildAt(i);

                // Find failure state for child
                ACNode f = curr.fail;
                while (f != null && f.getChild(c) == null && f != root) {
                    f = f.fail;
                }

                if (f != null && f.getChild(c) != null) {
                    child.fail = f.getChild(c);
                } else {
                    child.fail = root;
                }

                // Inherit outputs from failure state
                if (child.fail != null) {
                    CustomList<String> failOutput = child.fail.getOutput();
                    for (int o = 0; o < failOutput.size(); o++) {
                        String matchedPat = failOutput.get(o);
                        if (!child.output.contains(matchedPat)) {
                            child.output.add(matchedPat);
                        }
                    }
                }

                queue.enqueue(child);
            }
        }

        isBuilt = true;
    }

    /**
     * Searches the text for all occurrences of all registered patterns simultaneously.
     *
     * @param text The document text to scan
     * @return List of PatternMatch occurrences
     */
    public CustomList<PatternMatch> search(String text) {
        if (!isBuilt) {
            buildAutomaton();
        }

        CustomList<PatternMatch> matches = new CustomList<>();
        if (text == null || text.length() == 0) {
            return matches;
        }

        ACNode current = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Follow failure links if there is no edge for char c
            while (current != root && current.getChild(c) == null) {
                current = current.fail;
            }

            ACNode next = current.getChild(c);
            if (next != null) {
                current = next;
            } else {
                current = root;
            }

            // Check if current state emits any matched patterns
            if (current.output.size() > 0) {
                for (int m = 0; m < current.output.size(); m++) {
                    String pat = current.output.get(m);
                    int startIdx = i - pat.length() + 1;
                    matches.add(new PatternMatch(pat, startIdx, i));
                }
            }
        }

        return matches;
    }

    public CustomList<String> getPatterns() {
        return patterns;
    }
}
