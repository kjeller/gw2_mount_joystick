# gw2_mount_joystick
GW2 joystick device to select mounts ingame. 
A joystick and OLED display is controlled by an Arduino Nano (atmega 168), which sends serial messages (integers in the range 1-8), representing each notch on the joystick (clockwise). These messages are then decoded by a java program (SerialKeyboard), that maps the signals to different keystrokes.

Mount icons are displayed on an SSD1306 OLED display.
```icon/``` folder where the xbm icons are stored, together with the python script that 
convert png -> xbm. (The png images are from the gw2 wiki)
