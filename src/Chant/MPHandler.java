package Chant;

public class MPHandler extends Peripheral{
    public MPHandler(int MMIOaddr, byte[] region) {
            super(MMIOaddr, region);
    }
    public MicroPeripheral[] MicroPeripherals = new MicroPeripheral[255];
    int mp_count=0;
    public void addPeripheral(MicroPeripheral mp){
        if (mp_count>255) return;
        this.MicroPeripherals[mp_count] = mp;
        mp_count++;
    }
    @Override
    public void refresh(){
        if (this.region[this.MMIOaddr]==0) return;
        if (MicroPeripherals[this.region[this.MMIOaddr]-1]==null) return;
        if (CVM.verbose) System.out.printf("Peripheral %d sent command %d\n", this.region[this.MMIOaddr]-1, this.region[this.MMIOaddr+1]);
        byte[] tmp = MicroPeripherals[this.region[this.MMIOaddr]-1].refresh(this.region[this.MMIOaddr+1], this.region[this.MMIOaddr+2], this.region[this.MMIOaddr+3],this.region[this.MMIOaddr+4], this.region[this.MMIOaddr+5]);
        for (int i=0; i<6; i++){
            this.region[this.MMIOaddr+i]=tmp[i];
        }
    }   
    @Override
    public int getIOports() {
        return 6;
    }         
};