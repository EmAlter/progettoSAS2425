package catering.businesslogic.staff;
import catering.businesslogic.event.Role;

/**
 * Interface for receiving staff-related event notifications.
 * Implements the Observer pattern for staff management operations.
 * Classes that implement this interface will be notified when staff-related
 * events occur in the system.
 */
public interface StaffEventReceiver {
    /**
     * Called when a new role is created and added to an EventStaffSheet
     * 
     * @param r  The role that was created
     * @param sh The EventStaffSheet to which the role was added
     */
    void updateRoleCreated(Role r);

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
