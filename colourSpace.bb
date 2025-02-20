;***************************************************************************************************
;Colour Space library - V2.01, Nov 2002
;
;	Author:		Ghost Dancer, Aurora-Soft
;	Website:	www.aurora-soft.co.uk
;	Contact:	colour@aurora-soft.co.uk
;
;	Thanks to basocgamer2, Cyberseth & Dark Eagle for their help with drawing a filled circle,
;	and thanks to djwoodgate for helping to solve the memory leak problem :-)
;
;	Please read included documentation and examples for full details.  
;	Post any comments or bug reports on the above website or email address.
;	
;	Use:
;	Include this file in your code. Do not edit this source file or you may 
;	experience compatibility difficulties with future versions.
;
;	Version History:
;	V2.01
;		- Fixed memory leek in rgb2hsv() - IMPORTANT NOTE, there is now an extra parameter which is
;		  required for this function to work correctly. See function description below for details.
;		  A nice side effect to this, is that it is now a little faster :-)
;
;	V2.0
;		- Added 5 new functions (see below)
;		- Fixed bug in hsv2rgb() so it now returns grey correctly if saturation is 0
;		- Fixed bug in getRed() so it now masks out alpha bits, thus returning correct value!
;		- Optimised rgb2hsv() by using minCol() & maxCol() code directly instead of function calls
;		  These functions were only used for this so I have removed them
;		  (not much use for anything else, but let me know if you want them back!)
;		- hex2Int() now accepts the $ character in front of the hex number
;		- Better anotaion of code
;
;	Functions:
;	hex2int(hexRef$)				- returns the integer value of a hex string
;	getRed(rgb)						- returns the Red component of an RGB colour value
;	getGreen(rgb)					- returns the Green component of an RGB colour value
;	getBlue(rgb)					- returns the Red component of an RGB colour value
;	rgb(r, g, b)					- returns RGB value from Red, Gree, & Blue components					
;	rgb2hsv.hsv(rgb, temphsv.hsv)	- returns HSV (custom type) of an RGB value
;	hsv2rgb(h#, s#, v#)				- returns RGB value of an HSV values
;
;	Functions added in V2.0:
;	drawColourRect(x, y, w, h, vert, sat, val)	- draw horizontal or vertical colour bar
;	drawColourWheel(x, y, r)					- draw colour wheel 
;	drawBrightBar(x, y, w, h, vert)				- draw vertical or horizontal brightness bar
;	getCircleColour(x, y, r, mx, my)			- get RGB value of a pixel in a colour wheel
;	getBarColour(x, y, w, h, mx, my)			- get RGB value of a pixel in a rectangle
;***************************************************************************************************


;create a custom Type For HSV colour
Type hsv
	Field h#, s#, v#
End Type


;*******************************************************************
;Functions
;*******************************************************************

;-------------------------------------------------------------------
Function hex2int(hexRef$)
;-------------------------------------------------------------------
;Convert hex string to decimal integer
;
;Parameters:
;hexRef$	- hex string to convert (e.g. "$ffffff", or "ffffff")
;
;Return value:
;none
;-------------------------------------------------------------------

	If Left(hexRef, 1) = "$" Then hexRef = Right(hexRef, Len(hexRef) - 1)	;remove $ if present
	
	hexRef = Lower$(hexRef)
	hexNum = 0
	
	For n = Len(hexRef) To 1 Step -1
		thisNum = 0
		ascii = Asc(Mid(hexRef, n, 1))
		If ascii >= 48 And ascii <= 57 Then thisNum = ascii - 48
		If ascii >= 97 And ascii <= 122 Then thisNum = ascii - 97 + 10
		If thisNum >= 0 Then
			m = 6 - n + 1
			mult = (16 ^ (m-1))
			hexNum = hexNum + (thisNum * mult)
		End If
	Next

	Return hexNum
End Function


;-------------------------------------------------------------------
Function getRed(rgb)
;-------------------------------------------------------------------
;Get red component of RGB colour value
;
;Parameters:
;rgb	- RGB value that you want the red component from
;
;Return value:
;red value (0 to 255)
;-------------------------------------------------------------------

	Return rgb Shr 16 And $ff
End Function


;-------------------------------------------------------------------
Function getGreen(rgb)
;-------------------------------------------------------------------
;Get green component of RGB colour value
;
;Parameters:
;rgb	- RGB value that you want the green component from
;
;Return value:
;green value (0 to 255)
;-------------------------------------------------------------------

	Return (rgb Shr 8) And $ff
End Function


;-------------------------------------------------------------------
Function getBlue(rgb)
;-------------------------------------------------------------------
;Get blue component of rgb colour value
;
;Parameters:
;rgb	- RGB value that you want the blue component from
;
;Return value:
;blue value (0 to 255)
;-------------------------------------------------------------------

	Return rgb And $ff
End Function


;-------------------------------------------------------------------
Function rgb(r, g, b)
;-------------------------------------------------------------------
;Convert R,G,B components to single RGB value
;
;Parameters:
;r	- red value
;g	- green value
;b	- blue value
;
;Return value:
;rgb value
;-------------------------------------------------------------------

	Return (r Shl 16) + (g Shl 8) + b
End Function


;-------------------------------------------------------------------
Function rgb2hsv.hsv(rgb, temphsv.hsv)
;-------------------------------------------------------------------
;Convert RGB to HSV
;
;Parameters:
;rgb			- RGB value that you want convert
;temphsv.hsv	- Added in V2.01, it is required to prevent the
;				  memory leakage problem. This needs to be the same
;				  hsv type pointer that the function returns to.
;				  e.g.	myhsv.hsv = New hsv
;				  		myhsv = rgb2hsv($ff0000, myhsv)
;
;Return value:
;custom hsv type defined in this library
;-------------------------------------------------------------------

	;RGB components in  range 0 to 1
	r# = getRed(rgb) / 255.0
	g# = getGreen(rgb) / 255.0
	b# = getBlue(rgb) / 255.0

	;min value	
	If r < g Then minVal# = r Else minVal# = g
	If b < minVal Then minVal = b

	;max value	
	If r > g Then maxVal# = r Else maxVal# = g
	If b > maxVal Then maxVal = b

	;calculate difference
	diff# = maxVal - minVal
	
	temphsv\v = maxVal
	
	If maxVal = 0 Then
		temphsv\s = 0
		temphsv\h = -1		;h is undefined
	Else
		temphsv\s = diff / maxVal
	
		If r = maxVal Then
			temphsv\h = (g - b) / diff
		ElseIf g = maxVal Then
			temphsv\h = 2 + (b - r) / diff
		Else
			temphsv\h = 4 + (r - g) / diff
		EndIf
	
		temphsv\h = temphsv\h * 60
		If temphsv\h < 0 Then temphsv\h = temphsv\h + 360
	EndIf

	Return temphsv
End Function


;-------------------------------------------------------------------
Function hsv2rgb(h#, s#, v#)
;-------------------------------------------------------------------
;Convert HSV to RGB
;
;Parameters:
;h#	- hue (in degrees, 0 to 350)
;s#	- saturation (0.0 to 1.0)
;v#	- value (brightness, 0.0 to 1.0)
;
;Return value:
;rgb value
;-------------------------------------------------------------------

	If s = 0 Then
		;saturation of 0 = grey
		r# = v : g# = v : b# = v
	Else
		h = h / 60
		i = Floor(h)
		f# = h - i
		p# = v * (1 - s)
		q# = v * (1 - s * f)
		t# = v * (1 - s * (1 - f))

		Select i
		Case 0
			r# = v
			g# = t
			b# = p
		Case 1
			r# = q
			g# = v
			b# = p
		Case 2
			r# = p
			g# = v
			b# = t
		Case 3
			r# = p
			g# = q
			b# = v
		Case 4
			r# = t
			g# = p
			b# = v
		Default
			r# = v
			g# = p
			b# = q
		End Select		
	EndIf

	r = r * 255
	g = g * 255
	b = b * 255

	Return rgb(r, g, b)
End Function 


;*******************************************************************
;Functions added in V2.0
;*******************************************************************

;-------------------------------------------------------------------
Function drawColourRect(x, y, w, h, vert = False, sat# = 1.0, val# = 1.0)
;-------------------------------------------------------------------
;Draw a horizontal or vertical colour bar
;
;Parameters:
;x & y	- position of top left of bar
;w & h	- width & height of bar
;vert	- set to true if yoy want a vertical bar  (optional, default = false)
;sat#	- saturation to apply to bar, ranging from 0 to 1 (optional, default = 1)
;val#	- value to apply to bar, ranging from 0 to 1 (optional, default = 1)
;
;Return value:
;none
;-------------------------------------------------------------------

	;width & height are added to x, y position so subtract 1 to draw correct size...
	w = w - 1
	h = h - 1

	If vert Then
		;draw vertical bar...
		hueMod# = 360 / h									;hue increment depends on height
		For yy = 0 To h
			hue = yy * hueMod								;calc hue for this position
			rgb = hsv2rgb(hue, sat, val)					;get RGB value for this hue
			Color getRed(rgb), getBlue(rgb), getGreen(rgb)	;set drawing colour
			Line x, y + yy, x + w, y + yy					;draw line
		Next
	Else
		;draw horizontal bar...
		hueMod# = 360 / w									;hue increment depends on width
		For xx = 0 To w
			hue = xx * hueMod								;calc hue for this position
			rgb = hsv2rgb(hue, sat, val)					;get RGB value for this hue
			Color getRed(rgb), getBlue(rgb), getGreen(rgb)	;set drawing colour
			Line x + xx, y, x + xx, y + h					;draw line
		Next
	End If
End Function


;-------------------------------------------------------------------
Function drawBrightBar(x, y, w, h, vert = True)
;-------------------------------------------------------------------
;Draw a vertical or horizontal brightness bar
;
;Parameters:
;x & y		- position of top left of bar
;w & h		- width & height of bar
;vert		- set to true if yoy want a vertical bar  (optional, default = false)
;
;Return value:
;none
;-------------------------------------------------------------------

	If vert Then
		;draw vertical bar...
		valMod# = 1.0 / h									;val increment depends on height
		For yy = 1 To h
			val# = (yy * valMod) * -1						;calc val for this position
			rgb = hsv2rgb(0, 0.0, val)						;get RGB (grey) value for this val
			Color getRed(rgb), getBlue(rgb), getGreen(rgb)	;set drawing colour
			Line x, y + yy - 1, x + w - 1, y + yy - 1		;draw line
		Next
	Else
		;draw horizontal bar...
		valMod# = 1.0 / w									;val increment depends on height
		For xx = 1 To w
			val# = (xx * valMod) * -1						;calc val for this position
			rgb = hsv2rgb(0, 0.0, val)						;get RGB (grey) value for this val
			Color getRed(rgb), getBlue(rgb), getGreen(rgb)	;set drawing colour
			Line x + xx - 1, y, x + xx - 1, y + h - 1		;draw line
		Next
	End If
End Function


;-------------------------------------------------------------------
Function drawColourWheel(x, y, r)
;-------------------------------------------------------------------
;Draw a colour wheel
;Important - this is not intended to be usd for real time operations,
;I recommend that you draw it once to an image, and then use that!
;
;Parameters:
;x & y		- coords of circle center
;r			- radius of circle
;
;Return value:
;none
;-------------------------------------------------------------------

	LockBuffer
	
	For xx = -r To r
	    For yy = -r To r
	        If (xx * xx + yy * yy) <= r * r + r
				hue = ATan2(xx, yy)
				If hue < 0 Then hue = hue + 360
				saturation# = Sqr(xx * xx + yy * yy) / r
				If saturation > 1 Then saturation = 1
				rgb = hsv2rgb(hue, saturation, 1)
	            WritePixelFast x + xx, y + yy, rgb
	        End If
	    Next
	Next

	WritePixelFast x, y, $ffffff	;-)
	UnlockBuffer
End Function


;-------------------------------------------------------------------
Function getBarColour(x, y, w, h, mx, my)
;-------------------------------------------------------------------
;Get rgb of specific pixel in a rectangle
;
;Parameters:
;x & y		- coords of circle center
;w & h		- width & height of bar
;mx & my	- pixel to get value from (usually mouse)
;
;Return value:
;rgb value of selected colour, Or -1 If outside circle
;-------------------------------------------------------------------

	rgb = -1

	If mx >= x And mx < x + w And my >= y And my < y + h Then rgb = ReadPixel(mx, my)

	Return rgb
End Function



;-------------------------------------------------------------------
Function getCircleColour(x, y, r, mx, my)
;-------------------------------------------------------------------
;Get rgb of specific pixel in a circle
;
;Parameters:
;x & y		- coords of circle center
;r			- radius of circle
;mx & my	- pixel to get value from (usually mouse)
;
;Return value:
;rgb value of selected colour, Or -1 If outside circle
;-------------------------------------------------------------------

	rgb = -1

	distance = Sqr((mx-x) * (mx-x) + (my-y) * (my-y))
	If distance <= r Then rgb = ReadPixel(mx, my)

	Return rgb
End Function