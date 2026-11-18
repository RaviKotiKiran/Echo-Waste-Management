package com.echo.waste.controller;

import com.echo.waste.dto.ApiDtos.IssueCreate;
import com.echo.waste.dto.ApiDtos.IssueView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.service.ServiceIssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@PreAuthorize("hasRole('CITIZEN')")
public class ServiceIssueController {
    private final ServiceIssueService service;

    public ServiceIssueController(ServiceIssueService service) { this.service = service; }

    @GetMapping
    public List<IssueView> mine(@AuthenticationPrincipal AppUser resident) { return service.mine(resident); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueView create(@Valid @RequestBody IssueCreate input, @AuthenticationPrincipal AppUser resident) {
        return service.create(input, resident);
    }
}
