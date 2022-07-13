#include <ESP32-HUB75-MatrixPanel-I2S-DMA.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <string>

// Check for bluetooth
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

// LED MATRIX ---------------------------------------------------------------------------------------------
// Panel properties
#define PANEL_RES_X 32      // Number of pixels wide of each INDIVIDUAL panel module. 
#define PANEL_RES_Y 16     // Number of pixels tall of each INDIVIDUAL panel module.
#define PANEL_CHAIN 1      // Total number of panels chained one to another

// Pins
#define R1_PIN 23
#define G1_PIN 21
#define B1_PIN 32
#define R2_PIN 33
#define G2_PIN 19
#define B2_PIN 26
#define A_PIN 12
#define B_PIN 16
#define C_PIN 13
#define D_PIN -1 // Hier Ground
#define E_PIN -1 // required for 1/32 scan panels, like 64x64. Any available pin would do, i.e. IO32
#define LAT_PIN 27
#define OE_PIN 15
#define CLK_PIN 14

// MatrixPanel_I2S_DMA dma_display;
MatrixPanel_I2S_DMA *dma_display = nullptr;
// Button Pin
const int buttonPin = 25; 

uint16_t myBLACK = dma_display->color565(0, 0, 0);
uint16_t myWHITE = dma_display->color565(255, 255, 255);
uint16_t myRED = dma_display->color565(255, 0, 0);
uint16_t myGREEN = dma_display->color565(0, 255, 0);
uint16_t myBLUE = dma_display->color565(0, 0, 255);

// Data for images


// 0: apple, 1: key, 2: tree, 3: moon, 4: clock, 5: cactus, 6: earth, 7: yingyang, 8: lock, 9: lightning, 10:heart
union iconCollection {
  int ico[11][64] = {
    {
      0, 0, 0, 0, 14593, 0, 0, 0, 
      0, 0, 0, 14593, 0, 0, 0, 0, 
      0, 53508, 63488, 14593, 63488, 63488, 0, 0, 
      43430, 53508, 63488, 63488, 63488, 56335, 63488, 0,
      43430, 53508, 63488, 63488, 63488, 56335, 63488, 0, 
      43430, 53508, 63488, 63488, 63488, 63488, 63488, 0, 
      43430, 43430, 53508, 53508, 53508, 53508, 53508, 0, 
      0, 43430, 43430, 43430, 43430, 43430, 0, 0
    },{
      0, 0, 59244, 65504, 65504, 0, 0, 0, 
      0, 59244, 0, 0, 0, 46533, 0, 0, 
      0, 59244, 0, 0, 0, 46533, 0, 0, 
      0, 0, 65504, 65504, 46533, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0, 
      0, 0, 0, 65504, 65504, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0, 
      0, 0, 0, 65504, 46533, 0, 0, 0 
    },{
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 0, 13861, 4064, 13861, 15431, 0, 0, 
      0, 0, 13861, 4064, 13861, 15431, 0, 0, 
      0, 13861, 4064, 4064, 4064, 13861, 15431, 0, 
      0, 13861, 4064, 4064, 4064, 4064, 13861, 0, 
      13861, 4064, 4064, 4064, 4064, 4064, 13861, 15431, 
      0, 0, 0, 33543, 20964, 0, 0, 0, 
      0, 0, 20964, 33543, 33543, 20964, 0, 0
    },{
      0, 0, 40179, 52889, 52889, 52889, 0, 0, 
      0, 40179, 52889, 40179, 0, 0, 52889, 0, 
      40179, 52889, 52889, 0, 0, 0, 0, 0, 
      52889, 52889, 52889, 0, 0, 0, 0, 0, 
      52889, 52889, 52889, 0, 0, 0, 0, 0, 
      40179, 52889, 52889, 40179, 0, 0, 0, 40179, 
      0, 40179, 52889, 52889, 52889, 52889, 40179, 0, 
      0, 0, 40179, 40179, 40179, 40179, 0, 0
    },{
      0, 0, 12696, 12696, 12696, 12696, 0, 0, 
      0, 12696, 52889, 63488, 52889, 52889, 12696, 0, 
      12696, 52889, 52889, 63488, 52889, 52889, 52889, 12696, 
      12696, 52889, 52889, 63488, 52889, 63488, 52889, 12696, 
      12696, 52889, 52889, 63488, 63488, 52889, 52889, 12696, 
      12696, 52889, 52889, 52889, 52889, 52889, 52889, 12696, 
      0, 12696, 52889, 52889, 52889, 52889, 12696, 0, 
      0, 0, 12696, 12696, 12696, 12696, 0, 0
    },{
      0, 0, 0, 15431, 13861, 0, 0, 0, 
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 13861, 0, 13861, 13861, 0, 0, 0, 
      0, 13861, 13861, 15431, 13861, 0, 0, 0, 
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 0, 0, 15431, 13861, 0, 0, 0, 
      0, 64512, 64512, 64512, 64512, 64512, 48102, 0, 
      0, 0, 64512, 64512, 48102, 48102, 0, 0
    },{
      0, 0, 65535, 65535, 1503, 1503, 0, 0, 
      0, 15431, 1503, 15431, 1503, 1503, 65535, 0, 
      15431, 15431, 15431, 15431, 15431, 1503, 1503, 65535, 
      1503, 15431, 15431, 15431, 15431, 1503, 1503, 1503, 
      1503, 15431, 1503, 1503, 1503, 1503, 1503, 1503, 
      1503, 1503, 15431, 1503, 15431, 15431, 15431, 1503, 
      0, 1503, 1503, 1503, 15431, 15431, 15431, 0, 
      0, 0, 1503, 1503, 1503, 15431, 0, 0
    },{
      0, 0, 12696, 12696, 12696, 12696, 0, 0, 
      0, 12696, 12696, 12696, 12696, 12696, 12696, 0, 
      12696, 12696, 65535, 12696, 12696, 12696, 12696, 12696, 
      12696, 12696, 12696, 12696, 12696, 65535, 65535, 12696, 
      65535, 12696, 12696, 65535, 65535, 65535, 65535, 65535, 
      65535, 65535, 65535, 65535, 65535, 12696, 65535, 65535, 
      0, 65535, 65535, 65535, 65535, 65535, 65535, 0, 
      0, 0, 65535, 65535, 65535, 65535, 0, 0
    },{
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 29614, 29614, 21130, 0, 0, 0, 
      0, 29614, 0, 0, 0, 21130, 0, 0, 
      0, 29614, 0, 0, 0, 29614, 0, 0, 
      46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
      46533, 65504, 65504, 10597, 65504, 65504, 46533, 0, 
      0, 46533, 65504, 65504, 65504, 46533, 0, 0, 
      0, 0, 46533, 46533, 46533, 0, 0, 0
    },{
      0, 0, 46533, 65504, 65504, 46533, 0, 0, 
      0, 0, 65504, 65504, 46533, 0, 0, 0, 
      0, 46533, 65504, 46533, 0, 0, 0, 0, 
      0, 65504, 65504, 0, 0, 0, 0, 0, 
      46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
      0, 0, 0, 0, 65504, 46533, 0, 0, 
      0, 0, 0, 0, 65504, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0
    },{
      0, 63488, 63488, 0, 0, 53508, 53508, 0, 
      63488, 63488, 63488, 63488, 63488, 53508, 53508, 53508, 
      63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
      63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
      0, 63488, 63488, 63488, 63488, 53508, 53508, 0, 
      0, 0, 63488, 63488, 63488, 53508, 0, 0, 
      0, 0, 0, 63488, 53508, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0
    }
  };
} icons;


// Functions to controll LED MATRIX
void drawText(char *text){
    
  // init text parameters
  int font_size = 2;
  dma_display->setTextSize(font_size); // size 1 == 8 pixels high
  dma_display->setTextWrap(false); // Don't wrap at end of line - will do ourselves
  dma_display->setTextColor(dma_display->color444(50, 100, 127));
  dma_display->fillScreen(dma_display->color444(0, 0, 0));
  
  uint8_t w = 0;
  int shifts = font_size * (strlen(text) * 5 + strlen(text)); // 5 for width of a single letter
  for (int s = 0; s < 32 + shifts; s++){
    dma_display->setCursor(32 - s, 1);
    for (w=0; w<strlen(text); w++) {
      dma_display->print(text[w]);
    }
    delay(30);
    dma_display->fillScreen(dma_display->color444(0, 0, 0));
  }
}

void drawIcons(int ico[], int len){
  dma_display->fillScreen(dma_display->color444(0, 0, 0));
  if (len == 1){
    dma_display->drawIcon(icons.ico[ico[0]], 12, 4, 8, 8);
  } else if (len == 8){
    dma_display->drawIcon(icons.ico[ico[0]], 0, 0, 8, 8);
    dma_display->drawIcon(icons.ico[ico[1]], 8, 0, 8, 8);
    dma_display->drawIcon(icons.ico[ico[2]], 16, 0, 8, 8);
    dma_display->drawIcon(icons.ico[ico[3]], 24, 0, 8, 8);
    dma_display->drawIcon(icons.ico[ico[4]], 0, 8, 8, 8);
    dma_display->drawIcon(icons.ico[ico[5]], 8, 8, 8, 8);
    dma_display->drawIcon(icons.ico[ico[6]], 16, 8, 8, 8);
    dma_display->drawIcon(icons.ico[ico[7]], 24, 8, 8, 8);
  } else {
    Serial.println("Error in drawIcons(): Wrong number of icons!");
    return;
  }
}

// --------------------------------------------------------------------------------------------------------
// BLUETOOTH STUFF ----------------------------------------------------------------------------------------

// BLE
BLEService *pService;
BLECharacteristic *modeCharacteristic;
BLECharacteristic *dataCharacteristic;
BLECharacteristic *buttonCharacteristic;

// Services and Characteristics: https://www.uuidgenerator.net/
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define MODE_CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define DATA_CHARACTERISTIC_UUID "84e3a48f-7172-4952-8e59-64af6ce20583"
#define BUTTON_CHARACTERISTIC_UUID "07bf0001-7a36-490f-ba53-345b3642a694"

// Connection status 
boolean deviceConnected = false;

// Callback onConnect and onDisconnect
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

// Advertising function
void advertise(){
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

// --------------------------------------------------------------------------------------------------------
// HELPER FUNCTIONS ---------------------------------------------------------------------------------------

char* appendCharToCharArray(char* array, char a){
  size_t len = strlen(array);
  char* ret = new char[len+2];

  strcpy(ret, array);    
  ret[len] = a;
  ret[len+1] = '\0';

  return ret;
}

char* intToByteArray(int n, bool onebyte){
  if(onebyte){
    char bytes[1];
    bytes[0] = n & 0xFF;
    return bytes;
  } else {
    char bytes[4];
    bytes[0] = (n >> 24) & 0xFF;
    bytes[1] = (n >> 16) & 0xFF;
    bytes[2] = (n >> 8) & 0xFF;
    bytes[3] = n & 0xFF;
    return bytes;
  }
}

// --------------------------------------------------------------------------------------------------------
// SETUP --------------------------------------------------------------------------------------------------

void setup() {
  Serial.begin(115200);
  Serial.println("Characteristic defined! Now you can read it in your phone!");

  // INIT LED MATRIX ------------------------------------------------------------------------------------------------------------------------
  HUB75_I2S_CFG::i2s_pins _pins={R1_PIN, G1_PIN, B1_PIN, R2_PIN, G2_PIN, B2_PIN, A_PIN, B_PIN, C_PIN, D_PIN, E_PIN, LAT_PIN, OE_PIN, CLK_PIN};
  HUB75_I2S_CFG mxconfig(
    PANEL_RES_X,   // module width
    PANEL_RES_Y,   // module height
    PANEL_CHAIN,    // Chain length
    _pins
  );

  mxconfig.clkphase = false;
  mxconfig.driver = HUB75_I2S_CFG::FM6126A;

  // Display Setup
  dma_display = new MatrixPanel_I2S_DMA(mxconfig);
  dma_display->begin();
  dma_display->setBrightness8(200); //0-255
  dma_display->clearScreen();
  drawText("Welcome!");
  
  // INIT BLUETOOTH -------------------------------------------------------------------------------------------------------------------------
    // Setup device
  BLEDevice::init("LED MATRIX");
  uint16_t mtu = 128;
  BLEDevice::setMTU(mtu);
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // Create service
  pService = pServer->createService(SERVICE_UUID);

  // Mode characteristic  
  modeCharacteristic = pService->createCharacteristic(MODE_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  modeCharacteristic->setValue("0");
  
  // Data characteristic 
  dataCharacteristic = pService->createCharacteristic(DATA_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  dataCharacteristic->setValue("0");
  
  // Button characteristic
  buttonCharacteristic = pService->createCharacteristic(BUTTON_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_NOTIFY);
  buttonCharacteristic->setValue("0");

  // Start service and advertising
  pService->start();
  advertise();
}

// --------------------------------------------------------------------------------------------------------
// STATE MACHINE ------------------------------------------------------------------------------------------

void loop() {
  // TODO: REMOVE "- 48" later
  uint8_t MODE = modeCharacteristic->getValue()[0] - 48;
  Serial.println(appendCharToCharArray("Mode: ", MODE));
  // PAIRING MODE - No device connected in this state
  if (MODE == 0){
    if(deviceConnected){
      modeCharacteristic->setValue("1");
      return;
    }
    Serial.println("m0:");
    drawText("Ready to connect...");
  }
  // CONNECTED, BUT NOT READY YET - Not all devices connected, no game chosen, etc.
  else if (MODE == 1){
    Serial.println("m1:");
    drawText("Connected!");
  }
  // WHO AM I
  else if (MODE == 2){
    Serial.println("m2:");
  }
  // HOT PIXELS 
  else if (MODE == 3){
    Serial.println("m3:");
  }
  // DRAWING AND GUESSING - Montagsmaler
  else if (MODE == 4){
    Serial.println("m4:");
  }
  // DOBBLE
  else if (MODE == 5){
    Serial.println("m5:");
    int ic[] = {0, 1, 2, 3, 4, 5, 6, 7};
    drawIcons(ic, 8);
  }
  // SEND TEXT
  else if (MODE == 6){
    
  }

  if(deviceConnected && digitalRead(buttonPin)){
    char* buttonState = "1";
    buttonCharacteristic->setValue(buttonState);
    buttonCharacteristic->notify(); 
    delay(50);
    buttonCharacteristic->setValue("0");
  }
  delay(50);


}
