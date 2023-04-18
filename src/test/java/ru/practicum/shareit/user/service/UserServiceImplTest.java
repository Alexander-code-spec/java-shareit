package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private final EntityManager entityManager;
    private final UserService userService;
    private UserDto userDto;

    @BeforeEach
    void initialize() {
        userDto = saveUserDto("Jack", "jack@mail.com");
    }

    private UserDto saveUserDto(String name, String email) {
        return new UserDto(null, name, email);
    }

    private void addUsers() {
        userService.save(saveUserDto("John", "john@mail.com"));
        userService.save(saveUserDto("Bobby", "bobby@mail.com"));
        userService.save(saveUserDto("Clare", "clare@mail.com"));
    }

    @Test
    void emailExceptionTest() {
        userService.save(saveUserDto("Jack", "jack@mail.com"));
        Exception exception = assertThrows(DataIntegrityViolationException.class,
                () -> userService.save(saveUserDto("Jack", "jack@mail.com")));
        assertEquals("could not execute statement; SQL [n/a]; constraint [null]; nested exception " +
                "is org.hibernate.exception.ConstraintViolationException: could not execute statement", exception.getMessage());
    }

    @Test
    void nullTest() {
        UserDto dto = userService.save(saveUserDto("Jack", "jack@mail.com"));
        assertNotEquals(null, dto);
    }

    @Test
    void getTest() {
        userService.save(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        UserDto userDtoFrom = userService.get(user.getId());
        assertThat(userDtoFrom.getEmail(), equalTo(user.getEmail()));
        assertThat(userDtoFrom.getName(), equalTo(user.getName()));
        assertThat(userDtoFrom.getId(), equalTo(user.getId()));
    }

    @Test
    void saveTest() {
        userService.save(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getId(), notNullValue());
    }

    @Test
    void updateTest() {
        userService.save(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        UserDto dto = saveUserDto("Flore", "flore@mail.com");
        userService.update(dto, user.getId());
        User updatedUser = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.id = :id",
                        User.class)
                .setParameter("id", user.getId())
                .getSingleResult();
        assertThat(updatedUser.getEmail(), equalTo(dto.getEmail()));
        assertThat(updatedUser.getName(), equalTo(dto.getName()));
        assertThat(updatedUser.getId(), notNullValue());
    }

    @Test
    void deleteTest() {
        addUsers();
        List<User> usersBefore = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(usersBefore.size(), equalTo(3));
        userService.delete(usersBefore.get(0).getId());
        List<User> usersAfter = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(usersAfter.size(), equalTo(2));
    }

    @Test
    void getAllTest() {
        addUsers();
        List<User> users = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(users.size(), equalTo(3));
    }
}
