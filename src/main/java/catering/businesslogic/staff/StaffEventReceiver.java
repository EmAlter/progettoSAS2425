package catering.businesslogic.staff;

/**
 * Interfaccia per ricevere notifiche sugli eventi relativi allo staff.
 * Implementa il pattern Observer per le operazioni di gestione dello staff.
 * Le classi che implementano questa interfaccia saranno notificate quando
 * si verificano eventi relativi allo staff nel sistema.
 */
public interface StaffEventReceiver {
    /**
     * Chiamato quando un membro dello staff viene assunto
     * 
     * @param staff Il membro dello staff che è stato assunto
     */
    void updateStaffHired(Staff staff);

    /**
     * Chiamato quando i dati di un membro dello staff vengono modificati
     * 
     * @param staff Il membro dello staff i cui dati sono stati modificati
     */
    void updateStaffModified(Staff staff);

    /**
     * Chiamato quando una richiesta di ferie viene eliminata dal sistema
     * 
     * @param request La richiesta di ferie che è stata eliminata
     */
    void updateHolidayRequestDeleted(HolidayRequest request);

    /**
     * Chiamato quando una nota viene aggiunta alle note di valutazione di un membro dello staff
     * 
     * @param staff Il membro dello staff le cui note di valutazione sono state modificate
     * @param note La nota di valutazione che è stata aggiunta
     */
    void updateEvaluationNoteAdded(Staff staff, EvaluationNote note);

    /**
     * Chiamato quando una richiesta di ferie viene creata
     * 
     * @param request La richiesta di ferie che è stata creata
     */
    void updateHolidayRequestCreated(HolidayRequest request);
}
