package com.bulletjournal.controller;

import com.bulletjournal.controller.models.*;
import com.bulletjournal.controller.utils.TestHelpers;
import com.bulletjournal.es.repository.models.ProjectItemNameIndex;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link ProjectController}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class QueryControllerTest {
    private static final String ROOT_URL = "http://localhost:";
    private static final String TIMEZONE = "America/Los_Angeles";
    private final String[] sampleUsers = {
            "Michael_Zhou"
    };
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort
    int randomServerPort;

    @Before
    public void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void testSearch() {
        Group group = createGroup();
        Project p1 = createProject("P_Note", group, ProjectType.NOTE);

        List<Label> labels = createLabels();
        List<Note> notes = createNotes(p1, labels);

        for (Note note : notes) {
            List<ProjectItemNameIndex> projectItemNameIndices = searchNote(p1, note);
            assertEquals(projectItemNameIndices.size(), 1);
            assertEquals(projectItemNameIndices.get(0).getName(), note.getName());
        }
    }

    private List<ProjectItemNameIndex> searchNote(Project project, Note note) {
        String name = note.getName();

        String url = ROOT_URL + randomServerPort + QueryController.SEARCH_ROUTE;
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("term", name);

        ResponseEntity<ProjectItemNameIndex[]> response = this.restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                TestHelpers.actAsOtherUser(null, sampleUsers[0]),
                ProjectItemNameIndex[].class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        return Arrays.asList(response.getBody());
    }

    private Note createNote(Project p, String noteName) {
        CreateNoteParams note = new CreateNoteParams(noteName);
        ResponseEntity<Note> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + NoteController.NOTES_ROUTE,
                HttpMethod.POST,
                new HttpEntity<>(note),
                Note.class,
                p.getId());
        Note created = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assert created != null;
        assertEquals(noteName, created.getName());
        assertEquals(p.getId(), created.getProjectId());
        return created;
    }

    private List<Note> createNotes(Project p, List<Label> labels) {
        Note note1 = createNote(p, "n1");
        Note note2 = createNote(p, "n2");
        Note note3 = createNote(p, "n3");

        // Attach Labels to notes
        ResponseEntity<Note> setLabelResponse = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + NoteController.NOTE_SET_LABELS_ROUTE,
                HttpMethod.PUT,
                new HttpEntity<>(labels.stream().map(l -> l.getId()).collect(Collectors.toList())),
                Note.class,
                note2.getId());
        assertEquals(HttpStatus.OK, setLabelResponse.getStatusCode());
        note2 = setLabelResponse.getBody();
        assertEquals(labels.size(), note2.getLabels().size());

        ProjectItems projectItems = new ProjectItems();
        projectItems.setNotes(ImmutableList.of(note2));
        findItemsByLabels(labels, ImmutableList.of(projectItems));

        List<Note> result = new ArrayList<>();
        result.add(note1);
        result.add(note2);
        result.add(note3);
        return result;
    }

    private List<Label> createLabels() {
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CreateLabelParams createLabelParams = new CreateLabelParams("Label" + i, "Icon" + i);
            ResponseEntity<Label> response = this.restTemplate.exchange(
                    ROOT_URL + randomServerPort + LabelController.LABELS_ROUTE,
                    HttpMethod.POST,
                    new HttpEntity<>(createLabelParams),
                    Label.class);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            labels.add(response.getBody());
        }

        ResponseEntity<Label[]> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + LabelController.LABELS_ROUTE,
                HttpMethod.GET,
                null,
                Label[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Label[] labelsCreated = response.getBody();
        assertEquals(7, labelsCreated.length);
        return labels;
    }

    private void findItemsByLabels(List<Label> labels, List<ProjectItems> expectedItems) {
        String url = ROOT_URL + randomServerPort + LabelController.ITEMS_ROUTE;
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("labels", labels.stream().map(l -> l.getId()).collect(Collectors.toList()));
        ResponseEntity<ProjectItems[]> response = this.restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                ProjectItems[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<ProjectItems> items = Arrays.asList(response.getBody());
        for (int i = 0; i < expectedItems.size(); i++) {
            assertEquals(expectedItems.get(i).getNotes(), items.get(i).getNotes());
            assertEquals(expectedItems.get(i).getTasks(), items.get(i).getTasks());
            assertEquals(expectedItems.get(i).getTransactions(), items.get(i).getTransactions());
        }
    }

    private Group createGroup() {
        CreateGroupParams group = new CreateGroupParams("Group_QueryController");

        ResponseEntity<Group> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + GroupController.GROUPS_ROUTE,
                HttpMethod.POST,
                TestHelpers.actAsOtherUser(group, sampleUsers[0]),
                Group.class);
        Group created = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(created);
        assertEquals("Group_QueryController", created.getName());
        assertEquals(sampleUsers[0], created.getOwner());

        return created;
    }

    private Project createProject(String projectName, Group g, ProjectType type) {
        CreateProjectParams project = new CreateProjectParams(
                projectName, type, "d16", g.getId());

        ResponseEntity<Project> response = this.restTemplate.exchange(
                ROOT_URL + randomServerPort + ProjectController.PROJECTS_ROUTE,
                HttpMethod.POST,
                TestHelpers.actAsOtherUser(project, sampleUsers[0]),
                Project.class);
        Project created = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(created);
        assertEquals(projectName, created.getName());
        assertEquals(sampleUsers[0], created.getOwner());
        assertEquals(type, created.getProjectType());
        assertEquals("Group_QueryController", created.getGroup().getName());
        assertEquals(sampleUsers[0], created.getGroup().getOwner());
        assertEquals("d16", created.getDescription());
        return created;
    }
}
