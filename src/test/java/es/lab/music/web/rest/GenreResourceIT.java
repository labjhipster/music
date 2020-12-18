package es.lab.music.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.lab.music.MusicApp;
import es.lab.music.domain.Genre;
import es.lab.music.repository.GenreRepository;
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
 * Integration tests for the {@link GenreResource} REST controller.
 */
@SpringBootTest(classes = MusicApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class GenreResourceIT {
    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGenreMockMvc;

    private Genre genre;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Genre createEntity(EntityManager em) {
        Genre genre = new Genre().name(DEFAULT_NAME);
        return genre;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Genre createUpdatedEntity(EntityManager em) {
        Genre genre = new Genre().name(UPDATED_NAME);
        return genre;
    }

    @BeforeEach
    public void initTest() {
        genre = createEntity(em);
    }

    @Test
    @Transactional
    public void createGenre() throws Exception {
        int databaseSizeBeforeCreate = genreRepository.findAll().size();
        // Create the Genre
        restGenreMockMvc
            .perform(post("/api/genres").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(genre)))
            .andExpect(status().isCreated());

        // Validate the Genre in the database
        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeCreate + 1);
        Genre testGenre = genreList.get(genreList.size() - 1);
        assertThat(testGenre.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createGenreWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = genreRepository.findAll().size();

        // Create the Genre with an existing ID
        genre.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restGenreMockMvc
            .perform(post("/api/genres").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(genre)))
            .andExpect(status().isBadRequest());

        // Validate the Genre in the database
        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = genreRepository.findAll().size();
        // set the field null
        genre.setName(null);

        // Create the Genre, which fails.

        restGenreMockMvc
            .perform(post("/api/genres").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(genre)))
            .andExpect(status().isBadRequest());

        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllGenres() throws Exception {
        // Initialize the database
        genreRepository.saveAndFlush(genre);

        // Get all the genreList
        restGenreMockMvc
            .perform(get("/api/genres?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(genre.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    public void getGenre() throws Exception {
        // Initialize the database
        genreRepository.saveAndFlush(genre);

        // Get the genre
        restGenreMockMvc
            .perform(get("/api/genres/{id}", genre.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(genre.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    public void getNonExistingGenre() throws Exception {
        // Get the genre
        restGenreMockMvc.perform(get("/api/genres/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateGenre() throws Exception {
        // Initialize the database
        genreRepository.saveAndFlush(genre);

        int databaseSizeBeforeUpdate = genreRepository.findAll().size();

        // Update the genre
        Genre updatedGenre = genreRepository.findById(genre.getId()).get();
        // Disconnect from session so that the updates on updatedGenre are not directly saved in db
        em.detach(updatedGenre);
        updatedGenre.name(UPDATED_NAME);

        restGenreMockMvc
            .perform(put("/api/genres").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(updatedGenre)))
            .andExpect(status().isOk());

        // Validate the Genre in the database
        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeUpdate);
        Genre testGenre = genreList.get(genreList.size() - 1);
        assertThat(testGenre.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingGenre() throws Exception {
        int databaseSizeBeforeUpdate = genreRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGenreMockMvc
            .perform(put("/api/genres").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(genre)))
            .andExpect(status().isBadRequest());

        // Validate the Genre in the database
        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteGenre() throws Exception {
        // Initialize the database
        genreRepository.saveAndFlush(genre);

        int databaseSizeBeforeDelete = genreRepository.findAll().size();

        // Delete the genre
        restGenreMockMvc
            .perform(delete("/api/genres/{id}", genre.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Genre> genreList = genreRepository.findAll();
        assertThat(genreList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
