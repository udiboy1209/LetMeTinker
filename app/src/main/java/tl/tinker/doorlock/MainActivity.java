package tl.tinker.doorlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;

    public static String TL_MAC = "9c:d6:43:d7:69:bc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mainText = (TextView) findViewById(R.id.mainText);

        // Check for wifi is disabled
        if (mainWifi.isWifiEnabled() == false)
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }

        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();

        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi connections changed
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("Starting Scan...");
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            boolean inTL = false;

            if(mainWifi.getConnectionInfo().getBSSID().equalsIgnoreCase(TL_MAC)){
                inTL = true;
            } else {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult wifi = wifiList.get(i);
                    if (wifi.BSSID.equalsIgnoreCase(TL_MAC)) {
                        inTL = true;
                        Log.d("Wifi", wifi.SSID + ", " + wifi.level);

                        WifiConfiguration conf = new WifiConfiguration();
                        conf.SSID = "\"" + wifi.SSID + "\"";
                        conf.preSharedKey = "\"tinker@tl\"";
                        mainWifi.addNetwork(conf);

                        List<WifiConfiguration> list = mainWifi.getConfiguredNetworks();
                        for (WifiConfiguration w : list) {
                            if (w.SSID != null && w.SSID.equals("\"" + wifi.SSID + "\"")) {
                                mainWifi.disconnect();
                                mainWifi.enableNetwork(w.networkId, true);
                                mainWifi.reconnect();
                                break;
                            }
                        }
                    }
                }
            }

            if(inTL){
                mainText.setText("We're in TL!");
            } else {
                mainText.setText("We're not in TL :(");
            }
        }

    }
}
