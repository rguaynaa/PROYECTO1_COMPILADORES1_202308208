/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import proyecto1.model.Database;
import proyecto1.model.Tabla;

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
    
    //ADD
    
    public void agregarValorAdd(String campo, Object valor) {
    valoresAddTemp.put(campo, valor);
    }
    
    public void ejecutarAdd(String nombreTabla){
        if (dbActiva == null) {
            consola.append("Error: no hay base de datos activa.\n");
            return;
        }
        Tabla tabla = dbActiva.getTabla(nombreTabla);
        if (tabla == null) {
            consola.append("Error: tabla '" + nombreTabla + "' no existe.\n");
            return;
        }
        tabla.agregarRegistro(new HashMap<>(valoresAddTemp));
        consola.append("Registro agregado a '" + nombreTabla + "'.\n");
        valoresAddTemp.clear();
    }

    //READ
    
    public void setFieldsTodos() {
        fieldsTemp.clear();
        fieldsTemp.add("*");
    }

    public void agregarField(String campo) {
        fieldsTemp.add(campo);
    }

    public void setFiltro(String filtro) {
        this.filtroTemp = filtro;
    }

    public void ejecutarRead(String nombreTabla) {
        if (dbActiva == null) {
            consola.append("Error: no hay base de datos activa.\n");
            return;
        }
        Tabla tabla = dbActiva.getTabla(nombreTabla);
        if (tabla == null) {
            consola.append("Error: tabla '" + nombreTabla + "' no existe.\n");
            return;
        }

        List<Map<String, Object>> registros = tabla.getRegistros();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, Object> reg : registros) {
            if (filtroTemp == null || evaluarFiltro(reg, filtroTemp)) {
                if (fieldsTemp.contains("*")) {
                    resultado.add(reg);
                } else {
                    Map<String, Object> fila = new HashMap<>();
                    for (String f : fieldsTemp) {
                        fila.put(f, reg.get(f));
                    }
                    resultado.add(fila);
                }
            }
        }

        ultimoResultado = resultado;
        mostrarResultado(nombreTabla, resultado);
        fieldsTemp.clear();
        filtroTemp = null;
    }

    private void mostrarResultado(String tabla, List<Map<String, Object>> resultado) {
        consola.append("── Resultado de READ en '" + tabla + "' ──\n");
        for (Map<String, Object> fila : resultado) {
            consola.append(fila.toString() + "\n");
        }
        consola.append("Total: " + resultado.size() + " registro(s).\n");
    }

    // ────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────
    public void agregarItemSet(String campo, Object valor) {
        itemsSetTemp.put(campo, valor);
    }

    public void ejecutarUpdate(String nombreTabla) {
        if (dbActiva == null) {
            consola.append("Error: no hay base de datos activa.\n");
            return;
        }
        Tabla tabla = dbActiva.getTabla(nombreTabla);
        if (tabla == null) {
            consola.append("Error: tabla '" + nombreTabla + "' no existe.\n");
            return;
        }

        int count = 0;
        for (Map<String, Object> reg : tabla.getRegistros()) {
            if (filtroTemp == null || evaluarFiltro(reg, filtroTemp)) {
                reg.putAll(itemsSetTemp);
                count++;
            }
        }
        consola.append("Update: " + count + " registro(s) modificado(s) en '" + nombreTabla + "'.\n");
        itemsSetTemp.clear();
        filtroTemp = null;
    }

    // ────────────────────────────────────────────
    //  CLEAR
    // ────────────────────────────────────────────
    public void ejecutarClear(String nombreTabla) {
        if (dbActiva == null) {
            consola.append("Error: no hay base de datos activa.\n");
            return;
        }
        Tabla tabla = dbActiva.getTabla(nombreTabla);
        if (tabla == null) {
            consola.append("Error: tabla '" + nombreTabla + "' no existe.\n");
            return;
        }
        tabla.limpiar();
        consola.append("Tabla '" + nombreTabla + "' limpiada.\n");
    }

    // ────────────────────────────────────────────
    //  EXPORT
    // ────────────────────────────────────────────
    public void ejecutarExport(String archivo) {
        if (ultimoResultado == null) {
            consola.append("Error: no hay resultado previo para exportar.\n");
            return;
        }
        consola.append("Exportando a '" + archivo + "'...\n");
        // La lógica de escritura JSON la conectamos después
    }

    // ────────────────────────────────────────────
    //  ARRAY TEMPORAL
    // ────────────────────────────────────────────
    public void agregarElementoArray(Object val) {
        arrayTemp.add(val);
    }

    public List<Object> getArrayTemp() {
        List<Object> copia = new ArrayList<>(arrayTemp);
        arrayTemp.clear();
        return copia;
    }

    // ────────────────────────────────────────────
    //  EVALUADOR DE FILTROS
    // ────────────────────────────────────────────
    private boolean evaluarFiltro(Map<String, Object> registro, String filtro) {
        filtro = filtro.trim();

        // OR
        int idx = indexOfOperator(filtro, "||");
        if (idx != -1) {
            return evaluarFiltro(registro, filtro.substring(0, idx)) ||
                   evaluarFiltro(registro, filtro.substring(idx + 2));
        }
        // AND
        idx = indexOfOperator(filtro, "&&");
        if (idx != -1) {
            return evaluarFiltro(registro, filtro.substring(0, idx)) &&
                   evaluarFiltro(registro, filtro.substring(idx + 2));
        }
        // NOT
        if (filtro.startsWith("!")) {
            return !evaluarFiltro(registro, filtro.substring(1));
        }
        // Paréntesis
        if (filtro.startsWith("(") && filtro.endsWith(")")) {
            return evaluarFiltro(registro, filtro.substring(1, filtro.length() - 1));
        }

        // Operadores relacionales
        String[][] ops = {{">=",">="},{"<=","<="},{"==","=="},{"!=","!="},
                          {">",">"}, {"<","<"}};
        for (String[] op : ops) {
            idx = filtro.indexOf(op[0]);
            if (idx != -1) {
                String campo = filtro.substring(0, idx).trim();
                String valorStr = filtro.substring(idx + op[0].length()).trim();
                Object valReg = registro.get(campo);
                return compararValores(valReg, op[1], valorStr);
            }
        }
        return false;
    }

    private boolean compararValores(Object valReg, String op, String valorStr) {
        if (valReg == null) return false;
        try {
            if (valReg instanceof Integer) {
                int a = (Integer) valReg;
                int b = Integer.parseInt(valorStr);
                switch (op) {
                    case "==": return a == b;
                    case "!=": return a != b;
                    case ">":  return a > b;
                    case "<":  return a < b;
                    case ">=": return a >= b;
                    case "<=": return a <= b;
                }
            } else if (valReg instanceof Double) {
                double a = (Double) valReg;
                double b = Double.parseDouble(valorStr);
                switch (op) {
                    case "==": return a == b;
                    case "!=": return a != b;
                    case ">":  return a > b;
                    case "<":  return a < b;
                    case ">=": return a >= b;
                    case "<=": return a <= b;
                }
            } else if (valReg instanceof String) {
                String a = (String) valReg;
                String b = valorStr.replace("\"", "");
                switch (op) {
                    case "==": return a.equals(b);
                    case "!=": return !a.equals(b);
                }
            } else if (valReg instanceof Boolean) {
                boolean a = (Boolean) valReg;
                boolean b = Boolean.parseBoolean(valorStr);
                switch (op) {
                    case "==": return a == b;
                    case "!=": return a != b;
                }
            }
        } catch (Exception e) { }
        return false;
    }

    private int indexOfOperator(String expr, String op) {
        int parens = 0;
        for (int i = 0; i < expr.length() - op.length() + 1; i++) {
            char c = expr.charAt(i);
            if (c == '(') parens++;
            else if (c == ')') parens--;
            else if (parens == 0 && expr.startsWith(op, i)) return i;
        }
        return -1;
    }

    // ────────────────────────────────────────────
    //  GETTERS PARA LA UI
    // ────────────────────────────────────────────
    public String getConsola() { return consola.toString(); }
    public String getReporte() { return reporte.toString(); }
    public void limpiarConsola() { consola = new StringBuilder(); }
    public void limpiarReporte() { reporte = new StringBuilder(); }
}
    

