package p2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet único para las dos interfaces de la práctica:
 * - HTML: /P2TV
 * - REST: /P2TV/v1/*
 */
@WebServlet(name = "Sint7P2", urlPatterns = {"/P2TV", "/P2TV/v1/*"})
public class Sint7P2 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DataModel dataModel;
    private FrontEnd frontEnd;

    // Resultados de las consultas reutilizados por las dos interfaces.
    // Los canales se cargan al iniciar; programas y participantes se almacenan bajo demanda.
    private ArrayList<Canal> canales;
    private final ConcurrentMap<String, ArrayList<Programa>> programasCache = new ConcurrentHashMap<String, ArrayList<Programa>>();
    private final ConcurrentMap<String, ConcurrentMap<String, ArrayList<Participante>>> participantesCache = new ConcurrentHashMap<String, ConcurrentMap<String, ArrayList<Participante>>>();

    // Funciones de carga para las cachés. Cada llamada al DataModel queda definida en un único punto.
    private Function<String, ArrayList<Programa>> consultaProgramas;
    private BiFunction<String, String, ArrayList<Participante>> consultaParticipantes;

    /**
     * Carga el documento TVML una sola vez al inicializar el servlet.
     */
    @Override
    public void init() throws ServletException {
        try {
            dataModel = new DataModel();
            frontEnd = new FrontEnd();

            // Los canales se consultan una sola vez y se reutilizan en HTML y REST.
            canales = dataModel.getQ1Canales();

            // Las consultas de programas y participantes se cargan bajo demanda.
            // Se sincroniza el acceso al DOM porque el servlet puede atender peticiones simultáneas.
            consultaProgramas = idC -> {
                synchronized (dataModel) {
                    return dataModel.getQ1Programas(idC);
                }
            };

            consultaParticipantes = (idC, idP) -> {
                synchronized (dataModel) {
                    return dataModel.getQ1Participantes(idC, idP);
                }
            };
        } catch (IOException e) {
            throw new ServletException("Error al cargar el documento TVML desde " + DataModel.TVML_URL, e);
        }
    }

    /**
    * Gestiona las peticiones HTTP recibidas por el servlet.
    * La API REST solamente admite peticiones GET.
    * Cualquier otro método HTTP dirigido a la API REST
    * devolverá un error 404.
    * Las demás peticiones se procesan mediante el mecanismo
    * habitual de HttpServlet.
    */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // La API REST solamente admite GET.
        if (esPeticionRest(request) && !"GET".equals(request.getMethod())) {
            enviarErrorREST(response, "La API REST solamente admite GET");
            return;
        }

        // Procesar normalmente el resto de peticiones.
        super.service(request, response);
    }

    /**
     * Distingue la interfaz solicitada mediante la ruta del servlet.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (esPeticionRest(request)) {
            atenderRest(request, response);
        } else {
            atenderHTML(request, response);
        }
    }

    /**
     * La interfaz HTML también admite POST y procesa sus parámetros igual
     * que una petición GET. La API REST de esta práctica sólo implementa GET.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        atenderHTML(request, response); // No hace falta comprobar si es REST porque ya lo hace service(...)
    }

    private boolean esPeticionRest(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return servletPath != null && "/P2TV/v1".equals(servletPath);
    }

    // -----------------------------------------------------------------
    // Interfaz HTML
    // -----------------------------------------------------------------

    private void atenderHTML(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String fase = request.getParameter("pphase");
        String idC = request.getParameter("pidC");
        String idP = request.getParameter("pidP");

        if (fase == null) {
            fase = "0";
        }

        String ip = request.getRemoteAddr();
        String html;

        if (dataModel == null || frontEnd == null) {
            throw new IllegalStateException("El modelo de datos o FrontEnd no está inicializado");
        }

        // FASE 0: Página inicial.
        if ("0".equals(fase)) {

            String urlXML = DataModel.TVML_URL;
            html = FrontEnd.generarInicio(urlXML, ip);

        // FASE 1: Lista de canales.
        } else if ("1".equals(fase)) {

            html = FrontEnd.generarListaCanales(canales);

        // FASE 2: Lista de programas del canal seleccionado.
        } else if ("2".equals(fase)) {

            if (idC == null || idC.isEmpty()) {
                enviarErrorHTML(response, "Falta el parámetro pidC");
                return;
            }

            // Buscar el canal seleccionado.
            Canal canalSeleccionado = buscarCanal(idC);

            if (canalSeleccionado == null) {
                enviarErrorHTML(response, "Canal " + idC + " no encontrado");
            return;
            }

            // Obtener los programas del canal.
            ArrayList<Programa> listaProgramas = programasCache.computeIfAbsent(idC, consultaProgramas);

            if (listaProgramas.isEmpty()) {
                enviarErrorHTML(response, "El canal " + idC + " no tiene programas");
                return;
            }

            // Generar la página utilizando el nombre obtenido del XML.
            html = FrontEnd.generarProgramas(listaProgramas, idC, canalSeleccionado.getNombreCanal());

        // FASE 3: Lista de participantes del programa seleccionado.
        } else if ("3".equals(fase)) {

            if (idC == null || idC.isEmpty() || idP == null || idP.isEmpty()) {
                enviarErrorHTML(response, "Faltan los parámetros pidC o pidP");
                return;
            }

            // Buscar el canal seleccionado.
            Canal canalSeleccionado = buscarCanal(idC);

            if (canalSeleccionado == null) {
                enviarErrorHTML(response, "Canal " + idC + " no encontrado");
                return;
            }

            // Obtener los programas del canal seleccionado.
            ArrayList<Programa> listaProgramas = programasCache.computeIfAbsent(idC, consultaProgramas);

            if (listaProgramas.isEmpty()) {
                enviarErrorHTML(response, "El canal " + idC + " no tiene programas");
                return;
            }

            // Buscar el programa seleccionado.
            Programa programaSeleccionado = null;

            for (Programa programa : listaProgramas) {

                if (idP.equals(programa.getIdP())) {
                    programaSeleccionado = programa;
                    break;
                }
            }

            if (programaSeleccionado == null) {
                enviarErrorHTML(response, "El programa " + idP + " no existe en el canal " + idC);
                return;
            }

            // Obtener los participantes del programa.
            ConcurrentMap<String, ArrayList<Participante>> participantesDelCanal = participantesCache.computeIfAbsent(idC, claveCanal -> new ConcurrentHashMap<String, ArrayList<Participante>>());

            ArrayList<Participante> listaParticipantes = participantesDelCanal.computeIfAbsent(idP, clavePrograma -> consultaParticipantes.apply(idC, idP));

            // Generar la página utilizando los nombres obtenidos del XML.
            html = FrontEnd.generarParticipantes(listaParticipantes, idC, idP, canalSeleccionado.getNombreCanal(), programaSeleccionado.getNombrePrograma());

        } else {
            enviarErrorHTML(response, "Fase desconocida: " + fase);
            return;
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
    }

    // Busca un canal por su identificador.
    private Canal buscarCanal(String idC) {

        for (Canal canal : canales) {
            if (idC.equals(canal.getIdC())) {
                return canal;
            }
        }
        return null;
    }

    private void enviarErrorHTML(HttpServletResponse response, String mensaje) throws IOException {

        String html = FrontEnd.generarErrorHTML(mensaje);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
    }

    // -----------------------------------------------------------------
    // API REST
    // -----------------------------------------------------------------

    private void atenderRest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String ruta = request.getPathInfo();

        if (ruta == null || ruta.equals("/")) {
            enviarErrorREST(response, "Ruta no encontrada");
            return;
        }

        // Conserva los segmentos vacíos para rechazar URLs mal formadas.
        String[] partes = ruta.substring(1).split("/", -1);

        // GET /canales
        if (partes.length == 1 && "canales".equals(partes[0])) {

            enviarJSON(response, frontEnd.generarJSONCanales(canales));
            return;
        }

        // GET /canales/{idC}/programas
        if (partes.length == 3 && "canales".equals(partes[0]) && "programas".equals(partes[2])) {

            String idC = partes[1];

            if (idC.isEmpty()) {
                enviarErrorREST(response, "Falta el identificador del canal");
                return;
            }

            ArrayList<Programa> programas =
                    programasCache.computeIfAbsent(idC, consultaProgramas);

            if (programas.isEmpty()) {
                enviarErrorREST(response, "El canal " + idC + " no existe");
                return;
            }

            String json = frontEnd.generarJSONProgramas(programas);
            enviarJSON(response, json);
            return;
        }

        // GET /canales/{idC}/programas/{idP}/participantes
        if (partes.length == 5 && "canales".equals(partes[0]) && "programas".equals(partes[2]) && "participantes".equals(partes[4])) {

            String idC = partes[1];
            String idP = partes[3];

            if (idC.isEmpty() || idP.isEmpty()) {
                enviarErrorREST(response, "Falta el identificador del canal o del programa");
                return;
            }

            ArrayList<Programa> programas =
                    programasCache.computeIfAbsent(idC, consultaProgramas);

            if (programas.isEmpty()) {
                enviarErrorREST(response, "El canal " + idC + " no existe");
                return;
            }

            boolean programaExiste = false;

            for (Programa programa : programas) {
                if (idP.equals(programa.getIdP())) {
                    programaExiste = true;
                    break;
                }
            }

            if (!programaExiste) {
                enviarErrorREST(response, "El programa " + idP + " no existe en el canal " + idC);
                return;
            }

            ConcurrentMap<String, ArrayList<Participante>> participantesDelCanal = participantesCache.computeIfAbsent(idC, claveCanal -> new ConcurrentHashMap<String, ArrayList<Participante>>());

            ArrayList<Participante> participantes = participantesDelCanal.computeIfAbsent(idP, clavePrograma -> consultaParticipantes.apply(idC, idP));

            String json = frontEnd.generarJSONParticipantes(participantes);

            enviarJSON(response, json);
            return;
        }

        enviarErrorREST(response, "Ruta no encontrada");
    }

    private void enviarJSON(HttpServletResponse response, String json) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private void enviarErrorREST(HttpServletResponse response, String mensaje) throws IOException {

        String json = frontEnd.generarJSONError(mensaje);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    @Override
    public void destroy() {
        canales = null;
        programasCache.clear();
        participantesCache.clear();
        consultaProgramas = null;
        consultaParticipantes = null;
        dataModel = null;
        frontEnd = null;
        super.destroy();
    }
}