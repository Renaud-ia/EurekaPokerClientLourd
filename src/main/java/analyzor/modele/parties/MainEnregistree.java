package analyzor.modele.parties;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class MainEnregistree {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private long idNonUnique;

    // todo int ou string???
    private int heroCombo;
    private int montantBB;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Partie partie;

    //constructeurs
    public MainEnregistree() {}

    //getters, setters...

    private Long getId() {
        return id;
    }


    // recommandé de réécrire equals et hashCode quand relation réciproque
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainEnregistree)) return false;
        return id != null && id.equals(((MainEnregistree) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}