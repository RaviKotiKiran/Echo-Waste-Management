package com.echo.waste.repository;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<AppUser,Long>{Optional<AppUser> findByEmailIgnoreCase(String email);boolean existsByEmailIgnoreCase(String email);List<AppUser> findByRole(UserRole role);long countByRole(UserRole role);}
