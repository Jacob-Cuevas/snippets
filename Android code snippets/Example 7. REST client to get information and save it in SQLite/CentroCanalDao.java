package com.example.metodos1.puntodeventadetalle.Daos;

import com.example.metodos1.puntodeventadetalle.Dominio.CentroCanal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCuevas on 16/11/2016.
 */
public class CentroCanalDao {

    public CentroCanalDao(){}

    public void insertarCentroCanalDao(String idCentro,String descripcion,String idCanal){

        CentroCanal centroCanal = new CentroCanal(idCentro, descripcion, idCanal);
        centroCanal.save();

    }

    public List<CentroCanal> getTodosCentroCanal(){

       List<CentroCanal> listado = CentroCanal.listAll(CentroCanal.class);
        return listado;

    }

    public List<CentroCanal> getCentroCanalPoridCentro(String idCentro){

        List<CentroCanal> listado = CentroCanal.find(CentroCanal.class, " id_centro = ? ", idCentro );

        return listado;

    }

    public List<CentroCanal> getCentroCanalPorIdCanal(String idCanal){

        List<CentroCanal> listado = CentroCanal.find(CentroCanal.class, " id_canal = ? ", idCanal  );
        return listado;

    }

    public CentroCanal getCentroCanalPorId(String idCentro){

        CentroCanal centroCanal = null;

        List<CentroCanal> listado = CentroCanal.find(CentroCanal.class, " id_centro = ? ", idCentro);
        if(listado.size() > 0){
            centroCanal = listado.get(0);
        }

        return centroCanal;
    }

    public boolean deleteAllCentroCanal(){

        int result = CentroCanal.deleteAll(CentroCanal.class);
        if(result > 0){
            return true;
        }
        return false;
    }

}
