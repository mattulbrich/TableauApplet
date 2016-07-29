/* This file is part of TableauApplet.
 *
 * It has been written by Mattias Ulbrich <ulbrich@kit.edu>, 
 * Karlsruhe Institute of Technology, Germany.
 *
 * TableauApplet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TableauApplet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TableauApplet.  If not, see <http://www.gnu.org/licenses/>.
 */

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
