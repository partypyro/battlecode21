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
        for (RobotInfo r : allInfo){
            if (r.team == ENEMY && r.getType() == RobotType.SLANDERER){
                if(rc.canExpose(r.getLocation())){
                    rc.expose(r.getLocation());
                }
            }
        }
        explore();
    }
}
