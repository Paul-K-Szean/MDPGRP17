#include <EnableInterrupt.h>
#include "DualVNH5019MotorShield.h"

#define LeftTargetSpeed 300 //Speed used on Left Motor
#define RightTargetSpeed 300 //Speed used on Right Motor

//Encoder
volatile long ticksl = 0; //Count on left motor
volatile long ticksr = 0; //Count on right motor
volatile long change = 0; //Difference between left and right motor

//Sensor Pins
int sensorPin1 = A4; //Front Left Sensor
int sensorPin2 = A3; //Left Sensor
int sensorPin3 = A5; //Right Sensor
int sensorPin4 = A1; //Front Right Sensor
int sensorPin5 = A0; //Center Sensor

//Initialize Motor
DualVNH5019MotorShield md;

//Motor Variables
int target=0;

//Sensor Variables
float mean_sensor1, mean_sensor2, mean_sensor3; //Average voltage readings
float mean_sensor4, mean_sensor5;
int dist_sensor1, dist_sensor2, dist_sensor3; //Distance in terms of grids
int dist_sensor4, dist_sensor5; 

//Calibration Variables
float d1, d2=100, d3, d4, d5; //Separate for individual calculation
float ex_dist=100; //Previous distance 

//Count Error Variables
int countrightrot = 0;  //Add Offset when rotating right
int countleftrot = 0;   //Add Offset when rotating left

//Test Flag
bool flag = true; //Spare flag, used when necessary

//Incoming command
String command_String; //Command received from Algorith/Android team

//Main program
void setup() {
  Serial.begin(9600); //Baud rate used, matching RPi side
  md.init(); //Initialize motor
  enableInterrupt(3, rising, RISING); //Interrupt pin when high on left motor
  enableInterrupt(11, rising1, RISING); //Interrupt pin when high on right motor
}

void loop() {
  // Clear outgoing buffer
  Serial.flush();
  if (Serial.available()) {
    command_String = Serial.readString(); //'a' is Android and 'c' is Algorithm
    if (command_String.charAt(0) == 'a' || command_String.charAt(0) == 'c') { 
      switch (command_String.charAt(1)) {
        //Forward
        case 'I': case 'i':
          forward(540);
          read_sensors();
          break;
        //Left
        case 'J': case 'j':
          rotate_left(90);
          read_sensors();
          break;
        //Right
        case 'L': case 'l':
          rotate_right(90);
          read_sensors();
          break;
        //Reverse
        case 'K': case 'k':
          reverse(540);
          read_sensors();
          break;
        //Read Sensor Data in terms of Grid
        case 'S': case 's':
          read_sensors();
          break;
        //45 degree left turn
        case 'U': case 'u':
          rotate_left(45);
          break;
        //45 degree right turn
        case 'O': case 'o':
          rotate_right(45);
          break;
        //Calibrate Angle/Distance
        case 'M': case 'm': case'N': case 'n':
          //for(int g=0;g<4;g++){
            calibrate();
          //}
          read_sensors();
          break;
        //In case of error in command sent
        default:
          md.setBrakes(400, -400);
          break;
      }
    }
    //Clear incoming buffer
    unsigned long now = millis ();
    while (millis () - now < 100)
      Serial.read();
  }
}

//Motor Actions
void forward(int target) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  md.setSpeeds(LeftTargetSpeed, -RightTargetSpeed);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      delay(50);
      md.setBrakes(0, 0);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed(-(LeftTargetSpeed - change * 30));
    }
  }
  delay(50);
}

void reverse(int target) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  md.setSpeeds(-LeftTargetSpeed, RightTargetSpeed);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      delay(50);
      md.setBrakes(0, 0);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed((LeftTargetSpeed - change * 30));
    }
  }
  delay(50);
}

void rotate_right(int degree) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  if(degree == 45){
    target = 360;
  }
  else if (degree == 90){
    target = 720 + 8 * countrightrot;
  }
  md.setSpeeds(LeftTargetSpeed, RightTargetSpeed);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      delay(50);
      md.setBrakes(0, 0);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed((LeftTargetSpeed - change * 30));
    }
  }
  countrightrot++;
  if (countrightrot == 4) {
    countrightrot = 0;
  }
  delay(50);
}

void rotate_left(int degree) {
  ticksl = 0;
  ticksr = 0;
  long leftPos, rightPos;
  if(degree == 45){
    target = 360;
  }
  else if (degree == 90){
    target = 730 + 3 * countleftrot;
  }
  md.setSpeeds(-LeftTargetSpeed, -RightTargetSpeed);
  while (1) {
    leftPos = ticksl;
    rightPos = ticksr;
    if (rightPos >= target) {
      md.setBrakes(400, -400);
      delay(50);
      md.setBrakes(0, 0);
      break;
    }
    else {
      change = ticksr - ticksl;
      md.setM2Speed(-(LeftTargetSpeed - change * 30));
    }
  }
  countleftrot++;
  if (countleftrot == 4) {
    countleftrot = 0;
  }
  delay(50);
}

//Sensors
void read_sensors() {
  String outputSensor = "cb";
  for (int s = 0; s < 30; s++) {
    mean_sensor1 += (float)analogRead(sensorPin1);
    mean_sensor2 += (float)analogRead(sensorPin2);
    mean_sensor3 += (float)analogRead(sensorPin3);
    mean_sensor4 += (float)analogRead(sensorPin4);
    mean_sensor5 += (float)analogRead(sensorPin5);
  }
  //Find Median Values
  mean_sensor1 /= 6144;
  mean_sensor2 /= 6144;
  mean_sensor3 /= 6144;
  mean_sensor4 /= 6144;
  mean_sensor5 /= 6144;
  dist_sensor1 = distance_1(mean_sensor1);
  dist_sensor2 = distance_2(mean_sensor2);
  dist_sensor3 = distance_3(mean_sensor3);
  dist_sensor4 = distance_4(mean_sensor4);
  dist_sensor5 = distance_5(mean_sensor5);
  //Prepare Output to RPi -> Algorithm Team
  outputSensor += String((int)((dist_sensor1 / 10) + 0.5));
  outputSensor += String((int)((dist_sensor5 / 10) + 0.5));
  outputSensor += String((int)((dist_sensor3 / 10) + 0.5));
  outputSensor += String((int)((dist_sensor4 / 10) + 0.5));
  outputSensor += String((int)((dist_sensor2 / 10) + 0.5));
  outputSensor += "\n";
  Serial.print(outputSensor);
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

//Sensor 1
float distance_1(float mean1) {
  if (mean1 > 1.54 && mean1 < 3.2) {
    return (mean1 * (-6.165) + 20.51);
  }
  else if (mean1 > 0.66 && mean1 < 1.54) {
    return (mean1 * (-22.01) + 42.93);
  }
  //  else if (mean1 > 0.3 && mean1 < 0.66) {
  //    return (mean1 * (-121.6) + 115.3);
  //  }
  else {
    return 40;
  }
}

//Sensor 2
float distance_2(float mean2) {
  if (mean2 > 1.64 && mean2 < 3.2) {
    return (mean2 * (-6.666) + 22.11);
  }
  else if (mean2 > 0.78 && mean2 < 1.64) {
    return (mean2 * (-22.07) + 47.34);
  }
  //  else if (mean2 > 0.38 && mean2 < 0.78) {
  //    return (mean2 * (-109) + 120);
  //  }
  else {
    return 40;
  }
}

//Sensor 3
float distance_3(float mean3) {
  if (mean3 > 1.64 && mean3 < 3.26) {
    return (mean3 * (-6.756) + 21.85);
  }
  else if (mean3 > 0.72 && mean3 < 1.64) {
    d3 = mean3 * (-22.53) + 49.69;
    if (d3 <= 30) {
      return d3;
    }
    else {
      return 30;
    }
  }
  //  else if (mean3 > 0.32 && mean3 < 0.72) {
  //    return (mean3 * (-106.7) + 110);
  //  }
  else {
    return 30;
  }
}

//Sensor 4
float distance_4(float mean4) {
  if (mean4 > 1.71 && mean4 < 3.1) {
    return (mean4 * (-7.040) + 23.30);
  }
  else if (mean4 > 0.8 && mean4 < 1.71) {
    return (mean4 * (-20.62) + 44.41);
  }
  //  else if (mean4 > 0.32 && mean4 < 0.8) {
  //    return (mean4 * (-94.35) + 108.8);
  //  }
  else {
    return 40;
  }
}

//Sensor 5
float distance_5(float mean5) {
  if (mean5 > 1.75 && mean5 < 2.88) {
    return (mean5 * (-18.09) + 65.28);
  }
  else if (mean5 > 1.22 && mean5 < 1.75) {
    return (mean5 * (-28.7) + 86.55);
  }
  else if (mean5 > 0.65 && mean5 < 1.22) {
    d5 = (mean5 * (-43.13) + 102.5);
    if (d5 <= 50) {
      return d5;
    }
    else {
      return d5+10;
    }
  }
  else {
    return 60;
  }
}

//Calibration
void calibrate() {
  switch (command_String.charAt(1)) {
    case 'M': case 'm':
      while (1) {
        for (int s = 0; s < 30; s++) {
          mean_sensor1 += (float)analogRead(sensorPin1);
          mean_sensor4 += (float)analogRead(sensorPin4);
        }
        mean_sensor1 /= 6144;
        mean_sensor4 /= 6144;
        d1 = distance_1(mean_sensor1);
        d4 = distance_4(mean_sensor4);
        float diff1 = d1 - d4;
        float diff2 = d4 - d1;
        delay(50);
        if ((abs(diff1) < 0.2) && (abs(diff1) > 0.03)) {
          break;
        }
        else if (diff2 > 0.2) {
          md.setSpeeds(-100, -100);
          delay(50);
          md.setBrakes(100, -100);
        }
        else if (diff1 > 0.2) {
          md.setSpeeds(100, 100);
          delay(50);
          md.setBrakes(100, -100);
        }
      }
      break;
    case 'N': case 'n':
      d2=100;
      while (1) {
        for (int s = 0; s < 30; s++) {
          mean_sensor2 += (float)analogRead(sensorPin2);
        }
        mean_sensor2 /= 6144;
        ex_dist = d2;
        d2 = distance_2(mean_sensor2);
        if (d2>3) {
          reverse(100);
          if(ex_dist<d2){
            forward(100);
            forward(100);
            break;
          }
        }
        else {
          break;
        }
      }
      md.setBrakes(400, -400);
      break;
    default:
      md.setBrakes(400, -400);
  }
}
