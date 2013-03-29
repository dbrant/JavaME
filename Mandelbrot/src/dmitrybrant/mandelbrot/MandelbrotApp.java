/*
    Copyright (c) 2009-2013 Dmitry Brant <me@dmitrybrant.com>
    
    This software is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
  
    You should have received a copy of the GNU General Public License along
    with this program; if not, write the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

 */
package dmitrybrant.mandelbrot;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class MandelbrotApp extends MIDlet implements CommandListener {
    private Display myDisplay;
    private MandelbrotCanvas myCanvas;
    private Command exit = new Command ("Exit", Command.EXIT, 1);
    private Command about = new Command ("About", Command.ITEM, 2);

    private Thread currentThread = null;
    private boolean threadTerminateFlag = false;

    private int[] colorPalette;
    private int[] scanlineBuffer;
    private int screenWidth = 0, screenHeight = 0;

    public float rangex1, rangex2, rangey1, rangey2;
    public int numIterations = 24;


    public MandelbrotApp () {
        super ();

        myDisplay = Display.getDisplay (this);
        myCanvas = new MandelbrotCanvas (this);
        myCanvas.setCommandListener (this);
        myCanvas.addCommand (exit);
        myCanvas.addCommand(about);

        scanlineBuffer = null;
        colorPalette = new int[256];

        for(int i=0; i<64; i++)
            colorPalette[i] = (((i * 4) << 8) | ((63 - i) * 4));
        for(int i=64; i<128; i++)
            colorPalette[i] = ((((i - 64) * 4) << 16) | (((127 - i) * 4) << 8));
        for(int i=128; i<192; i++)
            colorPalette[i] = (((255) << 16) | ((i - 128) * 4));
        for(int i=192; i<256; i++)
            colorPalette[i] = ((((255 - i) * 4) << 16) | (255));

    }

    void initMinMax(){
        rangex1 = -2.0f;
        rangex2 = 1.0f;
        rangey1 = -1.5f;
        rangey2 = 1.5f;
        if(screenWidth > screenHeight){
            float pad =  (rangex2 - rangex1) / ((float)screenHeight / ((float)(screenWidth - screenHeight)/2));
            rangex1 -= pad;
            rangex2 += pad;
        }
        else if(screenHeight > screenWidth){
            float pad =  (rangey2 - rangey1) / ((float)screenWidth / ((float)(screenHeight - screenWidth)/2));
            rangey1 -= pad;
            rangey2 += pad;
        }
    }


    public void init () throws MIDletStateChangeException {
    }

    public void startApp () throws MIDletStateChangeException {
        myDisplay.setCurrent (myCanvas);
        RenderMandelbrot();
    }

    public void pauseApp () {
    }

    public void destroyApp (boolean cond) {
        TerminateThread();
        myDisplay.setCurrent ((Displayable) null);
        myCanvas.destroy ();
        notifyDestroyed ();
    }

    public void TerminateThread(){
        try{
            if(currentThread != null){
                threadTerminateFlag = true;
                if(currentThread.isAlive())
                    currentThread.join();
                currentThread = null;
            }
        }catch(Exception ex){}
    }


    public void RenderMandelbrot(){
        TerminateThread();

        currentThread = new Thread(){
            public void run(){
                threadTerminateFlag = false;
                if((myCanvas.getWidth() != screenWidth) || (myCanvas.getHeight() != screenHeight)){
                    screenWidth = myCanvas.getWidth();
                    screenHeight = myCanvas.getHeight();
                    scanlineBuffer = new int[screenWidth * screenHeight];
                    initMinMax();
                }

                float bmpWidth = (float)screenWidth;
                float bmpHeight = (float)screenHeight;

                float x, y, xsquare, ysquare, dx, dy, bail = 4, j, p;
                int i, mul, col;
                int xpos, ypos;

                //float rangex1 = -0.1795247F, rangex2 = -0.1404622F, rangey1 = 1.013583F, rangey2 = 1.053083F, p;
                float[] q = null;

                if(screenWidth > screenHeight) q = new float[screenWidth + 1];
                else q = new float[screenHeight + 1];

                mul = 255 / numIterations;
                dx = (rangex2 - rangex1) / bmpWidth;
                dy = (rangey2 - rangey1) / bmpHeight;

                q[0] = rangey2;
                for(i=1; i < q.length; i++) q[i] = q[i - 1] - dy;


                xpos = 0; ypos = 0;

                for(p = rangex1; p <= rangex2; p += dx){
                    i = 0;

                    for(j = rangey1; j <= rangey2; j += dy){
                        x = 0; y = 0; xsquare = 0; ysquare = 0; col = 1;

                        while(true){
                            if(col > numIterations){
                                scanlineBuffer[ypos*screenWidth + xpos] = 0;
                                break;
                            }
                            if((xsquare + ysquare) > bail){
                                scanlineBuffer[ypos*screenWidth + xpos] = colorPalette[(col*mul)%255];
                                break;
                            }
                            xsquare = x * x;
                            ysquare = y * y;
                            y *= x;

                            y += (y + q[i]);
                            x = xsquare - ysquare + p;
                            col++;
                        }
                        i++;
                        ypos++;
                        if(ypos >= screenHeight) break;
                    }

                    if((xpos % 8) == 0) myCanvas.repaint();
                    if(threadTerminateFlag) break;

                    xpos++;
                    if(xpos >= screenWidth) break;
                    ypos = 0;
                }
                myCanvas.repaint();
            }
        };

        currentThread.start();
    }



    public void paint (Graphics g) {

        g.drawRGB(scanlineBuffer, 0, screenWidth, 0, 0, screenWidth, screenHeight, false);

        g.setColor(0xFFFFFF);
        int fontHeight = g.getFont().getHeight();
        int strY = 4;
        g.drawString("(C) Dmitry Brant", 4, strY, 0); strY += fontHeight;
        g.drawString("Iterations: " + Integer.toString(numIterations), 4, strY, 0); strY += fontHeight;
    }

    public void commandAction (Command cmd, Displayable disp) {
        if (cmd == exit) {
            destroyApp (true);
        }
        else if(cmd == about){
            Alert alert = new Alert ("About...");
            alert.setType (AlertType.INFO);
            alert.setTimeout (Alert.FOREVER);
            alert.setString ("Copyright 2009 Dmitry Brant.\nhttp://dmitrybrant.com");
            myDisplay.setCurrent (alert);
        }
    }
}
