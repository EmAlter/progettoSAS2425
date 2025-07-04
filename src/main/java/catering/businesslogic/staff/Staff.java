package catering.businesslogic.staff;

import java.util.Set;
import java.util.Date;

public abstract class Staff {
    private JobType jobType;
    private boolean permanent;
    private String name;
    private String address;
    private String contact;
    private String fiscalCode;
    private int remainingDaysOff;
    private Set<Date> holidays;
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
        this.holidays = Set.of(); // Inizializza le ferie come un set vuoto
        this.evaluationNotes = Set.of(); // Inizializza le note di valutazione come un set vuoto
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

    public int getRemainingDaysOff() {
        return remainingDaysOff;
    }

    public Set<Date> getHolidays() {
        return holidays;
    }

    public Set<EvaluationNote> getEvaluationNotes() {
        return evaluationNotes;
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

    public void reduceHolidays(int remainingDaysOff) {
        if (remainingDaysOff < 0) {
            throw new IllegalArgumentException("Remaining days off cannot be negative");
        }
        this.remainingDaysOff = remainingDaysOff;
    }

    public void addHoliday(Date holiday) {
        if (holiday == null) {
            throw new IllegalArgumentException("Holiday date cannot be null");
        }
        if (holidays.contains(holiday)) {
            throw new IllegalArgumentException("Holiday already exists");
        }
        holidays.add(holiday);
    }

    public void AddNote(EvaluationNote note) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        evaluationNotes.add(note);
    }

    public boolean isAvailable(Set<Date> dates) {
        if( dates == null || dates.isEmpty() ) {
            throw new IllegalArgumentException("Dates cannot be null or empty");
        }

        for (Date date : dates) {
            if (holidays.contains(date)) {
                return false; // Il personale non è disponibile se ha ferie in queste date
            }
        }
        return true; // Il personale è disponibile se non ha ferie nelle date specificate
    }

}
