package swt2.virtualcollege.receive.auth;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – der "eingeloggte Nutzer".
 *
 * Nachdem der JwtAuthFilter ein Token geprueft hat, legt er dieses kleine Objekt
 * als "Principal" (= wer ist gerade angemeldet?) in den Spring-Security-Kontext.
 * Controller koennen es dann bequem per @AuthenticationPrincipal abrufen, ohne
 * das Token erneut zu entschluesseln.
 *
 * Die Angaben stammen direkt aus dem signierten Token, sind also vertrauenswuerdig.
 */
public record AngemeldeterNutzer(String email, String name, String rolle) {
}
