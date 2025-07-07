package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

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
public class RoleTest {

    private static final Logger LOGGER = LogManager.getLogger(RoleTest.class);

    private static CatERing app;
    private static User organizer;
    private static Staff testCook;
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

            // Create test staff members
            testCook = StaffFactory.createStaff(JobType.COOK, true, "Mario Test", 
                "Via Test 123", "333-1111111", "TSTMRA85M01F205X");
            testServiceStaff = StaffFactory.createStaff(JobType.SERVICE, false, "Luigi Test", 
                "Via Service 456", "333-2222222", "TSTLGU90L15F205Y");

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
    void testRoleCreation() {
        LOGGER.info("Testing role creation");

        // Test creating different types of roles
        Role cookRole = new Role("Preparazione antipasti", JobType.COOK);
        assertNotNull(cookRole, "Cook role should not be null");
        assertEquals("Preparazione antipasti", cookRole.getDescription(), "Role description should match");
        assertEquals(JobType.COOK, cookRole.getType(), "Role type should be COOK");
        assertFalse(cookRole.isAssigned(), "Newly created role should not be assigned");
        assertNull(cookRole.getAssignedStaff(), "Assigned staff should be null for new role");

        Role serviceRole = new Role("Responsabile sala", JobType.SERVICE);
        assertNotNull(serviceRole, "Service role should not be null");
        assertEquals("Responsabile sala", serviceRole.getDescription(), "Role description should match");
        assertEquals(JobType.SERVICE, serviceRole.getType(), "Role type should be SERVICE");
        assertFalse(serviceRole.isAssigned(), "Newly created role should not be assigned");

        Role chefRole = new Role("Chef supervisore", JobType.CHEF);
        assertNotNull(chefRole, "Chef role should not be null");
        assertEquals("Chef supervisore", chefRole.getDescription(), "Role description should match");
        assertEquals(JobType.CHEF, chefRole.getType(), "Role type should be CHEF");

        LOGGER.info("Successfully created roles: " + cookRole.getDescription() + ", " + 
                   serviceRole.getDescription() + ", " + chefRole.getDescription());
    }

    @Test
    @Order(2)
    void testRoleAssignment() {
        LOGGER.info("Testing role assignment to staff");

        // Create a role
        Role cookRole = new Role("Cuoco principale", JobType.COOK);
        assertFalse(cookRole.isAssigned(), "Role should not be assigned initially");

        // Assign staff to role
        cookRole.setStaff(testCook);

        // Verify assignment
        assertTrue(cookRole.isAssigned(), "Role should be assigned after setting staff");
        assertEquals(testCook, cookRole.getAssignedStaff(), "Assigned staff should match the test cook");

        LOGGER.info("Successfully assigned staff '" + testCook.getName() + "' to role '" + cookRole.getDescription() + "'");
    }

    @Test
    @Order(3)
    void testRoleUnassignment() {
        LOGGER.info("Testing role unassignment");

        // Create and assign a role
        Role serviceRole = new Role("Cameriere", JobType.SERVICE);
        serviceRole.setStaff(testServiceStaff);
        
        // Verify assignment
        assertTrue(serviceRole.isAssigned(), "Role should be assigned");
        assertEquals(testServiceStaff, serviceRole.getAssignedStaff(), "Assigned staff should match");

        // Remove assignment
        serviceRole.removeStaff();

        // Verify unassignment
        assertFalse(serviceRole.isAssigned(), "Role should not be assigned after removal");
        assertNull(serviceRole.getAssignedStaff(), "Assigned staff should be null after removal");

        LOGGER.info("Successfully unassigned staff from role '" + serviceRole.getDescription() + "'");
    }

    @Test
    @Order(4)
    void testRoleReassignment() {
        LOGGER.info("Testing role reassignment to different staff");

        // Create a role and assign first staff
        Role flexibleRole = new Role("Tuttofare", JobType.SERVICE);
        flexibleRole.setStaff(testServiceStaff);
        
        // Verify first assignment
        assertTrue(flexibleRole.isAssigned(), "Role should be assigned to first staff");
        assertEquals(testServiceStaff, flexibleRole.getAssignedStaff(), "Should be assigned to service staff");

        // Create another staff member for reassignment
        Staff anotherServiceStaff = StaffFactory.createStaff(JobType.SERVICE, true, "Anna Test", 
            "Via Another 789", "333-3333333", "TSTANN88F45F205Z");

        // Reassign to different staff
        flexibleRole.setStaff(anotherServiceStaff);

        // Verify reassignment
        assertTrue(flexibleRole.isAssigned(), "Role should still be assigned");
        assertEquals(anotherServiceStaff, flexibleRole.getAssignedStaff(), "Should be assigned to new staff");
        assertNotEquals(testServiceStaff, flexibleRole.getAssignedStaff(), "Should not be assigned to old staff");

        LOGGER.info("Successfully reassigned role '" + flexibleRole.getDescription() + 
                   "' from '" + testServiceStaff.getName() + "' to '" + anotherServiceStaff.getName() + "'");
    }

    @Test
    @Order(5)
    void testMultipleRolesForDifferentJobTypes() {
        LOGGER.info("Testing multiple roles with different job types");

        // Create roles for different job types
        Role[] roles = {
            new Role("Preparazione primi", JobType.COOK),
            new Role("Preparazione secondi", JobType.COOK),
            new Role("Supervisione generale", JobType.CHEF),
            new Role("Servizio tavoli", JobType.SERVICE),
            new Role("Servizio bar", JobType.SERVICE)
        };

        // Verify all roles are created properly
        for (Role role : roles) {
            assertNotNull(role, "Role should not be null");
            assertNotNull(role.getDescription(), "Role description should not be null");
            assertNotNull(role.getType(), "Role type should not be null");
            assertFalse(role.isAssigned(), "Role should not be assigned initially");
        }

        // Test that we can assign staff to compatible roles (same job type)
        Staff cookStaff = StaffFactory.createStaff(JobType.COOK, true, "Paolo Cook", 
            "Via Cook 123", "333-4444444", "COOPAOL85M01F205W");

        // Assign cook to cook roles
        roles[0].setStaff(cookStaff); // Preparazione primi
        assertTrue(roles[0].isAssigned(), "Cook role should be assigned");
        assertEquals(cookStaff, roles[0].getAssignedStaff(), "Cook should be assigned to cook role");

        LOGGER.info("Successfully tested multiple roles with different job types");
    }
}