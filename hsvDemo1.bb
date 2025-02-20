;--------------------------------------------------------------------------------------
;HSV Demo 1 - V2.0, Nov 2002
;
;	Author:		Ghost Dancer, Aurora-Soft
;	Website:	www.aurora-soft.co.uk
;	Contact:	colour@aurora-soft.co.uk
;
;	Post any comments or bug reports on the above website or email address.
;
;	This demo is an example of how to change a colour in an image whilst preserving
;	the luminosity. It also illustrates some uses of the new drawColourRect() function.
;
;	On the image in this example, you can change the yellow highlights and the blue armour.
;	Depending on which colour selector you use, it will change the default Hue 
;	(yellow or orange) to the selected Hue by adding the difference between
;	the two values. This preserves the luminosity of the pixel - neat huh?
;
;	If all this sounds confusing, just run the code and see what happens...
;
;	Version History
;	V2.0
;		- Now uses the new drawColourBar() & getBarColour() functions :-)
;		- Extra colour bar added to show how to change different colours in one image
;		- colourImage() - can now specify the Hue to change & the hue & saturation checks
;		- Better anotaion of code (slightly!)
;--------------------------------------------------------------------------------------

AppTitle "HSV Demo 1"

;include the colourSpace library...
Include "colourSpace.bb"

;constants & variables...
Const screenWidth = 640, screenHeight = 480, portLeft = 40, portTop = 90
Const boxLeft = 40, boxLeft2 = 10, boxTop = 60, boxSize = 20
hsv.hsv = New hsv		;set up our custom type for storing HSV colour values

;set up display...
Graphics screenWidth, screenHeight, 16, 2
ShowPointer
SetBuffer BackBuffer()

;create object for entire screen
scrnImg = CreateImage(screenWidth, screenHeight)

;draw image...
tempImg = LoadImage("newPortrait.bmp")
DrawBlock tempImg, portLeft, portTop	
Global portWidth = ImageWidth(tempImg), portHeight = ImageHeight(tempImg)
FreeImage tempImg

;store portrait pixels in array for faster processing
;this also allows us to work from the original colour values
Dim pixel(portWidth, portHeight)
LockBuffer
For y = portTop To portTop + portHeight
	For x = portLeft To portLeft + portWidth
		;get pixel rgb and mask out alpha bits
		pixel(x - portLeft, y - portTop) = ReadPixelFast(x, y) And $ffffff
	Next
Next
UnlockBuffer

;draw colour selectors...
;note - you can get a nice rainbow effect by making the horizontal a vertical & vice versa!
drawColourRect(boxLeft, boxTop, 360, boxSize)
drawColourRect(boxLeft2, boxTop, boxSize, 360, True, 1.0, 0.8)

;display some text...
Color $ff, $ff, $ff
Text 10, 10, "Click colour selectors to change image."
Text portLeft + portWidth + 20, portTop, "Highlight colour"
Text portLeft, 370 + boxSize, "Armour colour"

;store current screen...
GrabImage scrnImg, 0, 0

frameTimer = CreateTimer(30)	;30 frames per second
While Not KeyDown(1)
	WaitTimer(frameTimer)					;pause until timer reaches 30
	DrawBlock scrnImg, 0, 0					;draw main screen image
	
	mx = MouseX() : my = MouseY()

	;colour selection
	If MouseDown(1) Then
		hueToChange = 500	;set to arbitrary value (outside range -359 to 359) for when no colour is selected
		
		If getBarColour(boxLeft, boxTop, 360, boxSize, mx, my) <> -1
			hueToChange = 55
			hueRange1 = 40
			hueRange2 = 70
			saturation# = 0.1
		ElseIf getBarColour(boxLeft2, boxTop, boxSize, 360, mx, my) <> -1
			hueToChange = 215
			hueRange1 = 185
			hueRange2 = 245
			saturation# = 0.1
		End If

		;if hueToChange is not the arbitrary value, a new colour is selected
		If hueToChange <> 500
			hsv = rgb2hsv(ReadPixel(mx, my) And $ffffff, hsv)
			newHue = hsv\h
			If newHue <> oldHue Then
				oldHue = newHue
				colourImage(newHue, hueToChange, hueRange1, hueRange2, saturation#)
				GrabImage scrnImg, 0, 0
			End If
		End If
	End If

	Flip						;flip back buffer to front
Wend

;stop it & tidy up...
FreeImage scrnImg
End


;-------------------------------------------------------------------
;function to recolour pixels array using specified hue...
;
;note - I thought about making this a standard library function, but since you
;really need to read it into an array first (it will be too slow otherwise),
;things would start to get messy!
Function colourImage(newHue, hueToChange, hueRange1, hueRange2, saturation#)
	c.hsv = New hsv
	
	;calculate the difference in hue between selected hue and default hue (yellow)
	colourMod = newHue - hueToChange
	
	LockBuffer
	For y = portTop To portTop + portHeight
		For x = portLeft To portLeft + portWidth
			thisPix = pixel(x - portLeft, y - portTop)
			If  thisPix > $000000 And thisPix < $ffffff Then		;make sure it's not black or white
				c = rgb2hsv(thisPix, c)								;convert current pixel to hsv
				If c\h >= hueRange1 And c\h <= hueRange2 And c\s >= saturation# Then
					;if within hue & saturation range...
					c\h = c\h + colourMod							;add colour colour difference to pixel
					If c\h >= 360 Then c\h = c\h - 360				;make sure colour is within 360 degrees
					thisPix = hsv2rgb(c\h, c\s, c\v)				;convert new colour to rgb
					WritePixelFast x, y, thisPix					;write new pixel
				End If
			End If
		Next
	Next
	UnlockBuffer
	
	Delete c
End Function