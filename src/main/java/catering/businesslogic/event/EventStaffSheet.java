package catering.businesslogic.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import catering.businesslogic.staff.Staff;
import catering.util.LogManager;
import catering.businesslogic.event.Role;

public class EventStaffSheet {
    private static final Logger LOGGER = LogManager.getLogger(EventStaffSheet.class);

    private String note;
    private Map<Role, Staff> assignments;

    public static EventStaffSheet create() {
        EventStaffSheet sheet = new EventStaffSheet();
        LOGGER.info("Created new EventStaffSheet");
        return sheet;
    }

    public EventStaffSheet() {
        this.note = "";
        this.assignments = new HashMap<>();
        LOGGER.fine("EventStaffSheet instance initialized");
    }

    public void addRole(Role r) {
        if (r == null) {
            LOGGER.warning("Attempted to add null role to EventStaffSheet");
            return;
        }
        
        if (!assignments.containsKey(r)) {
            assignments.put(r, null);   // ruolo creato ma non ancora assegnato
            LOGGER.info("Added role to EventStaffSheet: " + r.getType());
        } else {
            LOGGER.fine("Role already exists in EventStaffSheet: " + r.getType());
        }
    }

    public void setNote(String note) {
        String oldNote = this.note;
        this.note = (note != null) ? note : "";
        
        if (!this.note.equals(oldNote)) {
            LOGGER.info("Updated EventStaffSheet note: " + 
                       (this.note.length() > 50 ? this.note.substring(0, 50) + "..." : this.note));
        }
    }

    public String getNote() {
        return note;
    }

    public boolean containsRole(Role r) {
        boolean contains = assignments.containsKey(r);
        LOGGER.fine("Role " + (r != null ? r.getType() : "null") + 
                   " " + (contains ? "found" : "not found") + " in EventStaffSheet");
        return contains;
    }

    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>(assignments.keySet());
        LOGGER.fine("Retrieved " + roles.size() + " roles from EventStaffSheet");
        return roles;
    }

    public void assignStaffToRole(Role r, Staff s) {
        if (r == null) {
            LOGGER.warning("Attempted to assign staff to null role");
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        if (s == null) {
            LOGGER.warning("Attempted to assign null staff to role: " + r.getType());
            throw new IllegalArgumentException("Staff cannot be null");
        }
        
        if (containsRole(r)) {
            Staff previousStaff = assignments.get(r);
            assignments.put(r, s);  // assegna il personale al ruolo
            
            if (previousStaff != null) {
                LOGGER.info("Reassigned role " + r.getType() + " from " + 
                           previousStaff.getName() + " to " + s.getName());
            } else {
                LOGGER.info("Assigned staff " + s.getName() + " to role " + r.getType());
            }
        } else {
            String errorMsg = "Role not found in the event staff sheet: " + r.getType();
            LOGGER.warning(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public void removeStaffFromRole(Role r, Staff s) {
        if (r == null) {
            LOGGER.warning("Attempted to remove staff from null role");
            return;
        }
        
        if (s == null) {
            LOGGER.warning("Attempted to remove null staff from role: " + r.getType());
            return;
        }
        
        if (containsRole(r) && assignments.get(r) == s) {
            assignments.put(r, null); // mantiene il ruolo ma rimuove l'assegnazione
            LOGGER.info("Removed staff " + s.getName() + " from role " + r.getType());
        } else {
            LOGGER.warning("Attempted to remove staff " + s.getName() + 
                          " from role " + r.getType() + 
                          " but assignment not found or staff mismatch");
        }
    }

    public Staff getStaffForRole(Role r) {
        if (r == null) {
            LOGGER.warning("Attempted to get staff for null role");
            return null;
        }
        
        Staff staff = assignments.get(r);
        LOGGER.fine("Retrieved staff for role " + r.getType() + ": " + 
                   (staff != null ? staff.getName() : "unassigned"));
        return staff;
    }

    public Map<Role, Staff> getAllAssignments() {
        LOGGER.fine("Retrieved all assignments: " + assignments.size() + " roles");
        return new HashMap<>(assignments);
    }

    public int getAssignedRoleCount() {
        int count = 0;
        for (Staff staff : assignments.values()) {
            if (staff != null) {
                count++;
            }
        }
        LOGGER.fine("Counted " + count + " assigned roles out of " + assignments.size() + " total roles");
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EventStaffSheet [");
        sb.append("roles: ").append(assignments.size());
        sb.append(", assigned: ").append(getAssignedRoleCount());
        sb.append(", note: ").append(note.length() > 0 ? "present" : "empty");
        sb.append("]");
        return sb.toString();
    }    
}
