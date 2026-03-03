/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import proyecto1.Model.Database;
import proyecto1.Model.Tabla;

/**
 *
 * @author rodrigo
 */
public class Controlador {
    
    private static Controlador instance;
    public static Controlador getInstance(){
        if (instance == null) instance = new Controlador();
        return instance;
    }
    
    
    //atraemos los atributos del model
    
    private Database dbActiva= null;
    private Tabla tablaTemp=null;
    private Map<String, Database> databases=new HashMap<>();
    private Map<String, Object> valoresAddTemp = new HashMap<>();
    private List<String> fieldsTemp = new ArrayList<>();
    private String filtroTemp = null;
    private Map<String, Object> itemsSetTemp = new HashMap<>();
    private List<Object> arrayTemp = new ArrayList<>();
    private Object ultimoResultado = null;
    
    //salida 
    private StringBuilder consola = new StringBuilder();
    private StringBuilder reporte = new StringBuilder();
    
    //DATABASE
    
    public void crearDatabase(String nombre,String ruta){
        
        Database db=new Database(nombre,ruta);
        databases.put(nombre,db);
        consola.append("base de datos  "+nombre + "  se creo correctamente");
        
    }
    
    public void usarDatabase(String nombre){
        if (databases.containsKey(nombre)){
            dbActiva=databases.get(nombre);
            consola.append("la base de datos" + nombre + "  en uso");
        }else{
            consola.append("Error en la base de datos o es inexistente");
        }
    }
    
    //TABLE
    
    public void crearTabla(String nombre){
        if (dbActiva==null){
            consola.append("la base de datos no esta activa");
            return;
        }
        tablaTemp=new Tabla(nombre);
        dbActiva.agregarTabla(nombre, tablaTemp);
        consola.append("Tabla "+ nombre + " creada");
        
    }
    
    public void agregarCampo(String campo,String tipo){
        if(tablaTemp !=null){
            tablaTemp.agregarCampo(campo, tipo);
        }
    }
    
    
}
