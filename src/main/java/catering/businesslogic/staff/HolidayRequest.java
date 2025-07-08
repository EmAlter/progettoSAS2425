package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.logging.Logger;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

public class HolidayRequest {
    private static final Logger LOGGER = LogManager.getLogger(HolidayRequest.class);

    private int id; // ID per la persistenza
    private Staff requester; // Personale che richiede le ferie
    private Set<Date> requestedDays; // Giorni richiesti per le ferie
    private String description; // Descrizione della richiesta di ferie

    /**
     * Costruttore della classe HolidayRequest.
     * 
     * @param requester Il personale che richiede le ferie.
     * @param requestedDays I giorni richiesti per le ferie.
     * @param description La descrizione della richiesta di ferie.
     */
    public HolidayRequest(Staff requester, Set<Date> requestedDays, String description) {
        if (requestedDays == null || requestedDays.isEmpty()) {
            throw new IllegalArgumentException("Requested days cannot be null or empty");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }
        this.requestedDays = new HashSet<>(requestedDays);
        this.requester = requester;
        this.description = description;
    }

    // Constructor for loading from DB
    private HolidayRequest() {
        this.requestedDays = new HashSet<>();
    }

    // ===== GETTERS =====
    public int getId() {
        return id;
    }

    public Staff getRequester() {
        return requester;
    }

    public String getDescription() {
        return description;
    }

    public Set<Date> getRequestedDays() {
        return new HashSet<>(requestedDays); // Return a copy
    }

    /**
     * Calcola il numero di giorni richiesti per le ferie.
     * 
     * @return Il numero di giorni richiesti per le ferie.
     */
    public int getNumberOfDays() {
        return requestedDays.size();
    }

    // ===== SETTERS =====
    public void setId(int id) {
        this.id = id;
    }

    public void setRequester(Staff requester) {
        this.requester = requester;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequestedDays(Set<Date> requestedDays) {
        if (requestedDays == null || requestedDays.isEmpty()) {
            throw new IllegalArgumentException("Requested days cannot be null or empty");
        }
        this.requestedDays = new HashSet<>(requestedDays);
    }

    // ===== DATABASE OPERATIONS =====

    /**
     * Carica tutte le richieste di ferie dal database
     * 
     * @return Lista di tutte le richieste di ferie
     */
    public static Set<HolidayRequest> loadAllHolidayRequests() {
        Set<HolidayRequest> requests = new HashSet<>();
        String query = "SELECT * FROM HolidayRequests ORDER BY id";

        LOGGER.info("Loading all holiday requests from database");

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                HolidayRequest request = createFromResultSet(rs);
                if (request != null) {
                    requests.add(request);
                }
            }
        });

        // Carica i giorni richiesti per ogni richiesta
        for (HolidayRequest request : requests) {
            request.loadRequestedDays();
        }

        LOGGER.info("Loaded " + requests.size() + " holiday requests");
        return requests;
    }

    /**
     * Carica le richieste di ferie per uno staff specifico
     * 
     * @param staffId L'ID dello staff
     * @return Lista delle richieste di ferie per lo staff
     */
    public static ArrayList<HolidayRequest> loadByStaffId(int staffId) {
        ArrayList<HolidayRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM HolidayRequests WHERE staff_id = ? ORDER BY id";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                HolidayRequest request = createFromResultSet(rs);
                if (request != null) {
                    requests.add(request);
                }
            }
        }, staffId);

        // Carica i giorni richiesti per ogni richiesta
        for (HolidayRequest request : requests) {
            request.loadRequestedDays();
        }

        return requests;
    }

    /**
     * Carica una richiesta di ferie per ID
     * 
     * @param id L'ID della richiesta
     * @return La richiesta di ferie o null se non trovata
     */
    public static HolidayRequest loadById(int id) {
        String query = "SELECT * FROM HolidayRequests WHERE id = ?";
        final HolidayRequest[] requestHolder = new HolidayRequest[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                requestHolder[0] = createFromResultSet(rs);
            }
        }, id);

        HolidayRequest request = requestHolder[0];
        if (request != null) {
            request.loadRequestedDays();
        }

        return request;
    }

    /**
     * Helper method per creare una HolidayRequest dal ResultSet
     */
    private static HolidayRequest createFromResultSet(ResultSet rs) throws SQLException {
        try {
            HolidayRequest request = new HolidayRequest();
            request.id = rs.getInt("id");
            request.description = rs.getString("description");
            
            // Carica lo staff richiedente
            int staffId = rs.getInt("staff_id");
            request.requester = Staff.loadById(staffId);
            
            if (request.requester == null) {
                LOGGER.warning("Could not load staff with ID " + staffId + " for holiday request " + request.id);
                return null;
            }
            
            return request;
        } catch (Exception e) {
            LOGGER.warning("Error creating holiday request from ResultSet: " + e.getMessage());
            return null;
        }
    }

    /**
     * Carica i giorni richiesti per questa richiesta di ferie
     */
    private void loadRequestedDays() {
        if (id == 0) return; // Not saved in database yet

        requestedDays.clear();
        String query = "SELECT requested_date FROM HolidayRequestDays WHERE request_id = ? ORDER BY requested_date";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                try {
                    String dateStr = rs.getString("requested_date");
                    
                    if (dateStr != null && !dateStr.trim().isEmpty()) {
                        try {
                            Date requestedDate = Date.valueOf(dateStr.trim());
                            requestedDays.add(requestedDate);
                            LOGGER.fine("Successfully added date: " + requestedDate + " to request " + id);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warning("Failed to parse date '" + dateStr + "' for holiday request " + id + ": " + e.getMessage());
                        }
                    } else {
                        LOGGER.warning("Null or empty date string for holiday request " + id);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error processing requested date for holiday request " + id + ": " + e.getMessage());
                }
            }
        }, id);
    }

    /**
     * Salva una nuova richiesta di ferie nel database
     */
    public void saveNewHolidayRequest() {
        if (id != 0) {
            LOGGER.warning("Attempting to save holiday request that already exists in database");
            return;
        }

        if (requester == null || requester.getId() == 0) {
            LOGGER.warning("Cannot save holiday request: requester not saved in database");
            return;
        }

        String query = "INSERT INTO HolidayRequests (staff_id, description) VALUES (?, ?)";

        PersistenceManager.executeUpdate(query, requester.getId(), description);
        id = PersistenceManager.getLastId();

        if (id > 0) {
            // Salva i giorni richiesti
            saveRequestedDays();
            LOGGER.info("Saved new holiday request for staff: " + requester.getName() + " (ID: " + id + ")");
        } else {
            LOGGER.severe("Failed to save holiday request for staff: " + requester.getName());
        }
    }
    

    /**
     * Aggiorna una richiesta di ferie esistente nel database
     */
    public void updateHolidayRequest() {
        if (id == 0) {
            LOGGER.warning("Attempting to update holiday request that is not saved in database yet");
            return;
        }

        String query = "UPDATE HolidayRequests SET description = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, description, id);

        if (rows > 0) {
            // Aggiorna i giorni richiesti
            saveRequestedDays();
            LOGGER.info("Updated holiday request: " + id);
        } else {
            LOGGER.warning("Failed to update holiday request: " + id);
        }
    }

    /**
     * Elimina una richiesta di ferie dal database
     * 
     * @return true se eliminata con successo, false altrimenti
     */
    public boolean deleteHolidayRequest() {
        if (id == 0) {
            return false; // Not saved in database yet
        }

        // I giorni richiesti saranno eliminati automaticamente tramite CASCADE

        String query = "DELETE FROM HolidayRequests WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, id);

        if (rows > 0) {
            LOGGER.info("Deleted holiday request: " + id);
            id = 0;
            return true;
        }
        return false;
    }

    /**
     * Salva i giorni richiesti nel database
     */
    private void saveRequestedDays() {
        if (id == 0) return; // Not saved in database yet

        // Prima elimina i giorni esistenti
        String deleteQuery = "DELETE FROM HolidayRequestDays WHERE request_id = ?";
        PersistenceManager.executeUpdate(deleteQuery, id);

        // Poi inserisce i nuovi giorni
        if (!requestedDays.isEmpty()) {
            String insertQuery = "INSERT INTO HolidayRequestDays (request_id, requested_date) VALUES (?, ?)";
            for (Date day : requestedDays) {
                PersistenceManager.executeUpdate(insertQuery, id, day);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("HolidayRequest [id=%d, requester=%s, days=%d, description=%s]", 
            id, 
            requester != null ? requester.getName() : "null", 
            requestedDays.size(), 
            description != null ? (description.length() > 30 ? description.substring(0, 30) + "..." : description) : "null");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        HolidayRequest other = (HolidayRequest) obj;

        // Se entrambi hanno ID validi, confronta per ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Altrimenti confronta per contenuto
        return requester != null && requester.equals(other.requester) &&
               requestedDays.equals(other.requestedDays) &&
               (description == null ? other.description == null : description.equals(other.description));
    }

}