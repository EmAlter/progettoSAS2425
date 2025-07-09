package catering.businesslogic.staff;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.user.User;

class StaffManagerTest {
    private StaffManager staffManager;
    
    @BeforeEach
    void setUp() {
        staffManager = CatERing.getInstance().getStaffManager();
        
        // Simulate a logged-in owner
        User owner = new User("testOwner");
        owner.addRole(User.Role.PROPRIETARIO);
        owner.setId(1);
        
        // Set the current user to the owner
        CatERing.getInstance().getUserManager().setCurrentUser(owner);
    }

    /**
     * Test per verificare che il metodo hireStaff crei correttamente un nuovo Staff e un User associato.
     * @throws UseCaseLogicException
     */
    @Test
    void testHireStaffCreatesUserAndStaff() throws UseCaseLogicException {
        // Arrange
        JobType jobType = JobType.COOK;
        boolean permanent = true;
        String name = "Artoria Pendragon";
        String address = "Via Avalon 123";
        String contact = "artoria.pendragon@email.com";
        String fiscalCode = "PNDART80A01H501X";
        
        // Act
        Staff newStaff = staffManager.hireStaff(jobType, permanent, name, address, contact, fiscalCode);
        
        // Assert
        assertNotNull(newStaff);
        assertEquals(name, newStaff.getName());
        assertEquals(address, newStaff.getAddress());
        assertEquals(contact, newStaff.getContact());
        assertEquals(fiscalCode, newStaff.getFiscalCode());
        assertEquals(jobType, newStaff.getJobType());
        assertEquals(permanent, newStaff.isPermanent());
        
        // Verify staff was saved (has an ID)
        assertTrue(newStaff.getId() > 0);
        
        // Verify user was created and linked to staff
        User createdUser = User.loadAllUsers().stream()
            .filter(u -> u.getStaffId() == newStaff.getId())
            .findFirst()
            .orElse(null);
        
        assertNotNull(createdUser, "User should be created and linked to staff with ID " + newStaff.getId());
        assertEquals("Artoria", createdUser.getUserName()); // Username should be first name only
        assertTrue(createdUser.isCook()); // Should have CUOCO role for COOK job type
        assertEquals(newStaff.getId(), createdUser.getStaffId());
    }

    /**
     * Test per verificare che il metodo hireStaff gestisca correttamente diversi tipi di lavoro.
     * @throws UseCaseLogicException
     */
    @Test
    void testHireStaffWithDifferentJobTypes() throws UseCaseLogicException {
        // Test CHEF
        Staff chef = staffManager.hireStaff(JobType.CHEF, true, "Chef Test", "Via Test 1", "chef@test.com", "CHFTST80A01H501X");
        User chefUser = User.loadAllUsers().stream()
            .filter(u -> u.getStaffId() == chef.getId())
            .findFirst()
            .orElse(null);
        assertNotNull(chefUser);
        assertTrue(chefUser.isChef());
        
        // Test SERVICE
        Staff service = staffManager.hireStaff(JobType.SERVICE, false, "Service Test", "Via Test 2", "service@test.com", "SRVTST80A01H501Y");
        User serviceUser = User.loadAllUsers().stream()
            .filter(u -> u.getStaffId() == service.getId())
            .findFirst()
            .orElse(null);
        assertNotNull(serviceUser);
        assertTrue(serviceUser.isServiceStaff());
    }

    /**
     * Test per verificare che il metodo hireStaff lanci un'eccezione se l'utente corrente non è il proprietario.
     * @throws UseCaseLogicException
     */
    @Test
    void testHireStaffUnauthorizedThrowsException() {
        // Arrange - Set current user to non-owner
        User cook = new User("testCook");
        cook.addRole(User.Role.CUOCO);
        cook.setId(2);
        CatERing.getInstance().getUserManager().setCurrentUser(cook);
        
        // Act & Assert
        assertThrows(UseCaseLogicException.class, () -> {
            staffManager.hireStaff(JobType.COOK, true, "Test", "Address", "Contact", "FISCAL");
        });
    }
}
