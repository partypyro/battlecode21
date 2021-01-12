package examplefuncsplayer;

import battlecode.common.MapLocation;

import java.util.Map;

public class Communication {
    MapLocation location;
    byte data;
    int turns;

    Communication(MapLocation location, byte data, int turns) {
        this.location = location;
        this.data = data;
        this.turns = turns;
    }
}
