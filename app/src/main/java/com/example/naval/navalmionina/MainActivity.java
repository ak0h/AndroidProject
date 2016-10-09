package com.example.naval.navalmionina;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements LocationListener{

    private SeekBar curseur;
    private LocationManager locationManager ;
    private Location lastKnown;
    private String FIC_NAME = "stock.txt";
    private Button mRead = null;
    private Button mDel = null;
    private TextView widget = null;

    private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Detection de secouement
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
			public void onShake(int count) {
                Toast.makeText(getBaseContext(), "Secouement détécté ...", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, TheService.class));
			}
		});


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "permission denied", Toast.LENGTH_LONG).show();
        } else {
            // Recupération des données tout les 10s
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
        }

        // Affichage derniere position connue
        widget = (TextView) findViewById(R.id.widget);
        widget.setText("Dernière position connue : Latitude "+lastKnown.getLatitude()+" Longitude "+lastKnown.getLongitude());




        // Lecture du fichier
        mRead = (Button) findViewById(R.id.read);
        mRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                Intent showAct = new Intent(MainActivity.this, ListShow.class);
                showAct.putExtra("ficName", FIC_NAME);
                startActivity(showAct);
            }
        });

        // Suppression du fichier
        mDel = (Button) findViewById(R.id.delete);
        mDel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View pView) {
                File fic = new File(getBaseContext().getFilesDir(), FIC_NAME);
                if (fic.exists()) {
                    getBaseContext().deleteFile(FIC_NAME);
                    Toast.makeText(getBaseContext(), "Fichier supprimé", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Aucun fichier à supprimer", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // implémentation du curseur
        curseur = (SeekBar) findViewById(R.id.seekBar);
        curseur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int choosenVal = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                choosenVal = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Frequence de mise à jour qui influence indirectement sur l'enregistrement dans le fichier
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Manifest permission failed ... please check-it", Toast.LENGTH_SHORT).show();
                }else {
                    locationManager.removeUpdates(MainActivity.this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,choosenVal, 10, MainActivity.this);
                    int aff = choosenVal/1000;
                    if(aff<60)
                        Toast.makeText(MainActivity.this, "Mise à jours tout les " + aff + " secondes", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "Mise à jours tout les " + aff/60 + " minutes", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }


    // Start the service
    public void startService(View view) {
        startService(new Intent(this, TheService.class));
    }

    // Stop the service
    public void stopService(View view) {
        stopService(new Intent(this, TheService.class));
    }


    @Override
    public void onLocationChanged(Location location) {

        // Affichage derniere position connue
        widget = (TextView) findViewById(R.id.widget);
        String msg = "Latitude: "+lastKnown.getLatitude()+" Longitude: "+lastKnown.getLongitude();
        widget.setText("Dernière position connue : "+msg);

        // Stockage donnée dans un fichier
        // on cherche si le fichier existe
        File fichier = new File(FIC_NAME);

        if(!fichier.exists())
            fichier = new File(getBaseContext().getFilesDir(), FIC_NAME);


        FileOutputStream outPut;

        try{
            outPut = openFileOutput(fichier.getName(), Context.MODE_APPEND);
            // Si le fichier dépasse les 3Mb on arrete de stocker
            if(fichier.length()<=(1024*3)) {
                outPut.write(msg.getBytes());
                outPut.write("\n".getBytes());
                Toast.makeText(getBaseContext(), "nouvelle coordonée stockée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Le fichier est trop volumineux, il doit être supprimé", Toast.LENGTH_LONG).show();
            }
            if(outPut != null)
                outPut.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Fonctions implémentées par l'interface LocationListener
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();

    }

    @Override
	public void onResume() {
		super.onResume();
		// Add the following line to register the Session Manager Listener onResume
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		// Add the following line to unregister the Sensor Manager onPause
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}

}