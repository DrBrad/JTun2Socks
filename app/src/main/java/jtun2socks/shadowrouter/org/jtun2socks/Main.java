package jtun2socks.shadowrouter.org.jtun2socks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jtun2socks.shadowrouter.org.jtun2socks.VPN.VPNService;

public class Main extends Activity implements VPNService.onStatusChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VPNService.addOnStatusChangedListener(this);
        checkRunning();

        SharedPreferences sharedPref = getSharedPreferences("JTun2Socks", Context.MODE_PRIVATE);
        final SharedPreferences.Editor ePref = sharedPref.edit();
        final EditText address = findViewById(R.id.address);
        if(!sharedPref.getString("address", "").equals("") && sharedPref.getInt("port", -1) != -1){
            address.setText(sharedPref.getString("address", "")+":"+sharedPref.getInt("port", -1));
        }

        Button connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(VPNService.vpnThread == null){
                    if(address.getText().toString().contains(":")){
                        ePref.putString("address", address.getText().toString().split("\\:")[0]);
                        ePref.putInt("port", Integer.parseInt(address.getText().toString().split("\\:")[1]));
                        ePref.apply();

                        Intent intent = VPNService.prepare(Main.this);

                        if(intent != null){
                            startActivityForResult(intent, 0);
                        }else{
                            onActivityResult(0, RESULT_OK, null);
                        }
                    }
                }else{
                    VPNService.vpnThread.interrupt();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int request, int result, Intent data){
        if(result == RESULT_OK){
            Intent intent = new Intent(this, VPNService.class);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy(){
        VPNService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onStatusChanged(final boolean status){
        final Button connect = findViewById(R.id.connect);
        if(connect != null){
            connect.post(new Runnable(){
                @Override
                public void run(){
                    connect.setText((status) ? "Disconnect" : "Connect");
                }
            });
        }
    }

    public void checkRunning(){
        boolean status = (VPNService.vpnThread != null && !VPNService.vpnThread.isInterrupted());
        Button connect = findViewById(R.id.connect);
        connect.setText((status) ? "Disconnect" : "Connect");
    }
}
