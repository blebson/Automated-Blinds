#include <RFM69.h>
#include <SPI.h>
#include <LowPower.h>
#include <Servo.h>

// Uncomment the appropriate frequency for your hardware
//#define FREQUENCY     RF69_433MHZ
//#define FREQUENCY     RF69_868MHZ
#define FREQUENCY     RF69_915MHZ
//#define IS_RFM69HW    //uncomment only for RFM69HW! Leave out if you have RFM69W!

//ENCRYPT_KEY must match gateway value and must be exactly 16 characters
#define ENCRYPT_KEY "sampleEncryptKey"
#define ADDRESS 4
#define NETWORK_ID 1

//#define BLINDS_1   0b00000001
//#define BLINDS_2   0b00000010
//#define BLINDS_3   0b00000100
#define BLINDS_4   0b00001000
//#define BLINDS_5   0b00010000
//#define BLINDS_6   0b00100000
//#define BLINDS_7   0b01000000
#define MY_NODE_FLAG  BLINDS_4

struct PACKET
{
  uint8_t percent;
  uint8_t voltage1;
  uint8_t voltage2;
  char charger;
} packetOut;

struct inPACKET
{
  uint8_t
  group,
  value;
} packetIn;

RFM69 radio;
const int servoPowerPin = 5;
const int servoPin = 6;
const int okStatus = 16;
const int chStatus = 15;
int currentAngle = 0;
Servo blindServo;


//Voltage Meter correction value: (Measured Voltage of Input) / (Voltage Reading when correctionValue = 1)
float correctionValue = 1.0000;

void setup() {
  Serial.begin(115200);

  radio.initialize(FREQUENCY, ADDRESS, NETWORK_ID);
  radio.encrypt(ENCRYPT_KEY);  
  radio.listenModeEnd();
  //Serial.print("Network ID: ");
  //Serial.print(NETWORK_ID);
  //Serial.print(", Address: ");
  //Serial.println(ADDRESS);  
  pinMode(servoPowerPin, OUTPUT);  
  pinMode(A0, INPUT);
  pinMode(okStatus, INPUT_PULLUP);
  pinMode(chStatus, INPUT_PULLUP);
  blindServo.attach(servoPin);
}

void loop() {
  int angle;

  if (radio.receiveDone()) {
    if (radio.ACKRequested()) {
      radio.sendACK();
    }
    //Serial.println("Received a normal message");
    //Serial.println((char*)radio.DATA);
    Serial.flush();
  }

  radio.listenModeStart();

  digitalWrite(servoPowerPin, LOW);  
  delay(5);
  //Serial.println("Entering low-power listen mode...");
  Serial.flush();
  LowPower.powerDown(SLEEP_FOREVER, ADC_OFF, BOD_OFF);
  // Woke up, check for a message
  //Serial.println("Woke up!");
  detachInterrupt(1);


  int sensorValue = analogRead(A0);
  ////Serial.println(sensorValue);
  //      delay(1);
  sensorValue = analogRead(A0);
  //    //Serial.println(sensorValue);
  // Convert the analog reading (which goes from 0 - 1023) to a voltage (0 - 5V):
  float voltage = ((sensorValue * (3.3 / 1023.0)) * 2);
  voltage *=  correctionValue;
  //    //Serial.println(voltage);
  voltage *= 1000;
  //    //Serial.println(voltage);
  float percent = (((voltage / 1000) - 3.3) / 0.9125) * 100;
  //    //Serial.print("Battery Voltage: ");
  //    //Serial.println(voltage);
  //    //Serial.print("Battery Percent: ");
  //    //Serial.println(percent);
  packetOut.percent = (int)percent;
  packetOut.voltage1 = (int)(voltage / 100);
  packetOut.voltage2 = (int)(voltage - (packetOut.voltage1 * 100));
  //    //Serial.print("Battery Percent: ");
  //    //Serial.println(packetOut.percent);
  //    //Serial.print("Battery Voltage1: ");
  //    //Serial.println(packetOut.voltage1);
  //    //Serial.print("Battery Voltage2: ");
  //    //Serial.println(packetOut.voltage2);


  //Check the status of the solar charger
  if (digitalRead(okStatus) == LOW && digitalRead(chStatus) == HIGH) {
    packetOut.charger = 'O';
    //      //Serial.println(digitalRead(okStatus));
    //      //Serial.println(digitalRead(chStatus));
    //      //Serial.println("Solar: Fully Charged");
  }
  else if (digitalRead(okStatus) == HIGH && digitalRead(chStatus) == LOW) {
    packetOut.charger = 'C';
    //      //Serial.println(digitalRead(okStatus));
    //      //Serial.println(digitalRead(chStatus));
    //      //Serial.println("Solar: Charging");
  }
  else if (digitalRead(okStatus) == LOW && digitalRead(chStatus) == LOW) {
    packetOut.charger = 'E';
    //      //Serial.println(digitalRead(okStatus));
    //      //Serial.println(digitalRead(chStatus));
    //      //Serial.println("Solar: ERROR");
  }
  else {
    packetOut.charger = 'N';
    //      //Serial.println(digitalRead(okStatus));
    //      //Serial.println(digitalRead(chStatus));
    //      //Serial.println("Solar: Not Charging");
    //      //Serial.println(packetOut.charger);
  }

  int initialPosition = blindServo.read();

  uint8_t from = 0;
  long burstRemaining = 0;
  if (radio.DATALEN > 0) {
      ////Serial.println("Received a message in listen mode");
      ////Serial.println((char*)radio.DATA);

      if (radio.TARGETID == 255)
      {
        struct inPACKET
        *myPacket = (struct inPACKET *)radio.DATA;
        angle = (myPacket->value);
        if (myPacket->group & MY_NODE_FLAG)
        {          
          digitalWrite(servoPowerPin, HIGH);
          delay(75);
          currentAngle = blindServo.read();
          //Serial.print("Angle: ");
          //Serial.println(angle);
          while (currentAngle != angle) {
            if (currentAngle > angle) {
              currentAngle -= 1;
              blindServo.write(currentAngle);
              delay(15);
            }
            else if (currentAngle < angle) {
              currentAngle += 1;
              blindServo.write(currentAngle);
              delay(15);
            }
          }
        }
      }
      else if ( radio.DATA != null && radio.TARGETID == ADDRESS) {        
        struct inPACKET
        *myPacket = (struct inPACKET *)radio.DATA;
        angle = (myPacket->value);
        if ( angle != 0 ) {
          digitalWrite(servoPowerPin, HIGH);
          delay(75);
          currentAngle = blindServo.read();
          //Serial.print("Angle: ");
          //Serial.println(angle);
          while (currentAngle != angle) {
            if (currentAngle > angle) {
              currentAngle -= 1;
              blindServo.write(currentAngle);
              delay(15);
            }
            else if (currentAngle < angle) {
              currentAngle += 1;
              blindServo.write(currentAngle);
              delay(15);
            }
          }
        }
      }
      delay(125);
      Serial.flush();
      from = radio.SENDERID;
      burstRemaining = radio.LISTEN_BURST_REMAINING_MS;
    }

  // Radio goes back to standby, ready for normal operations
  radio.listenModeEnd();

  if (from) {
    while (burstRemaining > 0) {
      LowPower.powerDown(SLEEP_60MS, ADC_OFF, BOD_OFF);
      burstRemaining -= 60;
    }
    LowPower.powerDown(SLEEP_30MS, ADC_OFF, BOD_OFF);
    radio.send(from, &packetOut, sizeof(packetOut));
  }
}
