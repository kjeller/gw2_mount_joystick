# Adjust threshold and inverts colors to remove background on gw2 mount icons, the 64x64 XBM files that 
# are the result of running this script will be used on a monographic OLED screen,
# automatically converted to black and white when converting to XBM format.
#
# Author: Karl Stralman

import wand.api
import ctypes
import os
import sys
import fileinput
from wand.image import Image
from wand.display import display
from PIL import Image as ImagePIL
import PIL.ImageOps

MagickEvaluateImage = wand.api.library.MagickEvaluateImage
MagickEvaluateImage.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_double]

#from:
# https://stackoverflow.com/questions/19339894/how-to-threshold-an-image-using-wand-in-python
def evaluate(self, operation, argument):
  MagickEvaluateImage(
      self.wand,
      wand.image.EVALUATE_OPS.index(operation),
      self.quantum_range * float(argument))

# Resize and use threshold to extract logo
def threshold(fin, fout):
    with Image(filename=fin) as img:
        img.resize(64, 64)
        evaluate(img, 'threshold', 0.67)
        img.type = 'grayscale'
        img.gamma(15)
        img.save(filename=fout)

# Convert image to xbm
def to_xbm(fin, fout):
    with Image(filename=fin) as img:
        img.format = 'xbm'
        img.save(filename=fout)

# Inverts colors on image. 
# NOTE: This functions uses a different image library (PIL)
def invert(fin, fout):
    img = ImagePIL.open(fin)
    img = img.convert('L')
    inverted_image = PIL.ImageOps.invert(img)
    inverted_image.save(fout)

# Uses to remove intermediate png files uses between the two image libraries
def clean(fname):
    os.remove(fname)

if __name__ == "__main__":
    for x in range(1, 9):
        fname = 'icon%d' % x
        fin_png = 'res/%s.png' % fname
        fout_png = 'out/%s.png' % fname
        fout_xbm = 'out/%s.xbm' % fname

        print 'Parsing: %s' % fin_png,
        threshold(fin_png, fout_png)
        invert(fout_png, fout_png)
        to_xbm(fout_png, fout_xbm)
        clean(fout_png)

        # Add PROGMEM to each file
        for line in fileinput.input(fout_xbm, inplace=1):
            if '[]' in line:
                line = line.replace('[]','[] PROGMEM')
            sys.stdout.write(line)
        print'...Success!'
