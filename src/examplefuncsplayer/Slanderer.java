package examplefuncsplayer;

import battlecode.common.*;

public class Slanderer extends Controller {

    Slanderer(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        MapLocation safetyLocation = getLocation(EC_ID);
        byte flag = getData(EC_ID);

        for (RobotInfo r : allInfo) {
            if (r.team == ENEMY) {
                tryMove(r.getLocation().directionTo(rc.getLocation()));
            }
        }
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
