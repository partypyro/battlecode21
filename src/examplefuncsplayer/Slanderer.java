package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Slanderer extends Controller {

    Slanderer(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        MapLocation safetyLocation = getLocation(EC_ID);
        byte flag = getData(EC_ID);

        if (flag == Flags.SLANDERER_SAFETY) {
            setDestination(safetyLocation);
        }

        if (destination != null) {
            moveToDestination();
        } else {
            orbit();
        }
    }
}
