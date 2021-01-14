package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

public class EnlightenmentCenter extends Controller {

    // Bookkeeping variables
    HashSet<Integer> children = new HashSet<>();
    HashMap<MapLocation, Team> discoveredECS = new HashMap<>();
    HashSet<MapLocation> targets = new HashSet<>();
    MapLocation currentTarget;

    // Voting variables
    int prev_vote_count = 0;
    double bid_percent = 0.01;

    EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {

        // setup, build 4 muckrakers
        if (turnCount == 1){
            buildIfCan(RobotType.MUCKRAKER, Direction.WEST, 1);
        }
        if (turnCount == 3){
            buildIfCan(RobotType.MUCKRAKER, Direction.WEST, 1);
        }
        if (turnCount == 5){
            buildIfCan(RobotType.MUCKRAKER, Direction.WEST, 1);
        }
        if (turnCount == 7){
            buildIfCan(RobotType.MUCKRAKER, Direction.WEST, 1);
        }


        // farming, spawn slanderer every 20 rounds with .2 of total influence

        if (turnCount > 7 && turnCount%20 == 0){
            for (Direction dir : Direction.cardinalDirections()){
                buildIfCan(RobotType.SLANDERER, dir, (int)(rc.getInfluence() * .2));
            }
        }

        // exploration, spawn muckraker every 30 rounds
        if (turnCount > 7 && turnCount%30 == 0){
            for (Direction dir : Direction.allDirections()){
                buildIfCan(RobotType.MUCKRAKER, dir, (int)(rc.getInfluence() * .2));
            }
        }



        // build politicians

        if (turnCount > 12 && turnCount%13 == 0){
            for (Direction dir : Direction.allDirections()){
                buildIfCan(RobotType.POLITICIAN, dir, (int) Math.min(500, (.1 * rc.getInfluence())));
            }
        }


        /// FOR ACTIONS IN EACH TURN
        //bid
        System.out.println(turnCount);
        bidIfCan((int) (rc.getInfluence() * bid_percent));

        //scan for new children
        checkForNewChildren();

        // Scan the flags of our children
        for (int id : children){
            byte data = getData(id);
            MapLocation location = getLocation(id);

            switch(data) {
                case Flags.NEUTRAL_EC_FOUND:    discoveredECS.put(location, NEUTRAL);   break;
                case Flags.ENEMY_EC_FOUND:      discoveredECS.put(location, ENEMY);     break;
                case Flags.FRIENDLY_EC_FOUND:   discoveredECS.put(location, FRIENDLY);  break;
            }
        }

        // Find an EC to target with our politicians
        if (currentTarget == null || discoveredECS.getOrDefault(currentTarget, FRIENDLY) == ENEMY) {
            if (discoveredECS.containsValue(NEUTRAL)) {
                for (Map.Entry<MapLocation, Team> target : discoveredECS.entrySet()) {
                    if (target.getValue() == NEUTRAL) {
                        currentTarget = target.getKey();
                        break;
                    }
                }
            } else if (discoveredECS.containsValue(ENEMY)) {
                for (Map.Entry<MapLocation, Team> target : discoveredECS.entrySet()) {
                    if (target.getValue() == ENEMY) {
                        currentTarget = target.getKey();
                        break;
                    }
                }
            }
        } else {
            Team team = discoveredECS.get(currentTarget);

            if (team == FRIENDLY) {
                currentTarget = null;
            }
        }

        if (currentTarget != null) {
            queueCommunication(currentTarget, Flags.NEUTRAL_EC_FOUND, 1);
        }

        prev_vote_count = rc.getTeamVotes();
    }

    void checkForNewChildren() {
        for (RobotInfo r : allInfo){
            if (curLocation.distanceSquaredTo(r.location) <= 1
                    && r.team == FRIENDLY){
                children.add(r.ID);
            }
        }
    }

    boolean buildIfCan(RobotType type, Direction dir, int influence) {
        try {
            if (rc.canBuildRobot(type, dir, influence)) {
                rc.buildRobot(type, dir, influence);
                return true;
            }
        } catch(GameActionException ignored) {

        }
        return false;
    }

    boolean bidIfCan(int influence) {
        try {
            if (rc.canBid(influence)) {
                rc.bid(influence);
                return true;
            }
        } catch (GameActionException ignored) {

        }
        return false;
    }
}
