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
package dmitrybrant.primespiral;


import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;


class PrimeSpiralCanvas extends Canvas {
    PrimeSpiralApp myApp;

    private int screenWidth = 0, screenHeight = 0;

    private int numIntegers = 1000;
    private int cellWidth = 4;
    private int startX, startY;

    private int[] colors = { 0xFFFFFF, 0xFF0000, 0xFF00, 0xFF, 0xFFFF00, 0xFFFF, 0xFF00FF };
    private int whichColor = 3;


    PrimeSpiralCanvas (PrimeSpiralApp primeTestlet) {
        myApp = primeTestlet;
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

        boolean[] sieve = new boolean[numIntegers+1];

        for(int i=0; i<numIntegers; i++) sieve[i] = false;
        int temp, upTo = (int)(numIntegers / 2) + 2;
        sieve[0] = true; sieve[1] = true;
        for(int i=2; i<upTo; i++){
            temp = i;
            while(true){
                temp += i;
                if(temp > numIntegers) break;
                sieve[temp] = true;
            }
        }

        //now draw it
        g.setColor(colors[whichColor]);

        int direction = 0; //0=right, 1=up, 2=left, 3=down
        int numMoves = 1, moveMod = 0, currentMoves = numMoves;
        int x = startX, y = startY;
        int numPrimes = 0;

        for(int i=1; i<numIntegers; i++){

            if(!sieve[i]){
                g.fillRect(x, y, cellWidth, cellWidth);
                numPrimes++;
            }

            //mark the beginning of the spiral
            if(i==1){
                g.setColor(0xFF0000);
                g.fillRect(x+(cellWidth/2), y+(cellWidth/2), 1, 1);
                g.setColor(colors[whichColor]);
            }

            if(direction == 0){
                x += cellWidth;
            }else if(direction == 1){
                y -= cellWidth;
            }else if(direction == 2){
                x -= cellWidth;
            }else if(direction == 3){
                y += cellWidth;
            }

            currentMoves--;
            if(currentMoves == 0){
                moveMod++;
                if(moveMod > 1){
                    moveMod = 0;
                    numMoves++;
                }
                currentMoves = numMoves;
                direction++;
                if(direction > 3) direction = 0;
            }

        }

        g.setColor(200, 200, 200);

        int fontHeight = g.getFont().getHeight();
        int strY = 4;
        g.drawString("(C) Dmitry Brant", 4, strY, 0); strY += fontHeight;
        g.drawString("Integers: " + Integer.toString(numIntegers), 4, strY, 0); strY += fontHeight;
        g.drawString("Primes: " + Integer.toString(numPrimes), 4, strY, 0); strY += fontHeight;

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
            whichColor++; whichColor %= 7;
        }

        switch (key){
            case KEY_NUM1:
                numIntegers = numIntegers * 75 / 100; if(numIntegers < 10) numIntegers = 10;
                break;
            case KEY_NUM3:
                numIntegers = numIntegers * 125 / 100;
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

    protected void keyRepeated (int key) {
        keyPressed(key);
    }

}
