package catering.businesslogic.staff;

/** La nota di valutazione non ha l'attributo Staff, 
 * perchè viene assegnata al Set di note di un determinato dipendente una volta creata */

public class EvaluationNote {
    private String note;
    private int EventId;

    public EvaluationNote(String note, int eventId) {
        this.note = note;
        this.EventId = eventId;
    }

    public String getNote() {
        return note;
    }

    public int getEventId() {
        return EventId;
    }
}
