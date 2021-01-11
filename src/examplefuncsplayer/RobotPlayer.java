package examplefuncsplayer;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        RobotPlayer.rc = rc;
        Controller c;
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER: c = new EnlightenmentCenter(rc); break;
            case POLITICIAN:           c = new Politician(rc);          break;
            case SLANDERER:            c = new Slanderer(rc);           break;
            case MUCKRAKER:            c = new Muckraker(rc);           break;
            default:
                throw new IllegalStateException("Unexpected value: " + rc.getType());
        }

        RobotType prev_robot_type =  rc.getType();

        while (true) {
            try {
                if (prev_robot_type != rc.getType()){
                    switch (rc.getType()) {
                        case ENLIGHTENMENT_CENTER: c = new EnlightenmentCenter(rc); break;
                        case POLITICIAN:           c = new Politician(rc);          break;
                        case SLANDERER:            c = new Slanderer(rc);           break;
                        case MUCKRAKER:            c = new Muckraker(rc);           break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + rc.getType());
                    }
                }
                c.turnCount++;
                c.readSensors();
                c.run();

                prev_robot_type = rc.getType();

                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
