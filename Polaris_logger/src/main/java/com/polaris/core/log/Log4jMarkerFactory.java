package com.polaris.core.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.status.StatusLogger;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * Log4j/SLF4J bridge to create SLF4J Markers based on name or based on existing SLF4J Markers.
 */
public class Log4jMarkerFactory extends BasicMarkerFactory {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private final ConcurrentMap<String, Marker> markerMap = new ConcurrentHashMap<>();

    /**
     * Returns a Log4j Marker that is compatible with SLF4J.
     * @param name The name of the Marker.
     * @return A Marker.
     */
    @Override
    public Marker getMarker(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        final Marker marker = markerMap.get(name);
        if (marker != null) {
            return marker;
        }
        final org.apache.logging.log4j.Marker log4jMarker = MarkerManager.getMarker(name);
        return addMarkerIfAbsent(name, log4jMarker);
    }

    private Marker addMarkerIfAbsent(final String name, final org.apache.logging.log4j.Marker log4jMarker) {
        final Marker marker = new Log4jMarker(log4jMarker);
        final Marker existing = markerMap.putIfAbsent(name, marker);
        return existing == null ? marker : existing;
    }

    /**
     * Returns a Log4j Marker converted from an existing custom SLF4J Marker.
     * @param marker The SLF4J Marker to convert.
     * @return A converted Log4j/SLF4J Marker.
     * @since 2.1
     */
    public Marker getMarker(final Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        final Marker m = markerMap.get(marker.getName());
        if (m != null) {
            return m;
        }
        return addMarkerIfAbsent(marker.getName(), convertMarker(marker));
    }

    private static org.apache.logging.log4j.Marker convertMarker(final Marker original) {
        if (original == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        return convertMarker(original, new ArrayList<Marker>());
    }

    private static org.apache.logging.log4j.Marker convertMarker(final Marker original,
                                                                 final Collection<Marker> visited) {
        final org.apache.logging.log4j.Marker marker = MarkerManager.getMarker(original.getName());
        if (original.hasReferences()) {
            final Iterator<Marker> it = original.iterator();
            while (it.hasNext()) {
                final Marker next = it.next();
                if (visited.contains(next)) {
                    LOGGER.warn("Found a cycle in Marker [{}]. Cycle will be broken.", next.getName());
                } else {
                    visited.add(next);
                    marker.addParents(convertMarker(next, visited));
                }
            }
        }
        return marker;
    }

    /**
     * Returns true if the Marker exists.
     * @param name The Marker name.
     * @return {@code true} if the Marker exists, {@code false} otherwise.
     */
    @Override
    public boolean exists(final String name) {
        return markerMap.containsKey(name);
    }

    /**
     * Log4j does not support detached Markers. This method always returns false.
     * @param name The Marker name.
     * @return {@code false}
     */
    @Override
    public boolean detachMarker(final String name) {
        return false;
    }

    /**
     * Log4j does not support detached Markers for performance reasons. The returned Marker is attached.
     * @param name The Marker name.
     * @return The named Marker (unmodified).
     */
    @Override
    public Marker getDetachedMarker(final String name) {
        LOGGER.warn("Log4j does not support detached Markers. Returned Marker [{}] will be unchanged.", name);
        return getMarker(name);
    }


}
