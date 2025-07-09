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
        User newUser = new User(name.split("\\s+")[0]);
        newUser.setStaffId(staffId);
    
        User.Role userRole = mapJobTypeToUserRole(jobType);
        newUser.addRole(userRole);
    
        return newUser.save() ? newUser : null;
    }

    public User.Role mapJobTypeToUserRole(JobType jobType) {
        switch (jobType) {
            case COOK: return User.Role.CUOCO;
            case CHEF: return User.Role.CHEF;
            case SERVICE: return User.Role.SERVIZIO;
            default: throw new IllegalArgumentException("Unsupported JobType: " + jobType);
        }
    }
}
