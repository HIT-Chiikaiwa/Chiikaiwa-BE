package org.hit.chiikaiwabe.admin;

import org.hit.chiikaiwabe.constant.SortByDataConstant;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationFullRequestDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.util.PaginationUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.config.import=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
}, showSql = false)
@ContextConfiguration(classes = AdminUserRepositoryTest.Config.class)
class AdminUserRepositoryTest {
  @Configuration
  @EntityScan(basePackageClasses = User.class)
  @EnableJpaAuditing
  @EnableJpaRepositories(basePackageClasses = UserRepository.class,
      includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserRepository.class))
  static class Config { }

  @Autowired UserRepository repository;

  @BeforeEach void setup() {
    repository.save(account("alice", "Alice", "Nguyen", "alice@example.com", "0123456789", false));
    repository.save(account("bob", "Bob", "Tran", "bob@example.com", "0987654321", false));
    repository.save(account("deleted", "Deleted", "User", "deleted@example.com", "0111111111", true));
    repository.flush();
  }

  User account(String username, String first, String last, String email, String phone, boolean deleted) {
    return User.builder().username(username).password("hash").firstName(first).lastName(last)
        .email(email).phone(phone).gender("MALE").trustScore(100.0).role(Role.USER)
        .userstatus(UserStatus.ACTIVE).deleteFlag(deleted).build();
  }

  @Test void searchesUsernameEmailPhoneAndFullNameCaseInsensitively() {
    for (String keyword : new String[]{"ALICE", "alice@EXAMPLE.com", "012345", "NGUYEN ALICE"}) {
      assertThat(repository.searchUsers(keyword, PageRequest.of(0, 10)).getContent())
          .extracting(User::getUsername).containsExactly("alice");
    }
  }

  @Test void excludesDeletedUsersEvenWhenSearchingByExactUsername() {
    assertThat(repository.searchUsers("deleted", PageRequest.of(0, 10))).isEmpty();
    assertThat(repository.searchUsers("", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
  }

  @Test void supportsEachDocumentedSortPropertyAndPagination() {
    for (String sortBy : new String[]{"firstName", "lastName", "lastModifiedDate", "createdDate"}) {
      PaginationFullRequestDto request = new PaginationFullRequestDto();
      request.setSortBy(sortBy);
      request.setPageSize(1);
      var result = repository.searchUsers("", PaginationUtil.buildPageable(request, SortByDataConstant.USER));
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getTotalPages()).isEqualTo(2);
    }
  }
}
