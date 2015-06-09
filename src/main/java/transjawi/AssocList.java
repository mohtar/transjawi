////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// A backing store for children nodes of Trie. Elements are inserted and looked
// up using binary search algorithm. This data structure should be compact and
// fast for small number of elements.
public class AssocList<K, V> {

    ArrayList<K> keys;
    ArrayList<V> values;

    public AssocList() {
        keys = new ArrayList();
        values = new ArrayList();
    }

    public void set(K key, V value) {
        int i = Arrays.binarySearch(keys.toArray(), key);
        if (i >= 0) {
            values.set(i, value);
        } else {
            keys.add(-(i) - 1, key);
            values.add(-(i) - 1, value);
        }
    }

    // Returns null if key does not exist.
    public V get(K key) {
        int i = Arrays.binarySearch(keys.toArray(), key);
        if (i >= 0) {
            return values.get(i);
        } else {
            return null;
        }
    }

    public boolean remove(K key) {
        int i = Arrays.binarySearch(keys.toArray(), key);
        if (i >= 0) {
            keys.remove(i);
            values.remove(i);
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return keys.size();
    }

    public List<K> keys() {
        return keys;
    }

    public List<V> values() {
        return values;
    }
}
