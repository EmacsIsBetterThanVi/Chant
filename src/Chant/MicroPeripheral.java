package Chant;

public abstract class MicroPeripheral{
    public short ID;
    public short[] unchanged(short cmd, short arg1, short arg2, short arg3, short arg4){return new short[]{this.ID, cmd, arg1, arg2, arg3, arg4};}
    public MicroPeripheral(int ID){this.ID=(short)ID;}
    public abstract short[] refresh(short cmd, short arg1, short arg2, short arg3, short arg4);
}