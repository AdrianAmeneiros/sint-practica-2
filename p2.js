
/* INFORMACION DEL NAVEGADOR */
function mostrarNavegador() {

    // Obtener la informacion del navegador
    var navegador = navigator.userAgent;

    // Buscar el elemento HTML donde mostrarla
    var elemento = document.getElementById("browserInfo");

    // Mostrar la informacion si el elemento existe
    if (elemento !== null) {
        elemento.textContent = navegador;
    }
}


/* BOTON VOLVER */
function volverAtras() {

    // Retroceder a la pagina anterior del historial
    window.history.back();
}


/* INICIALIZACION */
document.addEventListener("DOMContentLoaded", function() {

    // Mostrar la informacion del navegador
    mostrarNavegador();

    // Buscar el boton Volver
    var botonVolver = document.getElementById("backButton");

    // Configurar el evento del boton si existe
    if (botonVolver !== null) {
        botonVolver.addEventListener("click", volverAtras);
    }

});