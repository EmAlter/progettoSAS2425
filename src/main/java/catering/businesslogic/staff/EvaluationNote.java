package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

/**
 * La nota di valutazione non ha l'attributo Staff, 
 * perché viene assegnata al Set di note di un determinato dipendente una volta creata
 */
public class EvaluationNote {
    private int id; // ID per la persistenza
    private String note;
    private int eventId;
    private Timestamp createdDate;

    public EvaluationNote(String note, int eventId) {
        this.note = note;
        this.eventId = eventId;
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }

    // Constructor for loading from DB
    private EvaluationNote() {
    }

    // ===== GETTERS =====
    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public int getEventId() {
        return eventId;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    // ===== SETTERS =====
    public void setId(int id) {
        this.id = id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    // ===== DATABASE OPERATIONS =====

    /**
     * Carica tutte le note di valutazione dal database
     * 
     * @return Lista di tutte le note di valutazione
     */
    public static ArrayList<EvaluationNote> loadAllEvaluationNotes() {
        ArrayList<EvaluationNote> notes = new ArrayList<>();
        String query = "SELECT * FROM EvaluationNotes ORDER BY created_date DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EvaluationNote note = createFromResultSet(rs);
                if (note != null) {
                    notes.add(note);
                }
            }
        });

        return notes;
    }

    /**
     * Carica le note di valutazione per uno staff specifico
     * 
     * @param staffId L'ID dello staff
     * @return Lista delle note di valutazione per lo staff
     */
    public static ArrayList<EvaluationNote> loadByStaffId(int staffId) {
        ArrayList<EvaluationNote> notes = new ArrayList<>();
        String query = "SELECT * FROM EvaluationNotes WHERE staff_id = ? ORDER BY created_date DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EvaluationNote note = createFromResultSet(rs);
                if (note != null) {
                    notes.add(note);
                }
            }
        }, staffId);

        return notes;
    }

    /**
     * Carica le note di valutazione per un evento specifico
     * 
     * @param eventId L'ID dell'evento
     * @return Lista delle note di valutazione per l'evento
     */
    public static ArrayList<EvaluationNote> loadByEventId(int eventId) {
        ArrayList<EvaluationNote> notes = new ArrayList<>();
        String query = "SELECT * FROM EvaluationNotes WHERE event_id = ? ORDER BY created_date DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EvaluationNote note = createFromResultSet(rs);
                if (note != null) {
                    notes.add(note);
                }
            }
        }, eventId);

        return notes;
    }

    /**
     * Carica una nota di valutazione per ID
     * 
     * @param id L'ID della nota
     * @return La nota di valutazione o null se non trovata
     */
    public static EvaluationNote loadById(int id) {
        String query = "SELECT * FROM EvaluationNotes WHERE id = ?";
        final EvaluationNote[] noteHolder = new EvaluationNote[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                noteHolder[0] = createFromResultSet(rs);
            }
        }, id);

        return noteHolder[0];
    }

    /**
     * Helper method per creare una EvaluationNote dal ResultSet
     */
    private static EvaluationNote createFromResultSet(ResultSet rs) throws SQLException {
        try {
            EvaluationNote note = new EvaluationNote();
            note.id = rs.getInt("id");
            note.note = rs.getString("note");
            note.eventId = rs.getInt("event_id");
            note.createdDate = rs.getTimestamp("created_date");
            return note;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Salva una nuova nota di valutazione nel database
     * 
     * @param staffId L'ID dello staff a cui assegnare la nota
     */
    public void saveNewEvaluationNote(int staffId) {
        if (id != 0) {
            return; // Already exists in database
        }

        String query = "INSERT INTO EvaluationNotes (staff_id, event_id, note, created_date) VALUES (?, ?, ?, ?)";

        PersistenceManager.executeUpdate(query, staffId, eventId, note, createdDate);
        id = PersistenceManager.getLastId();
    }
    

    /**
     * Aggiorna una nota di valutazione esistente nel database
     */

    public void updateEvaluationNote() {
        if (id == 0) {
            return; // Not saved in database yet
        }

        String query = "UPDATE EvaluationNotes SET note = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, note, id);
    }
    

    /**
     * Elimina una nota di valutazione dal database
     * 
     * @return true se eliminata con successo, false altrimenti
     */
    public boolean deleteEvaluationNote() {
        if (id == 0) {
            return false; // Not saved in database yet
        }

        String query = "DELETE FROM EvaluationNotes WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, id);

        if (rows > 0) {
            id = 0;
            return true;
        }
        return false;
    }
    

    @Override
    public String toString() {
        return String.format("EvaluationNote [id=%d, eventId=%d, note=%s, createdDate=%s]", 
            id, eventId, note != null ? (note.length() > 50 ? note.substring(0, 50) + "..." : note) : "null", createdDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EvaluationNote other = (EvaluationNote) obj;

        // Se entrambi hanno ID validi, confronta per ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Altrimenti confronta per contenuto
        return eventId == other.eventId && 
               note != null && note.equals(other.note) &&
               createdDate != null && createdDate.equals(other.createdDate);
    }
}