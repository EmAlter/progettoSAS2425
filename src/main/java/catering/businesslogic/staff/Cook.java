package catering.businesslogic.staff;

public class Cook extends Staff {

    /**
     * Costruttore della classe Cook.
     * 
     * @param permanent Indica se il cuoco è a tempo indeterminato.
     * @param name Il nome del cuoco.
     * @param address L'indirizzo del cuoco.
     * @param contact Le informazioni di contatto del cuoco.
     * @param fiscalCode Il codice fiscale del cuoco.
     */
    public Cook(boolean permanent, String name, String address, String contact, String fiscalCode) {
        super(JobType.COOK, permanent, name, address, contact, fiscalCode);
    }

}
