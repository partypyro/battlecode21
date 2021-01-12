package examplefuncsplayer;

import battlecode.common.*;
import java.lang.Math;

public abstract class Controller {
    final RobotController rc;

    // Robot Constants
    final Team FRIENDLY;
    final Team ENEMY;
    final Team NEUTRAL;
    final int SENSOR_RADIUS;
    final int ACTION_RADIUS;
    final int SENSOR_RADIUS_SQ;
    final int ACTION_RADIUS_SQ;

    // Robot Data
    public int turnCount;
    MapLocation curLocation;
    RobotInfo[] allInfo;

    // Path Finding
    MapLocation destination;
    int minDist;
    boolean onWall;

    Controller(RobotController rc) {
        this.rc = rc;

        // Initialize constants
        FRIENDLY = rc.getTeam();
        ENEMY = rc.getTeam().opponent();
        NEUTRAL = Team.NEUTRAL;
        SENSOR_RADIUS_SQ = rc.getType().sensorRadiusSquared;
        ACTION_RADIUS_SQ = rc.getType().actionRadiusSquared;
        SENSOR_RADIUS = (int) Math.ceil(Math.sqrt(SENSOR_RADIUS_SQ));
        ACTION_RADIUS = (int) Math.ceil(Math.sqrt(ACTION_RADIUS_SQ));

        // Initialize info variables
        turnCount = 0;
        curLocation = rc.getLocation();
    }

    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir) && rc.isReady()) {
            rc.move(dir);
            return true;
        } else return false;
    }

    Direction randomDirection() throws GameActionException{

        int num =  (int)(Math.random() * (7 + 1));
        switch (num){
            case 0: return Direction.NORTH;
            case 1: return Direction.NORTHWEST;
            case 2: return Direction.WEST;
            case 3: return Direction.SOUTHWEST;
            case 4: return Direction.SOUTH;
            case 5: return Direction.SOUTHEAST;
            case 6: return Direction.EAST;
            case 7: return Direction.NORTHEAST;
        }
        return Direction.NORTH;
    }

    boolean moveTo(MapLocation loc) throws GameActionException {
        boolean isLocReached = rc.getLocation().equals(loc);

        if (!isLocReached) {
            Direction dir = rc.getLocation().directionTo(loc);

            if (!tryMove(dir)) {
                tryMove(dir.rotateLeft());
            }

            isLocReached = rc.getLocation().equals(loc);
        }

        return isLocReached;
    }

    void readSensors() {
        curLocation = rc.getLocation();
        allInfo = rc.senseNearbyRobots(-1);
    }

    void setDestination(MapLocation destination) {
        this.destination = destination;
        this.minDist = Integer.MAX_VALUE;
        this.onWall = false;
    }

    void moveToDestination() throws GameActionException {
        boolean isLocReached = curLocation.equals(destination);

        if (!isLocReached && rc.isReady()) {
            Direction dir = curLocation.directionTo(destination);
            int curDist = curLocation.distanceSquaredTo(destination);

            if (onWall) {
                if (curDist <= minDist && rc.canMove(dir)) {
                    onWall = false;
                } else {
                    if (rc.canMove(dir.rotateLeft()))
                        rc.move(dir.rotateLeft());
                }
            } else {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                } else {
                    onWall = true;
                }
            }
            minDist = Math.min(curDist, minDist);
        }
    }

    public abstract void run() throws GameActionException;
}