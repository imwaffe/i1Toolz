package EyeOne;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface ColorMunkiHelperDLL extends Library {
    int IsConnected();
    int ButtonPressed();
    int Calibrate();
    int TriggerMeasurement();
    String GetOption(String option);
    int SetOption(String option, String value);
    int GetSpectrum(Pointer spectrum, long val);
    int GetSensorPosition();
    int RegisterDeviceMessageHandler(Callback func);
    int RegisterErrorHandler(Callback func);
    int RemoveDeviceMessageHandler(Callback func);
    int RemoveErrorHandler(Callback func);
    void dispose();
    void Close();
}
