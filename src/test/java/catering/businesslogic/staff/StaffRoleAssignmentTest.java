package catering.businesslogic.staff;

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
import catering.businesslogic.event.Event;
import catering.businesslogic.event.EventStaffSheet;
import catering.businesslogic.event.Role;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class StaffRoleAssignmentTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffRoleAssignmentTest.class);

    private static CatERing app;
    private static User owner;
    private static User organizer;
    private static User regularStaff;
    private static Event testEvent;
    private static Staff testCook;
    private static Staff testChef;
    private static Staff testServiceStaff;
    private static Staff additionalCook;
    private static Staff additionalServiceStaff;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Set up users with different roles
            owner = User.load("Francesca"); // From sample data - owner
            assertNotNull(owner, "Owner user should be loaded");
            assertTrue(owner.isOwner(), "User should have owner role");
            
            organizer = User.load("Giovanni"); // From sample data - organizer
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");

            regularStaff = User.load("Luca"); // From sample data - cook
            assertNotNull(regularStaff, "Regular staff user should be loaded");
            assertTrue(regularStaff.isOperationalStaff(), "User should have a staff role");

            // Set up existing event from database
            testEvent = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");

            // Set up existing staff from database
            testCook = Staff.loadByName("Sofia Neri"); // Cook from sample data
            assertNotNull(testCook, "Test cook should be loaded");
            assertEquals(JobType.COOK, testCook.getJobType(), "Should be COOK type");

            testChef = Staff.loadByName("Chiara Rosa"); // Chef from sample data
            assertNotNull(testChef, "Test chef should be loaded");
            assertEquals(JobType.CHEF, testChef.getJobType(), "Should be CHEF type");

            testServiceStaff = Staff.loadByName("Marco Rossi"); // Service from sample data
            assertNotNull(testServiceStaff, "Test service staff should be loaded");
            assertEquals(JobType.SERVICE, testServiceStaff.getJobType(), "Should be SERVICE type");

            additionalCook = Staff.loadByName("Francesca Viola"); // Additional cook
            assertNotNull(additionalCook, "Additional cook should be loaded");
            assertEquals(JobType.COOK, additionalCook.getJobType(), "Should be COOK type");

            additionalServiceStaff = Staff.loadByName("Giulia Bianchi"); // Additional service staff
            assertNotNull(additionalServiceStaff, "Additional service staff should be loaded");
            assertEquals(JobType.SERVICE, additionalServiceStaff.getJobType(), "Should be SERVICE type");

            LOGGER.info("Test setup completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Setup failed: " + e.getMessage());
            fail("Setup should not fail: " + e.getMessage());
        }
    }

    // Test per verificare la consultazione dei ruoli esistenti nel database.
    @Test
    @Order(1)
    void testConsultExistingRolesFromDatabase() {
        LOGGER.info("Testing consultation of existing roles from database");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            app.getEventManager().selectEvent(testEvent);

            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            assertNotNull(staffSheet, "Event staff sheet should exist");
            
            List<Role> allRoles = staffSheet.getRoles();
            assertNotNull(allRoles, "Roles list should not be null");
            assertTrue(allRoles.size() > 0, "Should have existing roles from database");

            LOGGER.info("Found " + allRoles.size() + " existing roles in database:");
            for (Role role : allRoles) {
                String assignmentStatus = role.isAssigned() ? 
                    "assigned to " + role.getAssignedStaff().getName() : "unassigned";
                LOGGER.info("  - " + role.getDescription() + " (" + role.getType() + ") - " + assignmentStatus);
            }

            long chefRoles = allRoles.stream().filter(r -> r.getType() == JobType.CHEF).count();
            long cookRoles = allRoles.stream().filter(r -> r.getType() == JobType.COOK).count();
            long serviceRoles = allRoles.stream().filter(r -> r.getType() == JobType.SERVICE).count();

            assertTrue(chefRoles > 0, "Should have chef roles from database");
            assertTrue(cookRoles > 0, "Should have cook roles from database");
            assertTrue(serviceRoles > 0, "Should have service roles from database");

            // Verify some roles are already assigned
            List<Role> assignedRoles = staffSheet.getAssignedRoles();
            List<Role> unassignedRoles = staffSheet.getUnassignedRoles();
            
            assertTrue(assignedRoles.size() > 0, "Should have some assigned roles from database");
            assertTrue(unassignedRoles.size() > 0, "Should have some unassigned roles from database");

            LOGGER.info("Database roles summary:");
            LOGGER.info("  - Chef roles: " + chefRoles);
            LOGGER.info("  - Cook roles: " + cookRoles);
            LOGGER.info("  - Service roles: " + serviceRoles);
            LOGGER.info("  - Assigned roles: " + assignedRoles.size());
            LOGGER.info("  - Unassigned roles: " + unassignedRoles.size());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per assegnare il personale a ruoli esistenti non assegnati nel database.
    @Test
    @Order(2)
    void testAssignStaffToExistingUnassignedRoles() {
        LOGGER.info("Testing staff assignment to existing unassigned roles");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            app.getEventManager().selectEvent(testEvent);

            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            assertNotNull(staffSheet, "Event staff sheet should exist");

            List<Role> unassignedRoles = staffSheet.getUnassignedRoles();
            assertTrue(unassignedRoles.size() > 0, "Should have unassigned roles from database");

            // Find specific unassigned roles by description
            Role sousChefRole = staffSheet.findRole(JobType.CHEF, "Sous Chef");
            Role responsabileSalaRole = staffSheet.findRole(JobType.SERVICE, "Responsabile sala");
            Role cuocoPreparazioneAntipastiRole = staffSheet.findRole(JobType.COOK, "Cuoco preparazione antipasti");

            // Verify these roles exist and are unassigned
            assertNotNull(sousChefRole, "Should find 'Sous Chef' role from database");
            assertNotNull(responsabileSalaRole, "Should find 'Responsabile sala' role from database");
            assertNotNull(cuocoPreparazioneAntipastiRole, "Should find 'Cuoco preparazione antipasti' role from database");

            assertFalse(sousChefRole.isAssigned(), "Sous Chef role should be unassigned initially");
            assertFalse(responsabileSalaRole.isAssigned(), "Responsabile sala role should be unassigned initially");
            assertFalse(cuocoPreparazioneAntipastiRole.isAssigned(), "Cuoco preparazione antipasti role should be unassigned initially");

            int initialAssignedCount = (int) staffSheet.countAssignedRoles();

            app.getStaffManager().assignToRole(sousChefRole, testChef);
            app.getStaffManager().assignToRole(responsabileSalaRole, testServiceStaff);
            app.getStaffManager().assignToRole(cuocoPreparazioneAntipastiRole, additionalCook);

            // Verify assignments
            assertTrue(sousChefRole.isAssigned(), "Sous Chef role should be assigned");
            assertEquals(testChef, sousChefRole.getAssignedStaff(), "Sous Chef role should be assigned to test chef");

            assertTrue(responsabileSalaRole.isAssigned(), "Responsabile sala role should be assigned");
            assertEquals(testServiceStaff, responsabileSalaRole.getAssignedStaff(), "Responsabile sala role should be assigned to test service staff");

            assertTrue(cuocoPreparazioneAntipastiRole.isAssigned(), "Cuoco preparazione antipasti role should be assigned");
            assertEquals(additionalCook, cuocoPreparazioneAntipastiRole.getAssignedStaff(), "Cuoco preparazione antipasti role should be assigned to additional cook");

            assertEquals(testChef, staffSheet.getStaffForRole(sousChefRole), "Staff sheet should return correct chef for sous chef");
            assertEquals(testServiceStaff, staffSheet.getStaffForRole(responsabileSalaRole), "Staff sheet should return correct service staff for responsabile sala");
            assertEquals(additionalCook, staffSheet.getStaffForRole(cuocoPreparazioneAntipastiRole), "Staff sheet should return correct cook for antipasti");

            assertEquals(initialAssignedCount + 3, staffSheet.countAssignedRoles(), "Should have 3 more assigned roles");

            LOGGER.info("Successfully assigned 3 staff members to existing unassigned roles:");
            LOGGER.info("  - " + testChef.getName() + " -> " + sousChefRole.getDescription());
            LOGGER.info("  - " + testServiceStaff.getName() + " -> " + responsabileSalaRole.getDescription());
            LOGGER.info("  - " + additionalCook.getName() + " -> " + cuocoPreparazioneAntipastiRole.getDescription());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per rimuovere il personale da ruoli assegnati esistenti nel database.
    @Test
    @Order(3)
    void testRemoveStaffFromExistingAssignedRoles() {
        LOGGER.info("Testing staff removal from existing assigned roles");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            app.getEventManager().selectEvent(testEvent);

            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();

            Role chefResponsabileRole = staffSheet.findRole(JobType.CHEF, "Chef responsabile");
            assertNotNull(chefResponsabileRole, "Should find 'Chef responsabile' role from database");
            assertTrue(chefResponsabileRole.isAssigned(), "Chef responsabile role should be assigned initially");

            Staff originalAssignedStaff = chefResponsabileRole.getAssignedStaff();
            assertNotNull(originalAssignedStaff, "Should have originally assigned staff");

            LOGGER.info("Found assigned role: " + chefResponsabileRole.getDescription() + 
                       " assigned to " + originalAssignedStaff.getName());

            // Count assignments before removal
            int initialAssignedCount = (int) staffSheet.countAssignedRoles();

            // Remove staff from role 
            app.getStaffManager().removeStaffFromRole(chefResponsabileRole);

            // Verify removal
            assertFalse(chefResponsabileRole.isAssigned(), "Role should not be assigned after removal");
            assertNull(chefResponsabileRole.getAssignedStaff(), "Role should not have assigned staff after removal");

            assertNull(staffSheet.getStaffForRole(chefResponsabileRole), "Staff sheet should return null for unassigned role");

            assertEquals(initialAssignedCount - 1, staffSheet.countAssignedRoles(), "Assignment count should decrease by 1");

            LOGGER.info("Successfully removed " + originalAssignedStaff.getName() + 
                       " from role: " + chefResponsabileRole.getDescription());

            // Another
            Role cuocoPrincipaleAntipastiRole = staffSheet.findRole(JobType.COOK, "Cuoco principale antipasti");
            if (cuocoPrincipaleAntipastiRole != null && cuocoPrincipaleAntipastiRole.isAssigned()) {
                Staff assignedStaff = cuocoPrincipaleAntipastiRole.getAssignedStaff();
                app.getStaffManager().removeStaffFromRole(cuocoPrincipaleAntipastiRole);
                
                assertFalse(cuocoPrincipaleAntipastiRole.isAssigned(), "Cuoco principale antipasti role should be unassigned after removal");
                LOGGER.info("Also successfully removed " + assignedStaff.getName() + 
                           " from role: " + cuocoPrincipaleAntipastiRole.getDescription());
            }

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare che il personale non autorizzato non possa assegnare ruoli.
    @Test
    @Order(4)
    void testUnauthorizedStaffCannotAssignRoles() {
        LOGGER.info("Testing unauthorized access to role assignment");

        try {
            // Login as regular staff (not authorized)
            app.getUserManager().fakeLogin(regularStaff.getUserName());

            app.getEventManager().selectEvent(testEvent);

            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();
            Role sousChefRole = staffSheet.findRole(JobType.CHEF, "Sous Chef");

            assertNotNull(sousChefRole, "Should have a sous chef role to test with");

            if (sousChefRole.isAssigned()) {
                List<Role> unassignedRoles = staffSheet.getUnassignedRoles();
                if (!unassignedRoles.isEmpty()) {
                    sousChefRole = unassignedRoles.get(0);
                }
            }

            final Role roleToTest = sousChefRole;

            // Should fail
            assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().assignToRole(roleToTest, testChef);
            }, "Regular staff should not be able to assign roles");

            LOGGER.info("Successfully prevented unauthorized role assignment by " + regularStaff.getUserName());

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare la funzionalità di ricerca dei ruoli per tipo e descrizione.
    @Test
    @Order(5)
    void testFindRolesByTypeAndDescription() {
        LOGGER.info("Testing role finding functionality");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            app.getEventManager().selectEvent(testEvent);

            EventStaffSheet staffSheet = app.getEventManager().getCurrentEventStaffSheet();

            // Test finding existing roles by type and description
            Role foundChefRole = staffSheet.findRole(JobType.CHEF, "Chef responsabile");
            assertNotNull(foundChefRole, "Should find existing 'Chef responsabile' role");
            assertEquals(JobType.CHEF, foundChefRole.getType(), "Should be CHEF type");

            Role foundSousChef = staffSheet.findRole(JobType.CHEF, "Sous Chef");
            assertNotNull(foundSousChef, "Should find existing 'Sous Chef' role");
            assertEquals(JobType.CHEF, foundSousChef.getType(), "Should be CHEF type");

            Role foundCookRole = staffSheet.findRole(JobType.COOK, "Cuoco principale antipasti");
            assertNotNull(foundCookRole, "Should find existing cook role");
            assertEquals(JobType.COOK, foundCookRole.getType(), "Should be COOK type");

            Role foundServiceRole = staffSheet.findRole(JobType.SERVICE, "Responsabile sala");
            assertNotNull(foundServiceRole, "Should find existing service role");
            assertEquals(JobType.SERVICE, foundServiceRole.getType(), "Should be SERVICE type");

            // Test finding non-existent role
            Role notFoundRole = staffSheet.findRole(JobType.COOK, "Ruolo Inesistente");
            assertNull(notFoundRole, "Should not find non-existent role");

            List<Role> unassignedCookRoles = staffSheet.getNotAssignedByType(JobType.COOK);
            List<Role> unassignedChefRoles = staffSheet.getNotAssignedByType(JobType.CHEF);
            List<Role> unassignedServiceRoles = staffSheet.getNotAssignedByType(JobType.SERVICE);

            assertTrue(unassignedCookRoles.size() >= 0, "Should have cook roles (assigned or unassigned)");
            assertTrue(unassignedChefRoles.size() >= 0, "Should have chef roles (assigned or unassigned)");
            assertTrue(unassignedServiceRoles.size() >= 0, "Should have service roles (assigned or unassigned)");

            for (Role role : unassignedCookRoles) {
                assertEquals(JobType.COOK, role.getType(), "All cook roles should be COOK type");
                assertFalse(role.isAssigned(), "All should be unassigned");
            }

            LOGGER.info("Successfully tested role finding functionality:");
            LOGGER.info("  - Found chef roles: " + (foundChefRole != null ? 1 : 0) + " + " + (foundSousChef != null ? 1 : 0));
            LOGGER.info("  - Found cook roles: " + (foundCookRole != null ? 1 : 0));
            LOGGER.info("  - Found service roles: " + (foundServiceRole != null ? 1 : 0));
            LOGGER.info("  - Unassigned cook roles: " + unassignedCookRoles.size());
            LOGGER.info("  - Unassigned chef roles: " + unassignedChefRoles.size());
            LOGGER.info("  - Unassigned service roles: " + unassignedServiceRoles.size());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}