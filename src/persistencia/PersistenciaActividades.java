package persistencia;

import modelo.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;



public class PersistenciaActividades {

    private static final String ARCHIVO_ACTIVIDADES = "datos/actividades.json";

    // Cargar actividades desde el archivo JSON en un HashMap
    public static HashMap<String, Actividad> cargarActividades(HashMap<String, Review> reviewsMap, HashMap<String, Pregunta> preguntasMap) {
        HashMap<String, Actividad> actividades = new HashMap<>();
        try {
            // Leer todo el contenido del archivo JSON
            String content = new String(Files.readAllBytes(Paths.get(ARCHIVO_ACTIVIDADES)));

            // Convertir el contenido en un JSONArray
            JSONArray jsonActividades = new JSONArray(content);

            // Iterar sobre cada objeto en el JSONArray
            for (int i = 0; i < jsonActividades.length(); i++) {
                JSONObject jsonActividad = jsonActividades.getJSONObject(i);

                // Obtener los datos de la actividad
                String titulo = jsonActividad.getString("titulo");
                String descripcion = jsonActividad.getString("descripcion");
                int nivelDificultad = jsonActividad.getInt("nivelDificultad");
                int duracionMin = jsonActividad.getInt("duracionMin");
                boolean obligatorio = jsonActividad.getBoolean("obligatorio");
                int tiempoCompletarSugerido = jsonActividad.getInt("tiempoCompletarSugerido");
                String tipoActividad = jsonActividad.getString("tipoActividad");

                // Crear una instancia de Actividad
                Actividad actividad;
                switch (tipoActividad) {
                    case "Prueba":
                        String tipoPrueba = jsonActividad.getString("tipoPrueba");
                        switch (tipoPrueba) {
                            case "Encuesta":
                            	JSONArray jsonPreguntas = jsonActividad.getJSONArray("preguntas");
                            	List<PreguntaAbierta> listaPreguntas = new ArrayList<>();
                                for (int j = 0; j < jsonPreguntas.length(); j++) {
                                    String enunciadoPregunta = jsonPreguntas.getString(j);
                                    // Buscar la pregunta en el HashMap de preguntas usando el enunciado como clave
                                    Pregunta pregunta = preguntasMap.get(enunciadoPregunta );
                                    if (pregunta != null) {
                                        listaPreguntas.add((PreguntaAbierta) pregunta);
                                    }
                                }
                                actividad = new Encuesta(titulo, descripcion, nivelDificultad, duracionMin, obligatorio, tiempoCompletarSugerido, tipoActividad, listaPreguntas, tipoPrueba);
                                break;
                            case "Quiz":
                            	JSONArray jsonPreguntas3 = jsonActividad.getJSONArray("preguntas");
                            	List<PreguntaMultiple> listaPreguntas3 = new ArrayList<>();
                                for (int j = 0; j < jsonPreguntas3.length(); j++) {
                                    String enunciadoPregunta = jsonPreguntas3.getString(j);
                                    // Buscar la pregunta en el HashMap de preguntas usando el enunciado como clave
                                    Pregunta pregunta = preguntasMap.get(enunciadoPregunta );
                                    if (pregunta != null) {
                                        listaPreguntas3.add((PreguntaMultiple) pregunta);
                                    }
                                }
                            	float calificacionMinima = jsonActividad.getFloat("calificacionMinima");
                                actividad = new Quiz(titulo, descripcion, nivelDificultad, duracionMin, obligatorio,
                            			tiempoCompletarSugerido, tipoActividad, calificacionMinima, listaPreguntas3, tipoPrueba);
                                break;
                            case "Examen":
                            	JSONArray jsonPreguntas2 = jsonActividad.getJSONArray("preguntas");
                            	List<PreguntaAbierta> listaPreguntas2 = new ArrayList<>();
                                for (int j = 0; j < jsonPreguntas2.length(); j++) {
                                    String enunciadoPregunta = jsonPreguntas2.getString(j);
                                    Pregunta pregunta = preguntasMap.get(enunciadoPregunta );
                                    if (pregunta != null) {
                                        listaPreguntas2.add((PreguntaAbierta) pregunta);
                                    }
                                }
                                actividad = new Examen(titulo, descripcion, nivelDificultad, duracionMin,
                            			obligatorio, tiempoCompletarSugerido, tipoActividad, listaPreguntas2, tipoPrueba);
                                Boolean calificado = jsonActividad.getBoolean("calificado");
                                ((Examen) actividad).setCalificado(calificado);
                                break;
                            default:
                                throw new IllegalArgumentException("Tipo de actividad desconocido: " + tipoActividad);
                        }
                        break;
                    case "Tarea":
                    	String contenido = jsonActividad.getString("contenido");
                        actividad = new Tarea(titulo, descripcion, nivelDificultad, duracionMin, obligatorio, tiempoCompletarSugerido, tipoActividad, contenido);
                        break;
                    case "Recurso Educativo":
                    	String contenido2 = jsonActividad.getString("contenido");
                    	String tipoRecurso =  jsonActividad.getString("tipoRecurso");
                        actividad = new RecursoEducativo(titulo, descripcion, nivelDificultad, duracionMin,
                    			obligatorio, tiempoCompletarSugerido, tipoActividad, tipoRecurso, contenido2);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de actividad desconocido: " + tipoActividad);
                }

                String id = actividad.getId();

                // Leer las reviews de la actividad
                JSONArray jsonReviews = jsonActividad.getJSONArray("reviews");
                List<Review> listaReviews = new ArrayList<>();
                for (int j = 0; j < jsonReviews.length(); j++) {
                    String contenidoReview = jsonReviews.getString(j);
                    // Buscar la review en el HashMap de reviews usando el contenido como clave
                    Review review = reviewsMap.get(contenidoReview);
                    if (review != null) {
                        listaReviews.add(review);
                    }
                }
                actividad.setReviews(listaReviews);

                // Agregar la actividad al HashMap usando el id como llave
                actividades.put(id, actividad);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return actividades;  // Devolver el mapa de actividades
    }

    // Guardar actividades en el archivo JSON desde un HashMap
    public static void guardarActividades(HashMap<String, Actividad> actividades) {
        try {
            // Crear un JSONArray para almacenar las actividades
            JSONArray jsonActividades = new JSONArray();

            // Convertir cada actividad en un JSONObject
            for (Actividad actividad : actividades.values()) {
                JSONObject jsonActividad = new JSONObject();
                jsonActividad.put("titulo", actividad.getTitulo());
                jsonActividad.put("descripcion", actividad.getDescripcion());
                jsonActividad.put("nivelDificultad", actividad.getNivelDificultad());
                jsonActividad.put("duracionMin", actividad.getDuracionMin());
                jsonActividad.put("obligatorio", actividad.isObligatorio());
                jsonActividad.put("tiempoCompletarSugerido", actividad.getTiempoCompletarSugerido());
                jsonActividad.put("tipoActividad", actividad.getTipoActividad());
                jsonActividad.put("id", actividad.getId());

                // Procesar las actividades según su tipo
                if (actividad instanceof Encuesta) {
                    Encuesta encuesta = (Encuesta) actividad;
                    jsonActividad.put("tipoPrueba", "Encuesta");
                    
                    // Convertir las preguntas de Encuesta
                    JSONArray jsonPreguntas = new JSONArray();
                    for (PreguntaAbierta pregunta : encuesta.getPreguntas()) {
                        jsonPreguntas.put(pregunta.getEnunciado());
                    }
                    jsonActividad.put("preguntas", jsonPreguntas);

                } else if (actividad instanceof Quiz) {
                    Quiz quiz = (Quiz) actividad;
                    jsonActividad.put("tipoPrueba", "Quiz");
                    jsonActividad.put("calificacionMinima", quiz.getCalificacionMinima());

                    // Convertir las preguntas de Quiz
                    JSONArray jsonPreguntas = new JSONArray();
                    for (PreguntaMultiple pregunta : quiz.getPreguntas()) {
                        jsonPreguntas.put(pregunta.getEnunciado());
                    }
                    jsonActividad.put("preguntas", jsonPreguntas);

                } else if (actividad instanceof Examen) {
                    Examen examen = (Examen) actividad;
                    jsonActividad.put("tipoPrueba", "Examen");
                    jsonActividad.put("calificado", examen.isCalificado());

                    // Convertir las preguntas de Examen
                    JSONArray jsonPreguntas = new JSONArray();
                    for (PreguntaAbierta pregunta : examen.getPreguntas()) {
                        jsonPreguntas.put(pregunta.getEnunciado());
                    }
                    jsonActividad.put("preguntas", jsonPreguntas);

                } else if (actividad instanceof Tarea) {
                    Tarea tarea = (Tarea) actividad;
                    jsonActividad.put("contenido", tarea.getContenido());

                } else if (actividad instanceof RecursoEducativo) {
                    RecursoEducativo recurso = (RecursoEducativo) actividad;
                    jsonActividad.put("contenido", recurso.getContenido());
                    jsonActividad.put("tipoRecurso", recurso.getTipoRecurso());
                }

                // Guardar solo el contenido de las reviews en el JSON
                JSONArray jsonReviews = new JSONArray();
                for (Review review : actividad.getReviews()) {
                    jsonReviews.put(review.getContenido());
                }
                jsonActividad.put("reviews", jsonReviews);

                // Agregar el JSONObject al JSONArray
                jsonActividades.put(jsonActividad);
            }

            // Escribir el JSONArray al archivo
            try (FileWriter file = new FileWriter(ARCHIVO_ACTIVIDADES)) {
                file.write(jsonActividades.toString(4));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
