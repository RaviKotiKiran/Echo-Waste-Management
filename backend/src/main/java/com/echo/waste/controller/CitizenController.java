package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.Dashboard;
import com.echo.waste.dto.ApiDtos.CalendarView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.exception.ApiException;
import com.echo.waste.service.DashboardService;
import com.echo.waste.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
@RestController @RequestMapping("/api/citizen") @PreAuthorize("hasRole('CITIZEN')")
public class CitizenController {
 private final DashboardService dashboards;private final RequestService requests;public CitizenController(DashboardService dashboards,RequestService requests){this.dashboards=dashboards;this.requests=requests;}
 @GetMapping("/dashboard") public Dashboard dashboard(@AuthenticationPrincipal AppUser user){return dashboards.citizen(user);}
 @GetMapping("/calendar") public CalendarView calendar(@RequestParam String month,@AuthenticationPrincipal AppUser user){try{return requests.calendar(user,YearMonth.parse(month));}catch(DateTimeParseException invalid){throw new ApiException(HttpStatus.BAD_REQUEST,"Month must use YYYY-MM format.");}}
}
