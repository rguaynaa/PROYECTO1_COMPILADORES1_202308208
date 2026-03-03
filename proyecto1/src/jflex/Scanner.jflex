package proyecto1;

import java_cup.runtime.Symbol;

%%

%class Scanner
%unicode
%cup
%line
%column
%public

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

/* ── Definiciones ── */
Letra           = [a-zA-Z_]
Digito          = [0-9]
Identificador   = {Letra}({Letra}|{Digito})*
Entero          = {Digito}+
Decimal         = {Digito}+"."{Digito}+
Cadena          = \"([^\"\n\\]|\\.)*\"
WhiteSpace      = [ \t\r\n]+

%%

/* ── Comentarios ── */
"##"[^\n]*                          { /* comentario de línea, ignorar */ }
"#*"([^#]|"#"[^*])*"#*"           { /* comentario multilínea, ignorar */ }

/* ── Palabras reservadas ── */
"database"      { return symbol(sym.DATABASE); }
"use"           { return symbol(sym.USE); }
"table"         { return symbol(sym.TABLE); }
"read"          { return symbol(sym.READ); }
"add"           { return symbol(sym.ADD); }
"update"        { return symbol(sym.UPDATE); }
"clear"         { return symbol(sym.CLEAR); }
"export"        { return symbol(sym.EXPORT); }
"store"         { return symbol(sym.STORE); }
"at"            { return symbol(sym.AT); }
"fields"        { return symbol(sym.FIELDS); }
"filter"        { return symbol(sym.FILTER); }
"set"           { return symbol(sym.SET); }

/* ── Tipos de datos ── */
"int"           { return symbol(sym.INT); }
"float"         { return symbol(sym.FLOAT); }
"bool"          { return symbol(sym.BOOL); }
"string"        { return symbol(sym.STRING); }
"array"         { return symbol(sym.ARRAY); }
"object"        { return symbol(sym.OBJECT); }
"null"          { return symbol(sym.NULL); }

/* ── Literales booleanos ── */
"true"          { return symbol(sym.TRUE, true); }
"false"         { return symbol(sym.FALSE, false); }

/* ── Operadores relacionales ── */
"=="            { return symbol(sym.IGUAL); }
"!="            { return symbol(sym.DIFERENTE); }
">="            { return symbol(sym.MAYORIGUAL); }
"<="            { return symbol(sym.MENORIGUAL); }
">"             { return symbol(sym.MAYOR); }
"<"             { return symbol(sym.MENOR); }

/* ── Operadores lógicos ── */
"&&"            { return symbol(sym.AND); }
"||"            { return symbol(sym.OR); }
"!"             { return symbol(sym.NOT); }

/* Operador de asignacion */
"="             { return symbol(sym.ASIGNACION); }

/* ── Signos de agrupación ── */
"{"             { return symbol(sym.ABRELLAVE); }
"}"             { return symbol(sym.CIERRALLAVE); }
"("             { return symbol(sym.ABREPAREN); }
")"             { return symbol(sym.CIERRAPAREN); }
"["             { return symbol(sym.ABRECORCHETE); }
"]"             { return symbol(sym.CIERRACORCHETE); }

/* ── Puntuación ── */
";"             { return symbol(sym.PUNTOYCOMA); }
":"             { return symbol(sym.DOSPUNTOS); }
","             { return symbol(sym.COMA); }
"."             { return symbol(sym.PUNTO); }
"*"             { return symbol(sym.ASTERISCO); }

/* ── Literales numéricos ── */
{Decimal}       { return symbol(sym.DECIMAL, Double.parseDouble(yytext())); }
{Entero}        { return symbol(sym.ENTERO, Integer.parseInt(yytext())); }

/* ── Cadenas ── */
{Cadena}        { return symbol(sym.CADENA, yytext().substring(1, yytext().length()-1)); }

/* ── Identificadores ── */
{Identificador} { return symbol(sym.ID, yytext()); }

/* ── Espacios en blanco ── */
{WhiteSpace}    { /* ignorar */ }

/* ── Error léxico ── */
.               { 
                    System.err.println("Error léxico: carácter '" + yytext() + 
                    "' en línea " + (yyline+1) + ", columna " + (yycolumn+1));
                }