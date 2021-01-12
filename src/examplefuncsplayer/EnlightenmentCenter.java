package examplefuncsplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class EnlightenmentCenter extends Controller {

    EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

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
        if (rc.canBid((int)(rc.getInfluence() * .01))){
            rc.bid((int)(rc.getInfluence() * .01));
            System.out.println("Bidding: " + (int)(rc.getInfluence() * .01));
        }
    }
}
