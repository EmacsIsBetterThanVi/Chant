package Chant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class LibraryLoader extends MicroPeripheral{
    public LibraryLoader(int ID) {
        super(ID);
    }
    public int LIBadder = 0xC00000;
    private ArrayList<Pair<String, Pair<Integer, Integer>>> libs;
    private String Libname;
    private FileInputStream openLibrary;
    private int state=0; //0 - null, 1 - await character, 2 - Library Load Success, 3 - Library Load Failure (FILE NOT FOUND), 4 - Library Load Failure (COULD NOT WRITE),
    // 5 - Library Load Failure (OVERRUN), 6 - Library Load Failure (LIBRARY EMPTY), 7 - Library Load Failure (LIBRARY NOT ALIGN4ED), 8 - Library Already Loaded
    private int loadLibrary(int base){
        int libLen=0;
        //TODO write LibLoader
        return libLen;
    }
    private boolean OpenLibrary(String name){
        try {
            openLibrary=new FileInputStream(name);
        } catch (FileNotFoundException e){
            return true;
        }
        return false;
    }
    public int getLibrary(String name){
        for (Pair<String,Pair<Integer,Integer>> pair : libs) {
            if (pair.key().equals(name)){
                return pair.value().key().intValue();
            } 
        }
        return 0;
    }
    public int loadLibrary(String name){
        if (getLibrary(name)==0) return 8;
        if (OpenLibrary(name)) return 3;
        int libLen = loadLibrary(LIBadder);
        if (libLen == -2) return 4;
        if (LIBadder+libLen-1 > 0x1000000) return 5;
        if (libLen == 0) return 6;
        if (libLen == -1) return 7;
        Pair loc = new Pair(LIBadder, libLen);
        libs.add(new Pair(name, loc));
        CVM.memory.lockRegion(loc);
        LIBadder+=libLen;
        return 2;
    }
    public int loadLibrary(String name, int base){
        if (getLibrary(name)==0) return 8;
        if (OpenLibrary(name)) return 3;
        int libLen = loadLibrary(base);
        if (libLen == -2) return 4;
        if (base+libLen-1 > 0x1000000) return 5;
        if (libLen == 0) return 6;
        if (libLen == -1) return 7;
        Pair loc = new Pair(base, libLen);
        libs.add(new Pair(name, loc));
        CVM.memory.lockRegion(loc);
        return 2;
    }
    @Override
    public byte[] refresh(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4) {
        arg4 = (byte)state;
        switch (cmd) {
            case 0:
                if (state == 1 && arg1 !=0){
                    Libname = Libname+String.format("%c", arg1);
                    arg1=0;
                }
            case 1:
                if (state != 1) {
                    state=1;
                    cmd = 0;
                } else {
                    state = 0;
                    cmd = 0;
                }
                break;
            case 2:
                Libname = "";
                break;
            case 3:
                state = loadLibrary(Libname);
                break;
            case 4:
                int addr = getLibrary(Libname);
                arg4 = (byte)addr;
                arg3 = (byte)Integer.rotateRight(addr, 8);
                arg2 = (byte)Integer.rotateRight(addr, 16);
                arg1 = (byte)Integer.rotateRight(addr, 24);
        }
        return unchanged(cmd, arg1, arg2, arg3, arg4);
    }
}