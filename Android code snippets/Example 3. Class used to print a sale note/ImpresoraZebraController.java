package com.example.metodos1.puntodeventadetalle.Adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.metodos1.puntodeventadetalle.Configuracion.Constantes;
import com.example.metodos1.puntodeventadetalle.Configuracion.FuncionesGPS;
import com.example.metodos1.puntodeventadetalle.Configuracion.FuncionesMonetarias;
import com.example.metodos1.puntodeventadetalle.Daos.ClienteCensoDao;
import com.example.metodos1.puntodeventadetalle.Daos.ClienteDao;
import com.example.metodos1.puntodeventadetalle.Daos.ConfiguracionDao;
import com.example.metodos1.puntodeventadetalle.Daos.DetalleVentaDao;
import com.example.metodos1.puntodeventadetalle.Daos.ExistenciaDao;
import com.example.metodos1.puntodeventadetalle.Daos.LoginDao;
import com.example.metodos1.puntodeventadetalle.Daos.ProductoDaos;
import com.example.metodos1.puntodeventadetalle.Daos.VentaDao;
import com.example.metodos1.puntodeventadetalle.Daos.VisitaClienteDao;
import com.example.metodos1.puntodeventadetalle.Dominio.Cliente;
import com.example.metodos1.puntodeventadetalle.Dominio.ClienteCenso;
import com.example.metodos1.puntodeventadetalle.Dominio.Configuracion;
import com.example.metodos1.puntodeventadetalle.Dominio.DetalleVenta;
import com.example.metodos1.puntodeventadetalle.Dominio.Existencia;
import com.example.metodos1.puntodeventadetalle.Dominio.Producto;
import com.example.metodos1.puntodeventadetalle.Dominio.Venta;
import com.example.metodos1.puntodeventadetalle.Dominio.VisitaCliente;
import com.example.metodos1.puntodeventadetalle.R;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JCuevas on 31/03/2016.
 */
public class ImpresoraZebraController implements Runnable {

    //Variables de Impresora ZEBRA
    private Connection printerConnection;
    private ZebraPrinter printer;
    private SharedPreferences preferences;
    private Context context;
    private Activity activity;
    private AlertDialog alertDialogEstatus;
    private AlertDialog alertDialogError;
    private String operacionImpresion;
    private FuncionesMonetarias funcionesMonetarias;
    private int idVenta;
    private boolean isVentaConObsequio;
    public static final String IMPRESION_EXISTENCIA = "Existencias";
    public static final String IMPRESION_LIQUIDACION = "Liquidacion";
    public static final String IMPRESION_NOTA_VENTA = "NotaVenta";
    //Daos
    private ExistenciaDao existenciaDao;
    private ProductoDaos productoDaos;
    private LoginDao loginDao;
    private VentaDao ventaDao;
    private DetalleVentaDao detalleVentaDao;
    private ClienteDao clienteDao;
    private ConfiguracionDao configuracionDao;
    private VisitaClienteDao visitaClienteDao;
    private ClienteCensoDao clienteCensoDao;
    private int numTicketsImpresion = 1;

    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

    public ImpresoraZebraController(Context context, Activity activity, String operacionImpresion){

        this.context = context;
        this.activity = activity;
        this.operacionImpresion = operacionImpresion;
        preferences = context.getSharedPreferences(Constantes.MY_PREFERENCIAS_NOMBRE, Constantes.CONTEXT_PRIVATE);

    }

    public void cambiarNumeroTicketImpresion(int numTicketsImpresion){

        this.numTicketsImpresion = numTicketsImpresion;

    }

    public void setearIdVentaImpresionNotaVenta(int idVenta){
        this.idVenta = idVenta;
    }

    @Override
    public void run() {

        if (!bluetooth.isEnabled()) {

            showSettingsAlert(context, activity);

        } else {

            printer = connect();

            if(operacionImpresion.equals(IMPRESION_EXISTENCIA)){

                if(printer == null){

                    mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true, true);

                } else {

                    byte[] bytesContent = getBytesImpresioExistenciasProducto();
                    printContentZebra(bytesContent);

                }

            } else
            if(operacionImpresion.equals(IMPRESION_LIQUIDACION)){

                if(printer == null){

                    mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true, true);

                } else {

                    byte[] bytesContent = getBytesImpresionLiquidacionVentas();
                    printContentZebra(bytesContent);

                }

            } else
            if(operacionImpresion.equals(IMPRESION_NOTA_VENTA)){

                if(printer == null){

                    mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true, true);

                } else {

                    for(int i = 0; i < numTicketsImpresion; i++){
                        //Se imprime ticket de venta
                        byte[] bytesContent = getBytesImpresionNotaVenta(idVenta);
                        printContentZebra(bytesContent);
                        //Se imprime ticket de complementp de obsequios
                        if(isVentaConObsequio){
                            byte[] bytesComplemento = getBytesImpresionObsequiosNotaVenta(idVenta);
                            printContentZebra(bytesComplemento);
                        }

                    }

                    disconnect();

                }

            }

        }

    }

    private ZebraPrinter connect() {

        actualizarAlertDialogEstatusImpresionDescripcion("Conectando la impresora");

        String direccionMac = "";

        //Se recupera la mac de la impresora
        configuracionDao = new ConfiguracionDao();
        Configuracion configuracion = configuracionDao.getValorConfiguracionPorClave(Constantes.CLAVE_DIRECCION_MAC_IMPRESORA);

        //if(preferences.contains("mac_impresora")){
        if(configuracion != null && !configuracion.getValorOpcion().equals("")){
            //direccionMac = preferences.getString("mac_impresora","");
            direccionMac = configuracion.getValorOpcion();
        }

        printerConnection = new BluetoothConnection( direccionMac );

        try {
            printerConnection.open();

        } catch (ConnectionException e) {

            mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true, true);

            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {

                printer = ZebraPrinterFactory.getInstance(printerConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                //actualizarAlertDialogEstatusImpresionDescripcion("Impresora conectada");

            } catch (ConnectionException e) {

                printer = null;

                disconnect();

                mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true, true);

            } catch (ZebraPrinterLanguageUnknownException e) {

                printer = null;

                disconnect();

                mostrarMensajesLive("Ocurrió un error al conectar con la impresora",R.drawable.printer_connected_icon, true,true);

            }
        }

        return printer;
    }

    private void disconnect() {
        try {

            actualizarAlertDialogEstatusImpresionDescripcion("Desconectando impresora");

            if (printerConnection != null) {
                printerConnection.close();
            }

            if(alertDialogEstatus.isShowing()){
                alertDialogEstatus.dismiss();
            }

        } catch (ConnectionException e) {
            e.printStackTrace();
        } finally {

        }
    }

    //Impresion del ticket para inventario
    private byte[] getBytesImpresioExistenciasProducto() {

        productoDaos = new ProductoDaos();
        existenciaDao = new ExistenciaDao();
        loginDao = new LoginDao();

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;

        if (printerLanguage == PrinterLanguage.ZPL) {

            DateFormat dateFormatFecha = new SimpleDateFormat("dd/MM/yyyy");
            Date dateFecha = new Date();
            DateFormat dateFormatHora = new SimpleDateFormat("HH:mm:ss");
            Date dateHora = new Date();

            String textoImpresion = "";
            List<Producto> productos = productoDaos.getTodosProductos();
            textoImpresion += "! U1 BEGIN-PAGE\r\n" +
                    "! U1 JOURNAL \r\n" +
                    "! U SETLP 5 2 46\r\n" +
                    "SETSP 5 \r\n" +
                    "PRINT\r\n" +
                    "   GRUPO CMG\r\n" +
                    "! U SETLP 5 0 24\r\nSETSP 0 PRINT\r\n" +
                    "        Calle 41 No. 475B x 50 y 52,\r\n" +
                    "          Col. Centro, C.P. 97000\r\n" +
                    "                   (999) 983 2121\r\n\r\n" +
                    "! U1 SETLP 7 0 24\r\n" +
                    "   Fecha y hora de impresion:\r\n" +
                    "   " + dateFormatFecha.format(dateFecha) + "        " + dateFormatHora.format(dateHora) + "\r\n" +
                    "! U1 SETSP 0\r\n" +
                    "! U1 SETBOLD 1\r\nVendedor\r\n" +
                    "! U1 SETBOLD 0\r\n" + loginDao.buscarLoginPorIdVendedor(preferences.getString("idVendedor","")).getNombreVendedor() + "\r\n" +
                    //"! U1 SETBOLD 1\r\nRuta Actual\r\n" +
                    //"! U1 SETBOLD 0\r\n%ruta%\r\n" +
                    "! U1 SETBOLD 1\r\nExistencia Actual\r\n! U1 SETBOLD 0" +
                    "________________________________\r\n";

            for(Producto producto : productos){

                textoImpresion += "! U1 SETBOLD 0\r\n";
                textoImpresion += producto.getIdArticulo() + " - " + producto.getDescripcion() + "\r\n";

                Existencia existencia = existenciaDao.getExistenciaPorProducto(producto.getIdArticulo());
                if(existencia != null /*&& existencia.getCantidadExistencia() > 0*/){

                    textoImpresion += "Existencia: " + String.valueOf(existencia.getCantidadExistencia()) ;

                } else {

                    textoImpresion += "Existencia: " + "0.0" ;

                }

                textoImpresion += "\r\n" + "-------------------------------\r\n";

            }

            textoImpresion += "OBSERVACIONES\r\n";
            textoImpresion += "-------------------------------\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n";
            textoImpresion += "\r\n" + "-------------------------------\r\n";

            textoImpresion += "! U1 SETLP 7 0 24\r\n" +
                    "\r\n\r\n\r\n! U1 END-PAGE\r\n";

            configLabel = textoImpresion.getBytes();

        } else
        if (printerLanguage == PrinterLanguage.CPCL) {
            //DESCONOCIDO
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }

        return configLabel;
    }

    //Impresion de la liquidacion
    private byte[] getBytesImpresionLiquidacionVentas(){

        productoDaos = new ProductoDaos();
        loginDao = new LoginDao();
        ventaDao = new VentaDao();
        detalleVentaDao = new DetalleVentaDao();
        visitaClienteDao = new VisitaClienteDao();
        clienteDao = new ClienteDao();
        clienteCensoDao = new ClienteCensoDao();

        List<ImpresionLiquidacionVentaWrapper> listadoProductosPrecioDif = new ArrayList<ImpresionLiquidacionVentaWrapper>();
        HashMap<String, Double > hashMapCantidadProductosVendidos = new HashMap<String, Double >();
        HashMap<String, Double > hasMapMontoTotalProductoVendido = new HashMap<String, Double >();
        HashMap<String, Double > hasMapMontoTotalObsequios = new HashMap<String, Double>();

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;

        if (printerLanguage == PrinterLanguage.ZPL) {

            DateFormat dateFormatFecha = new SimpleDateFormat("dd/MM/yyyy");
            Date dateFecha = new Date();
            DateFormat dateFormatHora = new SimpleDateFormat("HH:mm:ss");
            Date dateHora = new Date();

            List<Venta> listadoVentas = ventaDao.getAllVentasActivas();
            List<DetalleVenta> listadoDetalleVentas = new ArrayList<DetalleVenta>();
            //Se recupera todos los detalles
            for(Venta venta : listadoVentas){
                List<DetalleVenta> detalleVentas_venta = detalleVentaDao.getListadoDetalleVentaPorIdVenta(venta.getIdVenta());
                listadoDetalleVentas.addAll(detalleVentas_venta);
            }
            //Se agrupan los detalles por producto
            for(DetalleVenta dv : listadoDetalleVentas){

                double cantidadProductos = 0;
                double importeProducto = 0;
                double cantidadObsequios = 0;

                //Se valida si es obsequio
                if(dv.getPrecio() == 0){

                    if(hasMapMontoTotalObsequios.containsKey(dv.getIdArticulo())){

                        cantidadObsequios = hasMapMontoTotalObsequios.get(dv.getIdArticulo());
                        cantidadObsequios += dv.getCantidad();
                        hasMapMontoTotalObsequios.put(dv.getIdArticulo(), cantidadObsequios);

                    } else {

                        hasMapMontoTotalObsequios.put(dv.getIdArticulo(), dv.getCantidad());

                    }

                } else {
                    //Producto de venta se valida por importe
                    if(hashMapCantidadProductosVendidos.containsKey(dv.getIdArticulo())){

                        importeProducto = hasMapMontoTotalProductoVendido.get(dv.getIdArticulo());
                        cantidadProductos = hashMapCantidadProductosVendidos.get(dv.getIdArticulo());

                        BigDecimal valuePrecioProductoExistente = FuncionesGPS.truncateDecimal((importeProducto/cantidadProductos), 2);
                        BigDecimal valuePrecioProductoNuevo = FuncionesGPS.truncateDecimal((dv.getPrecio()/dv.getCantidad()), 2);

                        if( valuePrecioProductoExistente.doubleValue() == valuePrecioProductoNuevo.doubleValue() ){
                        //if( (importeProducto/cantidadProductos) == (dv.getPrecio()/dv.getCantidad()) ){
                            //Se suman las cantidades
                            cantidadProductos += dv.getCantidad();
                            hashMapCantidadProductosVendidos.put(dv.getIdArticulo(), cantidadProductos);

                            importeProducto += dv.getPrecio();
                            hasMapMontoTotalProductoVendido.put(dv.getIdArticulo(), importeProducto);

                        } else {

                            //Se itera para ver si no existe ese producto con ese precio
                            boolean isInLista = false;
                            for(ImpresionLiquidacionVentaWrapper ilvw : listadoProductosPrecioDif){

                                BigDecimal valuePrecioProductExist = FuncionesGPS.truncateDecimal((ilvw.getPrecio()/ilvw.getCantidad()), 2);
                                BigDecimal valuePrecioProductNue = FuncionesGPS.truncateDecimal((dv.getPrecio()/dv.getCantidad()), 2);

                                if(ilvw.getIdArticulo().equals(dv.getIdArticulo())
                                        && ( valuePrecioProductExist.doubleValue() == valuePrecioProductNue.doubleValue() ) ){
                                        //&& ( (ilvw.getPrecio()/ilvw.getCantidad()) == (dv.getPrecio()/dv.getCantidad()) ) ){

                                    ImpresionLiquidacionVentaWrapper newImpresionWrapper = new ImpresionLiquidacionVentaWrapper();

                                    double price = ilvw.getPrecio();
                                    price += dv.getPrecio();

                                    double quantity = ilvw.getCantidad();
                                    quantity+= dv.getCantidad();

                                    newImpresionWrapper.setCantidad(quantity);
                                    newImpresionWrapper.setIdArticulo(ilvw.getIdArticulo());
                                    newImpresionWrapper.setPrecio(price);

                                    listadoProductosPrecioDif.remove(ilvw);
                                    listadoProductosPrecioDif.add(newImpresionWrapper);

                                    isInLista = true;
                                    break;

                                }
                            }

                            if(!isInLista){

                                ImpresionLiquidacionVentaWrapper newImpresionWrapper = new ImpresionLiquidacionVentaWrapper();
                                newImpresionWrapper.setCantidad(dv.getCantidad());
                                newImpresionWrapper.setIdArticulo(dv.getIdArticulo());
                                newImpresionWrapper.setPrecio(dv.getPrecio());
                                listadoProductosPrecioDif.add(newImpresionWrapper);

                            }
                            //hashMapCantidadProductosVendidos.put(dv.getIdArticulo(), dv.getCantidad());
                            //hasMapMontoTotalProductoVendido.put(dv.getIdArticulo(), dv.getPrecio());
                        }

                    } else {
                        hashMapCantidadProductosVendidos.put(dv.getIdArticulo(), dv.getCantidad());
                        hasMapMontoTotalProductoVendido.put(dv.getIdArticulo(), dv.getPrecio());
                    }

                }

            }
            //Se itera el resultado final para elaborar el ticket

            String textoImpresion = "! U1 BEGIN-PAGE\r\n" +
                    "! U1 JOURNAL \r\n" +
                    "! U SETLP 5 2 46\r\n" +
                    "SETSP 5 \r\n" +
                    "PRINT\r\n" +
                    "   GRUPO CMG\r\n" +
                    "! U SETLP 5 0 24\r\nSETSP 0 PRINT\r\n" +
                    "        Calle 41 No. 475B x 50 y 52,\r\n" +
                    "          Col. Centro, C.P. 97000\r\n" +
                    "                   (999) 983 2121\r\n\r\n" +
                    "! U1 SETLP 7 0 24\r\n" +
                    "   Fecha y hora de impresion:\r\n" +
                    "   " + dateFormatFecha.format(dateFecha) + "        " + dateFormatHora.format(dateHora) + "\r\n" +
                    "! U1 SETSP 0\r\n" +
                    "! U1 SETBOLD 1\r\nVendedor\r\n" +
                    "! U1 SETBOLD 0\r\n" + loginDao.buscarLoginPorIdVendedor(preferences.getString("idVendedor","")).getNombreVendedor() + "\r\n" +
                    //"! U1 SETBOLD 1\r\nRuta Actual\r\n" +
                    //"! U1 SETBOLD 0\r\n%ruta%\r\n" +
                    "! U1 SETBOLD 1\r\nLiquidacion de Ventas\r\n! U1 SETBOLD 0" +
                    "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\nProductos de Venta\r\n! U1 SETBOLD 0\r\nCantidad     Precio     Total\r\n! U1 SETLP 7 0 16\r\n" +
                    "================================\r\n";

            BigDecimal displayMontoVentaDia = new BigDecimal(0);

            //Productos vendidos
            for(String idArticulo : hashMapCantidadProductosVendidos.keySet()){

                Producto producto = productoDaos.getProductoPorId(idArticulo);
                double cantidadFinalProduct = hashMapCantidadProductosVendidos.get(idArticulo);
                double importeFinalProduct = hasMapMontoTotalProductoVendido.get(idArticulo);
                double importeProducto = importeFinalProduct / cantidadFinalProduct;

                BigDecimal bdMontoProducto = new BigDecimal(importeProducto);
                BigDecimal bdMontoFinal = new BigDecimal(importeFinalProduct);

                BigDecimal displayMontoProducto = bdMontoProducto.setScale(2, RoundingMode.HALF_UP);
                BigDecimal displayMontoFinal = bdMontoFinal.setScale(2, RoundingMode.HALF_UP);

                textoImpresion += "! U1 SETBOLD 1\r\n" +  producto.getDescripcion() + "\r\n! U1 SETBOLD 0\r\n" + "  " +
                        String.valueOf(cantidadFinalProduct) + String.format("%0" + (11 - String.valueOf(cantidadFinalProduct).length()) + "d", 0).replace('0', ' ') +
                        String.valueOf(displayMontoProducto.doubleValue()) + String.format("%0" + (10 - String.valueOf(displayMontoProducto.doubleValue()).length()) + "d", 0).replace('0', ' ') +
                        FuncionesMonetarias.getFormatoMoneda().format( displayMontoFinal.doubleValue() ) + "\r\n";

                displayMontoVentaDia = displayMontoVentaDia.add(new BigDecimal( displayMontoFinal.doubleValue() ));

            }

            //Productos con precio modificado
            for(ImpresionLiquidacionVentaWrapper impresionLiquidacionVentaWrapper : listadoProductosPrecioDif){

                Producto producto = productoDaos.getProductoPorId(impresionLiquidacionVentaWrapper.getIdArticulo());
                double cantidadFinalProduct = impresionLiquidacionVentaWrapper.getCantidad();
                double importeFinalProduct = impresionLiquidacionVentaWrapper.getPrecio();
                double importeProducto = importeFinalProduct / cantidadFinalProduct;

                BigDecimal bdMontoProducto = new BigDecimal(importeProducto);
                BigDecimal bdMontoFinal = new BigDecimal(importeFinalProduct);

                BigDecimal displayMontoProducto = bdMontoProducto.setScale(2, RoundingMode.HALF_UP);
                BigDecimal displayMontoFinal = bdMontoFinal.setScale(2, RoundingMode.HALF_UP);

                textoImpresion += "! U1 SETBOLD 1\r\n" +  producto.getDescripcion() + "\r\n! U1 SETBOLD 0\r\n" + "  " +
                        String.valueOf(cantidadFinalProduct) + String.format("%0" + (11 - String.valueOf(cantidadFinalProduct).length()) + "d", 0).replace('0', ' ') +
                        String.valueOf(displayMontoProducto.doubleValue()) + String.format("%0" + (10 - String.valueOf(displayMontoProducto.doubleValue()).length()) + "d", 0).replace('0', ' ') +
                        funcionesMonetarias.getFormatoMoneda().format( displayMontoFinal.doubleValue() ) + "\r\n";

                displayMontoVentaDia = displayMontoVentaDia.add(new BigDecimal( displayMontoFinal.doubleValue() ));

            }

            textoImpresion += "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\n\r\nObsequios\r\n! U1 SETBOLD 0\r\nCantidad     Precio     Total\r\n! U1 SETLP 7 0 16\r\n" +
                    "================================\r\n";

            //Productos obsequiados
            for(String idArticuloObsequio : hasMapMontoTotalObsequios.keySet()){

                Producto producto = productoDaos.getProductoPorId(idArticuloObsequio);
                double cantidadFinalObsequios = hasMapMontoTotalObsequios.get(idArticuloObsequio);

                textoImpresion += "! U1 SETBOLD 1\r\n" +  producto.getDescripcion() + "\r\n! U1 SETBOLD 0\r\n" + "  " +
                        String.valueOf(cantidadFinalObsequios) + String.format("%0" + (11 - String.valueOf(cantidadFinalObsequios).length()) + "d", 0).replace('0', ' ') +
                        String.valueOf( 0.0 ) + String.format("%0" + (10 - String.valueOf(0.0).length()) + "d", 0).replace('0', ' ') +
                        funcionesMonetarias.getFormatoMoneda().format( 0.0 ) + "\r\n";

            }

            //Se imprime los totales
            BigDecimal displayMontoTotalFormato = displayMontoVentaDia.setScale(2, RoundingMode.HALF_UP);
            textoImpresion += "================================\r\n! U1 SETLP 7 0 24\r\n" +
                    "TOTAL: " + funcionesMonetarias.convertirNumeroATexto(String.valueOf(displayMontoTotalFormato.doubleValue()), true) + "\r\n" +
                    "SON: " + funcionesMonetarias.getFormatoMoneda().format(displayMontoTotalFormato.doubleValue()) + "\r\n";

            //Relacion de Visitas Registradas del dia
            textoImpresion += "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\n\r\nVisitas Registradas\r\n! U1 SETBOLD 0\r\n! U1 SETLP 7 0 16\r\n" +
                    "================================\r\n";

            List<VisitaCliente> visitas = visitaClienteDao.getListadoVisitaClientesDia();
            int countVisitas = 0;
            int countVentas = 0;

            for(VisitaCliente visitaCliente : visitas){

                Cliente cliente = clienteDao.getClientePorIdCliente(String.valueOf(visitaCliente.getIdCliente()));

                if(visitaCliente.getIsVenta() == Constantes.IS_VISITA_CON_VENTA){

                    textoImpresion += "! U1 SETBOLD 1\r\n" +  cliente.getCodigoAux() + " - VENTA \r\n! U1 SETBOLD 0\r\n";
                    countVentas ++;

                } else {

                    textoImpresion += "! U1 SETBOLD 1\r\n" +  cliente.getCodigoAux() + " - SIN VENTA \r\n! U1 SETBOLD 0\r\n";
                    countVisitas ++;

                }

                textoImpresion +=  cliente.getNombre()  + "\r\n" +
                        "Hora: " + visitaCliente.getHora() + "  " + "Fecha: " + visitaCliente.getFecha() + "\r\n";

            }

            textoImpresion += "\r\n================================\r\n"
                    + "! U1 SETBOLD 1 VISITAS CON VENTA: " + String.valueOf(countVentas) + "\r\n"
                    + "! U1 SETBOLD 1 VISITAS SIN VENTA: " + String.valueOf(countVisitas) + "\r\n"
                    + "! U1 SETBOLD 1 TOTAL VISITAS: " + String.valueOf(visitas.size()) + "\r\n ! U1 SETBOLD 0\r\n";

            //Relacion de altas de clientes
            List<ClienteCenso> clienteCensoList = clienteCensoDao.getTodosClientesCenso();

            textoImpresion += "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\n\r\nAlta de Clientes\r\n! U1 SETBOLD 0\r\n! U1 SETLP 7 0 16\r\n" +
                    "================================\r\n";

            for(ClienteCenso clienteCenso : clienteCensoList){

                textoImpresion += "! U1 SETBOLD 1\r\n" +  clienteCenso.getClave() + "! U1 SETBOLD 0 - " + clienteCenso.getNombreTienda() + " \r\n";

            }

            textoImpresion += "________________________________\r\n" +
                    "! U1 SETBOLD 1  TOTAL ALTAS CLIENTES: " + String.valueOf(clienteCensoList.size()) + "\r\n ! U1 SETBOLD 0\r\n";

            textoImpresion += "! U1 SETLP 7 0 24\r\n" +
                    "\r\n\r\n\r\n! U1 END-PAGE\r\n";

            /*textoImpresion += "================================\r\n! U1 SETLP 7 0 24\r\n" +
                    "TOTAL: " + funcionesMonetarias.convertirNumeroATexto(String.valueOf(displayMontoTotalFormato.doubleValue()), true) + "\r\n" +
                    "SON: " + funcionesMonetarias.getFormatoMoneda().format(displayMontoTotalFormato.doubleValue()) + "\r\n" +
                    "! U1 SETLP 7 0 24\r\n" +
                    "\r\n\r\n\r\n! U1 END-PAGE\r\n";*/

            configLabel = textoImpresion.getBytes();

        } else
        if (printerLanguage == PrinterLanguage.CPCL) {
            //DESCONOCIDO
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }

        return configLabel;

    }

    private byte[] getBytesImpresionObsequiosNotaVenta(int idVenta){

        productoDaos = new ProductoDaos();
        loginDao = new LoginDao();
        ventaDao = new VentaDao();
        detalleVentaDao = new DetalleVentaDao();
        clienteDao = new ClienteDao();

        byte[] configLabel = null;

        DateFormat dateFormatFecha = new SimpleDateFormat("dd/MM/yyyy");
        Date dateFecha = new Date();
        DateFormat dateFormatHora = new SimpleDateFormat("HH:mm:ss");
        Date dateHora = new Date();

        Venta venta = ventaDao.getVentaPorIdVenta(idVenta);
        if(venta != null) {

            Cliente cliente = clienteDao.getClientePorIdCliente(venta.getIdCliente());

            String textoImpresion = "! U1 BEGIN-PAGE\r\n" +
                    "! U1 JOURNAL \r\n" +
                    "! U SETLP 5 2 46\r\n" +
                    "SETSP 5 \r\n" +
                    "PRINT\r\n" +
                    "   GRUPO CMG\r\n" +
                    "! U SETLP 5 0 24\r\nSETSP 0 PRINT\r\n" +
                    "        Calle 41 No. 475B x 50 y 52,\r\n" +
                    "          Col. Centro, C.P. 97000\r\n" +
                    "                   (999) 983 2121\r\n\r\n" +
                    "! U1 SETLP 7 0 24\r\n" +
                    "   Fecha y hora de impresion:\r\n" +
                    "   " + dateFormatFecha.format(dateFecha) + "        " + dateFormatHora.format(dateHora) + "\r\n" +
                    "   Folio: " + venta.getFolioVenta() + "\r\n" +
                    "   Estatus venta: " + venta.getEstatus() + "\r\n" +
                    "   Fecha entrega: " + venta.getFechaEntrega() + "\r\n" +
                    "! U1 SETSP 0\r\n" +
                    "! U1 SETBOLD 1\r\nVendedor\r\n" +
                    "! U1 SETBOLD 0\r\n" + loginDao.buscarLoginPorIdVendedor(preferences.getString("idVendedor", "")).getNombreVendedor() + "\r\n" +
                    "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\nCliente\r\n" +
                    "! U1 SETBOLD 0\r\n" + cliente.getNombre() + "\r\n" + cliente.getDireccion() + "\r\n" +
                    "________________________________\r\n" +
                    "! U1 SETBOLD 1\r\nObsequios\r\n! U1 SETBOLD 0\r\nCantidad     Precio     Monto\r\n! U1 SETLP 7 0 16\r\n" +
                    "================================\r\n";

            List<DetalleVenta> listadoDetalleVenta = detalleVentaDao.getListadoDetalleVentaPorIdVenta(idVenta);

            for (DetalleVenta detalleVenta : listadoDetalleVenta) {

                Producto producto = productoDaos.getProductoPorId(detalleVenta.getIdArticulo());

                BigDecimal bdSubtotalProducto = new BigDecimal(detalleVenta.getPrecio());
                BigDecimal displaySubtotalProducto = bdSubtotalProducto.setScale(2, RoundingMode.HALF_UP);

                if (detalleVenta.getPrecio() == 0) {//Obsequio

                    textoImpresion += "! U1 SETBOLD 1\r\n" + producto.getDescripcion() + "\r\n! U1 SETBOLD 0\r\n" +
                            "  " + String.valueOf(detalleVenta.getCantidad()) + String.format("%0" + (11 - String.valueOf(detalleVenta.getCantidad()).length()) + "d", 0).replace('0', ' ') +
                            String.valueOf(detalleVenta.getPrecio()) + String.format("%0" + (10 - String.valueOf(detalleVenta.getPrecio()).length()) + "d", 0).replace('0', ' ') +
                            funcionesMonetarias.getFormatoMoneda().format(displaySubtotalProducto.doubleValue()) + "\r\n";

                }

            }

            textoImpresion += "================================\r\n! U1 SETLP 7 0 24\r\n" +
                   "\r\n\r\n\r\n\r\n\r\n" +
                    "       Firma del cliente        \r\n\r\n\r\n" +
                    "! U1 END-PAGE\r\n";

            configLabel = textoImpresion.getBytes();
        }

        return configLabel;

    }

    private byte[] getBytesImpresionNotaVenta(int idVenta){

        productoDaos = new ProductoDaos();
        loginDao = new LoginDao();
        ventaDao = new VentaDao();
        detalleVentaDao = new DetalleVentaDao();
        clienteDao = new ClienteDao();

        isVentaConObsequio = false;

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;

        if (printerLanguage == PrinterLanguage.ZPL) {

            DateFormat dateFormatFecha = new SimpleDateFormat("dd/MM/yyyy");
            Date dateFecha = new Date();
            DateFormat dateFormatHora = new SimpleDateFormat("HH:mm:ss");
            Date dateHora = new Date();

            Venta venta = ventaDao.getVentaPorIdVenta(idVenta);
            if(venta != null){

                Cliente cliente = clienteDao.getClientePorIdCliente(venta.getIdCliente());

                String textoImpresion = "! U1 BEGIN-PAGE\r\n" +
                        "! U1 JOURNAL \r\n" +
                        "! U SETLP 5 2 46\r\n" +
                        "SETSP 5 \r\n" +
                        "PRINT\r\n" +
                        "   GRUPO CMG\r\n" +
                        "! U SETLP 5 0 24\r\nSETSP 0 PRINT\r\n" +
                        "        Calle 41 No. 475B x 50 y 52,\r\n" +
                        "          Col. Centro, C.P. 97000\r\n" +
                        "                   (999) 983 2121\r\n\r\n" +
                        "! U1 SETLP 7 0 24\r\n" +
                        "   Fecha y hora de impresion:\r\n" +
                        "   " + dateFormatFecha.format(dateFecha) + "        " + dateFormatHora.format(dateHora) + "\r\n" +
                        "   Folio: " + venta.getFolioVenta() + "\r\n" +
                        "   Estatus venta:" + venta.getEstatus() + "\r\n" +
                        "   Fecha entrega:" + venta.getFechaEntrega() + "\r\n" +
                        "! U1 SETSP 0\r\n" +
                        "! U1 SETBOLD 1\r\nVendedor\r\n" +
                        "! U1 SETBOLD 0\r\n" + loginDao.buscarLoginPorIdVendedor(preferences.getString("idVendedor","")).getNombreVendedor() + "\r\n" +
                        "________________________________\r\n" +
                        "! U1 SETBOLD 1\r\nCliente\r\n" +
                        "! U1 SETBOLD 0\r\n" + cliente.getNombre() + "\r\n" + cliente.getDireccion() + "\r\n" +
                        "________________________________\r\n" +
                        "! U1 SETBOLD 1\r\nProducto\r\n! U1 SETBOLD 0\r\nCantidad     Precio     Monto\r\n! U1 SETLP 7 0 16\r\n" +
                        "================================\r\n";

                List<DetalleVenta> listadoDetalleVenta =detalleVentaDao.getListadoDetalleVentaPorIdVenta(idVenta);

                BigDecimal bdImporteFinalNota = new BigDecimal(0);

                for(DetalleVenta detalleVenta : listadoDetalleVenta){

                    Producto producto = productoDaos.getProductoPorId(detalleVenta.getIdArticulo());
                    double precioProducto;

                    BigDecimal bdSubtotalProducto = new BigDecimal(detalleVenta.getPrecio());
                    BigDecimal displaySubtotalProducto = bdSubtotalProducto.setScale(2, RoundingMode.HALF_UP);

                    if(detalleVenta.getPrecio() == 0){//Obsequio
                        precioProducto = 0.0;
                        isVentaConObsequio = true;
                    }else {
                        precioProducto = ( detalleVenta.getPrecio() / detalleVenta.getCantidad() );

                        bdImporteFinalNota = bdImporteFinalNota.add(new BigDecimal(detalleVenta.getPrecio()));
                    }

                    BigDecimal bdPrecioProducto = new BigDecimal(precioProducto);
                    BigDecimal displayPrecioProducto = bdPrecioProducto.setScale(2, RoundingMode.HALF_UP);

                    textoImpresion += "! U1 SETBOLD 1\r\n" + producto.getDescripcion() + "\r\n! U1 SETBOLD 0\r\n" +
                            "  " + String.valueOf(detalleVenta.getCantidad()) + String.format("%0" + (11 - String.valueOf(detalleVenta.getCantidad()).length()) + "d", 0).replace('0', ' ') +
                            String.valueOf(displayPrecioProducto.doubleValue())  + String.format("%0" + (10 - String.valueOf(displayPrecioProducto.doubleValue()).length()) + "d", 0).replace('0', ' ') +
                            funcionesMonetarias.getFormatoMoneda().format( displaySubtotalProducto.doubleValue() ) + "\r\n";

                }

                BigDecimal displayBdImporteFinalNota = bdImporteFinalNota.setScale(2, RoundingMode.HALF_UP);

                textoImpresion += "================================\r\n! U1 SETLP 7 0 24\r\n" +
                        "               TOTAL: " + funcionesMonetarias.getFormatoMoneda().format(displayBdImporteFinalNota.doubleValue()) + "\r\n" +
                        "SON: " + funcionesMonetarias.convertirNumeroATexto(String.valueOf(displayBdImporteFinalNota.doubleValue()), true) + "\r\n\r\n\r\n" +
                       "! U1 END-PAGE\r\n";

                configLabel = textoImpresion.getBytes();

            }

        }

        return configLabel;
    }

    //Método para el envío de datos a la impresora
    private void printContentZebra(byte[] content){

        try {

            actualizarAlertDialogEstatusImpresionDescripcion("Imprimiendo ticket");

            byte[] configLabel = content;
            printerConnection.write(configLabel);

            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();

                Thread.sleep(500);
            }
        } catch (ConnectionException e) {

            mostrarMensajesLive("No se pudo imprimir el ticket",R.drawable.printer_connected_icon, true, true);

            e.printStackTrace();
        } catch (InterruptedException e) {

            e.printStackTrace();

        } finally {

            if(!operacionImpresion.equals(IMPRESION_NOTA_VENTA)){

                disconnect();

            }

        }

    }

    private AlertDialog monstrarMensajesEstatusProcesoImpresion(String mensaje, int R_idImagen, boolean showButton ){

        LayoutInflater factory = LayoutInflater.from(context);
        View statusImpresionView = factory.inflate( R.layout.alertdialog_custom_estatus_impresion_zebra, null);

        final AlertDialog statusDialog = new AlertDialog.Builder(context).create();
        statusDialog.setView(statusImpresionView);

        TextView textViewEstatusImpresioTicket = (TextView)statusImpresionView.findViewById(R.id.textViewEstatusImpresioTicket);
        ImageView imageViewEstatusImpresionTicket = (ImageView)statusImpresionView.findViewById(R.id.imageViewEstatusImpresionTicket);
        Button buttonAceptarEstatusImpresionTicket = (Button)statusImpresionView.findViewById(R.id.buttonAceptarEstatusImpresionTicket);

        buttonAceptarEstatusImpresionTicket.setVisibility(View.GONE);

        if(showButton){
            buttonAceptarEstatusImpresionTicket.setVisibility(View.VISIBLE);
            statusDialog.setCancelable(true);
        }else{
            statusDialog.setCancelable(false);
        }

        buttonAceptarEstatusImpresionTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                statusDialog.dismiss();

            }
        });

        textViewEstatusImpresioTicket.setText(mensaje);
        imageViewEstatusImpresionTicket.setBackgroundResource(R_idImagen);

        statusDialog.show();

        return statusDialog;

    }

    private void mostrarMensajesLive(final String mensaje, final int R_idImagen, final boolean showButton, final boolean isError){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(isError){

                    if(alertDialogError != null && alertDialogError.isShowing()){
                        alertDialogError.dismiss();
                    }

                    alertDialogError = monstrarMensajesEstatusProcesoImpresion(mensaje, R_idImagen, showButton);

                }else {

                    if(alertDialogEstatus != null && alertDialogEstatus.isShowing()){
                        alertDialogEstatus.dismiss();
                    }
                    alertDialogEstatus = monstrarMensajesEstatusProcesoImpresion(mensaje, R_idImagen, showButton);

                }

            }
        });

    }

    private void actualizarAlertDialogEstatusImpresionDescripcion(String mensaje){

        if(alertDialogEstatus != null && alertDialogEstatus.isShowing()){
            //Solo actualiza el dialogo de mensajes, NO el dialogo de ERROR
            alertDialogEstatus.dismiss();
        }

        mostrarMensajesLive(mensaje, R.drawable.printer_connected_icon, false, false);

    }

    //dialogo para enviar a confiuraciones para activar el bluetooth
    public static void showSettingsAlert(final Context context, final Activity activity){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

                alertDialog.setTitle("Configuracion de Bluetooth");

                alertDialog.setMessage("Bluetooth no esta habilitado. Desea ir a configuraciones y activarlo?");

                alertDialog.setPositiveButton("Ir a Configuración", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    }
                });

                // On pressing the cancel button
                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // Showing Alert Message
                alertDialog.show();

            }
        });

    }

}
