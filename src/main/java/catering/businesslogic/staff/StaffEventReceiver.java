package catering.businesslogic.staff;

/**
 * Interface for receiving staff-related event notifications.
 * Implements the Observer pattern for staff management operations.
 * Classes that implement this interface will be notified when staff-related
 * events occur in the system.
 */
public interface StaffEventReceiver {
    /**
     * Called when new staff is hired into the system
     * 
     * @param s The staff member that was hired
     */
    void updateStaffHired(Staff staff);

    /**
     * Called when staff data is modified
     * 
     * @param s The staff member whose data was modified
     */
    void updateStaffModified(Staff staff);

    /**
     * Called when a holiday request is deleted from the system
     * 
     * @param request The holiday request that was deleted
     */
    void updateHolidayRequestDeleted(HolidayRequest request);

    /**
     * Called when a note is added to a staff member's evaluation notes
     * 
     * @param staff The staff member whose evaluation notes were modified
     * @param note The evaluation note that was added
     */
    void updateEvaluationNoteAdded(Staff staff, EvaluationNote note);

    /**
     * Called when a holiday request is created
     * 
     * @param request The holiday request that was created
     */
    void updateHolidayRequestCreated(HolidayRequest request);
}
