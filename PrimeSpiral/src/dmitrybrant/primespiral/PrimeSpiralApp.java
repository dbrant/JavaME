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

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class PrimeSpiralApp extends MIDlet implements CommandListener {
    private Display myDisplay;
    private PrimeSpiralCanvas myCanvas;

    private Command exit = new Command ("Exit", Command.EXIT, 1);
    private Command about = new Command ("About", Command.ITEM, 2);


    public PrimeSpiralApp () {
        super ();

        myDisplay = Display.getDisplay (this);
        myCanvas = new PrimeSpiralCanvas (this);
        myCanvas.setCommandListener (this);
        myCanvas.addCommand(exit);
        myCanvas.addCommand(about);

    }

    public void init () throws MIDletStateChangeException {
    }

    public void startApp () throws MIDletStateChangeException {
        myDisplay.setCurrent (myCanvas);
    }

    public void pauseApp () {
    }

    /**
     * destroyApp()
     * <p/>
     * This is important.  It closes the app's RecordStore
     *
     * @param cond true if this is an unconditional destroy
     *             false if it is not
     *             currently ignored and treated as true
     */
    public void destroyApp (boolean cond) {
        myDisplay.setCurrent ((Displayable) null);
        myCanvas.destroy ();
        notifyDestroyed ();
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
