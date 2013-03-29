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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;


class MandelbrotCanvas extends Canvas {
    MandelbrotApp myApp;

    MandelbrotCanvas (MandelbrotApp mandelTestlet) {
        myApp = mandelTestlet;
    }

    void init () {
    }

    void destroy () {
    }


    protected void paint (Graphics g) {
        myApp.paint (g);
    }

    protected void keyPressed (int key) {
        int action = getGameAction (key);

        float xScale = (myApp.rangex2 - myApp.rangex1);
        float yScale = (myApp.rangey2 - myApp.rangey1);

        boolean gotAction = true, gotKey = true;
        switch (action) {
        case LEFT:
            myApp.rangex1 += (xScale / 16.0F);
            myApp.rangex2 += (xScale / 16.0F);
            break;
        case RIGHT:
            myApp.rangex1 -= (xScale / 16.0F);
            myApp.rangex2 -= (xScale / 16.0F);
            break;
        case UP:
            myApp.rangey1 -= (yScale / 16.0F);
            myApp.rangey2 -= (yScale / 16.0F);
            break;
        case DOWN:
            myApp.rangey1 += (yScale / 16.0F);
            myApp.rangey2 += (yScale / 16.0F);
            break;
        case FIRE:
        default:
            gotAction = false;
        }

        if(!gotAction){
            switch (key){
            case KEY_NUM1:
                myApp.rangex1 -= (xScale / 4.0F);
                myApp.rangex2 += (xScale / 4.0F);
                myApp.rangey1 -= (yScale / 4.0F);
                myApp.rangey2 += (yScale / 4.0F);
                break;
            case KEY_NUM3:
                myApp.rangex1 += (xScale / 4.0F);
                myApp.rangex2 -= (xScale / 4.0F);
                myApp.rangey1 += (yScale / 4.0F);
                myApp.rangey2 -= (yScale / 4.0F);
                break;
            case KEY_NUM7:
                myApp.numIterations-=4; if(myApp.numIterations < 2) myApp.numIterations = 2;
                break;
            case KEY_NUM9:
                myApp.numIterations+=4;
                break;
            default:
                gotKey = false;
            }
        }

        if(gotAction || gotKey)
            myApp.RenderMandelbrot();
    }

}
