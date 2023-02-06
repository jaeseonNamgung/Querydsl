package com.study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberDto;
import com.study.querydsl.dto.QMemberDto;
import com.study.querydsl.dto.UserDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import com.study.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //초기화
        em.flush();
        em.clear();
    }


      @Test
      void simpleProjection(){
          List<String> result = queryFactory
                  .select(member.username)
                  .from(member)
                  .fetch();
          for (String s : result) {
              System.out.println("s = " + s);
          }
      }

      @Test
      void tupleProjection(){
          List<Tuple> result = queryFactory
                  .select(member.username, member.age)
                  .from(member)
                  .fetch();
          for (Tuple tuple : result) {
              String username = tuple.get(member.username);
              Integer age = tuple.get(member.age);
              System.out.println("username = " + username);
              System.out.println("age = " + age);
          }
      }

      @Test
      void findDtoByJPQL(){
          List<MemberDto> result = em.createQuery("select " +
                          "new com.study.querydsl.dto.MemberDto(" +
                          "m.username, m.age)" +
                          "from Member m", MemberDto.class)
                  .getResultList();
          for (MemberDto memberDto : result) {
              System.out.println("memberDto = " + memberDto);
          }
      }

      @Test
      void findDtoBySetter(){
          List<MemberDto> result = queryFactory
                  .select(Projections.bean(MemberDto.class,
                          member.username,
                          member.age))
                  .from(member)
                  .fetch();
          for (MemberDto memberDto : result) {
              System.out.println("memberDto = " + memberDto);
          }
      }
      @Test
      void findDtoByField(){
          List<MemberDto> result = queryFactory
                  .select(Projections.fields(MemberDto.class,
                          member.username,
                          member.age))
                  .from(member)
                  .fetch();
          for (MemberDto memberDto : result) {
              System.out.println("memberDto = " + memberDto);
          }
      }
      @Test
      void findDtoByConstructor(){
          List<MemberDto> result = queryFactory
                  .select(Projections.constructor(MemberDto.class,
                          member.username,
                          member.age))
                  .from(member)
                  .fetch();
          for (MemberDto memberDto : result) {
              System.out.println("memberDto = " + memberDto);
          }
      }
    @Test
    void findUserDtoByField(){
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();
        for (UserDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findUserDtoByFiledAndSubquery(){
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();
        for (UserDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findDtoByQueryProjection(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

}

