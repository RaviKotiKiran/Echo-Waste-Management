package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.RequestStatus;
import com.echo.waste.service.RequestService;
import com.echo.waste.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/collector") @PreAuthorize("hasRole('COLLECTOR')")
public class CollectorController {
 private final RequestService requests;private final DashboardService dashboards;public CollectorController(RequestService requests,DashboardService dashboards){this.requests=requests;this.dashboards=dashboards;}
 @GetMapping("/dashboard") public Dashboard dashboard(@AuthenticationPrincipal AppUser user){return dashboards.collector(user);}
 @GetMapping("/assignments") public List<RequestView> assignments(@AuthenticationPrincipal AppUser user){return requests.collectorTasks(user);}
 @PutMapping("/assignments/{id}/status") public RequestView status(@PathVariable long id,@RequestBody StatusChange in,@AuthenticationPrincipal AppUser user){return requests.updateCollectorStatus(id,in.status(),user);}
}
