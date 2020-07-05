#include <U8g2lib.h>
#include <icons.h>

#include <avr/pgmspace.h>

/* Available icons defined in icons.h */
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

/* Construct */
U8G2_SSD1306_128X64_NONAME_1_HW_I2C u8g2 (U8G2_R0);
int joyStickPin1 = 0; // A0
int joyStickPin2 = 1; // A1
int joyStickSW = 12; //D12

void setup() {
  u8g2.begin();
}

int i = 0;
void loop() {
  /* Read the values of the joystick*/
  int v1 = analogRead(joyStickPin1);
  delay(100);
  int v2 = analogRead(joyStickPin2);

  /* Get new state from jstick position */
  Slot temp = decode_slot(v1, v2);
  if(temp != UNDEFINED)
    current = temp;

  /* Picture loop */
  u8g2.firstPage();
  do {
    if(current != UNDEFINED) {
      prev = current;
      drawIcon(current-1); //current in the range 1-8, icon range 0-7
    }
  } while ( u8g2.nextPage() );
  
  delay(100);  
}

/* 
 *  Jostick space is divided into eight equally sized chunks,
 *  where each of the chunk is a slot, that can be used as 
 *  a direction or to be encoded.
 * */
Slot decode_slot(int x, int y) {
  Slot s;
   if (x >= 0 && x <= 256 && y >= 0 && y <= 256)
    s = S1;
  else if (x <= 768 && x >= 256 && y >= 0 && y <= 256)
    s = S2;
  else if (x >= 768 && y >= 0 && y <= 256)
    s = S3;
  else if (x >= 1000 && y >= 256 && y <= 768)
    s = S4;
  else if (x >= 768 && y >= 768)
    s = S5;
  else if (x <= 768 && x >= 256 && y >= 1000)
    s = S6;
  else if (y >= 768 && x >= 0 && x <= 256)
    s = S7;
  else if (x >= 0 && x <= 256 && y <= 768 && y >= 256)
    s = S8;
  else
    s = UNDEFINED;
    
  return s;
}

void drawIcon(int i) {
  u8g2.drawXBMP(
    (u8g2.getDisplayWidth()  - 64 ) / 2,
    (u8g2.getDisplayHeight() - 64) / 2,
    64, 64, icon[i]);
}
