/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rodrigo
 */
public class Tabla {
    private String nombre;
    private Map<String , String> schema=new LinkedHashMap<>();
    
   private List<Map<String,Object>> registros = new ArrayList();
   
   public Tabla(String nombre){
       this.nombre=nombre;
   }
   
   public void agregarCampo(String campo, String tipo){
       schema.put(campo,tipo);
       
   }
   public void agregarRegistro(Map<String, Object> registro){
       registros.add(registro);
   }
   public void limpiar(){
       registros.clear();
   }
   
   //getters
   
   public String getNombre(){
       return nombre;
     
   }
   
   public Map<String , String> getSchema(){
       return schema;
   }
   public List<Map<String, Object>> getRegistros(){
       return registros;
   }
}
