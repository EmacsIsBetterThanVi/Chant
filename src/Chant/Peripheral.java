package Chant;

/*
A base class on which all peripherals should be built. The CVM will autoload any listed in the header, though there are many which are built in directly into the CVM.
*/
public abstract class Peripheral {
    int MMIOaddr;
    short[] region;
    public Peripheral(int MMIOaddr, short[] region){
        this.MMIOaddr=MMIOaddr;
        this.region=region;
    }
    // Called every frame to refresh the contents of the IO ports. This should be the main part of the peripheral
    public abstract void refresh();
    // Returns the number of bytes this requires for its IO ports
    public abstract int getIOports(); 
    public static Peripheral defaultPeripheral = new Peripheral(0, new short[0]) {
        @Override
        public void refresh() {
            return;
        }

        @Override
        public int getIOports() {
            return 0;
        }        
    };
}