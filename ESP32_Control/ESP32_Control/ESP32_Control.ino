#include <ESP32-HUB75-MatrixPanel-I2S-DMA.h>
#include <BluetoothSerial.h>
#include <string>
// Check for bluetooth
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif


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

//MatrixPanel_I2S_DMA dma_display;
MatrixPanel_I2S_DMA *dma_display = nullptr;

uint16_t myBLACK = dma_display->color565(0, 0, 0);
uint16_t myWHITE = dma_display->color565(255, 255, 255);
uint16_t myRED = dma_display->color565(255, 0, 0);
uint16_t myGREEN = dma_display->color565(0, 255, 0);
uint16_t myBLUE = dma_display->color565(0, 0, 255);


void drawText(char *text){
    
  // init text parameters
  int font_size = 2;
  dma_display->setTextSize(font_size);     // size 1 == 8 pixels high
  dma_display->setTextWrap(false); // Don't wrap at end of line - will do ourselves

  uint8_t w = 0;
  Serial.print("Länge des Strings: ");
  Serial.println(strlen(text));
  int shifts = font_size * (strlen(text) * 5 + strlen(text)); // 5 for width of a single letter
  for (int s = 0; s < 32 + shifts; s++){
    dma_display->setCursor(32 - s, 1);
    for (w=0; w<strlen(text); w++) {
      dma_display->setTextColor(dma_display->color444(50, 100, 200));
      dma_display->print(text[w]);
    }
    delay(30);
    dma_display->fillScreen(dma_display->color444(0, 0, 0));
  }

}
// BT stuff
BluetoothSerial SerialBT;

void setup() {

  // Module configuration
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
  dma_display->setBrightness8(255); //0-255
  dma_display->clearScreen();


  Serial.begin(115200);
  SerialBT.begin("ESP32test"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");

}

char *str = "ESP32";
int half_sun [50] = {
  0xf, 0xf0, 0xf00, 0xf000, 0xf0000, 0xf00000, 0xffe0, 65535, 2147483647, -2147483647,
  0x0000, 0xffe0, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0xffe0, 300000,
  0x0000, 0x0000, 0x0000, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0x0000, 0x0000, 0x0000,
  0xffe0, 0x0000, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0x0000, 0xffe0,
  0x0000, 0x0000, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0xffe0, 0x0000, 0x0000,
};


int apple [64] = {
  0, 0, 0, 0, 14593, 0, 0, 0, 
  0, 0, 0, 14593, 0, 0, 0, 0, 
  0, 53508, 63488, 14593, 63488, 63488, 0, 0, 
  43430, 53508, 63488, 63488, 63488, 56335, 63488, 0,
  43430, 53508, 63488, 63488, 63488, 56335, 63488, 0, 
  43430, 53508, 63488, 63488, 63488, 63488, 63488, 0, 
  43430, 43430, 53508, 53508, 53508, 53508, 53508, 0, 
  0, 43430, 43430, 43430, 43430, 43430, 0, 0
};


int key [64] = {
  0, 0, 59244, 65504, 65504, 0, 0, 0, 
  0, 59244, 0, 0, 0, 46533, 0, 0, 
  0, 59244, 0, 0, 0, 46533, 0, 0, 
  0, 0, 65504, 65504, 46533, 0, 0, 0, 
  0, 0, 0, 65504, 0, 0, 0, 0, 
  0, 0, 0, 65504, 65504, 0, 0, 0, 
  0, 0, 0, 65504, 0, 0, 0, 0, 
  0, 0, 0, 65504, 46533, 0, 0, 0 
};

int tree [64] = {
  0, 0, 0, 13861, 15431, 0, 0, 0, 
  0, 0, 13861, 4064, 13861, 15431, 0, 0, 
  0, 0, 13861, 4064, 13861, 15431, 0, 0, 
  0, 13861, 4064, 4064, 4064, 13861, 15431, 0, 
  0, 13861, 4064, 4064, 4064, 4064, 13861, 0, 
  13861, 4064, 4064, 4064, 4064, 4064, 13861, 15431, 
  0, 0, 0, 33543, 20964, 0, 0, 0, 
  0, 0, 20964, 33543, 33543, 20964, 0, 0
};


int moon [64] = {
  0, 0, 40179, 52889, 52889, 52889, 0, 0, 
  0, 40179, 52889, 40179, 0, 0, 52889, 0, 
  40179, 52889, 52889, 0, 0, 0, 0, 0, 
  52889, 52889, 52889, 0, 0, 0, 0, 0, 
  52889, 52889, 52889, 0, 0, 0, 0, 0, 
  40179, 52889, 52889, 40179, 0, 0, 0, 40179, 
  0, 40179, 52889, 52889, 52889, 52889, 40179, 0, 
  0, 0, 40179, 40179, 40179, 40179, 0, 0
};


int clck [64] = {
  0, 0, 12696, 12696, 12696, 12696, 0, 0, 
  0, 12696, 52889, 63488, 52889, 52889, 12696, 0, 
  12696, 52889, 52889, 63488, 52889, 52889, 52889, 12696, 
  12696, 52889, 52889, 63488, 52889, 63488, 52889, 12696, 
  12696, 52889, 52889, 63488, 63488, 52889, 52889, 12696, 
  12696, 52889, 52889, 52889, 52889, 52889, 52889, 12696, 
  0, 12696, 52889, 52889, 52889, 52889, 12696, 0, 
  0, 0, 12696, 12696, 12696, 12696, 0, 0
};


int cactus [64] = {
  0, 0, 0, 15431, 13861, 0, 0, 0, 
  0, 0, 0, 13861, 15431, 0, 0, 0, 
  0, 13861, 0, 13861, 13861, 0, 0, 0, 
  0, 13861, 13861, 15431, 13861, 0, 0, 0, 
  0, 0, 0, 13861, 15431, 0, 0, 0, 
  0, 0, 0, 15431, 13861, 0, 0, 0, 
  0, 64512, 64512, 64512, 64512, 64512, 48102, 0, 
  0, 0, 64512, 64512, 48102, 48102, 0, 0
};


int earth [64] = {
  0, 0, 65535, 65535, 1503, 1503, 0, 0, 
  0, 15431, 1503, 15431, 1503, 1503, 65535, 0, 
  15431, 15431, 15431, 15431, 15431, 1503, 1503, 65535, 
  1503, 15431, 15431, 15431, 15431, 1503, 1503, 1503, 
  1503, 15431, 1503, 1503, 1503, 1503, 1503, 1503, 
  1503, 1503, 15431, 1503, 15431, 15431, 15431, 1503, 
  0, 1503, 1503, 1503, 15431, 15431, 15431, 0, 
  0, 0, 1503, 1503, 1503, 15431, 0, 0
};

int yinyang [64] = {
  0, 0, 12696, 12696, 12696, 12696, 0, 0, 
  0, 12696, 12696, 12696, 12696, 12696, 12696, 0, 
  12696, 12696, 65535, 12696, 12696, 12696, 12696, 12696, 
  12696, 12696, 12696, 12696, 12696, 65535, 65535, 12696, 
  65535, 12696, 12696, 65535, 65535, 65535, 65535, 65535, 
  65535, 65535, 65535, 65535, 65535, 12696, 65535, 65535, 
  0, 65535, 65535, 65535, 65535, 65535, 65535, 0, 
  0, 0, 65535, 65535, 65535, 65535, 0, 0
};


int lock [64] = {
  0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 29614, 29614, 21130, 0, 0, 0, 
  0, 29614, 0, 0, 0, 21130, 0, 0, 
  0, 29614, 0, 0, 0, 29614, 0, 0, 
  46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
  46533, 65504, 65504, 10597, 65504, 65504, 46533, 0, 
  0, 46533, 65504, 65504, 65504, 46533, 0, 0, 
  0, 0, 46533, 46533, 46533, 0, 0, 0
};


int lightning [64] = {
  0, 0, 46533, 65504, 65504, 46533, 0, 0, 
  0, 0, 65504, 65504, 46533, 0, 0, 0, 
  0, 46533, 65504, 46533, 0, 0, 0, 0, 
  0, 65504, 65504, 0, 0, 0, 0, 0, 
  46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
  0, 0, 0, 0, 65504, 46533, 0, 0, 
  0, 0, 0, 0, 65504, 0, 0, 0, 
  0, 0, 0, 65504, 0, 0, 0, 0
};


int heart [64] = {
  0, 63488, 63488, 0, 0, 53508, 53508, 0, 
  63488, 63488, 63488, 63488, 63488, 53508, 53508, 53508, 
  63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
  63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
  0, 63488, 63488, 63488, 63488, 53508, 53508, 0, 
  0, 0, 63488, 63488, 63488, 53508, 0, 0, 
  0, 0, 0, 63488, 53508, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0
};

char* appendCharToCharArray(char* array, char a){
    size_t len = strlen(array);

    char* ret = new char[len+2];

    strcpy(ret, array);    
    ret[len] = a;
    ret[len+1] = '\0';

    return ret;
}

void readBT() {
  char *helper = "";
  for (int i = 0; i < 100; i++){
    if (SerialBT.available()) {
      char c = char(SerialBT.read());
      Serial.write(c);
      helper = appendCharToCharArray(helper, c);
    }
   
    delay(10);
  }
  Serial.write(strlen(helper));
  if (strlen(helper) > 0) {
    str = helper;
  }
  Serial.write('\n');
  Serial.write(str);
  Serial.write('\n');
}


void loop() {

  // animate by going through the colour wheel for the first two lines
  //dma_display->fillScreen(dma_display->color444(0, 0, 0));
  //drawText(str);
  //readBT();
  
  
  dma_display->fillScreen(dma_display->color444(0, 0, 0));
  dma_display->drawIcon(yinyang, 0, 0, 8, 8);
  dma_display->drawIcon(key, 8, 0, 8, 8);
  dma_display->drawIcon(lock, 16, 0, 8, 8);
  dma_display->drawIcon(apple, 24, 0, 8, 8);
  dma_display->drawIcon(heart, 0, 8, 8, 8);
  dma_display->drawIcon(clck, 8, 8, 8, 8);
  dma_display->drawIcon(earth, 16, 8, 8, 8);
  dma_display->drawIcon(lightning, 24, 8, 8, 8);
  delay(5000);

  dma_display->fillScreen(dma_display->color444(0, 0, 0));
  dma_display->drawIcon(yinyang, 12, 4, 8, 8);
  delay(5000);
  
  
  
}
