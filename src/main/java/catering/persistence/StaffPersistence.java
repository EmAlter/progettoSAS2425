package catering.persistence;

import catering.businesslogic.staff.EvaluationNote;
import catering.businesslogic.staff.HolidayRequest;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffEventReceiver;

/**
 * Persistence class for Staff operations.
 * Implements the Observer pattern to handle staff-related events.
 */
public class StaffPersistence implements StaffEventReceiver {

    @Override
    public void updateStaffHired(Staff staff) {
        staff.saveNewStaff();
    }

    @Override
    public void updateStaffModified(Staff staff) {
        staff.updateStaff();
    }

    @Override
    public void updateHolidayRequestDeleted(HolidayRequest holidayRequest) {
        holidayRequest.deleteHolidayRequest();
    }

    @Override
    public void updateEvaluationNoteAdded(Staff staff, EvaluationNote note) {
        note.saveNewEvaluationNote(staff.getId());
    }

    @Override
    public void updateHolidayRequestCreated(HolidayRequest request) {
        request.saveNewHolidayRequest();
    }
    
}
