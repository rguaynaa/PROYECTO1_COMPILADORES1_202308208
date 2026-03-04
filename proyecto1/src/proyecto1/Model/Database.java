/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rodrigo
 */
public class Database {
    private String nombre;
    private String ruta;
    private Map<String,Tabla>tablas = new HashMap<>();
    
    public Database(String nombre, String ruta){
        this.nombre=nombre;
        this.ruta=ruta;
        
        
    }
    
    //metodo que agrega la tabla
    public void agregarTabla(String nombre,Tabla tabla){
        tablas.put(nombre,tabla);
    }
    
    //getters
    
    public Tabla getTabla(String nombre){
        return tablas.get(nombre);
    }
    
   public String getNombre(){
       return nombre;
       
   }
   
   public String getRuta(){
       return ruta;
   }
   
   public Map<String,Tabla> getTablas(){
       return tablas;
   }
}
