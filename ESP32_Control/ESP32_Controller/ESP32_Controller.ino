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
#define D_PIN -1 // GROUND
#define E_PIN -1 // GROUND
#define LAT_PIN 27
#define OE_PIN 15
#define CLK_PIN 14

// MatrixPanel_I2S_DMA dma_display;
MatrixPanel_I2S_DMA *dma_display = nullptr;
// Button Pin
const int buttonPin = 25; 

// alles + 1
// colors: 0: red , 1: blau, 2: yellow, 3: orange, 4: magenta, 5: dark grey, 6: green, 7: brown, 8: white 9: light blue
int colors[] = {63488, 2110, 65504, 64512, 59608, 16968, 4064, 14593, 65535, 1503};


// Data for images
// 0: apple, 1: key, 2: tree, 3: moon, 4: clock, 5: cactus, 
// 6: earth, 7: yingyang, 8: lock, 9: lightning, 10: heart, 
// 11: bell, 12: cheries, 13: music, 14: question mark, 
// 15: star, 16: beer, 17: ghost, 18: flower, 19: mecces,
// 20: trophy, 21: sword, 22: duck, 23: crown, 24: baseball,
// 25: bird, 26: diamond, 27: garbage, 28: magnifier,
// 29: cat, 30: house, 31: pencil, 32: shield, 33: circle, 34: cross 
union iconCollection {  
  int ico[35][64] = {
    { // apple
      0, 0, 0, 0, 14593, 0, 0, 0, 
      0, 0, 0, 14593, 0, 0, 0, 0, 
      0, 53508, 63488, 14593, 63488, 63488, 0, 0, 
      43430, 53508, 63488, 63488, 63488, 56335, 63488, 0,
      43430, 53508, 63488, 63488, 63488, 56335, 63488, 0, 
      43430, 53508, 63488, 63488, 63488, 63488, 63488, 0, 
      43430, 43430, 53508, 53508, 53508, 53508, 53508, 0, 
      0, 43430, 43430, 43430, 43430, 43430, 0, 0
    },{ // key
      0, 0, 59244, 65504, 65504, 0, 0, 0, 
      0, 59244, 0, 0, 0, 46533, 0, 0, 
      0, 59244, 0, 0, 0, 46533, 0, 0, 
      0, 0, 65504, 65504, 46533, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0, 
      0, 0, 0, 65504, 65504, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0, 
      0, 0, 0, 65504, 46533, 0, 0, 0 
    },{ // tree
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 0, 13861, 4064, 13861, 15431, 0, 0, 
      0, 0, 13861, 4064, 13861, 15431, 0, 0, 
      0, 13861, 4064, 4064, 4064, 13861, 15431, 0, 
      0, 13861, 4064, 4064, 4064, 4064, 13861, 0, 
      13861, 4064, 4064, 4064, 4064, 4064, 13861, 15431, 
      0, 0, 0, 33543, 20964, 0, 0, 0, 
      0, 0, 20964, 33543, 33543, 20964, 0, 0
    },{ // moon
      0, 0, 40179, 52889, 52889, 52889, 0, 0, 
      0, 40179, 52889, 40179, 0, 0, 52889, 0, 
      40179, 52889, 52889, 0, 0, 0, 0, 0, 
      52889, 52889, 52889, 0, 0, 0, 0, 0, 
      52889, 52889, 52889, 0, 0, 0, 0, 0, 
      40179, 52889, 52889, 40179, 0, 0, 0, 40179, 
      0, 40179, 52889, 52889, 52889, 52889, 40179, 0, 
      0, 0, 40179, 40179, 40179, 40179, 0, 0
    },{ // clock
      0, 0, 12696, 12696, 12696, 12696, 0, 0, 
      0, 12696, 52889, 63488, 52889, 52889, 12696, 0, 
      12696, 52889, 52889, 63488, 52889, 52889, 52889, 12696, 
      12696, 52889, 52889, 63488, 52889, 63488, 52889, 12696, 
      12696, 52889, 52889, 63488, 63488, 52889, 52889, 12696, 
      12696, 52889, 52889, 52889, 52889, 52889, 52889, 12696, 
      0, 12696, 52889, 52889, 52889, 52889, 12696, 0, 
      0, 0, 12696, 12696, 12696, 12696, 0, 0
    },{ // cactus
      0, 0, 0, 15431, 13861, 0, 0, 0, 
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 13861, 0, 13861, 13861, 0, 0, 0, 
      0, 13861, 13861, 15431, 13861, 0, 0, 0, 
      0, 0, 0, 13861, 15431, 0, 0, 0, 
      0, 0, 0, 15431, 13861, 0, 0, 0, 
      0, 64512, 64512, 64512, 64512, 64512, 48102, 0, 
      0, 0, 64512, 64512, 48102, 48102, 0, 0
    },{ // earth
      0, 0, 65535, 65535, 1503, 1503, 0, 0, 
      0, 15431, 1503, 15431, 1503, 1503, 65535, 0, 
      15431, 15431, 15431, 15431, 15431, 1503, 1503, 65535, 
      1503, 15431, 15431, 15431, 15431, 1503, 1503, 1503, 
      1503, 15431, 1503, 1503, 1503, 1503, 1503, 1503, 
      1503, 1503, 15431, 1503, 15431, 15431, 15431, 1503, 
      0, 1503, 1503, 1503, 15431, 15431, 15431, 0, 
      0, 0, 1503, 1503, 1503, 15431, 0, 0
    },{ // yingyang
      0, 0, 12696, 12696, 12696, 12696, 0, 0, 
      0, 12696, 12696, 12696, 12696, 12696, 12696, 0, 
      12696, 12696, 65535, 12696, 12696, 12696, 12696, 12696, 
      12696, 12696, 12696, 12696, 12696, 65535, 65535, 12696, 
      65535, 12696, 12696, 65535, 65535, 65535, 65535, 65535, 
      65535, 65535, 65535, 65535, 65535, 12696, 65535, 65535, 
      0, 65535, 65535, 65535, 65535, 65535, 65535, 0, 
      0, 0, 65535, 65535, 65535, 65535, 0, 0
    },{ // lock
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 29614, 29614, 21130, 0, 0, 0, 
      0, 29614, 0, 0, 0, 21130, 0, 0, 
      0, 29614, 0, 0, 0, 29614, 0, 0, 
      46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
      46533, 65504, 65504, 10597, 65504, 65504, 46533, 0, 
      0, 46533, 65504, 65504, 65504, 46533, 0, 0, 
      0, 0, 46533, 46533, 46533, 0, 0, 0
    },{ // lightning
      0, 0, 46533, 65504, 65504, 46533, 0, 0, 
      0, 0, 65504, 65504, 46533, 0, 0, 0, 
      0, 46533, 65504, 46533, 0, 0, 0, 0, 
      0, 65504, 65504, 0, 0, 0, 0, 0, 
      46533, 65504, 65504, 65504, 65504, 65504, 46533, 0, 
      0, 0, 0, 0, 65504, 46533, 0, 0, 
      0, 0, 0, 0, 65504, 0, 0, 0, 
      0, 0, 0, 65504, 0, 0, 0, 0
    },{ // heart
      0, 63488, 63488, 0, 0, 53508, 53508, 0, 
      63488, 63488, 63488, 63488, 63488, 53508, 53508, 53508, 
      63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
      63488, 63488, 63488, 63488, 63488, 63488, 53508, 53508, 
      0, 63488, 63488, 63488, 63488, 53508, 53508, 0, 
      0, 0, 63488, 63488, 63488, 53508, 0, 0, 
      0, 0, 0, 63488, 53508, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0
    },{ // bell
      0, 0, 0, 65504, 46533, 0, 0, 0, 
      0, 0, 59244, 65504, 65504, 46533, 0, 0, 
      0, 0, 59244, 65504, 65504, 46533, 0, 0, 
      0, 0, 65504, 65504, 65504, 46533, 0, 0, 
      0, 0, 65504, 65504, 65504, 46533, 0, 0, 
      0, 65504, 65504, 65504, 65504, 65504, 65504, 0, 
      0, 0, 0, 64512, 64512, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0
    },{ // cherries
      0, 0, 0, 0, 15431, 15431, 15431, 15431, 
      0, 0, 0, 15431, 15431, 0, 15431, 0, 
      0, 0, 15431, 0, 0, 15431, 15431, 0, 
      0, 15431, 15431, 0, 0, 63808, 63808, 0, 
      0, 63808, 63808, 0, 63808, 64492, 64492, 63808, 
      63808, 64492, 63808, 63808, 63808, 64492, 63808, 63808, 
      63808, 64492, 63808, 63808, 0, 63808, 63808, 0, 
      0, 63808, 63808, 0, 0, 0, 0, 0
    },{ // music
      0, 0, 0, 0, 10613, 10613, 10613, 10613, 
      0, 0, 0, 0, 10613, 10613, 10613, 10613, 
      0, 0, 0, 0, 10613, 0, 0, 0, 
      0, 0, 0, 0, 10613, 0, 0, 0, 
      0, 0, 0, 0, 10613, 0, 0, 0, 
      0, 10613, 10613, 10613, 10613, 0, 0, 0, 
      10613, 10613, 10613, 10613, 10613, 0, 0, 0, 
      10613, 10613, 10613, 10613, 0, 0, 0, 0
    },{ // question mark
      0, 2110, 2110, 2110, 2110, 2110, 2110, 0, 
      2110, 2110, 2110, 2110, 2110, 2110, 2110, 2110, 
      2110, 2110, 0, 0, 0, 0, 2110, 2110, 
      0, 0, 0, 0, 0, 0, 2110, 2110, 
      0, 0, 0, 2110, 2110, 2110, 2110, 0, 
      0, 0, 0, 2110, 2110, 0, 0, 0, 
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 2110, 2110, 0, 0, 0
    },{ // star
      0, 0, 0, 65504, 65504, 0, 0, 0, 
      0, 0, 0, 65504, 65504, 0, 0, 0, 
      65504, 65504, 65504, 65504, 65504, 65504, 65504, 65504, 
      0, 65504, 65504, 65504, 65504, 65504, 65504, 0, 
      0, 0, 65504, 65504, 65504, 65504, 0, 0, 
      0, 65504, 65504, 65504, 65504, 65504, 65504, 0, 
      0, 65504, 65504, 0, 0, 65504, 65504, 0, 
      0, 65504, 0, 0, 0, 0, 65504, 0
    },{ // beer
      0, 0, 0, 65535, 65535, 65535, 65535, 0, 
      0, 0, 65535, 65535, 65535, 65535, 65535, 65535, 
      0, 0, 26139, 65535, 64512, 65535, 64512, 26139, 
      0, 26139, 26139, 64512, 64512, 64512, 64512, 26139, 
      26139, 0, 26139, 64512, 64512, 60779, 64512, 26139, 
      26139, 0, 26139, 60779, 64512, 64512, 64512, 26139, 
      0, 26139, 26139, 64512, 64512, 64512, 60779, 26139, 
      0, 0, 26139, 26139, 26139, 26139, 26139, 26139
    },{ // ghost
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 65535, 65535, 65535, 65535, 65535, 0, 0, 
      65535, 65535, 0, 65535, 0, 65535, 52889, 0, 
      65535, 65535, 0, 65535, 0, 65535, 52889, 0, 
      65535, 65535, 65535, 65535, 65535, 65535, 52889, 0, 
      65535, 65535, 65535, 65535, 65535, 65535, 52889, 0, 
      65535, 65535, 65535, 65535, 65535, 52889, 52889, 0, 
      65535, 0, 65535, 0, 52889, 0, 52889, 0
    },{ // flower
      0, 0, 0, 63488, 63488, 63488, 0, 0, 
      0, 0, 63488, 65504, 59244, 59244, 63488, 0, 
      0, 0, 53508, 65504, 65504, 59244, 63488, 0, 
      0, 0, 53508, 65504, 65504, 65504, 63488, 0, 
      0, 0, 0, 53508, 53508, 63488, 0, 0, 
      0, 0, 0, 0, 13861, 0, 0, 0, 
      0, 0, 13861, 0, 13861, 0, 13861, 0, 
      0, 0, 0, 13861, 13861, 13861, 0, 0
    },{ // mecces
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 15431, 15431, 15431, 15431, 15431, 15431, 15431, 
      0, 15431, 15431, 65504, 15431, 65504, 15431, 15431, 
      0, 15431, 65504, 15431, 65504, 15431, 65504, 15431, 
      0, 15431, 65504, 15431, 65504, 15431, 65504, 15431, 
      0, 15431, 65504, 15431, 65504, 15431, 65504, 15431, 
      0, 15431, 65504, 15431, 15431, 15431, 65504, 15431, 
      0, 15431, 15431, 15431, 15431, 15431, 15431, 15431
    },{ // trophy
      0, 0, 65504, 65504, 59244, 59244, 59244, 0, 
      0, 65504, 0, 65504, 65504, 59244, 0, 59244, 
      0, 65504, 0, 65504, 65504, 65504, 0, 59244, 
      0, 46533, 0, 65504, 65504, 65504, 0, 65504, 
      0, 0, 46533, 46533, 65504, 65504, 65504, 0, 
      0, 0, 0, 0, 46533, 0, 0, 0, 
      0, 0, 0, 0, 46533, 0, 0, 0, 
      0, 0, 0, 46533, 46533, 46533, 0, 0
    },{ // sword
      0, 0, 0, 0, 0, 0, 29614, 29614, 
      0, 0, 0, 0, 0, 29614, 40179, 29614, 
      0, 0, 0, 0, 29614, 40179, 29614, 0, 
      0, 0, 0, 29614, 40179, 29614, 0, 0, 
      20964, 0, 29614, 40179, 29614, 0, 0, 0, 
      0, 20964, 40179, 29614, 0, 0, 0, 0, 
      0, 20964, 20964, 0, 0, 0, 0, 0, 
      14593, 0, 0, 20964, 0, 0, 0, 0
    },{ // duck
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 0, 0, 0, 59244, 65504, 65504, 0, 
      0, 0, 0, 0, 65504, 1503, 65504, 0, 
      59244, 0, 0, 0, 65504, 65504, 64512, 64512, 
      59244, 65504, 65504, 65504, 65504, 65504, 65504, 0, 
      65504, 65504, 46533, 46533, 46533, 65504, 0, 0, 
      0, 65504, 65504, 46533, 46533, 65504, 0, 0, 
      0, 0, 65504, 65504, 65504, 0, 0, 0
    },{ // crown
      0, 0, 0, 59244, 59244, 0, 0, 0, 
      0, 0, 0, 64512, 64512, 0, 0, 0, 
      0, 59244, 59244, 63488, 63488, 59244, 59244, 0, 
      59244, 63488, 63488, 59244, 59244, 63488, 63488, 59244, 
      46533, 63488, 63488, 59244, 59244, 63488, 63488, 46533, 
      46533, 43430, 43430, 59244, 59244, 43430, 43430, 46533, 
      0, 46533, 43430, 46533, 46533, 43430, 46533, 0, 
      46533, 46533, 46533, 46533, 46533, 46533, 46533, 46533
    },{ // baseball
      0, 0, 65535, 65535, 65535, 56335, 0, 0, 
      0, 52889, 65535, 65535, 65535, 56335, 65535, 0, 
      53508, 53508, 52889, 65535, 65535, 53508, 65535, 65535, 
      52889, 53508, 52889, 65535, 65535, 53508, 65535, 65535, 
      40179, 52889, 53508, 52889, 65535, 65535, 53508, 65535, 
      40179, 40179, 31142, 52889, 65535, 65535, 53508, 53508, 
      0, 40179, 20805, 40179, 52889, 52889, 65535, 0, 
      0, 0, 20805, 40179, 40179, 52889, 0, 0
    },{ // bird
      0, 0, 1503, 1503, 1503, 0, 0, 0, 
      0, 1503, 1503, 1503, 1503, 1503, 0, 0, 
      0, 1503, 1503, 1503, 0, 1503, 0, 0, 
      65504, 65504, 1503, 1503, 1503, 1503, 0, 0, 
      0, 1503, 1503, 1503, 1503, 1503, 1503, 1503, 
      0, 1503, 1503, 1503, 1503, 1503, 1503, 0, 
      0, 0, 1503, 1503, 1503, 0, 0, 0, 
      0, 0, 65504, 0, 0, 65504, 0, 0
    },{ // diamond
      0, 0, 0, 0, 0, 0, 0, 0, 
      0, 50972, 1503, 1503, 1503, 1503, 10957, 0, 
      50972, 50972, 50972, 1503, 1503, 10957, 10957, 10957, 
      50972, 50972, 50972, 50972, 10957, 10957, 10957, 10957, 
      13266, 13266, 13266, 13266, 8490, 8490, 8490, 1503, 
      0, 13266, 13266, 13266, 8490, 8490, 1503, 0, 
      0, 0, 13266, 13266, 8490, 1503, 0, 0, 
      0, 0, 0, 13266, 1503, 0, 0, 0
    },{ // garbage
      0, 0, 0, 21130, 21130, 21130, 0, 0, 
      0, 0, 21130, 0, 0, 0, 21130, 0, 
      0, 21130, 21130, 21130, 21130, 21130, 21130, 21130, 
      0, 0, 29614, 52889, 29614, 52889, 29614, 0, 
      0, 0, 29614, 52889, 29614, 52889, 29614, 0, 
      0, 0, 29614, 52889, 29614, 52889, 29614, 0, 
      0, 0, 29614, 52889, 29614, 52889, 29614, 0, 
      0, 0, 29614, 29614, 29614, 29614, 29614, 0
    },{ // magnifier
      0, 29614, 29614, 29614, 29614, 0, 0, 0, 
      29614, 26139, 50972, 26139, 29614, 29614, 0, 0, 
      29614, 50972, 50972, 26139, 26139, 29614, 0, 0, 
      29614, 26139, 26139, 26139, 26139, 29614, 0, 0, 
      29614, 10957, 50972, 26139, 29614, 29614, 0, 0, 
      0, 29614, 29614, 29614, 29614, 33543, 33543, 0, 
      0, 0, 0, 0, 0, 14593, 33543, 33543, 
      0, 0, 0, 0, 0, 0, 14593, 33543
    },{ // cat
      0, 48102, 0, 0, 0, 0, 48102, 0, 
      0, 48102, 14593, 0, 0, 48102, 56335, 0, 
      0, 48102, 48102, 14593, 48102, 56335, 56335, 0, 
      14593, 48102, 48102, 48102, 48102, 48102, 48102, 0, 
      48102, 0, 48102, 48102, 0, 48102, 48102, 0, 
      48102, 0, 48102, 48102, 0, 48102, 48102, 48102, 
      41797, 48102, 58458, 48102, 48102, 48102, 48102, 0, 
      0, 41797, 48102, 48102, 48102, 14593, 0, 0
    },{ // house
      0, 0, 53508, 53508, 0, 29614, 29614, 0, 
      0, 0, 53508, 53508, 53508, 53508, 29614, 0, 
      0, 53508, 53508, 53508, 53508, 29614, 31142, 0, 
      0, 53508, 53508, 53508, 53508, 52889, 29614, 31142, 
      53508, 53508, 53508, 53508, 29614, 52889, 52889, 0, 
      0, 29614, 53508, 53508, 52889, 0, 52889, 0, 
      0, 29614, 29614, 29614, 52889, 0, 52889, 0, 
      0, 0, 0, 29614, 52889, 0, 0, 0
    },{ // pencil
      0, 0, 0, 0, 0, 58458, 58458, 0, 
      0, 0, 0, 0, 40179, 58458, 58458, 58458, 
      0, 0, 0, 60779, 60779, 40179, 58458, 58458, 
      0, 0, 60779, 60779, 60779, 48102, 21130, 0, 
      0, 60779, 48102, 60779, 48102, 48102, 0, 0, 
      0, 60779, 60779, 48102, 48102, 0, 0, 0, 
      52889, 60779, 60779, 48102, 0, 0, 0, 0, 
      52889, 52889, 0, 0, 0, 0, 0, 0
    },{ // shield
      52889, 52889, 52889, 52889, 29614, 29614, 29614, 29614, 
      52889, 27516, 27516, 27516, 8490, 8490, 8490, 29614, 
      52889, 27516, 27516, 27516, 8490, 8490, 8490, 29614, 
      29614, 27516, 27516, 27516, 8490, 8490, 8490, 29614, 
      29614, 27516, 27516, 27516, 8490, 8490, 8490, 52889, 
      29614, 27516, 27516, 27516, 8490, 8490, 8490, 52889, 
      0, 29614, 27516, 27516, 8490, 29614, 52889, 0, 
      0, 0, 29614, 29614, 29614, 52889, 0, 0
    },{ // circle
      0, 0, 15431, 4064, 4064, 4064, 15431, 0, 
      0, 15431, 4064, 15431, 15431, 15431, 4064, 15431, 
      0, 4064, 15431, 0, 0, 0, 15431, 4064, 
      0, 4064, 15431, 0, 0, 0, 15431, 4064, 
      0, 4064, 15431, 0, 0, 0, 15431, 4064, 
      0, 15431, 4064, 15431, 15431, 15431, 4064, 15431, 
      0, 0, 15431, 4064, 4064, 4064, 15431, 0, 
      0, 0, 0, 0, 0, 0, 0, 0
    },{ // cross
      20805, 0, 0, 0, 0, 0, 63488, 63488, 
      53508, 53508, 0, 0, 0, 63488, 63488, 20805, 
      0, 20805, 53508, 0, 63488, 63488, 20805, 0, 
      0, 0, 20805, 63488, 63488, 20805, 0, 0, 
      0, 0, 0, 63488, 63488, 0, 0, 0, 
      0, 0, 63488, 20805, 20805, 53508, 0, 0, 
      0, 63488, 20805, 0, 0, 20805, 53508, 0, 
      63488, 0, 0, 0, 0, 0, 20805, 63488
      }
  };
} icons;

int logo[512] = {
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 0, 0, 65535, 65535, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1503, 1503, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 0, 0, 65535, 65535, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1503, 1503, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 65535, 65535, 65535, 65535, 65535, 65535, 0, 0, 0, 0, 0, 0, 4064, 4064, 0, 0, 63488, 63488, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 65535, 65535, 65535, 65535, 65535, 65535, 0, 0, 0, 0, 0, 0, 4064, 4064, 0, 0, 63488, 63488, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 0, 0, 65535, 65535, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65504, 65504, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 28501, 28501, 0, 0, 65535, 65535, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 65504, 65504, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 28501, 28501, 28501, 28501, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

char* matrix_name = "pixx ink 3";

char memblock[50];
char* text;
boolean drawing = false;
TaskHandle_t handle_drawText;

// Functions to controll LED MATRIX
void drawText(void * pvParameters){  
  // init text parameters
  int font_size = 1;
  dma_display->setTextSize(font_size); // size 1 == 8 pixels high
  dma_display->setTextWrap(false); // Don't wrap at end of line - will do ourselves
  dma_display->setTextColor(dma_display->color444(50, 100, 127));
  dma_display->fillScreen(dma_display->color444(0, 0, 0));
  
  uint8_t w = 0;
  int shifts = font_size * (strlen(text) * 5 + strlen(text)); // 5 for width of a single letter
  for (int s = 0; s < 32 + shifts; s++){
    dma_display->setCursor(32 - s, 4);
    for (w=0; w<strlen(text); w++) {
      dma_display->print(text[w]);
    }
    delay(30);
    dma_display->fillScreen(dma_display->color444(0, 0, 0));
  }
  drawing = false;
  handle_drawText = NULL;
  vTaskDelete(NULL);
}

void drawPoints(uint16_t color, char* pointsStr){
  dma_display->setTextColor(color);
  dma_display->fillScreen(dma_display->color444(0, 0, 0));

  uint8_t w = 0;
  if (strlen(pointsStr) == 2){
    dma_display->setCursor(10, 4);
  } else {
    dma_display->setCursor(10, 4);
  }
    for (w=0; w<strlen(text); w++) {
      if (pointsStr[w] == '\0'){
        return;
      }
      dma_display->print(pointsStr[w]);
    }
}


void startDrawingThread(char* t){
  if(!drawing){
    drawing = true;
    
    for(size_t i = 0; i < strlen(t); i++){
      memblock[i] = t[i];
    }  
    memblock[strlen(t)] = '\0';
    text = &memblock[0];
    handle_drawText = NULL;
    handle_drawText = new TaskHandle_t;
    
    xTaskCreatePinnedToCore(
      drawText, // function to be executed
      "drawingTask", // name of Task
      6000, // stack size
      NULL, // pointer to parameters
      0, // priority
      &handle_drawText, // taskHandle
      0); // core 
  }
}

void drawIcons(int ico[], size_t len){
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

// Advertising function
void advertise(){
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

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

// --------------------------------------------------------------------------------------------------------
// HELPER FUNCTIONS ---------------------------------------------------------------------------------------

char * string_copy(const char *from, char *to) {
    for (char *p = to; ( *p = *from ) != '\0'; ++p, ++from){
        ;
    }
    return to;
}

// --------------------------------------------------------------------------------------------------------
// SETUP --------------------------------------------------------------------------------------------------

void setup() {
  Serial.begin(115200);

  // INIT LED MATRIX --------------------------------------------------------------------------------------
  HUB75_I2S_CFG::i2s_pins _pins={R1_PIN, G1_PIN, B1_PIN, R2_PIN, G2_PIN, B2_PIN, A_PIN, B_PIN, C_PIN, D_PIN, E_PIN, LAT_PIN, OE_PIN, CLK_PIN};
  HUB75_I2S_CFG mxconfig(
    PANEL_RES_X,   // module width
    PANEL_RES_Y,   // module height
    PANEL_CHAIN,    // Chain length
    _pins
  );

  mxconfig.clkphase = false;
  mxconfig.driver = HUB75_I2S_CFG::FM6126A;
  pinMode(buttonPin, INPUT);

  // Display Setup
  dma_display = new MatrixPanel_I2S_DMA(mxconfig);
  dma_display->begin();
  dma_display->setBrightness8(120); //0-255
  dma_display->clearScreen();
  
  // Print "Welcome!" on LED matrix
  startDrawingThread("Welcome!");
  
  // INIT BLUETOOTH ---------------------------------------------------------------------------------------
  // Setup device
  BLEDevice::init(matrix_name);
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
  dataCharacteristic->setValue("");
  
  // Button characteristic
  buttonCharacteristic = pService->createCharacteristic(BUTTON_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_NOTIFY);
  buttonCharacteristic->setValue("0");

  // Start service and advertising
  pService->start();
  advertise();
}

// --------------------------------------------------------------------------------------------------------
// STATE MACHINE ------------------------------------------------------------------------------------------ 

char prev_MODE = '0';
char MODE = '0';
boolean painting = false;
int buttonPressed = 0;

char* prev_data = "";

void loop() {
  if (!deviceConnected){
    modeCharacteristic->setValue("0");
  }

  // Get current mode
  prev_MODE = MODE;
  MODE = modeCharacteristic->getValue()[0];
  
  // If mode changed, stop drawing text task
  if (prev_MODE != MODE) {
    dataCharacteristic->setValue("");
    if (drawing){
      vTaskDelete(handle_drawText);
      drawing = false;
    }
    painting = false;
    dma_display->clearScreen();
  }
  
  // STATE MACHINE - START --------------------------------------------------------------------------------
  // PAIRING MODE - No device connected in this state
  if (MODE == '0'){
    if(deviceConnected){
      modeCharacteristic->setValue("1");
      return;
    }
    startDrawingThread("Connect to pixx ink 3...");
  }
  // CONNECTED, BUT NOT READY YET - Not all devices connected, no game chosen, etc.
  else if (MODE == '1'){
    Serial.println("m1");
    if (!drawing){
      dma_display->drawIcon(logo, 0, 0, 32, 16);
    }
  }
  // WHO AM I & Send Text
  else if (MODE == '2'){
    Serial.println("m2");
    char* name = (char*) dataCharacteristic->getValue().data();
    startDrawingThread(name);
  }
  // HOT PIXELS 
  else if (MODE == '3'){
    Serial.println("m3");
  }
  // DRAWING AND GUESSING - Montagsmaler
  else if (MODE == '4'){
    Serial.println("m4");
    if (!painting){
      dma_display->fillRect(11, 3, 10, 10, dma_display->color444(15, 15, 15));
      painting = true;

    } else if (painting){
      char* data = (char*) dataCharacteristic->getValue().data();
      Serial.print("Data: ");
      Serial.println(data);
      if (strlen(data) == 3){
        int16_t x_pos = ((int16_t)0x00 << 8) | data[0];
        int16_t y_pos = ((int16_t)0x00 << 8) | data[1];
        uint16_t color = colors[(((int16_t)0x00 << 8) | data[2]) - 1];
        dma_display->drawPixel(x_pos + 10, y_pos + 2, color);
      } else if (strlen(data) == 100) {
        Serial.println("Elseteil");
        int img[100];
        for (int i = 0; i < 100; i++){
          img[i] = colors[(((int16_t)0x00 << 8) | data[i]) - 1];
        }
        dma_display->drawIcon(img, 11, 3, 10, 10);
      }
    }
    
  }
  // DOBBLE
  else if (MODE == '5'){
    Serial.println("m5");
    Serial.println(drawing);
    Serial.println(painting);
    painting = false; 
    startDrawingThread("Dobble");

  } else if (MODE == '6') {
    Serial.println("m6");
    char* value = (char*) dataCharacteristic->getValue().data();
    Serial.println(strlen(value));
    int ic[strlen(value)];

    if (strcmp(value, prev_data) != 0){
      
      for (int i = 0; i < strlen(value); i++){
        ic[i] = (((uint16_t)0x00 << 8) | value[i]) - 1;
      }

      drawIcons(ic, strlen(value));

      prev_data = (char*) malloc(strlen(value) + 1);
      puts(string_copy(value, prev_data));

      painting = true;
    }
  } else if (MODE == '7'){
    // value[0] = Punkte, value[1] = Farbe
    char* value = (char*) dataCharacteristic->getValue().data();
    uint16_t color = colors[(((int16_t)0x00 << 8) | value[1]) - 1];
    int16_t points = (((int16_t)0x00 << 8) | value[0]) - 1;
    char pointsStr[5];
    sprintf(pointsStr, "%d", points);
    drawPoints(color, pointsStr);
    
  }

  // Check button states
  // blue button: digitRead == 1 --> PRESSED
  // black/red button: digitRead == 0 --> PRESSED
  if (digitalRead(buttonPin) == 1 && buttonPressed == 0){
      Serial.println("BUTTON PRESSED!");
      char* buttonState = "1";
      buttonCharacteristic->setValue(buttonState);
      buttonCharacteristic->notify(); 
      delay(25);
      buttonCharacteristic->setValue("0");
      buttonPressed++;
  } else if (digitalRead(buttonPin) == 1){
    Serial.println("buttonPressed:");
    Serial.println(buttonPressed);
    buttonPressed++;
  } else {
    buttonPressed = false;
    Serial.println("Not pressed");
  }

  if(buttonPressed >= 50 && deviceConnected){
    exit(0);
    buttonPressed = 0;
  }

  delay(100);


}
