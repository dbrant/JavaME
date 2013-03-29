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
package dmitrybrant.convertme;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;


public class ConvertMe extends MIDlet implements CommandListener, ItemCommandListener, ItemStateListener {
    private static final Command backCommand = new Command ("Back", Command.BACK, 1);
    private static final Command unitsCommand = new Command ("Units...", Command.ITEM, 1);
    private static final Command aboutCommand = new Command ("About", Command.ITEM, 2);
    private static final Command exitCommand = new Command ("Exit", Command.ITEM, 3);

    private String appName = "ConvertMe";
    private boolean firstTime;
    private Form mainForm;

    private TextField fromValue;

    private List categoryList;
    private ChoiceGroup unitGroup;

    private UnitCollection[] collection;


    private byte[] saveBuf;     // buffer for saved data
    private int saveBufIndex = 0;
    private int saveBufLength = 10;
    private RecordStore store;  // record store, null if not open


    public ConvertMe () {
        firstTime = true;
        mainForm = new Form (appName);
        saveBuf = new byte[saveBufLength];

        collection = UnitCollection.GetUnitCollection();

    }

    protected void startApp () {
        if (firstTime) {

            fromValue = new TextField ("Value to convert:", "1.0", 15, TextField.DECIMAL);

            mainForm.append (fromValue);
            mainForm.setItemStateListener(this);

            categoryList = new List("Select Unit Type", Choice.IMPLICIT, new String[0], null);
            for(int i=0; i<collection.length; i++){
                Image img = null;
                try{
                    img = Image.createImage (collection[i].imageName);
                }catch(Exception ex){ }
                categoryList.append(collection[i].name, img);
            }
            categoryList.addCommand(backCommand);
            categoryList.setCommandListener(this);

            int formHeight = mainForm.getHeight();
            formHeight -= fromValue.getMinimumHeight();

            mainForm.append("Convert units:");
            unitGroup = new ChoiceGroup (null, ChoiceGroup.EXCLUSIVE, new String[0], null);
            if(formHeight > 40){
                unitGroup.setPreferredSize(-1, formHeight);
            }
            mainForm.append (unitGroup);

            UpdateCategory();

            mainForm.addCommand (unitsCommand);
            mainForm.addCommand (aboutCommand);
            mainForm.addCommand (exitCommand);
            mainForm.setCommandListener (this);

            try {
                store = RecordStore.openRecordStore ("ConvertMeData", true);

                RecordEnumeration enm = store.enumerateRecords (null, null, false);
                while (enm.hasNextElement ()) {
                    int ndx = enm.nextRecordId ();
                    if (store.getRecordSize (ndx) == saveBufLength) {
                        int l = store.getRecord(ndx, saveBuf, 0);
                        if (l == saveBufLength) {
                            //first eight bytes are the last used value
                            long tempLong = (((long)(saveBuf[0] & 0xff) << 56) | ((long)(saveBuf[1] & 0xff) << 48) |
                                            ((long)(saveBuf[2] & 0xff) << 40) | ((long)(saveBuf[3] & 0xff) << 32) |
                                            ((long)(saveBuf[4] & 0xff) << 24) | ((long)(saveBuf[5] & 0xff) << 16) |
                                            ((long)(saveBuf[6] & 0xff) << 8) | (long)(saveBuf[7] & 0xff) );
                            
                            double lastValue = Double.longBitsToDouble(tempLong);
                            int lastCategory = saveBuf[8];
                            int lastUnit = saveBuf[9];

                            categoryList.setSelectedIndex(lastCategory, true);
                            UpdateCategory();

                            unitGroup.setSelectedIndex(lastUnit, true);
                            if(!Double.isInfinite(lastValue) && !Double.isNaN(lastValue))
                                fromValue.setString(Double.toString(lastValue));
                            UpdateValues();

                            saveBufIndex = ndx;
                            break;
                        }
                    }
                }
                store.closeRecordStore();
            }
            catch (Exception ex) {
                categoryList.set(0, ex.getMessage(), null);
            }

            firstTime = false;
        }

        Display.getDisplay (this).setCurrent (mainForm);
    }

    private void UpdateCategory(){

        int catIndex = categoryList.getSelectedIndex();
        unitGroup.deleteAll();
        
        for(int i=0; i<collection[catIndex].items.length; i++){
            int pos = unitGroup.append(collection[catIndex].items[i].name, null);
            unitGroup.setFont(pos, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        }

        UpdateValues();
    }


    private void UpdateValues(){
        double d = 0.0, p;
        try{
            d = Double.parseDouble(fromValue.getString());
        }catch(Exception ex){ }
        int catIndex = categoryList.getSelectedIndex();
        int unitIndex = unitGroup.getSelectedIndex();

        for(int i=0; i<collection[catIndex].items.length; i++){
            p = (d - collection[catIndex].items[unitIndex].offset) / collection[catIndex].items[unitIndex].multiplier;
            p *= collection[catIndex].items[i].multiplier;
            p += collection[catIndex].items[i].offset;
            p = round2(p, 8);

            unitGroup.set(i, (collection[catIndex].items[i].name + ": " + Double.toString(p)), null);
        }
    }


    public void commandAction (Command c, Displayable s) {
        if (c == unitsCommand) {
            Display.getDisplay(this).setCurrent(categoryList);
        }else if (c == exitCommand) {
            destroyApp (false);
            notifyDestroyed ();
        }else if (c == aboutCommand){
            Alert alert = new Alert ("About...");
            alert.setType (AlertType.INFO);
            alert.setTimeout (Alert.FOREVER);
            alert.setString (appName + " v0.5\nCopyright (c) 2009 Dmitry Brant.\nhttp://dmitrybrant.com");
            Display.getDisplay(this).setCurrent (alert);
        }
        if(s.equals(categoryList)){
            if (c == backCommand){         
            }else{
                //select command
                UpdateCategory();
            }
            Display.getDisplay(this).setCurrent(mainForm);
        }
    }

    public void commandAction (Command c, Item item) {

    }

    public void itemStateChanged(Item item){
        if(item == fromValue){
            UpdateValues();
        }
        else if(item == unitGroup){
            UpdateValues();
        }
    }

    protected void destroyApp (boolean unconditional) {

        try {
            store = RecordStore.openRecordStore ("ConvertMeData", true);
            long tempLong = Double.doubleToLongBits(Double.parseDouble(fromValue.getString()));

            saveBuf[0] = (byte) ((tempLong >> 56) & 0xff);
            saveBuf[1] = (byte) ((tempLong >> 48) & 0xff);
            saveBuf[2] = (byte) ((tempLong >> 40) & 0xff);
            saveBuf[3] = (byte) ((tempLong >> 32) & 0xff);
            saveBuf[4] = (byte) ((tempLong >> 24) & 0xff);
            saveBuf[5] = (byte) ((tempLong >> 16) & 0xff);
            saveBuf[6] = (byte) ((tempLong >> 8) & 0xff);
            saveBuf[7] = (byte) (tempLong & 0xff);

            saveBuf[8] = (byte)categoryList.getSelectedIndex();
            saveBuf[9] = (byte)unitGroup.getSelectedIndex();
            if(saveBufIndex == 0){
                saveBufIndex = store.addRecord(saveBuf, 0, saveBufLength);
            }else{
                store.setRecord(saveBufIndex, saveBuf, 0, saveBufLength);
            }
            store.closeRecordStore();
        }
        catch (Exception ex) {
        }

        Display.getDisplay(this).setCurrent ((Displayable) null);
        notifyDestroyed ();
    }

    protected void pauseApp () {
        //destroyApp(false);
    }


    protected static double round2(double x, int precision) {
        if (x == 0) return x;
        double y = Math.abs(x);
        int sign = x==y?1:-1;
        int intLog = log10(y);
        int shift = precision - intLog - 1;
        String str;
        
        double pow10 = 1.0;
        if(shift != 0) pow10 = Double.parseDouble(("1.0e" + Integer.toString(shift)));
        double powPrec = Double.parseDouble(("1.0e" + Integer.toString(precision)));
        y *= pow10;

        if((powPrec - y) < 0.01)
            intLog++;

        y = Math.floor(y + 0.5);

        str = Integer.toString((int)y);
        if(str.length() > 1)
            str = (str.substring(0, 1) + "." + str.substring(1, str.length()-1)) + "e" + Integer.toString(intLog);

        if(sign == -1) str = "-" + str;
        y = Double.parseDouble(str);
        return y;
    }


    protected static int log10(double x){
        double d = x;
        int t = 0;
        if(d >= 10){
            while(d >= 10){ d /= 10; t++; }
        }else if(d < 1){
            while(d < 1){ d *= 10; t--; }
        }
        return t;
    }


}
