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
    void updateStaffHired(Staff s);

    /**
     * Called when staff data is modified
     * 
     * @param s The staff member whose data was modified
     */
    void updateStaffModified(Staff s);

    /**
     * Called when a holiday request is deleted from the system
     * 
     * @param hr The holiday request that was deleted
     */
    void updateHolidayRequestDeleted(HolidayRequest hr);
}
