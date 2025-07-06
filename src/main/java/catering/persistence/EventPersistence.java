package catering.persistence;

import catering.businesslogic.event.Event;
import catering.businesslogic.event.EventReceiver;
import catering.businesslogic.event.EventStaffSheet;
import catering.businesslogic.event.Role;
import catering.businesslogic.event.Service;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.staff.Staff;

/**
 * Persistence class for Event operations.
 * Delegates to Event and Service classes for actual persistence.
 */
public class EventPersistence implements EventReceiver {

    @Override
    public void updateEventCreated(Event event) {
        event.saveNewEvent();
    }

    @Override
    public void updateEventModified(Event event) {
        event.updateEvent();
    }

    @Override
    public void updateEventDeleted(Event event) {
        event.deleteEvent();
    }

    @Override
    public void updateServiceCreated(Event event, Service service) {
        service.saveNewService();
    }

    @Override
    public void updateServiceModified(Service service) {
        service.updateService();
    }

    @Override
    public void updateServiceDeleted(Service service) {
        service.deleteService();
    }

    @Override
    public void updateMenuAssigned(Service service, Menu menu) {
        service.assignMenuToService(menu);
    }

    @Override
    public void updateMenuRemoved(Service service) {
        service.removeMenuFromService();
    }
    @Override
    public void updateEventStaffSheetCreated(EventStaffSheet sheet) {
        sheet.saveNewEventStaffSheet();
    }

    @Override
    public void updateEventStaffSheetModified(EventStaffSheet sheet) {
        sheet.updateEventStaffSheet();
    }

    @Override
    public void updateEventStaffSheetDeleted(EventStaffSheet sheet) {
        sheet.deleteEventStaffSheet();
    }

    @Override
    public void updateRoleCreated(EventStaffSheet sheet, Role role) {
        sheet.saveNewRole(role);
    }
    
    @Override
    public void updateRoleAssigned(EventStaffSheet sheet, Role role, Staff staff) {
        sheet.updateRoleAssignment(role);
    }

    @Override
    public void updateRoleUnassigned(EventStaffSheet sheet, Role role, Staff previousStaff) {
        sheet.updateRoleUnassignment(role);
    }

    @Override
    public void updateRoleRemoved(EventStaffSheet sheet, Role role) {
        sheet.updateRoleUnassignment(role);
    }
}