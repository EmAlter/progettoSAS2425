package catering.businesslogic.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;
import catering.util.LogManager;

public class EventStaffSheet {
    private static final Logger LOGGER = LogManager.getLogger(EventStaffSheet.class);

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
        LOGGER.fine("EventStaffSheet created.");
        LOGGER.info("Current state: " + this.toString());
    }

    public void addRole(String description, JobType type) {
        Role newRole = new Role(description, type);
        assignments.add(newRole);
        LOGGER.info("Added new role: " + description + " of type: " + type + " to EventStaffSheet.");
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
    public Role findRoleById(UUID id) {
        Role target = assignments.stream()
                        .filter(r -> r.getId().equals(id))
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
}
