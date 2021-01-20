package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

public class EnlightenmentCenter extends Controller {

    // Bookkeeping variables
    Queue<RobotInfo> buildQueue = new LinkedList<>();
    HashSet<Integer> children = new HashSet<>();
    HashMap<MapLocation, Team> discoveredECS = new HashMap<>();
    HashSet<MapLocation> targets = new HashSet<>();
    MapLocation currentTarget;

    // Voting variables
    int prev_vote_count = 0;
    double bid_percent = 0.01;

    // Constants
    int RUSH_THRESHOLD = 10;

    EnlightenmentCenter(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {

        // Rush detection
        int enemyTotal = 0;
        int rushInfluence = 0;
        for (RobotInfo r : allInfo) {
            if (r.team == ENEMY) {
                enemyTotal++;
                rushInfluence += r.influence;
            }
        }
        if (enemyTotal >= RUSH_THRESHOLD)
            addToBuildQueue(RobotType.POLITICIAN, Math.max(30, rushInfluence * .1), 15);

        // setup, build 15 muckrakers
        if (turnCount == 1){
            addToBuildQueue(RobotType.MUCKRAKER, 1, 10);
        }

        // farming, spawn slanderer every 40 rounds with .2 of total influence
        if (turnCount%7 == 0 && enemyTotal == 0){
            addToBuildQueue(RobotType.SLANDERER,  rc.getInfluence() * .3, 4);
        }

        // exploration, spawn muckraker every 60 rounds
        if (turnCount > 10 && turnCount%60 == 0){
            addToBuildQueue(RobotType.MUCKRAKER, 1, 20);
        }

        // build politicians

        if (turnCount > 12 && turnCount%10 == 0){
            addToBuildQueue(RobotType.POLITICIAN, .2 * rc.getInfluence(), 1);
            addToBuildQueue(RobotType.POLITICIAN,  25, 4);
        }


        /// FOR ACTIONS IN EACH TURN
        // Build the next robot in the build queue
        buildQueue();

        //bid
        bidIfCan((int) (rc.getInfluence() * bid_percent));

        // Scan the flags of our children
        for (int id : children) {
            byte data = getData(id);
            MapLocation location = getLocation(id);

            switch(data) {
                case Flags.NEUTRAL_EC_FOUND:    discoveredECS.put(location, NEUTRAL);   break;
                case Flags.ENEMY_EC_FOUND:      discoveredECS.put(location, ENEMY);     break;
                case Flags.FRIENDLY_EC_FOUND:   discoveredECS.put(location, FRIENDLY);  break;
            }
        }

        // Calculate a safe place for slanderers
        if (turnCount % 20 == 0 && discoveredECS.containsValue(ENEMY)) {
            int enemyECS = 1;
            int dx = 0;
            int dy = 0;
            for (Map.Entry<MapLocation, Team> EC : discoveredECS.entrySet()) {
                if (EC.getValue() == ENEMY) {
                    dx += curLocation.x - EC.getKey().x;
                    dy += curLocation.y - EC.getKey().y;
                    enemyECS++;
                }

                MapLocation safetyLocation = curLocation.translate(dx / enemyECS, dy / enemyECS);
                queueCommunication(safetyLocation, Flags.SLANDERER_SAFETY, 10);
            }
        }

        // Find an EC to target with our politicians
        if (currentTarget == null ||
                (discoveredECS.get(currentTarget) == ENEMY && !discoveredECS.containsValue(NEUTRAL))) {
            Team targetTeam = ENEMY;
            int minDistance = Integer.MAX_VALUE;

            if (discoveredECS.containsValue(NEUTRAL)) targetTeam = NEUTRAL;
            else if (discoveredECS.containsValue(ENEMY)) targetTeam = ENEMY;

            for (Map.Entry<MapLocation, Team> target : discoveredECS.entrySet()) {
                if (target.getValue() == targetTeam) {
                    if (target.getKey().distanceSquaredTo(curLocation) < minDistance) {
                        currentTarget = target.getKey();
                        minDistance = target.getKey().distanceSquaredTo(curLocation);
                    }
                }
            }
        } else {
            Team team = discoveredECS.get(currentTarget);

            if (team == FRIENDLY) {
                currentTarget = null;
            }
        }

        if (currentTarget != null && !containsCommunication(currentTarget)) {
            queueCommunication(currentTarget, Flags.NEUTRAL_EC_FOUND, 30);
            addToBuildQueue(RobotType.POLITICIAN,  25, 20);
        }

        prev_vote_count = rc.getTeamVotes();
    }

    void addToBuildQueue(RobotType type, int influence, int quantity) {
        for (int i = 1; i <= quantity; i++) {
            buildQueue.add(
                    new RobotInfo(quantity, FRIENDLY, type, influence, influence, curLocation)
            );
        }
    }

    void addToBuildQueue(RobotType type, double influence, int quantity) {
        addToBuildQueue(type, (int) influence, quantity);
    }

    void buildQueue() {
        RobotInfo nextRobot = buildQueue.peek();
        if (nextRobot != null) {
            for (Direction dir : Direction.allDirections()) {
                if (buildIfCan(nextRobot.type, dir, nextRobot.influence)) {
                    buildQueue.remove();
                    break;
                }
            }
        }
    }

    boolean addNewChild(Direction dir) {
        try {
            RobotInfo r = rc.senseRobotAtLocation(curLocation.add(dir));
            if (r != null) {
                children.add(r.ID);
                return true;
            }
            else return false;
        } catch (GameActionException ignored) {

        }
        return false;
    }

    boolean buildIfCan(RobotType type, Direction dir, int influence) {
        try {
            if (rc.canBuildRobot(type, dir, influence)) {
                rc.buildRobot(type, dir, influence);
                if (type == RobotType.MUCKRAKER || type == RobotType.POLITICIAN)
                    addNewChild(dir);
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
