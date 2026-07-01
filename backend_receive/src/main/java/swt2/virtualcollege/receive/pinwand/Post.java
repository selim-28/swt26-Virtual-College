package swt2.virtualcollege.receive.pinwand;

import java.util.ArrayList;
import java.util.List;

/*
 * "Post" (Kapitel 13): ein Beitrag an der Pinwand. Ein Post "besteht aus"
 * Kommentaren und Likes (Post *-- Kommentar, Post *-- Like).
 */
public class Post {

    private final String titel;
    private final String inhalt;
    private final List<Kommentar> kommentare = new ArrayList<>();
    private final List<Like> likes = new ArrayList<>();

    public Post(String titel, String inhalt) {
        this.titel = titel;
        this.inhalt = inhalt;
    }

    public String titel() {
        return titel;
    }

    public String inhalt() {
        return inhalt;
    }

    public void addKommentar(Kommentar k) {
        kommentare.add(k);
    }

    public void addLike(Like l) {
        likes.add(l);
    }

    public List<Kommentar> getKommentare() {
        return kommentare;
    }

    public List<Like> getLikes() {
        return likes;
    }
}
