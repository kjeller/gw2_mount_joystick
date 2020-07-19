# gw2_mount_joystick
A GW2 joystick device to select mounts in-game. 

<img src="https://github.com/kjeller/gw2_mount_joystick/blob/master/joystick-min.png" height="400"><img src="https://github.com/kjeller/gw2_mount_joystick/blob/master/settings.png" height="400" alt="Image of java program">

A joystick and OLED display is controlled by an Arduino Nano (atmega 168), which sends serial messages (integers in the range 1-8), representing each notch on the joystick (clockwise). These messages are then decoded by a java program (SerialKeyboard), that maps the signals to different keystrokes. (The generated keystrokes are sent directly as scan codes to Windows). Only a single modifier can be used at a time, together with a regular key e.g. (ALT + z). Multiple modifiers are not yet supported e.g. (ALT + CTRL + SHIFT + Z)



Mount icons are displayed in 64x64 on an SSD1306 OLED display.
```icon/``` folder where the xbm icons are stored, together with the python script that 
convert png -> xbm. (The png images are from the gw2 wiki: https://wiki.guildwars2.com/wiki/Category:Mount_skill_icons)

# Further additions
Things I might consider adding to the project:
  - Define slots as <serial char> to <key mapping> rather than using pre-defined slots <slot n> to <key mapping> (where n = 1, 2 .. 8)
  - Serial monitor in a tab when a connection has been established
 
