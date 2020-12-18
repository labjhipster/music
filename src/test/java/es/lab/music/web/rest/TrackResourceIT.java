package es.lab.music.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.lab.music.MusicApp;
import es.lab.music.domain.Track;
import es.lab.music.repository.TrackRepository;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TrackResource} REST controller.
 */
@SpringBootTest(classes = MusicApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class TrackResourceIT {
    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTrackMockMvc;

    private Track track;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Track createEntity(EntityManager em) {
        Track track = new Track().name(DEFAULT_NAME);
        return track;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Track createUpdatedEntity(EntityManager em) {
        Track track = new Track().name(UPDATED_NAME);
        return track;
    }

    @BeforeEach
    public void initTest() {
        track = createEntity(em);
    }

    @Test
    @Transactional
    public void createTrack() throws Exception {
        int databaseSizeBeforeCreate = trackRepository.findAll().size();
        // Create the Track
        restTrackMockMvc
            .perform(post("/api/tracks").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(track)))
            .andExpect(status().isCreated());

        // Validate the Track in the database
        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeCreate + 1);
        Track testTrack = trackList.get(trackList.size() - 1);
        assertThat(testTrack.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createTrackWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = trackRepository.findAll().size();

        // Create the Track with an existing ID
        track.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTrackMockMvc
            .perform(post("/api/tracks").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(track)))
            .andExpect(status().isBadRequest());

        // Validate the Track in the database
        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = trackRepository.findAll().size();
        // set the field null
        track.setName(null);

        // Create the Track, which fails.

        restTrackMockMvc
            .perform(post("/api/tracks").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(track)))
            .andExpect(status().isBadRequest());

        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTracks() throws Exception {
        // Initialize the database
        trackRepository.saveAndFlush(track);

        // Get all the trackList
        restTrackMockMvc
            .perform(get("/api/tracks?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(track.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    public void getTrack() throws Exception {
        // Initialize the database
        trackRepository.saveAndFlush(track);

        // Get the track
        restTrackMockMvc
            .perform(get("/api/tracks/{id}", track.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(track.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    public void getNonExistingTrack() throws Exception {
        // Get the track
        restTrackMockMvc.perform(get("/api/tracks/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTrack() throws Exception {
        // Initialize the database
        trackRepository.saveAndFlush(track);

        int databaseSizeBeforeUpdate = trackRepository.findAll().size();

        // Update the track
        Track updatedTrack = trackRepository.findById(track.getId()).get();
        // Disconnect from session so that the updates on updatedTrack are not directly saved in db
        em.detach(updatedTrack);
        updatedTrack.name(UPDATED_NAME);

        restTrackMockMvc
            .perform(put("/api/tracks").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(updatedTrack)))
            .andExpect(status().isOk());

        // Validate the Track in the database
        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeUpdate);
        Track testTrack = trackList.get(trackList.size() - 1);
        assertThat(testTrack.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingTrack() throws Exception {
        int databaseSizeBeforeUpdate = trackRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTrackMockMvc
            .perform(put("/api/tracks").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(track)))
            .andExpect(status().isBadRequest());

        // Validate the Track in the database
        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteTrack() throws Exception {
        // Initialize the database
        trackRepository.saveAndFlush(track);

        int databaseSizeBeforeDelete = trackRepository.findAll().size();

        // Delete the track
        restTrackMockMvc
            .perform(delete("/api/tracks/{id}", track.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Track> trackList = trackRepository.findAll();
        assertThat(trackList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
