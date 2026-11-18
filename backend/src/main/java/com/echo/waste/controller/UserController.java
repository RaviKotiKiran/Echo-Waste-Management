package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.UserUpdate;
import com.echo.waste.dto.ApiDtos.UserView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users")
public class UserController {
 private final UserService users;public UserController(UserService users){this.users=users;}
 @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public java.util.List<UserView> all(){return users.all();}
 @GetMapping("/{id}") public UserView get(@PathVariable long id,@AuthenticationPrincipal AppUser actor){if(!actor.getId().equals(id)&&actor.getRole()!=com.echo.waste.enums.UserRole.ADMIN)throw new com.echo.waste.exception.ApiException(org.springframework.http.HttpStatus.FORBIDDEN,"You can only view your own profile");return users.get(id);}
 @PutMapping("/{id}") public UserView update(@PathVariable long id,@Valid @RequestBody UserUpdate in,@AuthenticationPrincipal AppUser actor){return users.update(id,in,actor);}
 @DeleteMapping("/{id}") @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delete(@PathVariable long id){users.delete(id);}
}
