package es.lab.music.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Album.
 */
@Entity
@Table(name = "album")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(unique = true)
    private Artist artist;

    @OneToOne
    @JoinColumn(unique = true)
    private Genre genre;

    @OneToMany(mappedBy = "album")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Track> tracks = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Album name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album artist(Artist artist) {
        this.artist = artist;
        return this;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Genre getGenre() {
        return genre;
    }

    public Album genre(Genre genre) {
        this.genre = genre;
        return this;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Set<Track> getTracks() {
        return tracks;
    }

    public Album tracks(Set<Track> tracks) {
        this.tracks = tracks;
        return this;
    }

    public Album addTrack(Track track) {
        this.tracks.add(track);
        track.setAlbum(this);
        return this;
    }

    public Album removeTrack(Track track) {
        this.tracks.remove(track);
        track.setAlbum(null);
        return this;
    }

    public void setTracks(Set<Track> tracks) {
        this.tracks = tracks;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Album)) {
            return false;
        }
        return id != null && id.equals(((Album) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Album{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
