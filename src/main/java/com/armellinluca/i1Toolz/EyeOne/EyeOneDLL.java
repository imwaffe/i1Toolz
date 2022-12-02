package com.armellinluca.i1Toolz.EyeOne;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;


public interface EyeOneDLL extends Library {
    int I1_IsConnected();
    int I1_KeyPressed();
    int I1_Calibrate();
    int I1_TriggerMeasurement();
    String I1_GetOption(String option);
    int I1_SetOption(String option, String value);
    int I1_GetSpectrum(Pointer spectrum, long val);
    int I1_RegisterDeviceMessageHandler(Callback func);
    int I1_RegisterErrorHandler(Callback func);
}