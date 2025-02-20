;--------------------------------------------------------------------------------------
;HSV Demo 2 - V1.0, Mar 2002
;
;	Author:		Ghost Dancer, Aurora-Soft
;	Website:	www.aurora-soft.co.uk
;	Contact:	colour@aurora-soft.co.uk
;
;	Post any comments or bug reports on the above website or email address.
;
;	This demo calculates complementary colours (opposite & split) of a given colour.
;	The opposite is calculated by moving 180 degrees around the colour circle.
;	The split complementaries are those hues either side (30 degrees) of the opposite.
;--------------------------------------------------------------------------------------

AppTitle "HSV Demo 2"

ShowPointer

;include the colourSpace library...
Include "colourSpace.bb"

Const boxLeft = 110, boxTop = 10, boxWidth = 68, fontH = 18, yOffset = 30
c.hsv = New hsv : opp.hsv = New hsv : split1.hsv = New hsv : split2.hsv = New hsv

;*** colour to change ***
hexRef$ = "$ad0000"				;this could be input by user!

;convert hex string to a number
hexNum = hex2int(hexRef)

;convert to hsv...
c = rgb2hsv(hexNum, c)
opp = rgb2hsv(hexNum, opp)
split1 = rgb2hsv(hexNum, split1)
split2 = rgb2hsv(hexNum, split2)

;calculate opposite...
opp\h = opp\h + 180
If opp\h >= 360 Then opp\h = opp\h - 360
oppRgb = hsv2rgb(opp\h, opp\s, opp\v)

;calculate split 1...
split1\h = opp\h + 30
If split1\h >= 360 Then split1\h = split1\h - 360
split1Rgb = hsv2rgb(split1\h, split1\s, split1\v)

;calculate split 2...
split2\h = opp\h - 30
If split2\h < 0 Then split2\h = split2\h + 360
split2Rgb = hsv2rgb(split2\h, split2\s, split2\v)

;display splits...
Color 255, 255, 255
Text 10, boxTop, "Hex RGB  #" + Right(Hex$(hexNum),6) + RSet$("(" + Int(c\h) + " deg)", 20)
Text 10, boxTop + (yOffset * 1), "Split 1  #" + Right(Hex$(split1Rgb),6) + RSet$("(" + Int(split1\h) + " deg)", 20)
Text 10, boxTop + (yOffset * 2), "Opposite #" + Right(Hex$(oppRgb),6) + RSet$("(" + Int(opp\h) + " deg)", 20)
Text 10, boxTop + (yOffset * 3), "Split 2  #" + Right(Hex$(split2Rgb),6) + RSet$("(" + Int(split2\h) + " deg)", 20)

Text 10, boxTop + (yOffset * 5), "Split 1 & 2 are the hues either"
Text 10, boxTop + (yOffset * 5) + fontH, "side of the opposite!"

;display colour boxes...
Color getRed(hexNum), getGreen(hexNum), getBlue(hexNum)
Rect boxLeft + boxWidth + 20, boxTop, 20, fontH
Color getRed(oppRgb), getGreen(oppRgb), getBlue(oppRgb)
Rect boxLeft + boxWidth + 20, boxTop + yOffset, 20, fontH
Color getRed(split1Rgb), getGreen(split1Rgb), getBlue(split1Rgb)
Rect boxLeft + boxWidth + 20, boxTop + (yOffset * 2), 20, fontH
Color getRed(split2Rgb), getGreen(split2Rgb), getBlue(split2Rgb)
Rect boxLeft + boxWidth + 20, boxTop + (yOffset * 3), 20, fontH

WaitKey

End