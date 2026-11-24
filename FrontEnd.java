package p2;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FrontEnd {

    // Constructor
    public FrontEnd() {
    }

    // Sustituir caracteres especiales (para HTML)
    private static String sustituirCaractaresEspecialesHTML(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    // Sustituir caracteres especiales (para JSON)
    private String sustituirCaractaresEspecialesJSON(String texto) {

        if (texto == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {

            char c = texto.charAt(i);

            switch (c) {

                case '"':
                    resultado.append("\\\"");
                    break;

                case '\\':
                    resultado.append("\\\\");
                    break;

                case '\b':
                    resultado.append("\\b");
                    break;

                case '\f':
                    resultado.append("\\f");
                    break;

                case '\n':
                    resultado.append("\\n");
                    break;

                case '\r':
                    resultado.append("\\r");
                    break;

                case '\t':
                    resultado.append("\\t");
                    break;

                default:
                    if (c < 0x20) {
                        resultado.append(String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
                    break;
            }
        }
        return resultado.toString();
    }

    // Codifica valores para utilizarlos en los parámetros de una URL
    private static String codificarURL(String texto) {

        if (texto == null) {
            return "";
        }

        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }

    // ====================
    // Generar fichero HTML
    // ====================

    // Fase 0: Pide la pantalla inicial en la que se presenta un mensaje de bienvenida
    // Valor por defecto si no hay parámetro pphase
    // Se mostrará el URL del fichero TVML procesado, la IP del cliente y la información del navegador desde el que se hace la petición
    public static String generarInicio(String urlXML, String ip) {
        
        StringBuilder html = new StringBuilder(); // Crea un StringBuilder para construir el HTML
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        
        // Cabecera HTML
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Cartelera de televisión</title>");
        html.append("<link rel=\"stylesheet\" href=\"p2/p2.css\">"); // Enlaza el fichero CSS para estilos
        html.append("<script src=\"p2/p2.js\"></script>"); // Enlaza el fichero JS para funciones
        html.append("</head>");

        // Cuerpo HTML
        html.append("<body>");
        // Cabecera de la página
        html.append("<header>");
        html.append("<h1>Cartelera de televisión</h1>");
        html.append("</header>");
        // Contenido principal
        html.append("<main>");
        html.append("<h2>Bienvenido</h2>");
        html.append("<p>Navegador: <span id=\"browserInfo\"></span></p>"); // Se mostrará la información del navegador en el elemento con id "browserInfo"
        html.append("<p>");
        html.append("Este servicio permite consultar la programación ");
        html.append("de diferentes canales de televisión.");
        html.append("</p>");
        html.append("<section>");
        html.append("<h3>Información de conexión</h3>");
        html.append("<p><strong>Documento XML:</strong> ");
        html.append("<a href=\"");
        html.append(sustituirCaractaresEspecialesHTML(urlXML));
        html.append("\">");
        html.append(sustituirCaractaresEspecialesHTML(urlXML));
        html.append("</a></p>");
        html.append("<p><strong>Dirección IP del cliente:</strong> ");
        html.append(sustituirCaractaresEspecialesHTML(ip));
        html.append("</p>");
        html.append("</section>");
        html.append("<a class=\"boton\" href=\"?pphase=1\">");
        html.append("Consultar canales");
        html.append("</a>");
        html.append("</main>");
        // Pie de página
        html.append("<footer>");
        html.append("<p>Práctica 2 - Adrián Ameneiros Cazorla</p>");
        html.append("</footer>");
        html.append("</body>");

        html.append("</html>");

        return html.toString(); // Devuelve el HTML generado
    }

    // Fase 1: Se muestra la lista de canales conocidos
    // Estarán ordenados alfabéticamente por el atributo langDefault (y por idC si éste coincide)
    // Se seleccionará uno
    public static String generarListaCanales(ArrayList<Canal> canales) {

        StringBuilder html = new StringBuilder(); // Crea un StringBuilder para construir el HTML
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        
        // Cabecera HTML
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Lista de canales disponibles</title>");
        html.append("<link rel=\"stylesheet\" href=\"p2/p2.css\">"); // Enlaza el fichero CSS para estilos
        html.append("<script src=\"p2/p2.js\"></script>"); // Enlaza el fichero JS para funciones
        html.append("</head>");

        // Cuerpo HTML
        html.append("<body>");
        // Cabecera de la página
        html.append("<header>");
        html.append("<h1>Lista de canales disponibles</h1>");
        html.append("</header>");
        // Contenido principal
        html.append("<main>");
        html.append("<h2>Seleccione un canal:</h2>");
        // Si la lista de canales está vacía o no existe
        if (canales == null || canales.isEmpty()) {
            html.append("<p>No hay canales disponibles.</p>");
        // Si no está vacía y existe
        } else {
            // Lista de canales
            html.append("<ul class=\"lista canales\">");
            for (Canal canal : canales) {
                html.append("<li>");
                html.append("<a href=\"?pphase=2&amp;pidC=");
                html.append(codificarURL(canal.getIdC()));
                html.append("\">");
                html.append(sustituirCaractaresEspecialesHTML(canal.getNombreCanal()));
                html.append("</a>");
                html.append(" (ID:");
                html.append(sustituirCaractaresEspecialesHTML(canal.getIdC()));
                html.append(")");
                html.append(" - Idioma por defecto: ");
                html.append(sustituirCaractaresEspecialesHTML(canal.getDefaultLang()));
                html.append("</li>");
            }
            html.append("</ul>");
        }
        html.append("<button type=\"button\" id=\"backButton\">Volver</button>"); // Botón para volver a la página anterior
        html.append("<a class=\"boton\" href=\"?pphase=0\">Volver al inicio</a>"); // Botón para volver a la página de inicio
        html.append("</main>");
        // Pie de página
        html.append("<footer>");
        html.append("<p>Práctica 2 - Adrián Ameneiros Cazorla</p>");
        html.append("</footer>");
        html.append("</body>");

        html.append("</html>");

        return html.toString();
    }

    // Fase 2: Se muestran los programas del canal seleccionado en la fase 1 (su idC se recibirá en el parámetro pidC)
    // Se mostrará su nombre y sus atributos
    // Estarán ordenados decrecientemente por idP
    // Se seleccionará uno
    public static String generarProgramas(ArrayList<Programa> programas, String idC, String nombreCanal) {

        StringBuilder html = new StringBuilder(); // Crea un StringBuilder para construir el HTML
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        
        // Cabecera HTML
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Programas del canal seleccionado</title>");
        html.append("<link rel=\"stylesheet\" href=\"p2/p2.css\">"); // Enlaza el fichero CSS para estilos
        html.append("<script src=\"p2/p2.js\"></script>"); // Enlaza el fichero JS para funciones
        html.append("</head>");

        // Cuerpo HTML
        html.append("<body>");
        // Cabecera de la página
        html.append("<header>");
        html.append("<h1>Programas del canal seleccionado:</h1>");
        html.append("</header>");
        // Contenido principal
        html.append("<main>");
        html.append("<h2>Canal: ");
        html.append(sustituirCaractaresEspecialesHTML(nombreCanal));
        html.append(" (ID: ");
        html.append(sustituirCaractaresEspecialesHTML(idC));
        html.append(")</h2>");
        // Si la lista de programas está vacía o no existe
        if (programas == null || programas.isEmpty()) {
            html.append("<p>No hay programas disponibles para este canal.</p>");
        // Si no está vacía y existe
        } else {
            // Lista de programas
            html.append("<ul class=\"lista programas\">");
            for (Programa programa : programas) {
                html.append("<li>");
                html.append("<a href=\"?pphase=3&amp;pidC=");
                html.append(codificarURL(idC));
                html.append("&amp;pidP=");
                html.append(codificarURL(programa.getIdP()));
                html.append("\">");
                html.append(sustituirCaractaresEspecialesHTML(programa.getNombrePrograma()));
                html.append("</a>");
                html.append(" (ID: ");
                html.append(sustituirCaractaresEspecialesHTML(programa.getIdP()));
                html.append(")");
                // Si tiene edad mínima (es opcional)
                if (programa.getEdadMinima() != null && !programa.getEdadMinima().isEmpty()) {
                    html.append(" - Edad mínima: ");
                    html.append(sustituirCaractaresEspecialesHTML(programa.getEdadMinima()));
                }
                // Si tiene idiomas disponibles (es opcional)
                if (programa.getLangs() != null && !programa.getLangs().isEmpty()) {
                    html.append(" - Idiomas disponibles: ");
                    html.append(sustituirCaractaresEspecialesHTML(programa.getLangs()));
                }
                html.append("</li>");
            }
            html.append("</ul>");
        }
        html.append("<button type=\"button\" id=\"backButton\">Volver</button>"); // Botón para volver a la página anterior
        html.append("<a class=\"boton\" href=\"?pphase=0\">Volver al inicio</a>"); // Botón para volver a la página de inicio
        html.append("</main>");
        // Pie de página
        html.append("<footer>");
        html.append("<p>Práctica 2 - Adrián Ameneiros Cazorla</p>");
        html.append("</footer>");
        html.append("</body>");

        html.append("</html>");

        return html.toString();
    }

    // Fase 3: Se muestra la lista de participantes en el programa seleccionado en la fase 2 (su idP se recibirá en el parámetro pidP)
    // Estarán ordenados por el nombre, y por idS en caso de coincidir
    // Se mostrará el nombre de cadaparticipante, su rol, y su atributo idS
    public static String generarParticipantes(ArrayList<Participante> participantes, String idC, String idP, String nombreCanal, String nombrePrograma)  {
         
        StringBuilder html = new StringBuilder(); // Crea un StringBuilder para construir el HTML
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        
        // Cabecera HTML
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Participantes del programa seleccionado</title>");
        html.append("<link rel=\"stylesheet\" href=\"p2/p2.css\">"); // Enlaza el fichero CSS para estilos
        html.append("<script src=\"p2/p2.js\"></script>"); // Enlaza el fichero JS para funciones
        html.append("</head>");

        // Cuerpo HTML
        html.append("<body>");
        // Cabecera de la página
        html.append("<header>");
        html.append("<h1>Participantes del programa seleccionado:</h1>");
        html.append("</header>");
        // Contenido principal
        html.append("<main>");
        html.append("<h2>Canal: ");
        html.append(sustituirCaractaresEspecialesHTML(nombreCanal));
        html.append(" (ID: ");
        html.append(sustituirCaractaresEspecialesHTML(idC));
        html.append(")</h2>");
        html.append("<h2>Programa: ");
        html.append(sustituirCaractaresEspecialesHTML(nombrePrograma));
        html.append(" (ID: ");
        html.append(sustituirCaractaresEspecialesHTML(idP));
        html.append(")</h2>");
        // Si la lista de participantes está vacía o no existe
        if (participantes == null || participantes.isEmpty()) {
            html.append("<p>No hay participantes disponibles para este programa.</p>");
        // Si no está vacía y existe
        } else {
            // Lista de participantes
            html.append("<ul class=\"lista participantes\">");
            for (Participante participante : participantes) {
                html.append("<li>");
                html.append(sustituirCaractaresEspecialesHTML(participante.getNombreParticipante()));
                html.append(" (ID: ");
                html.append(sustituirCaractaresEspecialesHTML(participante.getIdS()));
                html.append(") - Rol: ");
                html.append(sustituirCaractaresEspecialesHTML(participante.getRole()));
                html.append("</li>");
            }
            html.append("</ul>");
        }
        html.append("<button type=\"button\" id=\"backButton\">Volver</button>"); // Botón para volver a la página anterior
        html.append("<a class=\"boton\" href=\"?pphase=0\">Volver al inicio</a>"); // Botón para volver a la página de inicio
        html.append("</main>");
        // Pie de página
        html.append("<footer>");
        html.append("<p>Práctica 2 - Adrián Ameneiros Cazorla</p>");
        html.append("</footer>");
        html.append("</body>");

        html.append("</html>");

        return html.toString();
    }

    // ====================
    // Generar fichero JSON
    // ====================

    // GET /canales
    
    public String generarJSONCanales(ArrayList<Canal> canales) {

        StringBuilder json = new StringBuilder();
        
        json.append("[");

        // Si la lista existe y no está vacía
        if (canales != null) {

            for (int i = 0; i < canales.size(); i++) {

                Canal canal = canales.get(i);
                json.append("{");
                json.append("\"name\": \"").append(sustituirCaractaresEspecialesJSON(canal.getNombreCanal())).append("\", ");
                json.append("\"idC\": \"").append(sustituirCaractaresEspecialesJSON(canal.getIdC())).append("\", ");
                json.append("\"langDefault\": \"").append(sustituirCaractaresEspecialesJSON(canal.getDefaultLang())).append("\"");
                json.append("}");

                if (i < canales.size() - 1) {
                    json.append(", ");
                }
            }
        }

        json.append("]");
        return json.toString();
    }

    // GET /canales/{idC}/programas
    
    
    public String generarJSONProgramas(ArrayList<Programa> programas) {

        StringBuilder json = new StringBuilder();

        json.append("[");

        if (programas != null) {

            for (int i = 0; i < programas.size(); i++) {

                Programa programa = programas.get(i);

                if (i > 0) {
                    json.append(", ");
                }

                json.append("{");
                json.append("\"name\": \"").append(sustituirCaractaresEspecialesJSON(programa.getNombrePrograma())).append("\", ");
                json.append("\"idP\": \"").append(sustituirCaractaresEspecialesJSON(programa.getIdP())).append("\"");

                // Atributo opcional edadMinima
                if (programa.getEdadMinima() != null&& !programa.getEdadMinima().isEmpty()) {
                    json.append(", \"edadMinima\": \"").append(sustituirCaractaresEspecialesJSON(programa.getEdadMinima())).append("\"");
                }

                // Atributo opcional langs
                if (programa.getLangs() != null && !programa.getLangs().isEmpty()) {
                    json.append(", \"langs\": \"").append(sustituirCaractaresEspecialesJSON(programa.getLangs())).append("\"");
                }

                json.append("}");
            }
        }

        json.append("]");
        return json.toString();
    } 

    // GET /canales/{idC}/programas/{idP}/participantes
    
    public String generarJSONParticipantes(ArrayList<Participante> participantes) {

        StringBuilder json = new StringBuilder();
        json.append("[");

        // Si la lista existe y no está vacía
        if (participantes != null) {

            for (int i = 0; i < participantes.size(); i++) {

                Participante participante = participantes.get(i);

                json.append("{");
                json.append("\"name\": \"").append(sustituirCaractaresEspecialesJSON(participante.getNombreParticipante())).append("\", ");
                json.append("\"idS\": \"").append(sustituirCaractaresEspecialesJSON(participante.getIdS())).append("\", ");
                json.append("\"role\": \"").append(sustituirCaractaresEspecialesJSON(participante.getRole())).append("\"");
                json.append("}");

                if (i < participantes.size() - 1) {
                    json.append(", ");
                }
            }
        }
        json.append("]");
        return json.toString();
    }

    // Generar página HTML de error
    public static String generarErrorHTML(String mensaje) {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Error - Cartelera de televisión</title>");
        html.append("<link rel=\"stylesheet\" href=\"p2/p2.css\">");
        html.append("</head>");
        html.append("<body>");
        html.append("<header>");
        html.append("<h1>Cartelera de televisión</h1>");
        html.append("</header>");
        html.append("<main>");
        html.append("<h2>Error 404</h2>");
        html.append("<p>");
        html.append(sustituirCaractaresEspecialesHTML(mensaje));
        html.append("</p>");
        html.append("<p>");
        html.append("<a href=\"P2TV\">Volver al inicio</a>");
        html.append("</p>");
        html.append("</main>");
        html.append("<footer>");
        html.append("<p>Autor: Adrián Ameneiros Cazorla</p>");
        html.append("</footer>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    // JSON de error para respuesta REST   
    public String generarJSONError(String mensaje) {

        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"status\": 404, ");
        json.append("\"error\": \"Not Found\", ");
        json.append("\"message\": \"").append(sustituirCaractaresEspecialesJSON(mensaje)).append("\"");
        json.append("}");
        return json.toString();
    }
}