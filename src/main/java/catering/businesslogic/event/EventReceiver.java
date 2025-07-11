package catering.businesslogic.event;

import catering.businesslogic.menu.Menu;

/**
 * Interface for receiving event-related notifications.
 * Implemented by classes that need to respond to event changes.
 */
public interface EventReceiver {

    void updateEventCreated(Event event);

    void updateEventModified(Event event);

    void updateEventDeleted(Event event);

    void updateServiceCreated(Event event, Service service);

    void updateServiceModified(Service service);

    void updateServiceDeleted(Service service);

    void updateMenuAssigned(Service service, Menu menu);

    void updateMenuRemoved(Service service);

    void updateEventStaffSheetCreated(EventStaffSheet sheet);

    void updateEventStaffSheetModified(EventStaffSheet sheet);

    void updateEventStaffSheetDeleted(EventStaffSheet sheet);

    void updateRoleCreated(EventStaffSheet sheet, Role role);
    
    void updateRoleAssigned(EventStaffSheet sheet, Role role);

    void updateRoleUnassigned(EventStaffSheet sheet, Role role);

    void updateRoleRemoved(EventStaffSheet sheet, Role role);
}