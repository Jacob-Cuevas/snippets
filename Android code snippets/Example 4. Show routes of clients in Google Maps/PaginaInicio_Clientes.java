package com.example.metodos1.puntodeventadetalle.Vistas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.metodos1.puntodeventadetalle.Adaptadores.ClienteListViewAdapter;
import com.example.metodos1.puntodeventadetalle.Adaptadores.ManejadorDireccionGoogleMaps;
import com.example.metodos1.puntodeventadetalle.Configuracion.Constantes;
import com.example.metodos1.puntodeventadetalle.Configuracion.FuncionesGPS;
import com.example.metodos1.puntodeventadetalle.Daos.ClienteDao;
import com.example.metodos1.puntodeventadetalle.Dominio.Cliente;
import com.example.metodos1.puntodeventadetalle.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCuevas on 03/02/2016.
 */
public class PaginaInicio_Clientes extends Fragment {

    private LinearLayout linearLayoutBtnClienteGenerico;
    private ClienteListViewAdapter clienteListViewAdapter;
    private List<Cliente> listadoClientesInicial;
    private EditText editTextClaveNombre;
    private Spinner spinnerOpcionesClientes;
    private List<String> nombreOpciones;
    private ViewFlipper viewFlipperBuscadorClientes;
    private Button buttonBuscarTiendasGps;
    private ImageButton imageButtonEscanearCodigoBarraCliente;
    private CheckBox checkBoxMostrarRutaDiaPaginaInicioClientes;
    private TextView textViewNumClientesBuscadosGps;
    //Manejadores de gps
    private LocationManager locationManager;
    private LocationListener locationListener;
    //Google maps
    private GoogleMap mMap;
    //Daos
    private ClienteDao clienteDao;
    //Preferencias
    private SharedPreferences preferences;
    //Direccion de Google Maps
    private ManejadorDireccionGoogleMaps manejadorDireccionGoogleMaps;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_paginainicio_seleccionarclientes, container, false);

        linearLayoutBtnClienteGenerico = (LinearLayout) view.findViewById(R.id.linearLayoutBtnClienteGenerico);

        //Daos
        clienteDao = new ClienteDao();

        //Preferencias
        preferences = getActivity().getSharedPreferences(Constantes.MY_PREFERENCIAS_NOMBRE, Constantes.CONTEXT_PRIVATE);

        //Por defecto se marca la opción de mostrar los clientes del día
        checkBoxMostrarRutaDiaPaginaInicioClientes = (CheckBox) view.findViewById(R.id.checkBoxMostrarRutaDiaPaginaInicioClientes);
        checkBoxMostrarRutaDiaPaginaInicioClientes.setChecked(true);

        //Listado de clientes
        ListView listViewRecorridos = (ListView) view.findViewById(R.id.listViewClientes);

        int[] colors = {0, 0xFFFF0000, 0}; // red
        listViewRecorridos.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        listViewRecorridos.setDividerHeight(1);

        clienteListViewAdapter = new ClienteListViewAdapter(getActivity(), getFragmentManager(), getContext(), checkBoxMostrarRutaDiaPaginaInicioClientes);
        clienteListViewAdapter.notifyDataSetChanged();

        listViewRecorridos.setAdapter(clienteListViewAdapter);

        linearLayoutBtnClienteGenerico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Se busca el cliente genérico
                Cliente clienteGenerico = clienteDao.getClientePorIdCliente("0");
                if (clienteGenerico != null) {

                    Fragment fragment = new PaginaInicio_CapturaPedidos();
                    Bundle args = new Bundle();
                    args.putString("codigo_aux", clienteGenerico.getCodigoAux());
                    args.putString("nombre", clienteGenerico.getNombre());
                    args.putString("direccion", clienteGenerico.getDireccion());
                    args.putInt("id_cliente", clienteGenerico.getIdCliente());
                    fragment.setArguments(args);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.your_placeholder, fragment);
                    ft.addToBackStack("fragment_captura_pedido");

                    ft.commit();

                }

            }
        });

        listadoClientesInicial = new ArrayList<Cliente>();
        listadoClientesInicial.addAll(clienteListViewAdapter.getListadoListView());

        editTextClaveNombre = (EditText) view.findViewById(R.id.editTextClaveNombre);
        editTextClaveNombre.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<Cliente> listadoBusquedaFinal = new ArrayList<Cliente>();

                if (editTextClaveNombre.getText().toString().equals("")) {

                    clienteListViewAdapter.setListadoListView(listadoClientesInicial);

                } else {

                    for (Cliente cliente : listadoClientesInicial) {

                        if (cliente.getCodigoAux().equals(s.toString()) || cliente.getNombre().contains(s.toString())) {

                            listadoBusquedaFinal.add(cliente);

                        }

                    }

                    clienteListViewAdapter.setListadoListView(listadoBusquedaFinal);

                }

                clienteListViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        //Se inicializa boton y trigger para el escaneo de codigo de barras
        imageButtonEscanearCodigoBarraCliente = (ImageButton) view.findViewById(R.id.imageButtonEscanearCodigoBarraCliente);
        imageButtonEscanearCodigoBarraCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Se valida si se tiene el permiso para usar la camara
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setOrientationLocked(true);
                    integrator.initiateScan();

                } else {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, Constantes.MY_PERMISSIONS_REQUEST_CAMERA);

                }

            }
        });

        //Se asigna a las constantes para ser visualizada desde cualquier clase en el proyecto
        Constantes.clienteListViewAdapter = clienteListViewAdapter;

        viewFlipperBuscadorClientes = (ViewFlipper) view.findViewById(R.id.viewFlipperBuscadorClientes);

        spinnerOpcionesClientes = (Spinner) view.findViewById(R.id.spinnerBusquedaClientes);

        nombreOpciones = new ArrayList<String>();
        nombreOpciones.add("Catálogo clientes");
        nombreOpciones.add("Buscar en mapa");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, nombreOpciones);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerAdapter.notifyDataSetChanged();
        spinnerOpcionesClientes.setAdapter(spinnerAdapter);

        spinnerOpcionesClientes.setSelection(0);

        spinnerOpcionesClientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {//Catalogo clientes
                    viewFlipperBuscadorClientes.setDisplayedChild(0);
                } else {//Buscar por GPS
                    viewFlipperBuscadorClientes.setDisplayedChild(1);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Google maps
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }

        if (mapFragment != null) {

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {

                    mMap = googleMap;

                    LatLng centroMerida = new LatLng(20.975446, -89.626325);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centroMerida.latitude, centroMerida.longitude), 11.0f));

                }
            });

        }

        //Se inicializa el listener del GPS
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                //Se elimina el escuchador de los eventos del GPS antes de usarlos nuevamente
                try {
                    //noinspection ResourceType
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Se realiza la peticion al GPS
                leerPosicion(location);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        //buscar clientes por GPS
        buttonBuscarTiendasGps = (Button) view.findViewById(R.id.buttonBuscarTiendasGps);
        buttonBuscarTiendasGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Primero se obtiene la posicion actual por GPS
                try {
                    //noinspection ResourceType
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                obtenerPosicionGps();

            }
        });

        //Textview de numClientes encontrados por el GPS
        textViewNumClientesBuscadosGps = (TextView)view.findViewById(R.id.textViewNumClientesBuscadosGps);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Se guarda en constantes el fragment actual para usarlo para procesar el ONACTIVITYRESULT en el MAINACTIVITY
        Constantes.paginaInicioClientes = this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Este ONACTIVITYRESULT es llamado desde el MAINACTIVITY
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult == null) {

            Toast toast = Toast.makeText(getContext(), "No se pudo escanear correctamente", Toast.LENGTH_SHORT);
            toast.show();

        } else {

            //SE valida si lo llamo el actualizar codigo de barras de la clase PAGINACENSOCLIENTES_MENUPRINCIPAL_ACTUALIZARCODIGO
            if (Constantes.editTextNuevoCodigoBarrasCliente != null) {

                String scanContenttext = scanningResult.getContents();
                Constantes.editTextNuevoCodigoBarrasCliente.setText(scanContenttext);
                Constantes.editTextNuevoCodigoBarrasCliente = null;

            } else
                //Se valida si lo llamo el agregar cliente de la clase PAGINACENSOCLIENTES_MENUPRINCIPAL_AGREGARCLIENTE
                if (Constantes.editTextClaveAgregarCliente != null) {

                    String scanContenttext = scanningResult.getContents();
                    Constantes.editTextClaveAgregarCliente.setText(scanContenttext);
                    Constantes.editTextClaveAgregarCliente = null;

                }
                //Fue llamado desde esta clase
                else {
                    //Fue llamado por el buscado de clientes
                    String scanContenttext = scanningResult.getContents();
                    editTextClaveNombre.setText(scanContenttext);
                }

        }

        //super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Metodo para realizar la petición de la posicion del GPS
     */
    private void obtenerPosicionGps() {

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {

            FuncionesGPS.showSettingsAlert(getContext(), getActivity());

        } else {
            //Se inicia la captura
            //noinspection ResourceType
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Constantes.MIN_TIME_BW_UPDATES,
                    Constantes.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);
        }

    }

    /**
     * Metodo para recuperar la laltitud y longitud una vez que contesta el GPS
     * @param location
     */
    private void leerPosicion(Location location) {

        if (location != null) {

            //Se buscan los clientes a partir de la posicion actual;
            final Location locacionOriginal = new Location("");
            locacionOriginal.setLatitude(FuncionesGPS.truncateDecimal(location.getLatitude(), 6).doubleValue());
            locacionOriginal.setLongitude(FuncionesGPS.truncateDecimal(location.getLongitude(), 6).doubleValue());

            List<Cliente> clientesbuscados = new ArrayList<Cliente>();

            for (Cliente cliente : listadoClientesInicial) {

                Location locacionCliente = new Location("");
                locacionCliente.setLatitude(Double.parseDouble(cliente.getLatitud()));
                locacionCliente.setLongitude(Double.parseDouble(cliente.getLongitud()));

                float distanceInMeters = locacionOriginal.distanceTo(locacionCliente);

                //Se recupera la distancia en MTS para la búsqueda de clientes
                int mtsLocalizacionClientes = preferences.getInt("distancia_gps", Constantes.MTS_LOCALIZACION_CLIENTES);

                //Se valida que el cliente este en la distancia establecida o sea un cliente del dia
                if (distanceInMeters <= mtsLocalizacionClientes
                        || cliente.getIsRutaActual() == Constantes.CLIENTE_RUTA_ACTUAL) {
                    clientesbuscados.add(cliente);
                }

            }

            //Se limpia el mapa
            mMap.clear();

            LatLngBounds.Builder bld = new LatLngBounds.Builder();

            int countClientes = 0;
            int posClienteRutaDia = 0;

            //Marcador seleccionado al clic
            final Marker[] markerSeleccionado = {null};

            //Se coloca el marcador de la posición actual del vendedor con un icon
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);

            //Latitud y longitud original
            LatLng latLongActual = new LatLng(locacionOriginal.getLatitude(), locacionOriginal.getLongitude());
            List<Polyline> lineasDibujadas = new ArrayList<>();

            //Se muestran los clientes en el mapa
            for (Cliente cliente : clientesbuscados) {

                LatLng ll = new LatLng(Double.parseDouble(cliente.getLatitud()), Double.parseDouble(cliente.getLongitud()));
                bld.include(ll);

                //Contador para controlar el ZOOM del mapa
                countClientes++;

                //Se crea un bitmap del orden de los cliente del dia
                Bitmap bmp = null;
                if(cliente.getIsRutaActual() == Constantes.CLIENTE_RUTA_ACTUAL){
                    //Se aumenta el contador del num de clientes de la ruta actual
                    posClienteRutaDia++;
                    //Se hace el bitmap para el marcado con el num de cliente
                    bmp = crearBitmapMarcadorClienteDia(String.valueOf(posClienteRutaDia));
                    //Se dibuja la mejor ruta de todos los clientes llendo de un cliente a otro
                }

                //Marcador del cliente
                final Marker marcadoActual = mMap.addMarker(new MarkerOptions()
                        .icon( cliente.getIsRutaActual() == Constantes.CLIENTE_RUTA_ACTUAL
                                ? BitmapDescriptorFactory.fromBitmap(bmp) /*BitmapDescriptorFactory.fromResource(R.drawable.marcador_cliente_actual_icon)*/
                                : BitmapDescriptorFactory.fromResource(R.drawable.pin_marcador_mapa)
                        )
                        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .title(cliente.getNombre())
                        .position(new LatLng(Double.parseDouble(cliente.getLatitud()), Double.parseDouble(cliente.getLongitud())))
                );

                //Animacion del marker inicial
                if(posClienteRutaDia == 1){
                    setMarkerBounce(marcadoActual);
                }

                //Evento del clic en el marcador del cliente
                //Se crea el listado de lineas de las rutas
                lineasDibujadas = new ArrayList<>();

                final List<Polyline> finalLineasDibujadas = lineasDibujadas;
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {

                        //Marker seleccionado
                        markerSeleccionado[0] = marker;

                        for(final Cliente clientetemp : listadoClientesInicial){
                            if(clientetemp.getNombre().equals(marker.getTitle())){

                                LayoutInflater factory = LayoutInflater.from(getContext());
                                View deleteDialogView = factory.inflate( R.layout.marker_custom_googlemaps_cliente, null);

                                final AlertDialog deleteDialog = new AlertDialog.Builder(getContext()).create();
                                deleteDialog.setView(deleteDialogView);

                                TextView textViewMarkerNombreCliente = (TextView)deleteDialogView.findViewById(R.id.textViewMarkerNombreCliente);
                                textViewMarkerNombreCliente.setText(clientetemp.getNombre());

                                TextView textViewMarkerDireccionCliente = (TextView)deleteDialogView.findViewById(R.id.textViewMarkerDireccionCliente);
                                textViewMarkerDireccionCliente.setText(clientetemp.getDireccion());

                                Button buttonMarkerCapturarVenta = (Button)deleteDialogView.findViewById(R.id.buttonMarkerCapturarVenta);
                                buttonMarkerCapturarVenta.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        deleteDialog.dismiss();
                                        Fragment fragment = new PaginaInicio_CapturaPedidos();
                                        Bundle args = new Bundle();
                                        args.putString("codigo_aux", clientetemp.getCodigoAux());
                                        args.putString("nombre", clientetemp.getNombre());
                                        args.putString("direccion", clientetemp.getDireccion());
                                        args.putInt("id_cliente", clientetemp.getIdCliente());
                                        fragment.setArguments(args);

                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.replace(R.id.your_placeholder, fragment);
                                        ft.addToBackStack(null);

                                        ft.commit();

                                    }
                                });

                                Button buttonDibujarRutaMarker = (Button)deleteDialogView.findViewById(R.id.buttonDibujarRutaMarker);
                                buttonDibujarRutaMarker.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        manejadorDireccionGoogleMaps = new ManejadorDireccionGoogleMaps(
                                                getContext(),
                                                String.valueOf(locacionOriginal.getLatitude()) ,
                                                String.valueOf(locacionOriginal.getLongitude()),
                                                String.valueOf(marker.getPosition().latitude),
                                                String.valueOf(marker.getPosition().longitude),
                                                mMap,
                                                finalLineasDibujadas
                                        );
                                        manejadorDireccionGoogleMaps.execute((String) null);

                                        deleteDialog.dismiss();

                                    }
                                });

                                Button buttonMarkerComoLlegar = (Button)deleteDialogView.findViewById(R.id.buttonMarkerComoLlegar);
                                buttonMarkerComoLlegar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //Se realiza la llamada a la aplicacion de Google Maps para iniciarl a navegacion.
                                        String url_google_maps = Constantes.URL_INTENT_GOOGLE_MAPS_NAVEGACION.replace("LATITUD_ORIGINAL", String.valueOf(locacionOriginal.getLatitude()));
                                        url_google_maps = url_google_maps.replace("LONGITUD_ORIGINAL", String.valueOf(locacionOriginal.getLongitude()));
                                        url_google_maps = url_google_maps.replace("LATITUD_DESTINO", String.valueOf(markerSeleccionado[0].getPosition().latitude) );
                                        url_google_maps = url_google_maps.replace("LONGITUD_DESTINO", String.valueOf(markerSeleccionado[0].getPosition().longitude));

                                        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url_google_maps) );

                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                                        startActivityForResult(intent, 1);

                                    }
                                });

                                Button buttonMarkerCerrarDialog = (Button)deleteDialogView.findViewById(R.id.buttonMarkerCerrarDialog);
                                buttonMarkerCerrarDialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        deleteDialog.dismiss();

                                    }
                                });

                                deleteDialog.show();

                                break;

                            }
                        }

                        return true;
                    }
                });

                //Se asigna a la posicion actual
                latLongActual = new LatLng(Double.parseDouble(cliente.getLatitud()), Double.parseDouble(cliente.getLongitud()));

            }

            if(countClientes > 0){

                LatLngBounds bounds = bld.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 70));
                textViewNumClientesBuscadosGps.setText("Clientes encontrados: " + String.valueOf(countClientes));

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Sin clientes cercanos")
                        .setMessage("No se encontraron clientes cercanos desde su posición.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }

            //mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

        }

        //Se elimina el escuchador de la peticion del GPS
        //noinspection ResourceType
        locationManager.removeUpdates(locationListener);

    }

    //Metodo para crear el marker personalizado
    private Bitmap crearBitmapMarcadorClienteDia(String numOrdenCliente){

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.marcador_circulo_background)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getContext(), 12));

        Rect textRect = new Rect();
        paint.getTextBounds(numOrdenCliente, 0, numOrdenCliente.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getContext(), 9));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(numOrdenCliente, xPos, yPos, paint);

        return  bm;

    }


    public static int convertToPixels(Context context, int nDP){
        final float conversionScale = context.getResources().getDisplayMetrics().density;
        return (int) ((nDP * conversionScale) + 0.5f) ;
    }

    //Se anima el primer marker para ubicarlo al pintarse el mapa
    private void setMarkerBounce(final Marker marker) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final long duration = 2000;
        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed/duration), 0);
                marker.setAnchor(0.5f, 1.0f +  t);

                if (t > 0.0) {
                    handler.postDelayed(this, 16);
                } else {
                    setMarkerBounce(marker);
                }
            }
        });
    }


}
