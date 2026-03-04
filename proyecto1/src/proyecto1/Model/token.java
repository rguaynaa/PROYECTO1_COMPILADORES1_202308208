/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1.model;

/**
 *
 * @author rodrigo
 */
public class token {
   private int numero;
   private String lexema;
   private String tipo;
   private int linea;
   private int columna;
   
   public token(int numero, String lexema,String tipo, int linea,int columna){
       this.numero=numero;
       this.lexema=lexema;
       this.tipo=tipo;
       this.linea=linea;
       this.columna=columna;
   }
   
    public int getNumero() { return numero; }
    public String getLexema() { return lexema; }
    public String getTipo() { return tipo; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
   
}
