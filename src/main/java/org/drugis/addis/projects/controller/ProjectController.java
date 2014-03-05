package org.drugis.addis.projects.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ProjectController extends AbstractAddisCoreController {

  final static Logger logger = LoggerFactory.getLogger(ProjectController.class);

  @Inject
  private ProjectRepository projectsRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value = "/projects", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Project> query(Principal currentUser, @RequestParam(required = false) Integer owner) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return owner == null ? projectsRepository.query() : projectsRepository.queryByOwnerId(owner);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}", method = RequestMethod.GET)
  @ResponseBody
  public Project get(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return projectsRepository.getProjectById(projectId);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects", method = RequestMethod.POST)
  @ResponseBody
  public Project create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody Project body) {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    Project Project = projectsRepository.create(user, body.getName(), body.getDescription(), body.getTrialverseId());
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", request.getRequestURL() + "/");
    return Project;
  }

  @RequestMapping(value = "/projects/{projectId}", method = RequestMethod.POST)
  @ResponseBody
  public Project update(Principal currentUser, @PathVariable Integer projectId, @RequestBody Project body) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Project project = projectsRepository.getProjectById(projectId);
    if (!project.getOwner().getUsername().equals(currentUser.getName())) {
      throw new MethodNotAllowedException();
    }
    return projectsRepository.update(body);
  }
}
