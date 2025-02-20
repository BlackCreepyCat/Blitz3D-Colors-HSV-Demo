;--------------------------------------------------------------------------------------
;HSV Demo 3 - V1.0, Nov 2002
;
;	Author:		Ghost Dancer, Aurora-Soft
;	Website:	www.aurora-soft.co.uk
;	Contact:	colour@aurora-soft.co.uk
;
;	Post any comments or bug reports on the above website or email address.
;
;	This example shows how to use some of the new functions to create a palette 
;	requester type program.
;--------------------------------------------------------------------------------------

AppTitle "HSV Demo 3"

;include the colourSpace library...
Include "colourSpace.bb"

;constants & variables...
Const screenWidth = 640, screenHeight = 480, xOffset = screenWidth / 2, yOffset = screenHeight / 2
Const circleRadius = 120
Const barX = xOffset + circleRadius + 20, barY = yOffset - circleRadius
Const barWidth = 20, barHeight = circleRadius * 2
Const valueMarkerX = barX + 10
selected.hsv = New hsv		;set up custom type for our currently selected colour
c.hsv = New hsv				;set up custom type for temporary colour store

;set up display...
Graphics screenWidth, screenHeight, 16, 2
ShowPointer

;create graphic for circle marker
circleMarkerImg = CreateImage(13, 13)
SetBuffer ImageBuffer(circleMarkerImg)
Color 8, 8, 8 : Oval 1, 1, 12, 12				;outline
Color 0, 0, 0 : Oval 4, 4, 6, 6					;cut hole
Color $ff, $ff, $ff : Oval 2, 2, 10, 10, False	;white highlight
MidHandle circleMarkerImg

;create graphic for value marker
valueMarkerImg = CreateImage(barWidth + 6, 7)
SetBuffer ImageBuffer(valueMarkerImg)
Rect 1, 1, barWidth + 4, 5, False
Color 8, 8, 8
Rect 0, 0, barWidth + 6, 7, False
Rect 2, 2, barWidth + 2, 3, False
MidHandle valueMarkerImg

;double buffering
SetBuffer BackBuffer()

;display some text...
Color $ff, $ff, $ff
Text 10, 10, "Click colour selector to view HSV & RGB values."

;draw brightness bar
Rect barX - 3, barY - 3, barWidth + 6, barHeight + 6, False		;border
drawBrightBar(barX, barY, barWidth, barHeight)					;value bar

;draw colour wheel...
drawColourWheel(xOffset, yOffset, circleRadius)

;create object for entire screen
scrnImg = CreateImage(screenWidth, screenHeight)

;store current screen so we can quickly redraw it every loop...
GrabImage scrnImg, 0, 0

;set initial colour & marker positions...
rgb = $ffffff
selected = rgb2hsv(rgb, selected)
circleMarkerX = xOffset : circleMarkerY = yOffset
valueMarkerY = barY

;set timer and main loop...
frameTimer = CreateTimer(40)	;40 frames per second
While Not KeyDown(1)
	WaitTimer(frameTimer)					;pause until timer reaches 30
	DrawBlock scrnImg, 0, 0					;draw main screen image
	
	mx = MouseX() : my = MouseY()			;get mouse position
	
	If MouseDown(1) Then
		;get value (brigthness) from bar
		selectedCol = getBarColour(barX, barY, barWidth, barHeight, mx, my)
		If selectedCol <> -1 Then
			c = rgb2hsv(selectedCol, c)
			selected\v = c\v
			valueMarkerY = my
		End If

		;get hue & saturation from colour wheel
		selectedCol = getCircleColour(xOffset, yOffset, circleRadius, mx, my)
		If selectedCol <> -1 Then
			c = rgb2hsv(selectedCol, c)
			selected\h = c\h : selected\s = c\s
			circleMarkerX = mx : circleMarkerY = my
		End If
		
		;set colour to new values
		rgb = hsv2rgb(selected\h, selected\s, selected\v)
	End If
	
	;draw markers...
	DrawImage circleMarkerImg, circleMarkerX, circleMarkerY
	DrawImage valueMarkerImg, valueMarkerX, valueMarkerY
	
	;set drawing colour to display info
	Color $ff, $ff, $ff
	
	;display hsv values
	If selected\h <> -1 Then hue$ = LSet$(Int(selected\h), 3) + " deg" Else hue$ = "-"
	Text 10, 50, "H: " + hue
	Text 10, 65, "S: " + Int(selected\s * 100) + "%"
	Text 10, 80, "V: " + Int(selected\v * 100) + "%"
	
	;display rgb values
	Text 10, 110, "R: " + getRed(rgb)
	Text 10, 125, "G: " + getGreen(rgb)
	Text 10, 140, "B: " + getBlue(rgb)
	Text 10, 155, "$: " + Right(Hex(rgb), 6)
	
	;draw rectangle in current colour
	Color getRed(rgb), getGreen(rgb), getBlue(rgb)
	Rect 125, 50, 45, 45
	
	Flip						;flip back buffer to front
Wend

;free images...
FreeImage valueMarkerImg
FreeImage circleMarkerImg
FreeImage scrnImg

End