package es.lab.music.domain;

import static org.assertj.core.api.Assertions.assertThat;

import es.lab.music.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

public class ArtistTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Artist.class);
        Artist artist1 = new Artist();
        artist1.setId(1L);
        Artist artist2 = new Artist();
        artist2.setId(artist1.getId());
        assertThat(artist1).isEqualTo(artist2);
        artist2.setId(2L);
        assertThat(artist1).isNotEqualTo(artist2);
        artist1.setId(null);
        assertThat(artist1).isNotEqualTo(artist2);
    }
}
