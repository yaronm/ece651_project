package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A collection of static utility methods for interacting with firebase.
 */
public class FirebaseUtils {

    /**
     * An auxiliary function to convert a set to a map for storage as a JSON object.
     *
     * @param set   an arbitrary set
     * @param value a singular value
     * @param <K>   the type of the set's elements
     * @param <V>   the type of the given value
     * @return a map where each element of the set is a key, mapped to the given value
     */
    public static <K, V> Map<K, V> setToMap(Set<K> set, V value) {
        Map<K, V> map = new HashMap<>(set.size());
        for (K t : set) {
            map.put(t, value);
        }
        return map;
    }

}
