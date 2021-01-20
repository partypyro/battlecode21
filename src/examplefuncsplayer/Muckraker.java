package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Muckraker extends Controller {


    Muckraker(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        if (EC_ID == 0) {
            EC_LOC = curLocation;
            explore();

            for (RobotInfo r : allInfo) {
                if (r.type == RobotType.ENLIGHTENMENT_CENTER && r.team == FRIENDLY) {
                    EC_ID = r.ID;
                    EC_LOC = r.location;
                }
            }
        } else {
            for (RobotInfo r : allInfo){
                if (r.team == ENEMY && r.getType() == RobotType.SLANDERER){
                    if(rc.canExpose(r.getLocation())){
                        rc.expose(r.getLocation());
                    }
                }
                if (r.team == ENEMY && r.getType() == RobotType.ENLIGHTENMENT_CENTER){
                    if (rc.isReady() && !tryMove(rc.getLocation().directionTo(r.getLocation()))){
                        tryMove(rc.getLocation().directionTo(r.getLocation()).rotateLeft());
                    }
                }
            }
            explore();
        }
    }
}
