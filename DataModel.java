package p2;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Gestiona la lectura y las consultas sobre el documento TVML.
 *
 * El XML se analiza una sola vez, al construir este objeto, y se conserva
 * como un arbol DOM. Las consultas recorren ese arbol y devuelven objetos
 * Canal, Programa y Participante ya ordenados.
 */
public class DataModel {

    public static final String TVML_URL = "https://alberto.gil.webs.uvigo.es/SINT/26-27/p2/tvml.xml";
    private final Document doc;

    /**
     * Construye el modelo leyendo el documento TVML
     * directamente desde la URL oficial.
     *
     * El documento se analiza una sola vez mediante DOM.
     */
    public DataModel() throws IOException {

        try {

            // Abrir conexion con el documento remoto.
            URL url = new URL(TVML_URL);
            URLConnection conexion = url.openConnection();

            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(15000);
            conexion.setUseCaches(false);

            // Configurar el parser DOM.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            // Proteccion del parser XML.
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Leer y analizar el XML desde la URL.
            try (InputStream xmlInputStream = conexion.getInputStream()) {

                Document documento = builder.parse(xmlInputStream);
                Element raiz = documento.getDocumentElement();

                if (raiz == null || !"Programacion".equals(raiz.getTagName())) {
                    throw new IOException("El elemento raiz del documento TVML debe ser Programacion");
                }

                raiz.normalize();

                // Comprobar los formatos de los campos del documento TVML.
                validarFormatosTVML(documento);

                doc = documento;
            }

        } catch (IOException e) {
            throw e;

        } catch (Exception e) {
            throw new IOException("Error al cargar el documento TVML desde la URL oficial", e);
        }
    }

    /**
     * Devuelve todos los canales ordenados alfabeticamente por
     * defaultLang y, en caso de coincidencia, por idC.
     */
    public ArrayList<Canal> getQ1Canales() {

        ArrayList<Canal> canales = new ArrayList<Canal>();
        NodeList nodosCanal = doc.getElementsByTagName("Canal");

        for (int i = 0; i < nodosCanal.getLength(); i++) {

            Element elementoCanal = (Element) nodosCanal.item(i);
            String idC = elementoCanal.getAttribute("idC");
            String defaultLang = elementoCanal.getAttribute("defaultLang");
            String nombreCanal = obtenerTexto(elementoCanal, "NombreCanal");

            canales.add(new Canal(idC, defaultLang, nombreCanal));
        }

        Collections.sort(canales, new Comparator<Canal>() {

            @Override
            public int compare(Canal canal1, Canal canal2) {

                int comparacionIdioma = canal1.getDefaultLang().compareTo(canal2.getDefaultLang());

                if (comparacionIdioma != 0) {
                    return comparacionIdioma;
                }

                return canal1.getIdC().compareTo(canal2.getIdC());
            }
        });

        return canales;
    }

    /**
     * Devuelve los programas del canal indicado, ordenados
     * de forma decreciente por idP.
     */
    public ArrayList<Programa> getQ1Programas(String idC) {

        ArrayList<Programa> programas = new ArrayList<Programa>();

        if (idC == null) {
            return programas;
        }

        NodeList nodosCanal = doc.getElementsByTagName("Canal");

        for (int i = 0; i < nodosCanal.getLength(); i++) {

            Element elementoCanal = (Element) nodosCanal.item(i);

            if (idC.equals(elementoCanal.getAttribute("idC"))) {

                NodeList nodosPrograma = elementoCanal.getElementsByTagName("Programa");

                for (int j = 0; j < nodosPrograma.getLength(); j++) {

                    Element elementoPrograma = (Element) nodosPrograma.item(j);
                    String idP = elementoPrograma.getAttribute("idP");
                    String edadMinima = obtenerAtributoOpcional(elementoPrograma, "edadMinima");
                    String langs = elementoPrograma.getAttribute("langs").trim();
                    String nombrePrograma = obtenerTexto(elementoPrograma, "NombrePrograma");

                    programas.add(new Programa(idP,edadMinima,langs,nombrePrograma));
                }

                // idC identifica un unico canal.
                break;
            }
        }

        Collections.sort(programas, new Comparator<Programa>() {

            @Override
            public int compare(Programa programa1, Programa programa2) {

                return programa2.getIdP().compareTo(programa1.getIdP());
            }
        });

        return programas;
    }

    /**
     * Devuelve los participantes del programa indicado dentro
     * del canal indicado, ordenados por nombre y, si coincide,
     * por idS.
     */
    public ArrayList<Participante> getQ1Participantes(String idC, String idP) {

        ArrayList<Participante> participantes = new ArrayList<Participante>();

        if (idC == null || idP == null) {
            return participantes;
        }

        NodeList nodosCanal = doc.getElementsByTagName("Canal");

        for (int i = 0; i < nodosCanal.getLength(); i++) {

            Element elementoCanal = (Element) nodosCanal.item(i);

            if (idC.equals(elementoCanal.getAttribute("idC"))) {

                NodeList nodosPrograma = elementoCanal.getElementsByTagName("Programa");

                for (int j = 0; j < nodosPrograma.getLength(); j++) {

                    Element elementoPrograma = (Element) nodosPrograma.item(j);

                    if (idP.equals(elementoPrograma.getAttribute("idP"))) {

                        NodeList nodosParticipante = elementoPrograma.getElementsByTagName("Participante");

                        for (int k = 0; k < nodosParticipante.getLength(); k++) {

                            Element elementoParticipante = (Element) nodosParticipante.item(k);
                            String idS = elementoParticipante.getAttribute("idS");
                            String nombre = obtenerTexto(elementoParticipante, "NombreParticipante");
                            String role = obtenerTexto(elementoParticipante, "Role");

                            participantes.add(new Participante(idS, nombre, role));
                        }

                        // idP identifica un unico programa en el canal.
                        break;
                    }
                }

                // idC identifica un unico canal.
                break;
            }
        }

        final Collator comparadorEspanol = Collator.getInstance(new Locale("es", "ES"));
        comparadorEspanol.setStrength(Collator.TERTIARY);
        comparadorEspanol.setDecomposition(Collator.CANONICAL_DECOMPOSITION);

        Collections.sort(participantes, new Comparator<Participante>() {

                @Override
                public int compare(Participante participante1, Participante participante2) {

                    int comparacionNombre = comparadorEspanol.compare(participante1.getNombreParticipante(), participante2.getNombreParticipante());

                    if (comparacionNombre != 0) {
                        return comparacionNombre;
                    }

                    return participante1.getIdS().compareTo(participante2.getIdS());
                }
            }
        );

        return participantes;
    }

    /**
     * Obtiene el texto de un elemento.
     * Devuelve una cadena vacia si no existe.
     */
    private String obtenerTexto(Element elementoPadre, String nombreElemento) {

        NodeList nodos = elementoPadre.getElementsByTagName(nombreElemento);

        if (nodos.getLength() == 0) {
            return "";
        }

        return nodos.item(0).getTextContent().trim();
    }

    /**
     * Obtiene un atributo opcional.
     * Devuelve null cuando no existe para permitir
     * que FrontEnd lo omita en la respuesta JSON.
     */
    private String obtenerAtributoOpcional(Element elemento, String nombreAtributo) {

        if (!elemento.hasAttribute(nombreAtributo)) {
            return null;
        }

        return elemento.getAttribute(nombreAtributo).trim();
    }

    /**
    * Comprueba que los campos del documento TVML
    * cumplen los formatos establecidos en el enunciado.
    */
    private void validarFormatosTVML(Document documento) throws IOException {

        NodeList canales = documento.getElementsByTagName("Canal");

        for (int i = 0; i < canales.getLength(); i++) {

            Element canal = (Element) canales.item(i);

            // Comprobar idC: 2 letras minúsculas y 3 dígitos.
            String idC = canal.getAttribute("idC");

            if (!idC.matches("[a-z]{2}[0-9]{3}")) {
                throw new IOException("Formato incorrecto de idC: " + idC);
            }

            // Comprobar defaultLang: exactamente 2 caracteres.
            String defaultLang = canal.getAttribute("defaultLang");

            if (defaultLang.length() != 2) {
                throw new IOException("Formato incorrecto de defaultLang: " + defaultLang);
            }

            NodeList programas = canal.getElementsByTagName("Programa");

            for (int j = 0; j < programas.getLength(); j++) {

                Element programa = (Element) programas.item(j);

                // Comprobar idP: 2 dígitos y 3 letras mayúsculas.
                String idP = programa.getAttribute("idP");

                if (!idP.matches("[0-9]{2}[A-Z]{3}")) {
                    throw new IOException("Formato incorrecto de idP: " + idP);
                }

                // Comprobar langs: cadenas de 2 caracteres separadas por espacios.
                String langs = programa.getAttribute("langs").trim();

                if (langs.isEmpty()) {
                    throw new IOException("Formato incorrecto de langs en el programa " + idP);
                }

                String[] idiomas = langs.split("\\s+");

                for (String idioma : idiomas) {
                    if (idioma.length() != 2) {
                        throw new IOException("Codigo de idioma incorrecto en el programa " + idP);
                    }
                }

                // edadMinima es opcional y el enunciado no especifica su formato.
                // Por tanto, no se realiza ninguna comprobación adicional.

                NodeList participantes = programa.getElementsByTagName("Participante");

                for (int k = 0; k < participantes.getLength(); k++) {

                    Element participante = (Element) participantes.item(k);

                    // Comprobar idS: formato DNI (8 dígitos y una letra mayúscula).
                    String idS = participante.getAttribute("idS");

                    if (!idS.matches("[0-9]{8}[A-Z]")) {
                        throw new IOException("Formato incorrecto de idS: " + idS);
                    }

                    // Comprobar Role: uno de los tres valores permitidos.
                    String role = obtenerTexto(participante, "Role");

                    if (!"principal".equals(role) && !"secundario".equals(role) && !"extra".equals(role)) {
                        throw new IOException("Rol incorrecto: " + role);
                    }
                }
            }
        }
    }
}