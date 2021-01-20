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
        // FOR ORPHANS (recently converted units) search for friendly EC to be parent
        if (EC_ID == 0) {
            EC_LOC = curLocation;
            explore();

            for (RobotInfo r : allInfo) {
                if (r.type == RobotType.ENLIGHTENMENT_CENTER && r.team == FRIENDLY) {
                    EC_ID = r.ID;
                    EC_LOC = r.location;
                }
            }
        }
        // explore/move/actions
        else {

            if (check_ec_flag() || destination != null){
                moveToDestination();
            }
            else{
                if (rc.getID() % 4 == 0){
                    orbit();
                }
                else explore();
            }
            for (RobotInfo r : allInfo) {
                if (r.team == NEUTRAL || r.team == ENEMY) {
                    empowerIfCan(ACTION_RADIUS_SQ);
                    break;
                }
                if (rc.getEmpowerFactor(rc.getTeam(), 0) >= 2 && r.team == FRIENDLY && r.getType() == RobotType.ENLIGHTENMENT_CENTER){
                    empowerIfCan(ACTION_RADIUS_SQ);
                    break;
                }
            }
        }
    }

    boolean check_ec_flag(){
        //return true if destination, false to explore randomly
        //current destinations: Neutral_EC, Enemy_EC
        if (getData(EC_ID) == Flags.NONE){
            return false;
        }
        else if (getData(EC_ID) == Flags.ENEMY_EC_FOUND || getData(EC_ID) == Flags.NEUTRAL_EC_FOUND) {
            setDestination(getLocation(EC_ID));
            return true;
        }
        return false;
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
