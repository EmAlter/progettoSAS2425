package catering.businesslogic.event;

import java.util.UUID;

import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;

public class Role {
    private UUID id; // ID univoco del ruolo
    private String description; // Descrizione del ruolo (es. "Addetto alla griglia", "Addetto alla sala", ecc.)
    private JobType type; // CUOCO, CHEF, SERVIZIO
    private Staff assignedStaff; // Staff assegnato a questo ruolo
    private EventStaffSheet sheet;

    /**
     * Crea un nuovo ruolo con ID univoco e tipo specificato.
     * @param description Descrizione del ruolo (es. "Addetto alla griglia", "Addetto alla sala", ecc.)
     * @param type Tipo di lavoro associato al ruolo (CUOCO, CHEF, SERVIZIO)
     */
    public Role(String description, JobType type) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.type = type;
        this.assignedStaff = null; // Staff assegnato a questo ruolo = null;
    }

    public JobType getType() {
        return type;
    }

    public boolean isStaffAssignedToRole() {
        return assignedStaff != null;
    }

    // ... altri getter/setter
}