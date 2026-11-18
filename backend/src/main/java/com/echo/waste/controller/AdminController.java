package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.repository.*;
import com.echo.waste.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 private final RequestService requestService;private final UserService userService;private final FeedbackService feedback;private final DashboardService dashboards;private final ServiceIssueService issues;
 public AdminController(RequestService requestService,UserService userService,FeedbackService feedback,DashboardService dashboards,ServiceIssueService issues){this.requestService=requestService;this.userService=userService;this.feedback=feedback;this.dashboards=dashboards;this.issues=issues;}
 @GetMapping("/dashboard") public Dashboard dashboard(){return dashboards.admin();}
 @GetMapping("/requests") public List<RequestView> requests(){return requestService.all();}
 @PutMapping("/requests/{id}/approve") public RequestView approve(@PathVariable long id,@RequestBody Decision in){return requestService.approve(id,in);}
 @PutMapping("/requests/{id}/reject") public RequestView reject(@PathVariable long id,@RequestBody Decision in){return requestService.reject(id,in);}
 @PutMapping("/requests/{id}/assign") public RequestView assign(@PathVariable long id,@Valid @RequestBody Assign in){return requestService.assign(id,in);}
 @GetMapping("/users") public List<UserView> users(){return userService.all();}
 @GetMapping("/collectors") public List<UserView> collectors(){return userService.collectors();}
 @PostMapping("/collectors") public UserView createCollector(@Valid @RequestBody CollectorCreate in){return userService.createCollector(in);}
 @DeleteMapping("/users/{id}") @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void deleteUser(@PathVariable long id){userService.delete(id);}
 @GetMapping("/feedback") public List<FeedbackView> feedback(){return feedback.all();}
 @GetMapping("/issues") public List<IssueView> issues(){return issues.all();}
 @PatchMapping("/issues/{id}/status") public IssueView updateIssue(@PathVariable long id,@Valid @RequestBody IssueStatusChange in){return issues.updateStatus(id,in);}
}
