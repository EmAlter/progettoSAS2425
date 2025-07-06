package catering.businesslogic.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

public class EventStaffSheet {
    private static final Logger LOGGER = LogManager.getLogger(EventStaffSheet.class);

    private int id; // ID della scheda riepilogativa -> è lo stesso ID dell'evento
    private String note; //note generali sull'evento
    private List<Role> assignments; //lista di ruoli di un evento
    private Event parentEvent; //l'evento a cui è associato il foglio riepilogativo
    
    public EventStaffSheet(Event event) {
        if (event == null) {
            LOGGER.severe("Event cannot be null when creating EventStaffSheet");
            throw new IllegalArgumentException("Event cannot be null");
        }
        this.note = "";
        this.assignments = new ArrayList<>();
        this.parentEvent = event;
        this.id = event.getId();
        LOGGER.fine("EventStaffSheet created.");
        LOGGER.info("Current state: " + this.toString());
    }

    public void addRole(Role role) {
        assignments.add(role);
        LOGGER.info("Added new role: " + role.getDescription() + " of type: " + role.getType() + " to EventStaffSheet.");
        LOGGER.info("Current state: " + this.toString());
    }

    public void setNote(String note) {
        String oldNote = this.note;
        this.note = (note != null) ? note : "";
        
        if (!this.note.equals(oldNote)) {
            LOGGER.info("Updated EventStaffSheet note: " + 
                       (this.note.length() > 50 ? this.note.substring(0, 50) + "..." : this.note));
        }
        LOGGER.info("Current state: " + this.toString());
    }

    public String getNote() {
        return note;
    }

    /**
     * Ritorna la lista di ruoli assegnati a questo evento.
     * Se non ci sono ruoli assegnati, viene registrato un avviso.
     * 
     * @return Lista di ruoli assegnati all'evento.
     */
    public List<Role> getRoles() {
        if (assignments.isEmpty()) {
            LOGGER.warning("No roles assigned in EventStaffSheet for event: " + parentEvent.getId());
        }
        List<Role> roles = new ArrayList<>(assignments);
        LOGGER.fine("Retrieved " + roles.size() + " roles from EventStaffSheet for event: " + parentEvent.getId());
        return roles;
    }

    /**
     * Assegna uno staff a un ruolo specifico.
     * Se il ruolo è già assegnato, viene registrato un avviso e l'assegnazione non viene effettuata.
     * 
     * @param r il ruolo a cui assegnare lo staff
     * @param s lo staff da assegnare al ruolo
     */
    public void assignStaffToRole(Role r, Staff s) {
        if (r != null && s != null) {
            if (r.isAssigned()) {
                LOGGER.warning("Role " + r.getDescription() + " is already assigned.");
                return;
            }
            r.setStaff(s);
            LOGGER.info("Assigned staff " + s.getName() + " to role " + r.getDescription());
            LOGGER.info("Current state: " + this.toString());
        } else {
            LOGGER.warning("Attempted to assign null staff or to a null role.");
        }
    }

    /**
     * Rimuove l'assegnazione dello staff da un ruolo specifico.
     * Se il ruolo non è assegnato, viene registrato un messaggio di log informativo.
     * 
     * @param r il ruolo da cui rimuovere l'assegnazione dello staff
     */
    public void removeAssignment(Role r) {
        if (r != null) {
            if (r.isAssigned()) {
                Staff previouslyAssigned = r.getAssignedStaff();
                r.removeStaff();
                LOGGER.info("Removed staff " + previouslyAssigned.getName() + " from role " + r.getDescription());
                LOGGER.info("Current state: " + this.toString());
            } else {
                LOGGER.fine("Role " + r.getDescription() + " was not assigned, no action needed.");
            }
        } else {
            LOGGER.warning("Attempted to remove assignment from a null role.");
        }
    }

    /**
     * Ritorna lo staff assegnato a un ruolo specifico.
     * Se il ruolo non è assegnato, viene registrato un messaggio di log informativo.
     * 
     * @param r il ruolo di cui ottenere lo staff assegnato
     * @return lo staff assegnato al ruolo, o null se non è assegnato
     */
    public Staff getStaffForRole(Role r) {
        if (r != null) {
            if (r.isAssigned()) {
                LOGGER.fine("Retrieved staff for role " + r.getDescription());
                return r.getAssignedStaff();
            } else {
                LOGGER.fine("Role " + r.getDescription() + " has no assigned staff.");
                return null; // Nessuno staff assegnato
            }
        }
        LOGGER.warning("Attempted to get staff for a null role.");
        return null;
    }

    /**
     * Ritorna la lista di ruoli già assegnati a questo evento.
     * 
     * @return Lista di ruoli assegnati dell'evento.
     */
    public List<Role> getAssignedRoles() {
        List<Role> assignedRoles = assignments.stream()
                                       .filter(Role::isAssigned)
                                       .collect(Collectors.toList());
        LOGGER.fine("Retrieved " + assignedRoles.size() + " assigned roles from EventStaffSheet for event: " + parentEvent.getId());
        return assignedRoles;
    }

    /**
     * Ritorna la lista di ruoli non assegnati a questo evento.
     * 
     * @return Lista di ruoli non ancora assegnati dell'evento.
     */
    public List<Role> getUnassignedRoles() {
        List<Role> res = assignments.stream()
                              .filter(r -> !r.isAssigned())
                              .collect(Collectors.toList());
        LOGGER.fine("Retrieved " + res.size() + " unassigned roles from EventStaffSheet for event: " + parentEvent.getId());
        return res;
    }

    /**
     * Conta il numero di ruoli assegnati dell'evento.
     * 
     * @return Numero di ruoli assegnati.
     */
    public long countAssignedRoles() {
        long count = assignments.stream().filter(Role::isAssigned).count();                               
        LOGGER.fine("Counted " + count + " assigned roles out of " + assignments.size() + " total roles");
        return count;
    }

    /**
     * Trova un ruolo specifico in base al tipo e alla descrizione.
     * Se il ruolo non viene trovato, viene registrato un avviso.
     * 
     * @param type Tipo di lavoro del ruolo (CUOCO, CHEF, SERVIZIO)
     * @param description Descrizione del ruolo
     * @return Il ruolo trovato o null se non esiste
     */
    public Role findRole(JobType type, String description) {
        Role target = assignments.stream()
                      .filter(r -> r.getType() == type && r.getDescription().equalsIgnoreCase(description))
                      .findFirst()
                      .orElse(null);
        if (target == null) {
            LOGGER.warning("Cannot find role of type " + type + " with description '" + description + "' in EventStaffSheet for event: " + parentEvent.getId());
        }
        else {
            LOGGER.fine("Found role: " + target.getDescription() + " of type " + type + " in EventStaffSheet for event: " + parentEvent.getId());
        }
        return target;
    }

    /**
     * Trova un ruolo specifico in base al suo ID.
     * Se il ruolo non viene trovato, viene registrato un avviso.
     * 
     * @param id ID del ruolo da cercare
     * @return Il ruolo trovato o null se non esiste
     */
    public Role findRoleById(int id) {
        Role target = assignments.stream()
                        .filter(r -> r.getId() == id)
                        .findFirst()
                        .orElse(null);
        if (target == null) {
            LOGGER.warning("Cannot find role with id " + id + "' in EventStaffSheet for event: " + parentEvent.getId());
        }
        else {
            LOGGER.fine("Found role " + id + " in EventStaffSheet for event: " + parentEvent.getId());
        }
        return target;
    }

    /**
     * Ritorna una lista di ruoli non assegnati di un certo tipo.
     * Se non ci sono ruoli di quel tipo non assegnati, viene registrato un avviso.
     * 
     * @param type Tipo di lavoro del ruolo (CUOCO, CHEF, SERVIZIO)
     * @return Lista di ruoli non assegnati del tipo specificato
     */
    public List<Role> getNotAssignedByType(JobType type) {
        List<Role> res = assignments.stream()
                .filter(r -> r.getType() == type && !r.isAssigned())
                .collect(Collectors.toList());
        LOGGER.fine("Found " + res.size() + " roles of type " + type + " that are not assigned in EventStaffSheet for event: " + parentEvent.getId());
        return res;
    } 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EventStaffSheet [");
        sb.append("eventId: ").append(parentEvent.getId());
        sb.append(", roles: ").append(assignments.size());
        sb.append(", assigned: ").append(countAssignedRoles());
        sb.append(", note: ").append(note.length() > 0 ? "present" : "empty");
        sb.append("]");
        return sb.toString();
    }

    // Database operations

    /**
     * Carica una EventStaffSheet per un evento specifico
     */
    public static EventStaffSheet loadByEventId(int eventId) {
        String query = "SELECT * FROM EventStaffSheets WHERE id = ?";
        EventStaffSheet[] sheetHolder = new EventStaffSheet[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Event event = Event.loadById(eventId);
                if (event != null) {
                    EventStaffSheet sheet = new EventStaffSheet(event);
                    sheet.id = rs.getInt("id");  // Imposta l'ID
                    sheet.note = rs.getString("note");
                    sheetHolder[0] = sheet;
                }
            }
        }, eventId);

        EventStaffSheet sheet = sheetHolder[0];
        if (sheet != null) {
            sheet.assignments = loadRolesForEvent(eventId);
        }

        return sheet;
    }

    /**
     * Carica tutte le EventStaffSheet
     */
    public static List<EventStaffSheet> loadAllEventStaffSheets() {
        String query = "SELECT * FROM EventStaffSheets ORDER BY id";
        List<EventStaffSheet> sheets = new ArrayList<>();
        List<Integer> eventIds = new ArrayList<>();

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                int eventId = rs.getInt("id");
                Event event = Event.loadById(eventId);
                if (event != null) {
                    EventStaffSheet sheet = new EventStaffSheet(event);
                    sheet.id = eventId;  // Imposta l'ID
                    sheet.note = rs.getString("note");
                    sheets.add(sheet);
                    eventIds.add(eventId);
                }
            }
        });

        // Carica i ruoli per ogni sheet
        for (int i = 0; i < sheets.size(); i++) {
            sheets.get(i).assignments = loadRolesForEvent(eventIds.get(i));
        }

        return sheets;
    }

    /**
     * Carica i ruoli per un evento specifico
     */
    private static List<Role> loadRolesForEvent(int eventId) {
        String query = "SELECT * FROM EventRoles WHERE event_id = ? ORDER BY id";
        List<Role> roles = new ArrayList<>();

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                String description = rs.getString("description");
                String jobTypeStr = rs.getString("job_type");
                JobType jobType = JobType.valueOf(jobTypeStr);
                
                Role role = new Role(description, jobType);
                role.setId(rs.getInt("id"));
                
                int staffId = rs.getInt("staff_id");
                if (staffId > 0 && !rs.wasNull()) {
                    // Carica lo staff assegnato (dovrai implementare Staff.loadById)
                    Staff staff = Staff.loadById(staffId);
                    if (staff != null) {
                        role.setStaff(staff);
                    }
                }
                
                roles.add(role);
            }
        }, eventId);

        return roles;
    }

    /**
     * Salva una nuova EventStaffSheet nel database
     */
    public void saveNewEventStaffSheet() {
        if (parentEvent == null) {
            LOGGER.severe("Cannot save EventStaffSheet without parent event");
            return;
        }

        this.id = parentEvent.getId();

        String query = "INSERT INTO EventStaffSheets (id, note) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, this.id, note);
        
        LOGGER.info("Saved new EventStaffSheet for event ID: " + id);
    }

    /**
     * Aggiorna una EventStaffSheet esistente
     */
    public void updateEventStaffSheet() {
        String query = "UPDATE EventStaffSheets SET note = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, note, id);
        
        LOGGER.info("Updated EventStaffSheet for event ID: " + id);
    }

    /**
     * Elimina una EventStaffSheet dal database
     */
    public boolean deleteEventStaffSheet() {
        // Prima elimina i ruoli
        String deleteRolesQuery = "DELETE FROM EventRoles WHERE event_id = ?";
        PersistenceManager.executeUpdate(deleteRolesQuery, id);

        // Poi elimina la sheet
        String deleteSheetQuery = "DELETE FROM EventStaffSheets WHERE id = ?";
        boolean success = PersistenceManager.executeUpdate(deleteSheetQuery, id) > 0;

        if (success) {
            LOGGER.info("Deleted EventStaffSheet for event ID: " + id);
        }

        return success;
    }

    /**
     * Salva un nuovo ruolo nel database
     */
    public void saveNewRole(Role role) {
        if (parentEvent == null || role == null) {
            LOGGER.warning("Cannot save role: missing parent event or role is null");
            return;
        }

        String query = "INSERT INTO EventRoles (event_id, description, job_type, staff_id) VALUES (?, ?, ?, ?)";
        
        Integer staffId = null;
        
        if (role.isAssigned()) {
            staffId = role.getAssignedStaff().getId();
        }

        PersistenceManager.executeUpdate(query, 
            this.id,
            role.getDescription(),
            role.getType().toString(),
            staffId
        );

        // Assegna l'ID generato dal database
        role.setId(PersistenceManager.getLastId());
        
        LOGGER.info("Saved new role: " + role.getDescription() + " for event ID: " + parentEvent.getId());
    }

    /**
     * Aggiorna l'assegnazione di un ruolo nel database (assegna staff)
     */
    public void updateRoleAssignment(Role role) {
        if (parentEvent == null || role == null || !role.isAssigned()) {
            LOGGER.warning("Cannot update role assignment: missing data or role not assigned");
            return;
        }

        String query = "UPDATE EventRoles SET staff_id = ? WHERE id = ?";
        
        int rowsAffected = PersistenceManager.executeUpdate(query, 
            role.getAssignedStaff().getId(),
            role.getId()
        );
        
        if (rowsAffected > 0) {
            LOGGER.info("Updated role assignment in database for role ID: " + role.getId());
        } else {
            LOGGER.warning("No rows affected when updating role assignment for role ID: " + role.getId());
        }
    }

    /**
     * Aggiorna l'assegnazione di un ruolo nel database (rimuove lo staff assegnato)
     */
    public void updateRoleUnassignment(Role role) {
        if (parentEvent == null || role == null) {
            LOGGER.warning("Cannot update role unassignment: missing parent event or role is null");
            return;
        }

        // Trova il ruolo nel database e rimuovi l'assegnazione
        String query = "UPDATE EventRoles SET staff_id = NULL WHERE id = ?";
        
        int rowsAffected = PersistenceManager.executeUpdate(query, role.getId());
        
        if (rowsAffected > 0) {
            LOGGER.info("Updated role unassignment in database for role ID: " + role.getId());
        } else {
            LOGGER.warning("No rows affected when updating role unassignment for role ID: " + role.getId());
        }
    }
    
}
