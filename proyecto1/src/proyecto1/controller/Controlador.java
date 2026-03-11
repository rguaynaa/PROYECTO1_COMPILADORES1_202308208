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
    
    //variables para el export
    private String ultimaTablaLeida = null;
    private List<String> ultimosCampos = new ArrayList<>();
    
    //persistencia de datos
    public void guardarPersistencia(){
        if (dbActiva==null) return;
        
        try{
            StringBuilder json=new StringBuilder();
            json.append("{\n");
            json.append("  \"database\": \"").append(dbActiva.getNombre()).append("\",\n");
            json.append("  \"tables\": {\n");
            
            List<String> nombreTablas= new ArrayList<>(dbActiva.getTablas().keySet());
            
            for(int t=0; t<nombreTablas.size();t++){
                String nombreTabla=nombreTablas.get(t);
                Tabla tabla=dbActiva.getTabla(nombreTabla);
                
                json.append("     \"").append(nombreTabla).append("\": {\n");
                
                //schema
                json.append("      \"schema\": {\n");
                List<String> campos=new ArrayList<>(tabla.getSchema().keySet());
                
                for(int i=0; i<campos.size(); i++){
                    String campo=campos.get(i);
                    String tipo=tabla.getSchema().get(campo);
                    
                    json.append("        \"").append(campo).append("\": \"").append(tipo).append("\"");
                    if (i < campos.size() - 1) json.append(",");
                    json.append("\n");
                }
                json.append("      },\n");
                
                // Records
                json.append("      \"records\": [\n");
                List<Map<String, Object>> registros = tabla.getRegistros();
                for (int r = 0; r < registros.size(); r++) {
                    Map<String, Object> reg = registros.get(r);
                    json.append("        {\n");
                    List<String> keys = new ArrayList<>(reg.keySet());
                    for (int j = 0; j < keys.size(); j++) {
                        String key = keys.get(j);
                        Object val = reg.get(key);
                        json.append("          \"").append(key).append("\": ");
                        if (val instanceof String) {
                            json.append("\"").append(val).append("\"");
                        } else if (val == null) {
                            json.append("null");
                        } else {
                            json.append(val);
                        }
                        if (j < keys.size() - 1) json.append(",");
                        json.append("\n");
                    }
                    json.append("        }");
                    if (r < registros.size() - 1) json.append(",");
                    json.append("\n");
                }
                json.append("      ]\n");
                json.append("    }");
                if (t < nombreTablas.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  }\n");
            json.append("}");

            java.io.FileWriter fw = new java.io.FileWriter(dbActiva.getRuta());
            fw.write(json.toString());
            fw.close();
            consola.append("Base de datos guardada en '" + dbActiva.getRuta() + "'.\n");

        } catch (Exception e) {
            consola.append("Error al guardar persistencia: " + e.getMessage() + "\n");
        }
    }
    
    //cargar la persistencia
    public void cargarPersistencia(String ruta) {
    try {
        java.io.File archivo = new java.io.File(ruta);
   
        if (!archivo.exists()) return;
        
        // Leer archivo
        StringBuilder contenido = new StringBuilder();
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(archivo));
        String linea;
        while ((linea = br.readLine()) != null) {
            contenido.append(linea).append("\n");
        }
        br.close();
        
        String json = contenido.toString().trim();
        
        // Obtener nombre de la database
        String nombreDb = extraerValorString(json, "database");
        if (nombreDb == null) return;
        
        // Crear database si no existe
        if (!databases.containsKey(nombreDb)) {
            Database db = new Database(nombreDb, ruta);
            databases.put(nombreDb, db);
        }
        Database db = databases.get(nombreDb);
        dbActiva = db;
        
        // Extraer tablas
        int tablesStart = json.indexOf("\"tables\"");
        if (tablesStart == -1) return;
        
        int tablesBlock = json.indexOf("{", tablesStart + 8);
        String tablesJson = extraerBloque(json, tablesBlock);
        
        // Parsear cada tabla
        int pos = 0;
        while (pos < tablesJson.length()) {
            int nombreStart = tablesJson.indexOf("\"", pos);
            if (nombreStart == -1) break;
            int nombreEnd = tablesJson.indexOf("\"", nombreStart + 1);
            if (nombreEnd == -1) break;
            String nombreTabla = tablesJson.substring(nombreStart + 1, nombreEnd);
            
            int tablaBlock = tablesJson.indexOf("{", nombreEnd);
            if (tablaBlock == -1) break;
            String tablaJson = extraerBloque(tablesJson, tablaBlock);
            
            // Crear tabla
            Tabla tabla = new Tabla(nombreTabla);
            tablaTemp = tabla;
            db.agregarTabla(nombreTabla, tabla);
            
            // Parsear schema
            int schemaStart = tablaJson.indexOf("\"schema\"");
            if (schemaStart != -1) {
                int schemaBlock = tablaJson.indexOf("{", schemaStart);
                String schemaJson = extraerBloque(tablaJson, schemaBlock);
                parsearSchema(schemaJson, tabla);
            }
            
            // Parsear records
            int recordsStart = tablaJson.indexOf("\"records\"");
            if (recordsStart != -1) {
                int recordsBlock = tablaJson.indexOf("[", recordsStart);
                String recordsJson = extraerArreglo(tablaJson, recordsBlock);
                parsearRecords(recordsJson, tabla);
            }
            
            pos = tablesBlock + tablaJson.length() + 1;
            tablesBlock = tablesJson.indexOf("{", nombreEnd + tablaJson.length());
            if (tablesBlock == -1) break;
            tablaJson = extraerBloque(tablesJson, tablesBlock);
            pos = tablesBlock + tablaJson.length();
        }
        
        consola.append("Base de datos '" + nombreDb + "' cargada desde '" + ruta + "'.\n");
        consola.append("Tablas cargadas:\n");
        for (String nombreTabla : db.getTablas().keySet()) {
            Tabla t = db.getTabla(nombreTabla);
            consola.append("  - " + nombreTabla + 
                " (" + t.getRegistros().size() + " registro(s))\n");
}
        
    } catch (Exception e) {
        consola.append("Error al cargar persistencia: " + e.getMessage() + "\n");
    }
    
}

private void parsearSchema(String schemaJson, Tabla tabla) {
    int pos = 0;
    while (pos < schemaJson.length()) {
        int keyStart = schemaJson.indexOf("\"", pos);
        if (keyStart == -1) break;
        int keyEnd = schemaJson.indexOf("\"", keyStart + 1);
        if (keyEnd == -1) break;
        String campo = schemaJson.substring(keyStart + 1, keyEnd);
        
        int valStart = schemaJson.indexOf("\"", keyEnd + 1);
        if (valStart == -1) break;
        int valEnd = schemaJson.indexOf("\"", valStart + 1);
        if (valEnd == -1) break;
        String tipo = schemaJson.substring(valStart + 1, valEnd);
        
        tabla.agregarCampo(campo, tipo);
        pos = valEnd + 1;
    }
}

private void parsearRecords(String recordsJson, Tabla tabla) {
     int pos = 0;
    while (pos < recordsJson.length()) {
        int regStart = recordsJson.indexOf("{", pos);
        if (regStart == -1) break;
        String regJson = extraerBloque(recordsJson, regStart);

        Map<String, Object> registro = new java.util.LinkedHashMap<>();
        int rpos = 0;
        while (rpos < regJson.length()) {
            // Buscar clave
            int keyStart = regJson.indexOf("\"", rpos);
            if (keyStart == -1) break;
            int keyEnd = regJson.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;
            String key = regJson.substring(keyStart + 1, keyEnd);

            // Buscar valor después de los dos puntos
            int colonPos = regJson.indexOf(":", keyEnd + 1);
            if (colonPos == -1) break;

            // Saltar espacios
            int valStart = colonPos + 1;
            while (valStart < regJson.length() && 
                   regJson.charAt(valStart) == ' ') valStart++;

            Object valor;
            int nextPos;

            if (regJson.charAt(valStart) == '"') {
                // Valor string
                int vEnd = regJson.indexOf("\"", valStart + 1);
                valor = regJson.substring(valStart + 1, vEnd);
                nextPos = vEnd + 1;
            } else if (regJson.startsWith("null", valStart)) {
                valor = null;
                nextPos = valStart + 4;
            } else if (regJson.startsWith("true", valStart)) {
                valor = true;
                nextPos = valStart + 4;
            } else if (regJson.startsWith("false", valStart)) {
                valor = false;
                nextPos = valStart + 5;
            } else {
                // Valor numérico
                int vEnd = valStart;
                while (vEnd < regJson.length() && 
                       (Character.isDigit(regJson.charAt(vEnd)) || 
                        regJson.charAt(vEnd) == '.' || 
                        regJson.charAt(vEnd) == '-')) {
                    vEnd++;
                }
                String numStr = regJson.substring(valStart, vEnd).trim();
                if (numStr.contains(".")) {
                    valor = Double.parseDouble(numStr);
                } else {
                    valor = Integer.parseInt(numStr);
                }
                nextPos = vEnd;
            }

            registro.put(key, valor);
            rpos = nextPos;
        }

        tabla.agregarRegistro(registro);
        pos = regStart + regJson.length() + 2;
    }
}

private String extraerBloque(String texto, int inicio) {
    int nivel = 0;
    int i = inicio;
    while (i < texto.length()) {
        char c = texto.charAt(i);
        if (c == '{') nivel++;
        else if (c == '}') {
            nivel--;
            if (nivel == 0) return texto.substring(inicio + 1, i);
        }
        i++;
    }
    return "";
}

private String extraerArreglo(String texto, int inicio) {
    int nivel = 0;
    int i = inicio;
    while (i < texto.length()) {
        char c = texto.charAt(i);
        if (c == '[') nivel++;
        else if (c == ']') {
            nivel--;
            if (nivel == 0) return texto.substring(inicio + 1, i);
        }
        i++;
    }
    return "";
}

private String extraerValorString(String json, String clave) {
    int idx = json.indexOf("\"" + clave + "\"");
    if (idx == -1) return null;
    int colonIdx = json.indexOf(":", idx);
    int valStart = json.indexOf("\"", colonIdx);
    int valEnd = json.indexOf("\"", valStart + 1);
    return json.substring(valStart + 1, valEnd);
}
    
    
    //DATABASE
    
    public void crearDatabase(String nombre,String ruta){
        
        java.io.File archivo = new java.io.File(ruta);
        if (archivo.exists()) {
            // Cargar datos existentes
            cargarPersistencia(ruta);
            consola.append("Base de datos '" + nombre + "' cargada desde persistencia.\n");
        } else {
            // Crear nueva
            Database db = new Database(nombre, ruta);
            databases.put(nombre, db);
            consola.append("Base de datos '" + nombre + "' creada.\n");
        }
        
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
    
    public void crearTabla(String nombre) {
    if (dbActiva == null) {
        consola.append("Error: no hay base de datos activa.\n");
        return;
    }
    tablaTemp = new Tabla(nombre);
    dbActiva.agregarTabla(nombre, tablaTemp);
    consola.append("Tabla '" + nombre + "' creada\n");
}

    public void agregarCampo(String campo, String tipo) {
    if (tablaTemp != null) {
        tablaTemp.agregarCampo(campo, tipo);
        consola.append("DEBUG campo agregado: " + campo + " tipo: " + tipo + "\n");
    } else {
        consola.append("ERROR: tablaTemp es null al agregar campo " + campo + "\n");
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
        guardarPersistencia(); //metodo para que al ejecutar se guarden los datos
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
        
        ultimaTablaLeida = nombreTabla;
        if (fieldsTemp.contains("*")) {
            ultimosCampos = new ArrayList<>(tabla.getSchema().keySet());
        } else {
            ultimosCampos = new ArrayList<>(fieldsTemp);
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

    try {
        Tabla tabla = dbActiva.getTabla(ultimaTablaLeida);
        List<Map<String, Object>> registros = (List<Map<String, Object>>) ultimoResultado;

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Nombre de la tabla
        json.append("  \"table\": \"").append(ultimaTablaLeida).append("\",\n");
        
        consola.append("DEBUG campos: " + ultimosCampos + "\n");
        consola.append("DEBUG schema: " + tabla.getSchema() + "\n");

        // Fields con tipos correctos del schema
        json.append("  \"fields\": {\n");
        for (int i = 0; i < ultimosCampos.size(); i++) {
            String campo = ultimosCampos.get(i);
            String tipo = tabla.getSchema().get(campo);
            if (tipo == null) tipo = "string";
            json.append("    \"").append(campo).append("\": \"").append(tipo).append("\"");
            if (i < ultimosCampos.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  },\n");

        // Records
        json.append("  \"records\": [\n");
        for (int i = 0; i < registros.size(); i++) {
            Map<String, Object> reg = registros.get(i);
            json.append("    {\n");
            List<String> keys = new ArrayList<>(reg.keySet());
            for (int j = 0; j < keys.size(); j++) {
                String key = keys.get(j);
                Object val = reg.get(key);
                json.append("      \"").append(key).append("\": ");
                if (val instanceof String) {
                    json.append("\"").append(val).append("\"");
                } else if (val == null) {
                    json.append("null");
                } else {
                    json.append(val);
                }
                if (j < keys.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("    }");
            if (i < registros.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");

        // Escribir archivo
        java.io.FileWriter fw = new java.io.FileWriter(archivo);
        fw.write(json.toString());
        fw.close();
        consola.append("Exportado correctamente a '" + archivo + "'.\n");

    } catch (Exception e) {
        consola.append("Error al exportar: " + e.getMessage() + "\n");
    }
            
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
    

