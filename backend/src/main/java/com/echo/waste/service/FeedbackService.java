package com.echo.waste.service;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.*;
import com.echo.waste.enums.RequestStatus;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class FeedbackService {
 private final FeedbackRepository feedback;private final WasteRequestRepository requests;
 public FeedbackService(FeedbackRepository feedback,WasteRequestRepository requests){this.feedback=feedback;this.requests=requests;}
 @Transactional public FeedbackView create(FeedbackCreate in,AppUser actor){WasteRequest r=requests.findById(in.requestId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Request not found"));if(!r.getUser().getId().equals(actor.getId()))throw new ApiException(HttpStatus.FORBIDDEN,"You can only review your own request");if(r.getStatus()!=RequestStatus.COLLECTED)throw new ApiException(HttpStatus.CONFLICT,"Feedback is available after collection");if(feedback.existsByRequestId(r.getId()))throw new ApiException(HttpStatus.CONFLICT,"Feedback was already submitted for this request");return view(feedback.save(new Feedback(actor,r,in.rating(),in.comments())));}
 @Transactional(readOnly=true) public List<FeedbackView> all(){return feedback.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();}
 private FeedbackView view(Feedback f){return new FeedbackView(f.getId(),f.getUser().getId(),f.getUser().getFullName(),f.getRequest().getId(),f.getRating(),f.getComments(),f.getCreatedAt());}
}
