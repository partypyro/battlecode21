package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Politician extends Controller {

    Politician(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {

        //tryMove(randomDirection());
        explore();
        for (RobotInfo r : allInfo){
            if (r.team == NEUTRAL){
                if(rc.canEmpower(ACTION_RADIUS_SQ)){
                    rc.empower(ACTION_RADIUS_SQ);
                }
            }
        }



    }
}
