PRACTICA 2 - SERVICIOS DE INTERNET (CURSO 2026-2027)

IDENTIFICADOR DE CUENTA: sint7

1. INFORMACION GENERAL

La practica implementa un servicio de consulta de carteleras de television
utilizando Java 11, Servlets (API Jakarta) y Tomcat 10. El codigo pertenece
al paquete Java p2.

2. ARCHIVOS ENTREGADOS

Canal.java             Clase para representar un canal.
Programa.java          Clase para representar un programa.
Participante.java      Clase para representar un participante.
DataModel.java         Carga y analiza el documento TVML mediante DOM/JAXP,
                       y proporciona las consultas de datos.
FrontEnd.java          Genera las respuestas HTML y JSON.
Sint7P2.java           Servlet único para las interfaces HTML y REST.
p2.css                Hoja de estilos de la interfaz HTML.
p2.js                 Codigo JavaScript de la interfaz HTML.

3. DOCUMENTO TVML

El documento de datos se obtiene directamente de la URL oficial indicada
para la practica:

https://alberto.gil.webs.uvigo.es/SINT/26-27/p2/tvml.xml

No se incluye ni se utiliza una copia local del XML. DataModel.java abre la
conexion con esa URL y analiza el documento con un parser DOM de JAXP. La
instancia de DataModel se crea al inicializar Sint7P2 y se reutiliza
para atender las dos interfaces mediante el mismo arbol DOM.

4. DESPLIEGUE Y UBICACION DE LOS ARCHIVOS

El contexto de Tomcat correspondiente a esta cuenta es:

sint7

De acuerdo con la estructura indicada en el enunciado:

- Los ficheros fuente Java y este readme.txt deben estar en:
  public_html/p2/

- Las clases compiladas (.class) deben estar en:
  public_html/webapps/WEB-INF/classes/p2/

- Los recursos estaticos de la interfaz deben estar en:
  public_html/webapps/p2/
  (p2.css y p2.js).

El contexto sint7 debe estar configurado para utilizar el directorio
public_html/webapps como raiz de la aplicacion web, segun la configuracion
indicada en el enunciado.

5. ACCESO A LA PRACTICA

Suponiendo que Tomcat se ejecuta en la misma maquina que el navegador y que
se utiliza el puerto indicado en el enunciado (7000 + numero de cuenta):

Interfaz HTML:
http://localhost:7007/sint7/P2TV

API REST (raiz):
http://localhost:7007/sint7/P2TV/v1

Endpoints REST implementados:

GET /canales
GET /canales/{idC}/programas
GET /canales/{idC}/programas/{idP}/participantes

Las rutas REST se solicitan bajo la raiz anterior. Por ejemplo:
http://localhost:7007/sint7/P2TV/v1/canales

6. NAVEGACION HTML

La interfaz HTML utiliza el parametro pphase para seleccionar la fase:

- pphase=0: pantalla inicial.
- pphase=1: lista de canales.
- pphase=2: programas del canal seleccionado (pidC).
- pphase=3: participantes del programa seleccionado (pidC y pidP).

Los enlaces de navegacion transmiten tambien los nombres del canal y del
programa seleccionados para mostrar la ruta de consulta sin volver a
recuperar las listas de fases anteriores.

7. OBSERVACIONES PARA LA CORRECCION

- No es necesario instalar un fichero tvml.xml en el directorio webapps/p2:
  los datos se leen desde la URL remota indicada en el apartado 3.
- El servlet crea el DataModel durante su inicializacion y conserva esa
  instancia para atender las dos interfaces.
- La API REST comprueba que los identificadores recibidos correspondan
  a canales y programas existentes. Si no existen, devuelve un error 404.
- La interfaz utiliza los recursos p2/p2.css y p2/p2.js.