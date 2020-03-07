# Adjust levels to remove background on gw2 mount icons, the 64x64 XBM files that 
# are the result of running this script will be used on a monographic OLED screen,
# automatically converted to black and yellow when converting to XBM format.
#
# Author: Karl Stralman

import wand.api
import ctypes
from wand.image import Image
from wand.display import display

MagickEvaluateImage = wand.api.library.MagickEvaluateImage
MagickEvaluateImage.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_double]

#from:
# https://stackoverflow.com/questions/19339894/how-to-threshold-an-image-using-wand-in-python
def evaluate(self, operation, argument):
  MagickEvaluateImage(
      self.wand,
      wand.image.EVALUATE_OPS.index(operation),
      self.quantum_range * float(argument))

for x in range(1, 9):
    fname = 'icon%d' % x
    print('Parsing: %s' % fname)
    with Image(filename='res/%s.png' % fname) as img:
        img.resize(64, 64)
        evaluate(img, 'threshold', 0.67)
        img.type = 'grayscale'
        img.gamma(15)
        img.format = 'xbm'
        fout = "out/%s.xbm" % fname
        img.save(filename=fout)
            
