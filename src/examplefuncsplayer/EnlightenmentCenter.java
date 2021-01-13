package examplefuncsplayer;

import battlecode.common.*;
import java.util.HashSet;

public class EnlightenmentCenter extends Controller {

    EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

    int prev_vote_count = 0;
    double bid_percent = 0.01;

    HashSet<Integer> children = new HashSet<Integer>();

    @Override
    public void run() throws GameActionException {

        // setup, build 4 muckrakers
        if (turnCount == 1){
            if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.NORTH, 1)){
                rc.buildRobot(RobotType.MUCKRAKER, Direction.NORTH, 1);
            }
        }
        if (turnCount == 3){
            if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.SOUTH, 1)){
                rc.buildRobot(RobotType.MUCKRAKER, Direction.SOUTH, 1);
            }
        }
        if (turnCount == 5){
            if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.EAST, 1)){
                rc.buildRobot(RobotType.MUCKRAKER, Direction.EAST, 1);
            }
        }
        if (turnCount == 7){
            if (rc.canBuildRobot(RobotType.MUCKRAKER, Direction.WEST, 1)){
                rc.buildRobot(RobotType.MUCKRAKER, Direction.WEST, 1);
            }
        }

        // farming, spawn slanderer every 20 rounds with .2 of total influence

        if (turnCount > 7 && turnCount%20 == 0){
            for (Direction dir : Direction.allDirections()){
                if (rc.canBuildRobot(RobotType.SLANDERER, dir, (int)(rc.getInfluence() * .2))){
                    rc.buildRobot(RobotType.SLANDERER, dir, (int)(rc.getInfluence() * .2));
                }
            }
        }

        /// FOR ACTIONS IN EACH TURN
        //bid
        System.out.println(turnCount);
        if (rc.canBid((int)(rc.getInfluence() * bid_percent))){
            rc.bid((int)(rc.getInfluence() * bid_percent));
        }

        //scan for new children
        for (RobotInfo r : allInfo){
            if (!children.contains(r.getID())){
                children.add(r.getID());
            }
        }

        //scan for neutral enlightenment center flags, broadcast if found
        for (int i: children){
            if (rc.canGetFlag(i)){
                int flag  = rc.getFlag(i);
                System.out.println("Flag! :" + flag);
                System.out.println("Data :" + getData(i));
                if (getData(i) == Flags.NEUTRAL_EC_FOUND){
                    System.out.println("Found Enlightenment Center!" + rc.getFlag(i));
                    queueCommunication(getLocation(i), Flags.NEUTRAL_EC_FOUND, 50);
                }
            }
        }
        prev_vote_count = rc.getTeamVotes();
    }


}
