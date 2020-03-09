#include <icons.h>

#include <avr/pgmspace.h>
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_GFX.h>

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels

// Declaration for an SSD1306 display connected to I2C (SDA, SCL pins)
#define OLED_RESET     4 // Reset pin # (or -1 if sharing Arduino reset pin)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

const uint8_t * const icon[] = {
  (uint8_t*)icon1_bits, (uint8_t*)icon2_bits, (uint8_t*)icon3_bits, (uint8_t*)icon4_bits,
  (uint8_t*)icon5_bits, (uint8_t*)icon6_bits, (uint8_t*)icon7_bits, (uint8_t*)icon8_bits
};

enum Slot {
  UNDEFINED = 0,
  S1,
  S2,
  S3,
  S4,
  S5,
  S6,
  S7,
  S8
};
Slot current = UNDEFINED;
Slot prev = UNDEFINED;

int ledPin = 13;
int joyPin1 = 0;                 // slider variable connecetd to analog pin 0
int joyPin2 = 1;                 // slider variable connecetd to analog pin 1
int v1 = 0;                  // variable to read the value from the analog pin 0
int v2 = 0;                  // variable to read the value from the analog pin 1

void setup() {
  pinMode(ledPin, OUTPUT);              // initializes digital pins 0 to 7 as outputs
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.display();
  delay(100);
  display.clearDisplay();
  Serial.begin(9600);
}

int treatValue(int data) {
  return (data * 9 / 1024) + 48;
}

/* 
 *  Jostick space is divided into eight equally sized chunks,
 *  where each of the chunk is a slot, that can be used as 
 *  a direction or to be encoded.
 * */
Slot decode_slot(int x, int y) {
  Slot s;
   if (x >= 0 && x <= 256 && y >= 0 && y <= 256 )
    s = S1;
  else if (x <= 768 && x >= 256 && y == 0)
    s = S2;
  else if (x >= 768 && y >= 0 && y <= 256)
    s = S3;
  else if (x >= 1020 && y >= 256 && y <= 768)
    s = S4;
  else if (x >= 768 && y >= 768)
    s = S5;
  else if (x <= 768 && x >= 256 && y >= 1020)
    s = S6;
  else if (y >= 768 && x >= 0 && x <= 256)
    s = S7;
  else if (x == 0 && y <= 768 && y >= 256)
    s = S8;
  else
    s = UNDEFINED;
    
  return s;
}

void drawIcon(int i) {
  display.clearDisplay();
  display.drawXBitmap(
    (display.width()  - 64 ) / 2,
    (display.height() - 64) / 2,
    icon[i], 64, 64, 1);
  display.display();
}

void loop() {
  /* reads the value of the variable resistors */
  v1 = analogRead(joyPin1);
  delay(100);
  v2 = analogRead(joyPin2);

  Serial.print("X: ");
  Serial.print(v1);
  Serial.print("| Y: ");
  Serial.println(v2);

  /* Get new state from jstick position */
  current = decode_slot(v1, v2);
  
  if(current != UNDEFINED) {
    prev = current;
    drawIcon(current-1);
  }
    
  Serial.print("Slot :");
  Serial.println(current);
  delay(100);
}
