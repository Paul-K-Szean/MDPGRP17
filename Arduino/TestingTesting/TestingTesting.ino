#include <EnableInterrupt.h>

#define K1L 16.08 //11.27992128
#define K1R 3.95 //13.34972528

#define K2L -25.38923975 //-19.2902999
#define K2R -6.45 //-24.91948717

#define K3L 9.5 //8.247302
#define K3R 2.5 //11.679094

// Speed taget
#define LeftTargetSpeed 100
#define RightTargetSpeed 100
#define mult 0.97
#define mult2 0.97

#include "DualVNH5019MotorShield.h"
DualVNH5019MotorShield md;

volatile long pwm_valuel = 0;
volatile long prev_timel = 0;
volatile long pwm_valuer = 0;
volatile long prev_timer = 0;

float err1l = 0;
float err1r = 0;
float err2l = 0;
float err2r = 0;
float err3l = 0;
float err3r = 0;

float newspeedl = 0;
float newspeedr = 0;
float rpm1 = 0;
float rpm2 = 0;

float pwmArrayl[11];
float pwmArrayr[11];
float median_pwml;
float median_pwmr;

int count = 0;

bool flag = true;

void setup() {
  Serial.begin(9600);
  md.init();
  enableInterrupt(3, rising, RISING);
  enableInterrupt(11, rising1, RISING);
  delay(3000);
  set_rpm(LeftTargetSpeed, RightTargetSpeed);
}

void loop() {
    rpm_finder();
    if (rpm1 < 120 && rpm2 < 120) {
      compute_pid();
      err1l = LeftTargetSpeed - rpm1;
      err1r = RightTargetSpeed - rpm2;
      newspeedl = newspeedl + (K1L * err1l) + (K2L * err2l) + (K3L * err3l);
      newspeedr = newspeedr + (K1R * err1r) + (K2R * err2r) + (K3R * err3r);
      if(count<140){
        md.setSpeeds(newspeedl, -(newspeedr*mult));
        count++;
      }
      else{
        md.setSpeeds(newspeedl, -(newspeedr*mult2));
      }
      count++;
    }
}

void rpm_finder() {
  for (int r = 0; r < 11; r++) {
    pwmArrayl[r] = pwm_valuel;
    pwmArrayr[r] = pwm_valuer;
  }
  median_pwml = find_median(pwmArrayl);
  median_pwmr = find_median(pwmArrayr);
  rpm1 = calc_rpm(median_pwml);
  rpm2 = calc_rpm(median_pwmr);
}

void compute_pid() {
  err3l = err2l;
  err3r = err2r;
  err2l = err1l;
  err2r = err1r;
}

void set_rpm(float rpml, float rpmr) {
  newspeedl = (rpml + 5.712) / 0.3885;
  newspeedr = (rpmr + 9.330) / 0.3591;
  md.setSpeeds(newspeedl, -newspeedr);
  delay(200);
}

float calc_rpm(float in_pwm) {
  float rpm;
  rpm = 60000 / (in_pwm * 1.1245);
  return rpm;
}

//Find Median
float find_median(float pwmArray[]) {
  float tmp;
  for (int i = 0; i < 11; i++) {
    for (int j = i; j > 0; j--) {
      if (pwmArray[j] < pwmArray[j - 1]) {
        tmp = pwmArray[j];
        pwmArray[j] = pwmArray[j - 1];
        pwmArray[j - 1] = tmp;
      }
      else {
        break;
      }
    }
  }
  return pwmArray[6];
}

//PWM Calculation
void rising() {
  enableInterrupt(3, falling, FALLING);
  prev_timel = micros();
}

void rising1() {
  enableInterrupt(11, falling1, FALLING);
  prev_timer = micros();
}

void falling() {
  enableInterrupt(3, rising, RISING);
  pwm_valuel = micros() - prev_timel;
}

void falling1() {
  enableInterrupt(11, rising1, RISING);
  pwm_valuer = micros() - prev_timer;
}

