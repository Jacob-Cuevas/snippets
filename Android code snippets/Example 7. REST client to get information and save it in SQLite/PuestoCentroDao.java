package com.example.metodos1.puntodeventadetalle.Daos;

import com.example.metodos1.puntodeventadetalle.Dominio.PuestoCentro;

import java.util.List;

/**
 * Created by JCuevas on 16/11/2016.
 */
public class PuestoCentroDao {

    public PuestoCentroDao(){}

    public void insertarPuestoCentro(String idPuesto,String descripcion,String idCentro){

        PuestoCentro puestoCentro = new PuestoCentro(idPuesto, descripcion, idCentro);
        puestoCentro.save();

    }

    public List<PuestoCentro> getTodosPuestoCentro(){

        List<PuestoCentro> listado = PuestoCentro.listAll(PuestoCentro.class);
        return listado;

    }

    public List<PuestoCentro> getPuestoCentroPorIdCentro(String idCentro){

        List<PuestoCentro> listado = PuestoCentro.find(PuestoCentro.class, " id_centro = ? ", idCentro );
        return listado;

    }

    public PuestoCentro getPuestoCentroPorId(String idPuesto){

        PuestoCentro puestoCentro = null;
        List<PuestoCentro> listado = PuestoCentro.find(PuestoCentro.class, " id_puesto = ? ", idPuesto);
        if(listado.size() > 0){
            puestoCentro = listado.get(0);
        }

        return puestoCentro;
    }

    public boolean deleteAllPuestoCentro(){

        int result = PuestoCentro.deleteAll(PuestoCentro.class);
        if(result > 0){
            return true;
        }
        return false;
    }

}
