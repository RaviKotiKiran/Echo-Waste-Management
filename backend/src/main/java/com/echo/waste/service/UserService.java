package com.echo.waste.service;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class UserService {
 private final UserRepository users;private final PasswordEncoder encoder;private final com.echo.waste.repository.WasteRequestRepository requests;private final com.echo.waste.repository.AssignmentRepository assignments;private final com.echo.waste.repository.FeedbackRepository feedback;
 public UserService(UserRepository users,PasswordEncoder encoder,com.echo.waste.repository.WasteRequestRepository requests,com.echo.waste.repository.AssignmentRepository assignments,com.echo.waste.repository.FeedbackRepository feedback){this.users=users;this.encoder=encoder;this.requests=requests;this.assignments=assignments;this.feedback=feedback;}
 @Transactional(readOnly=true) public List<UserView> all(){return users.findAll().stream().map(this::view).toList();}
 @Transactional(readOnly=true) public UserView get(long id){return view(find(id));}
 @Transactional public UserView update(long id,UserUpdate input,AppUser actor){if(actor.getRole()!=UserRole.ADMIN&&!actor.getId().equals(id))throw new ApiException(HttpStatus.FORBIDDEN,"You can only update your own profile");AppUser user=find(id);user.setFullName(input.fullName().trim());user.setPhone(clean(input.phone()));user.setAddress(clean(input.address()));return view(user);}
 @Transactional public void delete(long id){AppUser user=find(id);if(user.getRole()==UserRole.ADMIN)throw new ApiException(HttpStatus.CONFLICT,"Administrator accounts cannot be deleted here");if(requests.existsByUserId(id)||assignments.existsByCollectorId(id)||feedback.existsByUserId(id))throw new ApiException(HttpStatus.CONFLICT,"This account has collection history and cannot be deleted");users.delete(user);}
 @Transactional public UserView createCollector(CollectorCreate in){String email=in.email().trim().toLowerCase(java.util.Locale.ROOT);if(users.existsByEmailIgnoreCase(email))throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");return view(users.save(new AppUser(in.fullName().trim(),email,encoder.encode(in.password()),clean(in.phone()),clean(in.address()),UserRole.COLLECTOR)));}
 @Transactional(readOnly=true) public List<UserView> collectors(){return users.findByRole(UserRole.COLLECTOR).stream().map(this::view).toList();}
 private AppUser find(long id){return users.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found"));}
 private UserView view(AppUser u){return new UserView(u.getId(),u.getFullName(),u.getEmail(),u.getPhone(),u.getAddress(),u.getRole(),u.getCreatedAt());}
 private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
}
