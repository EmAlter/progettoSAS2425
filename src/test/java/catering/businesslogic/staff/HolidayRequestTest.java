package catering.businesslogic.staff;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class HolidayRequestTest {

    private static final Logger LOGGER = LogManager.getLogger(HolidayRequestTest.class);

    private static CatERing app;
    private static User owner;
    private static User organizer;
    private static User regularStaff;
    private static Staff testCook;
    private static Staff testServiceStaff;
    private static Staff testChef;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        try {
            // Set up users with different roles
            owner = User.load("Francesca"); // From sample data - organizer with owner privileges
            assertNotNull(owner, "Owner user should be loaded");
            assertTrue(owner.isOwner(), "User should have owner role");
            
            organizer = User.load("Giovanni"); // From sample data - organizer
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");

            regularStaff = User.load("Luca"); // From sample data - cook
            assertNotNull(regularStaff, "Regular staff user should be loaded");
            assertTrue(regularStaff.isOperationalStaff(), "User should have a staff role");

            testCook = Staff.loadById(4);
            assertNotNull(testCook, "Test cook should be loaded");
            assertEquals(JobType.COOK, testCook.getJobType(), "Test cook should be of type COOK");
            assertEquals(20, testCook.getRemainingHolidays(), "Test cook should have 20 days available");

            testServiceStaff = Staff.loadById(2);
            assertEquals(JobType.SERVICE, testServiceStaff.getJobType(), "Test service staff should be of type SERVICE");
            assertEquals(10, testServiceStaff.getRemainingHolidays(), "Test service staff should have 10 days available");

            testChef = Staff.loadById(5);
            assertEquals(JobType.CHEF, testChef.getJobType(), "Test chef should be of type CHEF");
            assertEquals(5, testChef.getRemainingHolidays(), "Test chef should have 5 days available");

            // richieste di ferie già esistenti nel db

            LOGGER.info("Test setup completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Setup failed: " + e.getMessage());
            fail("Setup should not fail: " + e.getMessage());
        }
    }

    // Test per verificare la consultazione delle richieste di ferie esistenti nel database
    @Test
    @Order(1)
    void testConsultHolidayRequestsFromDatabase() {
        LOGGER.info("Testing consultation of existing holiday requests from database");

        try {
            // Login as organizer (authorized to consult requests)
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Consult holiday requests that exist in the database from sample data
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();

            // Verify we can access existing requests from the database
            assertNotNull(allRequests, "Holiday requests set should not be null");
            assertTrue(allRequests.size() >= 0, "Should be able to retrieve holiday requests");

            // Log information about found requests
            LOGGER.info("Found " + allRequests.size() + " holiday requests in the database");
            
            for (HolidayRequest req : allRequests) {
                LOGGER.info("Request: " + req.getRequester().getName() + " - " + 
                           req.getNumberOfDays() + " days - " + req.getDescription());
            }

            // If there are requests, verify they have valid data
            for (HolidayRequest req : allRequests) {
                assertNotNull(req.getRequester(), "Each request should have a requester");
                assertTrue(req.getNumberOfDays() > 0, "Each request should have positive days");
                assertNotNull(req.getRequestedDays(), "Each request should have requested days");
                assertFalse(req.getRequestedDays().isEmpty(), "Requested days should not be empty");
            }

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare specifiche richieste di ferie esistenti nel database
    @Test
    @Order(2)
    void testConsultSpecificHolidayRequests() {
        LOGGER.info("Testing consultation of specific holiday requests from sample data");

        try {
            // Login as organizer (authorized to consult requests)
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Consult all holiday requests
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();

            // The database should contain sample holiday requests from the init script
            assertNotNull(allRequests, "Holiday requests set should not be null");
        
            
            for (HolidayRequest req : allRequests) {
                String description = req.getDescription();
                if (description != null) {
                    if (description.contains("Matrimonio") || description.contains("fratello")) {
                        assertEquals("Matrimonio fratello", description, "Wedding request description should match");
                        LOGGER.info("Found wedding request for: " + req.getRequester().getName());
                    }
                    if (description.contains("Vacanza") || description.contains("estiva")) {
                        assertEquals("Vacanza estiva", description, "Vacation request description should match");
                        LOGGER.info("Found vacation request for: " + req.getRequester().getName());
                    }
                    if (description.contains("Ferie") || description.contains("natalizie")) {
                        assertEquals("Ferie natalizie", description, "Christmas request description should match");
                        LOGGER.info("Found Christmas request for: " + req.getRequester().getName());
                    }
                    if (description.contains("Settimana") || description.contains("riposo")) {
                        assertEquals("Settimana di riposo", description, "Rest request description should match");
                        LOGGER.info("Found rest request for: " + req.getRequester().getName());
                    }
                }
            }

            // Log all found requests for debugging
            LOGGER.info("Total requests found: " + allRequests.size());
            for (HolidayRequest req : allRequests) {
                LOGGER.info("Request ID " + req.getId() + ": " + req.getRequester().getName() + 
                           " - " + req.getDescription() + " (" + req.getNumberOfDays() + " days)");
            }

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare la disponibilità delle ferie per diversi membri dello staff
    @Test
    @Order(3)
    void testCheckHolidayAvailability() {
        LOGGER.info("Testing holiday availability checking");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Check availability for different staff members
            int cookAvailability = app.getStaffManager().getHolidaysAvailability(testCook);
            int serviceAvailability = app.getStaffManager().getHolidaysAvailability(testServiceStaff);
            int chefAvailability = app.getStaffManager().getHolidaysAvailability(testChef);

            // Verify availability matches expected values
            assertEquals(20, cookAvailability, "Cook should have 20 days available");
            assertEquals(10, serviceAvailability, "Service staff should have 10 days available");
            assertEquals(5, chefAvailability, "Chef should have 5 days available");

            LOGGER.info("Availability check results - Cook: " + cookAvailability + 
                       ", Service: " + serviceAvailability + ", Chef: " + chefAvailability);

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare l'accettazione di una richiesta di ferie
    @Test
    @Order(4)
    void testAcceptHolidayRequest() {
        LOGGER.info("Testing holiday request acceptance");

        try {
            // Login as owner (required for accepting requests)
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get existing holiday requests
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();

            // Find a request for testCook to accept
            HolidayRequest requestToAccept = null;
            for (HolidayRequest req : allRequests) {
                if (req.getRequester().equals(testCook) && 
                    req.getDescription().equals("Matrimonio fratello")) {
                    requestToAccept = req;
                    break;
                }
            }
            
            assertNotNull(requestToAccept, "Should find test cook's holiday request");

            // Check initial availability
            int initialAvailability = app.getStaffManager().getHolidaysAvailability(testCook);
            assertEquals(20, initialAvailability, "Cook should initially have 20 days");

            // Accept the holiday request
            app.getStaffManager().acceptHolidayRequest(requestToAccept);

            // Verify availability was reduced
            int updatedAvailability = app.getStaffManager().getHolidaysAvailability(testCook);
            int expectedDays = 20 - requestToAccept.getNumberOfDays();
            assertEquals(expectedDays, updatedAvailability, 
                        "Cook should now have " + expectedDays + " days remaining");

            // Verify holidays were added to staff
            Staff updatedStaff = Staff.loadById(testCook.getId());
            Set<LocalDate> staffHolidays = updatedStaff.getHolidays();
            
            for (LocalDate requestedDay : requestToAccept.getRequestedDays()) {
                assertTrue(staffHolidays.contains(requestedDay), 
                          "Staff holidays should contain requested day: " + requestedDay);
            }

            LOGGER.info("Successfully accepted holiday request. Remaining days: " + updatedAvailability);

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare il rifiuto di una richiesta di ferie
    @Test
    @Order(5)
    void testRejectHolidayRequest() {
        LOGGER.info("Testing holiday request rejection");

        try {
            // Login as owner (required for rejecting requests)
            app.getUserManager().fakeLogin(owner.getUserName());

            // Get existing holiday requests  
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();
            
            // Find a request for testServiceStaff to reject
            HolidayRequest requestToReject = null;
            for (HolidayRequest req : allRequests) {
                if (req.getRequester().equals(testServiceStaff) && 
                    req.getDescription().equals("Ferie natalizie")) {
                    requestToReject = req;
                    break;
                }
            }
            
            assertNotNull(requestToReject, "Should find test service staff's holiday request");

            // Check initial availability
            int initialAvailability = app.getStaffManager().getHolidaysAvailability(testServiceStaff);
            assertEquals(10, initialAvailability, "Service staff should initially have 10 days");

            // Reject the holiday request
            app.getStaffManager().rejectHolidayRequest(requestToReject);

            // Verify availability was NOT reduced (request was rejected)
            int unchangedAvailability = app.getStaffManager().getHolidaysAvailability(testServiceStaff);
            assertEquals(10, unchangedAvailability, "Service staff should still have 10 days");

            // Verify holidays were NOT added to staff
            Staff unchangedStaff = Staff.loadById(testServiceStaff.getId());
            Set<LocalDate> staffHolidays = unchangedStaff.getHolidays();
            
            for (LocalDate requestedDay : requestToReject.getRequestedDays()) {
                assertFalse(staffHolidays.contains(requestedDay), 
                           "Staff holidays should NOT contain rejected day: " + requestedDay);
            }

            LOGGER.info("Successfully rejected holiday request. Days remain: " + unchangedAvailability);

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare che uno staff regolare non possa consultare le richieste di ferie
    @Test
    @Order(6)
    void testUnauthorizedUserCannotConsultRequests() {
        LOGGER.info("Testing unauthorized access to holiday requests consultation");

        try {
            // Login as regular staff (not organizer or owner)
            app.getUserManager().fakeLogin(regularStaff.getUserName());

            // Attempt to consult holiday requests should fail
            assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().consultHolidayRequests();
            }, "Regular staff should not be able to consult holiday requests");

            LOGGER.info("Successfully prevented unauthorized consultation of holiday requests");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare che uno staff regolare non possa accettare le richieste di ferie
    @Test
    @Order(7)
    void testUnauthorizedUserCannotAcceptRequests() {
        LOGGER.info("Testing unauthorized access to holiday request acceptance");

        try {
            // Login as organizer (not owner - only owner can accept/reject)
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Get existing holiday requests
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();
            
            // Find any existing request to test with
            HolidayRequest anyRequest = null;
            if (!allRequests.isEmpty()) {
                anyRequest = allRequests.iterator().next();
            }

            // If no requests exist, we'll test with null (which should also fail with proper error)
            HolidayRequest requestToTest = anyRequest;

            // Attempt to accept should fail
            assertThrows(UseCaseLogicException.class, () -> {
                if (requestToTest != null) {
                    app.getStaffManager().acceptHolidayRequest(requestToTest);
                } else {
                    HolidayRequest testRequest = HolidayRequest.loadById(4);
                    app.getStaffManager().acceptHolidayRequest(testRequest);
                }
            }, "Non-owner should not be able to accept holiday requests");

            LOGGER.info("Successfully prevented unauthorized acceptance of holiday requests");

        } catch (UseCaseLogicException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    // Test per verificare che non sia possibile accettare una richiesta di ferie con giorni insufficienti
    @Test
    @Order(8)
    void testInsufficientHolidayDaysException() {
        LOGGER.info("Testing insufficient holiday days exception");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Create a request for more days than available (chef has only 5 days)
            HolidayRequest excessiveRequest = HolidayRequest.loadById(2); // This is a request for 6 days

            // Attempt to accept should fail due to insufficient days
            assertThrows(IllegalArgumentException.class, () -> {
                app.getStaffManager().acceptHolidayRequest(excessiveRequest);
            }, "Should not accept request exceeding available holiday days");

            // Verify chef's days are unchanged
            int unchangedAvailability = app.getStaffManager().getHolidaysAvailability(testChef);
            assertEquals(5, unchangedAvailability, "Chef should still have 5 days available");

            LOGGER.info("Successfully prevented acceptance of excessive holiday request");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown during setup: " + e.getMessage());
        }
    }

    // Test per verificare la consultazione della tabella dei turni
    @Test
    @Order(9)
    void testConsultShiftTable() {
        LOGGER.info("Testing shift table consultation scenario");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Consult shift table (part of the use case scenarios)
            List<Shift> shifts = app.getStaffManager().consultShiftTable();

            // Verify we can access shift information
            assertNotNull(shifts, "Shift table should not be null");
            assertTrue(shifts.size() >= 0, "Should be able to retrieve shift information");

            LOGGER.info("Successfully consulted shift table: " + shifts.size() + " shifts found");

            // Test with owner as well
            app.getUserManager().fakeLogin(owner.getUserName());
            
            List<Shift> ownerShifts = app.getStaffManager().consultShiftTable();
            assertNotNull(ownerShifts, "Owner should also be able to consult shift table");

            LOGGER.info("Owner can also consult shift table: " + ownerShifts.size() + " shifts found");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare la persistenza delle richieste di ferie
    // (verifica che le richieste create vengano salvate correttamente nel database)
    @Test
    @Order(10)
    void testHolidayRequestPersistence() {
        LOGGER.info("Testing holiday request persistence");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Create a new holiday request
            Set<LocalDate> requestedDates = new HashSet<>();
            requestedDates.add(LocalDate.of(2025, 12, 20));
            requestedDates.add(LocalDate.of(2025, 12, 21));

            HolidayRequest newRequest = app.getStaffManager().createHolidayRequest(
                testServiceStaff, 
                requestedDates, 
                "Test persistence"
            );

            assertNotNull(newRequest, "Request should be created");
            assertTrue(newRequest.getId() > 0, "Request should have valid ID");

            // Reload request from database to verify persistence
            HolidayRequest reloadedRequest = HolidayRequest.loadById(newRequest.getId());
            assertNotNull(reloadedRequest, "Request should be retrievable from database");
            assertEquals(newRequest.getDescription(), reloadedRequest.getDescription(), 
                        "Descriptions should match after reload");
            assertEquals(newRequest.getNumberOfDays(), reloadedRequest.getNumberOfDays(), 
                        "Number of days should match after reload");
            assertEquals(newRequest.getRequester().getId(), reloadedRequest.getRequester().getId(), 
                        "Requester should match after reload");

            // Verify requested dates persistence
            assertEquals(newRequest.getRequestedDays(), reloadedRequest.getRequestedDays(), 
                        "Requested dates should match after reload");

            LOGGER.info("Successfully verified holiday request persistence");

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    // Test per verificare il flusso completo di richieste di ferie
    @Test
    @Order(11)
    void testCompleteHolidayRequestWorkflow() {
        LOGGER.info("Testing complete holiday request workflow");

        try {
            // Login as organizer to consult requests
            app.getUserManager().fakeLogin(organizer.getUserName());

            // Consult all existing holiday requests
            Set<HolidayRequest> allRequests = app.getStaffManager().consultHolidayRequests();
            assertNotNull(allRequests, "Should be able to consult holiday requests");
            
            int initialRequestCount = allRequests.size();
            LOGGER.info("Found " + initialRequestCount + " holiday requests to process");

            // Check availabilities for all test staff
            int cookInitialAvailability = app.getStaffManager().getHolidaysAvailability(testCook);
            int serviceInitialAvailability = app.getStaffManager().getHolidaysAvailability(testServiceStaff);
            int chefInitialAvailability = app.getStaffManager().getHolidaysAvailability(testChef);

            assertEquals(20, cookInitialAvailability, "Cook should start with 20 days");
            assertEquals(10, serviceInitialAvailability, "Service staff should start with 10 days");
            assertEquals(5, chefInitialAvailability, "Chef should start with 5 days");

            // Switch to owner to process requests
            app.getUserManager().fakeLogin(owner.getUserName());

            // Find specific requests to process
            HolidayRequest cookRequest = null;
            HolidayRequest serviceRequest = null;
            HolidayRequest chefRequest = null;

            for (HolidayRequest req : allRequests) {
                if (req.getRequester().equals(testCook) && req.getDescription().equals("Matrimonio fratello")) {
                    cookRequest = req;
                } else if (req.getRequester().equals(testServiceStaff) && req.getDescription().equals("Ferie natalizie")) {
                    serviceRequest = req;
                } else if (req.getRequester().equals(testChef) && req.getDescription().equals("Vacanza estiva")) {
                    chefRequest = req;
                }
            }

            assertNotNull(cookRequest, "Should find cook's request");
            assertNotNull(serviceRequest, "Should find service staff's request");
            assertNotNull(chefRequest, "Should find chef's request");
            
            // Accept cook's request (reasonable request)
            app.getStaffManager().acceptHolidayRequest(cookRequest);
            LOGGER.info("Accepted cook's request for " + cookRequest.getNumberOfDays() + " days");

            // Reject service staff's request (management decision)
            app.getStaffManager().rejectHolidayRequest(serviceRequest);
            LOGGER.info("Rejected service staff's request for " + serviceRequest.getNumberOfDays() + " days");

            // Try to accept chef's excessive request (should fail)
            boolean exceptionThrown = false;
            try {
                app.getStaffManager().acceptHolidayRequest(chefRequest);
            } catch (IllegalArgumentException e) {
                exceptionThrown = true;
                LOGGER.info("Chef's excessive request correctly rejected: " + e.getMessage());
            }
            assertTrue(exceptionThrown, "Chef's excessive request should have thrown exception");

            // Cook's availability should be reduced
            int cookFinalAvailability = app.getStaffManager().getHolidaysAvailability(testCook);
            int expectedCookDays = cookInitialAvailability - cookRequest.getNumberOfDays();
            assertEquals(expectedCookDays, cookFinalAvailability, 
                        "Cook should have " + expectedCookDays + " days remaining");

            // Service staff's availability should be unchanged (request rejected)
            int serviceFinalAvailability = app.getStaffManager().getHolidaysAvailability(testServiceStaff);
            assertEquals(serviceInitialAvailability, serviceFinalAvailability, 
                        "Service staff should still have " + serviceInitialAvailability + " days");

            // Chef's availability should be unchanged (request failed)
            int chefFinalAvailability = app.getStaffManager().getHolidaysAvailability(testChef);
            assertEquals(chefInitialAvailability, chefFinalAvailability, 
                        "Chef should still have " + chefInitialAvailability + " days");

            // Verify holidays were added/not added correctly
            Staff finalCook = Staff.loadById(testCook.getId());
            Staff finalService = Staff.loadById(testServiceStaff.getId());
            Staff finalChef = Staff.loadById(testChef.getId());

            // Cook should have accepted holidays
            for (LocalDate day : cookRequest.getRequestedDays()) {
                assertTrue(finalCook.getHolidays().contains(day), 
                          "Cook should have accepted holiday: " + day);
            }

            // Service staff should NOT have rejected holidays
            for (LocalDate day : serviceRequest.getRequestedDays()) {
                assertFalse(finalService.getHolidays().contains(day), 
                           "Service staff should NOT have rejected holiday: " + day);
            }

            // Chef should NOT have the excessive holidays
            for (LocalDate day : chefRequest.getRequestedDays()) {
                assertFalse(finalChef.getHolidays().contains(day), 
                           "Chef should NOT have excessive holiday: " + day);
            }

            LOGGER.info("Successfully completed comprehensive holiday request workflow");
            LOGGER.info("Final availabilities - Cook: " + cookFinalAvailability +
                       ", Service: " + serviceFinalAvailability +
                       ", Chef: " + chefFinalAvailability);

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

}