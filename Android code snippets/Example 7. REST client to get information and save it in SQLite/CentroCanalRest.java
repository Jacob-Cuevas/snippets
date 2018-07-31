package com.example.metodos1.puntodeventadetalle.Rest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.metodos1.puntodeventadetalle.Configuracion.Constantes;
import com.example.metodos1.puntodeventadetalle.Daos.ActualizacionesLocalesDao;
import com.example.metodos1.puntodeventadetalle.Daos.AlmacenCentroDao;
import com.example.metodos1.puntodeventadetalle.Daos.CentroCanalDao;
import com.example.metodos1.puntodeventadetalle.Daos.PuestoCentroDao;
import com.example.metodos1.puntodeventadetalle.Dominio.VendedorPreventa;
import com.example.metodos1.puntodeventadetalle.R;
import com.google.firebase.crash.FirebaseCrash;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by JCuevas on 16/11/2016.
 */
public class CentroCanalRest {

    private String URL_CENTROCANAL_PREVENTA = Constantes.URL_SERVIDOR;
    private Context context;
    private String idDispositivo;
    private String token;
    private FragmentManager fragmentManager;
    private CentroCanalDao centroCanalDao;
    private AlmacenCentroDao almacenCentroDao;
    private PuestoCentroDao puestoCentroDao;
    private ImageView imageViewStatus;
    private ActualizacionesLocalesDao actualizacionesLocalesDao;
    private TextView textViewStatusError;
    private TextView textViewUltimaAct;

    public CentroCanalRest(FragmentManager fragmentManager, Context context, String idDispositivo, String token, ImageView imageViewStatus, TextView textViewUltimaAct, TextView textViewStatusError){
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.idDispositivo = idDispositivo;
        this.token = token;
        this.imageViewStatus = imageViewStatus;
        this.textViewUltimaAct = textViewUltimaAct;
        this.textViewStatusError = textViewStatusError;
    }

    public RequestHandle obtenerOpcionesPreventa(){

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Cargando almacenes, centros y puestos");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        centroCanalDao = new CentroCanalDao();
        almacenCentroDao = new AlmacenCentroDao();
        puestoCentroDao = new PuestoCentroDao();
        actualizacionesLocalesDao = new ActualizacionesLocalesDao();

        final JSONObject jsonParams = new JSONObject();
        try{

            jsonParams.put("controlador", "App");
            jsonParams.put("metodo", "getOpcionesPreventa");
            jsonParams.put("dispositivo", idDispositivo);
            jsonParams.put("token", token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringEntity entityLogin = null;
        try {
            entityLogin = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AsyncHttpClient httpOpcionesPreventa = new AsyncHttpClient();
        httpOpcionesPreventa.setTimeout(Constantes.TIMEOUT_SINCRONIZACION_RESTS);

        RequestHandle requestHandle = httpOpcionesPreventa.post(context, URL_CENTROCANAL_PREVENTA, entityLogin, "application/json", new TextHttpResponseHandler() {

            private void mostrarMensajeError(String titulo, String mensaje){

                dialog.dismiss();

                fragmentManager.popBackStackImmediate();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(titulo)
                        .setMessage(mensaje)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                mostrarMensajeError("Error de conexión", "No se pudo conectar con el servidor.");
                FirebaseCrash.report(new Exception( "No se pudo conectar con el servidor." + ":" + jsonParams.toString()));

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                try{

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();

                    centroCanalDao.deleteAllCentroCanal();
                    almacenCentroDao.deleteAllAlmacenCentro();
                    puestoCentroDao.deleteAllPuestoCentro();

                    JSONObject objectOpciones = new JSONObject(responseString);

                    if(objectOpciones.getBoolean("success")){

                        JSONArray jsonArrayCentrosCanal = objectOpciones.getJSONArray("centrosCanal");
                        JSONArray jsonArrayAlmacenesCentro = objectOpciones.getJSONArray("almacenesCentro");
                        JSONArray jsonArrayPuestosCentro = objectOpciones.getJSONArray("puestosCentro");

                        for(int i = 0; i < jsonArrayCentrosCanal.length(); i++){

                            JSONObject jsonCentroCanal = jsonArrayCentrosCanal.getJSONObject(i);
                            JSONObject jsonCanalDistribucion = jsonCentroCanal.getJSONObject("canalDistribucion");
                            JSONArray jsonArrayCentros = jsonCentroCanal.getJSONArray("centros");

                            for(int a = 0; a < jsonArrayCentros.length(); a++ ){

                                JSONObject jsonCentro = jsonArrayCentros.getJSONObject(a);
                                centroCanalDao.insertarCentroCanalDao(
                                        jsonCentro.getString("id"),
                                        jsonCentro.getString("descripcion"),
                                        jsonCanalDistribucion.getString("id")
                                );

                            }

                        }

                        for(int i = 0; i < jsonArrayAlmacenesCentro.length(); i++ ){

                            JSONObject jsonAlmacenCentro = jsonArrayAlmacenesCentro.getJSONObject(i);
                            JSONObject jsonCentro = jsonAlmacenCentro.getJSONObject("centro");
                            JSONArray jsonArrayAlmacenes = jsonAlmacenCentro.getJSONArray("almacenes");

                            for(int x = 0; x < jsonArrayAlmacenes.length(); x++ ){

                                JSONObject jsonAlmacen = jsonArrayAlmacenes.getJSONObject(x);
                                almacenCentroDao.insertarAlmacenCentroDao(
                                        jsonAlmacen.getString("id"),
                                        jsonAlmacen.getString("descripcion"),
                                        jsonCentro.getString("id")
                                );

                            }

                        }

                        for(int i = 0; i < jsonArrayPuestosCentro.length(); i++ ){

                            JSONObject jsonPuestoCentro = jsonArrayPuestosCentro.getJSONObject(i);
                            JSONObject jsonCentro = jsonPuestoCentro.getJSONObject("centro");
                            JSONArray jsonArrayPuestosExpedicion = jsonPuestoCentro.getJSONArray("puestosExpedicion");

                            for(int y = 0; y < jsonArrayPuestosExpedicion.length(); y++ ){

                                JSONObject jsonPuestoExpedicion = jsonArrayPuestosExpedicion.getJSONObject(y);
                                puestoCentroDao.insertarPuestoCentro(
                                        jsonPuestoExpedicion.getString("id"),
                                        jsonPuestoExpedicion.getString("descripcion"),
                                        jsonCentro.getString("id")

                                );

                            }

                        }

                        dialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Información")
                                .setMessage("Los centros, almacenes y puestos SAP han sido actualizados.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                        actualizacionesLocalesDao.actualizarFechasLocales(dateFormat.format(date),Constantes.ACT_TIPO_FECHA_CENTROS_SAP);
                        textViewUltimaAct.setText(actualizacionesLocalesDao.getActualizacionesLocales(Constantes.CLAVE_ACTUALIZACIONES).getFechaCentros());

                        mostrarMensajesIconosResultado("Centros, almacenes y puestos SAP actualizados", R.drawable.status_ok_actualizacion);

                    } else {

                        mostrarMensajeError("Error del servidor", objectOpciones.getString("mensaje"));
                        mostrarMensajesIconosResultado(objectOpciones.getString("mensaje"),R.drawable.status_error_actualizacion);
                        FirebaseCrash.report(new Exception( objectOpciones.getString("mensaje") + ":" + jsonParams.toString()  ));

                    }

                }catch (Exception e){

                    mostrarMensajeError("Error interno", "No se pudo procesar la respuesta del servidor");
                    FirebaseCrash.report(new Exception( "No se pudo procesar la respuesta del servidor" + ":" + jsonParams.toString() + " - " + e.toString() ));
                    e.printStackTrace();
                }

            }



        });

        return requestHandle;

    }

    private void mostrarMensajesIconosResultado(String mensaje, int idDrawable){
        imageViewStatus.setImageResource(idDrawable);
        textViewStatusError.setText(mensaje);
    }

}
