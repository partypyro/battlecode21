package examplefuncsplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.awt.*;

public class Politician extends Controller {

    Politician(RobotController rc) {
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

            //tryMove(randomDirection());
            if (check_ec_flag()){
                moveToDestination();
            }
            else{
                explore();
            }
            if (destination != null
                    && curLocation.isWithinDistanceSquared(destination, ACTION_RADIUS_SQ)) {
                for (RobotInfo r : allInfo) {
                    if (r.team == NEUTRAL || r.team == ENEMY) {
                        empowerIfCan(ACTION_RADIUS_SQ);
                        break;
                    }
                }
            }
        }
    }

    boolean check_ec_flag(){
        //return true if destination, false to explore randomly
        if (getData(EC_ID) == Flags.NONE){
            return false;
        }
        else {
            setDestination(getLocation(EC_ID));
            return true;
        }
    }

    boolean empowerIfCan(int radius) {
        try {
            if (rc.canEmpower(radius)) {
                rc.empower(radius);
                return true;
            }
        } catch(GameActionException ignored) {

        } return false;
    }
}
