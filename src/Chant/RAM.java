package Chant;

import java.util.ArrayList;

record Pair<T1, T2>(T1 key, T2 value) {}

public class RAM{
    private byte[][] data;
    private int[] starts;
    private int[] lengths;
    private int[] ROMregions;
    private ArrayList<Pair> LockedRegions;
    public RAM(int[] starts, int[] lengths, byte[][] ROMS, int[] ROMregions){
        LockedRegions = new ArrayList<>();
        if (starts.length != lengths.length) throw new IllegalArgumentException("Mismatched base addresses and lengths length");
	    this.data = new byte[starts.length][];
        for (int i=0; i< lengths.length; i++){
            this.data[i] = new byte[lengths[i]];
        }
	    this.starts = starts;
	    this.lengths = lengths;
        if (ROMS.length != ROMregions.length) throw new IllegalArgumentException("Mismatched ROMS and associated regions");
        for (int i=0; i<ROMS.length; i++){
            if (ROMS[i].length > data[ROMregions[i]].length) throw new IllegalArgumentException("ROM contains more data than its region can hold");
            for(int a=0; a<ROMS[i].length; a++){
                data[ROMregions[i]][a] = ROMS[i][a];
            }
        }
        this.ROMregions=ROMregions;
    }
    public void lockRegion(int start, int len){
        LockedRegions.add(new Pair<Integer,Integer>(start, len));
    }
    public void lockRegion(Pair<Integer, Integer> pair){
        LockedRegions.add(pair);
    }
    public boolean isLocked(int addr){
        for (Pair<Integer, Integer> p : LockedRegions){
            if (addr>=p.key() && addr < (p.key()+p.value())) return true;
        }
        return false;
    }
    public byte[] getRegion(int address){
        for(int i=0; i<this.starts.length; i++){
            if (this.starts[i] <= address && this.starts[i]+this.lengths[i] > address) return data[i];
        }
        return null;
    }
    public int get(int address){
        return (getByte(address) << 24) + (getByte(address+1) << 16) + (getByte(address+2) << 8) + getByte(address+3);
    }
    public long getD(int address){
        return get(address) + (get(address+4) << 32);
    }
    public byte getByte(int address){
        for(int i=0; i<this.starts.length; i++){
            if (this.starts[i] <= address && this.starts[i]+this.lengths[i] > address) return data[i][address-this.starts[i]];
        }
        return 0;
    }
    public void write(int address, int value){
        writeByte(address, (byte)Math.floorDiv(value,1<<24));
        writeByte(address+1, (byte)(Math.floorDiv(value,1<<16)%256));
        writeByte(address+2, (byte)(Math.floorDiv(value,1<<8)%256));
        writeByte(address+3, (byte)(value%256));
    }
    public void writeD(int address, long value){
        write(address, (int)(value%(1<<32)));
        write(address+4, (int)(Math.floorDiv(value, 1<<32)));
    }
    public boolean writeByte(int address, byte value){
        for(int i=0; i<this.starts.length; i++){
            if (this.starts[i] <= address && this.starts[i]+this.lengths[i] > address){
                for (int j: this.ROMregions){
                    if (j==i) return false;
                }
                if (isLocked(address)) return false;
                data[i][address-this.starts[i]] = value;
                return true;
            }
        }
        return false;
    }
}