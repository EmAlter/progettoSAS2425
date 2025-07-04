package catering.businesslogic.staff;

import java.util.Set;
import java.util.Date;

public class HolidayRequest {
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
        this.requestedDays = requestedDays;
        this.requester = requester;
        this.description = description;
    }

    public Staff getRequester() {
        return requester;
    }
    public String getDescription() {
        return description;
    }
    public Set<Date> getRequestedDays() {
        return requestedDays;
    }

    /**
     * Calcola il numero di giorni richiesti per le ferie.
     * 
     * @return Il numero di giorni richiesti per le ferie.
     */
    public int getNumberOfDays() {
        return requestedDays.size();
    }
}
