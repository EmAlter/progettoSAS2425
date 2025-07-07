package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staff.JobType;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffFactory;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class EventStaffSheetTest {

    private static final Logger LOGGER = LogManager.getLogger(EventStaffSheetTest.class);

    private static CatERing app;
    private static User organizer;
    private static User chef;
    private static Event testEvent;
    private static Staff testCook;
    private static Staff testChef;
    private static Staff testServiceStaff;

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

            // Set up test event
            testEvent = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");

            // Create test staff members
            testCook = StaffFactory.createStaff(JobType.COOK, true, "Mario Cook Test", 
                "Via Cook 123", "333-1111111", "TSTMRA85M01F205X");
            testCook.saveNewStaff();

            testChef = StaffFactory.createStaff(JobType.CHEF, true, "Luigi Chef Test", 
                "Via Chef 456", "333-2222222", "TSTLGU90L15F205Y");
            testChef.saveNewStaff();

            testServiceStaff = StaffFactory.createStaff(JobType.SERVICE, false, "Anna Service Test", 
                "Via Service 789", "333-3333333", "TSTANN88F45F205Z");
            testServiceStaff.saveNewStaff();

            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "Current user should be the organizer");

        } catch (UseCaseLogicException e) {
            LOGGER.severe(e.getMessage());
            fail("Setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testEventStaffSheetCreation() {
        LOGGER.info("Testing EventStaffSheet creation");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Verify sheet creation
        assertNotNull(sheet, "EventStaffSheet should not be null");
        assertEquals("", sheet.getNote(), "Initial note should be empty");
        assertNotNull(sheet.getRoles(), "Roles list should not be null");
        assertTrue(sheet.getRoles().isEmpty(), "Roles list should be empty initially");
        assertEquals(0, sheet.countAssignedRoles(), "No roles should be assigned initially");

        LOGGER.info("Successfully created EventStaffSheet for event: " + testEvent.getName());
        LOGGER.info("EventStaffSheet details: " + sheet.toString());
    }

    @Test
    @Order(2)
    void testAddingRolesToSheet() {
        LOGGER.info("Testing adding roles to EventStaffSheet");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Create roles
        Role cookRole = new Role("Preparazione antipasti", JobType.COOK);
        Role chefRole = new Role("Supervisione cucina", JobType.CHEF);
        Role serviceRole = new Role("Responsabile sala", JobType.SERVICE);

        // Add roles to sheet
        sheet.addRole(cookRole);
        sheet.addRole(chefRole);
        sheet.addRole(serviceRole);

        // Verify roles were added
        List<Role> roles = sheet.getRoles();
        assertEquals(3, roles.size(), "Should have 3 roles");
        assertTrue(roles.contains(cookRole), "Should contain cook role");
        assertTrue(roles.contains(chefRole), "Should contain chef role");
        assertTrue(roles.contains(serviceRole), "Should contain service role");

        // Verify all roles are unassigned
        assertEquals(0, sheet.countAssignedRoles(), "No roles should be assigned yet");
        assertEquals(3, sheet.getUnassignedRoles().size(), "All 3 roles should be unassigned");
        assertTrue(sheet.getAssignedRoles().isEmpty(), "Assigned roles list should be empty");

        LOGGER.info("Successfully added 3 roles to EventStaffSheet");
        LOGGER.info("EventStaffSheet with roles: " + sheet.toString());
    }

    @Test
    @Order(3)
    void testAssigningStaffToRoles() {
        LOGGER.info("Testing assigning staff to roles");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Create and add roles
        Role cookRole = new Role("Preparazione primi", JobType.COOK);
        Role chefRole = new Role("Chef supervisore", JobType.CHEF);
        Role serviceRole = new Role("Cameriere", JobType.SERVICE);

        sheet.addRole(cookRole);
        sheet.addRole(chefRole);
        sheet.addRole(serviceRole);

        // Assign staff to roles
        sheet.assignStaffToRole(cookRole, testCook);
        sheet.assignStaffToRole(chefRole, testChef);
        sheet.assignStaffToRole(serviceRole, testServiceStaff);

        // Verify assignments
        assertTrue(cookRole.isAssigned(), "Cook role should be assigned");
        assertEquals(testCook, cookRole.getAssignedStaff(), "Cook role should be assigned to test cook");
        assertEquals(testCook, sheet.getStaffForRole(cookRole), "Sheet should return correct staff for cook role");

        assertTrue(chefRole.isAssigned(), "Chef role should be assigned");
        assertEquals(testChef, chefRole.getAssignedStaff(), "Chef role should be assigned to test chef");
        assertEquals(testChef, sheet.getStaffForRole(chefRole), "Sheet should return correct staff for chef role");

        assertTrue(serviceRole.isAssigned(), "Service role should be assigned");
        assertEquals(testServiceStaff, serviceRole.getAssignedStaff(), "Service role should be assigned to test service staff");
        assertEquals(testServiceStaff, sheet.getStaffForRole(serviceRole), "Sheet should return correct staff for service role");

        // Verify assignment counts
        assertEquals(3, sheet.countAssignedRoles(), "All 3 roles should be assigned");
        assertEquals(3, sheet.getAssignedRoles().size(), "Assigned roles list should have 3 roles");
        assertTrue(sheet.getUnassignedRoles().isEmpty(), "Unassigned roles list should be empty");

        LOGGER.info("Successfully assigned staff to all roles");
        LOGGER.info("EventStaffSheet with assignments: " + sheet.toString());
    }

    @Test
    @Order(4)
    void testRemovingAssignments() {
        LOGGER.info("Testing removing staff assignments");

        // Create event staff sheet with assignments
        EventStaffSheet sheet = new EventStaffSheet(testEvent);
        Role cookRole = new Role("Cuoco principale", JobType.COOK);
        Role serviceRole = new Role("Cameriere principale", JobType.SERVICE);

        sheet.addRole(cookRole);
        sheet.addRole(serviceRole);
        sheet.assignStaffToRole(cookRole, testCook);
        sheet.assignStaffToRole(serviceRole, testServiceStaff);

        // Verify initial assignments
        assertEquals(2, sheet.countAssignedRoles(), "Should have 2 assigned roles initially");
        assertTrue(cookRole.isAssigned(), "Cook role should be assigned");
        assertTrue(serviceRole.isAssigned(), "Service role should be assigned");

        // Remove one assignment
        sheet.removeAssignment(cookRole);

        // Verify removal
        assertFalse(cookRole.isAssigned(), "Cook role should not be assigned after removal");
        assertNull(sheet.getStaffForRole(cookRole), "Should return null for unassigned role");
        assertTrue(serviceRole.isAssigned(), "Service role should still be assigned");

        // Verify counts
        assertEquals(1, sheet.countAssignedRoles(), "Should have 1 assigned role");
        assertEquals(1, sheet.getAssignedRoles().size(), "Assigned roles list should have 1 role");
        assertEquals(1, sheet.getUnassignedRoles().size(), "Unassigned roles list should have 1 role");

        // Remove remaining assignment
        sheet.removeAssignment(serviceRole);

        // Verify all assignments removed
        assertEquals(0, sheet.countAssignedRoles(), "Should have no assigned roles");
        assertTrue(sheet.getAssignedRoles().isEmpty(), "Assigned roles list should be empty");
        assertEquals(2, sheet.getUnassignedRoles().size(), "Unassigned roles list should have 2 roles");

        LOGGER.info("Successfully removed all staff assignments");
        LOGGER.info("EventStaffSheet after removals: " + sheet.toString());
    }

    @Test
    @Order(5)
    void testFindingRoles() {
        LOGGER.info("Testing finding roles by type and description");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Create diverse roles
        Role cookRole1 = new Role("Preparazione antipasti", JobType.COOK);
        Role cookRole2 = new Role("Preparazione primi", JobType.COOK);
        Role chefRole = new Role("Chef supervisore", JobType.CHEF);
        Role serviceRole1 = new Role("Cameriere sala", JobType.SERVICE);
        Role serviceRole2 = new Role("Cameriere bar", JobType.SERVICE);

        sheet.addRole(cookRole1);
        sheet.addRole(cookRole2);
        sheet.addRole(chefRole);
        sheet.addRole(serviceRole1);
        sheet.addRole(serviceRole2);

        // Test finding specific role
        Role foundRole = sheet.findRole(JobType.CHEF, "Chef supervisore");
        assertNotNull(foundRole, "Should find chef role");
        assertEquals(chefRole, foundRole, "Should return the correct chef role");

        // Test finding non-existent role
        Role notFoundRole = sheet.findRole(JobType.COOK, "Non-existent role");
        assertNull(notFoundRole, "Should not find non-existent role");

        // Test finding roles by type only
        List<Role> cookRoles = sheet.getNotAssignedByType(JobType.COOK);
        assertEquals(2, cookRoles.size(), "Should find 2 cook roles");
        assertTrue(cookRoles.contains(cookRole1), "Should contain first cook role");
        assertTrue(cookRoles.contains(cookRole2), "Should contain second cook role");

        List<Role> serviceRoles = sheet.getNotAssignedByType(JobType.SERVICE);
        assertEquals(2, serviceRoles.size(), "Should find 2 service roles");

        List<Role> chefRoles = sheet.getNotAssignedByType(JobType.CHEF);
        assertEquals(1, chefRoles.size(), "Should find 1 chef role");
        assertTrue(chefRoles.contains(chefRole), "Should contain chef role");

        LOGGER.info("Successfully tested role finding functionality");
    }

    @Test
    @Order(6)
    void testAddingNotesToSheet() {
        LOGGER.info("Testing adding notes to EventStaffSheet");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Initial note should be empty
        assertEquals("", sheet.getNote(), "Initial note should be empty");

        // Add first note
        String firstNote = "Evento molto importante - richiedere massima attenzione";
        sheet.setNote(firstNote);
        assertEquals(firstNote, sheet.getNote(), "Note should match what was set");

        // Update note
        String updatedNote = "Aggiornamento: cliente molto esigente, staff esperto necessario";
        sheet.setNote(updatedNote);
        assertEquals(updatedNote, sheet.getNote(), "Note should be updated");

        // Test with null note (should set empty string)
        sheet.setNote(null);
        assertEquals("", sheet.getNote(), "Null note should result in empty string");

        // Test with empty note
        sheet.setNote("");
        assertEquals("", sheet.getNote(), "Empty note should remain empty");

        LOGGER.info("Successfully tested note functionality");
        LOGGER.info("Final EventStaffSheet: " + sheet.toString());
    }

    @Test
    @Order(7)
    void testCompleteEventStaffSheetWorkflow() {
        LOGGER.info("Testing complete EventStaffSheet workflow");

        // Create event staff sheet
        EventStaffSheet sheet = new EventStaffSheet(testEvent);

        // Add comprehensive note
        sheet.setNote("Evento gala aziendale - richiesta massima professionalità");

        // Create roles for different aspects of the event
        Role headChef = new Role("Chef responsabile", JobType.CHEF);
        Role sousChef = new Role("Sous chef", JobType.CHEF);
        Role mainCook = new Role("Cuoco principale", JobType.COOK);
        Role assistantCook = new Role("Cuoco assistente", JobType.COOK);
        Role headWaiter = new Role("Responsabile sala", JobType.SERVICE);
        Role waiter1 = new Role("Cameriere tavoli", JobType.SERVICE);
        Role waiter2 = new Role("Cameriere bar", JobType.SERVICE);

        // Add all roles
        sheet.addRole(headChef);
        sheet.addRole(sousChef);
        sheet.addRole(mainCook);
        sheet.addRole(assistantCook);
        sheet.addRole(headWaiter);
        sheet.addRole(waiter1);
        sheet.addRole(waiter2);

        // Verify all roles added
        assertEquals(7, sheet.getRoles().size(), "Should have 7 roles total");
        assertEquals(0, sheet.countAssignedRoles(), "No roles should be assigned initially");

        // Create additional staff for comprehensive test
        Staff sousChefStaff = StaffFactory.createStaff(JobType.CHEF, true, "Roberto Sous", 
            "Via Sous 123", "333-4444444", "ROBSOU85M01F205W");
        Staff assistantCookStaff = StaffFactory.createStaff(JobType.COOK, false, "Paola Assistant", 
            "Via Assistant 456", "333-5555555", "PAOASS90F45F205V");
        Staff headWaiterStaff = StaffFactory.createStaff(JobType.SERVICE, true, "Marco Head", 
            "Via Head 789", "333-6666666", "MARHED88L20F205U");

        // Assign staff strategically
        sheet.assignStaffToRole(headChef, testChef);
        sheet.assignStaffToRole(sousChef, sousChefStaff);
        sheet.assignStaffToRole(mainCook, testCook);
        sheet.assignStaffToRole(assistantCook, assistantCookStaff);
        sheet.assignStaffToRole(headWaiter, headWaiterStaff);
        // Leave waiter1 and waiter2 unassigned intentionally

        // Verify partial assignments
        assertEquals(5, sheet.countAssignedRoles(), "Should have 5 assigned roles");
        assertEquals(2, sheet.getUnassignedRoles().size(), "Should have 2 unassigned roles");

        // Verify role types distribution
        assertEquals(2, sheet.getNotAssignedByType(JobType.SERVICE).size(), 
                    "Should have 2 unassigned service staff roles");
        assertEquals(0, sheet.getNotAssignedByType(JobType.COOK).size(), 
                    "Should have 0 unassigned cook roles");
        assertEquals(0, sheet.getNotAssignedByType(JobType.CHEF).size(), 
                    "Should have 0 unassigned chef roles");

        // Test finding specific assignments
        assertEquals(testChef, sheet.getStaffForRole(headChef), "Head chef should be assigned correctly");
        assertEquals(testCook, sheet.getStaffForRole(mainCook), "Main cook should be assigned correctly");
        assertNull(sheet.getStaffForRole(waiter1), "Unassigned role should return null");

        // Test role finding
        Role foundRole = sheet.findRole(JobType.SERVICE, "Cameriere bar");
        assertNotNull(foundRole, "Should find specific service role");
        assertEquals(waiter2, foundRole, "Should return correct waiter role");
        assertFalse(foundRole.isAssigned(), "Found role should be unassigned");

        LOGGER.info("Successfully completed comprehensive EventStaffSheet workflow");
        LOGGER.info("Final comprehensive sheet: " + sheet.toString());
    }
}