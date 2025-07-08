package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class EventManagerTest {

    private static final Logger LOGGER = LogManager.getLogger(EventManagerTest.class);

    private static CatERing app;
    private static User organizer;
    private static User chef;
    private static Staff testStaff;
    private static Event testEvent;
    private static Service testService;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Set up the organizer user
            organizer = User.load("Giovanni");
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");

            // Set up the chef user
            chef = User.load("Antonio");
            assertNotNull(chef, "Chef user should be loaded");
            assertTrue(chef.isChef(), "User should have chef role");

            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), 
                "Current user should be the organizer");

            // Get existing staff from database (Marco Rossi - CHEF from sample data)
            testStaff = Staff.loadByName("Marco Rossi");
            assertNotNull(testStaff, "Mario Rossi should exist in database");

            // Set up existing event
            testEvent = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");

            // Get existing service
            testService = Service.loadByName("Pranzo Buffet Aziendale");
            assertNotNull(testService, "Test service should be loaded");

        } catch (UseCaseLogicException e) {
            LOGGER.severe("Setup failed: " + e.getMessage());
            fail("Setup should not fail: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testEventCreation() {
        LOGGER.info("Testing event creation");

        try {
            // Create a new event
            Date startDate = Date.valueOf("2025-08-15");
            Date endDate = Date.valueOf("2025-08-15");
            Event newEvent = app.getEventManager().createEvent(
                "Test Corporate Dinner", startDate, endDate, chef);

            // Verify event was created properly
            assertNotNull(newEvent, "Event should not be null");
            assertEquals("Test Corporate Dinner", newEvent.getName(), 
                "Event name should match");
            assertEquals(startDate, newEvent.getDateStart(), 
                "Event start date should match");
            assertEquals(endDate, newEvent.getDateEnd(), 
                "Event end date should match");
            assertEquals(chef, newEvent.getChef(), 
                "Event chef should match");

            // Verify event is selected
            assertEquals(newEvent, app.getEventManager().getSelectedEvent(), 
                "Created event should be selected");

            LOGGER.info("Created event: " + newEvent.toString());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testServiceCreation() {
        LOGGER.info("Testing service creation for event");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Create a new service
            Date serviceDate = Date.valueOf("2025-08-15");
            Time startTime = Time.valueOf("19:00:00");
            Time endTime = Time.valueOf("23:00:00");
            Service newService = app.getEventManager().createService(
                "Test Evening Service", serviceDate, startTime, endTime, "Main Hall");

            // Verify service was created properly
            assertNotNull(newService, "Service should not be null");
            assertEquals("Test Evening Service", newService.getName(), 
                "Service name should match");
            assertEquals(serviceDate, newService.getDate(), 
                "Service date should match");
            assertEquals(startTime, newService.getTimeStart(), 
                "Service start time should match");
            assertEquals(endTime, newService.getTimeEnd(), 
                "Service end time should match");
            assertEquals("Main Hall", newService.getLocation(), 
                "Service location should match");

            // Verify service is added to event
            assertTrue(testEvent.containsService(newService), 
                "Event should contain the new service");

            // Verify service is selected
            assertEquals(newService, app.getEventManager().getCurrentService(), 
                "Created service should be selected");

            LOGGER.info("Created service: " + newService.toString());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testEventStaffSheetCreation() {
        LOGGER.info("Testing EventStaffSheet creation");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Get or create EventStaffSheet
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();

            // Verify staff sheet was created properly
            assertNotNull(staffSheet, "EventStaffSheet should not be null");
            assertNotNull(staffSheet.getRoles(), "Roles list should not be null");
            assertEquals(testEvent.getId(), staffSheet.getId(), 
                "Staff sheet ID should match event ID");

            LOGGER.info("EventStaffSheet created/loaded: " + staffSheet.toString());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void testRoleCreation() {
        LOGGER.info("Testing role creation");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Create roles
            Role chefRole = app.getEventManager().createRole("Head Chef", JobType.CHEF);
            Role cookRole = app.getEventManager().createRole("Sous Chef", JobType.COOK);
            Role serviceRole = app.getEventManager().createRole("Head Waiter", JobType.SERVICE);

            // Verify roles were created properly
            assertNotNull(chefRole, "Chef role should not be null");
            assertNotNull(cookRole, "Cook role should not be null");
            assertNotNull(serviceRole, "Service role should not be null");

            assertEquals("Head Chef", chefRole.getDescription(), 
                "Chef role description should match");
            assertEquals(JobType.CHEF, chefRole.getType(), 
                "Chef role type should match");
            assertFalse(chefRole.isAssigned(), 
                "Newly created role should not be assigned");

            assertEquals("Sous Chef", cookRole.getDescription(), 
                "Cook role description should match");
            assertEquals(JobType.COOK, cookRole.getType(), 
                "Cook role type should match");

            assertEquals("Head Waiter", serviceRole.getDescription(), 
                "Service role description should match");
            assertEquals(JobType.SERVICE, serviceRole.getType(), 
                "Service role type should match");

            // Verify roles are in staff sheet
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            List<Role> roles = staffSheet.getRoles();
            assertTrue(roles.contains(chefRole), 
                "Staff sheet should contain chef role");
            assertTrue(roles.contains(cookRole), 
                "Staff sheet should contain cook role");
            assertTrue(roles.contains(serviceRole), 
                "Staff sheet should contain service role");

            LOGGER.info("Created roles - Chef: " + chefRole.getDescription() + 
                ", Cook: " + cookRole.getDescription() + 
                ", Service: " + serviceRole.getDescription());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testRoleAssignment() {
        LOGGER.info("Testing role assignment to staff");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Create a role
            Role testRole = app.getEventManager().createRole("Test Kitchen Manager", JobType.CHEF);
            assertNotNull(testRole, "Test role should be created");
            assertFalse(testRole.isAssigned(), "Role should not be assigned initially");

            // Assign staff to role
            app.getEventManager().assignRole(testRole, testStaff);

            // Verify assignment
            assertTrue(testRole.isAssigned(), "Role should be assigned after assignment");
            assertEquals(testStaff, testRole.getAssignedStaff(), 
                "Role should be assigned to correct staff");

            // Verify through staff sheet
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            Staff assignedStaff = staffSheet.getStaffForRole(testRole);
            assertEquals(testStaff, assignedStaff, 
                "Staff sheet should return correct assigned staff");

            LOGGER.info("Successfully assigned staff to role. Role: " + 
                testRole.getDescription() + ", Staff: " + testStaff.getName());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testRoleUnassignment() {
        LOGGER.info("Testing role unassignment");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Create and assign a role
            Role testRole = app.getEventManager().createRole("Test Temp Role", JobType.COOK);
            app.getEventManager().assignRole(testRole, testStaff);

            // Verify assignment first
            assertTrue(testRole.isAssigned(), "Role should be assigned");
            assertEquals(testStaff, testRole.getAssignedStaff(), 
                "Role should be assigned to test staff");

            // Remove assignment
            app.getEventManager().removeAssignment(testRole);

            // Verify unassignment
            assertFalse(testRole.isAssigned(), "Role should not be assigned after removal");
            assertNull(testRole.getAssignedStaff(), 
                "Role should not have assigned staff after removal");

            // Verify through staff sheet
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            Staff assignedStaff = staffSheet.getStaffForRole(testRole);
            assertNull(assignedStaff, 
                "Staff sheet should return null for unassigned role");

            LOGGER.info("Successfully removed staff assignment from role: " + 
                testRole.getDescription());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    void testEventStaffSheetNotes() {
        LOGGER.info("Testing EventStaffSheet notes functionality");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Add notes to the event staff sheet
            String testNote = "This is a test note for the event staff management.";
            app.getEventManager().addNotes(testNote);

            // Verify notes were added
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            assertEquals(testNote, staffSheet.getNote(), 
                "Staff sheet should contain the added note");

            LOGGER.info("Successfully added note to EventStaffSheet: " + testNote);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    void testMultipleRoleAssignments() {
        LOGGER.info("Testing multiple role assignments and role finding");

        try {
            // Select the test event
            app.getEventManager().selectEvent(testEvent);

            // Create multiple roles
            Role role1 = app.getEventManager().createRole("Kitchen Coordinator", JobType.CHEF);
            Role role2 = app.getEventManager().createRole("Assistant Chef", JobType.COOK);
            Role role3 = app.getEventManager().createRole("Service Coordinator", JobType.SERVICE);

            // Assign staff to some roles
            app.getEventManager().assignRole(role1, testStaff);
            // Leave role2 unassigned
            // Leave role3 unassigned

            // Verify assignments
            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            
            // Test assigned roles
            List<Role> assignedRoles = staffSheet.getAssignedRoles();
            assertTrue(assignedRoles.contains(role1), 
                "Assigned roles should contain role1");
            assertFalse(assignedRoles.contains(role2), 
                "Assigned roles should not contain unassigned role2");
            assertFalse(assignedRoles.contains(role3), 
                "Assigned roles should not contain unassigned role3");

            // Test unassigned roles
            List<Role> unassignedRoles = staffSheet.getUnassignedRoles();
            assertFalse(unassignedRoles.contains(role1), 
                "Unassigned roles should not contain assigned role1");
            assertTrue(unassignedRoles.contains(role2), 
                "Unassigned roles should contain role2");
            assertTrue(unassignedRoles.contains(role3), 
                "Unassigned roles should contain role3");

            // Test role finding
            Role foundRole = staffSheet.findRole(JobType.CHEF, "Kitchen Coordinator");
            assertEquals(role1, foundRole, "Should find the correct role by type and description");

            Role notFoundRole = staffSheet.findRole(JobType.COOK, "Nonexistent Role");
            assertNull(notFoundRole, "Should return null for non-existent role");

            LOGGER.info("Successfully tested multiple role assignments. " +
                "Assigned: " + staffSheet.countAssignedRoles() + "/" + staffSheet.getRoles().size());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}