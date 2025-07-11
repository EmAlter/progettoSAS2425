package catering.businesslogic.staff;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import java.time.LocalDate;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.*;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserManager;
import catering.util.LogManager;

public class StaffManager {
    private static final Logger LOGGER = LogManager.getLogger(StaffManager.class);

    private ArrayList<StaffEventReceiver> eventReceivers;

    public StaffManager() {
        this.eventReceivers = new ArrayList<>();
    }

    // ===== EVENT RECEIVER MANAGEMENT =====

    public void addEventReceiver(StaffEventReceiver receiver) {
        if (receiver != null && !eventReceivers.contains(receiver)) {
            eventReceivers.add(receiver);
        }
    }

    public void removeEventReceiver(StaffEventReceiver receiver) {
        eventReceivers.remove(receiver);
    }

    // ===== METODI DEL MODELLO =====

    /**
     * Aggiunge un nuovo membro dello staff al sistema.
     * @param jobType Tipo di lavoro
     * @param permanent Se è permanente o occasionale
     * @param name Nome del dipendente
     * @param address Indirizzo
     * @param contact Contatto
     * @param fiscalCode Codice fiscale
     * @throws UseCaseLogicException Se l'utente non è proprietario
     */
    public Staff hireStaff(JobType jobType, boolean permanent, String name, String address, String contact, String fiscalCode) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        UserManager userManager = CatERing.getInstance().getUserManager();

        if (!user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to hire staff");
            throw new UseCaseLogicException("User is not authorized to hire staff");
        }

        validateHireStaffInput(name, address, contact, fiscalCode);

        // Crea un'istanza di Staff utilizzando la fabbrica
        Staff newStaff = StaffFactory.createStaff(jobType, permanent, name, address, contact, fiscalCode);
        
        notifyStaffHired(newStaff);

         // Verifica che il salvataggio sia riuscito
        if (newStaff.getId() == 0) {
            LOGGER.severe("Failed to save new staff member: " + name);
            throw new UseCaseLogicException("Failed to save new staff member");
        }

        userManager.createUserForNewStaff(newStaff.getName(), newStaff.getJobType(), newStaff.getId());
        LOGGER.info("Successfully hired new staff: " + name);
        return newStaff;
    }

    /**
     * Aggiunge una nota di valutazione a un membro dello staff per un evento specifico.
     * 
     * @param note Il testo della nota di valutazione
     * @param event L'evento per cui viene creata la nota
     * @param staff Il membro dello staff a cui assegnare la nota
     * @throws UseCaseLogicException Se l'utente non è organizzatore
     * @throws IllegalArgumentException Se i parametri sono nulli o non validi
     */
    public void addNotes(String description, Event event, Staff staff) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to add evaluation notes");
            throw new UseCaseLogicException("User is not authorized to add evaluation notes");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        
        // Verifica che lo staff esista nel database
        if (Staff.loadById(staff.getId()) == null) {
            throw new IllegalArgumentException("Staff member not found in the system");
        }

        // Verifica che l'evento esista nel database
        if(CatERing.getInstance().getEventManager().checkIfEventExists(event)) {
            throw new IllegalArgumentException("Event not found in the system");
        }

        // Creazione della nota di valutazione
        EvaluationNote evaluationNote = staff.createNewEvaluationNote(description, event.getId());

        notifyEvaluationNoteAdded(staff, evaluationNote);
        staff.addNote(evaluationNote);
        
        LOGGER.info("Added evaluation note for staff " + staff.getName() + " for event " + event.getName());
    }

    /**
     * Assegna un ruolo a un membro dello staff. (Delega la responsabilità all'EventManager)
     * @param role Il ruolo da assegnare
     * @param staff Il membro dello staff
     */
    public void assignToRole(Role role, Staff staff) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to assign roles");
            throw new UseCaseLogicException("User is not authorized to assign roles");
        }

        CatERing.getInstance().getEventManager().assignRole(role, staff);
        LOGGER.info("Assigned role " + role.getDescription() + " to staff " + staff.getName());
    }

    /**
     * Rimuove un membro dello staff da un ruolo specifico. (Delega la responsabilità all'EventManager)
     * @param role Il ruolo da cui rimuovere lo staff
     */
    public void removeStaffFromRole(Role role) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to remove role assignments");
            throw new UseCaseLogicException("User is not authorized to remove role assignments");
        }

        String staffName = role.isAssigned() ? role.getAssignedStaff().getName() : "unknown";
        CatERing.getInstance().getEventManager().removeAssignment(role);
        LOGGER.info("Removed staff " + staffName + " from role " + role.getDescription());
    }

    /**
     * Consulta una lista di tutte le note di valutazione dei dipendenti.
     * @return Una lista di EvaluationNote che rappresentano le note di valutazione
     * @throws UseCaseLogicException se l'utente non è autorizzato
     */
    public List<EvaluationNote> consultAllEvaluationNotes() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to consult all the evaluation notes");
            throw new UseCaseLogicException("User is not authorized to consult all the evaluation notes");
        }

        // Carica dal database
        List<EvaluationNote> notes = EvaluationNote.loadAllEvaluationNotes();
        return new ArrayList<>(notes);
    }

    /**
     * Consulta una lista di tutte le note di valutazione di un membro dello staff specifico.
     * @param staff Il membro dello staff di cui consultare le note
     * @return Una lista di EvaluationNote che rappresentano le note di valutazione
     * @throws UseCaseLogicException se l'utente non è autorizzato
     * @throws IllegalArgumentException Se lo staff è null o non esiste nel sistema
     */
    public List<EvaluationNote> consultEvaluationNotesForStaff(Staff staff) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to consult all the evaluation notes");
            throw new UseCaseLogicException("User is not authorized to consult all the evaluation notes");
        }

        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }

        Staff existingStaff = Staff.loadById(staff.getId());
        if (existingStaff == null) {
            throw new IllegalArgumentException("Staff member not found in the system");
        }

        // Carica dal database
        List<EvaluationNote> notes = EvaluationNote.loadByStaffId(staff.getId());
        return new ArrayList<>(notes);
    }

    /**
     * Consulta le note di valutazione per un evento specifico.
     * 
     * @param event L'evento di cui consultare le note
     * @return Lista delle note di valutazione per l'evento specificato
     * @throws UseCaseLogicException Se l'utente non è autorizzato
     * @throws IllegalArgumentException Se l'evento è null
     */
    public List<EvaluationNote> consultEvaluationNotesForEvent(Event event) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to consult evaluation notes");
            throw new UseCaseLogicException("User is not authorized to consult evaluation notes");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        List<EvaluationNote> eventNotes = EvaluationNote.loadByEventId(event.getId());
        return eventNotes;
    }

    /**
    * Consulta le note di valutazione solo per i dipendenti occasionali.
    * Accessibile solo a organizzatori e proprietari.
    * 
    * @return Lista delle note di valutazione per i dipendenti occasionali
    * @throws UseCaseLogicException Se l'utente non è autorizzato
    */
    public List<EvaluationNote> consultEvaluationNotesForOccasionalStaff() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to consult evaluation notes");
            throw new UseCaseLogicException("User is not authorized to consult evaluation notes");
        }

        List<Staff> occasionalStaff = Staff.loadByPermanentStatus(false);

        List<EvaluationNote> occasionalNotes = new ArrayList<>();
        for (Staff staff : occasionalStaff) {
            List<EvaluationNote> staffNotes = EvaluationNote.loadByStaffId(staff.getId());
            occasionalNotes.addAll(staffNotes);
        }

        return occasionalNotes;
    }

    /**
     * Modifica i dati di un membro dello staff esistente.
     * @param staff Il membro dello staff da modificare
     * @param contact Nuovo contatto (opzionale)
     * @param address Nuovo indirizzo (opzionale)
     */
    public void modifyStaffData(Staff staff, String contact, String address) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to modify staff data");
            throw new UseCaseLogicException("User is not authorized to modify staff data");
        }
        
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }

        // Verifica che lo staff esista nel database
        Staff existingStaff = Staff.loadById(staff.getId());
        if (existingStaff == null) {
            throw new IllegalArgumentException("Staff member not found in the system");
        }

        boolean modified = false;

        // Validazione e aggiornamento dei parametri
        if (contact != null && !contact.isBlank()) {
            existingStaff.setContact(contact.trim());
            modified = true;
        }
        
        if (address != null && !address.isBlank()) {
            existingStaff.setAddress(address.trim());
            modified = true;
        }

        if (!modified) {
            throw new IllegalArgumentException("At least one of contact or address must be provided");
        }

        notifyStaffModified(staff);
        LOGGER.info("Modified staff data for: " + staff.getName());
    }

    /**
     * Consulta le richieste di ferie attualmente registrate nel sistema.
     * @return Le richieste di ferie registrate
     */
    public Set<HolidayRequest> consultHolidayRequests() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to consult holiday requests");
            throw new UseCaseLogicException("User is not authorized to consult holiday requests");
        }
        
        // Carica dal database
        Set<HolidayRequest> requests = HolidayRequest.loadAllHolidayRequests();
        return new HashSet<>(requests);
    }

    /**
     * Ottiene la disponibilità di ferie rimanenti per un membro dello staff.
     * @param staff Il membro dello staff
     * @return Il numero di giorni di ferie rimanenti
     */
    public int getHolidaysAvailability(Staff staff) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOrganizer() && !user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to check holiday availability");
            throw new UseCaseLogicException("User is not authorized to check holiday availability");
        }
        
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }

        // Carica i dati aggiornati dal database
        Staff currentStaff = Staff.loadById(staff.getId());
        if (currentStaff == null) {
            throw new IllegalArgumentException("Staff member not found in the system");
        }

        return currentStaff.getRemainingDaysOff();
    }

    /**
     * Accetta una richiesta di ferie.
     * 
     * @param request La richiesta di ferie da accettare
     * @throws UseCaseLogicException Se l'utente non è proprietario
     * @throws IllegalArgumentException Se la richiesta è nulla o non valida
     */
    public void acceptHolidayRequest(HolidayRequest request) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to accept holiday requests");
            throw new UseCaseLogicException("User is not authorized to accept holiday requests");
        }
        
        if (request == null) {
            throw new IllegalArgumentException("Holiday request cannot be null");
        }
        
        validateHolidayRequest(request);

        try {
            // 1. Carica staff aggiornato dal database
            Staff staff = Staff.loadById(request.getRequester().getId());
            if (staff == null) {
                throw new IllegalArgumentException("Staff not found: " + request.getRequester().getId());
            }
            
            // 2. Aggiorna giorni e ferie
            staff.reduceHolidays(request.getNumberOfDays());
            staff.addHolidays(request.getRequestedDays());
            // 3. Notifica le modifiche
            notifyStaffModified(staff);
            notifyHolidayRequestDeleted(request);
            
            LOGGER.info("Accepted holiday request for " + staff.getName() + " (" + request.getNumberOfDays() + " days)");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to accept holiday request: " + e.getMessage());
            throw new IllegalArgumentException("Failed to process holiday request: " + e.getMessage());
        }
        
        LOGGER.info("Accepted holiday request for " + request.getRequester().getName());
    }

    /**
     * Rifiuta una richiesta di ferie.
     * @param request La richiesta di ferie da rifiutare
     * @throws UseCaseLogicException Se l'utente non è proprietario
     * @throws IllegalArgumentException Se la richiesta è nulla o non valida
     */
    public void rejectHolidayRequest(HolidayRequest request) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) {
            LOGGER.warning("User " + user.getUserName() + " is not authorized to reject holiday requests");
            throw new UseCaseLogicException("User is not authorized to reject holiday requests");
        }
        
        if (request == null) {
            throw new IllegalArgumentException("Holiday request cannot be null");
        }

        // Notifica per eliminare la richiesta dal database (è stata rifiutata)
        notifyHolidayRequestDeleted(request);
        
        LOGGER.info("Rejected holiday request for " + request.getRequester().getName());
    }


    /**
     * Controllo aggiunto fatto dal sistema per verificare la validità di una richiesta di ferie.
     * 
     * @param request La richiesta di ferie da verificare
     * @throws IllegalArgumentException Se la richiesta non è valida
     */
    private void validateHolidayRequest(HolidayRequest request) throws IllegalArgumentException {
        if (request.getNumberOfDays() <= 0) {
            throw new IllegalArgumentException("Number of requested days must be greater than zero");
        }
        
        if (request.getRequestedDays() == null || request.getRequestedDays().isEmpty()) {
            throw new IllegalArgumentException("Requested days cannot be null or empty");
        }
        
        if (request.getRequester() == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        // Verifica disponibilità giorni dal database
        Staff currentRequester = Staff.loadById(request.getRequester().getId());
        if (currentRequester == null) {
            throw new IllegalArgumentException("Requester not found in the system");
        }

        if (currentRequester.getRemainingHolidays() < request.getNumberOfDays()) {
            throw new IllegalArgumentException("Insufficient holiday days available");
        }

    }

    /**
     * Validazione dell'input per il metodo hireStaff.
     * @param name
     * @param address
     * @param contact
     * @param fiscalCode
     */
    private void validateHireStaffInput(String name, String address, String contact, String fiscalCode) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact cannot be null or empty");
        }
        if (fiscalCode == null || fiscalCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Fiscal code cannot be null or empty");
        }
    }

    /**
     * Crea una richiesta di ferie per un membro dello staff.
     * 
     * @param staff Il membro dello staff che richiede le ferie
     * @param dates Le date richieste
     * @param description Descrizione della richiesta
     * @return La richiesta creata
     * @throws UseCaseLogicException Se l'utente non è autorizzato
     * @throws IllegalArgumentException Se i parametri sono nulli o non validi
     */
    public HolidayRequest createHolidayRequest(Staff staff, Set<LocalDate> dates, String description) throws UseCaseLogicException, IllegalArgumentException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }

        if (dates == null || dates.isEmpty()) {
            throw new IllegalArgumentException("Requested dates cannot be null or empty");
        }
        
        // Verifica che l'utente sia autorizzato a creare richieste di ferie
        // Se è staff, può creare richieste solo per se stesso
        if (user.isOperationalStaff() && !isCurrentUserStaff(staff)) {
            throw new UseCaseLogicException("Staff can only create holiday requests for themselves");
        }
        
        HolidayRequest request = staff.createHolidayRequest(dates, description);
        
        notifyHolidayRequestCreated(request);
        
        LOGGER.info("Created holiday request for " + staff.getName());
        return request;
    }

    /**
     * Verifica se l'utente corrente corrisponde al membro dello staff specificato.
     * 
     * @param staff Il membro dello staff da verificare
     * @return true se l'utente corrente è lo stesso staff, false altrimenti
     */
    private boolean isCurrentUserStaff(Staff staff) {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        return currentUser.getStaffId() == staff.getId();
    }

    // ===== DATABASE OPERATIONS =====

    /**
     * Trova un membro dello staff per codice fiscale.
     * 
     * @param fiscalCode Il codice fiscale da cercare
     * @return Il membro dello staff trovato o null se non esiste
     */
    public Staff findStaffByFiscalCode(String fiscalCode) {
        return Staff.loadByFiscalCode(fiscalCode);
    }
    
    /**
     * Trova un membro dello staff per nome.
     * 
     * @param name Il nome da cercare
     * @return Il membro dello staff trovato o null se non esiste
     */
    public Staff findStaffByName(String name) {
        return Staff.loadByName(name);
    }
    
    /**
     * Trova un membro dello staff per ID.
     * 
     * @param id L'ID da cercare
     * @return Il membro dello staff trovato o null se non esiste
     */
    public Staff findStaffById(int id) {
        return Staff.loadById(id);
    }

    /**
     * Ottiene tutto il personale per tipo di contratto (permanente o occasionale).
     * @param permanent Se true, ottiene il personale permanente; se false, ottiene il personale occasionale.
     * @return Lista del personale di quel tipo
     */
    public ArrayList<Staff> getStaffByPermanentStatus(boolean permanent) {
        return Staff.loadByPermanentStatus(permanent);
    }

    /**
     * Ottiene tutto il personale per tipo di lavoro.
     * 
     * @param jobType Il tipo di lavoro
     * @return Lista del personale di quel tipo
     */
    public ArrayList<Staff> getStaffByJobType(JobType jobType) {
        return Staff.loadByJobType(jobType);
    }
    
    /**
     * Ottiene tutti i membri dello staff.
     * 
     * @return Lista di tutto il personale
     */
    public ArrayList<Staff> getAllStaff() {
        return Staff.loadAllStaff();
    }

    // ===== NOTIFICATION METHODS =====

    private void notifyStaffHired(Staff staff) {
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateStaffHired(staff);
        }
    }

    private void notifyStaffModified(Staff staff) {
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateStaffModified(staff);
        }
    }

    private void notifyHolidayRequestDeleted(HolidayRequest request) {
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateHolidayRequestDeleted(request);
        }
    }

    private void notifyEvaluationNoteAdded(Staff staff, EvaluationNote note) {
        for (StaffEventReceiver receiver : eventReceivers) {
        receiver.updateEvaluationNoteAdded(staff, note);
        }
    }
    
    private void notifyHolidayRequestCreated(HolidayRequest request) {
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateHolidayRequestCreated(request);
        }
    }
}
