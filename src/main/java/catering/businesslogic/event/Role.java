package catering.businesslogic.event;

import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;

public class Role {
    private int id; // ID univoco del ruolo assegnato dal db
    private String description; // Descrizione del ruolo (es. "Addetto alla griglia", "Addetto alla sala", ecc.)
    private JobType type; // CUOCO, CHEF, SERVIZIO
    private Staff assignedStaff; // Staff assegnato a questo ruolo

    /**
     * Crea un nuovo ruolo con ID univoco e tipo specificato.
     * @param description Descrizione del ruolo (es. "Addetto alla griglia", "Addetto alla sala", ecc.)
     * @param type Tipo di lavoro associato al ruolo (CUOCO, CHEF, SERVIZIO)
     */
    public Role(String description, JobType type) {
        this.description = description;
        this.type = type;
        this.assignedStaff = null; // il ruolo non ha staff assegnato inizialmente
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JobType getType() {
        return type;
    }

    public boolean isAssigned() {
        return assignedStaff != null;
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public String getDescription() {
        return description;
    }

    public void removeStaff() {
        if (assignedStaff != null) {
            assignedStaff = null; // Rimuove l'assegnazione dello staff a questo ruolo
        }
    }

}