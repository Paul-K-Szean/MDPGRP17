#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>
#include <PID_v1.h>
#include <SharpIR.h>

#define s1 A0
#define s2 A1
#define s3 A2
#define s4 A3
#define s5 A4
#define s6 A5

SharpIR Rsensor =  SharpIR(s1, 10801);
SharpIR Lsensor =  SharpIR(s2, 10802);
SharpIR FRsensor =  SharpIR(s3, 10803);
SharpIR FLsensor =  SharpIR(s4, 10804);
SharpIR FMsensor =  SharpIR(s5, 10805);
SharpIR LGSensor =  SharpIR(s6, 20150);

//E1 - Left Motor
int encoderpin1a = 3;
int encoderpin1b = 5;

//E2 - Right Motor
int encoderpin2a = 11;
int encoderpin2b = 13;

volatile long delta = 0;
volatile long ticksmoved1 = 0;
volatile long ticksmoved2 = 0;

unsigned long currentpulsetime1 = 0;
unsigned long currentpulsetime2 = 0;

unsigned long previoustime1 = 0;
unsigned long previoustime2 = 0;

double motor1rpm;
double motor2rpm;
double newmotor1rpm;
double newmotor2rpm;

/*-------Adjustments-------*/
int adjustleft = 0;
int adjustright = 0;

DualVNH5019MotorShield md;

void setup() {
  pinMode(encoderpin1a, INPUT);
  pinMode(encoderpin1b, INPUT);
  pinMode(encoderpin2a, INPUT);
  pinMode(encoderpin2b, INPUT);
  enableInterrupt(encoderpin1a, encoder1change, RISING);
  enableInterrupt(encoderpin2a, encoder2change, RISING);
  md.init();
  Serial.begin(115200);
}


/*--------------------FUNCTIONS--------------------*/

void setTimerInterrupt() {
  cli();          // disable global interrupts
  // Timer/Counter Control Registers
  TCCR1A = 0;     // set entire TCCR1A register to 0
  TCCR1B = 0;     // same for TCCR1B

  // set compare match register to desired timer count:
  OCR1A = 1562;   // scale = 1024, OCR1A = (xxx / 64 / 1024)

  // turn on CTC mode:
  TCCR1B |= (1 << WGM12);
  // Set CS10 and CS12 bits for 1024 prescaler:
  TCCR1B |= (1 << CS10);
  TCCR1B |= (1 << CS12);

  //  enable timer compare interrupt:
  //  Timer/Counter Interrupt Mask Register
  //  Compare A&B interrupts
  TIMSK1 |= (1 << OCIE1A);
  sei();          // enable global interrupts
}

void detachTimerInterrupt() {
  cli();
  TIMSK1 &= 0; // disable
  sei();
}

ISR(TIMER1_COMPA_vect) {
  //    md.setM2Speed((motor_speed - delta*5) * motor_direction);
}

void moveForward(float dist, int targetRPM, int sensorDist) {
  //setTimerInterrupt();
  int motor_speed = 300;
  int motor_direction = 1;
  ticksmoved1 = 0;
  ticksmoved2 = 0;
  long leftPosition, rightPosition;
  int targetTicks = 333 * dist;
  md.setSpeeds(motor_speed, motor_speed);
  //md.setM2Speed(300);

  while (1) {
    analyseMotor();
    rightPosition = ticksmoved1;
    leftPosition = ticksmoved2;
    if (rightPosition >= targetTicks - 70) {
      md.setBrakes(400, 400);
      delay(100);
      md.setBrakes(0, 0);
      break;
    } else {
      delta = ticksmoved2 - ticksmoved1;
      md.setM2Speed((motor_speed - delta * 50) * motor_direction);
    }
  }
  //detachTimerInterrupt();


  //  while((abs(ticksmoved1)<=(266*dist)) && abs(ticksmoved2)<=(266*dist)){
  //    analyseMotor();
  //
  //    double targetrpm1=targetRPM;
  //    double targetrpm2=targetRPM;
  //
  //    PID Motor1PID(&motor1rpm,&newmotor1rpm,&targetrpm1,1.4828,0.035979,0.00267,DIRECT);
  //    PID Motor2PID(&motor2rpm,&newmotor2rpm,&targetrpm2,1.4928,0.035726,0.00592,DIRECT);
  //
  ////    PID Motor1PID(&motor1rpm,&newmotor1rpm,&targetrpm1,0.2688,0.24405,0.4579,DIRECT);
  ////    PID Motor2PID(&motor2rpm,&newmotor2rpm,&targetrpm2,0.2082,0.3168,0.5320,DIRECT);
  //    Motor1PID.SetOutputLimits(70,200);
  //    Motor1PID.SetSampleTime(10);
  //    Motor1PID.SetMode(AUTOMATIC);
  //
  //    Motor2PID.SetOutputLimits(70,200);
  //    Motor2PID.SetSampleTime(10);
  //    Motor2PID.SetMode(AUTOMATIC);
  //
  //    motor1rpm=getRPM1();
  //    motor2rpm=getRPM2();
  //
  //    Motor1PID.Compute();
  //    Motor2PID.Compute();
  //
  //    if(sensorDist==0){
  //      md.setSpeeds(motor1rpmtospeed(newmotor1rpm),motor2rpmtospeed(newmotor2rpm));
  //    } else {
  //      if(FrontMiddle(sensorDist)!=true){
  //        md.setSpeeds(motor1rpmtospeed(newmotor1rpm),motor2rpmtospeed(newmotor2rpm));
  //      } else {
  //        stop_temp();
  //        break;
  //      }
  //    }
  //  }
  //  stop_temp();
}

void turn (float degree, int targetRPM, boolean dir) {
  ticksmoved1 = 0;
  ticksmoved2 = 0;
  //  while ((abs(ticksmoved1) <= (3.85 * degree)) && abs(ticksmoved2) <= (3.85 * degree)) {
  //
  //
  //    double targetrpm1 = targetRPM;
  //    double targetrpm2 = targetRPM;
  //
  //    PID Motor1PID(&motor1rpm, &newmotor1rpm, &targetrpm1, 1.4828, 0.035979, 0.00267, DIRECT);
  //    PID Motor2PID(&motor2rpm, &newmotor2rpm, &targetrpm2, 1.4928, 0.035926, 0.0059, DIRECT);
  //
  //    Motor1PID.SetOutputLimits(100, 200);
  //    Motor1PID.SetSampleTime(10);
  //    Motor1PID.SetMode(AUTOMATIC);
  //
  //    Motor2PID.SetOutputLimits(100, 200);
  //    Motor2PID.SetSampleTime(10);
  //    Motor2PID.SetMode(AUTOMATIC);
  //
  //    motor1rpm = getRPM1();
  //    motor2rpm = getRPM2();
  //
  //    Motor1PID.Compute();
  //    Motor2PID.Compute();
  //    if (dir == true) {
  //      md.setSpeeds(-motor1rpmtospeed(newmotor1rpm), motor2rpmtospeed(newmotor2rpm)); // turn right
  //      //Serial.println("turn Right");
  //    }
  //    if (dir == false) {
  //      md.setSpeeds(motor1rpmtospeed(newmotor1rpm), -motor2rpmtospeed(newmotor2rpm)); // turn left
  //      //Serial.println("turn left");
  //    }
  //  }
  //  md.setBrakes(400, 400);
  //  delay(1000);
  //  md.setBrakes(0, 0);
  //stop_temp();
}

void calibrate()
{
  float threshold = 0.5;
  float fLeft = 0;
  float fRight = 0;
  int count = 0;

  while (1) {
    fLeft = FLsensor.distance();
    fRight = FRsensor.distance();

    if (abs(fLeft - fRight) < threshold)
    {
      count++;
      if (count == 3) {
        break;
        stop_temp();
      }
    }
    else if (fLeft - fRight > threshold) {
      md.setM1Speed(-100);
      md.setM2Speed(100);
      delay(50);
      stop_temp();
      count = 0;
    }
    else if (fLeft - fRight < -threshold) {
      md.setM1Speed(100);
      md.setM2Speed(-100);
      delay(50);
      stop_temp();
      count = 0;
    }
  }
}

void stop_temp() {
  md.setM1Brake(400);
  md.setM2Brake(400);
  delay(500);
  ticksmoved1 = 0;
  ticksmoved2 = 0;
}

void rturn() {
  //turn(100, 100, true);
  int motor_speed = 300;
  int motor_direction = -1;
  ticksmoved1 = 0;
  ticksmoved2 = 0;
  long leftPosition, rightPosition;
  int targetTicks = 436;
  md.setSpeeds(-300, 300);
  //md.setM2Speed(300);

  while (1) {
    analyseMotor();
    rightPosition = ticksmoved1;
    leftPosition = ticksmoved2;
    if (leftPosition >= targetTicks - 70) {
      md.setBrakes(400, 400);
      delay(1000);
      md.setBrakes(0, 0);
      break;
    } else {
      delta = ticksmoved1 - ticksmoved2;
      md.setM1Speed((300 - delta * 8) * motor_direction);
    }
  }
}
void lturn() {
  //  turn(100, 100, false);
  int motor_speed = 300;
  int motor_direction = 1;
  ticksmoved1 = 0;
  ticksmoved2 = 0;
  long leftPosition, rightPosition;
  int targetTicks = 436;
  md.setSpeeds(300, -300);
  //md.setM2Speed(300);

  while (1) {
    analyseMotor();
    rightPosition = ticksmoved1;
    leftPosition = ticksmoved2;
    if (leftPosition >= targetTicks - 70) {
      md.setBrakes(400, 400);
      delay(1000);
      md.setBrakes(0, 0);
      break;
    } else {
      delta = ticksmoved1 - ticksmoved2;
      md.setM1Speed((300 - delta * 8) * motor_direction);
    }
  }
}

void uturn() {
  //  turn(200, 100, false);
  int motor_speed = 300;
  int motor_direction = -1;
  ticksmoved1 = 0;
  ticksmoved2 = 0;
  long leftPosition, rightPosition;
  int targetTicks = 836;
  md.setSpeeds(-300, 300);
  //md.setM2Speed(300);

  while (1) {
    analyseMotor();
    rightPosition = ticksmoved1;
    leftPosition = ticksmoved2;
    if (leftPosition >= targetTicks - 70) {
      md.setBrakes(400, 400);
      delay(1000);
      md.setBrakes(0, 0);
      break;
    } else {
      delta = ticksmoved1 - ticksmoved2;
      md.setM1Speed((300 - delta * 8) * motor_direction);
    }
  }
}
void taskA6() {
  moveForward(15, 125, 15);
  turn(45, 100, true);
  moveForward(4, 125, 0);
  turn(100, 100, false);
  moveForward(4, 125, 0);
  turn(45, 100, true);
  //moveForward(5,125,15);
  delay(10000);
}


void taskA5() {
  moveForward(15, 125, 10);
  turn(100, 100, true);
  moveForward(3, 125, 0);
  turn(100, 100, false);
  moveForward(5, 125, 0);
  turn(100, 100, false);
  moveForward(3, 125, 0);
  turn(100, 100, true);
  //moveForward(5,125,15);
  delay(10000);
}

void analyseMotor() {
  Serial.print("Left Ticks: ");
  Serial.print(ticksmoved1);
  Serial.print("  Right Ticks: ");
  Serial.print(ticksmoved2);
  Serial.print("         Delta: ");
  Serial.print(delta);
  Serial.print("         Left Actual : ");
  Serial.print(motor1rpm);
  Serial.print("  Right Actual : ");
  Serial.print(motor2rpm);
  Serial.print("         Left Speed:");
  Serial.print(newmotor1rpm);
  Serial.print("   Right Speed:");
  Serial.println(newmotor2rpm);
}
/*--------------------SENSOR--------------------*/
boolean Left(double dist) {
  if (Lsensor.distance() <= dist)
  {
    //Serial.println("Left");
    return true;
  }
  return false;
}

boolean FrontLeft(double dist) {
  if (FLsensor.distance() <= dist)
  {
    //Serial.println("FrontLeft");
    return true;
  }
  return false;
}

boolean FrontMiddle(double dist) {
  if (FMsensor.distance() <= dist)
  {
    //Serial.println("FrontMiddle");
    return true;
  }
  return false;
}

boolean FrontRight(double dist) {
  if (FRsensor.distance() <= dist)
  {
    //Serial.println("FrontRight");
    return true;
  }
  return false;
}

boolean Right(double dist) {
  if (Rsensor.distance() <= dist)
  {
    //Serial.println("Right");
    return true;
  }
  return false;
}

boolean LongSensor(double dist) {
  if (LGSensor.distance() <= dist)
  {
    //Serial.println("Long");
    return true;
  }
  return false;
}

/*--------------------ENCODER--------------------*/
void encoder1change()
{
  if (digitalRead(encoderpin1b) == LOW)
  {
    ticksmoved1++;
  }
  else
  {
    ticksmoved1++;
  }
  //currentpulsetime1 = micros() - previoustime1;
  //previoustime1 = micros();
}

void encoder2change()
{
  if (digitalRead(encoderpin2b) == LOW)
  {
    ticksmoved2++;
  }
  else
  {
    ticksmoved2++;
  }
  //currentpulsetime2 = micros() - previoustime2;
  //previoustime2 = micros();
}

/* --------------- SPEED to RPM--------------- */
//Left Motor Forward Speed to RPM (-400 to 0)
double motor1speedtorpm(int speed)
{
  return (0.4229 * speed) + 14.414;
}
//Right Motor Forward Speed to RPM (0 to 400)
double motor2speedtorpm(int speed)
{
  return (0.4156 * speed) + 17.601;
}

/* --------------- RPM to SPEED--------------- */
//Left Motor Forward RPM to Speed (-400 to 0)
int motor1rpmtospeed(double rpm)
{
  //return (rpm+15.599)/0.394;
  return (rpm * 2.4042) + 42.501;
}

//Right Motor Forward RPM to Speed (0 to 400)
int motor2rpmtospeed(double rpm)
{
  //return (rpm+15.904)/0.368;
  return (rpm * 2.363) + 23.166;

}

double getRPM1()
{
  if (currentpulsetime1 == 0)
  {
    return 0;
  }
  return 60000 / (((currentpulsetime1) / 1000.0) * 562.215);
}

double getRPM2()
{
  if (currentpulsetime2 == 0)
  {
    return 0;
  }
  return 60000 / (((currentpulsetime2) / 1000.0) * 562.215);
}

/*--------------------COMMUNICATION--------------------*/
//int SIZE = 40;
//char strValue[40] = {};
char command;
int p1 = -1;
int p2 = -1;


//void stringTOCommand(char input[]) {
//  command = input[0];
//  String temp = "";
//  String temp1 = "";
//  int flag = 0;
//  for (int x = 0; x < SIZE; x++) {
//    if (input[x] == ':') {
//      flag++;
//      continue;
//    }
//    if (input[x])
//      if (isDigit(input[x])) {
//        if (flag == 1)
//          temp += input[x];
//        else if (flag == 2)
//          temp1 += input[x];
//      }
//  }
//  if (temp != "")
//    p1 = temp.toInt();
//  if (temp1 != "")
//    p2 = temp1.toInt();
//}
//
//void readSerial(int mode = 0) {
//  int index = 0;
//  for (int x = 0; x < SIZE; x++)
//    strValue[x] = 0;
//  p1 = -1;
//  p2 = -1;
//  command = 'x';
//  if (Serial.available() == 0)
//    return;
//  while (Serial.available() > 0) {
//    delay(1);
//    char ch = Serial.read();
//    if (index > SIZE)
//      continue;
//    strValue[index++] = ch;
//  }
//  command = strValue[0];
//  if (index == 1 ) {
//    return;
//  }
//  String temp = "";
//  String temp1 = "";
//  int flag = 0;
//  for (int x = 0; x < SIZE; x++) {
//    if (strValue[x] == ':') {
//      flag++;
//      continue;
//    }
//    if (strValue[x])
//      if (isDigit(strValue[x])) {
//        if (flag == 1)
//          temp += strValue[x];
//        else if (flag == 2)
//          temp1 += strValue[x];
//      }
//  }
//  if (temp != "")
//    p1 = temp.toInt();
//  if (temp1 != "")
//    p2 = temp1.toInt();
//}

/*--------------------MAIN--------------------*/
int done = 0;
void loop() {
  //analyseMotor();
  //Serial.println(s1);
  //SensorReadDistance();
  //  SerialEvents();
  //  moveForward(6, 100, 15);
  //  uturn();
  moveForward(15, 100, 15);
  ////  lturn();
  //  moveForward(5, 100, 15);
  ////  lturn();

  ////  lturn();
  uturn();
  //  //calibrate();
  //  //delay(10000);

  delay(1000);
}

void SensorRead() {
  double s1 = Rsensor.distance();
  double s2 = Lsensor.distance();
  double s3 = FRsensor.distance();
  double s4 = FLsensor.distance();
  double s5 = FMsensor.distance();
  double s6 = LGSensor.distance();
  int se1, se2, se3, se4, se5, se6;
  se1 = (s1 + 4) / 10;
  se2 = (s2 + 1.5) / 10;
  se3 = (s3 + 5) / 10;
  se4 = (s4 + 5) / 10;
  se5 = (s5 + 1.5) / 10;
  se6 = (s6 + 10) / 10;
  Serial.print(se1);
  Serial.print(",");
  Serial.print(se2);
  Serial.print(",");
  Serial.print(se3);
  Serial.print(",");
  Serial.print(se4);
  Serial.print(",");
  Serial.print(se5);
  Serial.print(",");
  Serial.println(se6);
}

void SensorReadDistance() {
  double s1 = Rsensor.distance();
  double s2 = Lsensor.distance();
  double s3 = FRsensor.distance();
  double s4 = FLsensor.distance();
  double s5 = FMsensor.distance();
  double s6 = LGSensor.distance();
  int se1, se2, se3, se4, se5, se6;
  Serial.println(s5);
}
void SerialEvents() {
  command = 'X';
  //for (int x = 0; x < 10; x++)
  //  strValue[x] = 0;

  //int delay1 = 40;
  //long delayTiming = 0;
  int step_int = 0;

  if (Serial.available() > 0) {
    command = Serial.read();
    switch (command) {
      case 'S':
        SensorRead();
        break;
      case 'M':
        step_int = Serial.parseInt();
        moveForward(step_int, 125, 8);
        //calibrate();
        SensorRead();
        break;
      case 'R':
        rturn();
        //calibrate();
        SensorRead();
        break;
      case 'L':
        lturn();
        //calibrate();
        SensorRead();
        break;
      case 'U':
        uturn();
        //calibrate();
        SensorRead();
        break;
      default:
        break;
    }
    Serial.flush();
  }
}


