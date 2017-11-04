//Libraries Included
#include <EnableInterrupt.h>
#include "DualVNH5019MotorShield.h"

#define LeftFast 323 //Speed used for exploration.
#define LeftFaster 340 //Unused speed for fastest path.
#define RightFast 350 //Representation of speed but unused.
#define LeftTargetSpeed 335 //Speed used for fastest path.
#define RightFastest 400 //Speed used for fastest path.

//Delay per moving action
#define MOVE_DELAY 0 //Delay per move on exploration.
#define MOVE_DELAYII 100 //Delay per move on fastest path (except rotations which still uses original).

#define PER_CM 50 //Ticks required to move 1 cm.
#define PER_CM_II 45
#define PER_DEG 13.75 //Ticks required to move 1 degree or so.
#define PER_DEG_II 15

//Wall Constants
#define WALL_GAP 16.78
#define WALL_GAPII 16.73
#define WALL_STICK 5
#define WALL_LIMIT 26.3
#define DEG_DIFF 0.23
#define DEG_DIFF_II 0.41
#define Threshold 0.2

//Spare constants to be used if needed for calibration.
//#define WALL_GAPIII 17.08
//#define WALL_GAPIV 17.25
//#define WALL_GAPV 17.66 //to be re-measured if to be used.
//#define WALL_GAPVI 17.9 //to be re-measured if to be used.
//#define WALL_STICKII 5 //to delete if useless.
//#define WALL_STICKIII 5 //to delete if useless.
//#define WALL_LIMITII 21.12
//#define WALL_LIMITIII 21.59

//Encoder
volatile long ticksl = 0; //Count on left motor
volatile long ticksr = 0; //Count on right motor
volatile long change = 0; //Difference between left and right motor

//Sensor Pins
int sensorPin1 = A4; //Front Left Sensor
int sensorPin2 = A3; //Center Sensor
int sensorPin3 = A5; //Right Sensor
int sensorPin4 = A1; //Front Right Sensor
int sensorPin5 = A0; //Left Sensor

//Initialize Motor
DualVNH5019MotorShield md;

//Motor Variables
int target = 0; //General target to reach for rotation ticks.
int rightCount = 0; //Keeps track of number of consecutive wall or obstacle on right.
int leftCount = 0; //Keeps track of number of consecutive wall or obstacle on left.

//Sensor Variables
float mean_sensor1, mean_sensor2, mean_sensor3; //Average voltage readings
float mean_sensor4, mean_sensor5;
float dist_sensor1, dist_sensor2, dist_sensor3; //Spare floating point distances.
float dist_sensor4, dist_sensor5;
int grid1, grid2, grid3, grid4, grid5; //Number of grids in front of robot for each sensor.

//Calibration Variables
float d1, d2, d3, d4, d5; //Separate for individual calculation
float medd1[31], medd2[31], medd3[31], medd4[31], medd5[31]; //Array to store all sensor analog reads.
float diff1, diff2; //Difference between reading of sensor 1 and sensor 4, can be used for other general purposes.
float cal_target = 0; //Target to be calculated for calibration of distance.
float cal_target_angle = 0; //Target to be calculated for calibration of angle.

//Test Variables
bool flag = true;  //Spare flag, used when necessary.
bool done = false; //Spare flag II, used when necessary.
int moveCount = 0; //Keeps track of number of forward and reverses done such that if after 3 times, robot will auto calibrate.

//Incoming command
char command;
String fixedSend = "cb"; //Used to determine the destination which is algorithm for sending the sensor values.

//Grid Target Array used for Fastest Path. Each element represents the number of 'ticks'  the robot should reach before stopping at the specified grid.
int gridTarget[20] = {530, 1100, 1690, 2290, 2910, 3510, 4130, 4730, 5350, 5930, 6530, 7140, 7740, 8380, 9020, 9660, 10300, 10940, 11580, 12220};
int noGrids; //Represents number of grids decoded and to be used to extract from above array.
int targets; //Stores the extracted 'ticks' from array.
int counti = 1; //Keeps track of current position of index on fastest path string received from algorithm.

//Main program
void setup() {
  Serial.begin(9600); //Baud rate used, matching RPi side
  md.init(); //Initialize motor
  enableInterrupt(3, rising, RISING); //Interrupt pin when high on left motor
  enableInterrupt(11, rising1, RISING); //Interrupt pin when high on right motor
}

void loop() {
  char ch;
  char commandBuffer[50];
  int i = 0;
  while (1) {
    if (Serial.available()) {
      ch = Serial.read();
      commandBuffer[i] = ch;
      i++;
      if (ch == '|') {
        i = 1;
        break;
      }
    }
  }
  command = commandBuffer[0];
  switch (command) {
    //Forward
    case 'i':
      forward_fast(532);
      moveCount++;
      break;
    //Left
    case 'j':
      rotate_left_fast(90);
      break;
    //Right
    case 'l':
      rotate_right_fast(90);
      break;
    //Reverse
    case 'k':
      reverse_fast(525);
      moveCount++;
      break;
    //Read Sensor Data in terms of Grid and also finding distance.
    case 's':
      distance_print();
      break;
    //Once robot completes exploration and is at start point, stop sending sensor values and calibrate to left wall.
    case 'q':
      delay(6000);
      rotate_left_fast(180); //Quicker rotation than doing it twice.
      check_calibration();
      rotate_right_fast(90);
      check_calibration();
      rotate_right_fast(90);
      flag = false;
      break;
    //Initial calibration at start point commanded by Nexus.
    case 'y':
      rotate_left_fast(180);
      check_calibration();
      rotate_right_fast(90);
      check_calibration();
      rotate_right_fast(90);
      break;
    //Test case for testing calibration code.
    case '1':
      check_calibration();
      break;
    //Initiate fastest path decode.
    case 'm':
      path_fastest(commandBuffer);
      break;
    //In case of error in command sent, robot will brake.
    default:
      md.setBrakes(400, -400);
  }
  //Stops sending sensor values once exploration completes.
  if (flag == true) {
    read_sensors();
  }
  memset(commandBuffer, 0, sizeof(commandBuffer)); //Empty memory buffer to prevent error in receiving next command.
  Serial.flush(); //Flush outgoing buffer to prevent potential sending error in the sensor values.
}

//Fastest Path function
//Read string given by algorithm and determine what actions to take.
//For example, if given string is mi1jli14jli10, it means to move forward by 1 grid, turn right, turn left, forward by 14 grids and so on.
void path_fastest(char commandBuffer[]) {
  moveCount = 0;
  while (commandBuffer[counti] != '|') {
    targets = 0;
    while (1) {
      if (moveCount == 4) {
        rotate_left_fast(2);
        moveCount = 0;
      }
      if (commandBuffer[counti] == 'i' || commandBuffer[counti] == 'I') {
        counti++;
        if (commandBuffer[counti] == '1') {
          counti++;
          if (isdigit(commandBuffer[counti])) {
            noGrids = commandBuffer[counti] - 38;
            targets = gridTarget[noGrids - 1];
            ticksl = 0;
            ticksr = 0;
            long leftPos, rightPos;
            md.setSpeeds(LeftFaster, -RightFast);
            while (1) {
              leftPos = ticksl;
              rightPos = ticksr;
              if (rightPos >= targets) {  //Target to reach before stopping
                md.setBrakes(400, -400); //Optimal braking speed
                break;
              }
              else {
                change = ticksl - ticksr; //Ticks L > Ticks R
                md.setM1Speed(RightFastest - change * 46.9); //Increasing right speed to match left speed.
              }
            }
            delay(MOVE_DELAYII);
            break;
          }
          else {
            counti--;
            noGrids = commandBuffer[counti] - 48;
            targets = gridTarget[noGrids - 1];
            ticksl = 0;
            ticksr = 0;
            long leftPos, rightPos;
            md.setSpeeds(LeftFaster, -RightFast);
            while (1) {
              leftPos = ticksl;
              rightPos = ticksr;
              if (rightPos >= targets) {  //Target to reach before stopping
                md.setBrakes(400, -400); //Optimal braking speed
                break;
              }
              else {
                change = ticksl - ticksr; //Ticks L > Ticks R
                md.setM1Speed(RightFastest - change * 46.9); //Increasing right speed to match left speed.
              }
            }
            delay(MOVE_DELAYII);
            break;
          }
        }
        else if (commandBuffer[counti] == '2') {
          counti++;
          if (isdigit(commandBuffer[counti])) {
            noGrids = commandBuffer[counti] - 28;
            targets = gridTarget[noGrids - 1];
            ticksl = 0;
            ticksr = 0;
            long leftPos, rightPos;
            md.setSpeeds(LeftFaster, -RightFast);
            while (1) {
              leftPos = ticksl;
              rightPos = ticksr;
              if (rightPos >= targets) {  //Target to reach before stopping
                md.setBrakes(400, -400); //Optimal braking speed
                break;
              }
              else {
                change = ticksl - ticksr; //Ticks L > Ticks R
                md.setM1Speed(RightFastest - change * 46.9); //Increasing right speed to match left speed.
              }
            }
            delay(MOVE_DELAYII);
            break;
          }
          else {
            counti--;
            noGrids = commandBuffer[counti] - 48;
            targets = gridTarget[noGrids - 1];
            ticksl = 0;
            ticksr = 0;
            long leftPos, rightPos;
            md.setSpeeds(LeftFaster, -RightFast);
            while (1) {
              leftPos = ticksl;
              rightPos = ticksr;
              if (rightPos >= targets) {  //Target to reach before stopping
                md.setBrakes(400, -400); //Optimal braking speed
                break;
              }
              else {
                change = ticksl - ticksr; //Ticks L > Ticks R
                md.setM1Speed(RightFastest - change * 46.9); //Increasing right speed to match left speed.
              }
            }
            delay(MOVE_DELAY);
            break;
          }
        }
        else {
          noGrids = commandBuffer[counti] - 48;
          targets = gridTarget[noGrids - 1];
          ticksl = 0;
          ticksr = 0;
          long leftPos, rightPos;
          md.setSpeeds(LeftFaster, -RightFast);
          while (1) {
            leftPos = ticksl;
            rightPos = ticksr;
            if (rightPos >= targets) {  //Target to reach before stopping
              md.setBrakes(400, -400); //Optimal braking speed
              break;
            }
            else {
              change = ticksl - ticksr; //Ticks L > Ticks R
              md.setM1Speed(RightFastest - change * 46.9); //Increasing right speed to match left speed.
            }
          }
          delay(MOVE_DELAY);
          break;
        }
      }
      else if (commandBuffer[counti] == 'j' || commandBuffer[counti] == 'J') {
        rotate_left_fast(90);
        break;
      }
      else if (commandBuffer[counti] == 'l' || commandBuffer[counti] == 'L') {
        rotate_right_fast(90);
        break;
      }
      else {
        break;
      }
    }
    counti++;
    moveCount++;
  }
  md.setBrakes(400, -400);
  flag = false;
  counti = 1;
}

//Shifts robot front by 1 grid.
void forward_fast(int targus) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  md.setSpeeds(LeftFast, -RightFast);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= targus) {  //Target to reach before stopping
      md.setBrakes(400, -400); //Optimal braking speed
      break;
    }
    else {
      change = ticksr - ticksl; //Ticks L > Ticks R
      md.setM2Speed(-(LeftFast - change * 47)); //Increasing right speed to match left speed.
    }
  }
  delay(MOVE_DELAY);
}

//Shifts robot backwards by 1 grid.
void reverse_fast(int targus) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  md.setSpeeds(-LeftFast, RightFast);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= targus) {
      md.setBrakes(400, -400);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed((LeftFast - change * 47));
    }
  }
  delay(MOVE_DELAY);
}

//Rotates robot to the right depending on the 'degree' given.
//45 degrees used to correct encoder error, 90 degrees for standard rotation to the right.
// 1 'degree' used to calibrate angle.
void rotate_right_fast(int reference) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  //Used to fix errorneous turning.
  if (reference == 45) {
    target = 280;
  }
  //Regular 90 degree turn to the right.
  else if (reference == 90) {
    target = 730;
  }
  //Calibration turning.
  else if (reference == 1) {
    target = cal_target_angle;
  }
  md.setSpeeds(LeftFaster, RightFast);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed((LeftFaster - change * 47));
    }
  }
  if (rightPos > target) {
    rotate_right_fast(45);
  }
  delay(MOVE_DELAY);
}

//Rotates robot to the left depending on the 'degree' given.
//45 degrees used to correct encoder error, 90 degrees for standard rotation to the left.
// 1 'degree' used to calibrate angle and 2 'degree' used to calibrate if after 7 move or forward there is no calibration.
void rotate_left_fast(int reference) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  //Used to fix errorneous turning.
  if (reference == 45) {
    target = 280;
  }
  //Regular 90 degree turn to the left.
  else if (reference == 90) {
    target = 730;
  }
  //Calibration turn.
  else if (reference == 1) {
    target = cal_target_angle;
  }
  //Slight turn every 3 move if no calibration was done.
  else if (reference == 2) {
    target = 5;
  }
  //Initial calibration turning for quicker effect.
  else if(reference == 180){
    target = 1500;
  }
  md.setSpeeds(-LeftFaster, -RightFast);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed(-(LeftFaster - change * 47));
    }
  }
  if (rightPos > target) {
    rotate_left_fast(45);
  }
  delay(MOVE_DELAY);
}

//Sensors
void read_sensors() {
  String outputSensor;
  //Measure sensor values to see if can calibrate.
  for (int s = 0; s < 31; s++) {
    medd5[s] = (float)analogRead(sensorPin5);
    medd1[s] = (float)analogRead(sensorPin1);
    medd4[s] = (float)analogRead(sensorPin4);
    medd2[s] = (float)analogRead(sensorPin2);
    medd3[s] = (float)analogRead(sensorPin3);
  }
  mean_sensor5 = find_median(medd5);
  mean_sensor5 /= 204.8;
  mean_sensor1 = find_median(medd1);
  mean_sensor1 /= 204.8;
  mean_sensor4 = find_median(medd4);
  mean_sensor4 /= 204.8;
  mean_sensor2 = find_median(medd2);
  mean_sensor2 /= 204.8;
  mean_sensor3 = find_median(medd3);
  mean_sensor3 /= 204.8;
  grid1 = grid_ret1(mean_sensor1);
  grid2 = grid_ret2(mean_sensor2);
  grid3 = grid_ret3(mean_sensor3);
  grid4 = grid_ret4(mean_sensor4);
  grid5 = grid_ret5(mean_sensor5);
  //If detected obstacle or wall on right.
  if (grid3 == 1) {
    rightCount++;
  }
  //Reset if next is not.
  else {
    rightCount = 0;
  }
  //If detected obstacle or wall on left.
  if (grid5 == 1) {
    leftCount++;
  }
  //Reset if next is not.
  else {
    leftCount = 0;
  }
  //If 3 blocks on left, right and center in front of robot.
  if (grid1 == 1 && grid2 == 1 && grid4 == 1) {
    check_calibration();
    rightCount = 0;
    leftCount = 0;
    moveCount = 0;
  }
  //If 3 consecutive detected chance to calibrate on the right.
  else if (rightCount == 3) {
    rotate_right_fast(90);
    for (int s = 0; s < 31; s++) {
      medd1[s] = (float)analogRead(sensorPin1);
      medd4[s] = (float)analogRead(sensorPin4);
      medd2[s] = (float)analogRead(sensorPin2);
    }
    mean_sensor1 = find_median(medd1);
    mean_sensor1 /= 204.8;
    mean_sensor4 = find_median(medd4);
    mean_sensor4 /= 204.8;
    mean_sensor2 = find_median(medd2);
    mean_sensor2 /= 204.8;
    grid1 = grid_ret1(mean_sensor1);
    grid2 = grid_ret2(mean_sensor2);
    grid4 = grid_ret4(mean_sensor4);
    if ((grid1 == 1 && grid2 == 1 && grid4 == 1)) {
      check_calibration();
    }
    rotate_left_fast(90);
    rightCount = 0;
    leftCount = 0;
    moveCount = 0;
  }
  //If 3 consecutive detected chance to calibrate on the left (unlikely to occur since right hug).
  else if (leftCount == 3) {
    rotate_left_fast(90);
    for (int s = 0; s < 31; s++) {
      medd1[s] = (float)analogRead(sensorPin1);
      medd4[s] = (float)analogRead(sensorPin4);
      medd2[s] = (float)analogRead(sensorPin2);
    }
    mean_sensor1 = find_median(medd1);
    mean_sensor1 /= 204.8;
    mean_sensor4 = find_median(medd4);
    mean_sensor4 /= 204.8;
    mean_sensor2 = find_median(medd2);
    mean_sensor2 /= 204.8;
    grid1 = grid_ret1(mean_sensor1);
    grid2 = grid_ret2(mean_sensor2);
    grid4 = grid_ret4(mean_sensor4);
    if ((grid1 == 1 && grid2 == 1 && grid4 == 1)) {
      check_calibration();
    }
    rotate_right_fast(90);
    rightCount = 0;
    leftCount = 0;
    moveCount = 0;
  }
  //If single block in center in front of robot.
  //else if(grid1 != 1 && grid2 == 1 && grid4 != 1){
    //calibrate_dist();
  //}
  //If left and right has block and/or wall.
  //else if(grid3 == 1 && grid5 == 1){
    //calibrate_angle_II();
    //rightCount = 0;
    //leftCount = 0;
    //moveCount = 0;
  //}
  //If no calibration has occurred after 7 total forward or reverse.
  else if (moveCount == 3) {
    rotate_left_fast(2);
    moveCount = 0;
  }
  //Re-measure sensor values to output.
  for (int s = 0; s < 31; s++) {
    medd5[s] = (float)analogRead(sensorPin5);
    medd1[s] = (float)analogRead(sensorPin1);
    medd4[s] = (float)analogRead(sensorPin4);
    medd2[s] = (float)analogRead(sensorPin2);
    medd3[s] = (float)analogRead(sensorPin3);
  }
  mean_sensor5 = find_median(medd5);
  mean_sensor5 /= 204.8;
  mean_sensor1 = find_median(medd1);
  mean_sensor1 /= 204.8;
  mean_sensor4 = find_median(medd4);
  mean_sensor4 /= 204.8;
  mean_sensor2 = find_median(medd2);
  mean_sensor2 /= 204.8;
  mean_sensor3 = find_median(medd3);
  mean_sensor3 /= 204.8;
  grid1 = grid_ret1(mean_sensor1);
  grid2 = grid_ret2(mean_sensor2);
  grid3 = grid_ret3(mean_sensor3);
  grid4 = grid_ret4(mean_sensor4);
  grid5 = grid_ret5(mean_sensor5);
  //Prepare Output to RPi -> Algorithm Team
  outputSensor += String(grid1);
  outputSensor += String(grid5);
  outputSensor += String(grid3);
  outputSensor += String(grid4);
  outputSensor += String(grid2);
  Serial.print((fixedSend + outputSensor + "\n"));
  md.setBrakes(400, -400);
}

//Encoder Calculation
void rising() {
  enableInterrupt(3, falling, FALLING);
  ticksl++;
}

void rising1() {
  enableInterrupt(11, falling1, FALLING);
  ticksr++;
}

void falling() {
  enableInterrupt(3, rising, RISING);
  ticksl++;
}

void falling1() {
  enableInterrupt(11, rising1, RISING);
  ticksr++;
}

//Grid Returner 1
int grid_ret1(float mean1) {
  if (mean1 >= 1.5 && mean1 <= 3.4) {
    return 1;
  }
  else if (mean1 >= 0.95 && mean1 < 1.5) {
    return 2;
  }
  else if (mean1 >= 0.72 && mean1 < 0.95) {
    return 3;
  }
  else {
    return 0;
  }
}

//Grid Returner 2
int grid_ret2(float mean2) {
  if (mean2 >= 1.6 && mean2 <= 3.4) {
    return 1;
  }
  else if (mean2 >= 1.04 && mean2 < 1.6) {
    return 2;
  }
  else if (mean2 >= 0.81 && mean2 < 1.04) {
    return 3;
  }
  else {
    return 0;
  }
}

//Grid Returner 3
int grid_ret3(float mean3) {
  if (mean3 >= 1.57 && mean3 < 3.5) {
    return 1;
  }
  else if (mean3 >= 1.0 && mean3 < 1.57) {
    return 2;
  }
  else if (mean3 >= 0.7 && mean3 < 1.0) {
    return 3;
  }
  else {
    return 0;
  }
}

//Grid Returner 4
int grid_ret4(float mean4) {
  if (mean4 >= 1.6 && mean4 < 3.4) {
    return 1;
  }
  else if (mean4 >= 1.0 && mean4 < 1.6) {
    return 2;
  }
  else if (mean4 >= 0.85 && mean4 < 1.0) {
    return 3;
  }
  else {
    return 0;
  }
}

//Grid Returner 5
int grid_ret5(float mean5) {
  if (mean5 >= 1.04 && mean5 < 1.22) {
    return 5;
  }
  else if (mean5 >= 1.22 && mean5 < 1.46) {
    return 4;
  }
  else if (mean5 >= 1.46 && mean5 < 1.87) {
    return 3;
  }
  else if (mean5 >= 1.87 && mean5 < 2.46) {
    return 2;
  }
  else if (mean5 >= 2.46 && mean5 < 3.2) {
    return 1;
  }
  else {
    return 0;
  }
}

//Sensor 1
float distance_1(float mean1) {
  if (mean1 >= 1.54 && mean1 < 3.2) {
    return (mean1 * (-6.165) + 29.81);
  }
  else if (mean1 >= 0.66 && mean1 < 1.54) {
    return (mean1 * (-22.01) + 52.93);
  }
  else {
    return 0;
  }
}

//Sensor 2
float distance_2(float mean2) {
  if (mean2 >= 1.64 && mean2 < 3.4) {
    return (mean2 * (-6.666) + 32.11);
  }
  else if (mean2 >= 0.70 && mean2 < 1.64) {
    return (mean2 * (-22.07) + 56.34);
  }
  else {
    return 0;
  }
}

//Sensor 3
float distance_3(float mean3) {
  if (mean3 >= 1.64 && mean3 < 3.5) {
    return (mean3 * (-6.756) + 31.85);
  }
  else if (mean3 > 0.72 && mean3 < 1.64) {
    d3 = mean3 * (-22.53) + 59.69;
    if (d3 <= 30) {
      return d3 + 10;
    }
    else {
      return 40;
    }
  }
  else {
    return 0;
  }
}

//Sensor 4
float distance_4(float mean4) {
  if (mean4 >= 1.67 && mean4 < 3.4) {
    return (mean4 * (-6.883) + 31.61);
  }
  else if (mean4 >= 0.87 && mean4 < 1.67) {
    return (mean4 * (-18.83) + 45.62);
  }
  else {
    return 0;
  }
}

//Sensor 5
float distance_5(float mean5) {
  if (mean5 >= 1.75 && mean5 < 3) {
    return (mean5 * (-18.09) + 62.28);
  }
  else if (mean5 >= 1.35 && mean5 < 1.75) {
    return (mean5 * (-28.7) + 83.55) ;
  }
  else if (mean5 >= 1.22 && mean5 < 1.35) {
    return 40;
  }
  else if (mean5 > 0.9 && mean5 < 1.22) {
    d5 = (mean5 * (-43.13) + 100.5);
    return d5;
  }
  else {
    return 0;
  }
}

//Calibration at the start (unused).
void start_cal() {
  check_calibration();
  rotate_right_fast(90);
  check_calibration();
  rotate_right_fast(90);
}

//Function used to print valid distances of each sensor.
void distance_print() {
  for (int s = 0; s < 31; s++) {
    medd5[s] = (float)analogRead(sensorPin5);
    medd1[s] = (float)analogRead(sensorPin1);
    medd4[s] = (float)analogRead(sensorPin4);
    medd2[s] = (float)analogRead(sensorPin2);
    medd3[s] = (float)analogRead(sensorPin3);
  }
  mean_sensor5 = find_median(medd5);
  mean_sensor5 /= 204.8;
  mean_sensor1 = find_median(medd1);
  mean_sensor1 /= 204.8;
  mean_sensor4 = find_median(medd4);
  mean_sensor4 /= 204.8;
  mean_sensor2 = find_median(medd2);
  mean_sensor2 /= 204.8;
  mean_sensor3 = find_median(medd3);
  mean_sensor3 /= 204.8;
  dist_sensor1 = distance_1(mean_sensor1);
  dist_sensor2 = distance_2(mean_sensor2);
  dist_sensor3 = distance_3(mean_sensor3);
  dist_sensor4 = distance_4(mean_sensor4);
  dist_sensor5 = distance_5(mean_sensor5);
}

//Perform final checks to see if robot should calibrate.
void check_calibration() {
  calibrate_angle();
  calibrate_dist();
  calibrate_angle();
  delay(20);
}

//Function to align robot back to correct orientation.
void calibrate_angle() { //correct region where need to 2 times.
  for (int s = 0; s < 31; s++) {
    medd1[s] = (float)analogRead(sensorPin1);
    medd4[s] = (float)analogRead(sensorPin4);
  }
  mean_sensor1 = find_median(medd1);
  mean_sensor1 /= 204.8;
  mean_sensor4 = find_median(medd4);
  mean_sensor4 /= 204.8;
  d1 = distance_1(mean_sensor1);
  d4 = distance_4(mean_sensor4);
  diff1 = abs(d1 - d4);
  if (diff1 > DEG_DIFF) {
    if (d1 > d4) {
      cal_target_angle = (diff1 - DEG_DIFF) * PER_DEG;
      rotate_right_fast(1);
    }
    else if (d4 > d1) {
      cal_target_angle = (diff1 - DEG_DIFF) * PER_DEG;
      rotate_left_fast(1);
    }
  }
  if (diff1 > 4) {
    calibrate_angle();
  }
}

//Unused calibration for when Sensor 3 and Sensor 5 both returns 1.
void calibrate_angle_II() { 
  for (int s = 0; s < 31; s++) {
    medd3[s] = (float)analogRead(sensorPin3);
    medd5[s] = (float)analogRead(sensorPin5);
  }
  mean_sensor3 = find_median(medd3);
  mean_sensor3 /= 204.8;
  mean_sensor5 = find_median(medd5);
  mean_sensor5 /= 204.8;
  d3 = distance_3(mean_sensor3);
  d5 = distance_5(mean_sensor5);
  diff2 = abs(d3 - d5);
  if (diff2 > DEG_DIFF_II) {
    if (d5 > d3) {
      cal_target_angle = (diff2 - DEG_DIFF_II) * PER_DEG;
      rotate_left_fast(1);
    }
    else if (d3 > d5) {
      cal_target_angle = (diff2 - DEG_DIFF_II) * PER_DEG;
      rotate_right_fast(1);
    }
  }
  if (diff2 > 4) {
    calibrate_angle_II();
  }
}

//Function to align robot back to correct distance away from obstacles or wall.
void calibrate_dist() {
  for (int t = 0; t < 1; t++) {
    for (int s = 0; s < 31; s++) {
      medd2[s] = (float)analogRead(sensorPin2);
    }
    mean_sensor2 = find_median(medd2);
    mean_sensor2 /= 204.8;
    d2 = distance_2(mean_sensor2);
    if (d2 > WALL_GAP) {
      cal_target = (d2 - WALL_GAP) * PER_CM;
      if (cal_target > 200) {
        cal_target = 100;
      }
      forward_fast(cal_target);
    }
    else {
      cal_target = (WALL_GAP - d2) * PER_CM_II;
      if (cal_target > 200) {
        cal_target = 100;
      }
      reverse_fast(cal_target);
    }
  }
}

//Find Median
float find_median(float someArray[]) {
  float tmp;
  for (int i = 0; i < 31; i++) {
    for (int j = i; j > 0; j--) {
      if (someArray[j] < someArray[j - 1]) {
        tmp = someArray[j];
        someArray[j] = someArray[j - 1];
        someArray[j - 1] = tmp;
      }
      else {
        break;
      }
    }
  }
  return someArray[6];
}
