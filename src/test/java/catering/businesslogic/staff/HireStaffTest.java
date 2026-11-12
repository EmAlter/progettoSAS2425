package catering.businesslogic.staff;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class HireStaffTest {

    private static final Logger LOGGER = LogManager.getLogger(HireStaffTest.class);

    private static CatERing app;
    private static User owner;
    private static User organizer;
    private static User regularStaff;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Set up users with different roles
            owner = User.load("Francesca"); // From sample data - organizer with owner privileges
            assertNotNull(owner, "Owner user should be loaded");
            assertTrue(owner.isOwner(), "User should have owner role");

            organizer = User.load("Giovanni"); // From sample data - organizer without owner privileges
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");
            assertFalse(organizer.isOwner(), "Organizer Giovanni should not have owner privileges");

            regularStaff = User.load("Luca"); // From sample data - cook (regular staff)
            assertNotNull(regularStaff, "Regular staff user should be loaded");
            assertTrue(regularStaff.isOperationalStaff(), "User should have a staff role");
            assertFalse(regularStaff.isOwner(), "Regular staff Luca should not have owner privileges");

            LOGGER.info("Test setup completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Setup failed: " + e.getMessage());
            fail("Setup should not fail: " + e.getMessage());
        }
    }

    // Test per verificare la corretta assunzione di un cuoco permanente.
    @Test
    @Order(1)
    void testHirePermanentCook() {
        LOGGER.info("Testing hiring a permanent cook");

        try {
            // Login as owner (required for hiring)
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get initial staff count
            ArrayList<Staff> initialStaff = app.getStaffManager().getAllStaff();
            int initialCount = initialStaff.size();

            // Hire a new permanent cook
            Staff newCook = app.getStaffManager().hireStaff(
                    JobType.COOK,
                    true, // permanent
                    "Giuseppe Nuovo",
                    "Via Nuova 123, Milano",
                    "334-1111111",
                    "NVOGSP85M01F205X");

            // Verify staff was created properly
            assertNotNull(null, "New staff should not be null");
            assertEquals("Giuseppe Nuovo pppp", newCook.getName(), "Name should match");
            assertEquals("Via Nuova 123, Milano", newCook.getAddress(), "Address should match");
            assertEquals("334-1111111", newCook.getContact(), "Contact should match");
            assertEquals("NVOGSP85M01F205X", newCook.getFiscalCode(), "Fiscal code should match");
            assertEquals(JobType.COOK, newCook.getJobType(), "Job type should be COOK");
            assertTrue(newCook.isPermanent(), "Should be permanent");
            assertEquals(30, newCook.getRemainingHolidays(), "Should have default 30 days off");
            assertTrue(newCook.getId() > 0, "Should have valid database ID");

            // Verify staff was added to the system
            ArrayList<Staff> updatedStaff = app.getStaffManager().getAllStaff();
            assertEquals(initialCount + 1, updatedStaff.size(), "Staff count should increase by 1");
            assertTrue(updatedStaff.contains(newCook), "New staff should be in the list");

            // Verify staff can be found by different methods
            Staff foundByName = app.getStaffManager().findStaffByName("Giuseppe Nuovo");
            assertEquals(newCook, foundByName, "Should find staff by name");

            Staff foundByFiscalCode = app.getStaffManager().findStaffByFiscalCode("NVOGSP85M01F205X");
            assertEquals(newCook, foundByFiscalCode, "Should find staff by fiscal code");

            Staff foundById = app.getStaffManager().findStaffById(newCook.getId());
            assertEquals(newCook, foundById, "Should find staff by ID");

            LOGGER.info("Successfully hired permanent cook: " + newCook.getName() + " (ID: " + newCook.getId() + ")");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare l'assunzione di un membro dello staff occasionale.
    @Test
    @Order(2)
    void testHireOccasionalServiceStaff() {
        LOGGER.info("Testing hiring occasional service staff");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get initial service staff count
            ArrayList<Staff> initialServiceStaff = app.getStaffManager().getStaffByJobType(JobType.SERVICE);
            int initialServiceCount = initialServiceStaff.size();

            // Hire new occasional service staff
            Staff newServiceStaff = app.getStaffManager().hireStaff(
                    JobType.SERVICE,
                    false, // occasional (not permanent)
                    "Maria Occasionale",
                    "Via Temporanea 456, Milano",
                    "335-2222222",
                    "OCCMRA90F45F205Y");

            // Verify staff was created properly
            assertNotNull(newServiceStaff, "New service staff should not be null");
            assertEquals("Maria Occasionale", newServiceStaff.getName(), "Name should match");
            assertEquals("Via Temporanea 456, Milano", newServiceStaff.getAddress(), "Address should match");
            assertEquals("335-2222222", newServiceStaff.getContact(), "Contact should match");
            assertEquals("OCCMRA90F45F205Y", newServiceStaff.getFiscalCode(), "Fiscal code should match");
            assertEquals(JobType.SERVICE, newServiceStaff.getJobType(), "Job type should be SERVICE");
            assertFalse(newServiceStaff.isPermanent(), "Should be occasional (not permanent)");
            assertEquals(30, newServiceStaff.getRemainingHolidays(), "Should have default 30 days off");
            assertTrue(newServiceStaff.getId() > 0, "Should have valid database ID");

            // Verify service staff count increased
            ArrayList<Staff> updatedServiceStaff = app.getStaffManager().getStaffByJobType(JobType.SERVICE);
            assertEquals(initialServiceCount + 1, updatedServiceStaff.size(),
                    "Service staff count should increase by 1");

            LOGGER.info("Successfully hired occasional service staff: " + newServiceStaff.getName() +
                    " (ID: " + newServiceStaff.getId() + ")");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare l'assunzione di uno chef permanente.
    @Test
    @Order(3)
    void testHirePermanentChef() {
        LOGGER.info("Testing hiring a permanent chef");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get initial chef count
            ArrayList<Staff> initialChefs = app.getStaffManager().getStaffByJobType(JobType.CHEF);
            int initialChefCount = initialChefs.size();

            // Hire new permanent chef
            Staff newChef = app.getStaffManager().hireStaff(
                    JobType.CHEF,
                    true, // permanent
                    "Roberto Executivo",
                    "Via Esecutivo 789, Milano",
                    "336-3333333",
                    "EXERBT88L20F205Z");

            // Verify chef was created properly
            assertNotNull(newChef, "New chef should not be null");
            assertEquals("Roberto Executivo", newChef.getName(), "Name should match");
            assertEquals(JobType.CHEF, newChef.getJobType(), "Job type should be CHEF");
            assertTrue(newChef.isPermanent(), "Chef should be permanent");

            // Verify chef count increased
            ArrayList<Staff> updatedChefs = app.getStaffManager().getStaffByJobType(JobType.CHEF);
            assertEquals(initialChefCount + 1, updatedChefs.size(), "Chef count should increase by 1");

            LOGGER.info("Successfully hired permanent chef: " + newChef.getName() +
                    " (ID: " + newChef.getId() + ")");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare che un organizzatore senza privilegi di proprietario non
    // possa assumere.
    @Test
    @Order(4)
    void testUnauthorizedOrganizerCannotHire() {
        LOGGER.info("Testing that organizer without owner privileges cannot hire staff");

        try {
            // Login as organizer (not owner)
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Attempt to hire should fail
            assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        "Staff Non Autorizzato",
                        "Via Fallimento 123",
                        "337-4444444",
                        "FALNAU85M01F205W");
            }, "Non-owner should not be able to hire staff");

            LOGGER.info("Successfully prevented unauthorized hiring by organizer");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare che uno staff regolare non possa assumere.
    @Test
    @Order(5)
    void testUnauthorizedRegularStaffCannotHire() {
        LOGGER.info("Testing that regular staff cannot hire");

        try {
            // Login as regular staff
            app.getUserManager().fakeLogin(regularStaff.getUserName());

            // Attempt to hire should fail
            assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.SERVICE,
                        false,
                        "Staff Impossibile",
                        "Via Impossibile 456",
                        "338-5555555",
                        "IMPOSS90F45F205V");
            }, "Regular staff should not be able to hire");

            LOGGER.info("Successfully prevented unauthorized hiring by regular staff");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare che i dati di input siano validi durante l'assunzione.
    // Controlla che non siano accettati valori nulli o vuoti.
    @Test
    @Order(6)
    void testHireStaffWithInvalidData() {
        LOGGER.info("Testing hiring staff with invalid data");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Test with null name
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        null, // null name
                        "Via Test 123",
                        "339-6666666",
                        "TESTTS85M01F205U");
            }, "Should not accept null name");

            // Test with empty name
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        "", // empty name
                        "Via Test 123",
                        "339-6666666",
                        "TESTTS85M01F205U");
            }, "Should not accept empty name");

            // Test with null address
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        "Test Name",
                        null, // null address
                        "339-6666666",
                        "TESTTS85M01F205U");
            }, "Should not accept null address");

            // Test with null contact
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        "Test Name",
                        "Via Test 123",
                        null, // null contact
                        "TESTTS85M01F205U");
            }, "Should not accept null contact");

            // Test with null fiscal code
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().hireStaff(
                        JobType.COOK,
                        true,
                        "Test Name",
                        "Via Test 123",
                        "339-6666666",
                        null // null fiscal code
                );
            }, "Should not accept null fiscal code");

            LOGGER.info("Successfully validated input data requirements");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare l'assunzione di più membri dello staff di diversi tipi.
    @Test
    @Order(7)
    void testHireMultipleStaffOfDifferentTypes() {
        LOGGER.info("Testing hiring multiple staff of different types");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get initial counts
            ArrayList<Staff> initialAllStaff = app.getStaffManager().getAllStaff();
            ArrayList<Staff> initialCooks = app.getStaffManager().getStaffByJobType(JobType.COOK);
            ArrayList<Staff> initialChefs = app.getStaffManager().getStaffByJobType(JobType.CHEF);
            ArrayList<Staff> initialService = app.getStaffManager().getStaffByJobType(JobType.SERVICE);

            int initialTotalCount = initialAllStaff.size();
            int initialCookCount = initialCooks.size();
            int initialChefCount = initialChefs.size();
            int initialServiceCount = initialService.size();

            // Hire multiple staff
            Staff cook1 = app.getStaffManager().hireStaff(
                    JobType.COOK, true, "Paolo Cuoco", "Via Cuoco 1", "340-1111111", "CUOPAO85M01F205T");

            Staff cook2 = app.getStaffManager().hireStaff(
                    JobType.COOK, false, "Anna Cuoca", "Via Cuoco 2", "340-2222222", "CUOANN90F45F205S");

            Staff chef1 = app.getStaffManager().hireStaff(
                    JobType.CHEF, true, "Marco Chef", "Via Chef 1", "340-3333333", "CHEMAR88L20F205R");

            Staff service1 = app.getStaffManager().hireStaff(
                    JobType.SERVICE, true, "Lucia Servizio", "Via Servizio 1", "340-4444444", "SERLUC92D55F205Q");

            Staff service2 = app.getStaffManager().hireStaff(
                    JobType.SERVICE, false, "Davide Servizio", "Via Servizio 2", "340-5555555", "SERDAV89L15F205P");

            // Verify all staff were created
            assertNotNull(cook1, "Cook 1 should be created");
            assertNotNull(cook2, "Cook 2 should be created");
            assertNotNull(chef1, "Chef 1 should be created");
            assertNotNull(service1, "Service 1 should be created");
            assertNotNull(service2, "Service 2 should be created");

            // Verify counts
            ArrayList<Staff> finalAllStaff = app.getStaffManager().getAllStaff();
            ArrayList<Staff> finalCooks = app.getStaffManager().getStaffByJobType(JobType.COOK);
            ArrayList<Staff> finalChefs = app.getStaffManager().getStaffByJobType(JobType.CHEF);
            ArrayList<Staff> finalService = app.getStaffManager().getStaffByJobType(JobType.SERVICE);

            assertEquals(initialTotalCount + 5, finalAllStaff.size(),
                    "Total staff count should increase by 5");
            assertEquals(initialCookCount + 2, finalCooks.size(),
                    "Cook count should increase by 2");
            assertEquals(initialChefCount + 1, finalChefs.size(),
                    "Chef count should increase by 1");
            assertEquals(initialServiceCount + 2, finalService.size(),
                    "Service count should increase by 2");

            // Verify job types are correct
            assertEquals(JobType.COOK, cook1.getJobType(), "Cook 1 should have COOK job type");
            assertEquals(JobType.COOK, cook2.getJobType(), "Cook 2 should have COOK job type");
            assertEquals(JobType.CHEF, chef1.getJobType(), "Chef 1 should have CHEF job type");
            assertEquals(JobType.SERVICE, service1.getJobType(), "Service 1 should have SERVICE job type");
            assertEquals(JobType.SERVICE, service2.getJobType(), "Service 2 should have SERVICE job type");

            // Verify permanent status
            assertTrue(cook1.isPermanent(), "Cook 1 should be permanent");
            assertFalse(cook2.isPermanent(), "Cook 2 should be occasional");
            assertTrue(chef1.isPermanent(), "Chef 1 should be permanent");
            assertTrue(service1.isPermanent(), "Service 1 should be permanent");
            assertFalse(service2.isPermanent(), "Service 2 should be occasional");

            LOGGER.info("Successfully hired 5 staff members: 2 cooks, 1 chef, 2 service staff");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare la consultazione delle note di valutazione per le
    // decisioni di assunzione.
    @Test
    @Order(8)
    void testConsultEvaluationNotes() {
        LOGGER.info("Testing evaluation notes consultation for hiring decisions");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Step 2: Get occasional staff members who might be considered for permanent
            // positions
            ArrayList<Staff> occasionalStaff = app.getStaffManager().getStaffByPermanentStatus(false);
            assertNotNull(occasionalStaff, "Should be able to retrieve occasional staff");

            LOGGER.info("Found " + occasionalStaff.size() + " occasional staff members");

            // Consult all evaluation notes
            List<EvaluationNote> allNotes = app.getStaffManager().consultAllEvaluationNotes();
            assertNotNull(allNotes, "All evaluation notes should not be null");

            LOGGER.info("Total evaluation notes in system: " + allNotes.size());

            // Focus on evaluation notes for occasional staff
            List<EvaluationNote> occasionalStaffNotes = app.getStaffManager()
                    .consultEvaluationNotesForOccasionalStaff();
            assertNotNull(occasionalStaffNotes, "Occasional staff notes should not be null");

            LOGGER.info("Evaluation notes for occasional staff: " + occasionalStaffNotes.size());

            // Examine individual occasional staff members for hiring consideration
            for (Staff staff : occasionalStaff) {
                LOGGER.info("Evaluating occasional staff member: " + staff.getName() + " (" + staff.getJobType() + ")");

                // Get specific evaluation notes for this staff member
                List<EvaluationNote> staffNotes = app.getStaffManager().consultEvaluationNotesForStaff(staff);
                assertNotNull(staffNotes, "Staff notes should not be null for " + staff.getName());

                LOGGER.info("  - Found " + staffNotes.size() + " evaluation notes for " + staff.getName());
            }

            // Verify authorization - test that organizer can also consult notes for hiring
            // input
            app.getUserManager().fakeLogin(organizer.getUserName());

            List<EvaluationNote> organizerConsultedNotes = app.getStaffManager().consultAllEvaluationNotes();
            assertNotNull(organizerConsultedNotes, "Organizer should be able to consult evaluation notes");
            assertEquals(allNotes.size(), organizerConsultedNotes.size(),
                    "Organizer should see same notes as owner");

            LOGGER.info("Organizer can also consult evaluation notes for hiring input");

            // Verify comprehensive data integrity
            for (EvaluationNote note : allNotes) {
                assertNotNull(note.getNote(), "Note content should not be null");
                assertFalse(note.getNote().trim().isEmpty(), "Note content should not be empty");
                assertTrue(note.getEventId() > 0, "Note should have valid event ID");
                assertNotNull(note.getCreatedDate(), "Note should have creation date");
                assertTrue(note.getId() > 0, "Note should have valid database ID");
            }

            LOGGER.info("Successfully completed evaluation notes consultation for hiring decisions");
            LOGGER.info("This information is crucial for making informed hiring decisions about occasional staff");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare la persistenza dei dati dopo l'assunzione di uno staff.
    // Controlla che i dati siano correttamente salvati nel database e recuperabili
    @Test
    @Order(9)
    void testHireStaffPersistenceVerification() {
        LOGGER.info("Testing staff hiring persistence verification");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            Staff newStaff = app.getStaffManager().hireStaff(
                    JobType.SERVICE,
                    false,
                    "Verifica Persistenza",
                    "Via Database 999",
                    "342-8888888",
                    "VERPER92D55F205N");

            assertNotNull(newStaff, "Staff should be created");
            assertTrue(newStaff.getId() > 0, "Staff should have valid ID");

            // Reload staff from database to verify persistence
            Staff reloadedStaff = app.getStaffManager().findStaffById(newStaff.getId());
            assertNotNull(reloadedStaff, "Staff should be retrievable from database");
            assertEquals(newStaff.getName(), reloadedStaff.getName(), "Names should match after reload");
            assertEquals(newStaff.getJobType(), reloadedStaff.getJobType(), "Job types should match after reload");
            assertEquals(newStaff.isPermanent(), reloadedStaff.isPermanent(),
                    "Permanent status should match after reload");
            assertEquals(newStaff.getFiscalCode(), reloadedStaff.getFiscalCode(),
                    "Fiscal codes should match after reload");

            // Verify staff is in type-specific lists
            ArrayList<Staff> serviceStaff = app.getStaffManager().getStaffByJobType(JobType.SERVICE);
            assertTrue(serviceStaff.contains(reloadedStaff), "Staff should be in service staff list");

            ArrayList<Staff> occasionalStaff = app.getStaffManager().getStaffByPermanentStatus(false);
            assertTrue(occasionalStaff.contains(reloadedStaff), "Staff should be in occasional staff list");

            LOGGER.info("Successfully verified staff persistence");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare un workflow completo di assunzione dello staff.
    @Test
    @Order(10)
    void testCompleteHireStaffWorkflow() {
        LOGGER.info("Testing complete hire staff workflow");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            ArrayList<Staff> initialAllStaff = app.getStaffManager().getAllStaff();
            ArrayList<Staff> initialPermanentStaff = app.getStaffManager().getStaffByPermanentStatus(true);
            ArrayList<Staff> initialOccasionalStaff = app.getStaffManager().getStaffByPermanentStatus(false);

            int initialTotal = initialAllStaff.size();
            int initialPermanent = initialPermanentStaff.size();
            int initialOccasional = initialOccasionalStaff.size();

            // Hire diverse staff
            Staff permanentCook = app.getStaffManager().hireStaff(
                    JobType.COOK, true, "Alessandro Permanente",
                    "Via Permanente 1", "343-1111111", "PERALE85M01F205M");

            Staff occasionalService = app.getStaffManager().hireStaff(
                    JobType.SERVICE, false, "Federica Occasionale",
                    "Via Occasionale 2", "343-2222222", "OCCFED90F45F205L");

            Staff permanentChef = app.getStaffManager().hireStaff(
                    JobType.CHEF, true, "Stefano Capo",
                    "Via Capo 3", "343-3333333", "CAPSTE88L20F205K");

            // Verify all staff were created successfully
            assertNotNull(permanentCook, "Permanent cook should be created");
            assertNotNull(occasionalService, "Occasional service staff should be created");
            assertNotNull(permanentChef, "Permanent chef should be created");

            // Verify final counts
            ArrayList<Staff> finalAllStaff = app.getStaffManager().getAllStaff();
            ArrayList<Staff> finalPermanentStaff = app.getStaffManager().getStaffByPermanentStatus(true);
            ArrayList<Staff> finalOccasionalStaff = app.getStaffManager().getStaffByPermanentStatus(false);

            assertEquals(initialTotal + 3, finalAllStaff.size(), "Total should increase by 3");
            assertEquals(initialPermanent + 2, finalPermanentStaff.size(), "Permanent should increase by 2");
            assertEquals(initialOccasional + 1, finalOccasionalStaff.size(), "Occasional should increase by 1");

            // Verify staff are retrievable through various methods
            assertEquals(permanentCook, app.getStaffManager().findStaffByName("Alessandro Permanente"));
            assertEquals(occasionalService, app.getStaffManager().findStaffByFiscalCode("OCCFED90F45F205L"));
            assertEquals(permanentChef, app.getStaffManager().findStaffById(permanentChef.getId()));

            // Verify staff attributes are correct
            assertTrue(permanentCook.isPermanent(), "Cook should be permanent");
            assertEquals(JobType.COOK, permanentCook.getJobType(), "Should be COOK type");
            assertEquals(30, permanentCook.getRemainingHolidays(), "Should have 30 holiday days");

            assertFalse(occasionalService.isPermanent(), "Service staff should be occasional");
            assertEquals(JobType.SERVICE, occasionalService.getJobType(), "Should be SERVICE type");

            assertTrue(permanentChef.isPermanent(), "Chef should be permanent");
            assertEquals(JobType.CHEF, permanentChef.getJobType(), "Should be CHEF type");

            LOGGER.info("Successfully completed comprehensive hire staff workflow");
            LOGGER.info("Hired 3 staff members: 1 permanent cook, 1 occasional service, 1 permanent chef");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

}