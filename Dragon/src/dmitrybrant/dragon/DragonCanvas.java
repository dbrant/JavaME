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
package dmitrybrant.dragon;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;


class DragonCanvas extends Canvas {
    DragonApp myApp;

    private int screenWidth = 0, screenHeight = 0;

    private int numIterations = 6;
    private int cellWidth = 4;
    private int startX, startY;

    private int[] colors = { 0x70FF, 0xFF00, 0xFF0000, 0xFFFF00, 0xFFFF, 0xFF00FF };
    private int whichColor = 0;


    DragonCanvas (DragonApp dragonTestlet) {
        myApp = dragonTestlet;
    }

    void init () {
    }

    void destroy () {
    }


    public void paint (Graphics g) {

        if((getWidth() != screenWidth) || (getHeight() != screenHeight)){
            screenWidth = getWidth();
            screenHeight = getHeight();
            startX = screenWidth / 2;
            startY = screenHeight / 2;
        }

        g.setColor(0x00000000);
        g.fillRect(0, 0, screenWidth, screenHeight);

        boolean[] poo = new boolean[(1 << (numIterations + 1))];
        poo[0] = true;

        int curIndex = 1, tempIndex;
        for(int i=1; i<=numIterations; i++){
            poo[curIndex] = true;
            tempIndex = curIndex - 1;
            curIndex++;
            for(int j = tempIndex; j >= 0; j--){
                poo[curIndex] = !poo[j];
                curIndex++;
            }
        }

        //now draw it
        g.setColor(colors[whichColor]);
        int x = startX, y = startY;
        int prevX = x, prevY = y, direction = 0; //0=right, 1=down, 2=left, 3=up

        for(int i=0; i<curIndex; i++){
            if(poo[i]){
                if(direction == 0){ direction = 1; y += cellWidth; }
                else if(direction == 1){ direction = 2; x -= cellWidth; }
                else if(direction == 2){ direction = 3; y -= cellWidth; }
                else if(direction == 3){ direction = 0; x += cellWidth; }
            }else{
                if(direction == 0){ direction = 3; y -= cellWidth; }
                else if(direction == 1){ direction = 0; x += cellWidth; }
                else if(direction == 2){ direction = 1; y += cellWidth; }
                else if(direction == 3){ direction = 2; x -= cellWidth; }
            }

            g.drawLine(prevX, prevY, x, y);

            prevX = x; prevY = y;
        }

        g.setColor(200, 200, 200);
        int fontHeight = g.getFont().getHeight();
        int strY = 4;
        g.drawString("(C) Dmitry Brant", 4, strY, 0); strY += fontHeight;
        g.drawString("Iterations: " + Integer.toString(numIterations), 4, strY, 0); strY += fontHeight;
    }


    protected void keyPressed (int key) {
        int action = getGameAction (key);

        switch (action) {
        case RIGHT:
            startX += 16;
            break;
        case LEFT:
            startX -= 16;
            break;
        case UP:
            startY -= 16;
            break;
        case DOWN:
            startY += 16;
            break;
        case FIRE:
            whichColor++; whichColor %= 6;
        }

        switch (key){
            case KEY_NUM1:
                numIterations--; if(numIterations < 1) numIterations = 1;
                break;
            case KEY_NUM3:
                numIterations++;
                break;
            case KEY_NUM7:
                cellWidth--; if(cellWidth < 1) cellWidth = 1;
                break;
            case KEY_NUM9:
                cellWidth++;
                break;
        }

        repaint ();
    }

}
