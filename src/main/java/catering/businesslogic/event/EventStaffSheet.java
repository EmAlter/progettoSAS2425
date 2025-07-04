package catering.businesslogic.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;
import catering.util.LogManager;
import catering.businesslogic.event.Role;

public class EventStaffSheet {
    private static final Logger LOGGER = LogManager.getLogger(EventStaffSheet.class);

    private String note;
    private List<Role> assignments;
    private Event parentEvent;

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

    public List<Role> getRoles() {
        if (assignments.isEmpty()) {
            LOGGER.warning("No roles assigned in EventStaffSheet for event: " + parentEvent.getId());
        }
        List<Role> roles = new ArrayList<>(assignments);
        LOGGER.fine("Retrieved " + roles.size() + " roles from EventStaffSheet for event: " + parentEvent.getId());
        return roles;
    }

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

    public List<Role> getAssignedRoles() {
        List<Role> assignedRoles = assignments.stream()
                                       .filter(Role::isAssigned)
                                       .collect(Collectors.toList());
        LOGGER.fine("Retrieved " + assignedRoles.size() + " assigned roles from EventStaffSheet for event: " + parentEvent.getId());
        return assignedRoles;
    }

    public List<Role> getUnassignedRoles() {
        List<Role> res = assignments.stream()
                              .filter(r -> !r.isAssigned())
                              .collect(Collectors.toList());
        LOGGER.fine("Retrieved " + res.size() + " unassigned roles from EventStaffSheet for event: " + parentEvent.getId());
        return res;
    }

    public long countAssignedRoles() {
        long count = assignments.stream().filter(Role::isAssigned).count();                               
        LOGGER.fine("Counted " + count + " assigned roles out of " + assignments.size() + " total roles");
        return count;
    }

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
