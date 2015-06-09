////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.util.*;

public class Trie<T> {

    AssocList<Character, Trie<T>> children;
    boolean end;
    T value;

    public Trie() {
        children = new AssocList();
        end = false;
        value = null;
    }

    public void set(String key, T value) {
        Trie<T> trie = this;
        for (int i = 0; i < key.length(); i++) {
            Trie<T> subtrie = trie.children.get(key.charAt(i));
            if (subtrie == null) {
                subtrie = new Trie<T>();
                trie.children.set(key.charAt(i), subtrie);
            }
            trie = subtrie;
        }
        trie.end = true;
        trie.value = value;
    }

    // Returns null if key does not exist.
    public T get(String key) {
        Trie<T> trie = this;
        for (int i = 0; i < key.length(); i++) {
            trie = trie.children.get(key.charAt(i));
            if (trie == null) {
                return null;
            }
        }
        return trie.value;
    }

    public List<Integer> walk(String source) {
        return walk(source, 0);
    }

    public List<Integer> walk(String source, int offset) {
        Trie<T> trie = this;
        List<Integer> prefixes = new ArrayList<Integer>();
        int i = 0;
        if (trie.end) {
            prefixes.add(i + 1);
        }
        while (offset + i < source.length()) {
            trie = trie.children.get(source.charAt(offset + i));
            if (trie == null) {
                break;
            }
            if (trie.end) {
                prefixes.add(i + 1);
            }
            i++;
        }
        return prefixes;
    }

    // Checks if the trie contains the given key.
    public boolean contains(String key) {
        Trie<T> trie = this;
        for (int i = 0; i < key.length(); i++) {
            trie = trie.children.get(key.charAt(i));
            if (trie == null) {
                return false;
            }
        }
        return trie.end;
    }

    // Returns true on success.
    public boolean remove(String key) {
        Trie<T> trie = this;
        for (int i = 0; i < key.length(); i++) {
            trie = trie.children.get(key.charAt(i));
            if (trie == null) {
                return false;
            }
        }
        trie.end = false;
        trie.value = null;
        return true;
    }

    // Returns the size of the trie.
    public int size() {
        int size = 0;
        if (this.value != null) {
            size++;
        }
        for (Trie<T> child : this.children.values()) {
            size += child.size();
        }
        return size;
    }
}
