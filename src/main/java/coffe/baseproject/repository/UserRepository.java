package coffe.baseproject.repository;

import coffe.baseproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = :userName")
    User findByUsername(@Param("userName") String userName);

    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    User findByUsernameOrEmail(@Param("login") String login);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
