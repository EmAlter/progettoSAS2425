package catering.businesslogic.staff;

public class ServiceStaff extends Staff {

    /**
     * Costruttore della classe ServiceStaff.
     * 
     * @param permanent Indica se il personale di servizio è a tempo indeterminato.
     * @param name Il nome del personale di servizio.
     * @param address L'indirizzo del personale di servizio.
     * @param contact Le informazioni di contatto del personale di servizio.
     * @param fiscalCode Il codice fiscale del personale di servizio.
     */
    public ServiceStaff(boolean permanent, String name, String address, String contact, String fiscalCode) {
        super(JobType.SERVICE, permanent, name, address, contact, fiscalCode);
    }

}
