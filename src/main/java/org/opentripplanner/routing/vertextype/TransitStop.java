package org.opentripplanner.routing.vertextype;

import org.opentripplanner.model.Stop;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.PathwayEdge;
import org.opentripplanner.routing.edgetype.TransferEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransitStop extends TransitStationStop {

    private static final Logger LOG = LoggerFactory.getLogger(TransitStop.class);

    // Do we actually need a set of modes for each stop?
    // It's nice to have for the index web API but can be generated on demand.
    private TraverseModeSet modes = new TraverseModeSet();

    private static final long serialVersionUID = 1L;

    private boolean wheelchairEntrance;

    private boolean isEntrance;

    /**
     * For stops that are deep underground, there is a time cost to entering and exiting the stop;
     * all stops are assumed to be at street level unless we have configuration to the contrary
     */
    private int streetToStopTime = 0;

    /*
      We sometimes want a reference to a TransitStop's corresponding arrive or depart vertex.
      Rather than making a Map in the GraphIndex, we just store them here.
      This should also help make the GTFS-loading context object unnecessary, and eventually help
      eliminate explicit transit edges.
    */
    public TransitStopArrive arriveVertex;
    public TransitStopDepart departVertex;

    public TransitStop(Graph graph, Stop stop) {
        super(graph, stop);
        this.wheelchairEntrance = stop.getWheelchairBoarding() != 2;
        isEntrance = stop.getLocationType() == 2;
        //Adds this vertex into graph envelope so that we don't need to loop over all vertices
        graph.expandToInclude(stop.getLon(), stop.getLat());
    }

    public boolean hasWheelchairEntrance() {
        return wheelchairEntrance;
    }

    public boolean isEntrance() {
        return isEntrance;
    }

    public boolean hasEntrances() {
        for (Edge e : this.getOutgoing()) {
            if (e instanceof PathwayEdge) {
                return true;
            }
        }
        for (Edge e : this.getIncoming()) {
            if (e instanceof PathwayEdge) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGtfsTransfers() {
        for (Edge e : this.getOutgoing()) {
            if (e instanceof TransferEdge || e instanceof PathwayEdge) {
                return true;
            }
        }
        return false;
     }

    public int getStreetToStopTime() {
        return streetToStopTime;
    }

    public void setStreetToStopTime(int streetToStopTime) {
        this.streetToStopTime = streetToStopTime;
        LOG.debug("Stop {} access time from street level set to {}", this, streetToStopTime);
    }

    public TraverseModeSet getModes() {
        return modes;
    }

    public void addMode(TraverseMode mode) {
        modes.setMode(mode, true);
    }
    
    public boolean isStreetLinkable() {
        return isEntrance() || !hasEntrances();
    }
}
