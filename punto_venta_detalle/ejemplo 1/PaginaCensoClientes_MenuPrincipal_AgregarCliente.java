package com.example.metodos1.puntodeventadetalle.Vistas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.metodos1.puntodeventadetalle.Configuracion.Constantes;
import com.example.metodos1.puntodeventadetalle.Configuracion.FuncionesGPS;
import com.example.metodos1.puntodeventadetalle.Daos.ClienteCensoDao;
import com.example.metodos1.puntodeventadetalle.Daos.LoginDao;
import com.example.metodos1.puntodeventadetalle.Daos.RutaCensoDao;
import com.example.metodos1.puntodeventadetalle.Dominio.ClienteCenso;
import com.example.metodos1.puntodeventadetalle.Dominio.Login;
import com.example.metodos1.puntodeventadetalle.Dominio.RutaCenso;
import com.example.metodos1.puntodeventadetalle.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by METODOS1 on 16/05/2016.
 */
public class PaginaCensoClientes_MenuPrincipal_AgregarCliente extends Fragment {

    //Selector de ruta
    private Spinner spinnerRutasAgregarCliente;
    //Visualizadores de la posicion
    private TextView textViewLatitudAgregarCliente;
    private TextView textViewLongitudAgregarCliente;
    private TextView textViewPrecisionAgregarCliente;
    private ImageView imageViewButonGpsAgregarCliente;
    //Elementos de la clave del cliente
    private EditText editTextClaveAgregarCliente;
    private ImageView imageViewButtonBorrarClaveAgregarCliente;
    private ImageView imageViewButtonEscaneoAgregarCliente;
    //Elementos del Formulario
    private EditText editTextNombreTiendaAgregarCliente;
    private EditText editTextNombreClienteAgregarCliente;
    private EditText editTextCalleAgregarCliente;
    private EditText editTextNumExtAgregarCliente;
    private EditText editTextNumIntAgregarCliente;
    private EditText editTextCruzamientosAgregarCliente;
    private EditText editTextColoniaAgregarCliente;
    private EditText editTextPoblacionAgregarCliente;
    private EditText editTextCiudadAgregarCliente;
    private EditText editTextObservacionesAgregarCliente;
    private ImageView imageViewButtonGuardarAgregarCliente;
    private ImageView imageViewButtonCancelarAgregarCliente;
    //Manejadores de gps
    private LocationManager locationManager;
    private LocationListener locationListener;
    //Preferencias
    private SharedPreferences preferences;
    //Daos
    private RutaCensoDao rutaCensoDao;
    private ClienteCensoDao clienteCensoDao;
    private LoginDao loginDao;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //inicialización del custom view para el layout.
        View view = inflater.inflate(R.layout.content_paginacensoclientes_menuprincipal_agregarcliente, container, false);

        //inicializacion de Daos
        rutaCensoDao = new RutaCensoDao();
        clienteCensoDao = new ClienteCensoDao();
        loginDao = new LoginDao();
        //obtencion de Preferencias
        preferences = getActivity().getSharedPreferences(Constantes.MY_PREFERENCIAS_NOMBRE, Constantes.CONTEXT_PRIVATE);

        //Spinner de rutas
        spinnerRutasAgregarCliente = (Spinner)view.findViewById(R.id.spinnerRutasAgregarCliente);
        List<RutaCenso> rutasCenso = rutaCensoDao.getTodasRutasCenso();
        /*if(rutasCenso.isEmpty()){
            Toast.makeText(getContext(),"Sincronice las rutas antes de agregar clientes.",Toast.LENGTH_SHORT).show();
        }*/
        inicializarValoresSpinner(rutasCenso);

        //Elementos de la vista
        textViewLatitudAgregarCliente = (TextView)view.findViewById(R.id.textViewLatitudAgregarCliente);
        textViewLongitudAgregarCliente = (TextView)view.findViewById(R.id.textViewLongitudAgregarCliente);
        textViewPrecisionAgregarCliente = (TextView)view.findViewById(R.id.textViewPrecisionAgregarCliente);
        editTextClaveAgregarCliente = (EditText)view.findViewById(R.id.editTextClaveAgregarCliente);
        imageViewButtonBorrarClaveAgregarCliente = (ImageView)view.findViewById(R.id.imageViewButtonBorrarClaveAgregarCliente);
        imageViewButonGpsAgregarCliente = (ImageView)view.findViewById(R.id.imageViewButonGpsAgregarCliente);
        imageViewButtonEscaneoAgregarCliente = (ImageView)view.findViewById(R.id.imageViewButtonEscaneoAgregarCliente);
        editTextNombreTiendaAgregarCliente = (EditText)view.findViewById(R.id.editTextNombreTiendaAgregarCliente);
        editTextNombreClienteAgregarCliente = (EditText)view.findViewById(R.id.editTextNombreClienteAgregarCliente);
        editTextCalleAgregarCliente = (EditText)view.findViewById(R.id.editTextCalleAgregarCliente);
        editTextNumExtAgregarCliente = (EditText)view.findViewById(R.id.editTextNumExtAgregarCliente);
        editTextNumIntAgregarCliente = (EditText)view.findViewById(R.id.editTextNumIntAgregarCliente);
        editTextCruzamientosAgregarCliente = (EditText)view.findViewById(R.id.editTextCruzamientosAgregarCliente);
        editTextColoniaAgregarCliente = (EditText)view.findViewById(R.id.editTextColoniaAgregarCliente);
        editTextPoblacionAgregarCliente = (EditText)view.findViewById(R.id.editTextPoblacionAgregarCliente);
        editTextCiudadAgregarCliente = (EditText)view.findViewById(R.id.editTextCiudadAgregarCliente);
        editTextObservacionesAgregarCliente = (EditText)view.findViewById(R.id.editTextObservacionesAgregarCliente);
        imageViewButtonGuardarAgregarCliente = (ImageView)view.findViewById(R.id.imageViewButtonGuardarAgregarCliente);
        imageViewButtonCancelarAgregarCliente = (ImageView)view.findViewById(R.id.imageViewButtonCancelarAgregarCliente) ;

        //Inicialización del GPS
        inicializarGpsListener();

        //Listeners de widgets
        spinnerRutasAgregarCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Se guardan las preferencias
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constantes.MY_PREFERENCIAS_NOMBRE, Constantes.CONTEXT_PRIVATE).edit();
                editor.putInt("ruta_seleccionada", position );
                editor.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        imageViewButtonBorrarClaveAgregarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editTextClaveAgregarCliente.setText("");

            }
        });

        imageViewButonGpsAgregarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tomarPosicionGps();

            }
        });

        imageViewButtonEscaneoAgregarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Iniciar lectura codigo barras
                Constantes.editTextClaveAgregarCliente = editTextClaveAgregarCliente;

                //Se valida si se tiene el permiso para usar la camara
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setOrientationLocked(true);
                    integrator.initiateScan(); // `this` is the current Activity
                    //LA RESPUESTA DEL ONACTIVITYRESULT ES TRATADA EN LA CLASE PAGINAINICIO_CLIENTES.JAVA

                } else {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, Constantes.MY_PERMISSIONS_REQUEST_CAMERA);

                }

            }
        });

        imageViewButtonGuardarAgregarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Se valida si ya se tiene la posicion del GPS
                if (!textViewLatitudAgregarCliente.getText().toString().equals("")) {
                    //Se valida el codigo de barras del cliente
                    if(editTextClaveAgregarCliente.getText().toString().equals("")){
                        Snackbar.make(v, "Escanee el codigo de barras.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        return;
                    }
                    if ( editTextNombreTiendaAgregarCliente.getText().toString().equals("") ) {
                        Snackbar.make(v, "Ingrese el nombre de la tienda.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        return;
                    }
                    /*if(spinnerRutasAgregarCliente.getSelectedItem() == null){
                        Snackbar.make(v, "Seleccione la ruta.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        return;
                    }*/

                    //RutaCenso rutaSelect = rutaCensoDao.getRutaCensoPorNombre(spinnerRutasAgregarCliente.getSelectedItem().toString()).get(0);

                    //Creacion del objeto cliente en la BD
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();

                    DateFormat dateFormatTime = new SimpleDateFormat("hh:mm:ss");

                    clienteCensoDao.insertar(editTextNombreTiendaAgregarCliente.getText().toString(),
                            editTextClaveAgregarCliente.getText().toString(),
                            textViewLatitudAgregarCliente.getText().toString(),
                            textViewLongitudAgregarCliente.getText().toString(),
                            editTextNombreClienteAgregarCliente.getText().toString(),
                            editTextCalleAgregarCliente.getText().toString(),
                            editTextNumExtAgregarCliente.getText().toString(),
                            editTextNumIntAgregarCliente.getText().toString(),
                            editTextCruzamientosAgregarCliente.getText().toString(),
                            editTextColoniaAgregarCliente.getText().toString(),
                            editTextPoblacionAgregarCliente.getText().toString(),
                            editTextCiudadAgregarCliente.getText().toString(),
                            editTextObservacionesAgregarCliente.getText().toString(),
                            "",//rutaSelect.getCodigo(),
                            dateFormatTime.format(date),
                            dateFormat.format(date)
                    );

                    //Se verifica la inserción
                    List<ClienteCenso> clienteCensoList = clienteCensoDao.buscarPorClaveClienteCenso(editTextClaveAgregarCliente.getText().toString());
                    if(clienteCensoList != null && !clienteCensoList.isEmpty()){

                        Snackbar.make(v, "Cliente " + editTextClaveAgregarCliente.getText().toString() + " guardado con exito.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        limpiarCamposInsercion();

                    } else {

                        Snackbar.make(v, "No se ha podido guardar los datos.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    }

                } else {

                    Snackbar.make(v, "Presione el boton de capturar posición antes de guardar.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                }

            }
        });

        imageViewButtonCancelarAgregarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                limpiarCamposInsercion();

            }
        });

        return view;
    }

    //Reinicia las variables y elementos de la vista
    private void limpiarCamposInsercion() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try{
            locationManager.removeUpdates(locationListener);
        }catch(Exception e){
            e.printStackTrace();
        }

        //Se eliminan valores previos
        textViewLatitudAgregarCliente.setText("");
        textViewLongitudAgregarCliente.setText("");
        textViewPrecisionAgregarCliente.setText("");
        editTextClaveAgregarCliente.setText("");
        //Se limpia lo capturado
        editTextNombreTiendaAgregarCliente.setText("");
        editTextNombreClienteAgregarCliente.setText("");
        editTextCalleAgregarCliente.setText("");
        editTextNumExtAgregarCliente.setText("");
        editTextNumIntAgregarCliente.setText("");
        editTextCruzamientosAgregarCliente.setText("");
        editTextColoniaAgregarCliente.setText("");
        editTextPoblacionAgregarCliente.setText("");
        editTextCiudadAgregarCliente.setText("");
        editTextObservacionesAgregarCliente.setText("");

    }


    /*
    * Init valores del spinner con los nombres de las rutas del vendedor
    */
    private void inicializarValoresSpinner(List<RutaCenso> rutasCenso){

        List<String> nombreRutasCenso = new ArrayList<String>();

        for(RutaCenso rutaCenso : rutasCenso){
            nombreRutasCenso.add(rutaCenso.toString());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, nombreRutasCenso);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerAdapter.notifyDataSetChanged();

        if(!nombreRutasCenso.isEmpty()){

            spinnerRutasAgregarCliente.setAdapter(spinnerAdapter);
            //Se recupera del usuario logeado
            String nombreAlmacen = loginDao.buscarLoginPorIdVendedor(preferences.getString("idVendedor","")).getNombreAlmacen();
            int numVendedorSelect = 0;

            for(int i = 0; i < nombreRutasCenso.size(); i ++){

                if(nombreRutasCenso.get(i).equals(nombreAlmacen)){

                    numVendedorSelect = i;
                    break;

                }

            }

            //Se verifica las preferencias
            //spinnerRutasAgregarCliente.setSelection(preferences.getInt("ruta_seleccionada",0));
            spinnerRutasAgregarCliente.setSelection(preferences.getInt("ruta_seleccionada", numVendedorSelect ));
        }

    }

    private void inicializarGpsListener(){

        //Se inicializa el listener del Gps
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                leerPosicion(location);
            }

            //Validación del estatus del GPS para avisar al usuario de continuar el proceso
            public void onStatusChanged(String provider, int status, Bundle extras) {

                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        //Snackbar.make(getCurrentFocus(), "Sin servicio GPS", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        //Snackbar.make(getCurrentFocus(), "Servicio temporalmente no disponible de GPS", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        break;
                    case LocationProvider.AVAILABLE:
                        Snackbar.make(getView(), "Señal GPS disponible", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        break;
                }

            }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) { }
        };


    }

    //Realiza la peticion de la posicion al GPS
    private void tomarPosicionGps() {

        try {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //Validacion de la disponibilidad del GPS para mostrar el dialogo de activación en caso de que este apagado.
            if (!isGPSEnabled) {

                FuncionesGPS.showSettingsAlert(getContext(), getActivity());

            } else {
                //Se inicia la captura

                //Metodo para validar el GPS en ANDROID 6.0
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    return;
                }

                //IF para validar el PERMISO del GPS en ANDROID 6.0
                if(checkLocationPermission()){

                    //noinspection ResourceType
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            Constantes.MIN_TIME_BW_UPDATES,
                            Constantes.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListener);

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //Metodo para leer la respuesta de la posicion del GPS
    private void leerPosicion(Location location) {
        if (location != null) {
            textViewLatitudAgregarCliente.setText(FuncionesGPS.truncateDecimal(location.getLatitude(), 6) + "");
            textViewLongitudAgregarCliente.setText(FuncionesGPS.truncateDecimal(location.getLongitude(), 6) + "");
            textViewPrecisionAgregarCliente.setText(location.getAccuracy() + " mts");
        } else {
            textViewLatitudAgregarCliente.setText("0.0");
            textViewLongitudAgregarCliente.setText("0.0");
            textViewPrecisionAgregarCliente.setText("0.0 mts");
        }

        try {
            //noinspection ResourceType
            locationManager.removeUpdates(locationListener);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Metodod para validar el permiso del gps en ANDROID 6.0
    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

}
