package catering.businesslogic.staff;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Date;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.*;
import catering.businesslogic.shift.*;
import catering.businesslogic.user.User;

public class StaffManager {
    private Set<Staff> staffList; //Elenco del personale
    private Set<HolidayRequest> holidayRequests; // Elenco delle richieste di ferie

    public StaffManager() {
        this.staffList = new HashSet<>(); // Inizializza l'elenco del personale come un Set vuoto e mutabile
    }

    /** METODI DEL MODELLO */
    public void hireStaff(JobType jobType, boolean permanent, String name, String address, String contact, String fiscalCode) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!user.isOwner()) {
            throw new UseCaseLogicException();
        }
        // Crea un'istanza di Staff utilizzando la fabbrica
        Staff newStaff = StaffFactory.createStaff(jobType, permanent, name, address, contact, fiscalCode);
        staffList.add(newStaff);
        //Bisogna salvare il nuovo personale nel database e notificare gli osservatori
    }

    /**
     * Aggiunge una nota di valutazione a un membro dello staff per un evento specifico.
     * 
     * @param note Il testo della nota di valutazione
     * @param event L'evento per cui viene creata la nota
     * @param staff Il membro dello staff a cui assegnare la nota
     * @throws IllegalArgumentException se i parametri sono nulli o non validi
     */
    public void addNotes(String note, Event event, Staff staff) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer()) {
            throw new UseCaseLogicException();
        }

        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Note cannot be null or empty");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        if (!staffList.contains(staff)) {
            throw new IllegalArgumentException("Staff member not found in the system");
        }
        
        // Creazione della nota di valutazione (responsabilità del Manager)
        EvaluationNote evaluationNote = new EvaluationNote(note.trim(), event.getId());
        
        // Delega allo staff l'aggiunta della nota alla sua collezione
        staff.AddNote(evaluationNote);
        
        // Salva la nota nel database (forse?) e notificare gli osservatori
    }

    /**
     * Assegna un ruolo a un membro dello staff. (Delega la responsabilità all'EventManager)
     * @param role
     * @param staff
     */
    public void assignRole(Role role, Staff staff) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!user.isOrganizer() || !user.isOwner()) {
            throw new UseCaseLogicException();
        }

        EventManager eventManager = EventManager.getInstance().assignRole(role, staff);
    }

    /**
     * Rimuove un membro dello staff da un ruolo specifico. (Delega la responsabilità all'EventManager)
     * @param role
     * @param staff
     */
    public void removeStaffFromRole(Role role, Staff staff) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() || !user.isOwner()) {
            throw new UseCaseLogicException();
        }

        EventManager eventManager = EventManager.getInstance().removeAssignment(role, staff);
    }

    public List<EventStaffSheet> consultHistoricalEvents() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() || !user.isOwner()) {
            throw new UseCaseLogicException();
        }

        return EventManager.getInstance().getAllEventSheets();
    }
    /**
     * Modifica i dati di un membro dello staff esistente.
     * @param staff
     * @param contact
     * @param address
     */
    public void modifyStaffData(Staff staff, String contact, String address) throws UseCaseLogicException, IllegalArgumentException  {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            throw new UseCaseLogicException();
        }
        if(staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        // Validazione dei parametri di contatto e indirizzo
        if((contact != null && !contact.isBlank()) && (address == null || address.isBlank())) {
            staff.setContact(contact.trim());
        } else if((address != null && !address.isBlank()) && (contact == null || contact.isBlank())) {
            staff.setAddress(address.trim());
        } else if(contact != null && !contact.isBlank() && address != null && !address.isBlank()) {
            staff.setContact(contact.trim());
            staff.setAddress(address.trim());
        } else {
            throw new IllegalArgumentException("At least one of contact or address must be provided");
        }
    }

    /**
     * Consulta le richieste di ferie attualmente registrate nel sistema.
     * @return Le richieste di ferie registrate
     */
    public Set<HolidayRequest> consultHolidayRequests() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() || !user.isOwner()) {
            throw new UseCaseLogicException();
        }
        // Restituisce una copia del set di richieste di ferie
        return Set.copyOf(holidayRequests);
    }

    /**
     * Ottiene la disponibilità di ferie rimanenti per un membro dello staff.
     * @param staff
     * @return
     */
    public int getHolidaysAvailability(Staff staff) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            throw new UseCaseLogicException();
        }
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        return staff.getRemainingDaysOff();
    }

    /**
     * Consulta il tabellone dei turni del personale.
     * @return Una lista dei turni attualmente registrati
     */
    public List<Shift> consultShiftTable() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (user.isOwner()) {
            throw new UseCaseLogicException();
        }
        return ShiftManager.getInstance().getShiftTable();
    }

    public void acceptHolidayRequest(HolidayRequest request) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            throw new UseCaseLogicException();
        }
        if (request == null) {
            throw new IllegalArgumentException("Holiday request cannot be null");
        }
        
        int numberOfDaysRequested = request.getNumberOfDays();
        if (numberOfDaysRequested <= 0) {
            throw new IllegalArgumentException("Number of requested days must be greater than zero");
        }
        Set<Date> requestedDays = request.getRequestedDays();
        if (requestedDays == null || requestedDays.isEmpty()) {
            throw new IllegalArgumentException("Requested days cannot be null or empty");
        }
        Staff requester = request.getRequester();
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        requester.reduceHolidays(numberOfDaysRequested);
        requester.addHolidays(requestedDays);

        holidayRequests.remove(request);
        // Notifica gli osservatori (se necessario)
    }

    public void rejectHolidayRequest(HolidayRequest request) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            throw new UseCaseLogicException();
        }
        if (request == null) {
            throw new IllegalArgumentException("Holiday request cannot be null");
        }
        
        holidayRequests.remove(request);
        // Potrebbe essere necessario notificare il richiedente o gli osservatori
    }
    
    /**
     * Trova un membro dello staff per codice fiscale.
     * 
     * @param fiscalCode Il codice fiscale da cercare
     * @return Il membro dello staff trovato o null se non esiste
     */
    public Staff findStaffByFiscalCode(String fiscalCode) {
        if (fiscalCode == null || fiscalCode.trim().isEmpty()) {
            return null;
        }
        
        return staffList.stream()
                .filter(staff -> fiscalCode.equals(staff.getFiscalCode()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Trova un membro dello staff per nome.
     * 
     * @param name Il nome da cercare
     * @return Il membro dello staff trovato o null se non esiste
     */
    public Staff findStaffByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        return staffList.stream()
                .filter(staff -> name.equals(staff.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Ottiene tutti i membri dello staff.
     * 
     * @return Una copia del set di staff
     */
    public Set<Staff> getAllStaff() {
        return Set.copyOf(staffList);
    }
}
