package es.lab.music.repository;

import es.lab.music.domain.Track;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Track entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {}
