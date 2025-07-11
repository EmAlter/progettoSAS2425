package catering.businesslogic.user;

import java.util.logging.Logger;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staff.JobType;
import catering.util.LogManager;

public class UserManager {
    private static final Logger LOGGER = LogManager.getLogger(UserManager.class);

    private User currentUser;

    public void fakeLogin(String username) throws UseCaseLogicException {
        LOGGER.info("Attempting login for user: " + username);
        this.currentUser = User.load(username);
        if (this.currentUser == null) {
            LOGGER.warning("Login failed: user not found - " + username);
            throw new UseCaseLogicException("User not found");
        }
        LOGGER.info("User successfully logged in: " + username);
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User createUserForNewStaff(String name, JobType jobType, int staffId) {
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warning("Cannot create user with null or empty name");
            return null;
        }
        
        // Genera un username unico
        String uniqueUsername = generateUniqueUsername(name.trim());
        
        if (uniqueUsername == null) {
            LOGGER.warning("Failed to generate unique username for: " + name);
            return null;
        }
        
        LOGGER.info("Creating user with username: " + uniqueUsername + " for staff: " + name);
        
        User newUser = new User(uniqueUsername);
        newUser.setStaffId(staffId);

        User.Role userRole = mapJobTypeToUserRole(jobType);
        newUser.addRole(userRole);

        return newUser.save() ? newUser : null;
    }

    /**
     * Genera un username unico basato sul nome completo della persona.
     * Prima prova con solo il nome, poi nome + prima lettera cognome
     * 
     * @param fullName Il nome completo (es. "Mario Rossi")
     * @return Un username unico, o null se non riesce a generarne uno
     */
    private String generateUniqueUsername(String fullName) {
        String[] nameParts = fullName.split("\\s+");
        
        if (nameParts.length == 0) {
            return null;
        }
        
        String firstName = nameParts[0];
        
        // Strategia 1: Solo il nome (comportamento originale)
        if (!User.usernameExists(firstName)) {
            return firstName;
        }
        
        LOGGER.info("Username '" + firstName + "' already exists, trying alternatives...");
        
        // Strategia 2: Nome + prima lettera del cognome
        if (nameParts.length > 1) {
            String lastName = nameParts[1];
            String nameWithInitial = firstName + lastName.charAt(0);
            
            if (!User.usernameExists(nameWithInitial)) {
                return nameWithInitial;
            }
            
            LOGGER.info("Username '" + nameWithInitial + "' already exists, trying with numbers...");
            
            // Strategia 3: Nome + prima lettera cognome + numero
            for (int i = 1; i <= 99; i++) {
                String numberedUsername = nameWithInitial + i;
                if (!User.usernameExists(numberedUsername)) {
                    return numberedUsername;
                }
            }
        }
        
        LOGGER.severe("Could not generate unique username for: " + fullName);
        return null;
    }

    /**
     * Ritorna il ruolo utente corrispondente al tipo di lavoro
     * 
     * @param jobType Il tipo di lavoro
     * @return Il ruolo utente corrispondente
     */
    public User.Role mapJobTypeToUserRole(JobType jobType) {
        switch (jobType) {
            case COOK: return User.Role.CUOCO;
            case CHEF: return User.Role.CHEF;
            case SERVICE: return User.Role.SERVIZIO;
            default: throw new IllegalArgumentException("Unsupported JobType: " + jobType);
        }
    }
}
