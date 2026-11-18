package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.AppUser;
import com.echo.waste.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/feedback")
public class FeedbackController {
 private final FeedbackService feedback;public FeedbackController(FeedbackService feedback){this.feedback=feedback;}
 @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public java.util.List<FeedbackView> all(){return feedback.all();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @org.springframework.security.access.prepost.PreAuthorize("hasRole('CITIZEN')") public FeedbackView create(@Valid @RequestBody FeedbackCreate in,@AuthenticationPrincipal AppUser actor){return feedback.create(in,actor);}
}
