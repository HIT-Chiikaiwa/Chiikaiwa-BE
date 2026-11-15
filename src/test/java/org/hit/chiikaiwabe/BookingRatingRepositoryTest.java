package org.hit.chiikaiwabe;

import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.config.import=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"
}, showSql = false)
@ContextConfiguration(classes = BookingRatingRepositoryTest.Config.class)
class BookingRatingRepositoryTest {
    @Configuration
    @EntityScan(basePackageClasses = BookingRating.class)
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = BookingRatingRepository.class,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                    classes = BookingRatingRepository.class))
    static class Config { }

    @Autowired TestEntityManager em;
    @Autowired BookingRatingRepository repository;

    @Test
    void readsOnlyRequestedBookingsAndCurrentRatersScores() {
        User creator = account("creator");
        User invitee = account("invitee");
        OfflineBooking first = booking(creator);
        OfflineBooking second = booking(creator);
        OfflineBooking unrelated = booking(creator);
        saveRating(first, creator, invitee, 2);
        saveRating(first, invitee, creator, 5);
        saveRating(second, creator, invitee, 4);
        saveRating(unrelated, creator, invitee, 1);
        em.flush();
        em.clear();

        assertThat(repository.findByRaterIdAndBookingIds(creator.getId(), List.of(first.getId(), second.getId())))
                .extracting(BookingRating::getScore).containsExactlyInAnyOrder(2, 4);
        assertThat(repository.findByRaterIdAndBookingIds(invitee.getId(), List.of(first.getId(), second.getId())))
                .extracting(BookingRating::getScore).containsExactly(5);
        assertThat(repository.findByBookingIdAndRaterId(first.getId(), creator.getId()))
                .get().extracting(BookingRating::getScore).isEqualTo(2);
        assertThat(repository.findByBookingIdAndRaterId(second.getId(), invitee.getId())).isEmpty();
    }

    private User account(String name) {
        return em.persist(User.builder().username(name).password("test-hash").firstName(name).lastName("Test")
                .gender("MALE").trustScore(0.0).role(Role.USER).userstatus(UserStatus.ACTIVE).deleteFlag(false).build());
    }

    private OfflineBooking booking(User creator) {
        return em.persist(OfflineBooking.builder().creator(creator).status(BookingStatus.COMPLETED)
                .scheduledAt(LocalDateTime.now().minusDays(1)).locationName("Cafe").locationAddress("Address")
                .locationDistrict("District").locationCity("City").build());
    }

    private void saveRating(OfflineBooking booking, User rater, User ratedUser, int score) {
        em.persist(BookingRating.builder().booking(booking).rater(rater).ratedUser(ratedUser).score(score).build());
    }
}
