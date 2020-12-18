package es.lab.music.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.lab.music.MusicApp;
import es.lab.music.domain.Album;
import es.lab.music.repository.AlbumRepository;
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
 * Integration tests for the {@link AlbumResource} REST controller.
 */
@SpringBootTest(classes = MusicApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class AlbumResourceIT {
    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlbumMockMvc;

    private Album album;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Album createEntity(EntityManager em) {
        Album album = new Album().name(DEFAULT_NAME);
        return album;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Album createUpdatedEntity(EntityManager em) {
        Album album = new Album().name(UPDATED_NAME);
        return album;
    }

    @BeforeEach
    public void initTest() {
        album = createEntity(em);
    }

    @Test
    @Transactional
    public void createAlbum() throws Exception {
        int databaseSizeBeforeCreate = albumRepository.findAll().size();
        // Create the Album
        restAlbumMockMvc
            .perform(post("/api/albums").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(album)))
            .andExpect(status().isCreated());

        // Validate the Album in the database
        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeCreate + 1);
        Album testAlbum = albumList.get(albumList.size() - 1);
        assertThat(testAlbum.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createAlbumWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = albumRepository.findAll().size();

        // Create the Album with an existing ID
        album.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlbumMockMvc
            .perform(post("/api/albums").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(album)))
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = albumRepository.findAll().size();
        // set the field null
        album.setName(null);

        // Create the Album, which fails.

        restAlbumMockMvc
            .perform(post("/api/albums").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(album)))
            .andExpect(status().isBadRequest());

        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAlbums() throws Exception {
        // Initialize the database
        albumRepository.saveAndFlush(album);

        // Get all the albumList
        restAlbumMockMvc
            .perform(get("/api/albums?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(album.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    public void getAlbum() throws Exception {
        // Initialize the database
        albumRepository.saveAndFlush(album);

        // Get the album
        restAlbumMockMvc
            .perform(get("/api/albums/{id}", album.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(album.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    public void getNonExistingAlbum() throws Exception {
        // Get the album
        restAlbumMockMvc.perform(get("/api/albums/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAlbum() throws Exception {
        // Initialize the database
        albumRepository.saveAndFlush(album);

        int databaseSizeBeforeUpdate = albumRepository.findAll().size();

        // Update the album
        Album updatedAlbum = albumRepository.findById(album.getId()).get();
        // Disconnect from session so that the updates on updatedAlbum are not directly saved in db
        em.detach(updatedAlbum);
        updatedAlbum.name(UPDATED_NAME);

        restAlbumMockMvc
            .perform(put("/api/albums").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(updatedAlbum)))
            .andExpect(status().isOk());

        // Validate the Album in the database
        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeUpdate);
        Album testAlbum = albumList.get(albumList.size() - 1);
        assertThat(testAlbum.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingAlbum() throws Exception {
        int databaseSizeBeforeUpdate = albumRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(put("/api/albums").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(album)))
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAlbum() throws Exception {
        // Initialize the database
        albumRepository.saveAndFlush(album);

        int databaseSizeBeforeDelete = albumRepository.findAll().size();

        // Delete the album
        restAlbumMockMvc
            .perform(delete("/api/albums/{id}", album.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Album> albumList = albumRepository.findAll();
        assertThat(albumList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
