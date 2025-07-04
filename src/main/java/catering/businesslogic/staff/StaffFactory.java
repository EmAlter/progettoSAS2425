package catering.businesslogic.staff;

public class StaffFactory {
    
    /**
     * Crea un'istanza di Staff basata sul JobType specificato.
     * 
     * @param jobType Il tipo di lavoro che determina quale sottoclasse di Staff creare.
     * @param permanent Indica se il personale è a tempo indeterminato.
     * @param name Il nome del personale.
     * @param address L'indirizzo del personale.
     * @param contact Le informazioni di contatto del personale.
     * @param fiscalCode Il codice fiscale del personale.
     * @return Un'istanza della sottoclasse appropriata di Staff.
     * @throws IllegalArgumentException Se il JobType non è supportato.
     */
    public static Staff createStaff(JobType jobType, boolean permanent, String name, 
                                   String address, String contact, String fiscalCode) {
        if (jobType == null) {
            throw new IllegalArgumentException("JobType cannot be null");
        }
        
        switch (jobType) {
            case COOK:
                return new Cook(permanent, name, address, contact, fiscalCode);
            case CHEF:
                return new Chef(permanent, name, address, contact, fiscalCode);
            case SERVICE_STAFF:
                return new ServiceStaff(permanent, name, address, contact, fiscalCode);
            default:
                throw new IllegalArgumentException("Unsupported JobType: " + jobType);
        }
    }
}
