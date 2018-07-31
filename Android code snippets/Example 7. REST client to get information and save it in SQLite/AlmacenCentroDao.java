package com.example.metodos1.puntodeventadetalle.Daos;

import com.example.metodos1.puntodeventadetalle.Dominio.AlmacenCentro;

import java.util.List;

/**
 * Created by JCuevas on 16/11/2016.
 */
public class AlmacenCentroDao {

    public AlmacenCentroDao(){}

    public void insertarAlmacenCentroDao(String idAlmacen,String descripcion,String idCentro){

        AlmacenCentro almacenCentro = new AlmacenCentro(idAlmacen, descripcion, idCentro);
        almacenCentro.save();

    }

    public List<AlmacenCentro> getTodosAlmacenCentro(){

        List<AlmacenCentro> listado = AlmacenCentro.listAll(AlmacenCentro.class);
        return listado;

    }

    public List<AlmacenCentro> getAlmacenCentroPorIdCentro(String idCentro){

        List<AlmacenCentro> listado = AlmacenCentro.find(AlmacenCentro.class, " id_centro = ? ", idCentro );
        return listado;

    }

    public AlmacenCentro getAlmacenCentroPorId(String idAlmacen){

        AlmacenCentro almacenCentro = null;
        List<AlmacenCentro> listado = AlmacenCentro.find(AlmacenCentro.class, " id_almacen = ? ", idAlmacen);
        if(listado.size() > 0){
            almacenCentro = listado.get(0);
        }

        return almacenCentro;

    }

    public boolean deleteAllAlmacenCentro(){

        int result = AlmacenCentro.deleteAll(AlmacenCentro.class);
        if(result > 0){
            return true;
        }
        return false;

    }

}
