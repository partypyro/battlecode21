package examplefuncsplayer;

import battlecode.common.*;

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
    RobotInfo[][] mapKnowledge;

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
        mapKnowledge = new RobotInfo[SENSOR_RADIUS * 2 + 1][SENSOR_RADIUS * 2 + 1];
    }

    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir) && rc.isReady()) {
            rc.move(dir);
            return true;
        } else return false;
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
        mapKnowledge = new RobotInfo[SENSOR_RADIUS * 2 + 1][SENSOR_RADIUS * 2 + 1];

        for (RobotInfo r : allInfo) {
            int dX = r.getLocation().x - curLocation.x;
            int dY = r.getLocation().y - curLocation.y;
            mapKnowledge[SENSOR_RADIUS + dX][SENSOR_RADIUS + dY] = r;
        }
    }

    public abstract void run() throws GameActionException;
}