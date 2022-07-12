/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
    Ported to Arduino ESP32 by Evandro Copercini
    updates by chegewara
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <string>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define BUTTON_CHARACTERISTIC_UUID "07bf0001-7a36-490f-ba53-345b3642a694"

BLEService *pService;
BLECharacteristic *tCharacteristic;
BLECharacteristic *buttonCharacteristic;

const int buttonPin = 25; 
boolean deviceConnected = false;


void advertise(){
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

//Setup callbacks onConnect and onDisconnect
class ServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    Serial.println("connected!");
    deviceConnected = true;
  };
  void onDisconnect(BLEServer* pServer) {
    Serial.println("disconnected!");
    deviceConnected = false;
    advertise();
  }
};

void setup() {
  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  pinMode(buttonPin, INPUT);

  // Setup device
  BLEDevice::init("Long name works now");
  uint16_t mtu = 128;
  BLEDevice::setMTU(mtu);
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create service
  pService = pServer->createService(SERVICE_UUID);

  // Test characteristic  
  tCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  tCharacteristic->setValue("xjcscqavtijsytibtacpqddaewyjmtfsgtcghkqxkbbmpcpirwpajfhrncixfzxqmkijwnakmuhbkzybjwxdxihvlgjpvqbkvswjthvipopfzbnaochhtggbdbatkxafp");
  
  // Button characteristic
  buttonCharacteristic = pService->createCharacteristic(BUTTON_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_NOTIFY);
  buttonCharacteristic->setValue("0");

  // Start service and advertising
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  advertise();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  if(deviceConnected && digitalRead(buttonPin)){
    char* buttonState = "1";
    buttonCharacteristic->setValue(buttonState);
    buttonCharacteristic->notify(); 
    delay(50);
    buttonCharacteristic->setValue("0");
  }
  std::string value = tCharacteristic->getValue();
  const char* chr = value.data();
  Serial.println(chr);
  delay(2000);
}
