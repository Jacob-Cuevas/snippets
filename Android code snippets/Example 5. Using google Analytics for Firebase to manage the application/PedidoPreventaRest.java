package com.example.metodos1.puntodeventadetalle.Rest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.metodos1.puntodeventadetalle.Adaptadores.ImpresionTicketPreventaWrapper;
import com.example.metodos1.puntodeventadetalle.Adaptadores.ImpresoraZebraController;
import com.example.metodos1.puntodeventadetalle.Configuracion.Constantes;
import com.example.metodos1.puntodeventadetalle.Daos.AlmacenCentroDao;
import com.example.metodos1.puntodeventadetalle.Daos.AreaVentaDao;
import com.example.metodos1.puntodeventadetalle.Daos.ArticuloPreventaDao;
import com.example.metodos1.puntodeventadetalle.Daos.DetalleDescuentoPreventaSapDao;
import com.example.metodos1.puntodeventadetalle.Daos.DetallePreventaSapDao;
import com.example.metodos1.puntodeventadetalle.Daos.PreventaSapDao;
import com.example.metodos1.puntodeventadetalle.Dominio.AlmacenCentro;
import com.example.metodos1.puntodeventadetalle.Dominio.AreaVenta;
import com.example.metodos1.puntodeventadetalle.Dominio.ArticuloDescuentoValorWrapper;
import com.example.metodos1.puntodeventadetalle.Dominio.ClientePreventa;
import com.example.metodos1.puntodeventadetalle.Dominio.DetalleDescuentoPreventaSap;
import com.example.metodos1.puntodeventadetalle.Dominio.DetalleTemporalPreventaWrapper;
import com.example.metodos1.puntodeventadetalle.Dominio.PreventaSap;
import com.google.firebase.crash.FirebaseCrash;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by JCuevas on 19/12/2016.
 */

public class PedidoPreventaRest {

    private String URL_CAPTURA_PREVENTA = Constantes.URL_SERVIDOR;
    private Context context;
    private String idDispositivo;
    private String token;
    private FragmentManager fragmentManager;
    private ArticuloPreventaDao articuloPreventaDao;
    private AreaVentaDao areaVentaDao;
    private AlmacenCentroDao almacenCentroDao;
    private PreventaSapDao preventaSapDao;
    private DetallePreventaSapDao detallePreventaSapDao;
    private DetalleDescuentoPreventaSapDao detalleDescuentoPreventaSapDao;
    private Activity activity;
    private int idEmpresaActual;
    //Impresion controller
    private ImpresoraZebraController impresoraZebraController;

    public PedidoPreventaRest(FragmentManager fragmentManager, Context context, Activity activity, String idDispositivo, String token, int idEmpresaActual){
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.activity = activity;
        this.idDispositivo = idDispositivo;
        this.token = token;
        this.idEmpresaActual = idEmpresaActual;
    }

    public RequestHandle guardarPedidoPreventaServidor(final String idAlmacen,
                                                       final String idCentro,
                                                       final String idSector,
                                                       final String folioPedido,
                                                       final String idAreaVentas,
                                                       final String idCanal,
                                                       final String idPuestoExpedicion,
                                                       final String observaciones,
                                                       final String fechaEntrega,
                                                       final String fechaCaptura,
                                                       final String idVendedor,
                                                       final String codigoVendedor,
                                                       final ClientePreventa clientePreventa,
                                                       final List<DetalleTemporalPreventaWrapper> detalleTemporalPreventaWrappers,
                                                       final String latitud,
                                                       final String longitud){

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Guardando pedido, porfavor espere...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        //Init DAOS
        articuloPreventaDao = new ArticuloPreventaDao();
        areaVentaDao = new AreaVentaDao();
        almacenCentroDao = new AlmacenCentroDao();
        preventaSapDao = new PreventaSapDao();
        detallePreventaSapDao = new DetallePreventaSapDao();
        detalleDescuentoPreventaSapDao = new DetalleDescuentoPreventaSapDao();

        AreaVenta areaVenta = null;

        final Long tsLong = System.currentTimeMillis()/1000;

        //Crear JSONObject de request
        JSONObject jsonParams = new JSONObject();
        try{

            jsonParams.put("controlador", "App");
            jsonParams.put("metodo", "guardarPreventa");
            jsonParams.put("dispositivo", idDispositivo);
            jsonParams.put("token", token);

            JSONObject jsonObjectVenta = new JSONObject();
            jsonObjectVenta.put("dispositivo", idDispositivo);

            jsonObjectVenta.put("folioDispositivo", tsLong.intValue());

            JSONObject jsonObjectTipoVenta = new JSONObject();
            jsonObjectTipoVenta.put("id", Constantes.TIPO_VENTA_PREVENTA);

            areaVenta = areaVentaDao.getAreaVentaPorId(idAreaVentas);

            JSONObject jsonObjectAreaVentas = new JSONObject();
            jsonObjectAreaVentas.put("id", idAreaVentas);
            jsonObjectAreaVentas.put("descripcion", areaVenta.getDescripcion() );

            JSONObject jsonObjectCanal = new JSONObject();
            jsonObjectCanal.put("id", idCanal);

            JSONObject jsonObjectCentro = new JSONObject();
            jsonObjectCentro.put("id", idCentro);

            JSONObject jsonObjectAlmacen = new JSONObject();
            jsonObjectAlmacen.put("codigo", idAlmacen);

            AlmacenCentro almacenCentro = almacenCentroDao.getAlmacenCentroPorId(idAlmacen);
            if(almacenCentro != null){
                jsonObjectAlmacen.put("descripcion", almacenCentro.getDescripcion());
            }

            JSONObject jsonObjectPuestoExpedicion = new JSONObject();
            jsonObjectPuestoExpedicion.put("id", idPuestoExpedicion);

            jsonObjectVenta.put("tipoVenta", jsonObjectTipoVenta);
            jsonObjectVenta.put("areaVentas", jsonObjectAreaVentas);
            jsonObjectVenta.put("canal", jsonObjectCanal);
            jsonObjectVenta.put("centro", jsonObjectCentro);
            jsonObjectVenta.put("almacen", jsonObjectAlmacen);
            jsonObjectVenta.put("puestoExpedicion",jsonObjectPuestoExpedicion );
            jsonObjectVenta.put("idSector", idSector);
            jsonObjectVenta.put("idGrupoVendedores", areaVenta.getIdGrupoVendedores());
            jsonObjectVenta.put("ordenCompra", folioPedido);
            jsonObjectVenta.put("comentarios", observaciones);
            jsonObjectVenta.put("fechaEntrega", fechaEntrega);
            jsonObjectVenta.put("fechaCaptura", fechaCaptura);

            switch (idEmpresaActual){

                case Constantes.TEMA_CONFIGURACION_PROTEINAS:
                    jsonObjectVenta.put("latitud", latitud.equals("") ? Constantes.LATITUD_PROTEINAS : latitud);
                    jsonObjectVenta.put("longitud", longitud.equals("") ? Constantes.LONGITUD_PROTEINAS : longitud );
                    break;
                case Constantes.TEMA_CONFIGURACION_CMG:
                    jsonObjectVenta.put("latitud", latitud.equals("") ? Constantes.LATITUD_CMG : latitud);
                    jsonObjectVenta.put("longitud", longitud.equals("") ? Constantes.LONGITUD_CMG : longitud );
                    break;
                case Constantes.TEMA_CONFIGURACION_MAIZZA:
                    jsonObjectVenta.put("latitud", latitud.equals("") ? Constantes.LATITUD_CMG : latitud);
                    jsonObjectVenta.put("longitud", longitud.equals("") ? Constantes.LONGITUD_CMG : longitud );
                    break;
                case Constantes.TEMA_CONFIGURACION_PRINCESA:

                    break;
                case Constantes.TEMA_CONFIGURACION_HOMBRE_CAMION:
                    jsonObjectVenta.put("latitud", latitud.equals("") ? Constantes.LATITUD_CMG : latitud);
                    jsonObjectVenta.put("longitud", longitud.equals("") ? Constantes.LONGITUD_CMG : longitud );
                    break;
                default:
                    break;

            }

            jsonObjectVenta.put("posicionDefault", longitud.equals("") ? 1 : 0 );

            JSONObject jsonObjectVendedor = new JSONObject();
            jsonObjectVendedor.put("id", idVendedor);
            jsonObjectVendedor.put("codigo", codigoVendedor);

            JSONObject jsonObjectCliente = new JSONObject();
            jsonObjectCliente.put("id", clientePreventa.getIdClientePreventa());
            jsonObjectCliente.put("codigoSap", clientePreventa.getCodigoSap());
            jsonObjectCliente.put("grupoPrecio", clientePreventa.getGrupoPrecio());
            jsonObjectCliente.put("zonaVentas", clientePreventa.getZonaVentas());

            JSONArray jsonArrayDetalle = new JSONArray();

            for(DetalleTemporalPreventaWrapper detalleTemporalPreventaWrapper : detalleTemporalPreventaWrappers){

                JSONObject jsonObjectDetalle = new JSONObject();

                JSONObject jsonObjectArticulo = new JSONObject();
                jsonObjectArticulo.put("id", detalleTemporalPreventaWrapper.getArticuloPreventa().getIdArticulo());
                jsonObjectArticulo.put("codigo", detalleTemporalPreventaWrapper.getArticuloPreventa().getCodigo());
                jsonObjectArticulo.put("descripcion", detalleTemporalPreventaWrapper.getArticuloPreventa().getDescripcion());
                jsonObjectArticulo.put("peso", detalleTemporalPreventaWrapper.getArticuloPreventa().getPeso());
                jsonObjectArticulo.put("unidadPeso", detalleTemporalPreventaWrapper.getArticuloPreventa().getUnidadPeso());
                jsonObjectArticulo.put("unidadVenta", detalleTemporalPreventaWrapper.getArticuloPreventa().getUnidadVenta());
                jsonObjectArticulo.put("sectorSap", detalleTemporalPreventaWrapper.getArticuloPreventa().getSectorSap());

                jsonObjectDetalle.put("articulo", jsonObjectArticulo);
                jsonObjectDetalle.put("cantidad", detalleTemporalPreventaWrapper.getCantidad());

                JSONObject jsonObjectDetalleArticulo = new JSONObject();

                JSONArray jsonArrayDescuentos = new JSONArray();

                for(ArticuloDescuentoValorWrapper articuloDescuentoValorWrappers : detalleTemporalPreventaWrapper.getArticuloDescuentoValorWrappers() ){

                    JSONObject jsonObjectDescuento = new JSONObject();
                    jsonObjectDescuento.put("id", articuloDescuentoValorWrappers.getDescuento().getIdDescuento());
                    jsonObjectDescuento.put("nombre", articuloDescuentoValorWrappers.getDescuento().getNombre());

                    JSONObject jsonObjectTipoCondicion = new JSONObject();
                    jsonObjectTipoCondicion.put("id", articuloDescuentoValorWrappers.getDescuento().getTipoCondicion());

                    jsonObjectDescuento.put("tipoCondicion", jsonObjectTipoCondicion);
                    jsonObjectDescuento.put("aplicacion", articuloDescuentoValorWrappers.getDescuento().getAplicacion());
                    jsonObjectDescuento.put("cantidad", articuloDescuentoValorWrappers.getValorDescuentoAplicado());

                    jsonArrayDescuentos.put(jsonObjectDescuento);

                }

                jsonObjectDetalleArticulo.put("descuentos", jsonArrayDescuentos);

                jsonObjectDetalle.put("detalle", jsonObjectDetalleArticulo);

                jsonArrayDetalle.put(jsonObjectDetalle);

            }

            jsonObjectVenta.put("vendedor", jsonObjectVendedor);
            jsonObjectVenta.put("cliente", jsonObjectCliente);
            jsonObjectVenta.put("detalle", jsonArrayDetalle);
            jsonParams.put("venta", jsonObjectVenta);

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception(e.toString() + jsonParams.toString()));
            e.printStackTrace();
        }

        StringEntity entityLogin = null;
        try {
            entityLogin = new StringEntity(jsonParams.toString(), ContentType.APPLICATION_JSON);
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(new Exception(e.toString() + jsonParams.toString() ));
        }

        //Se notifica al firebase la peticion al servidor
        //FirebaseCrash.report(new Exception( "Notificacion preventa vendedor:" + codigoVendedor + " JSON: " + jsonParams.toString()  ));

        //Se realiza la llamada http
        AsyncHttpClient httpCapturaPreventa = new AsyncHttpClient();
        httpCapturaPreventa.setTimeout(Constantes.TIMEOUT_SINCRONIZACION_RESTS);
        final AreaVenta finalAreaVenta = areaVenta;

        RequestHandle requestHandle = httpCapturaPreventa.post(context, URL_CAPTURA_PREVENTA, entityLogin, "application/json", new TextHttpResponseHandler() {

            /**
             * Metodo para mostrar un mensaje resultado del request
             * @param titulo
             * @param mensaje
             * @param result
             * @param folio
             */
            private void mostrarMensajeError(String titulo, String mensaje, final boolean result, final String folio){

                dialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(titulo)
                        .setMessage(mensaje)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(result){
                                    //Se imprime el ticket
                                    //Se inicializan los parametros de impresion
                                    ImpresionTicketPreventaWrapper impresionTicketPreventaWrapper = new ImpresionTicketPreventaWrapper();
                                    impresionTicketPreventaWrapper.setClientePreventa(clientePreventa);
                                    impresionTicketPreventaWrapper.setFolioCapturaPreventa(folioPedido + "--" + folio);
                                    impresionTicketPreventaWrapper.setDetalleTemporalPreventaWrappers(detalleTemporalPreventaWrappers);

                                    impresoraZebraController = new ImpresoraZebraController(context, activity, ImpresoraZebraController.IMPRESION_TICKET_PREVENTA);
                                    impresoraZebraController.setImpresionTicketPreventaWrapper(impresionTicketPreventaWrapper);
                                    impresoraZebraController.cambiarNumeroTicketImpresion(Constantes.NUM_COPIAS_TICKET_PREDETERMINADO);

                                    new Thread(impresoraZebraController).start();

                                    //Se regresa a la seleccion de cliente
                                    fragmentManager.popBackStackImmediate();
                                }
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }

            /**
             * Metodo para capturar el error
             * @param statusCode
             * @param headers
             * @param responseString
             * @param throwable
             */
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                mostrarMensajeError("Error de conexión", "No se pudo conectar con el servidor.",false, "");

            }

            /**
             * Metodo para obtener el resultado del request
             * @param statusCode
             * @param headers
             * @param responseString
             */
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                try{

                    JSONObject objectArticulos = new JSONObject(responseString);

                    if(objectArticulos.getBoolean("success")){

                        //Se guarda en el historico de pedidos de preventa, detalle y desglose de descuentos
                        preventaSapDao.insertarPreventaSap(objectArticulos.getString("folioPedido"),
                                fechaEntrega,
                                String.valueOf(tsLong.intValue()),
                                idAreaVentas,
                                idCanal,
                                idCentro,
                                idAlmacen,
                                idPuestoExpedicion,
                                idSector,
                                finalAreaVenta.getIdGrupoVendedores(),
                                fechaCaptura,
                                observaciones,
                                latitud,
                                longitud,
                                longitud.equals("") ? 1 : 0,
                                idVendedor,
                                codigoVendedor,
                                clientePreventa.getIdClientePreventa()
                        );


                        for(DetalleTemporalPreventaWrapper detalleTemporalPreventaWrapper : detalleTemporalPreventaWrappers){

                            int idDetallePreventaSapGuardado = (int) detallePreventaSapDao.insertarDetallePreventaSap(
                                    objectArticulos.getString("folioPedido"), detalleTemporalPreventaWrapper.getArticuloPreventa().getIdArticulo(), detalleTemporalPreventaWrapper.getCantidad(),
                                    detalleTemporalPreventaWrapper.getConsultaPrecioArticuloPreventaRestResponseWrapper().getPrecioDescuento(),
                                    detalleTemporalPreventaWrapper.getConsultaPrecioArticuloPreventaRestResponseWrapper().getTotalIva());

                            for(ArticuloDescuentoValorWrapper articuloDescuentoValorWrappers : detalleTemporalPreventaWrapper.getArticuloDescuentoValorWrappers() ){

                                detalleDescuentoPreventaSapDao.insertarDetalleDescuentoPreventaSap(idDetallePreventaSapGuardado, articuloDescuentoValorWrappers.getDescuento().getIdDescuento(), articuloDescuentoValorWrappers.getValorDescuentoAplicado());

                            }

                        }

                        //Se muestra el mensaje de resultado y la impresion del ticket
                        mostrarMensajeError("Preventa sincronizada", "Se ha generado el pedido con folio: " + objectArticulos.getString("folioPedido"), true, objectArticulos.getString("folioPedido"));

                    } else {

                        mostrarMensajeError("Error del servidor", objectArticulos.getString("mensaje"), false, "");

                    }

                }catch (Exception e){
                    mostrarMensajeError("Error interno", "No se pudo procesar la respuesta del servidor para los articulos.", false, "");
                    FirebaseCrash.report(new Exception(e.toString()));
                    e.printStackTrace();
                }

            }

        });


        return requestHandle;

    }

}
