package com.echo.waste.service;
import com.echo.waste.dto.ApiDtos.Dashboard;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.RequestStatus;
import com.echo.waste.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class DashboardService {
 private final WasteRequestRepository requests;private final UserRepository users;private final AssignmentRepository assignments;
 public DashboardService(WasteRequestRepository requests,UserRepository users,AssignmentRepository assignments){this.requests=requests;this.users=users;this.assignments=assignments;}
 public Dashboard admin(){return new Dashboard(users.count(),users.countByRole(com.echo.waste.enums.UserRole.COLLECTOR),requests.count(),requests.countByStatus(RequestStatus.PENDING),requests.countByStatus(RequestStatus.APPROVED),requests.countByStatus(RequestStatus.ASSIGNED),requests.countByStatus(RequestStatus.IN_PROGRESS),requests.countByStatus(RequestStatus.COLLECTED),requests.countByStatus(RequestStatus.CANCELLED));}
 @Transactional(readOnly=true) public Dashboard citizen(AppUser u){var list=requests.findByUserIdOrderByCreatedAtDesc(u.getId());return dashboard(1,0,list.size(),list,0);}
 @Transactional(readOnly=true) public Dashboard collector(AppUser u){var list=assignments.findByCollectorIdOrderByAssignedAtDesc(u.getId()).stream().map(a->a.getRequest()).toList();return dashboard(0,1,list.size(),list,assignments.countByCollectorId(u.getId()));}
 private Dashboard dashboard(long userCount,long collectorCount,long total,java.util.List<com.echo.waste.entity.WasteRequest> list,long assigned){return new Dashboard(userCount,collectorCount,total,count(list,RequestStatus.PENDING),count(list,RequestStatus.APPROVED),count(list,RequestStatus.ASSIGNED),count(list,RequestStatus.IN_PROGRESS),count(list,RequestStatus.COLLECTED),count(list,RequestStatus.CANCELLED));}
 private long count(java.util.List<com.echo.waste.entity.WasteRequest> list,RequestStatus status){return list.stream().filter(r->r.getStatus()==status).count();}
}
