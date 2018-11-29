import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int bustersPerPlayer = in.nextInt(); // the amount of busters you control
		int ghostCount = in.nextInt(); // the amount of ghosts on the map
		int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right

		// game loop
		while (true) {
			int entities = in.nextInt(); // the number of busters and ghosts visible to you
			int hunterTargetX, hunterTargetY;
			for (int i = 0; i < entities; i++) {
				int entityId = in.nextInt(); // buster id or ghost id
				int x = in.nextInt();
				int y = in.nextInt(); // position of this buster / ghost
				int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
				int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
				int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.
				if(entityType == -1){
					if(state <= 5){
						hunterTargetX = x;
						hunterTargetY = y;

					}
				}
			}
			for (int i = 0; i < bustersPerPlayer; i++) {

				// Write an action using System.out.println()
				// To debug: System.err.println("Debug messages...");




				// First: MOVE x y | BUST id
				// Second: MOVE x y | TRAP id | RELEASE
				// Third: MOVE x y | STUN id | RADAR
				System.out.println("MOVE 8000 4500");
			}
		}
	}
}