# robocode_AI
## Description of Q-learning:
The behavior control model of my choice is Q-learning. It’s one kind of Reinforcement learning(RL). Reinforcement learning involves teaching a machine (agent) to think for itself by using a system of rewards. For example, a robot can be trained by giving it a series of rewards: for every correct step taken by the robot a reward is made and for every incorrect move a reward is taken away. 
Temporal Difference(TD) learning algorithm was applied on my robot tank. In TD, learning takes place as the episode unfolds, after each action is taken. 
Q-learning is a model-free reinforcement learning algorithm to learn a policy telling an agent what action to take under what circumstances. It does not require a model of the environment, and it can handle problems with stochastic transitions and rewards, without requiring adaptations. The ‘q’ in q-learning stands for quality. Quality in this case represents how useful a given action is in gaining some future reward.

## Robocode bot design description:
We have to formally define all the following vital components for the solution: 
1.	Actions
2.	States
3.	Reward
4.	Q-function


### Actions
Here are four different actions my robot may take. 
1.	Turn clockwise
2.	Turn counterclockwise
3.	Move towards enemy robot
4.	Move away from enemy robot

### States
1.	x-axis coordinates of my robot
2.	y-axis coordinates of my robot
3.	Distance between my robot and enemy Robot
4.	Absolute bearing angle between my robot and enemy Robot 

### Rewards
The reward feedback from the environment is stated as following. 
1.	If the robot hits a wall, reward= reward - 3.5 
2.	If the robot is hit by an enemy bullet, reward= reward - 3
3.	If the robot crashes an enemy, reward= reward - 2
4.	If the robot’s bullet hits an enemy, reward= reward + 3

## Q-function:
The core of the algorithm is a Bellman equation as a simple value iteration update, using the weighted average of the old value and the new information.
The learning rate alpha is set to 0.1, which controls how quickly the robot adopts to the random changes imposed by the environment and the discount factor gamma is set to 0.9.
Before learning begins, Q (s,a) is initialized to a possibly arbitrary fixed value. Then, at each time t the agent selects an action at, observes a reward rt, enters a new state st+1, which depends on both the previous state st and the selected action, and Q table is updated. An episode of the algorithm ends when state st+1 is a final or terminal state.

## Q-learning Algorithm Process: 
### Step 1: Initialize the Q-Table
Firstly, the Q-table has to be built. 
In my example, the state-action pairs (n,m) can be selected from n=turn clockwise, turn counterclockwise, move towards enemy robot and move away from enemy robot and m= x-axis coordinates of my robot, y-axis coordinates of my robot, Distance between my robot and enemy Robot, Absolute bearing angle between my robot and enemy Robot. First, let’s initialize the values at 0.

### Step 2: Choose an action

### Step 3: Perform the action
The combination of steps 2 and 3 is performed for an undefined amount of time. These steps run until the time training is stopped, or when the training loop stopped as defined in the code.
First, an action (a) in the state (s) is chosen based on the components that I listed above. Note that, as mentioned earlier, when the episode initially starts, every Q-value should be 0.
Then, update the Q-values for being at the start and choose an action randomly using the Q- function listed above.

### Steps 4: Measure Reward
Now we have taken an action and observed an outcome and reward.

### Steps 5: Evaluate
We need to update the function Q(s,a).
This process is repeated again and again until the learning is stopped. In this way the Q-Table is been updated and the value function Q is maximized. Here the Q(state, action) returns the expected future reward of that action at that state.

### Here are the reasons why these design decisions make my robot more likely to win the tournament:

#### 1.	Attack:
The fire power will change according to the distance between my robot and enemy robot, which can increase the bullet damage.
#### 2.	Defense:
My robot will go ahead or away from the enemy robot according to the trained Q table. The direction of moving will also be modified which increases the survival rate and ram bonus.  


## A description of my implementation
### 1.	Run() 
If the parameter ‘train’ is set to true, the algorithm will choose random action to perform, in order to train the robot.
If the parameter ‘test’ is set to true, the algorithm will find the action that produces maximum Q value and perform it, in order to make the robot perform the most optimal actions. 
### 2.	normalizeBearing(double angle) 
This method normalizes the bearing angle between the enemy robot and my robot.
### 3.	onHitRobot(HitRobotEvent event); onBulletHit(BulletHitEvent event); onHitByBullet(HitByBulletEvent event)
These methods return the updated reward according to the event my robot has encountered. 
### 4.	quantize_angle(double absbearing2); quantize_distance(double distance2); quantize_position(double rl_x2)
These methods quantize the absolute bearing angle, the position and the distance between enemy robot and my robot, making them smaller and becoming more converged. 
### 5.	absoluteBearing(float x1, float y1, float x2, float y2)
This method calculates the absolute bearing angle between enemy and my robot.
### 6.	myRobotAction(int x)
This method defines the 4 different actions that my robot will choose to perform during the competition.
### 7.	initialiseQTable()
We initialize Q table in this method
### 8.	save(); load()
These methods save updates to the ‘QTable.txt’ and load information from it.
### 9.	getMax(double[] array)
This method will find the maximum q value in the array.
### 10.	onHitWall(HitWallEvent e)
This method makes sure my robot does not get stuck in the wall.





