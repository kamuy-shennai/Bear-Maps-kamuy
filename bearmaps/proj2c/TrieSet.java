package bearmaps.proj2c;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TrieSet
 * 字典树
 */
public class TrieSet {
    private Node root;

    private static class Node {
        private char ch;
        private boolean isKey;
        private final HashMap<Character, Node> map;

        public Node() {
            map = new HashMap<>();
        }

        public Node(char c, boolean b) {
            ch = c;
            isKey = b;
            map = new HashMap<>();
        }
    }

    public TrieSet() {
        root = new Node();
    }

    public void add(String str) {
        if (str == null || str.length() < 1) {
            return;
        }
        Node current = root;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!current.map.containsKey(c)) {
                current.map.put(c, new Node(c, false));
            }
            current = current.map.get(c);
        }
        current.isKey = true;
    }

    public boolean contains(String key) {
        boolean isFound = true; // assume true
        Node current = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (current.map.containsKey(c)) {
                current = current.map.get(c);
                isFound = current.isKey;
            } else {
                isFound = false;
                break;
            }
        }
        return isFound;
    }

    public void clear() {
        root = null;
        root = new Node();
    }

    public List<String> keysWithPrefix(String prefix) {
        List<String> x = new LinkedList<>();
        Node current = root;
        // Test if has prefix
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (current.map.containsKey(c)) {
                current = current.map.get(c);
            }
        }

        if (current.isKey) {
            x.add(prefix);
        }
        Set<Character> keys = current.map.keySet();
        for (char c : keys) {
            keyPrefixHelper(current.map.get(c), x, new StringBuilder(prefix));
        }
        return x;
    }

    private void keyPrefixHelper(Node n, List<String> mylist, StringBuilder prefix) {
        if (n.isKey) {
            mylist.add(prefix.append(n.ch).toString());
            prefix.deleteCharAt(prefix.length() - 1);
        }

        Set<Character> keys = n.map.keySet();
        for (char c : keys) {
            keyPrefixHelper(n.map.get(c), mylist, new StringBuilder(prefix.append(n.ch)));
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    public String longestPrefixOf(String key) {
        throw new UnsupportedOperationException();
    }


}
