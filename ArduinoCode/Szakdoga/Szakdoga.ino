#include <WiFi.h>
#include <WebSocketsServer.h>
#include <PinButton.h>
#include <ESP32Servo.h>

#define KELETLED 23
#define NYUGATLED 22
#define TIMEBUTTON 26

#define CHARGERLED 15
#define GARAGELED 2
#define GARAGEMOTION 34

#define BATHLED 5
#define BATHMOTION 39
#define BATHSERVO 32
#define HMVLED 13

#define ENGINEMOTION 35
#define ENGINELED 12

#define LIVINGBUTTON 16
#define LIVINGLED 4

#define KITCHENBUTTON 25
#define KITCHENLED_M 27
#define KITCHENLED_S 14

#define BEDBUTTON 19
#define BEDTVBUTTON 18
#define BEDLED 21
#define BEDTVLED 17
#define BEDSERVO 33

const char* ssid = "Unknown";
const char* password = "********";

WebSocketsServer webSocket(81);

int bedServoLevel = 180;
int bathServoLevel = 180;
int bedTvCurrent = LOW;
int bedTvPrevious = LOW;

const char* timeOfDayStrings[] = {
  "MIDNIGHT",
  "MORNING",
  "NOON",
  "EVENING"
};

void timeLightning(int idx) {
  switch (idx) {
    case (0):
      digitalWrite(KELETLED, LOW);
      digitalWrite(NYUGATLED, LOW);
      bedServoLevel = 180;
      webSocket.broadcastTXT("MIDNIGHT");
      break;
    case (1):
      digitalWrite(KELETLED, HIGH);
      digitalWrite(NYUGATLED, LOW);
      bedServoLevel = 0;
      webSocket.broadcastTXT("MORNING");
      break;
    case (2):
      digitalWrite(KELETLED, HIGH);
      digitalWrite(NYUGATLED, HIGH);
      bedServoLevel = 90;
      webSocket.broadcastTXT("NOON");
      break;
    case (3):
      digitalWrite(KELETLED, LOW);
      digitalWrite(NYUGATLED, HIGH);
      bedServoLevel = 180;
      webSocket.broadcastTXT("EVENING");
      break;
  }
}

String dayTime = timeOfDayStrings[0];
PinButton TimeButton(TIMEBUTTON);
void changeTime() {
  int idx = -1; //find index of current timeOfDay
  for (int i = 0; i < 4; i++)
    if (dayTime == timeOfDayStrings[i]) {
      idx = i;
      break;
    }

  idx = (idx + 1) % 4; //get next timeOfDay

  dayTime = timeOfDayStrings[idx];

  timeLightning(idx);
}

int garageMotionCurrent = LOW;
int garageMotionPrevious = LOW;
bool prevElectricUsage = false;
void garage() {
  //TURN GARAGE LIGHT ON MOTION
  garageMotionPrevious = garageMotionCurrent;
  garageMotionCurrent = digitalRead(GARAGEMOTION);
  if (garageMotionPrevious == LOW && garageMotionCurrent == HIGH) {
    digitalWrite(GARAGELED, HIGH);
    webSocket.broadcastTXT(String(GARAGELED) + " ON");
  } else if (garageMotionPrevious == HIGH && garageMotionCurrent == LOW) {
    digitalWrite(GARAGELED, LOW);
    webSocket.broadcastTXT(String(GARAGELED) + " OFF");
  }

  //CHARGERLED ONLY LIGHT AT MIDNIGHT AND WHEN THERE IS NO ELECTRICITY USAGE
  if (dayTime == timeOfDayStrings[0] && electricUsage() == false && prevElectricUsage == true) {
    digitalWrite(CHARGERLED, HIGH);
    webSocket.broadcastTXT(String(CHARGERLED) + " ON");
    prevElectricUsage = false;
  } else if ((dayTime != timeOfDayStrings[0] || electricUsage() != false) && prevElectricUsage == false) {
    digitalWrite(CHARGERLED, LOW);
    webSocket.broadcastTXT(String(CHARGERLED) + " OFF");
    prevElectricUsage = true;
  }
}

int engineMotionCurrent = LOW;
int engineMotionPrevious = LOW;
void engineroom() {
  //TURN ENGINEROOM LIGHT ON MOTION
  engineMotionPrevious = engineMotionCurrent;
  engineMotionCurrent = digitalRead(ENGINEMOTION);
  if (engineMotionPrevious == LOW && engineMotionCurrent == HIGH) {
    digitalWrite(ENGINELED, HIGH);
    webSocket.broadcastTXT(String(ENGINELED) + " ON");
  } else if (engineMotionPrevious == HIGH && engineMotionCurrent == LOW) {
    digitalWrite(ENGINELED, LOW);
    webSocket.broadcastTXT(String(ENGINELED) + " OFF");
  }
}

PinButton LivingButton(LIVINGBUTTON);
int PWMLevel = 0;
int prevPWMLevel = 0;
bool PWMInc = false;
void livingroom() {
  LivingButton.update();
  prevPWMLevel = PWMLevel;
  if (LivingButton.isSingleClick() && PWMLevel < 252) {
    PWMLevel += 63;
    PWMInc = true;
  } else if (LivingButton.isDoubleClick() && PWMLevel > 0) {
    PWMLevel -= 63;
    PWMInc = false;
  } else if (LivingButton.isLongClick() && PWMLevel == 0) {
    PWMLevel = 252;
    PWMInc = true;
  } else if (LivingButton.isLongClick() && PWMLevel == 252) {
    PWMLevel = 0;
    PWMInc = false;
  } else if (LivingButton.isLongClick() && PWMInc == true) {
    PWMLevel = 252;
    PWMInc = true;
  } else if (LivingButton.isLongClick() && PWMInc == false) {
    PWMLevel = 0;
    PWMInc = false;
  }

  if (prevPWMLevel != PWMLevel) {
    analogWrite(LIVINGLED, PWMLevel);
    webSocket.broadcastTXT(String(LIVINGLED) + " " + String(PWMLevel));

    prevPWMLevel = PWMLevel;
  }
}

PinButton KitchenButton(KITCHENBUTTON);
void kitchen() {
  KitchenButton.update();
  if (KitchenButton.isSingleClick()) {
    digitalWrite(KITCHENLED_M, !digitalRead(KITCHENLED_M));
    webSocket.broadcastTXT(String(KITCHENLED_M) + (digitalRead(KITCHENLED_M) ? " ON" : " OFF"));
  } else if (KitchenButton.isDoubleClick()) {
    digitalWrite(KITCHENLED_S, !digitalRead(KITCHENLED_S));
    webSocket.broadcastTXT(String(KITCHENLED_S) + (digitalRead(KITCHENLED_S) ? " ON" : " OFF"));
  }
}

int bathMotionCurrent = LOW;
int bathMotionPrevious = LOW;
Servo bathServo;
void bathroom() {
  //TURN BATHROOM LIGHT ON MOTION
  bathMotionPrevious = bathMotionCurrent;
  bathMotionCurrent = digitalRead(BATHMOTION);
  if (bathMotionPrevious == LOW && bathMotionCurrent == HIGH) {
    bathServoLevel = 0;
    digitalWrite(BATHLED, HIGH);
    webSocket.broadcastTXT(String(BATHLED) + " ON");
    digitalWrite(HMVLED, HIGH);
    webSocket.broadcastTXT(String(HMVLED) + " ON");

  } else if (bathMotionPrevious == HIGH && bathMotionCurrent == LOW) {
    bathServoLevel = 180;
    digitalWrite(BATHLED, LOW);
    webSocket.broadcastTXT(String(BATHLED) + " OFF");
    digitalWrite(HMVLED, LOW);
    webSocket.broadcastTXT(String(HMVLED) + " OFF");
  }

  if (!(bathServo.read() >= bathServoLevel - 10 && bathServo.read() <= bathServoLevel + 10)) {
    bathServo.write(bathServoLevel);
    webSocket.broadcastTXT(String(BATHSERVO) + " " + String(bathServoLevel));
  }
}

Servo bedServo;
PinButton BedButton(BEDBUTTON);
PinButton BedTVButton(BEDTVBUTTON);
void bedroom() {
  BedButton.update();
  if (BedButton.isClick()) {
    digitalWrite(BEDLED, !digitalRead(BEDLED));
    webSocket.broadcastTXT(String(BEDLED) + (digitalRead(BEDLED) ? " ON" : " OFF"));
  }

  bedTvPrevious = bedTvCurrent;
  bedTvCurrent = digitalRead(BEDTVLED);
  BedTVButton.update();
  if (BedTVButton.isClick()) {
    digitalWrite(BEDTVLED, !digitalRead(BEDTVLED));
    webSocket.broadcastTXT(String(BEDTVLED) + (digitalRead(BEDTVLED) ? " ON" : " OFF"));
  }

  if (bedTvCurrent == HIGH && bedTvPrevious == LOW) {
    bedServoLevel = 180;
    digitalWrite(BEDLED, LOW);
    webSocket.broadcastTXT(String(BEDLED) + " OFF");
  }

  if (!(bedServo.read() >= bedServoLevel - 10 && bedServo.read() <= bedServoLevel + 10)) {
    bedServo.write(bedServoLevel);
    webSocket.broadcastTXT(String(BEDSERVO) + " " + String(bedServoLevel));
  }
}

bool electricUsage() {
  if (digitalRead(HMVLED) == HIGH || digitalRead(ENGINELED) == HIGH || digitalRead(GARAGELED) == HIGH || PWMLevel > 0 || digitalRead(KITCHENLED_M) == HIGH || digitalRead(KITCHENLED_S) == HIGH || bedServo.read() <= 100 || bathServo.read() <= 100 || digitalRead(BATHLED) == HIGH || digitalRead(BEDLED) == HIGH || digitalRead(BEDTVLED) == HIGH)
    return true;
  return false;
}

void seperateString(String commands[], uint8_t* payload) {
  String payloadString = String((char*)payload);
  int index = payloadString.indexOf(' ');
  if (index == -1) {
    commands[0] = payloadString;
    commands[1] = "";
  } else {
    commands[0] = payloadString.substring(0, index);
    commands[1] = payloadString.substring(index + 1);
  }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t* payload, size_t length) {
  String commands[2];
  seperateString(commands, payload);

  //Serial.println(commands[0] + " " + commands[1]);

  switch (type) {
    case WStype_TEXT:
      //TIME
      if (strcmp(commands[0].c_str(), "getTime") == 0) {  //request
        webSocket.sendTXT(num, dayTime);
      } else if (strcmp(commands[0].c_str(), "setTime") == 0) {  //set
        int idx = commands[1].toInt();
        dayTime = timeOfDayStrings[idx];
        webSocket.broadcastTXT(dayTime);
        timeLightning(idx);
      }
      //DIGITAL
      else if (strcmp(commands[0].c_str(), "isON") == 0) {  //request
        String state = digitalRead(commands[1].toInt()) ? " ON" : " OFF";
        webSocket.sendTXT(num, String(commands[1].toInt()) + state);
      } else if (strcmp(commands[0].c_str(), "setON") == 0) {  //set
        digitalWrite(commands[1].toInt(), HIGH);
        webSocket.broadcastTXT(commands[1] + " ON");
      } else if (strcmp(commands[0].c_str(), "setOFF") == 0) {  //set
        digitalWrite(commands[1].toInt(), LOW);
        webSocket.broadcastTXT(commands[1] + " OFF");
      }
      //ANALOG Request
      else if (strcmp(commands[0].c_str(), "getPos") == 0) { 
        if (strcmp(commands[1].c_str(), String(BEDSERVO).c_str()) == 0) {
          webSocket.broadcastTXT(String(BEDSERVO) + " " + String(bedServoLevel));
        } else if (strcmp(commands[1].c_str(), String(BATHSERVO).c_str()) == 0) {
          webSocket.broadcastTXT(String(BATHSERVO) + " " + String(bathServoLevel));
        } else if (strcmp(commands[1].c_str(), String(LIVINGLED).c_str()) == 0) {
          webSocket.broadcastTXT(String(LIVINGLED) + " " + String(PWMLevel));
        }
      } 
      //ANALOG Set
      else if (strcmp(commands[0].c_str(), String(BEDSERVO).c_str()) == 0) {
        bedServoLevel = commands[1].toInt();
        webSocket.broadcastTXT(String(BEDSERVO) + " " + String(bedServoLevel));
      } else if (strcmp(commands[0].c_str(), String(BATHSERVO).c_str()) == 0) {
        bathServoLevel = commands[1].toInt();
        webSocket.broadcastTXT(String(BATHSERVO) + " " + String(bathServoLevel));
      } else if (strcmp(commands[0].c_str(), String(LIVINGLED).c_str()) == 0) {
        PWMLevel = commands[1].toInt();
        analogWrite(LIVINGLED, PWMLevel);
        webSocket.broadcastTXT(String(LIVINGLED) + " " + String(PWMLevel));
      }
      break;
  }
}

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  webSocket.begin();
  webSocket.onEvent(webSocketEvent);

  //Outside
  dayTime = timeOfDayStrings[0];
  pinMode(KELETLED, OUTPUT);
  pinMode(NYUGATLED, OUTPUT);
  digitalWrite(KELETLED, LOW);
  digitalWrite(NYUGATLED, LOW);

  //EngineRoom
  pinMode(ENGINELED, OUTPUT);
  pinMode(ENGINEMOTION, INPUT);
  digitalWrite(ENGINELED, LOW);

  //Garage
  pinMode(GARAGELED, OUTPUT);
  pinMode(CHARGERLED, OUTPUT);
  pinMode(GARAGEMOTION, INPUT);
  digitalWrite(GARAGELED, LOW);
  digitalWrite(CHARGERLED, HIGH);

  //LivingRoom
  pinMode(LIVINGLED, OUTPUT);
  digitalWrite(LIVINGLED, LOW);

  //Kitchen
  pinMode(KITCHENLED_M, OUTPUT);
  pinMode(KITCHENLED_S, OUTPUT);
  digitalWrite(KITCHENLED_M, LOW);
  digitalWrite(KITCHENLED_S, LOW);

  //Bathroom
  pinMode(BATHLED, OUTPUT);
  pinMode(HMVLED, OUTPUT);
  pinMode(BATHMOTION, INPUT);
  bathServo.attach(BATHSERVO);
  bathServo.write(bathServoLevel);
  digitalWrite(BATHLED, LOW);
  digitalWrite(HMVLED, LOW);

  //Bedroom
  pinMode(BEDLED, OUTPUT);
  pinMode(BEDTVLED, OUTPUT);
  bedServo.attach(BEDSERVO);
  bedServo.write(bedServoLevel);
  digitalWrite(BEDLED, LOW);
  digitalWrite(BEDTVLED, LOW);
}

void loop() {
  webSocket.loop();

  TimeButton.update();
  if (TimeButton.isClick()) {
    changeTime();
    delay(200);
  }

  engineroom();
  garage();  
  livingroom();  
  kitchen();     
  bathroom();  
  bedroom();
}