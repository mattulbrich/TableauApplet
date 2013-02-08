package de.uka.ilkd.tablet;

import java.util.ArrayList;
import java.util.List;

public class StackMap<K, V> {
    
    private List<K> keys = new ArrayList<K>();
    private List<V> values = new ArrayList<V>();

    public void clear() {
        keys.clear();
        values.clear();
    }

    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    public V get(Object key) {
        for (int i = keys.size()-1; i >= 0; i--) {
            if(key.equals(keys.get(i)))
                return values.get(i);
        }
        return null;
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public void push(K key, V value) {
        keys.add(key);
        values.add(value);
    }

    public int size() {
        return keys.size();
    }
    
    public void pop() {
        int s = size() - 1;
        keys.remove(s);
        values.remove(s);
    }

}
