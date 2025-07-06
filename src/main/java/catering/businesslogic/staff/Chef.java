package catering.businesslogic.staff;

public class Chef extends Staff {

    /**
     * Costruttore della classe Chef.
     * 
     * @param permanent Indica se lo chef è a tempo indeterminato.
     * @param name Il nome dello chef.
     * @param address L'indirizzo dello chef.
     * @param contact Le informazioni di contatto dello chef.
     * @param fiscalCode Il codice fiscale dello chef.
     */
    public Chef(boolean permanent, String name, String address, String contact, String fiscalCode) {
        super(JobType.CHEF, permanent, name, address, contact, fiscalCode);
    }

}
