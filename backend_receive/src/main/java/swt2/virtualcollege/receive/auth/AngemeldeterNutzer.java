package swt2.virtualcollege.receive.auth;

/**
 * Der angemeldete Nutzer als Principal im Sicherheitskontext. Controller erhalten
 * ihn ueber @AuthenticationPrincipal; die Angaben stammen aus dem signierten Token.
 */
public record AngemeldeterNutzer(String email, String name, String rolle) {
}
