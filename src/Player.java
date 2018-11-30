import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
	static int MINRANGE = 900, MAXRANGE = 1760, BASEX, BASEY, BASERADIUS = 1600, myTeamId, ennemyTeamId;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int bustersPerPlayer = in.nextInt(); // the amount of busters you control
		int ghostCount = in.nextInt(); // the amount of ghosts on the map
		boolean reconIsDone = false;

		myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
		ennemyTeamId = 1 - myTeamId;

		BASEX = (myTeamId == 0) ? 0 : 16000;
		BASEY = (myTeamId == 0) ? 0 : 9000;

		HashMap<Integer, Buster> opponentBusters = new HashMap<>();
		HashMap<Integer, Ghost> ghosts = new HashMap<>();
		ArrayList<Buster> busters = new ArrayList<>();

		Hunter hunter = null;
		Catcher catcher = null;
		Support support = null;

		// game loop
		while (true) {
			int entities = in.nextInt(); // the number of busters and ghosts visible to you
			ArrayList<Entity> entityArrayList = new ArrayList<>();
			for(Ghost g : ghosts.values()){
				g.isVisible = false;
			}
			for (int i = 0; i < entities; i++) {
				int entityId = in.nextInt(); // buster id or ghost id
				int x = in.nextInt();
				int y = in.nextInt(); // position of this buster / ghost
				int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
				int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
				int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.


				if (entityType == -1) {
					Ghost currentGhost = new Ghost(x, y, state, entityId, entityType);//x, y, stamina, ID
					entityArrayList.add(currentGhost);
					Ghost currentExistingGhost = ghosts.get(entityId);
					if(currentExistingGhost != null){
						currentExistingGhost.update(x, y, state);
						currentExistingGhost.isVisible = true;
					} else {
						currentGhost.isVisible = true;
						ghosts.put(entityId, currentGhost);
					}
				} else if (entityType == myTeamId) {
					if (entityId == 0 + myTeamId * 3) {
						if (hunter == null) {
							hunter = new Hunter(x, y, state, entityId, entityType);
							busters.add(hunter);
						} else {
							hunter.update(x, y, state);
						}
					} else if (entityId == 1 + myTeamId * 3) {
						if (catcher == null) {
							catcher = new Catcher(x, y, state, entityId, entityType);
							busters.add(catcher);
						} else {
							catcher.update(x, y, state);
						}
					} else if (entityId == 2 + myTeamId * 3) {
						if (support == null) {
							support = new Support(x, y, state, entityId, entityType);
							busters.add(support);
						} else {
							support.update(x, y, state);
						}
					}
				} else if (entityType == ennemyTeamId) {
					if (entityId == 0 + ennemyTeamId * 3) {
						Hunter e_hunter = new Hunter(x, y, state, entityId, entityType);
						entityArrayList.add(e_hunter);
						opponentBusters.put(0, e_hunter);
					} else if (entityId == 1 + ennemyTeamId * 3) {
						Catcher e_catcher = new Catcher(x, y, state, entityId, entityType);
						entityArrayList.add(e_catcher);
						opponentBusters.put(1, e_catcher);
					} else if (entityId == 2 + ennemyTeamId * 3) {
						Support e_support = new Support(x, y, state, entityId, entityType);
						entityArrayList.add(e_support);
						opponentBusters.put(2, e_support);
					}
				}

			}


			System.err.println("Number of ghosts in memory : "+ ghosts.size());
			int numberOfVisibleGhosts = 0;
			for(Ghost g : ghosts.values()){
				g.score = g.state+Utils.distance(hunter, g)/800;
				if(g.isVisible)
					numberOfVisibleGhosts++;
			}

			System.err.println("Visible ghosts : "+numberOfVisibleGhosts);

			catcher.setHunter(hunter);
			support.setCatcher(catcher);

			//Select target
			for(Buster b : busters) {

				b.setGhosts(ghosts);

				b.computeTarget(entityArrayList);
				if(Utils.distance(b.x, b.y, Player.BASEX, Player.BASEY) > 7500){
					reconIsDone = true;
				}
				if(!reconIsDone){
					b.executeInitialRecon();
				}
			}
			support.computeStunTarget(entityArrayList);




			for (int i = 0; i < bustersPerPlayer; i++) {

				// Write an action using System.out.println()
				// To debug: System.err.println("Debug messages...");
				String command = "MOVE 15000 8000";

				if (i == 0) {//hunter
					command = hunter.computeCommand();
				} else if (i == 1) {//Catcher
					command = catcher.computeCommand();
				} else if (i == 2) {
					command = support.computeCommand();
				}

				// First: MOVE x y | BUST id
				// Second: MOVE x y | TRAP id | RELEASE
				// Third: MOVE x y | STUN id | RADAR
				System.out.println(command);
			}
		}
	}
}

class Entity{
	int x, y, state, id, teamId;
	public Entity(int px, int py, int pstate, int pid, int pteamId){
		x = px;
		y = py;
		state = pstate;
		id = pid;
		teamId = pteamId;
	}
	public void update(int px, int py, int pstate){
		x = px;
		y = py;
		state = pstate;
	}

	@Override
	public String toString() {
		return "Entity{" +
				"x=" + x +
				", y=" + y +
				", state=" + state +
				", id=" + id +
				", teamId=" + teamId +
				'}';
	}
}
abstract class Buster extends Entity{
	Entity target;
	double angleInitialRecon;
	boolean executingRecon;
	HashMap<Integer, Ghost> ghosts;


	public Buster(int px, int py, int pstate, int pid, int pteamId, double angle) {
		super(px, py, pstate, pid, pteamId);
		angleInitialRecon = angle;
		executingRecon = true;
	}

	abstract public void computeTarget(ArrayList<Entity> entities);
	abstract public String computeCommand();

	public void executeInitialRecon(){
		Entity movementTarget = new Entity((int)(8000*Math.cos(angleInitialRecon/2)), (int)(9000*Player.myTeamId-8000*Math.sin(angleInitialRecon/2)), -1, -1, -2) {
		};
		System.err.println("Recon target :"+movementTarget);
		target = movementTarget;
		executingRecon = true;
	}
	public String reconCommand(){
		if(executingRecon){
			executingRecon = false;
			return "MOVE "+target.x+" "+target.y+" Init";
		} else {
			return null;
		}
	}
	public void setGhosts(HashMap<Integer, Ghost> ghosts) {
		this.ghosts = ghosts;
	}

}

class Hunter extends Buster{

	int movementX = -1, movementY = -1;
	double angle;

	public Hunter(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId, Math.PI*Player.myTeamId-Math.PI/4);
		angle = 0;
	}

	@Override
	public void computeTarget(ArrayList<Entity> entities) {
		double minScore = 100000;
		target = null;
		for(Ghost g : ghosts.values()){
			if(g.state > 0){
				if(g.score < minScore){
					target = g;
					minScore = g.score;
				}
			}
		}
		if(target == null){
			target = new Entity((int)(Math.random()*16000), (int)(Math.random()*9000), -1, -1, -2);
		}
	}

	@Override
	public String computeCommand() {
		String command = reconCommand();
		if(command == null){
				if(target.teamId == -1 && Utils.distance(this, target) > Player.MINRANGE && Utils.distance(this, target) < Player.MAXRANGE){
					Ghost targetGhost = (Ghost) target;
					if(targetGhost.isVisible) {
						command = "BUST " + target.id;
					} else {
						if (angle > Math.PI * 2) {
							ghosts.remove(target.id);//Ghost is probably gone
						}
						command = "MOVE " + (int) (target.x + Player.MAXRANGE * Math.cos(angle)) + " " + (int) (target.y + Player.MAXRANGE * Math.sin(angle)) + " Searching";
						angle += Math.PI / 2;
					}
				} else {
					command = "MOVE "+target.x+" "+target.y+" Roger roger";
				}
		}
		return command;
	}
}

class Catcher extends Buster{

	Hunter hunter;
	double angle;


	public Catcher(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId, Math.PI*Player.myTeamId-Math.PI*3/4);
		this.angle = 0;
	}

	@Override
	public void computeTarget(ArrayList<Entity> entities) {
		if(state == 1){//carrying
			target = new Entity(Player.BASEX, Player.BASEY, -1, -2, -2);
			return;
		}
		int minDistance = 25000;
		for(Ghost g : ghosts.values()){
			if(g.state == 0) {
				if (Utils.distance(this, g) < minDistance) {
					target = g;
					minDistance = Utils.distance(this, g);
				}
			}
		}
		if(target == null){
			target = new Entity((int)(Math.random()*16000), (int)(Math.random()*9000), -1, -1, -2);
		}
	}

	@Override
	public String computeCommand() {
		String command = reconCommand();
		if(command == null) {
			command = "MOVE " + target.x + " " + target.y +" Roger roger";
			int distanceToTarget = Utils.distance(this, target);
			if(target.teamId == -1) {
				Ghost targetGhost = (Ghost) target;
				if (distanceToTarget < Player.MAXRANGE && distanceToTarget > Player.MINRANGE && targetGhost.isVisible) {
					command = "TRAP " + target.id + " Found";
				} else {
					if (distanceToTarget < 2100 && !targetGhost.isVisible) {//ghost isn't here
						if (angle > Math.PI * 2) {
							ghosts.remove(target.id);//Ghost is probably gone
						}
						command = "MOVE " + (int) (target.x + Player.MAXRANGE * Math.cos(angle)) + " " + (int) (target.y + Player.MAXRANGE * Math.sin(angle)) + " Searching";
						angle += Math.PI / 2;
					}
				}
			}
			if (state == 1) {//carrying
				command = "MOVE "+target.x+" "+target.y+" Home";

				if (target.id == -2 && Utils.distance(this, target) < Player.BASERADIUS) {//Inside base
					command = "RELEASE Yay!";
				}
			}
		}
		return command;
	}

	public void setHunter(Hunter hunter) {
		this.hunter = hunter;
	}
}

class Support extends Buster{
	Catcher catcher;
	Entity stunTarget;
	int angle = 0;
	int decompte = 0;
	public Support(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId, Math.PI*Player.myTeamId-Math.PI/2);
	}

	@Override
	public void computeTarget(ArrayList<Entity> entities) {
		target = null;
		for(Entity e : entities) {
			if(e.teamId == -1 && e.state == 0){
				target = e;
			}
		}
		if(target == null) target = catcher;
	}

	public void computeStunTarget(ArrayList<Entity> entities) {
		stunTarget = null;
		for(Entity e :entities) {
			if (e.teamId == Player.ennemyTeamId && e.id == 3*Player.ennemyTeamId + 1){
				stunTarget = e;
			}
		}
		for(Entity e :entities) {
			if(e.teamId == Player.ennemyTeamId && e.id == 3*Player.ennemyTeamId + 2){
				if(stunTarget == null) stunTarget = e;
			}
		}

	}

	@Override
	public String computeCommand() {
		String command = reconCommand();
		if(command == null){
			if(stunTarget != null) {
				if (Utils.distance(stunTarget, this) <= Player.MAXRANGE && stunTarget.state != 2 && decompte == 0) {
					command = "STUN " + stunTarget.id;
					decompte = 20;
				} else {
					command = "MOVE " + stunTarget.x + " " + stunTarget.y;
				}
			} else {
				command = "MOVE " + (int) (target.x + Player.MAXRANGE * Math.cos(angle)) + " " + (int) (target.y + Player.MAXRANGE * Math.sin(angle));
			}
			this.angle += 1;
		}
		return command;
	}

	@Override
	public void update(int px, int py, int pstate){
		super.update(px, py, pstate);
		if (decompte > 0) decompte -= 1;
	}

	public void setCatcher(Catcher catcher) {
		this.catcher = catcher;
	}

}

class Ghost extends Entity{
	double score;
	boolean isVisible;
	public Ghost(int px, int py, int pstate, int pid, int pteamId) {
		super(px, py, pstate, pid, pteamId);
		isVisible = false;
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