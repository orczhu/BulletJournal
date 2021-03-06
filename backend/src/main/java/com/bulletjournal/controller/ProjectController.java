package com.bulletjournal.controller;

import com.bulletjournal.clients.UserClient;
import com.bulletjournal.controller.models.*;
import com.bulletjournal.controller.utils.EtagGenerator;
import com.bulletjournal.notifications.*;
import com.bulletjournal.repository.ProjectDaoJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class ProjectController {
    protected static final String PROJECTS_ROUTE = "/api/projects";
    protected static final String PROJECT_ROUTE = "/api/projects/{projectId}";
    protected static final String UPDATE_SHARED_PROJECTS_ORDER_ROUTE = "/api/updateSharedProjectsOrder";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
    @Autowired
    private ProjectDaoJpa projectDaoJpa;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserClient userClient;

    @GetMapping(PROJECTS_ROUTE)
    public ResponseEntity<Projects> getProjects() {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Projects projects = this.projectDaoJpa.getProjects(username);

        String ownedProjectsEtag = EtagGenerator.generateEtag(EtagGenerator.HashAlgorithm.MD5,
                EtagGenerator.HashType.TO_HASHCODE,
                projects.getOwned());

        String sharedProjectsEtag = EtagGenerator.generateEtag(EtagGenerator.HashAlgorithm.MD5,
                EtagGenerator.HashType.TO_HASHCODE,
                projects.getShared());

        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.setETag(ownedProjectsEtag + "|" + sharedProjectsEtag);

        projects.getOwned().forEach((p) -> addOwnerAvatar(p));
        projects.getShared().forEach((p) -> {
            p.setOwnerAvatar(this.userClient.getUser(p.getOwner()).getAvatar());
            p.getProjects().forEach((pp) -> addOwnerAvatar(pp));
        });
        return ResponseEntity.ok().headers(responseHeader).body(projects);
    }

    @GetMapping(PROJECT_ROUTE)
    public Project getProject(@NotNull @PathVariable Long projectId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        Project project = this.projectDaoJpa.getProject(projectId, username).toVerbosePresentationModel();
        return addOwnerAvatar(project);
    }

    private Project addOwnerAvatar(Project project) {
        project.setOwnerAvatar(this.userClient.getUser(project.getOwner()).getAvatar());
        for (Project child : project.getSubProjects()) {
            addOwnerAvatar(child);
        }
        return project;
    }

    @PostMapping(PROJECTS_ROUTE)
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(@Valid @RequestBody CreateProjectParams project) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        List<Event> events = new ArrayList<>();
        Project createdProject = projectDaoJpa.create(project, username, events).toVerbosePresentationModel();
        if (!events.isEmpty()) {
            this.notificationService.inform(new CreateProjectEvent(events, username));
        }
        return createdProject;
    }

    @PatchMapping(PROJECT_ROUTE)
    public Project updateProject(@NotNull @PathVariable Long projectId,
                                 @Valid @RequestBody UpdateProjectParams updateProjectParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        List<Event> joined = new ArrayList<>();
        List<Event> removed = new ArrayList<>();
        this.projectDaoJpa.partialUpdate(username, projectId, updateProjectParams, joined, removed);
        if (!joined.isEmpty()) {
            this.notificationService.inform(new JoinProjectEvent(joined, username));
        }
        if (!removed.isEmpty()) {
            this.notificationService.inform(new RemoveFromProjectEvent(removed, username));
        }
        return getProject(projectId);
    }

    /**
     * Delete project deletes its child projects as well
     */
    @DeleteMapping(PROJECT_ROUTE)
    public void deleteProject(@NotNull @PathVariable Long projectId) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        List<Event> events = this.projectDaoJpa.deleteProject(username, projectId);
        if (!events.isEmpty()) {
            this.notificationService.inform(new RemoveProjectEvent(events, username));
        }
    }

    @PostMapping(UPDATE_SHARED_PROJECTS_ORDER_ROUTE)
    public void updateSharedProjectsOrder(
            @Valid @RequestBody UpdateSharedProjectsOrderParams updateSharedProjectsOrderParams) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        LOGGER.info("updateSharedProjectsOrderParams {}",
                Arrays.toString(updateSharedProjectsOrderParams.getProjectOwners()));
        this.projectDaoJpa.updateSharedProjectsOrder(username, updateSharedProjectsOrderParams);
    }

    @PutMapping(PROJECTS_ROUTE)
    public ResponseEntity<Projects> updateProjectRelations(@Valid @RequestBody List<Project> projects) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        this.projectDaoJpa.updateUserOwnedProjects(username, projects);
        return getProjects();
    }
}