/*
   HTTP based RF blinds base station.

   Request URL: http://IP/Blind/ID=X/level=XXX
*/

#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <SPI.h>
#include <RFM69.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>
#include <Adafruit_NeoPixel.h>

char reqBuffer[100];
MDNSResponder mdns;


//RADIO SETTINGS
#define FREQUENCY     RF69_915MHZ

//ENCRYPT_KEY must be exactly 16 characters
#define ENCRYPT_KEY "sampleEncryptKey"

#define ADDRESS 99
#define NETWORK_ID 1
#define BURST_REPLY_TIMEOUT_MS 3000

struct PACKET
{
    uint8_t 
       group,
       value;
} packetOut;

struct inPACKET
{
  uint8_t percent;
  uint8_t voltage1;
  uint8_t voltage2;
  char charger;
} packetIn;

//BLIND DEFINITION

#define BLINDS_1   0b00000001
#define BLINDS_2   0b00000010
#define BLINDS_3   0b00000100
#define BLINDS_4   0b00001000
#define BLINDS_5   0b00010000
#define BLINDS_6   0b00100000
#define BLINDS_7   0b01000000

#define MASTER_BEDROOM_GROUP  (BLINDS_1 | BLINDS_2 | BLINDS_3)
#define ALL_GROUP (BLINDS_1 | BLINDS_2 | BLINDS_3 | BLINDS_4 | BLINDS_5 | BLINDS_6 | BLINDS_7)

RFM69 radio;

// Create an instance of the server
// specify the port to listen on as an argument
WiFiServer server(80);

#define PIN            D3

// How many NeoPixels are attached to the Arduino?
#define NUMPIXELS      2

// When we setup the NeoPixel library, we tell it how many pixels, and which pin to use to send signals.
// Note that for older NeoPixel strips you might need to change the third parameter--see the strandtest
// example for more information on possible values.
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

void setup() {
  Serial.begin(115200);
  delay(10);
  pixels.begin();
   pixels.setPixelColor(0, pixels.Color(50,0,0)); // Moderately bright green color.
    pixels.show(); // This sends the updated pixel color to the hardware.
  // Connect to WiFi network
  Serial.println();
  Serial.println();
  WiFiManager wifiManager;
  wifiManager.autoConnect("AutoConnectAP");
  Serial.println("");
  Serial.println("WiFi Connected");  

  // Start the server
  server.begin();
  Serial.println("HTTP Server Started");
  pixels.setPixelColor(0, pixels.Color(0,50,0)); // Moderately bright green color.
  pixels.show(); // This sends the updated pixel color to the hardware.

  // Print the IP address
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  Serial.println();
  Serial.println();

  radio.initialize(FREQUENCY, ADDRESS, NETWORK_ID);
  radio.encrypt(ENCRYPT_KEY);

}

int delayval = 500;
uint8_t sendAddress;

void loop() {
   pixels.setPixelColor(0, pixels.Color(0,50,0)); // Moderately bright green color.
    pixels.setPixelColor(1, pixels.Color(0,0,0)); // Moderately bright green color.
    pixels.show(); // This sends the updated pixel color to the hardware.
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  // Wait until the client sends some data
  Serial.println("new client");
//  while (!client.available()) {
//    delay(1);
//  }

  // Read the first line of the request
  String req = client.readStringUntil('\r');
  req.toCharArray(reqBuffer, 100);
  Serial.println(req);
  client.flush();

  // Match the request
  String level;
  String blindID;
  int blindInt;
  int levelInt;
  char *get = strtok(reqBuffer, " ");
  char *request = strtok(NULL, " ");
  char *rtype = strtok(NULL, " ");

  //Prepare response
  String s = "HTTP/1.1 200 OK\r\n";
  s += "Content-Type: text/html\r\n\r\n";
  s += "<!DOCTYPE HTML>\r\n<html>\r\n";

  if (request != NULL) {
    pixels.setPixelColor(0, pixels.Color(0,0,50)); // Moderately bright green color.  
    pixels.show();  
    char *part = strtok(request, "/");
    String request = String(part);
    Serial.println(request);
    bool seenBlind = false;
    while (part) { // While there is a section to process...

      if (seenBlind) {
        if (!strncmp(part, "level=", 6)) { // We have the ID
          level = String(part + 6);
          Serial.print("Level: ");
          Serial.println(level);
          s += "Level: ";
          s += level;
          s += "<br>";
          packetOut.value = level.toInt();
        }
        else if (!strncmp(part, "ID=", 3)) { // We have the ID
          blindID = String(part + 3);
          Serial.print("Blind ID: ");
          Serial.println(blindID); 
          blindInt = blindID.toInt();
          Serial.println(blindInt);
          if (blindID == "MBR"){
            blindInt = 254;
            s += "Blind ID: 1,2,3<br>";
            s += "Group: MBR<br>";
          }
          else if (blindID == "ALL")
          {
            blindInt = 255;
            s += "Blind ID: ALL<br>";
            s += "Group: ALL<br>";
          }
          else{
            blindInt = blindID.toInt();
            Serial.println(blindInt);
            s += "Blind ID: ";
            s += blindID;
            s += "<br>";
            s += "Group: Null<br>";
          }

          switch(blindInt){
            case 1:
            Serial.println("Case 1.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 2:
            Serial.println("Case 2.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 3:
            Serial.println("Case 3.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 4:
            Serial.println("Case 4.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 5:
            Serial.println("Case 5.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 6:
            Serial.println("Case 6.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 7:
            Serial.println("Case 7.");
              packetOut.group = null;
              sendAddress = blindInt;
              break;
            case 254:
            Serial.println("Case MBR.");
              packetOut.group = MASTER_BEDROOM_GROUP;
              sendAddress = 255;
              break;
            case 255:
            Serial.println("Case ALL.");
              packetOut.group = ALL_GROUP;
              sendAddress = 255;
              break;
            
          }
        }

      } else if (!strcmp(part, "Blind")) {
        seenBlind = true;
      }

      part = strtok(NULL, "/");
    }

    if ((level != null) && (blindID != null)) {

      Serial.print("Sending command to Group ");
      Serial.print(packetOut.group);
      Serial.print(". Sending command to Blind ");
      Serial.print(sendAddress);
      Serial.print(". Set to angle: ");
      Serial.print(packetOut.value);
      Serial.print("...");


      bool replied = false;
        radio.listenModeSendBurst(sendAddress, &packetOut, sizeof(packetOut));   
//        long start = millis();
//        while ((millis() - start) < BURST_REPLY_TIMEOUT_MS) {
//          if (radio.receiveDone()) {
//            struct inPACKET
//            *myPacket = (struct inPACKET *)radio.DATA;
//            Serial.println((char*)radio.DATA);
//            Serial.println("Success");
//            s += "Battery: ";
//            s += myPacket->percent;
//            Serial.println(myPacket->percent);
//            s += "<br>Voltage: ";            
//            s += myPacket->voltage;
//            Serial.println(myPacket->voltage);
//            s += "<br>Charger: ";
//            s += myPacket->charger;
//            Serial.println((const char *)myPacket->charger);
//            s += "<br>Success.<br>";
//            replied = true;
//            pixels.setPixelColor(1, pixels.Color(0,50,0)); // Moderately bright green color.
//    pixels.show(); // This sends the updated pixel color to the hardware.
//            break;
//          }
//        }
            int delayTime = 0;
             while ( delayTime < BURST_REPLY_TIMEOUT_MS) {
          if (radio.receiveDone()) {
            struct inPACKET
            *myPacket = (struct inPACKET *)radio.DATA;
            Serial.println((char*)radio.DATA);
            Serial.println("Success");
            s += "Battery: ";
            s += myPacket->percent;
            Serial.println(myPacket->percent);
            Serial.println((int)myPacket->voltage1);            
            Serial.println((int)myPacket->voltage2);
            float voltage = ((((float)myPacket->voltage1)*100) + (float)myPacket->voltage2)/1000;
            s += "<br>Voltage: ";            
            s += voltage;
            Serial.println(voltage);
            s += "<br>Charger: ";
            s += myPacket->charger;
            Serial.println((char)myPacket->charger);            
            
            s += "<br>Success.<br>";
            replied = true;
            pixels.setPixelColor(1, pixels.Color(0,50,0)); // Moderately bright green color.
    pixels.show(); // This sends the updated pixel color to the hardware.
            break;
          }
          delay(1);
          delayTime++;
        }

        if (!replied) {
          Serial.println("Failed"); 
            s += "Failed.<br>";
            pixels.setPixelColor(1, pixels.Color(50,0,0)); // Moderately bright green color.
            pixels.show(); // This sends the updated pixel color to the hardware.

        }

    }
  }
  else {
    pixels.setPixelColor(0, pixels.Color(50,0,0)); // Moderately bright green color.
    pixels.show(); // This sends the updated pixel color to the hardware.
    Serial.println("Invalid request.");
    s += "Invalid request.<br>";
    client.stop();
    return;
  }
  
  s += "</html>\n";
  client.flush();

  // Send the response to the client
  //client.print(s);
  client.print(s);
  delay(1);
  Serial.println("Client Disconnected");
  Serial.println();
  // The client will actually be disconnected
  // when the function returns and 'client' object is detroyed
}

