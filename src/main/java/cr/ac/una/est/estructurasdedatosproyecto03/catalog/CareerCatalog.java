package cr.ac.una.est.estructurasdedatosproyecto03.catalog;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CareerCatalog{
    private static final List<Option> CAMPUSES = List.of(
        new Option("omar-dengo", "Campus Omar Dengo"),
        new Option("benjamin-nunez", "Campus Benjamín Núñez"),
        new Option("liberia", "Campus Liberia"),
        new Option("nicoya", "Campus Nicoya"),
        new Option("perez-zeledon", "Campus Pérez Zeledón"),
        new Option("coto", "Campus Coto"),
        new Option("sarapiqui", "Campus Sarapiquí"),
        new Option("alajuela-interuniversitaria", "Sede Interuniversitaria Alajuela")
    );
    private static final Map<String, Option> CAMPUSES_BY_ID = createCampusIndex();
    private static final Map<String, List<String>> CAREER_NAMES_BY_CAMPUS_ID = createCareerNamesByCampus();
    private static final Map<String, Option> CAREERS_BY_ID = createCareerIndex();
    private static final Map<String, List<String>> CAMPUS_IDS_BY_CAREER_ID = createCampusIdsByCareer();

    // Prevent instances because the catalog only provides shared reference data.
    // Output: no instance can be created.
    private CareerCatalog (){
    }

    // Return every campus so the sign-up screen can show the complete initial list.
    // Output: an immutable list of campus options.
    public static List<Option> getCampuses (){
        return List.copyOf(CAMPUSES);
    }

    // Return every career so the sign-up screen can show the complete initial list.
    // Output: an immutable list of career options.
    public static List<Option> getCareers (){
        return List.copyOf(CAREERS_BY_ID.values());
    }

    // Return the careers offered at a campus so the screen can filter its career menu.
    // Inputs: campus ID; output: its available careers, or an empty list for an unknown campus.
    public static List<Option> getCareersForCampus (String campusId){
        List<String> careerNames = CAREER_NAMES_BY_CAMPUS_ID.get(campusId);

        if (careerNames == null){
            return List.of();
        }

        List<Option> availableCareers = new ArrayList<>();

        for (String careerName : careerNames){
            availableCareers.add(CAREERS_BY_ID.get(createCareerId(careerName)));
        }

        return List.copyOf(availableCareers);
    }

    // Return the campuses that offer a career so the screen can filter its campus menu.
    // Inputs: career ID; output: its available campuses, or an empty list for an unknown career.
    public static List<Option> getCampusesForCareer (String careerId){
        List<String> campusIds = CAMPUS_IDS_BY_CAREER_ID.get(careerId);

        if (campusIds == null){
            return List.of();
        }

        List<Option> availableCampuses = new ArrayList<>();

        for (String campusId : campusIds){
            availableCampuses.add(CAMPUSES_BY_ID.get(campusId));
        }

        return List.copyOf(availableCampuses);
    }

    // Check that a career and campus are paired in the 2026 offer.
    // Inputs: career ID and campus ID; output: true only when that combination is available.
    public static boolean isAvailable (String careerId, String campusId){
        if (careerId == null || campusId == null){
            return false;
        }

        List<String> careerNames = CAREER_NAMES_BY_CAMPUS_ID.get(campusId);

        if (careerNames == null){
            return false;
        }

        for (String careerName : careerNames){
            if (createCareerId(careerName).equals(careerId)){
                return true;
            }
        }

        return false;
    }

    // Find a career's Spanish display name for a stored career ID.
    // Inputs: career ID; output: its Spanish name, or null if the ID is unknown.
    public static String getCareerName (String careerId){
        Option career = CAREERS_BY_ID.get(careerId);
        return career == null ? null : career.getName();
    }

    // Find a campus's Spanish display name for a stored campus ID.
    // Inputs: campus ID; output: its Spanish name, or null if the ID is unknown.
    public static String getCampusName (String campusId){
        Option campus = CAMPUSES_BY_ID.get(campusId);
        return campus == null ? null : campus.getName();
    }

    // Index all campuses by their stable IDs for quick lookups.
    // Output: an immutable map of campus IDs to campus options.
    private static Map<String, Option> createCampusIndex (){
        Map<String, Option> campusesById = new LinkedHashMap<>();

        for (Option campus : CAMPUSES){
            campusesById.put(campus.getId(), campus);
        }

        return Collections.unmodifiableMap(campusesById);
    }

    // Store each campus's 2026 career offer as Spanish catalog names.
    // Output: an immutable campus-to-career mapping.
    private static Map<String, List<String>> createCareerNamesByCampus (){
        Map<String, List<String>> careerNamesByCampusId = new LinkedHashMap<>();

        careerNamesByCampusId.put("omar-dengo", List.of(
            "Administración",
            "Administración de Oficinas",
            "Arte Escénico",
            "Arte y Comunicación Visual",
            "Bibliotecología y Gestión de la Información",
            "Biología",
            "Cartografía y Diseño Digital",
            "Ciencias Geográficas",
            "Comercio y Negocios Internacionales",
            "Danza",
            "Economía",
            "Educación Comercial",
            "Educación Especial",
            "Enseñanza del Arte y Comunicación Visual",
            "Enseñanza del Español",
            "Enseñanza del Francés",
            "Enseñanza del Inglés",
            "Enseñanza del Inglés para I y II Ciclos",
            "Enseñanza de la Matemática",
            "Enseñanza de la Religión",
            "Enseñanza de las Ciencias",
            "Enseñanza de los Estudios Sociales y la Educación Cívica",
            "Filosofía",
            "Física Aplicada",
            "Historia",
            "Ingeniería en Agronomía",
            "Ingeniería en Bioprocesos Industriales",
            "Ingeniería en Ciencias Forestales",
            "Ingeniería en Gestión Ambiental",
            "Ingeniería en Topografía y Catastro",
            "Ingeniería Matemática en Ciencias de Datos",
            "Inglés",
            "Lengua Francesa",
            "Literatura y Lingüística con Énfasis en Español",
            "Orientación",
            "Pedagogía con Énfasis en Educación Preescolar y Primera Infancia",
            "Pedagogía con Énfasis en I y II Ciclos de la Educación General Básica",
            "Planificación Económica y Social",
            "Psicología",
            "Química Industrial",
            "Relaciones Internacionales",
            "Sociología",
            "Teología",
            "Música (énfasis en Educación Musical)",
            "Música (énfasis en Viola)",
            "Música (énfasis en Violín)",
            "Música (énfasis en Clarinete)",
            "Música (énfasis en Contrabajo)",
            "Música (énfasis en Flauta Traversa)",
            "Música (énfasis en Guitarra)",
            "Música (énfasis en Percusión)",
            "Música (énfasis en Piano)",
            "Música (énfasis en Dirección Coral)"
        ));

        careerNamesByCampusId.put("benjamin-nunez", List.of(
            "Ciencias del Movimiento Humano",
            "Ingeniería en Sistemas de Información",
            "Inteligencia y Estrategia Global",
            "Medicina Veterinaria"
        ));

        careerNamesByCampusId.put("liberia", List.of(
            "Administración",
            "Comercio y Negocios Internacionales",
            "Gestión Empresarial del Turismo Sostenible",
            "Ingeniería en Calidad y Tecnología de Producción",
            "Ingeniería en Energías Sostenibles",
            "Ingeniería en Sistemas de Información",
            "Inglés",
            "Pedagogía con Énfasis en I y II Ciclos de la Educación General Básica"
        ));

        careerNamesByCampusId.put("nicoya", List.of(
            "Administración",
            "Enseñanza del Inglés",
            "Gestión Empresarial del Turismo Sostenible",
            "Ingeniería Hidrológica",
            "Ingeniería en Calidad e Innovación Agroalimentaria",
            "Ingeniería en Sistemas de Información"
        ));

        careerNamesByCampusId.put("perez-zeledon", List.of(
            "Administración",
            "Enseñanza del Inglés",
            "Gestión Empresarial del Turismo Sostenible",
            "Ingeniería en Calidad e Innovación Agroalimentaria",
            "Ingeniería en Calidad y Tecnología de Producción",
            "Ingeniería en Sistemas de Información",
            "Ingeniería en Tecnologías Computacionales para la Ciencia de Datos",
            "Inglés",
            "Inteligencia y Estrategia Global",
            "Planificación Económica y Social"
        ));

        careerNamesByCampusId.put("coto", List.of(
            "Administración",
            "Enseñanza del Inglés",
            "Ingeniería en Sistemas de Información",
            "Ingeniería en Tecnologías Computacionales para la Ciencia de Datos"
        ));

        careerNamesByCampusId.put("sarapiqui", List.of(
            "Administración",
            "Comercio y Negocios Internacionales",
            "Educación Comercial",
            "Ingeniería en Sistemas de Información",
            "Ingeniería en Tecnologías Computacionales para la Ciencia de Datos",
            "Inglés"
        ));

        careerNamesByCampusId.put("alajuela-interuniversitaria", List.of(
            "Administración",
            "Ingeniería en Sistemas de Información",
            "Inglés",
            "Química Industrial"
        ));

        return Collections.unmodifiableMap(careerNamesByCampusId);
    }

    // Build a unique career index so the same career can be offered at multiple campuses.
    // Output: an immutable map of career IDs to career options.
    private static Map<String, Option> createCareerIndex (){
        Map<String, Option> careersById = new LinkedHashMap<>();

        for (List<String> careerNames : CAREER_NAMES_BY_CAMPUS_ID.values()){
            for (String careerName : careerNames){
                String careerId = createCareerId(careerName);
                Option existingCareer = careersById.get(careerId);

                if (existingCareer != null && !existingCareer.getName().equals(careerName)){
                    throw new IllegalStateException("Two careers generated the same catalog ID.");
                }

                if (existingCareer == null){
                    careersById.put(careerId, new Option(careerId, careerName));
                }
            }
        }

        return Collections.unmodifiableMap(careersById);
    }

    // Build the reverse mapping so a selected career can filter the campus menu.
    // Output: an immutable career-to-campus mapping.
    private static Map<String, List<String>> createCampusIdsByCareer (){
        Map<String, List<String>> campusIdsByCareerId = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> campusEntry : CAREER_NAMES_BY_CAMPUS_ID.entrySet()){
            String campusId = campusEntry.getKey();

            for (String careerName : campusEntry.getValue()){
                String careerId = createCareerId(careerName);
                List<String> campusIds = campusIdsByCareerId.computeIfAbsent(
                    careerId,
                    ignored -> new ArrayList<>()
                );
                campusIds.add(campusId);
            }
        }

        Map<String, List<String>> immutableCampusIdsByCareerId = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : campusIdsByCareerId.entrySet()){
            immutableCampusIdsByCareerId.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        return Collections.unmodifiableMap(immutableCampusIdsByCareerId);
    }

    // Create a consistent career ID from its canonical Spanish catalog name.
    // Inputs: career name; output: a lowercase, accent-free identifier.
    private static String createCareerId (String careerName){
        String normalizedName = Normalizer.normalize(
            careerName.trim().toLowerCase(Locale.ROOT),
            Normalizer.Form.NFD
        );

        return normalizedName
            .replaceAll("\\p{M}+", "")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");
    }

    public static final class Option{
        private final String id;
        private final String name;

        // Create an immutable catalog choice for a campus or career.
        // Inputs: stable ID and Spanish display name; output: one catalog option.
        private Option (String id, String name){
            this.id = id;
            this.name = name;
        }

        // Return the stable ID so account data can store the option without its display text.
        // Output: this option's ID.
        public String getId (){
            return id;
        }

        // Return the Spanish display name so the interface can show the option to the student.
        // Output: this option's display name.
        public String getName (){
            return name;
        }

        // Let JavaFX controls display the Spanish name when no custom converter is set.
        // Output: this option's display name.
        @Override
        public String toString (){
            return name;
        }
    }
}
