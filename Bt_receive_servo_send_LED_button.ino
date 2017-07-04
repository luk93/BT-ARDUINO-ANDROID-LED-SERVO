#include <SoftwareSerial.h>
#include <Servo.h>
#define bt_tx 10
#define bt_rx 11
#define led 8 
#define servoPin 6
#define button 2

Servo servo;
SoftwareSerial bt = SoftwareSerial(bt_rx, bt_tx);

byte bits;
char command;
String string;
boolean servo_angle = 0;

void setup() {
  pinMode(bt_rx, INPUT);
  pinMode(bt_tx, OUTPUT);
  pinMode(led, OUTPUT);
  pinMode(button, INPUT_PULLUP);
  Serial.begin(9600); 
  bt.begin(9600);
  servo.attach(servoPin);
}
 
void loop() 
{
  sendData();
  listenForData();
}

void sendData()
{
  if(digitalRead(button) == LOW)
  {
    digitalWrite(led, HIGH);
    bt.write('9');
  }
  if(digitalRead(button) == HIGH)
  {
    digitalWrite(led, LOW);
  }
}
void listenForData()
{
  if (bt.available() > 0 ) 
  {
    string = "";
  }
  while (bt.available() > 0)
  {
    command = ((byte)bt.read());
    if (command == ":")
    {
      break;   
    }
    else
    {
      string += command;
    }
    delay(1);
  }
  if(string == "ZERO")
  {
    servo.write(0);
    delay(10);
  }
  if(string == "MAX")
  {
    servo.write(180);
    delay(10);
  }
  if ((string.toInt()>=0)&&(string.toInt()<=180))
  {
      servo.write(string.toInt());
  }
}




