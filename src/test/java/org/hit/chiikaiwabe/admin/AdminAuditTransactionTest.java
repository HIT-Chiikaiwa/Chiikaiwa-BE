package org.hit.chiikaiwabe.admin;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.mapper.UserMapper;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.UserService;
import org.hit.chiikaiwabe.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.config.import=", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"}, showSql = false)
@ContextConfiguration(classes = AdminAuditTransactionTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminAuditTransactionTest {
    @Configuration
    @EntityScan(basePackageClasses = User.class)
    @EnableJpaRepositories(basePackageClasses = UserRepository.class)
    @EnableJpaAuditing
    @EnableMethodSecurity
    static class Config {
        @Bean UserMapper userMapper() { return Mappers.getMapper(UserMapper.class); }
        @Bean UserService userService(UserRepository users, UserMapper mapper, AdminActionLogRepository logs, UserReportRepository reports) {
            return new UserServiceImpl(users, mapper, logs, reports);
        }
    }

    @Autowired UserRepository users;
    @SpyBean AdminActionLogRepository logs;
    @Autowired UserReportRepository reports;
    @Autowired UserService service;
    User admin, target;

    @BeforeEach void setup() {
        admin = users.saveAndFlush(account("admin", Role.ADMIN));
        target = users.saveAndFlush(account("target", Role.USER));
        var principal = UserPrincipal.create(admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach void cleanup() {
        reset(logs);
        logs.deleteAll(); reports.deleteAll(); users.deleteAll();
        SecurityContextHolder.clearContext();
    }

    User account(String name, Role role) {
        return User.builder().username(name).firstName(name).lastName("Test").password("hash")
                .gender("MALE").trustScore(100.0).userstatus(UserStatus.ACTIVE).role(role).build();
    }

    @Test void statusAndAuditPersistWithActorReasonReportAndTime() {
        UserReport report = reports.saveAndFlush(UserReport.builder().reporter(admin).reported(target)
                .reason(ReportReason.SPAM).build());
        service.lockUser(target.getId(), new AdminAccountActionRequestDto("Spam confirmed", report.getId()));
        assertThat(users.findById(target.getId()).orElseThrow().getUserstatus()).isEqualTo(UserStatus.LOCKED);
        var history = service.getAdminActions(target.getId(), new AdminUserSearchRequestDto());
        assertThat(history.getItems()).hasSize(1);
        var action = history.getItems().get(0);
        assertThat(action.actorId()).isEqualTo(admin.getId());
        assertThat(action.targetId()).isEqualTo(target.getId());
        assertThat(action.action()).isEqualTo(AdminAction.LOCK);
        assertThat(action.previousStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(action.newStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(action.createdDate()).isNotNull();
        assertThat(action.reason()).isEqualTo("Spam confirmed");
        assertThat(action.reportId()).isEqualTo(report.getId());
        service.unlockUser(target.getId(), new AdminAccountActionRequestDto("Review completed", null));
        assertThat(users.findById(target.getId()).orElseThrow().getUserstatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(service.getAdminActions(target.getId(), new AdminUserSearchRequestDto()).getItems()).hasSize(2);
    }

    @Test void auditFailureRollsBackAccountStatus() {
        doThrow(new IllegalStateException("audit storage unavailable")).when(logs).save(any(AdminActionLog.class));
        assertThatThrownBy(() -> service.lockUser(target.getId(), new AdminAccountActionRequestDto("Spam", null)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(users.findById(target.getId()).orElseThrow().getUserstatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(logs.findByTarget_Id(target.getId(), PageRequest.of(0, 20))).isEmpty();
    }

    @Test void repeatedActionDoesNotDuplicateHistory() {
        var request = new AdminAccountActionRequestDto("Spam", null);
        service.lockUser(target.getId(), request);
        service.lockUser(target.getId(), request);
        assertThat(logs.findByTarget_Id(target.getId(), PageRequest.of(0, 20)).getTotalElements()).isEqualTo(1);
    }

    @Test void reportMustConcernTargetAccount() {
        UserReport report = reports.saveAndFlush(UserReport.builder().reporter(target).reported(admin)
                .reason(ReportReason.SPAM).build());
        assertThatThrownBy(() -> service.lockUser(target.getId(), new AdminAccountActionRequestDto("Spam", report.getId())))
                .isInstanceOf(InvalidException.class);
        assertThat(users.findById(target.getId()).orElseThrow().getUserstatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(logs.count()).isZero();
    }
}
