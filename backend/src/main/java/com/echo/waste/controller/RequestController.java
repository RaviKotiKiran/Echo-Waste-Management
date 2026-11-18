package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/requests")
public class RequestController {
 private final RequestService requests;public RequestController(RequestService requests){this.requests=requests;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @org.springframework.security.access.prepost.PreAuthorize("hasRole('CITIZEN')") public RequestView create(@Valid @RequestBody CreateRequest in,@AuthenticationPrincipal AppUser actor){return requests.create(in,actor);}
 @GetMapping public List<RequestView> list(@AuthenticationPrincipal AppUser actor){return switch(actor.getRole()){case ADMIN->requests.all();case COLLECTOR->requests.collectorTasks(actor);case CITIZEN->requests.mine(actor);};}
 @GetMapping("/my-requests") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CITIZEN')") public List<RequestView> mine(@AuthenticationPrincipal AppUser actor){return requests.mine(actor);}
 @GetMapping("/{id}") public RequestView get(@PathVariable long id,@AuthenticationPrincipal AppUser actor){return requests.get(id,actor);}
 @PutMapping("/{id}/cancel") @org.springframework.security.access.prepost.PreAuthorize("hasRole('CITIZEN')") public RequestView cancel(@PathVariable long id,@AuthenticationPrincipal AppUser actor){return requests.cancel(id,actor);}
}
