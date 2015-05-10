/*
  WiFi Web Server, connected to Servo. 

 */
#include <Servo.h>
#include <SPI.h>
#include <WiFi.h>


#define LED 7      // the number of the LED pin
#define SERVO 3 // Number of hte servo pin
#define TIMEOUT_MS 15000 // How long before the lid closes again.
String latestRequest = "";
boolean reading = false;

char ssid[] = "MassChallenge Now";      // your network SSID (name) 
char pass[] = "hackster15";   // your network password
int keyIndex = 0;                 // your network key Index number (needed only for WEP)
int swearCounts = 0;

int status = WL_IDLE_STATUS;

Servo servo;
int lidClosed = 105; 
int lidOpen = 150;
int goalDegree;
int commandedDegree;
int openerTimeout = 0;
int slewRate = 2;

WiFiServer server(80);

void setup() {
  //Initialize serial and wait for port to open:
  Serial.begin(9600); 
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  
  //Setup Analog IO
  pins_init();
  servo.attach(3);
  servo.write(lidClosed);
  
  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present"); 
    // don't continue:
    while(true);
  } 

  String fv = WiFi.firmwareVersion();
  if( fv != "1.1.0" )
    Serial.println("Please upgrade the firmware");
  
  // attempt to connect to Wifi network:
  while ( status != WL_CONNECTED) { 
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:    
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  } 
  server.begin();
  // you're connected now, so print out the status:
  printWifiStatus();
}


void loop() {
  // listen for incoming clients
  WiFiClient client = server.available();
  if (client) {
    Serial.println("new client");
    // an http request ends with a blank line
    boolean currentLineIsBlank = true;
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        
        latestRequest += c;
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank, the http request has ended,
        // so you can send a reply
        if (c == '\n' && currentLineIsBlank) {
          // send a standard http response header
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text/html");
          client.println("Connection: close");  // the connection will be closed after completion of the response
          //client.println("Refresh: 50 ");  // refresh the page automatically every 5 sec
          client.println();
          client.println("<!DOCTYPE HTML>");
          client.println("<html>");
          client.println("<p> "+String(swearCounts)+" swear report(s) received!</p>");
          client.println("<br />");       
          
          client.println("</html>");
          parseServoRequest(latestRequest);
          
          latestRequest = "";
        
           break;
        }
        if (c == '\n') {
          // you're starting a new line
          currentLineIsBlank = true;
        } 
        else if (c != '\r') {
          // you've gotten a character on the current line
          currentLineIsBlank = false;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    latestRequest = "";
    // close the connection:
    client.stop();
    Serial.println("client disonnected");
  }
  
  doServoWork();
  
  //Close the lid if it's been open too long:
  if (openerTimeout > TIMEOUT_MS){
      goalDegree = lidClosed ;
      turnOffLED();
  } else {
    delay(1);
    openerTimeout++;
  }
  
}

void doServoWork(){
  if (goalDegree != commandedDegree){    
      if(abs(commandedDegree - goalDegree) < slewRate){
        commandedDegree = goalDegree;
        
      } else { //Don't command too big a change too fast or cheap servo won't like it. 
        if (goalDegree > commandedDegree) commandedDegree = commandedDegree + slewRate;
        else if (goalDegree <= commandedDegree) commandedDegree = commandedDegree - slewRate;
      }
     servo.write(commandedDegree);
     Serial.println("Servo position:"+String(commandedDegree)); 
     
  }
  
}


void parseServoRequest(String httpget){
  
  Serial.print("Parsing servo request:"+ httpget);
          
  int startServoIndex = httpget.indexOf("openJar");
  if (startServoIndex != -1){
      goalDegree = lidOpen;
      turnOnLED();
      swearCounts++;
      openerTimeout = 0; 
  }
  
}


void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void pins_init()
{
	pinMode(LED, OUTPUT);
	
}
void turnOnLED()
{
	digitalWrite(LED,HIGH);
}
void turnOffLED()
{
	digitalWrite(LED,LOW);
}
