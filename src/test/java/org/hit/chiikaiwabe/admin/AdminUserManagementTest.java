package org.hit.chiikaiwabe.admin;

import org.hit.chiikaiwabe.controller.UserController;
import org.hit.chiikaiwabe.controller.AdminUserController;
import org.hit.chiikaiwabe.repository.AdminActionLogRepository;
import org.hit.chiikaiwabe.repository.UserReportRepository;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationFullRequestDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.domain.mapper.UserMapper;
import org.hit.chiikaiwabe.exception.GlobalExceptionHandler;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.UserService;
import org.hit.chiikaiwabe.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(AdminUserManagementTest.Config.class)
class AdminUserManagementTest {
  @Configuration
  @EnableMethodSecurity
  static class Config {
    @Bean UserRepository userRepository() { return mock(UserRepository.class); }
    @Bean UserMapper userMapper() { return Mappers.getMapper(UserMapper.class); }
    @Bean AdminActionLogRepository actionLogs() { return mock(AdminActionLogRepository.class); }
    @Bean UserReportRepository reports() { return mock(UserReportRepository.class); }
    @Bean UserService userService(UserRepository repository, UserMapper mapper,
        AdminActionLogRepository logs, UserReportRepository reports) {
      return new UserServiceImpl(repository, mapper, logs, reports);
    }
    @Bean AdminUserController adminController(UserService service) { return new AdminUserController(service); }
    @Bean UserController userController(UserService service) { return new UserController(service); }
  }

  @Autowired UserController controller;
  @Autowired AdminUserController adminController;
  @Autowired AdminActionLogRepository actionLogs;
  @Autowired UserService service;
  @Autowired UserRepository repository;
  MockMvc mvc;
  User target;

  @BeforeEach void setup() {
    reset(repository, actionLogs);
    signIn(Role.ADMIN);
    target = User.builder().id("user-1").username("alice").password("secret-hash")
        .email("alice@example.com").firstName("Alice").lastName("Nguyen")
        .role(Role.USER).userstatus(UserStatus.ACTIVE).build();
    when(repository.findById("user-1")).thenReturn(Optional.of(target));
    when(repository.findForAdminUpdate("user-1")).thenReturn(Optional.of(target));
    when(repository.getReferenceById("actor")).thenReturn(User.builder().id("actor").build());
    mvc = MockMvcBuilders.standaloneSetup(controller, adminController)
        .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
        .setControllerAdvice(new GlobalExceptionHandler(new StaticMessageSource())).build();
  }

  @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

  void signIn(Role role) {
    UserPrincipal principal = UserPrincipal.create(User.builder().id("actor").username("actor")
        .role(role).userstatus(UserStatus.ACTIVE).build());
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
  }

  @Test void adminCanViewDetailWithoutPassword() throws Exception {
    mvc.perform(get("/api/v1/user/user-1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("user-1"))
        .andExpect(jsonPath("$.data.userstatus").value("ACTIVE"))
        .andExpect(jsonPath("$.data.password").doesNotExist());
  }

  @Test void listSupportsKeywordPaginationAndEntityPropertySorting() throws Exception {
    when(repository.searchUsers(eq("Alice"), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable pageable = invocation.getArgument(1);
      assertThat(pageable.getPageNumber()).isEqualTo(1);
      assertThat(pageable.getPageSize()).isEqualTo(5);
      assertThat(pageable.getSort().getOrderFor("firstName").getDirection()).isEqualTo(Sort.Direction.ASC);
      return new PageImpl<>(List.of(target), pageable, 6);
    });
    mvc.perform(get("/api/v1/user").param("keyword", " Alice ").param("pageNum", "2")
            .param("pageSize", "5").param("sortBy", "firstName").param("isAscending", "true"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].id").value("user-1"))
        .andExpect(jsonPath("$.data.meta.totalElements").value(6))
        .andExpect(jsonPath("$.data.meta.pageNum").value(2));
  }

  @Test void emptyListReturnsMetadataAndEmptyItems() {
    when(repository.searchUsers(eq(""), any(Pageable.class))).thenReturn(Page.empty());
    var result = service.getCustomers(new PaginationFullRequestDto());
    assertThat(result.getItems()).isEmpty();
    assertThat(result.getMeta().getTotalElements()).isZero();
  }

  @Test void adminCanLockAndUnlock() throws Exception {
    mvc.perform(patch("/api/v1/user/user-1/lock")).andExpect(status().isOk());
    assertThat(target.getUserstatus()).isEqualTo(UserStatus.LOCKED);
    mvc.perform(patch("/api/v1/user/user-1/unlock")).andExpect(status().isOk());
    assertThat(target.getUserstatus()).isEqualTo(UserStatus.ACTIVE);
    verify(repository, times(2)).save(target);
  }

  @Test void repeatedLockAndUnlockAreIdempotent() {
    service.lockUser("user-1");
    service.lockUser("user-1");
    verify(repository).save(target);
    service.unlockUser("user-1");
    service.unlockUser("user-1");
    verify(repository, times(2)).save(target);
  }

  @Test void regularUserCannotReadOrManageOtherUsers() throws Exception {
    signIn(Role.USER);
    mvc.perform(get("/api/v1/user")).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/user/user-1")).andExpect(status().isForbidden());
    mvc.perform(patch("/api/v1/user/user-1/lock")).andExpect(status().isForbidden());
    mvc.perform(patch("/api/v1/user/user-1/unlock")).andExpect(status().isForbidden());
    verifyNoInteractions(repository);
  }

  @Test void serviceAlsoChecksAdminAuthority() {
    signIn(Role.USER);
    assertThatThrownBy(() -> service.lockUser("user-1"))
        .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    verifyNoInteractions(repository);
  }

  @Test void regularUserCanStillReadOwnCurrentAccount() throws Exception {
    signIn(Role.USER);
    when(repository.getUser(any())).thenReturn(target);
    mvc.perform(get("/api/v1/user/current")).andExpect(status().isOk());
  }

  @Test void cannotLockAdminIncludingSelf() throws Exception {
    target.setRole(Role.ADMIN);
    mvc.perform(patch("/api/v1/user/user-1/lock")).andExpect(status().isForbidden());
    mvc.perform(patch("/api/v1/user/user-1/unlock")).andExpect(status().isForbidden());
    verify(repository, never()).save(any());
  }

  @Test void cannotLockOrActivateUnverifiedAccount() throws Exception {
    target.setUserstatus(UserStatus.UNVERIFIED);
    mvc.perform(patch("/api/v1/user/user-1/lock")).andExpect(status().isBadRequest());
    mvc.perform(patch("/api/v1/user/user-1/unlock")).andExpect(status().isBadRequest());
    verify(repository, never()).save(any());
  }

  @Test void deletedUsersAreNotManageableOrVisible() throws Exception {
    target.setDeleteFlag(true);
    mvc.perform(get("/api/v1/user/user-1")).andExpect(status().isNotFound());
    mvc.perform(patch("/api/v1/user/user-1/lock")).andExpect(status().isNotFound());
    mvc.perform(patch("/api/v1/user/user-1/unlock")).andExpect(status().isNotFound());
    verify(repository, never()).save(any());
  }

  @Test void missingUserReturns404() throws Exception {
    mvc.perform(get("/api/v1/user/missing")).andExpect(status().isNotFound());
    mvc.perform(patch("/api/v1/user/missing/lock")).andExpect(status().isNotFound());
  }
  @Test void newAdminApiUsesZeroBasedPagination() throws Exception {
    when(repository.searchUsers(eq(""), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable page = invocation.getArgument(1);
      assertThat(page.getPageNumber()).isZero();
      assertThat(page.getPageSize()).isEqualTo(20);
      return new PageImpl<>(List.of(target), page, 1);
    });
    mvc.perform(get("/api/v1/admin/users")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.meta.pageNum").value(0))
        .andExpect(jsonPath("$.data.items[0].password").doesNotExist());
  }

  @Test void newAdminApiRequiresReason() throws Exception {
    mvc.perform(patch("/api/v1/admin/users/user-1/lock").contentType("application/json")
        .content("{\"reason\":\"  \"}")).andExpect(status().isBadRequest());
    mvc.perform(patch("/api/v1/admin/users/user-1/lock")).andExpect(status().isBadRequest());
    mvc.perform(patch("/api/v1/admin/users/user-1/lock").contentType("application/json")
        .content("{broken")).andExpect(status().isBadRequest());
    verify(repository, never()).save(any());
  }

  @Test void newAdminApiWritesAuditWithCurrentActorAndReason() throws Exception {
    mvc.perform(patch("/api/v1/admin/users/user-1/lock").contentType("application/json")
        .content("{\"reason\":\"Spam confirmed\"}")).andExpect(status().isOk());
    var captor = org.mockito.ArgumentCaptor.forClass(org.hit.chiikaiwabe.domain.entity.AdminActionLog.class);
    verify(actionLogs).save(captor.capture());
    assertThat(captor.getValue().getActor().getId()).isEqualTo("actor");
    assertThat(captor.getValue().getReason()).isEqualTo("Spam confirmed");
    assertThat(captor.getValue().getPreviousStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(captor.getValue().getNewStatus()).isEqualTo(UserStatus.LOCKED);
  }

  @Test void newAdminApiRejectsUserRoleAndInvalidPagination() throws Exception {
    mvc.perform(get("/api/v1/admin/users").param("page", "-1")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/admin/users").param("size", "101")).andExpect(status().isBadRequest());
    signIn(Role.USER);
    mvc.perform(get("/api/v1/admin/users")).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/admin/users/user-1")).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/admin/users/user-1/actions")).andExpect(status().isForbidden());
    mvc.perform(patch("/api/v1/admin/users/user-1/lock").contentType("application/json")
        .content("{\"reason\":\"Spam\"}")).andExpect(status().isForbidden());
  }

}
