import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobotStatus;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import java.util.Random;
/**
* 
* @author Wenjia Wang sgwwan20 201448494
* @robot_name: Julia19990204
*/

public class Julia19990204 extends AdvancedRobot {
	// learning rate. if alpha=0, it means old info is more important
	final double alpha = 0.1;
	// discount factor. if gamma=1, it means info for the future is more important
	final double gamma = 0.9;
	double distance = 0;
	double robot_x = 0;
	double robot_y = 0;
	double robot_x_q = 0;
	// declaring states
	int[] your_x = new int[8];
	int[] your_y = new int[6];
	int[] distance_to_enemy = new int[4];
	int[] gear_angle = new int[4];
	// declaring actions
	int[] action = new int[4];
	// LUT table initialization
	int[] total_states_actions = new int[8 * 6 * 4 * 4 * action.length];
	int[] total_actions = new int[4];
	String[][] qTable = new String[total_states_actions.length][2];
	String[][] CUM = new String[10][2];
	double[][] LUT_double = new double[total_states_actions.length][2];
	// quantized parameters
	int qrl_x = 0;
	int qrl_y = 0;
	int qenemy_x = 0;
	int qenemy_y = 0;
	private RobotStatus robotStatus;
	int qdistancetoenemy = 0;
	// train or test
	boolean train = false;
	boolean test = true;
	double absbearing = 0;
	int q_absbearing = 0;
	// initialize reward
	double reward = 0;
	String state_action_combi = null;
	String state_action_combi_greedy = null;
	double robot_energy = 0;
	int sa_combi_inLUT = 0;
	// Run command-Robocode
	String q_present = null;
	double q_present_double = 0;
	int random_action = 0;
	String state_action_combi_next = null;
	int sa_combi_inLUT_next = 0;
	String q_next = null;
	double q_next_double = 0;
	int count = 0;
	int Qmax_action = 0;
	int[] actions_indices = new int[total_actions.length];
	double[] q_possible = new double[total_actions.length];
	int Qmax_actual_action = 0;
	double enemy_energy = 0;
	double reward1 = 0;
	double my_energy_pres = 0;
	double enemy_energy_pres = 0;
	double my_energy_next = 0;
	double enemy_energy_next = 0;
	double gunTurnAmt;
	double bearing;
	int rlaction;
	int store_action;
	private double getHeadingRadians;
	private double getVelocity;
	private double absBearing;
	private double getBearing;
	private double getTime;
	private double normalizeBearing;
	int count_battles;
	double cum_reward;
	double cum_reward_while = 0;
	static double[] cum_reward_array = new double[1000];
	double cum_reward_hun = 0;
	static int index1 = 0;
	public int getRoundNum;
	
	// save to file "cum.txt"
	public void saveCum() {

		PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("cum.txt")));
			for (int i = 0; i < cum_reward_array.length; i++) {
				w.println(cum_reward_array[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			w.flush();
			w.close();
		}

	}
	
	public void onBattleEnded(BattleEndedEvent e) {
		saveCum();
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		System.out.println("cumulative reward of one full battle is ");
		System.out.println(cum_reward_while);
		System.out.println("index number ");
		System.out.println(getRoundNum());
		cum_reward_array[getRoundNum()] = cum_reward_while;
		for (int i = 0; i < cum_reward_array.length; i++) {
			System.out.println(cum_reward_array[i]);
			System.out.println();
		}

		index1 = index1 + 1;
		saveCum();
	}

	public void run() {
		if (count == 0) {
			try {
				load();
			} catch (IOException e) {
				e.printStackTrace();

			}
		} 
		count = count + 1;
		setColors(Color.pink,Color.WHITE,Color.red);
		while (true) {
			// train the robot
			if (train) { 
				save();				
				try {
					load();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// predict current state:
				turnGunRight(360);
				random_action = randInt(1, total_actions.length);
				state_action_combi = qrl_x + "" + qrl_y + "" + qdistancetoenemy + "" + q_absbearing + ""
						+ random_action;

				for (int i = 0; i < qTable.length; i++) {
					if (qTable[i][0].equals(state_action_combi)) {
						sa_combi_inLUT = i;
						break;
					}
				}
				q_present = qTable[sa_combi_inLUT][1];
				q_present_double = Double.parseDouble(q_present);
				reward = 0;

				// performing next state and scanning

				my_energy_pres = robot_energy;
				enemy_energy_pres = enemy_energy;
				myRobotAction(random_action);

				turnGunRight(360);
				my_energy_next = robot_energy;
				enemy_energy_next = enemy_energy;
				reward1 = 0;
				reward1 = (my_energy_next - my_energy_pres) - (enemy_energy_next - enemy_energy_pres);

				state_action_combi_next = qrl_x + "" + qrl_y + "" + qdistancetoenemy + "" + q_absbearing + ""
						+ random_action;
				for (int i = 0; i < qTable.length; i++) {
					if (qTable[i][0].equals(state_action_combi_next)) {
						sa_combi_inLUT_next = i;
						break;
					}
				}
				q_next = qTable[sa_combi_inLUT_next][1];
				q_next_double = Double.parseDouble(q_next);

				// performing update of the q value. it is the Q-function
				q_present_double = q_present_double + alpha * (reward + gamma * q_next_double - q_present_double);
				qTable[sa_combi_inLUT][1] = Double.toString(q_present_double);
				cum_reward_while += reward;

			}   	
			// test the robot
			if (test) {
				save();
				// load command
				try {
					load();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// load command
				// predict current state:
				turnGunRight(360);
				// finding action that produces maximum Q value
				for (int j = 1; j <= total_actions.length; j++) {
					state_action_combi = qrl_x + "" + qrl_y + "" + qdistancetoenemy + "" + q_absbearing + "" + j;

					for (int i = 0; i < qTable.length; i++) {
						if (qTable[i][0].equals(state_action_combi)) {
							actions_indices[j - 1] = i;
							break;

						}
					}

				}
				// converting table to double
				for (int i = 0; i < total_states_actions.length; i++) {
					for (int j = 0; j < 2; j++) {
						LUT_double[i][j] = Double.valueOf(qTable[i][j]).doubleValue();
					}
				}
				// converting table to double
				for (int k = 0; k < total_actions.length; k++) {
					q_possible[k] = LUT_double[actions_indices[k]][1];
				}

				Qmax_action = getMax(q_possible) + 1;
				int jj = 0;

				// find position of actions
				for (int i = 0; i < 4; i++) {
					if (actions_indices[i] == Qmax_action) {
						Qmax_actual_action = i + 1;
					}
				}
				// find position of actions

				// finding action that produces maximum q
				state_action_combi_greedy = qrl_x + "" + qrl_y + "" + qdistancetoenemy + "" + q_absbearing + ""
						+ Qmax_action;

				for (int i = 0; i < qTable.length; i++) {
					if (qTable[i][0].equals(state_action_combi_greedy)) {
						sa_combi_inLUT = i;
						break;
					}
				}

				q_present = qTable[sa_combi_inLUT][1];
				q_present_double = Double.parseDouble(q_present);
				reward = 0;

				// performing next state and scanning
				reward1 = 0;
				my_energy_pres = robot_energy;
				enemy_energy_pres = enemy_energy;

				myRobotAction(Qmax_action);

				turnGunRight(360);

				my_energy_next = robot_energy;
				enemy_energy_next = enemy_energy;
				reward1 = 0;
				reward1 = (my_energy_next - my_energy_pres) - (enemy_energy_next - enemy_energy_pres);

				state_action_combi_next = qrl_x + "" + qrl_y + "" + qdistancetoenemy + "" + q_absbearing + ""
						+ Qmax_action;
				for (int i = 0; i < qTable.length; i++) {
					if (qTable[i][0].equals(state_action_combi_next)) {
						sa_combi_inLUT_next = i;
						break;
					}
				}
				q_next = qTable[sa_combi_inLUT_next][1];
				q_next_double = Double.parseDouble(q_next);

				// performing update
				q_present_double = q_present_double + alpha * (reward + gamma * q_next_double - q_present_double);
				qTable[sa_combi_inLUT][1] = Double.toString(q_present_double);
				cum_reward_while += reward;
			} 
		} 
	}

	//function definitions for RL robot:
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();
		this.absBearing = absBearing;
		double getVelocity = e.getVelocity();
		double getHeadingRadians = e.getHeadingRadians();
		this.getHeadingRadians = getHeadingRadians;
		this.getVelocity = getVelocity;

		double getBearing = e.getBearing();
		this.getBearing = getBearing;
		double getTime = getTime();
		this.getTime = getTime;
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		this.gunTurnAmt = gunTurnAmt;

		double normalizeBearing = normalizeBearing(getBearing + 90 - (15 * 1));
		this.normalizeBearing = normalizeBearing;
		robot_energy = getEnergy();
		enemy_energy = e.getEnergy();
		distance = e.getDistance(); // distance to the enemy
		qdistancetoenemy = quantize_distance(distance); // distance to enemy state number 3

		// fire
		if (qdistancetoenemy == 1) {
			fire(3);

		}
		if (qdistancetoenemy == 2) {
			fire(2);
		}
		if (qdistancetoenemy == 3) {
			fire(1);
		}
		// fire

		// your robot

		qrl_x = quantize_position(getX()); // your x position -state number 1
		qrl_y = quantize_position(getY()); // your y position -state number 2
		// Calculating Enemy X & Y:
		double angleToEnemy = e.getBearing();

		// Calculate the angle to the scanned robot
		double angle = Math.toRadians((getHeading() + angleToEnemy % 360));
		// Calculate the coordinates of the robot
		double enemyX = (getX() + Math.sin(angle) * e.getDistance());
		double enemyY = (getY() + Math.cos(angle) * e.getDistance());
		qenemy_x = quantize_position(enemyX); // enemy x-position
		qenemy_y = quantize_position(enemyY); // enemy y-position

		// distance to enemy
		// absolute angle to enemy
		absbearing = absoluteBearing((float) getX(), (float) getY(), (float) enemyX, (float) enemyY);

		q_absbearing = quantize_angle(absbearing); // state number 4

	}
	// normalize the bearing angle
	public double normalizeBearing(double angle) {
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;

	}

	
	//reward functions
	public void onHitRobot(HitRobotEvent event) {
		// our robot hit by enemy robot
		reward -= 2;
	} 

	public void onBulletHit(BulletHitEvent event) {
		// one of our bullet hits enemy robot
		reward += 3;
	} 

	public void onHitByBullet(HitByBulletEvent event) {
		// when our robot is hit by a bullet
		reward -= 3;
	} 

	private int quantize_angle(double absbearing2) {

		if ((absbearing2 > 0) && (absbearing2 <= 90)) {
			q_absbearing = 1;
		} else if ((absbearing2 > 90) && (absbearing2 <= 180)) {
			q_absbearing = 2;
		} else if ((absbearing2 > 180) && (absbearing2 <= 270)) {
			q_absbearing = 3;
		} else if ((absbearing2 > 270) && (absbearing2 <= 360)) {
			q_absbearing = 4;
		}
		return q_absbearing;
	}

	// quantize distance to the enemy
	private int quantize_distance(double distance2) {

		if ((distance2 > 0) && (distance2 <= 250)) {
			qdistancetoenemy = 1;
		} else if ((distance2 > 250) && (distance2 <= 500)) {
			qdistancetoenemy = 2;
		} else if ((distance2 > 500) && (distance2 <= 750)) {
			qdistancetoenemy = 3;
		} else if ((distance2 > 750) && (distance2 <= 1000)) {
			qdistancetoenemy = 4;
		}
		return qdistancetoenemy;
	}

	//absolute bearing angle
	double absoluteBearing(float x1, float y1, float x2, float y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;
		// both pos: lower-Left
		if (xo > 0 && yo > 0) { 			
			bearing = arcSin;
		// x neg, y pos: lower-right
		} else if (xo < 0 && yo > 0) { 
			// arcsin is negative here, actuall 360 - ang
			bearing = 360 + arcSin; 
		// x pos, y neg: upper-left
		} else if (xo > 0 && yo < 0) { 
			bearing = 180 - arcSin;
		// both neg: upper-right
		} else if (xo < 0 && yo < 0) { 
			// arcsin is negative here, actually 180 + ang
			bearing = 180 - arcSin; 
		}

		return bearing;
	}

	
	
	private int quantize_position(double rl_x2) {
	
		if ((rl_x2 > 0) && (rl_x2 <= 100)) {
			qrl_x = 1;
		} else if ((rl_x2 > 100) && (rl_x2 <= 200)) {
			qrl_x = 2;
		} else if ((rl_x2 > 200) && (rl_x2 <= 300)) {
			qrl_x = 3;
		} else if ((rl_x2 > 300) && (rl_x2 <= 400)) {
			qrl_x = 4;
		} else if ((rl_x2 > 400) && (rl_x2 <= 500)) {
			qrl_x = 5;
		} else if ((rl_x2 > 500) && (rl_x2 <= 600)) {
			qrl_x = 6;
		} else if ((rl_x2 > 600) && (rl_x2 <= 700)) {
			qrl_x = 7;
		} else if ((rl_x2 > 700) && (rl_x2 <= 800)) {
			qrl_x = 8;
		}
		return qrl_x;

	}

	public void myRobotAction(int x) {
		switch (x) {
		// action 1 of the RL robot
		case 1: 
			// moves in anticlockwise direction
			int moveDirection = +1; 
			if (getVelocity == 0)
				moveDirection *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection);
			break;
		// action 2 of the RL robot	
		case 2: 
			// moves in clockwise direction
			int moveDirection1 = -1; 
			if (getVelocity == 0)
				moveDirection1 *= 1;

			// circle our enemy
			setTurnRight(getBearing + 90);
			setAhead(150 * moveDirection1);
			break;
		// action 3 of the RL robot
		case 3: 
			// Try changing these to setTurnGunRight
			turnGunRight(gunTurnAmt); 
			turnRight(getBearing - 25); 
			// move close to the enemy
			ahead(150);
			break;
		// action 4 of the RL robot
		case 4: 
			// Try changing these to setTurnGunRight
			turnGunRight(gunTurnAmt); 
			turnRight(getBearing - 25);
			// move away from the enemy
			back(150);
			break;

		}
	}

	//random integer
	public static int randInt(int min, int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	//q table initialization:
	public void initialiseQTable() {
		int[] total_states_actions = new int[8 * 6 * 4 * 4 * action.length];
		qTable = new String[total_states_actions.length][2];
		int z = 0;
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 6; j++) {
				for (int k = 1; k <= 4; k++) {
					for (int l = 1; l <= 4; l++) {
						for (int m = 1; m <= action.length; m++) {
							qTable[z][0] = i + "" + j + "" + k + "" + l + "" + m;
							qTable[z][1] = "0";
							z = z + 1;
						}
					}
				}
			}
		}

	} 
	// save to file "QTable.txt"
	public void save() {

		PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("QTable.txt")));
			for (int i = 0; i < qTable.length; i++) {
				w.println(qTable[i][0] + "    " + qTable[i][1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			w.flush();
			w.close();
		}

	}
	// load from file "QTable.txt"
	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(getDataFile("QTable.txt")));
		String line = reader.readLine();
		try {
			int zz = 0;
			while (line != null) {
				String splitLine[] = line.split("    ");
				qTable[zz][0] = splitLine[0];
				qTable[zz][1] = splitLine[1];
				zz = zz + 1;
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}

	//get max value
	public static int getMax(double[] array) {

		double largest = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] >= largest) {
				largest = array[i];
				index = i;
			}
		}
		return index;
	}

	//To make sure my robot does not get stuck in the wall
	public void onHitWall(HitWallEvent e) {
		reward -= 3.5;// earlier it was -2
		double xPos = this.getX();
		double yPos = this.getY();
		double width = this.getBattleFieldWidth();
		double height = this.getBattleFieldHeight();
		// too close to the bottom
		if (yPos < 80)
		{

			turnLeft(getHeading() % 90);
			if (getHeading() == 0) {
				turnLeft(0);
			}
			if (getHeading() == 90) {
				turnLeft(90);
			}
			if (getHeading() == 180) {
				turnLeft(180);
			}
			if (getHeading() == 270) {
				turnRight(90);
			}
			ahead(150);
			if ((this.getHeading() < 180) && (this.getHeading() > 90)) {
				this.setTurnLeft(90);
			} else if ((this.getHeading() < 270) && (this.getHeading() > 180)) {
				this.setTurnRight(90);
			}
		// too close to the top
		} else if (yPos > height - 80) { 
			// System.out.println("Too close to the Top");
			if ((this.getHeading() < 90) && (this.getHeading() > 0)) {
				this.setTurnRight(90);
			} else if ((this.getHeading() < 360) && (this.getHeading() > 270)) {
				this.setTurnLeft(90);
			}
			turnLeft(getHeading() % 90);
			if (getHeading() == 0) {
				turnRight(180);
			}
			if (getHeading() == 90) {
				turnRight(90);
			}
			if (getHeading() == 180) {
				turnLeft(0);
			}
			if (getHeading() == 270) {
				turnLeft(90);
			}
			ahead(150);

		} else if (xPos < 80) {
			turnLeft(getHeading() % 90);
			if (getHeading() == 0) {
				turnRight(90);
			}
			if (getHeading() == 90) {
				turnLeft(0);
			}
			if (getHeading() == 180) {
				turnLeft(90);
			}
			if (getHeading() == 270) {
				turnRight(180);
			}
			ahead(150);
		} else if (xPos > width - 80) {
			turnLeft(getHeading() % 90);
			if (getHeading() == 0) {
				turnLeft(90);
			}
			if (getHeading() == 90) {
				turnLeft(180);
			}
			if (getHeading() == 180) {
				turnRight(90);
			}
			if (getHeading() == 270) {
				turnRight(0);
			}
			ahead(150);
		}

	}
}
