package examplefuncsplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;

public abstract class Controller {
    final RobotController rc;

    // Robot Constants
    final Team FRIENDLY;
    final Team ENEMY;
    final Team NEUTRAL;
    final int SENSOR_RADIUS;
    final int ACTION_RADIUS;
    final int SENSOR_RADIUS_SQ;
    final int ACTION_RADIUS_SQ;
    final int ID;
    int EC_ID;
    MapLocation EC_LOC;

    // Robot Data
    public int turnCount;
    MapLocation curLocation;
    RobotInfo[] allInfo;
    HashMap<MapLocation, Team> discoveredECS = new HashMap<>();

    // Path Finding
    MapLocation destination;
    int minDist;
    boolean onWall;
    Direction explore_direction;

    // Communication variables
    final int Y_BITS = 8;
    final int DATA_BITS = 16;
    final int X_MASK = 0xFF;
    final int Y_MASK = 0xFF << Y_BITS;
    final int DATA_MASK = 0xFF << DATA_BITS;
    Queue<Communication> commsQueue = new LinkedList<>();

    Controller(RobotController rc) {
        this.rc = rc;

        // Initialize constants
        FRIENDLY = rc.getTeam();
        ENEMY = rc.getTeam().opponent();
        NEUTRAL = Team.NEUTRAL;
        SENSOR_RADIUS_SQ = rc.getType().sensorRadiusSquared;
        ACTION_RADIUS_SQ = rc.getType().actionRadiusSquared;
        SENSOR_RADIUS = (int) Math.ceil(Math.sqrt(SENSOR_RADIUS_SQ));
        ACTION_RADIUS = (int) Math.ceil(Math.sqrt(ACTION_RADIUS_SQ));
        ID = rc.getID();
        // Find the location of the enlightenment center we spawned from (or ourself if we are an enlightenment center)
        readSensors();
        if (rc.getType() == RobotType.ENLIGHTENMENT_CENTER) {
            EC_ID = ID;
            EC_LOC = curLocation;
        } else {
            for (RobotInfo r : allInfo) {
                if (r.type == RobotType.ENLIGHTENMENT_CENTER && r.team == FRIENDLY) {
                    EC_ID = r.ID;
                    EC_LOC = r.location;
                    break;
                }
            }
        }

        // Initialize info variables
        turnCount = 0;
        curLocation = rc.getLocation();
        if (EC_LOC != null) explore_direction = curLocation.directionTo(EC_LOC).opposite();
        else explore_direction = randomDirection();
    }

    void readSensors() {
        curLocation = rc.getLocation();
        allInfo = rc.senseNearbyRobots(-1);

        // Look through our sensor data and check if we've discovered any new ECS or if any ECS have changed teams
        for (RobotInfo r : allInfo) {
            if (r.type == RobotType.ENLIGHTENMENT_CENTER) {
                boolean newEC = false;
                if (!discoveredECS.containsKey(r.location)) {
                    discoveredECS.put(r.location, r.team);
                    newEC = true;
                }
                else {
                    Team team = discoveredECS.get(r.location);
                    if (team != r.team) {
                        discoveredECS.put(r.location, r.team);
                        newEC = true;
                    }
                }

                // If we've found any new ECS or ECS that have changed teams, add them to the communication queue
                if(newEC) {
                    byte commFlag = 0;
                    if(r.team == NEUTRAL)    commFlag = Flags.NEUTRAL_EC_FOUND;
                    if(r.team == FRIENDLY)   commFlag = Flags.FRIENDLY_EC_FOUND;
                    if(r.team == ENEMY)      commFlag = Flags.ENEMY_EC_FOUND;

                    queueCommunication(r.location, commFlag, 10);
                }
            }
        }
    }

    Direction randomDirection(){
        int num =  (int)(Math.random() * (7 + 1));
        switch (num){
            case 0: return Direction.NORTH;
            case 1: return Direction.NORTHWEST;
            case 2: return Direction.WEST;
            case 3: return Direction.SOUTHWEST;
            case 4: return Direction.SOUTH;
            case 5: return Direction.SOUTHEAST;
            case 6: return Direction.EAST;
            case 7: return Direction.NORTHEAST;
        }
        return Direction.NORTH;
    }

    boolean tryMove(Direction dir){
        try{
            if (rc.canMove(dir) && rc.isReady()) {
                rc.move(dir);
                return true;
            } else return false;
        }
        catch (GameActionException err) {
            return false;
        }
    }

    boolean moveTo(MapLocation loc) {
        boolean isLocReached = rc.getLocation().equals(loc);

        if (!isLocReached) {
            Direction dir = rc.getLocation().directionTo(loc);

            if (!tryMove(dir)) {
                tryMove(dir.rotateLeft());
            }

            isLocReached = rc.getLocation().equals(loc);
        }

        return isLocReached;
    }

    void explore(){
        // move continuously in a exploration direction
        if (rc.isReady() && !tryMove(explore_direction)){
            explore_direction = randomDirection();
        }
    }

    void orbit(){
        //orbit around friendly enlightenment center
        Direction dir = curLocation.directionTo(EC_LOC);
        Direction orbit_dir = dir.rotateLeft().rotateLeft();

        //want to maximize distance from EC while staying within its detection radius
        MapLocation move_1 = curLocation.add(orbit_dir.rotateLeft());
        MapLocation move_2 = curLocation.add(orbit_dir);
        MapLocation move_3 = curLocation.add(orbit_dir.rotateRight());

        if (move_1.distanceSquaredTo(EC_LOC) > RobotType.ENLIGHTENMENT_CENTER.sensorRadiusSquared){
            move_1 = null;
        }
        if (move_2.distanceSquaredTo(EC_LOC) > RobotType.ENLIGHTENMENT_CENTER.sensorRadiusSquared){
            move_2 = null;
        }
        if (move_3.distanceSquaredTo(EC_LOC) > RobotType.ENLIGHTENMENT_CENTER.sensorRadiusSquared){
            move_3 = null;
        }

        MapLocation moves[] = {move_1, move_2, move_3};

        // filter out moves we dont want
        ArrayList<MapLocation> possible = new ArrayList<>();

        for (MapLocation loc : moves){
            if (loc != null){
                possible.add(loc);
            }
        }

        int max_dist = 0;
        MapLocation best_move = possible.get(0);
        for (MapLocation loc : possible){
            if (loc.distanceSquaredTo(EC_LOC) > max_dist){
                max_dist = loc.distanceSquaredTo(EC_LOC);
                best_move = loc;
            }
        }

        if(!tryMove(curLocation.directionTo(best_move))){
            tryMove(curLocation.directionTo(best_move).rotateRight());
        }

    }
    void setDestination(MapLocation destination) {
        this.destination = destination;
        this.minDist = Integer.MAX_VALUE;
        this.onWall = false;
    }

    void moveToDestination() throws GameActionException {
        boolean isLocReached = curLocation.equals(destination);

        if (!isLocReached && rc.isReady()) {
            Direction dir = curLocation.directionTo(destination);
            int curDist = curLocation.distanceSquaredTo(destination);

            if (onWall) {
                if (curDist <= minDist && rc.canMove(dir)) {
                    onWall = false;
                } else {
                    if (rc.canMove(dir.rotateLeft()))
                        rc.move(dir.rotateLeft());
                    else if (rc.canMove(dir.rotateRight()))
                        rc.move(dir.rotateRight());
                }
            } else {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                } else {
                    onWall = true;
                }
            }
            minDist = Math.min(curDist, minDist);
        }
    }

    boolean sendCurLocation() {
        return sendLocation(curLocation);
    }

    boolean sendLocation(MapLocation loc) {
        try {
            // Get data bits from our current flag
            int data = 0;
            if (rc.canGetFlag(ID)) data = rc.getFlag(ID) & DATA_MASK;

            // Find x and y relative to starting location
            byte x = (byte) (loc.x - EC_LOC.x);
            byte y = (byte) (loc.y - EC_LOC.y);

            // Set the new x and y bits
            int flag = data | (y & 0xFF) << Y_BITS | (x & 0xFF);
            if (rc.canSetFlag(flag)) {
                rc.setFlag(flag);
                return true;
            }
        } catch (GameActionException e) {
            // If we fail to get our own flag or cannot set our flag, return false
        }
        return false;
    }

    boolean sendData(byte data) {
        try {
            int flag = 0;
            // Get the current flag and erase the data bits
            if (rc.canGetFlag(ID)) flag = rc.getFlag(ID) & (X_MASK | Y_MASK);
            // Set the new data bits
            flag = (data & 0xFF) << DATA_BITS | flag;
            if (rc.canSetFlag(flag)) {
                rc.setFlag(flag);
                return true;
            }
        } catch (GameActionException e) {
            // If we fail to get our own flag or cannot set our flag, return false
        }
        return false;
    }

    MapLocation getLocation(int id) {
        try {
            int flag;
            if (rc.canGetFlag(id)) flag = rc.getFlag(id);
            else return null;

            // Get the y value
            byte y = (byte) (((flag & Y_MASK) >> Y_BITS) & 0xFF);
            byte x = (byte) ((flag & X_MASK) & 0xFF);

            return new MapLocation(x + EC_LOC.x, y + EC_LOC.y);
        } catch (GameActionException e) {
            // If we cannot get the flag return null
        }
        return null;
    }

    byte getData(int id) {
        try {
            // Return the data bits if possible
            if (rc.canGetFlag(id))
                return (byte) ((rc.getFlag(id) & DATA_MASK) >> DATA_BITS);
            else return 0;

        } catch (GameActionException e) {
            // If we cannot get the flag return 0
        }
        return 0;
    }

    boolean sendCommunication(Communication comm) {
        return sendLocation(comm.location) && sendData(comm.data);
    }

    void queueCommunication(MapLocation loc, byte data, int turns) {
        commsQueue.add(new Communication(loc, data, turns));
    }

    void stopCommunication() {
        commsQueue.remove();
    }

    boolean containsCommunication(MapLocation loc) {
        return commsQueue.stream().anyMatch(communication -> communication.location != null && communication.location.equals(loc));
    }

    void broadcast() {
        Communication comm = commsQueue.peek();
        if (comm != null){
            if (sendCommunication(comm))
                comm.turns--;
            if (comm.turns <= 0) commsQueue.remove();
        } else {
            sendCommunication(new Communication(curLocation, Flags.NONE, 0));
        }
    }

    public abstract void run() throws GameActionException;
}