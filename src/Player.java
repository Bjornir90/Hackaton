import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
	static int MINRANGE = 900, MAXRANGE = 1760, BASEX, BASEY, BASERADIUS = 1600;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int bustersPerPlayer = in.nextInt(); // the amount of busters you control
		int ghostCount = in.nextInt(); // the amount of ghosts on the map
		int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
		int ennemyTeamId = 1-myTeamId;

		BASEX = (myTeamId == 0)?0:16000;
		BASEY = (myTeamId == 0)?0:9000;

		// game loop
		while (true) {
			int entities = in.nextInt(); // the number of busters and ghosts visible to you
			ArrayList<Entity> entityArrayList = new ArrayList<>();
			Hunter hunter = null;
			Catcher catcher = null;
			int busterTargetX = 0, busterTargetY = 0, busterTargetID = -1;
			int busterX = 0, busterY = 0, busterState = 0;
			for (int i = 0; i < entities; i++) {
				int entityId = in.nextInt(); // buster id or ghost id
				int x = in.nextInt();
				int y = in.nextInt(); // position of this buster / ghost
				int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
				int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
				int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.

				if(entityType == -1){
					Ghost currentGhost = new Ghost(x, y, state, entityId, entityType);//x, y, stamina, ID
					entityArrayList.add(currentGhost);
				} else if(entityType == myTeamId){
					if(entityId == 0){
						hunter = new Hunter(x, y, state, entityId, entityType);
					} else if(entityId == 1){
						catcher = new Catcher(x, y, state, entityId, entityType);
					} else if(entityId == 2){
						//supportX = x;
						//supportY = y;
					}
				}

			}

			catcher.setHunter(hunter);

			//Select target
			hunter.computeTarget(entityArrayList);
			catcher.computeTarget(entityArrayList);



			for (int i = 0; i < bustersPerPlayer; i++) {

				// Write an action using System.out.println()
				// To debug: System.err.println("Debug messages...");
				String command = "MOVE 15000 8000";

				if(i == 0){//hunter
					command = hunter.computeCommand();
				} else if(i == 1){//Catcher
					command = catcher.computeCommand();
				}

				// First: MOVE x y | BUST id
				// Second: MOVE x y | TRAP id | RELEASE
				// Third: MOVE x y | STUN id | RADAR
				System.err.println(i+command);
				System.out.println(command);
			}
		}
	}
}
abstract class Entity{
	int x, y, state, id, teamId;
	public Entity(int px, int py, int pstate, int pid, int pteamId){
		x = px;
		y = py;
		state = pstate;
		id = pid;
		teamId = pteamId;
	}


}
abstract class Buster extends Entity{
	Ghost target;

	public Buster(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId);
	}

	abstract public void computeTarget(ArrayList<Entity> entities);
	abstract public String computeCommand();

}

class Hunter extends Buster{

	public Hunter(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId);
	}

	@Override
	public void computeTarget(ArrayList<Entity> entities) {
		int currentLowestStamina = 41;
		for(Entity e : entities){
			if(e.teamId == -1 && e.state < currentLowestStamina && e.state > 0){
				target = (Ghost) e;
				currentLowestStamina = e.state;
				System.err.println("Found hunter target : "+e.id);
			}
		}
	}

	@Override
	public String computeCommand() {
		String command = "MOVE 15000 8000";
		if(target != null) {
			System.err.println("Hunter has a target");
			int distanceToTarget = Utils.distance(this, target);
			if (distanceToTarget < Player.MAXRANGE && distanceToTarget > Player.MINRANGE) {
				command = "BUST " + target.id;
			} else if (distanceToTarget > Player.MAXRANGE) {
				command = "MOVE " + target.x + " " + target.y;
			}
			System.err.println("Hunter command : "+command);
		}
		return command;
	}
}

class Catcher extends Buster{

	Hunter hunter;

	public Catcher(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId);
	}

	@Override
	public void computeTarget(ArrayList<Entity> entities) {
		for(Entity e : entities) {
			if(e.teamId == -1 && e.state == 0){
				target = (Ghost) e;
			}
		}
	}

	@Override
	public String computeCommand() {
		String command = "MOVE "+hunter.x+" "+hunter.y;
		if(state == 1) {//carrying
			command = "Move " + Player.BASEX + " " + Player.BASEY;
			if(Utils.distance(x, y, Player.BASEX, Player.BASEY) < Player.BASERADIUS){
				command = "RELEASE";
			}
		}
		if(target != null) {
			if (target.id != -1) {
				int distanceToTarget = Utils.distance(this, target);
				if (distanceToTarget < Player.MAXRANGE && distanceToTarget > Player.MINRANGE) {
					command = "TRAP " + target.id;
				} else if (distanceToTarget > Player.MAXRANGE) {
					command = "MOVE " + target.x + " " + target.y;
				}
			}
		}
		return command;
	}

	public void setHunter(Hunter hunter) {
		this.hunter = hunter;
	}
}

class Ghost extends Entity{
	public Ghost(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId);
	}
}

class Utils {
	static public int distance(Entity e1, Entity e2){
		return distance(e1.x, e1.y, e2.x, e2.y);
	}

	static public int distance(int x1, int y1, int x2, int y2){
		int diffX = Math.abs(x1-x2), diffY = Math.abs(y1-y2);
		return (int) Math.sqrt(diffX*diffX+diffY*diffY);
	}
}