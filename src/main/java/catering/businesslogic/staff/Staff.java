package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.time.LocalDate;

import java.util.logging.Logger;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

public abstract class Staff {
    private static final Logger LOGGER = LogManager.getLogger(Staff.class);

    private int id; // ID univoco per la persistenza
    private JobType jobType;
    private boolean permanent;
    private String name;
    private String address;
    private String contact;
    private String fiscalCode;
    private int remainingDaysOff;
    
    // Collections mantenute in memoria (come Event.services)
    private Set<LocalDate> holidays;
    private Set<EvaluationNote> evaluationNotes;

    /**
     * Costruttore della classe Staff.
     * 
     * @param jobType Il tipo di lavoro del personale.
     * @param permanent Indica se il personale è a tempo indeterminato.
     * @param name Il nome del personale.
     * @param address L'indirizzo del personale.
     * @param contact Le informazioni di contatto del personale.
     * @param fiscalCode Il codice fiscale del personale.
     */
    public Staff(JobType jobType, boolean permanent, String name, String address, String contact, String fiscalCode) {
        this.jobType = jobType;
        this.permanent = permanent;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.fiscalCode = fiscalCode;
        this.remainingDaysOff = 30; // Inizializza i giorni di ferie rimanenti a 30
        this.holidays = new HashSet<>(); // Inizializza le ferie come un set vuoto e mutabile
        this.evaluationNotes = new HashSet<>(); // Inizializza le note di valutazione come un set vuoto e mutabile
    }

    // Default constructor for loading from DB
    protected Staff() {
        this.holidays = new HashSet<>();
        this.evaluationNotes = new HashSet<>();
    }

    // ===== GETTERS =====
    public int getId() {
        return id;
    }

    public JobType getJobType() {
        return jobType;
    }

    public boolean isPermanent() {
        return permanent;
    }
    
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public int getRemainingHolidays() {
        return remainingDaysOff;
    }

    public Set<LocalDate> getHolidays() {
        return new HashSet<>(holidays); // Return a copy
    }

    public Set<EvaluationNote> getEvaluationNotes() {
        return new HashSet<>(evaluationNotes); // Return a copy
    }

    // ===== SETTERS =====
    public void setId(int id) {
        this.id = id;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.address = address;
    }

    public void setContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact cannot be null or empty");
        }
        this.contact = contact;
    }

    public void setFiscalCode(String fiscalCode) {
        if (fiscalCode == null || fiscalCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Fiscal code cannot be null or empty");
        }
        this.fiscalCode = fiscalCode;
    }

    public void setRemainingDaysOff(int remainingDaysOff) {
        if (remainingDaysOff < 0) {
            throw new IllegalArgumentException("Remaining days off cannot be negative");
        }
        this.remainingDaysOff = remainingDaysOff;
    }

    // ===== BUSINESS METHODS =====

    public EvaluationNote createNewEvaluationNote(String description, int eventId) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Note cannot be null or empty");
        }
        EvaluationNote evaluationNote = new EvaluationNote(description.trim(), eventId);
        this.evaluationNotes.add(evaluationNote); // Add the note to the staff's collection

        return evaluationNote;
    }

    public void reduceHolidays(int daysToReduce) {
        if (daysToReduce < 0) {
            throw new IllegalArgumentException("Days to reduce cannot be negative");
        }
        if (daysToReduce > this.remainingDaysOff) {
            throw new IllegalArgumentException("Cannot reduce more days than available");
        }
        this.remainingDaysOff -= daysToReduce;
    }

    public void addHolidays(Set<LocalDate> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("Holidays cannot be null or empty");
        }
        for (LocalDate day : days) {
            if (day == null) {
                throw new IllegalArgumentException("Holiday date cannot be null");
            }
            holidays.add(day);
        }
    }

    public boolean isAvailable(Set<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            throw new IllegalArgumentException("Dates cannot be null or empty");
        }

        for (LocalDate date : dates) {
            if (holidays.contains(date)) {
                return false; // Il personale non è disponibile se ha ferie in queste date
            }
        }
        return true; // Il personale è disponibile se non ha ferie nelle date specificate
    }

    /**
     * Questo membro del personale crea una richiesta di ferie.
     * 
     * @param dates Le date richieste per le ferie.
     * @param description Una descrizione della richiesta di ferie.
     * @return Una nuova istanza di HolidayRequest.
     * @throws IllegalArgumentException Se le date sono nulle o vuote.
     */
    public HolidayRequest createHolidayRequest(Set<LocalDate> dates, String description) {
        if (dates == null || dates.isEmpty()) {
            throw new IllegalArgumentException("Dates cannot be null or empty");
        }
        return new HolidayRequest(this, dates, description);
    }


    // ===== DATABASE OPERATIONS =====

    /**
     * Carica tutto il personale dal database
     * 
     * @return Lista di tutto il personale
     */
    public static ArrayList<Staff> loadAllStaff() {
        ArrayList<Staff> staffList = new ArrayList<>();
        String query = "SELECT * FROM Staff ORDER BY name";

        LOGGER.info("Loading all staff from database");

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff staff = createStaffFromResultSet(rs);
                if (staff != null) {
                    staffList.add(staff);
                }
            }
        });

        // Carica le ferie per ogni membro del personale
        for (Staff staff : staffList) {
            staff.loadHolidays();
            staff.loadEvaluationNotes();
        }

        LOGGER.info("Loaded " + staffList.size() + " staff members");
        return staffList;
    }

    /**
     * Carica un membro del personale per ID
     * 
     * @param id L'ID del membro del personale
     * @return Il membro del personale o null se non trovato
     */
    public static Staff loadById(int id) {
        String query = "SELECT * FROM Staff WHERE id = ?";
        return loadStaffByQuery(query, id);
    }

    /**
     * Carica un membro del personale per codice fiscale
     * 
     * @param fiscalCode Il codice fiscale da cercare
     * @return Il membro del personale o null se non trovato
     */
    public static Staff loadByFiscalCode(String fiscalCode) {
        String query = "SELECT * FROM Staff WHERE fiscal_code = ?";
        return loadStaffByQuery(query, fiscalCode);
    }

    /**
     * Carica un membro del personale per nome
     * 
     * @param name Il nome da cercare
     * @return Il membro del personale o null se non trovato
     */
    public static Staff loadByName(String name) {
        String query = "SELECT * FROM Staff WHERE name = ?";
        return loadStaffByQuery(query, name);
    }

    /**
     * Carica il personale per tipo di lavoro
     * 
     * @param jobType Il tipo di lavoro
     * @return Lista del personale di quel tipo
     */
    public static ArrayList<Staff> loadByJobType(JobType jobType) {
        ArrayList<Staff> staffList = new ArrayList<>();
        String query = "SELECT * FROM Staff WHERE job_type = ? ORDER BY name";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff staff = createStaffFromResultSet(rs);
                if (staff != null) {
                    staffList.add(staff);
                }
            }
        }, jobType.toString());

        // Carica le ferie per ogni membro del personale
        for (Staff staff : staffList) {
            staff.loadHolidays();
            staff.loadEvaluationNotes();
        }

        return staffList;
    }

    /**
     * Helper method per caricare il personale tramite query
     */
    private static Staff loadStaffByQuery(String query, Object param) {
        final Staff[] staffHolder = new Staff[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                staffHolder[0] = createStaffFromResultSet(rs);
            }
        }, param);

        Staff staff = staffHolder[0];
        if (staff != null) {
            staff.loadHolidays();
            staff.loadEvaluationNotes();
        }

        return staff;
    }

    /**
     * Crea un oggetto Staff da un ResultSet
     * @param rs
     * @return
     * @throws SQLException
     */
    private static Staff createStaffFromResultSet(ResultSet rs) throws SQLException {
        try {
            JobType jobType = JobType.valueOf(rs.getString("job_type"));
            boolean permanent = rs.getBoolean("permanent");
            String name = rs.getString("name");
            String address = rs.getString("address");
            String contact = rs.getString("contact");
            String fiscalCode = rs.getString("fiscal_code");

            Staff staff = StaffFactory.createStaff(jobType, permanent, name, address, contact, fiscalCode);
            staff.setId(rs.getInt("id"));
            staff.setRemainingDaysOff(rs.getInt("remaining_days_off"));

            return staff;
        } catch (Exception e) {
            LOGGER.warning("Error creating staff from ResultSet: " + e.getMessage());
            return null;
        }
    }

    /**
     * Salva un nuovo membro del personale nel database
     */
    public void saveNewStaff() {
        if (id != 0) {
            LOGGER.warning("Attempting to save staff that already exists in database: " + name);
            return;
        }

        String query = "INSERT INTO Staff (name, contact, address, fiscal_code, permanent, job_type, remaining_days_off) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeUpdate(query, 
            name, 
            contact, 
            address, 
            fiscalCode, 
            permanent ? 1 : 0, 
            jobType.toString(), 
            remainingDaysOff);

        id = PersistenceManager.getLastId();

        if (id > 0) {
            // Salva le ferie se presenti
            saveHolidays();
            // Salva le note di valutazione se presenti
            saveEvaluationNotes();
            LOGGER.info("Saved new staff member: " + name + " (ID: " + id + ")");
        } else {
            LOGGER.severe("Failed to save staff member: " + name);
        }
    }

    /**
     * Aggiorna un membro del personale esistente nel database
     */
    public void updateStaff() {
        if (id == 0) {
            LOGGER.warning("Attempting to update staff that is not saved in database yet: " + name);
            return;
        }

        String query = "UPDATE Staff SET name = ?, contact = ?, address = ?, fiscal_code = ?, permanent = ?, job_type = ?, remaining_days_off = ? WHERE id = ?";

        int rows = PersistenceManager.executeUpdate(query, 
            name, 
            contact, 
            address, 
            fiscalCode, 
            permanent ? 1 : 0, 
            jobType.toString(), 
            remainingDaysOff, 
            id);

        if (rows > 0) {
            // Aggiorna le ferie
            saveHolidays();
            // Aggiorna le note di valutazione
            saveEvaluationNotes();
            LOGGER.info("Updated staff member: " + name + " (ID: " + id + ")");
        } else {
            LOGGER.warning("Failed to update staff member: " + name + " (ID: " + id + ")");
        }
    }

    /**
     * Elimina un membro del personale dal database
     * 
     * @return true se eliminato con successo, false altrimenti
     */
    public boolean deleteStaff() {
        if (id == 0) {
            LOGGER.warning("Attempting to delete staff that is not saved in database yet: " + name);
            return false;
        }

        // Le ferie e le note di valutazione saranno eliminate automaticamente tramite CASCADE

        String query = "DELETE FROM Staff WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, id);

        if (rows > 0) {
            LOGGER.info("Deleted staff member: " + name + " (ID: " + id + ")");
            id = 0;
            return true;
        } else {
            LOGGER.warning("Failed to delete staff member: " + name + " (ID: " + id + ")");
            return false;
        }
    }

    /**
     * Carica le ferie per questo membro del personale
     */
    private void loadHolidays() {
        if (id == 0) return; 

        holidays.clear();
        String query = "SELECT holiday_date FROM StaffHolidays WHERE staff_id = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                try {
                    String dateStr = rs.getString("holiday_date");
                    if (dateStr != null && !dateStr.trim().isEmpty()) {
                        LocalDate holidayDate = LocalDate.parse(dateStr.trim());
                        holidays.add(holidayDate);
                        LOGGER.fine("Loaded holiday date: " + holidayDate + " for staff " + id);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error parsing holiday date for staff " + id + ": " + e.getMessage());
                }
            }
        }, id);
    }

    /**
     * Salva le ferie nel database
     */
    private void saveHolidays() {
        if (id == 0) return; 

        // Prima elimina le ferie esistenti
        String deleteQuery = "DELETE FROM StaffHolidays WHERE staff_id = ?";
        PersistenceManager.executeUpdate(deleteQuery, id);

        // Poi inserisce le nuove ferie
        if (!holidays.isEmpty()) {
            String insertQuery = "INSERT INTO StaffHolidays (staff_id, holiday_date) VALUES (?, ?)";
            for (LocalDate holiday : holidays) {
                if (holiday == null) {
                    LOGGER.warning("Skipping null holiday date for staff " + id);
                    continue; // Skip null dates
                }
                String dateStr = holiday.toString();
                PersistenceManager.executeUpdate(insertQuery, id, dateStr);
            }
        }
    }

    /**
     * Carica le note di valutazione per questo membro del personale
     */
    private void loadEvaluationNotes() {
        if (id == 0) return; 

        evaluationNotes.clear();
        String query = "SELECT * FROM EvaluationNotes WHERE staff_id = ? ORDER BY created_date DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                try {
                    String note = rs.getString("note");
                    int eventId = rs.getInt("event_id");
                    EvaluationNote evalNote = new EvaluationNote(note, eventId);
                    evaluationNotes.add(evalNote);
                } catch (Exception e) {
                    LOGGER.warning("Error loading evaluation note for staff " + id + ": " + e.getMessage());
                }
            }
        }, id);
    }

    /**
     * Salva le note di valutazione nel database
     */
    private void saveEvaluationNotes() {
        if (id == 0) return; // Not saved in database yet

        // Prima elimina le note esistenti
        String deleteQuery = "DELETE FROM EvaluationNotes WHERE staff_id = ?";
        PersistenceManager.executeUpdate(deleteQuery, id);

        // Poi inserisce le nuove note
        if (!evaluationNotes.isEmpty()) {
            String insertQuery = "INSERT INTO EvaluationNotes (staff_id, event_id, note) VALUES (?, ?, ?)";
            for (EvaluationNote note : evaluationNotes) {
                PersistenceManager.executeUpdate(insertQuery, id, note.getEventId(), note.getNote());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Staff [id=%d, name=%s, jobType=%s, permanent=%s, fiscalCode=%s]", 
            id, name, jobType, permanent, fiscalCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Staff other = (Staff) obj;

        // Se entrambi hanno ID validi, confronta per ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Altrimenti confronta per codice fiscale (dovrebbe essere univoco)
        return fiscalCode != null && fiscalCode.equals(other.fiscalCode);
    }
}