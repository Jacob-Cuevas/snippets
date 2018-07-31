package xtremebodygym.app.jack.login;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import xtremebodygym.app.jack.login.dominio.Globals;
import xtremebodygym.app.jack.login.dominio.ProgresoCliente;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCuevas on 07/05/2017.
 */
public class VerProgresoGraficas extends Fragment {

    private ProgresoCliente progresoCliente;
    private TextView tvindiceActual;
    private TextView tv_resultadoImc;
    private TextView tv_actualgrasacorporal;
    private TextView tv_grasacorpesencial;
    private TextView tv_tipograsacorp;
    private TextView tv_valormasamagra;
    private DataBaseManager manager;

    private LinearLayout linearlayoutindicemasacorp;
    private LinearLayout linearlayoutgrasacorporal;
    private LinearLayout linearlayoutmasamagra;
    private LinearLayout linearlayoutgraficabarras;
    private LinearLayout linearlayoutsindatosprogreso;

    public VerProgresoGraficas(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.l_ver_progreso_graficas, container, false);
        progresoCliente = Globals.manager.buscarProgresoCliente(Globals.cliente.getClaveCliente());

        linearlayoutindicemasacorp = (LinearLayout)rootView.findViewById(R.id.linearlayoutindicemasacorp);
        linearlayoutgrasacorporal = (LinearLayout)rootView.findViewById(R.id.linearlayoutgrasacorporal);
        linearlayoutmasamagra = (LinearLayout)rootView.findViewById(R.id.linearlayoutmasamagra);
        linearlayoutgraficabarras = (LinearLayout)rootView.findViewById(R.id.linearlayoutgraficabarras);
        linearlayoutsindatosprogreso = (LinearLayout)rootView.findViewById(R.id.linearlayoutsindatosprogreso);

        if(progresoCliente.getIdavance() != 0 ){

            linearlayoutindicemasacorp.setVisibility(View.VISIBLE);
            linearlayoutgrasacorporal.setVisibility(View.VISIBLE);
            linearlayoutmasamagra.setVisibility(View.VISIBLE);
            linearlayoutgraficabarras.setVisibility(View.VISIBLE);

            linearlayoutsindatosprogreso.setVisibility(View.GONE);

            double valIMC = getPorcentajeIndiceMasaCorporal();
            tvindiceActual = (TextView) rootView.findViewById(R.id.tv_indiceActual);
            tvindiceActual.setText(String.valueOf(truncateDecimal(valIMC,2)));

            tv_resultadoImc = (TextView)rootView.findViewById(R.id.tv_resultadoImc);
            String resultIMC = "";
            if(valIMC < 18){
                resultIMC = "Bajo";
            } else
            if(valIMC > 18 && valIMC <= 24.9){
                resultIMC = "Normal";
            } else
            if(valIMC > 24.9 && valIMC <= 26.9 ){
                resultIMC = "Sobrepeso";
            } else{
                resultIMC = "Obesidad";
            }
            tv_resultadoImc.setText(resultIMC);

            tv_actualgrasacorporal = (TextView)rootView.findViewById(R.id.tv_actualgrasacorporal);
            double actgrasa = getPorcentajeGrasaCorporal();
            tv_actualgrasacorporal.setText(String.valueOf(truncateDecimal(actgrasa,2)));

            tv_grasacorpesencial = (TextView)rootView.findViewById(R.id.tv_grasacorpesencial);
            String valoresencial = "";
            if(Globals.cliente.getSexo().equals("H")){
                valoresencial = "2 - 4%";
            } else {
                valoresencial = "10 - 12%";
            }
            tv_grasacorpesencial.setText(valoresencial);

            tv_tipograsacorp = (TextView)rootView.findViewById(R.id.tv_tipograsacorp);
            String descriptipograsa = "";
            if(Globals.cliente.getSexo().equals("H")){
                if(actgrasa <= 4){
                    descriptipograsa = "Esencial";
                } else
                if(actgrasa > 4 && actgrasa <= 13){
                    descriptipograsa = "Atleta";
                } else
                if(actgrasa > 13 && actgrasa <= 17){
                    descriptipograsa = "Fitness";
                } else
                if(actgrasa > 17 && actgrasa <= 25){
                    descriptipograsa = "Aceptable";
                } else {
                    descriptipograsa = "Obesidad";
                }
            } else {
                if(actgrasa <= 12){
                    descriptipograsa = "Esencial";
                } else
                if(actgrasa > 12 && actgrasa <= 20){
                    descriptipograsa = "Atleta";
                } else
                if(actgrasa > 20 && actgrasa <= 24){
                    descriptipograsa = "Fitness";
                } else
                if(actgrasa > 24 && actgrasa <= 31){
                    descriptipograsa = "Aceptable";
                } else {
                    descriptipograsa = "Obesidad";
                }
            }
            tv_tipograsacorp.setText(descriptipograsa);

            tv_valormasamagra = (TextView)rootView.findViewById(R.id.tv_valormasamagra);
            tv_valormasamagra.setText(String.valueOf(truncateDecimal(getMasaMagra(),2)));

 		    /*creamos una lista de colores*/
            ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(getResources().getColor(R.color.red_flat));
            colors.add(getResources().getColor(R.color.green_flat));

            //Init BarChart
            BarChart barChart = (BarChart) rootView.findViewById(R.id.barchart);
            Legend lbar = barChart.getLegend();
            lbar.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            BarData barData = new BarData(getXAxisValues(), getDataSet());
            barChart.setDrawValuesForWholeStack(true);
            barChart.setData(barData);
            barChart.zoom(2.4f, 0, 0, 0);
            barChart.setHorizontalScrollBarEnabled(true);
            barChart.setDrawValueAboveBar(true);
            barChart.setDescription("");
            barChart.animateX(1000, Easing.EasingOption.EaseInOutQuad);
            barChart.animateXY(1500, 1500);
            /*barChart.setDrawHighlightArrow(true);
            barChart.setHighlightEnabled(true);*/
            barChart.invalidate();

        } else {
            //Sin progreso aun
            linearlayoutindicemasacorp.setVisibility(View.GONE);
            linearlayoutgrasacorporal.setVisibility(View.GONE);
            linearlayoutmasamagra.setVisibility(View.GONE);
            linearlayoutgraficabarras.setVisibility(View.GONE);

            linearlayoutsindatosprogreso.setVisibility(View.VISIBLE);

        }

        return rootView;
    }

    private ArrayList<BarDataSet> getDataSet() {
        ArrayList<BarDataSet> dataSets = null;

        //Se recupera el listado de Historial progreso
        manager = Globals.manager;
        List<ProgresoCliente> listado = manager.buscarListadoProgresoPorClaveCliente(Globals.cliente.getClaveCliente());

        //Progreso de hace 10 registros Atras
        ProgresoCliente histProgCliente = listado.get(listado.size()-1);

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry((float) histProgCliente.getPeso(), 0); // Jan
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry((float) histProgCliente.getCuello(), 1); // Feb
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry((float) histProgCliente.getBrazo(), 2); // Mar
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry((float) histProgCliente.getAbdomen(), 3); // Apr
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry((float) histProgCliente.getPierna(), 4); // May
        valueSet1.add(v1e5);
        if(Globals.cliente.getSexo().equals("H")){
            BarEntry v1e6 = new BarEntry((float) histProgCliente.getCintura(), 5); // Jun
            valueSet1.add(v1e6);
            BarEntry v1e7 = new BarEntry((float) histProgCliente.getPecho(), 6); // Jun
            valueSet1.add(v1e7);
            BarEntry v1e8 = new BarEntry((float) histProgCliente.getPantorrilla(), 7); // Jun
            valueSet1.add(v1e8);
        } else {
            BarEntry v1e6 = new BarEntry((float) histProgCliente.getGluteos(), 5); // Jun
            valueSet1.add(v1e6);
            BarEntry v1e7 = new BarEntry((float) histProgCliente.getCadera(), 6); // Jun
            valueSet1.add(v1e7);
            BarEntry v1e8 = new BarEntry((float) histProgCliente.getBusto(), 7); // Jun
            valueSet1.add(v1e8);
        }
        //Progreso Actual
        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        BarEntry v2e1 = new BarEntry((float) progresoCliente.getPeso(), 0); // Jan
        valueSet2.add(v2e1);
        BarEntry v2e2 = new BarEntry((float) progresoCliente.getCuello(), 1); // Feb
        valueSet2.add(v2e2);
        BarEntry v2e3 = new BarEntry((float) progresoCliente.getBrazo(), 2); // Mar
        valueSet2.add(v2e3);
        BarEntry v2e4 = new BarEntry((float) progresoCliente.getAbdomen(), 3); // Apr
        valueSet2.add(v2e4);
        BarEntry v2e5 = new BarEntry((float) progresoCliente.getPierna(), 4); // May
        valueSet2.add(v2e5);
        if(Globals.cliente.getSexo().equals("H")){
            BarEntry v2e6 = new BarEntry((float) progresoCliente.getCintura(), 5); // Jun
            valueSet2.add(v2e6);
            BarEntry v2e7 = new BarEntry((float) progresoCliente.getPecho(), 6); // Jun
            valueSet2.add(v2e7);
            BarEntry v2e8 = new BarEntry((float) progresoCliente.getPantorrilla(), 7); // Jun
            valueSet2.add(v2e8);
        } else {
            BarEntry v2e6 = new BarEntry((float) progresoCliente.getGluteos(), 5); // Jun
            valueSet2.add(v2e6);
            BarEntry v2e7 = new BarEntry((float) progresoCliente.getCadera(), 6); // Jun
            valueSet2.add(v2e7);
            BarEntry v2e8 = new BarEntry((float) progresoCliente.getBusto(), 7); // Jun
            valueSet2.add(v2e8);
        }

        //Crear cada BarDataSet
        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Medidas " + histProgCliente.getFecha().split(" ")[0]);
        barDataSet1.setColor(Color.rgb(12,203,49));
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Medidas " + progresoCliente.getFecha().split(" ")[0]);
        barDataSet2.setColor(Color.rgb(253,146,38));

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("Peso");
        xAxis.add("Cuello");
        xAxis.add("Bicep");
        xAxis.add("Abdomen");
        xAxis.add("Pierna");
        if(Globals.cliente.getSexo().equals("H")){
            xAxis.add("Cintura");
            xAxis.add("Pecho");
            xAxis.add("Pantorrilla");
        }else {
            xAxis.add("Gluteos");
            xAxis.add("Cadera");
            xAxis.add("Busto");
        }
        return xAxis;
    }

    /**
     * Metodo para calcular el índice de masa corporal
     * @return
     */
    private double getPorcentajeIndiceMasaCorporal(){
        double altura = progresoCliente.getAltura()/100;
        return progresoCliente.getPeso()/(Math.pow(altura, 2));
    }

    /**
     * Metodo para calcular el porcentaje de grasa corporal
     * @return
     */
    private double getPorcentajeGrasaCorporal(){
        if(Globals.cliente.getSexo().equals("H")){
            //%Grasa=495/(1.0324-0.19077(log(cintura-cuello))+0.15456(log(altura)))-450
            return 495 / (1.0324 - 0.19077 * (Math.log10(progresoCliente.getCintura() - progresoCliente.getCuello())) + 0.15456 * (Math.log10(progresoCliente.getAltura()))) - 450;
        } else {
            //%Grasa=495/(1.29579-0.35004(log(cintura+cadera-cuello))+0.22100(log(altura)))-450
            return 495 / (1.29579 - 0.35004 * (Math.log10(progresoCliente.getCintura() + progresoCliente.getCadera() - progresoCliente.getCuello())) + 0.22100 * (Math.log10(progresoCliente.getAltura()))) - 450;
        }
    }

    /**
     * Metodo para obtener la cantidad de masa magra
     * @return
     */
    private double getMasaMagra(){
        return progresoCliente.getPeso() * ( 100 - getPorcentajeGrasaCorporal() );
    }

    /**
     * Metodo para realizar el truncamiento de decimales
     * @param x
     * @param numberofDecimals
     * @return
     */
    private static BigDecimal truncateDecimal(double x,int numberofDecimals){
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }

}