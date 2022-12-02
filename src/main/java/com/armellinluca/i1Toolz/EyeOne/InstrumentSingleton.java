package com.armellinluca.i1Toolz.EyeOne;

public class InstrumentSingleton {
    private static Class<?> instrumentClass = null;
    private static Instrument instrument = null;

    public static Instrument getInstrument(){
        if(instrumentClass == null)
            return null;
        if(instrument == null) {
            try {
                instrument = (Instrument)instrumentClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return instrument;
    }

    public static Instrument setInstrument(Class<?> instClass){
        if(instrument!=null)
            instrument.disconnect();
        instrumentClass = instClass;
        instrument = null;
        return getInstrument();
    }

    public static void disconnect(){
        instrument.disconnect();
    }
}
